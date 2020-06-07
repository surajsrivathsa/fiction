package org.ovgu.de.fiction.search;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.ovgu.de.fiction.feature.extraction.FeatureExtractorUtility;
import org.ovgu.de.fiction.model.TopKResults;
import org.ovgu.de.fiction.utils.FRConstants;
import org.ovgu.de.fiction.utils.FRGeneralUtils;


import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.CorrelationAttributeEval;
import weka.attributeSelection.GreedyStepwise;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.SMO;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.NumericToNominal;

import org.ovgu.de.fiction.utils.FRGeneralUtils;



public class InterpretSearchResults {

	public static  double SIMIL_TOPMATCH  = 0.70;//Deafult_VALUES_that_get_changed
	public static  double SIMIL_FAIRMATCH = 0.40;
	
	public InterpretSearchResults() {
		
	}
/**
 * @see - Create a instance-feature space with binning of values to get 5 'pseudo class lables' or 'pseudo clusters' 
 *  
 * Find out over only the result space:
 *  1) Features that are highly correlated with the class labels
 *  2) Feature Selection - Rank features in their discriminating power with regard to class labels, using Mutual Information, Entropy
 *  3) 
 *  
 *  Find out over entire space:
 *  3) Perform PCA of the entire Feature-Instance space to rank features
 * @param topKResults
 * @throws Exception 
 */
	public Map<String,Map<String,String>> performStatiscalAnalysis(TopKResults topKResults) throws Exception {
		Map<String, Map<String, double[]>> books = topKResults.getBooks();
		SortedMap<Double, String> results_topK = topKResults.getResults_topK();
		
		Map<Integer , TopKResults> searched_result_bins = createBinsModified(books,results_topK); // createBins(books,results_topK);
		writeBinsToFiles(searched_result_bins);
		Map<String,Map<String,String>> stats = getStatistics(FRGeneralUtils.getPropertyVal("file.results.arff"));
		//rankFeatures(FRGeneralUtils.getPropertyVal("file.results.arff"));
		
		Regression r = new Regression();
		r.runLR();
		
		return stats;
		
	}
	///////////////////////////////////////////////////////////
	public Map<Integer,String> performStatiscalAnalysisUsingRegression(TopKResults topKResults,int i, String flag) throws Exception {
		Map<String, Map<String, double[]>> books = topKResults.getBooks();
		SortedMap<Double, String> results_topK = topKResults.getResults_topK();
		int features_to_be_considered = 0;
		
		if(flag == FRConstants.SIMI_INCLUDE_TTR_NUMCHARS) 
			features_to_be_considered = 21;
		else
			features_to_be_considered = 51;
		
		if(flag == FRConstants.SIMI_EXCLUDE_TTR_NUMCHARS) {
			List<double []> searched_result_bins_regression = createBinsForRegressionLocal(books,results_topK, i, features_to_be_considered);
			Instances regression_instances = loadDatasetLocal(searched_result_bins_regression);
			return featureSelection_RegressionLocal(regression_instances);
		}
		else {
			List<double []> searched_result_bins_regression = createBinsForRegression(books,results_topK, i, features_to_be_considered);
			Instances regression_instances = loadDataset(searched_result_bins_regression);
			SortedMap<Integer, String> global_explanations = featureSelection_Regression(regression_instances);
			System.out.println(" ========================= ");
			System.out.println(global_explanations.toString());
			System.out.println(" ========================= ");
			
			List<double []> searched_result_bins_regression_local = createBinsForRegressionLocal(books,results_topK, i, features_to_be_considered);
			Instances regression_instances_local = loadDatasetLocal(searched_result_bins_regression_local);
			SortedMap<Integer, String> local_explanations =  featureSelection_RegressionLocal(regression_instances_local);
			
			SortedMap<Integer, String> combined_explanations = new TreeMap<Integer, String>();
			
			int local_feature_counter = 3;
			for(Entry<Integer, String> item: local_explanations.entrySet() ) 
			{
				if(item.getKey() <= 3) 
				{
					local_feature_counter-=1;
					combined_explanations.put(item.getKey(), item.getValue());
				}
		           if(local_feature_counter == 0) 
		        	   break;
		    }
			
			int global_feature_counter = 2;
			for(Entry<Integer, String> item: global_explanations.entrySet() ) 
			{
				if(item.getKey() <= 2) 
				{
					global_feature_counter-=1;
					combined_explanations.put(item.getKey()+3, item.getValue());
				}
		           if(global_feature_counter == 0) 
		        	   break;
		    }
			
			System.out.println("======= ======= Printing combined explanations =========== ======");
			for(Entry<Integer, String> item: combined_explanations.entrySet() ) 
			{
				System.out.println("Rank: " + item.getKey() + " Feature: " + item.getValue());
		    }
			return combined_explanations;
		}
		
	}
	
