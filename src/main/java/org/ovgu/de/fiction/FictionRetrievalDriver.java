package org.ovgu.de.fiction;

import org.apache.commons.exec.*;
import org.apache.commons.exec.launcher.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import me.xdrop.fuzzywuzzy.*;
import org.aksw.palmetto.*;
import org.aksw.palmetto.corpus.CorpusAdapter;
import org.ovgu.de.fiction.feature.extraction.ChunkDetailsGenerator;
import org.ovgu.de.fiction.feature.extraction.FeatureExtractorUtility;
import org.ovgu.de.fiction.model.BookDetails;
import org.ovgu.de.fiction.model.TopKResults;
import org.ovgu.de.fiction.preprocess.ContentExtractor;
import org.ovgu.de.fiction.search.FictionRetrievalSearch;
import org.ovgu.de.fiction.search.InterpretSearchResults;
import org.ovgu.de.fiction.utils.FRConstants;
import org.ovgu.de.fiction.utils.FRGeneralUtils;

/**
 * @author Suhita, Sayantan
 */
public class FictionRetrievalDriver {

	public static void main(String[] args) throws Exception {

		long start = System.currentTimeMillis();
		//System.out.println("Hello world");

		/* 1> Extract content from Gutenberg corpus - one time */
		
		//ContentExtractor.generateContentFromAllEpubs();
		//System.out.println("Time taken for generating content (min)-" + (System.currentTimeMillis() - start) / (1000 * 60));

		//start = System.currentTimeMillis();
		/**/
		
		// 2> Generate features from the extracted content - one time 
		
		/* Commenting below block as i needed to extract only html output. Un comment when required*/
		//List<BookDetails> features = generateOtherFeatureForAll();
		System.out.println("Time taken for feature extraction and chunk generation (min)-" + (System.currentTimeMillis() - start) / (1000 * 60));
		start = System.currentTimeMillis();
		/**/
		

		/* 3> Write features to CSV - one time */
	
		/* Commenting below block as i needed to extract only html output. Un comment when required*/
		//FeatureExtractorUtility.writeFeaturesToCsv(features);
		 start = System.currentTimeMillis();
		System.out.println("Time taken for writing to CSV (min)-" + (System.currentTimeMillis() - start) / (1000 * 60));
		/**/
		
		
		/*
		 // @Suraj: Adding feature 3 extraction program calling from java to python
		 
		if(FRGeneralUtils.getPropertyVal(FRConstants.IS_WINDOWS) == "Yes")
		{
			extract_Feature3_windows();
		}
		else {
			extract_Feature3_linux();
		}
		
		*/
		//System.out.println("Hello world");
		/* 4> Query */
		/* Commenting below block as i needed to extract only html output. Un comment when required*/
		/*
		 * {"pg108DoyleReturnSherlk", "pg2852DoyleHound", "pg834DoyleMemoirsSherlk", "pg1155Agatha2", "pg620CarolBruno", 
				"pg620CarolBruno", "pg12CarolGlassLook", "pg11CarolAlice",
				"pg13720HermanVoyage1", "pg2701HermanMobyDick", "pg13721HermanVoyage2", "pg21816HermanConfidence",
				"pg786DickensHardTimes", "pg766DickensDavidCopfld", "pg730DickensOliverTw", 
				"pg158JaneAustenEmma", "pg161SJaneAusSensSensi", "pg1342JaneAustenPP",
				"pg2684Galsw4", "pg2911Galsw2",
				"pg2148EdgarPoe2", "pg2150EdgarPoe4", "pg2149EdgarPoe3"};
				
				String[] query_books = {"pg108",  "pg834", "pg1155", 
				 "pg21816",
				"pg786", "pg766", "pg730", 
				"pg158", "pg161", "pg1342",
				"pg2684", "pg2911",
				"pg2148", "pg2150", "pg2149", "pg2701", "pg2852"};
				
				String[] query_books = {"pg108",  "pg834", "pg1155", 
				"pg766", "pg730", "pg158", "pg1342",
				 "pg2701", "pg2852"};
				 {"pg108DoyleReturnSherlk", "pg2852DoyleHound", "pg834DoyleMemoirsSherlk", "pg1155Agatha2", 
				"pg620CarolBruno", "pg12CarolGlassLook", "pg11CarolAlice",
				"pg13720HermanVoyage1", "pg2701HermanMobyDick", "pg13721HermanVoyage2", "pg21816HermanConfidence",
				"pg786DickensHardTimes", "pg766DickensDavidCopfld", "pg730DickensOliverTw", 
				"pg158JaneAustenEmma", "pg161SJaneAusSensSensi", "pg1342JaneAustenPP",
				"pg2684Galsw4", "pg4765Galsw3", "pg2148EdgarPoe2", "pg2150EdgarPoe4", "pg2149EdgarPoe3"};
		
		
		 
		
		
		
		String[] query_books = {"pg108", "pg2701"};//, "pg2701", "pg766", "pg1342", "pg158","pg1155",  "pg1400", "pg730"};
		
		for(int i = 0; i < query_books.length; i++)
		{
			String qryBookNum = query_books[i]; 
			System.out.println("Querying for book : " + qryBookNum);
			String FEATURE_CSV_FILE = FRGeneralUtils.getPropertyVal("file.feature");
			
			TopKResults topKResults = FictionRetrievalSearch.findRelevantBooks(qryBookNum, FEATURE_CSV_FILE, 
					FRConstants.SIMI_PENALISE_BY_CHUNK_NUMS, FRConstants.SIMI_ROLLUP_BY_ADDTN, 
					FRConstants.SIMI_INCLUDE_TTR_NUMCHARS,FRConstants.TOP_K_RESULTS,FRConstants.SIMILARITY_L2);
					
			
			InterpretSearchResults interp = new InterpretSearchResults();
			//interp.performStatiscalAnalysis(topKResults, qryBookNum);
			
			//@suraj: InterpretSearchResults interp = new InterpretSearchResults();
			//interp.performStatiscalAnalysis(topKResults);
			//@suraj: 
			interp.performStatiscalAnalysisUsingRegression(topKResults, 0, FRConstants.SIMI_INCLUDE_TTR_NUMCHARS);
			//findLuceneRelevantBooks(qryBookNum);
			
		}

		*/
		
		//findLuceneRelevantBooks(qryBookNum);
		
		String[] query_books = {"crime", "hour", "business", "manner", "murder" , "reason" ,"theory" , "investigation", "side", "arrest"};//, "pg2701", "pg766", "pg1342", "pg158","pg1155",  "pg1400", "pg730"};
		String[] result_books = {"crime", "hour", "business", "manner", "murder" , "reason" ,"theory" , "investigation", "side", "arrest", "beast", "reason"};
		//String[] result_books = {"beast", "reason", "murderous", "hand", "finger", "bed", "game", "manners", "evidence", "protection"};
		System.out.println(jacardcomparision(query_books, result_books));
		
		System.out.println(FuzzySearch.ratio("prison","prisoner"));
		System.out.println(FuzzySearch.ratio("affection","affectionately"));
		System.out.println(FuzzySearch.ratio("cowardice","cowardly"));
		System.out.println(FuzzySearch.ratio("seafaring","sea"));
		System.out.println(FuzzySearch.ratio("try","fry"));
		System.out.println(FuzzySearch.ratio("opposite","near"));
		System.out.println(FuzzySearch.ratio("treason","reason"));
		System.out.println(FuzzySearch.ratio("murder","murderous"));
		System.out.println(FuzzySearch.ratio("tweedle dee","tweedle dum"));
		//CorpusAdapter ca = Palmetto.getCorpusAdapter("NPMI", "");
		start = System.currentTimeMillis();
		String[] palmetto_aruments= {"/Volumes/suraj/XAI_project/Wikipedia_bd/wikipedia_bd", "npmi", "/Volumes/suraj/XAI_project/Wikipedia_bd/topics_df.txt"};
		//Palmetto.main(palmetto_aruments);
		long end = System.currentTimeMillis();
		System.out.println((end-start) * 1.0 /1000);
	}