	private SortedMap<Integer, String> featureSelection_Regression(Instances dataset) throws Exception {
		SortedMap<Float,String> all_features =new TreeMap<Float,String>(Collections.reverseOrder());  
		SortedMap<Integer,String> reduced_features =new TreeMap<Integer,String>(); 
		int trainSize = (int) Math.round(dataset.numInstances() * 0.8);
		int testSize = dataset.numInstances() - trainSize;
		Instances train_dataset = new Instances(dataset, 0, trainSize);
		Instances test_dataset = new Instances(dataset, trainSize, testSize);
			
		LinearRegression lr = new LinearRegression();
		lr.setRidge(0.3); // Ridge value is set by hyper parameter tuning
		
		lr.buildClassifier(train_dataset);
		
		Evaluation evaluation = new Evaluation(test_dataset);
		evaluation.crossValidateModel(lr, dataset, 5, new Random(1));
		double rmse = evaluation.rootMeanSquaredError();
		System.out.println("The RMSE value on 5 fold cross validation is :" + rmse);
		System.out.print(lr.toString());
		
        String[] get_lines = lr.toString().split("\n");
        for(String line : get_lines) {
        	if(line.contains(" * ")) {
              String[] features = line.split("\\*");
        	  float key;
        	  String value = " ";
              for(int feature =0 ; feature < features.length; feature+=2) {
            	  key = Float.valueOf(features[feature]);
            	  value = features[feature+1].replace(" +", "");
            	  all_features.put(Math.abs(key), value);
              }
        	}
        }
        
        int count = 0;
	    System.out.println("\n\n The top features from regression are : ");
        
        for(Entry<Float, String> item: all_features.entrySet() ) {
           count+=1;
      	   reduced_features.put(count, item.getValue());
      	   System.out.println("Rank "+count+ " = " + item.getValue() );
           if(count == 5) break;
        }
        
        return reduced_features;
		
	}
	
	private List<double []> createBinsForRegression(Map<String, Map<String, double[]>> books, SortedMap<Double, String> results_topK,int iter, int features_to_be_considered) {
		List<double []> searched_result_bins = new ArrayList<double[]>();
		double weight = 0;

		for(Map.Entry<String, Map<String, double[]>> corpus: books.entrySet()) { // loop over all books of corpus
			  // create a global feature vector for a single book
			 String book = corpus.getKey();
			 
			 if(results_topK.containsValue(book)) {
			 	for(Map.Entry<Double, String> result: results_topK.entrySet()){//loop_over_all_chunks_of_a_given_book
			        if (Objects.equals(book, result.getValue())) {
			           weight = result.getKey();
			           //System.out.println("weight: " + weight);
			           break;
			        }
			 	}	
				 Map<String, double[]> bookChunks =  corpus.getValue();
				 	for(Map.Entry<String, double[]> chunks: bookChunks.entrySet()){//loop_over_all_chunks_of_a_given_book
				 		double[] final_chunk_vector = new double[FRConstants.FEATURE_NUMBER + 1];
				 		double[] chunk_vector = chunks.getValue();
				 		 for(int i=0;i<chunk_vector.length;i++)
				 		 {
				 			 if(i <= 20)
				 				final_chunk_vector[i] = chunk_vector[i] * FRConstants.CHUNK_WEIGHT/20;
				 			 else if (i == 21)
				 				final_chunk_vector[i] = chunk_vector[i] * FRConstants.CHAR_WEIGHT;
				 			else if (i == 22)
				 				final_chunk_vector[i] = chunk_vector[i] * FRConstants.NUMCHAR_WEIGHT;
				 			else if (i == 23)
				 				final_chunk_vector[i] = chunk_vector[i] * FRConstants.TTR_WEIGHT; 
				 			else if (i >23 && i <=33)
				 				final_chunk_vector[i] = chunk_vector[i] * FRConstants.GENRE_WEIGHT; 
				 			else 
				 				final_chunk_vector[i] = chunk_vector[i] * FRConstants.EMO_WEIGHT;
					 			//final_chunk_vector[i] = chunk_vector[i];
					 			//System.out.print("here" + final_chunk_vector + " " + chunk_vector[i]);
					 	 
				 		 
				 		 
				 		 
				 		 }
				 		final_chunk_vector[FRConstants.FEATURE_NUMBER] = weight;
				 		//System.out.println(final_chunk_vector);
				 		searched_result_bins.add(final_chunk_vector);
				 }		 
			 }
		}
		return searched_result_bins;
	}
	
	private Instances loadDataset(List<double []> bins) throws RuntimeException {
    	ArrayList<Attribute> attributes = new ArrayList<Attribute>();
    	for(int i=0; i< FRConstants.FEATURE_NUMBER; i++) {
    		attributes.add(new Attribute("Feature "+i));
    	}
    	attributes.add(new Attribute("Class label"));
    	Instances dataRaw = new Instances("Instances", attributes , FRConstants.FEATURE_NUMBER+1);
    	dataRaw.setClassIndex(FRConstants.FEATURE_NUMBER);

    	for (double[] a: bins) {
    	    dataRaw.add(new DenseInstance(1.0, a));
    	}
	    return dataRaw;
	}
	
	//@suraj: added below three function for only local features
	private SortedMap<Integer, String> featureSelection_RegressionLocal(Instances dataset) throws Exception {
		SortedMap<Float,String> all_features =new TreeMap<Float,String>(Collections.reverseOrder());  
		SortedMap<Integer,String> reduced_features =new TreeMap<Integer,String>(); 
		int trainSize = (int) Math.round(dataset.numInstances() * 0.8);
		int testSize = dataset.numInstances() - trainSize;
		Instances train_dataset = new Instances(dataset, 0, trainSize);
		Instances test_dataset = new Instances(dataset, trainSize, testSize);
			
		LinearRegression lr = new LinearRegression();
		lr.setRidge(0.3); // Ridge value is set by hyper parameter tuning
		
		lr.buildClassifier(train_dataset);
		
		Evaluation evaluation = new Evaluation(test_dataset);
		evaluation.crossValidateModel(lr, dataset, 5, new Random(1));
		double rmse = evaluation.rootMeanSquaredError();
		System.out.println("The RMSE value on 5 fold cross validation is :" + rmse);
		System.out.print(lr.toString());
		
        String[] get_lines = lr.toString().split("\n");
        for(String line : get_lines) {
        	if(line.contains(" * ")) {
              String[] features = line.split("\\*");
        	  float key;
        	  String value = " ";
              for(int feature =0 ; feature < features.length; feature+=2) {
            	  key = Float.valueOf(features[feature]);
            	  value = features[feature+1].replace(" +", "");
            	  all_features.put(Math.abs(key), value);
              }
        	}
        }
        
        int count = 0;
	    System.out.println("\n\n The top features from regression are : ");
        
        for(Entry<Float, String> item: all_features.entrySet() ) {
           count+=1;
      	   reduced_features.put(count, item.getValue());
      	   System.out.println("Rank "+count+ " = " + item.getValue() );
           if(count == 5) break;
        }
        
        return reduced_features;
		
	}
	
	private List<double []> createBinsForRegressionLocal(Map<String, Map<String, double[]>> books, SortedMap<Double, String> results_topK,int iter, int features_to_be_considered) {
		List<double []> searched_result_bins = new ArrayList<double[]>();
		double weight = 0;

		for(Map.Entry<String, Map<String, double[]>> corpus: books.entrySet()) { // loop over all books of corpus
			  // create a global feature vector for a single book
			 String book = corpus.getKey();
			 
			 if(results_topK.containsValue(book)) {
			 	for(Map.Entry<Double, String> result: results_topK.entrySet()){//loop_over_all_chunks_of_a_given_book
			        if (Objects.equals(book, result.getValue())) {
			           weight = result.getKey();
			           //System.out.println("weight: " + weight);
			           break;
			        }
			 	}	
				 Map<String, double[]> bookChunks =  corpus.getValue();
				 	for(Map.Entry<String, double[]> chunks: bookChunks.entrySet()){//loop_over_all_chunks_of_a_given_book
				 		double[] final_chunk_vector = new double[FRConstants.FEATURE_NUMBER_LOCAL + 1];
				 		double[] chunk_vector = chunks.getValue();
				 		 for(int i=0;i<FRConstants.FEATURE_NUMBER_LOCAL;i++)
				 		 {
					 			final_chunk_vector[i] = chunk_vector[i];
					 			//System.out.print("here" + final_chunk_vector + " " + chunk_vector[i]);				 		 
				 		 }
				 		final_chunk_vector[FRConstants.FEATURE_NUMBER_LOCAL] = weight;
				 		//System.out.println(" =========== printing the final chunk vector ===============");
				 		//System.out.println(Arrays.toString(final_chunk_vector));
				 		searched_result_bins.add(final_chunk_vector);
				 }		 
			 }
		}
		return searched_result_bins;
	}
	