	public static List<BookDetails> generateOtherFeatureForAll() throws IOException {
		ChunkDetailsGenerator chunkImpl = new ChunkDetailsGenerator();
		List<BookDetails> books = chunkImpl.getChunksFromAllFiles();
		return books;
	}
	
	public static int jacardFuzzySearch(String[] qry_topics, String[] result_book_topics) {
		List<String> qry_topics_lst = new ArrayList<>(Arrays.asList(qry_topics));
		List<String> result_book_topics_lst = new ArrayList<>(Arrays.asList(result_book_topics));
		
		List<String> intersection_lst = new ArrayList<String>();
		int union_count = qry_topics_lst.size() + result_book_topics_lst.size();	
		int intersection_count = intersection_lst.size();
		double fuzzyjacard = 0.0;
				
		for(int i = 0; i < qry_topics_lst.size(); i++) {
			
			int j = 0;
			int indx = result_book_topics_lst.size();
			while(result_book_topics_lst.size() > 0 && indx > 0) {
				
				if(FuzzySearch.ratio(qry_topics_lst.get(i),result_book_topics_lst.get(j)) > 70) {
					intersection_lst.add(result_book_topics_lst.get(j));
					result_book_topics_lst.remove(result_book_topics_lst.get(j));
					j++;
					indx--;
					break;
				}
				
				else {
					j++;
					indx--;
				}
			}
			
			fuzzyjacard = (intersection_lst.size() * 1.0)/(union_count - intersection_lst.size());
			
		}
		
		System.out.println("From fuzzyjacard: " + fuzzyjacard + " Intersection list size: " + intersection_lst.size() + " Union list size: " + union_count);
		/*
		qry_topics_lst.stream().forEach((qry_topic -> {
			System.out.println(qry_topic);
			int index = -1;
			int counter = 0;
			result_book_topics_lst.stream().forEach(corpus_topic -> {
				System.out.println(corpus_topic);
				
				if(FuzzySearch.ratio(qry_topic,corpus_topic) > 70) {
					intersection_lst.add(corpus_topic);
					result_book_topics_lst.remove(result_book_topics_lst.get(counter));
				}
				
				
			});
			}
		));
		*/
		return intersection_lst.size();		
	}