	private Instances loadDatasetLocal(List<double []> bins) throws RuntimeException {
    	ArrayList<Attribute> attributes = new ArrayList<Attribute>();
    	for(int i=0; i< FRConstants.FEATURE_NUMBER_LOCAL; i++) {
    		attributes.add(new Attribute("Feature "+i));
    	}
    	attributes.add(new Attribute("Class label"));
    	Instances dataRaw = new Instances("Instances", attributes , FRConstants.FEATURE_NUMBER_LOCAL+1);
    	dataRaw.setClassIndex(FRConstants.FEATURE_NUMBER_LOCAL);

    	for (double[] a: bins) {
    	    dataRaw.add(new DenseInstance(1.0, a));
    	}
	    return dataRaw;
	}

	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	private Map<String,Map<String,String>> getStatistics(String ARFF_RESULTS_FILE) throws Exception {
		Map<String,Map<String,String>> stats = new HashMap<>();
		DataSource source = new DataSource(ARFF_RESULTS_FILE);
		Instances instances = source.getDataSet();
		instances.setClassIndex(instances.numAttributes()-1);
		System.out.println("");
		//System.out.println(instances.toSummaryString());
		
		NumericToNominal convert= new NumericToNominal();
        String[] options= new String[2];
        options[0]="-R";
        options[1]="first-last";  //range of variables to make numeric

        convert.setOptions(options);
        convert.setInputFormat(instances);

        Instances newData=Filter.useFilter(instances, convert);
        System.out.println(newData.toSummaryString());

        
        //Map<String,String> correlations = findCorrelations(instances); //Key=Attribute, Value = Correlation with Class labels
        Map<String,String> important_features= featureSelection(newData);//FE1,FE2,FE3, ACCURACY
        stats.put("CORR", null); // this is not used in jsf
        stats.put("FEAT", important_features);
        return stats;
		
	
}
	private Map<String,String> featureSelection(Instances instances) throws Exception {
		Map<String,String> reduced_features = new HashMap<>();
		//System.out.println("before "+instances.toSummaryString());
		instances.deleteAttributeAt(0); //delete primary key
		//instances.setClassIndex(instances.numAttributes()-1);
		//System.out.println("after "+instances.toSummaryString());
		
		/*
		AttributeSelection filter1 = new AttributeSelection(); // create and initiate a new AttributeSelection instance
		CfsSubsetEval eval1 = new CfsSubsetEval();
		GreedyStepwise search1 = new GreedyStepwise();
		search1.setNumToSelect(5);
		search1.setSearchBackwards(true);
		filter1.setEvaluator(eval1);
		filter1.setSearch(search1);
		filter1.setInputFormat(instances);
		Instances newData1 = Filter.useFilter(instances, filter1);
		System.out.println("Reduced Dimensionality 1 ="+newData1.toSummaryString());
		*/
		
		System.out.println("-----------------------------------------------------------------");
		AttributeSelection filter2 = new AttributeSelection(); // create and initiate a new AttributeSelection instance
		InfoGainAttributeEval eval2 = new InfoGainAttributeEval();
		Ranker search2 = new Ranker();
		search2.setNumToSelect(4);
		search2.setGenerateRanking(true);
		//search2.getThreshold();
		//search2.setThreshold(0.1);
		filter2.setEvaluator(eval2);
		filter2.setSearch(search2);
		filter2.setInputFormat(instances);
	    Instances newData2 = Filter.useFilter(instances, filter2);
	    //System.out.println("** Reduced Dimensionality 2 "+newData2.toSummaryString());
	    System.out.println("** Printing Top Features ***************");
	    
	    System.out.println("1st = "+newData2.attribute(0).toString().split(" ")[1]);
	    System.out.println("2nd = "+newData2.attribute(1).toString().split(" ")[1]);
	    System.out.println("3rd = "+newData2.attribute(2).toString().split(" ")[1]);
	    
	   
	    reduced_features.put("Feature1", newData2.attribute(0).toString().split(" ")[1]);
	    reduced_features.put("Feature2", newData2.attribute(1).toString().split(" ")[1]);
	    reduced_features.put("Feature3", newData2.attribute(2).toString().split(" ")[1]);
	   
	    /**
	     * This part is commented out
	     * This was added to check if the reduced features are good enough to classify the topK books from all others in the corpus
	     
		SMO svm = new SMO();//SVM
			
		Evaluation eval11 = null;
		Random rand = new Random(1);
		int folds = 5;
		
		eval11 = new Evaluation(newData1);
		svm.buildClassifier(newData1);
		eval11.crossValidateModel(svm, newData1, folds, rand);
		System.out.println("Correct % with reduced dimen1 = "+eval11.pctCorrect());
		
		eval11 = new Evaluation(newData2);
		svm.buildClassifier(newData2);
		eval11.crossValidateModel(svm, newData2, folds, rand);
		System.out.println("Correct % with reduced dimen2 = "+eval11.pctCorrect());
		double acc = 0;
		acc = Math.round(eval11.pctCorrect()*10.0)/10.0;
		reduced_features.put("Accuracy", String.valueOf(acc));
		
		eval11 = new Evaluation(instances);
		svm.buildClassifier(instances);
		eval11.crossValidateModel(svm, instances, folds, rand);
		System.out.println("Correct % with full dimen = "+eval11.pctCorrect());
		*/
		return reduced_features;
		
		
	}
	private Map<String,String> findCorrelations(Instances instances) throws Exception {
		//check correlation of each attribute with class label
		Map<String,String> correlations = new HashMap<>(); //Key=Attribute, Value = Correlation
				for(int k=1;k<instances.numAttributes()-1;k++){ //leave the first attribute = primary key
					double correlation_val=0;
					CorrelationAttributeEval corr = new CorrelationAttributeEval();
					corr.buildEvaluator(instances);
					correlation_val = corr.evaluateAttribute(k);
					if(correlation_val>=0.5 || correlation_val<=-0.5){
					System.out.println("Correl ="+Math.round(correlation_val*1000.000)/1000.000+" for attrib ="+instances.attribute(k));
					correlations.put(String.valueOf(instances.attribute(k).toString().split(" ")[1]), String.valueOf(Math.round(correlation_val*1000.000)/1000.000));
					}
				}
		return correlations;
		
	}
	
	/**
	 * @about This method will simply write the instance-feature space to arff files for machine learning
	 */
	private void writeBinsToFiles(Map<Integer, TopKResults> searched_result_bins) throws IOException {
	
		double dummy = 10000.0000;
		String RESULTS_CSV_FILE = FRGeneralUtils.getPropertyVal("file.results.csv");
		String RESULTS_ARFF_FILE = FRGeneralUtils.getPropertyVal("file.results.arff");
		try (FileWriter fileWriter = new FileWriter(RESULTS_CSV_FILE);) {

			fileWriter.append(FRConstants.FILE_HEADER_RES_CSV.toString());
			fileWriter.append(FRConstants.NEW_LINE);
			
			for (Map.Entry<Integer, TopKResults> book_features : searched_result_bins.entrySet()) {
				TopKResults topResults = book_features.getValue();
				int rank = book_features.getKey();
				fileWriter.append(topResults.getBookName()+"-"+String.valueOf(rank) + FRConstants.COMMA); //bookID-row_num
				double[] book_vector = topResults.getBookGlobalFeatureVector();
				  for(int k=0;k<book_vector.length;k++){
					  fileWriter.append(String.format("%.4f", Math.round((book_vector[k])* dummy) / dummy) + FRConstants.COMMA);
					  }
				fileWriter.append(String.format("%.4f", Math.round((topResults.getBookClassLabel())* dummy) / dummy) + FRConstants.NEW_LINE);
				  }
			}
		
		FeatureExtractorUtility.writeCSVtoARFF(RESULTS_CSV_FILE,RESULTS_ARFF_FILE);
		}
	