	public static double jacardcomparision(String[] qry_topics, String[] result_book_topics) {
		Set<String> qry_topics_s1 = new HashSet<String>(Arrays.asList(qry_topics));
		Set<String> result_book_topics_s2 = new HashSet<String>(Arrays.asList(result_book_topics));
		
		System.out.println(qry_topics_s1.toString());
		double union = qry_topics_s1.size() + result_book_topics_s2.size();
		
		Set<String> intersection = new HashSet<String>();
		
		int intersection_count = jacardFuzzySearch(qry_topics, result_book_topics);
		
		qry_topics_s1.retainAll(result_book_topics_s2);
		System.out.println(qry_topics_s1.toString());
		
		double jacqard_coefficient = 0.0, jacqard_coefficient_fuzzy = 0.0;
		jacqard_coefficient = qry_topics_s1.size()/(union - qry_topics_s1.size());
		jacqard_coefficient_fuzzy = intersection_count/(union - intersection_count);
		System.out.println("Intersection_count: " + intersection_count + " Union count: " + union);
		System.out.println(jacqard_coefficient + " | " + jacqard_coefficient_fuzzy);
		return jacqard_coefficient;
	}
	
	public static void extract_Feature3_windows() throws IOException {
		// String python_parameters = "--feature_file_path C:\\OvGU_DKe\\Project\\GutenbergDataset\\Features_Extracted_English.csv --book_file_path C:\\OvGU_DKe\\Project\\GutenbergDataset\\Short_epubs_extracted\\ --emoticon_file_path C:\\Users\\rambo\\git\\fiction\\all_language_emotions.csv --feature_fields 24 --language en --encoding utf-8 --new_feature_file_path C:\\OvGU_DKe\\Project\\GutenbergDataset\\Short_epubs_extracted\\new_Features_Extracted.csv --book_list_file_path C:\\Users\\rambo\\git\\fiction\\Final_Booklist.xlsx --logging_flag True";
        String python_parameters = " --feature_file_path " + FRGeneralUtils.getPropertyVal(FRConstants.FEATURE_FILE_LOCATION) + 
        		" --book_file_path " + FRGeneralUtils.getPropertyVal(FRConstants.BOOK_FILE_PATH);
        python_parameters = python_parameters + " --emoticon_file_path " + FRGeneralUtils.getPropertyVal(FRConstants.EMOTICON_FILE_PATH) + " --feature_fields " +  
        		FRGeneralUtils.getPropertyVal(FRConstants.FEATURE_FIELDS) + " --language " + "\"en\"" +  " --encoding "+ "\"utf-8\"" ;
        python_parameters = python_parameters + " --new_feature_file_path " + FRGeneralUtils.getPropertyVal(FRConstants.NEW_FEATURE_FILE_LOCATION) + 
        		" --book_list_file_path " + FRGeneralUtils.getPropertyVal(FRConstants.BOOK_LIST_FILE_PATH) + " --logging_flag " + FRGeneralUtils.getPropertyVal(FRConstants.LOGGING_FLAG);
		
        System.out.println(python_parameters);
        System.out.println();
        Process p = Runtime.getRuntime().exec("python C:\\Users\\rambo\\git\\fiction\\extract_emotion_features\\extract_emotions_driver.py " + python_parameters);
        
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        StringBuffer response = new StringBuffer();
        StringBuffer errorStr = new StringBuffer();
        boolean alreadyWaited = false;
        String res ="";
        while (p.isAlive()) {
                if(alreadyWaited) {
                    String temp;
                    while ((temp = stdInput.readLine()) != null) {
                    	response.append("\n");
                       response.append(temp);
                        
                    }
                    String errTemp;
                    while ((errTemp = stdError.readLine()) != null) {
                        errorStr.append(errTemp);
                    }  
                    res=response.toString();
                }
                alreadyWaited = true;
                System.out.println(response.toString() + errorStr.toString());  
        }
	}
	public static void extract_Feature3_linux() throws ExecuteException, IOException, InterruptedException {
		System.out.println("hh");//FRGeneralUtils.getPropertyVal(FRConstants.SCRIPT_TYPE)
		 CommandLine cmdLine = new CommandLine(FRGeneralUtils.getPropertyVal(FRConstants.SCRIPT_TYPE));
		 //cmdLine.addArgument("/c");
		 cmdLine.addArgument(FRGeneralUtils.getPropertyVal(FRConstants.SCRIPT_NAME));
		 cmdLine.addArgument(FRGeneralUtils.getPropertyVal(FRConstants.PYTHON_ENVIRONMENT_NAME));
		 cmdLine.addArgument(FRGeneralUtils.getPropertyVal(FRConstants.FEATURE_FILE_LOCATION));
		 cmdLine.addArgument(FRGeneralUtils.getPropertyVal(FRConstants.BOOK_FILE_PATH));
		 cmdLine.addArgument(FRGeneralUtils.getPropertyVal(FRConstants.EMOTICON_FILE_PATH));
		 cmdLine.addArgument(FRGeneralUtils.getPropertyVal(FRConstants.FEATURE_FIELDS));
		 cmdLine.addArgument(FRGeneralUtils.getPropertyVal(FRConstants.LANGUAGE));
		 cmdLine.addArgument(FRGeneralUtils.getPropertyVal(FRConstants.ENCODING));
		 cmdLine.addArgument(FRGeneralUtils.getPropertyVal(FRConstants.BOOK_START_PERCENTAGE));
		 cmdLine.addArgument(FRGeneralUtils.getPropertyVal(FRConstants.BOOK_END_PERCENTAGE));
		 cmdLine.addArgument(FRGeneralUtils.getPropertyVal(FRConstants.SIMILARITY_TYPE));
		 cmdLine.addArgument(FRGeneralUtils.getPropertyVal(FRConstants.NEW_FEATURE_FILE_LOCATION));
		 cmdLine.addArgument(FRGeneralUtils.getPropertyVal(FRConstants.BOOK_LIST_FILE_PATH));
		 cmdLine.addArgument(FRGeneralUtils.getPropertyVal(FRConstants.LOGGING_FLAG));
		 cmdLine.addArgument(FRGeneralUtils.getPropertyVal(FRConstants.PYTHON_CODE_FILE_PATH));
		 DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

		 // ExecuteWatchdog watchdog = new ExecuteWatchdog(60*1000);
		 Executor executor = new DefaultExecutor();
		 System.out.println(cmdLine);
		 //executor.setExitValue(1);
		 //executor.setWatchdog(watchdog);
		 //executor.execute(cmdLine, resultHandler);
		 int exitvalue = executor.execute(cmdLine);
		 if(exitvalue == 0)
		 {
			 System.out.println("Exiting java program after successfully running python script");
			 System.exit(0);
		 }
		 else
		 {
			 System.out.println("Java: Problem while running shell script, got exit status as: " + exitvalue);
			 System.exit(1);
		 }
			 
		 // some time later the result handler callback was invoked so we
		 // can safely request the exit value
		 // resultHandler.waitFor();	 
	}
	
	/*String line = "sh";
	 CommandLine cmdLine2 = CommandLine.parse(line);
	 cmdLine2.addArgument("run_python_jobs.sh");
	 cmdLine2.addArgument("nlp_env");
	 DefaultExecutor executor2 = new DefaultExecutor();
	 executor2.setExitValue(1);
	 int exitValue2 = executor.execute(cmdLine2);
	 
	 System.out.println("Exit value: " + exitValue2);
	 if (exitValue2 == 0) {
		 System.exit(0);
	 }*/
	
	
	
	
}