	/**
	 *  three fixed bins are created with dynamic bounds based on similarity weight data distribution (skew center, left or right)
	 * @param books
	 * @param results_topK
	 * @return 
	 */
	private Map<Integer, TopKResults> createBins(Map<String, Map<String, double[]>> books, SortedMap<Double, String> results_topK) {
	TopKResults staging_results = null;
	Map<Integer , TopKResults> searched_result_bins = new HashMap<>();
	int rank=0;
	
	
	String distrib = checkNaiveDataDistribution(results_topK);
	System.out.println("Weight Data Distrib = "+distrib);
	
		for(Map.Entry<Double, String> search_results:results_topK.entrySet()){
			rank++;
			String bookName = search_results.getValue();
			double bookWt   = search_results.getKey();
			staging_results = new TopKResults();
			double [] book_global_feature_vector = new double[FRConstants.FEATURE_NUMBER];
			int num_Of_chunks=0;
			 for(Map.Entry<String, Map<String, double[]>> corpus: books.entrySet()){
				 if(corpus.getKey().equals(bookName)){
					 	for(Map.Entry<String, double[]> chunks: corpus.getValue().entrySet()){//loop_over_chunks
					 		num_Of_chunks++; // to get average out all chunks of this book
					 		double [] chunk_vector = chunks.getValue();
					 		 for(int i=0;i<chunk_vector.length;i++){
					 			book_global_feature_vector[i] = book_global_feature_vector[i]+chunk_vector[i];
					 		 }
					 	}
				 }
			 }
			 for(int j=0;j<book_global_feature_vector.length;j++){//average out over number of chunks
				 book_global_feature_vector[j]=book_global_feature_vector[j]/num_Of_chunks;
			 }
			 if(bookWt>=SIMIL_TOPMATCH)// based_on_similarity_weight_distribution
			 staging_results.setBookClassLabel(FRConstants.SIMIL_TOPMATCH_CLASS);//assign class label
			 
			 if(bookWt>=SIMIL_FAIRMATCH && bookWt<SIMIL_TOPMATCH)
				 staging_results.setBookClassLabel(FRConstants.SIMIL_FAIRMATCH_CLASS);
			 
			 if(bookWt<SIMIL_FAIRMATCH)
				 staging_results.setBookClassLabel(FRConstants.SIMIL_POORMATCH_CLASS);
			 
			 staging_results.setBookGlobalFeatureVector(book_global_feature_vector);
			 staging_results.setBookName(bookName);
			 searched_result_bins.put(rank, staging_results);
			 
		}
		
		return searched_result_bins;
	
}
	private Map<Integer, TopKResults> createBinsModified(Map<String, Map<String, double[]>> books, SortedMap<Double, String> results_topK) {
	TopKResults staging_results = null;
	Map<Integer , TopKResults> searched_result_bins = new HashMap<>();
	int rank=0;
	
	//create global book vector and add class labels - for top k results
	 for(Map.Entry<String, Map<String, double[]>> corpus: books.entrySet()){ // loop over all books of corpus
		 double [] book_global_feature_vector = new double[FRConstants.FEATURE_NUMBER]; // create a global feature vector for a single book
		 String bookName = corpus.getKey();
		 rank++;
		 staging_results = new TopKResults();
		 Map<String, double[]> bookChunks =  corpus.getValue(); // get all chunks of a given book
			 	for(Map.Entry<String, double[]> chunks: bookChunks.entrySet()){//loop_over_all_chunks_of_a_given_book
			 		double [] chunk_vector = chunks.getValue();
			 		 for(int i=0;i<chunk_vector.length;i++){
			 			book_global_feature_vector[i] = book_global_feature_vector[i]+chunk_vector[i];
			 		 }
			 	}
			 	for(int j=0;j<book_global_feature_vector.length;j++){//average out over number of chunks
					 book_global_feature_vector[j]=book_global_feature_vector[j]/bookChunks.entrySet().size();
				 }
		 if((results_topK.containsValue(bookName)))	
		 staging_results.setBookClassLabel(FRConstants.SIMIL_TOPMATCH_CLASS);//assign class label_for_topK
		 else
		 staging_results.setBookClassLabel(FRConstants.SIMIL_POORMATCH_CLASS);//assign class label_non_match	
		 
		 staging_results.setBookGlobalFeatureVector(book_global_feature_vector);
		 staging_results.setBookName(bookName);
		 searched_result_bins.put(rank, staging_results);
		 
	 }
		
		return searched_result_bins;
	
}
	
	private String checkNaiveDataDistribution(SortedMap<Double, String> results_topK) {
		String data_distrib = FRConstants.DATA_DISTRIB_AT_CENTR;
		double maxWt = results_topK.firstKey();
		double minWt = results_topK.lastKey();
		double data_points = results_topK.size();
		double stand_Dev =0.0;
		double mean =0.0;
		double median=0;
		
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for( Map.Entry<Double, String> res: results_topK.entrySet()) {
	        stats.addValue(res.getKey());
		}
		stand_Dev = stats.getStandardDeviation();
		mean = stats.getMean();
		median = stats.getPercentile(50);

		
		double center_skew_count=0;
		double skew_1_count=0;
		double skew_0_count=0;
		
		for(Map.Entry<Double, String> res: results_topK.entrySet()){
			double val = res.getKey();
			if(median-(stand_Dev*0.5)<=val && val<=median+(0.5*stand_Dev)){
				center_skew_count++;
			}
			if(maxWt-(0.5*stand_Dev)<=val && val<=maxWt){
				skew_1_count++;
			}
			if(minWt<=val && val<=minWt+(0.5*stand_Dev)){
				skew_0_count++;
			}
		}
		System.out.println("center skew count ="+center_skew_count+" skew1 count ="+skew_1_count+" skew_0_count ="+skew_0_count+" StD_DEV ="+stand_Dev+" mean ="+mean+" median ="+median);
		
		
		//case1: MAX SKEW: around 40% data points around 2 SD of MAX, set data_distrib = MAX, bins {TOP>0.9, FAIR between 0.7-0.9, POOR below 0.7}
		if((Math.abs(skew_1_count/data_points)-FRConstants.DATA_DISTRIB_40_PERCENT)>FRConstants.DATA_DISTRIB_DIFFER_CUTOFF){
			SIMIL_TOPMATCH  = 0.90;//0.9 and above is top
			SIMIL_FAIRMATCH = 0.70;//0.7-0.9 is fair
			data_distrib = FRConstants.DATA_DISTRIB_SKEW_1;
			return data_distrib;
		}
		//case2: MIN SKEW: around 40-50% data points around 2 SD of MIN, set data_distrib = MIN, bins {TOP>0.70, FAIR between 0.15-0.70, POOR below 0.15}
		if((Math.abs(skew_0_count/data_points)-FRConstants.DATA_DISTRIB_40_PERCENT)>FRConstants.DATA_DISTRIB_DIFFER_CUTOFF){
			SIMIL_TOPMATCH  = 0.90;//0.75 and above is top
			SIMIL_FAIRMATCH = 0.70;//0.15-0.75 is fair
			data_distrib = FRConstants.DATA_DISTRIB_SKEW_0;
			return data_distrib;
		}
		else{//the deafult case, we do not need to check!
			//case3: CENTER SKEW: around 50% data points around 2 SD of mean, set data_distrib = center, bins {TOP>0.75, FAIR between 0.25-0.75, POOR below 0.25}
			//if((Math.abs(center_skew_count/data_points)-FRConstants.DATA_DISTRIB_50_PERCENT)>FRConstants.DATA_DISTRIB_DIFFER_CUTOFF){
			SIMIL_TOPMATCH  = 0.90;//0.75 and above is top
			SIMIL_FAIRMATCH = 0.70;//0.25-0.75 is fair
			data_distrib = FRConstants.DATA_DISTRIB_AT_CENTR;
			return data_distrib;
		}
	}
	
	
	public void loadAndPrintRawData() throws Exception{
		String FEATURE_ARFF_FILE = FRGeneralUtils.getPropertyVal("file.wekafeature");
		DataSource source = new DataSource(FEATURE_ARFF_FILE);
		Instances instances = source.getDataSet();
		instances.setClassIndex(instances.numAttributes()-1);
		System.out.println("");
		System.out.println(instances.toSummaryString());
		System.out.println("");
	}

}
