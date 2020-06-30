package org.ovgu.de.fiction.search;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Random;

import org.ovgu.de.fiction.model.TopKResults;
import org.ovgu.de.fiction.utils.FRConstants;
import org.ovgu.de.fiction.utils.FRGeneralUtils;
import org.ovgu.de.fiction.utils.FRSimilarityUtils;

/**
 * @author Suhita,Sayantan
 */
public class FictionRetrievalSearch {
	
	private static String pathToBOW =  "";
    private static HashMap<String, Double[]> bow_featuremap = new HashMap<String, Double[]>();

	public static TopKResults findRelevantBooks(String qryBookNum, String PENALISE, String ROLLUP, 
			String TTR_CHARS,int topKRes,String similarity, String search_engine_type, String search_engine_lang) throws IOException {
		
		TopKResults topK = new TopKResults();
		String featureCsvFile = "";
		
		/*
		 * @suraj: according to search engine type: choose simfic/lucene/random search type
		 */
		if(search_engine_type.equals(FRConstants.SEARCH_ENGINE_TYPE_SIMFIC)) {
			
			if(search_engine_lang.equals(FRConstants.SEARCH_ENGINE_LANG_COMBINED)) {
				System.out.println("Using combined file of simfic");
				featureCsvFile = FRGeneralUtils.getPropertyVal("file.feature"); //Use the default file.feature having combined explanations
			}
			else if(search_engine_lang.equals(FRConstants.SEARCH_ENGINE_LANG_DE)) {
				System.out.println("Using german file of simfic");
				featureCsvFile = FRGeneralUtils.getPropertyVal("file.feature.german");//Use german simfic feature file 
			}
			else if(search_engine_lang.equals(FRConstants.SEARCH_ENGINE_LANG_EN)) {
				System.out.println("Using english file of simfic");
				featureCsvFile = FRGeneralUtils.getPropertyVal("file.feature.english");//Use english simfic feature file 
			}
			else {
				System.out.println("Using combined file of simfic");
				featureCsvFile = FRGeneralUtils.getPropertyVal("file.feature");//Use default combined, if any other parameter is passed
			}
			
			System.out.println("We are using simfic search system");
			Map<String, Map<String, double[]>> books = getChunkFeatureMapForAllBooks(featureCsvFile);

			SortedMap<Double, String> results_topK = compareQueryBookWithCorpus(qryBookNum, books, PENALISE, ROLLUP, TTR_CHARS,topKRes,similarity);
			
			topK.setBooks(books);
			topK.setResults_topK(results_topK);
		}
		
		else if(search_engine_type.equals(FRConstants.SEARCH_ENGINE_TYPE_LUCENE)){
			System.out.println("We are using lucene search system");
			topK = bagofwordsSearch(qryBookNum, search_engine_lang);			
		}
		
		else if(search_engine_type.equals(FRConstants.SEARCH_ENGINE_TYPE_RANDOM)){
			System.out.println("We are using random search system");
			topK = randomsearch(qryBookNum, search_engine_lang);
		}
		
		else {
			System.out.println("User has chosen no option, by default we will do random search");
			topK = randomsearch(qryBookNum, search_engine_lang);
		}
		
		return topK;
		
	}

	private static Map<String, Map<String, double[]>> getChunkFeatureMapForAllBooks(String featureCsvFile)
			throws IOException, FileNotFoundException {
		String line = "";
		Map<String, Map<String, double[]>> books = new HashMap<>();
		int csvRow = 0;
		try (BufferedReader br = new BufferedReader(new FileReader(featureCsvFile));) {

			while ((line = br.readLine()) != null) {
				if (csvRow > 0) { // ignore headers
					String[] csvElemArr = line.split(",");// array of 22 elements local and others global, 
					books = generateChunkFeatureMapForAChunk(csvElemArr, books);
				}
				csvRow = 1;

			}
		}
		return books;
	}

	private static SortedMap<Double, String> compareQueryBookWithCorpus(String qryBookId, Map<String, Map<String, double[]>> books, 
			String PENALISE, String ROLLUP, String TTR_CHARS,int topKRes, String similarity)
			throws IOException {
		// Step: Send the query book chunk wise and find relevance rank with corpus
		FRSimilarityUtils simUtils = new FRSimilarityUtils();
		String simType = FRConstants.SIMILARITY_L2;
		if(similarity!=null)
		simType = similarity;//FRConstants.SIMILARITY_L2; to change siiliarity to cosine or l1. default will be l2
		System.out.println("Vector similarity Type = " + simType);
		Map<String, Map<Double, String>> staging_results = new HashMap<>();

		/*
		 * chunk_results = Top 20 results Map with => Key = bookId_ChunkNUM , Value = Similarity
		 * normalized
		 */
		Map<String, double[]> queryChunkMap = books.get(qryBookId);

		for (Map.Entry<String, double[]> queryChunk : queryChunkMap.entrySet()) { // loop over
																					 // corpus and
																					 // find those
																					 // elements
																					 // that *do*
																					 // match the
																					 // query book
            // Important: LEAVE_LAST_K_ELEMENTS_OF_FEATURE = from similarity computation
			//@suraj: gives you comparision between each chunk of query book vs all other books in corpus. A special case od != qery book is present to prevent comparing qery book with itself
			Map<Double, String> chunkSimResults = simUtils.getSingleNaiveSimilarity(books, qryBookId, queryChunk, simType,topKRes, FRConstants.LEAVE_LAST_K_ELEMENTS_OF_FEATURE);
			System.out.println("for qry chunk = " + qryBookId + " - " + queryChunk.getKey() + " Similar Book chunks are");
			System.out.println(chunkSimResults);
			System.out.println(".. ");
			staging_results.put(qryBookId + "-" + queryChunk.getKey(), chunkSimResults);// this will
																						// always
																						// return 20
																						// or
			// 10 results per query chunk
		}

		System.out.println("stg results size =" + staging_results.size());// size = no of chunks of
		
		/*
		 * @suraj: staging results now has querychunk to corpuschunk mapping in the form of below
		 * { Querybookid-querychunkid : { similarity score : corpusbookid-corpuschunkid}}
		 * 
		 * {123-pg1: {{3: 456-cc1},{2: 456-cc2}, {1: 234-cc1},       123-pg2 :{{10: 456-cc5}, {4: 456-cc1}} 
		 * {456-cc1:0 + 3 + 4, 456-cc2 : 2}
		 * We have mapping of each query chunk with entire corpus, this result can further be narrowed to 10/20 using topKRes
		 * Remeber toKRes only controls the number of corpus chunks that are mapped to each query chunk and 
		 * it is not the final top k search result
		 */
		// query book
		// loop over the staging results to create a final weighted result map
		SortedMap<Double, String> sorted_results_wo_TTR = new TreeMap<Double, String>(Collections.reverseOrder());// final DS to hold sorted ranks
		SortedMap<Double, String> sorted_results_mit_TTR = new TreeMap<Double, String>(Collections.reverseOrder());
		
		// Multimap<Double, String> multimap_results = ArrayListMultimap.create();//useful to
		// combine many values for same similarity weight
		Map<String, Double> chunk_results = new TreeMap<>();// useful, this has chunks rolled up,
															// i.e. all occurrences of 'pg547-1'
															// clubbed

		// outer for loop: key = q1, Val = [Map of similar chunks], Key = q2, Val =[Map of similar
		// chunks]
		for (Map.Entry<String, Map<Double, String>> stg_results : staging_results.entrySet()) {
			Map<Double, String> chunk_res = stg_results.getValue(); // this has relevance weights
																	 // for a query chunk
			// below for loop over a specific query chunk: q1
			for (Map.Entry<Double, String> res : chunk_res.entrySet()) {
				/*
				 * @suraj: don't get confused with chunkres and chunkresults. chunkres is the input map having similarity score for corpus chunk
				 * chunk results is used for output rollup
				 * Rollup: extract corpuschunkid and add it as key to map chunkresults if it doesn't already exists in the map.
				 * if already exists then just add similarity score to it
				 * Again remeber there are twoways to calculate similarity from paper(by addition doble sigma and another by multiplication
				 * we do likewise according to rollup flag
				 */
				// add relevant output in a final results map, Key ="Corpus_Chunk" = bookId_ChunkId,
				// Value = Cumulative_Weights
				if (!chunk_results.containsKey(res.getValue())) // new chunk ('pg547-1') item, just add
					chunk_results.put(res.getValue(), res.getKey());
				else {
					double temp = 0.00;
					temp = chunk_results.get(res.getValue());// get current sim. weight
					if(ROLLUP.equals(FRConstants.SIMI_ROLLUP_BY_ADDTN))
					chunk_results.put(res.getValue(), Math.round((temp + res.getKey()) * 10000.0000) / 10000.0000);// if key present, update the weight
					if(ROLLUP.equals(FRConstants.SIMI_ROLLUP_BY_MULPN))
					chunk_results.put(res.getValue(), Math.round((temp * res.getKey()) * 10000.0000) / 10000.0000);// if key present, update the weight
						
				}
			}
		}

		System.out.println("chunk results = " + chunk_results); // this will break the number
																 // topK=top20, when it will combine
																 // result chunks

		Map<String, Double> book_results = new TreeMap<>(); // rolled up values per book!
		// roll up from chunks to a corpus book level, i.e. 'pg547-1' , 'pg547-2' ... all clubbed to
		// 'pg547'
		/*
		 * @suraj: Use above results and rollup chunk simialrity to book level
		 * extract bookid from bookid-chunkid key. For each bookid accumulate its respective chunk similarity
		 * Check the penalty flag and penalize by sqrt(number of chunks in book) or number of chunks in book or nothing
		 * 
		 * possible bug: In one of the paper penalization is by N+M, here we are just penalizing by N , query book chunk not being considered
		 */
		
		for (Map.Entry<String, Double> stg1 : chunk_results.entrySet()) {
			String book_chunk = stg1.getKey(); // 'pg547-1'
			String bookId = book_chunk.split("-")[0]; // 'pg547'
			double book_weight = 0.00;
			for (Map.Entry<String, Double> stg2 : chunk_results.entrySet()) {
				if (bookId.equals(stg2.getKey().split("-")[0])) { // compare the first part of
																	 // 'pg547-1', i.e. 'pg547'
					book_weight = book_weight + stg2.getValue();// accumulate weights
				}
			}// end of a chunk rolling here
			
			/*
			 * query book = 123
			 * {456-cc1:7, 456-cc2 : 2, 456-cc3:1, 789-cc1 : 2, 789-cc2 : 10}
			 * output: {456-10, 789:12}
			 */
			double noOfChunks = books.get(bookId).size();
			if(PENALISE.equals(FRConstants.SIMI_PENALISE_BY_NOTHING))
				book_results.put(bookId, Math.round((book_weight) * 10000.0000) / 10000.0000);
			if(PENALISE.equals(FRConstants.SIMI_PENALISE_BY_CHUNK_NUMS))
				book_results.put(bookId, Math.round((book_weight/noOfChunks) * 10000.0000) / 10000.0000);
			if(PENALISE.equals(FRConstants.SIMI_PENALISE_BY_CHUNK_SQR_ROOT))
				book_results.put(bookId, Math.round((book_weight/(Math.sqrt(noOfChunks))) * 10000.0000) / 10000.0000);
			//book_results.put(bookId, Math.round((book_weight /noOfChunks) * 10000.0000) / 10000.0000);
		}

		System.out.println("book results = " + book_results);

		// @suraj: This contains rolled up similarity without last two global features(only 19 columns)
		for (Map.Entry<String, Double> unranked_weights : book_results.entrySet()) {
			sorted_results_wo_TTR.put(unranked_weights.getValue(), unranked_weights.getKey()); // this is a reverse sorted tree , by decreasing relevance rank,
			// 3.987-> book6, book67 -> top
			// 2.851-> book5
			// 1.451-> book9, book89 -> lowest rank
		}
		//System.out.println("final Similarity results = " + sorted_results);
		
		if(TTR_CHARS.equals(FRConstants.SIMI_EXCLUDE_TTR_NUMCHARS)){
			
			System.out.println("");
			System.out.println("For Chunk based Similarity, QBE Book = " + qryBookId + " printing top " + FRConstants.TOP_K_RESULTS + " results");
			sorted_results_wo_TTR = printTopKResults(sorted_results_wo_TTR);
			return sorted_results_wo_TTR;
		
		}
		else{//i.e. include TTR and Num of characters
			//compose a feature array with 3 elements
			//0. Similarity Relevance score - weight 0.85
			//1. TTR - weight 0.10
			//2. Numbr of Chars - weight 0.05
			
			//Compose a corpus of all books (not chunks) with above 3 dimensional vector
			// find L2 similarity and rank results
			
			/* @suraj:
			 * The combined sum of 3-D vector should be equasl to one for global features to work
			 * Currently i have scaled it as 0.6 * similarity_without_global + 0.1 * TTR + 0.05 * characters + 0.3 * other global features
			 */
			
			Map<String, double[]> global_corpus = new TreeMap<>();
			//create feature vectors below
			
			/*@suraj: sorted_results_wo_TTR - {3.1: pg156, 0.7: pg234, 0.01: pg456}
			 * 
			 */
			for(Map.Entry<Double, String> global_books:sorted_results_wo_TTR.entrySet())
			{
				double [] global_feature = new double[FRConstants.FEATURE_NUMBER_GLOBAL];
				//@suraj added below line from outside loop to inner loop
				
				global_feature[0] = global_books.getKey()*FRConstants.CHUNK_WEIGHT;
				
				/*@suraj: Take the bookid from similarity wo ttr map.
				 * For each of this book query the actual book feature hashmap.
				 * Retrieve the books all values(local + global)
				 * set the contribution of only global variables towards similarity
				 * 
				 * Note: If we want our features to be more impactful then we can set higher weights, But finally all weights should add up to one.
				 * Hence, if our features have more columns, it contributes less to final search
				 */
				for(Map.Entry<String, Map<String, double[]>> input_books: books.entrySet())
				{
					
					   if(global_books.getValue().equals(input_books.getKey()))
					   { 
						   //@suraj: match bookId of similarity wo ttr with bookId of book object.
						   //@suraj: if matched retrieve the ttr and num of characters back
						   //@suraj: While retrieving features from below we can vary the weights or contribution of each feature
						   Map<String, double[]> chunk_map = input_books.getValue();
						     for(Map.Entry<String, double[]> temp_chunk: chunk_map.entrySet()){
						    	 global_feature[1] = temp_chunk.getValue()[FRConstants.TTR_21]*FRConstants.TTR_WEIGHT;
					    	 	 global_feature[2] = temp_chunk.getValue()[FRConstants.NUM_CHARS_20]*FRConstants.NUMCHAR_WEIGHT;
					    	 	 global_feature[3] = temp_chunk.getValue()[21]*FRConstants.CHAR_WEIGHT;
					    	 	 global_feature[4] = temp_chunk.getValue()[24]*FRConstants.GENRE_WEIGHT;
					    	 	 global_feature[5] = temp_chunk.getValue()[25]*FRConstants.GENRE_WEIGHT;
					    	 	 global_feature[6] = temp_chunk.getValue()[26]*FRConstants.GENRE_WEIGHT;
					    	 	 global_feature[7] = temp_chunk.getValue()[27]*FRConstants.GENRE_WEIGHT;
					    	 	 global_feature[8] = temp_chunk.getValue()[28]*FRConstants.GENRE_WEIGHT;
					    	 	 global_feature[9] = temp_chunk.getValue()[29]*FRConstants.GENRE_WEIGHT;
					    	 	 global_feature[10] = temp_chunk.getValue()[30]*FRConstants.GENRE_WEIGHT;
					    	 	 global_feature[11] = temp_chunk.getValue()[31]*FRConstants.GENRE_WEIGHT;
					    	 	 global_feature[12] = temp_chunk.getValue()[32]*FRConstants.GENRE_WEIGHT;
					    	 	 global_feature[13] = temp_chunk.getValue()[33]*FRConstants.GENRE_WEIGHT;
					    	 	 global_feature[14] = temp_chunk.getValue()[34]*FRConstants.EMO_WEIGHT;
					    	 	 global_feature[15] = temp_chunk.getValue()[35]*FRConstants.EMO_WEIGHT;
					    	 	 global_feature[16] = temp_chunk.getValue()[36]*FRConstants.EMO_WEIGHT;
					    	 	 global_feature[17] = temp_chunk.getValue()[37]*FRConstants.EMO_WEIGHT;
					    	 	 global_feature[18] = temp_chunk.getValue()[38]*FRConstants.EMO_WEIGHT;
					    	 	 global_feature[19] = temp_chunk.getValue()[39]*FRConstants.EMO_WEIGHT;
					    	 	 global_feature[20] = temp_chunk.getValue()[40]*FRConstants.EMO_WEIGHT;
					    	 	 global_feature[21] = temp_chunk.getValue()[41]*FRConstants.EMO_WEIGHT;
					    	 	 global_feature[22] = temp_chunk.getValue()[42]*FRConstants.EMO_WEIGHT;
					    	 	 global_feature[23] = temp_chunk.getValue()[43]*FRConstants.EMO_WEIGHT;
					    	 	 global_feature[24] = temp_chunk.getValue()[44]*FRConstants.EMO_WEIGHT;
					    	 	 global_feature[25] = temp_chunk.getValue()[45]*FRConstants.EMO_WEIGHT;
					    	 	 global_feature[26] = temp_chunk.getValue()[46]*FRConstants.EMO_WEIGHT;
					    	 	 global_feature[27] = temp_chunk.getValue()[47]*FRConstants.EMO_WEIGHT;
					    	 	 global_feature[28] = temp_chunk.getValue()[48]*FRConstants.EMO_WEIGHT;
					    	 	 global_feature[29] = temp_chunk.getValue()[49]*FRConstants.EMO_WEIGHT;
					    	 	 global_feature[30] = temp_chunk.getValue()[50]*FRConstants.EMO_WEIGHT;
						    	 	 /* for surajs uncomment below block
						    	 	 global_feature[13] = temp_chunk.getValue()[32]*FRConstants.FEATURE_WEIGHT_SIMILARITY_MISC_GLOBAL;
						    	 	 global_feature[14] = temp_chunk.getValue()[33]*FRConstants.FEATURE_WEIGHT_SIMILARITY_MISC_GLOBAL;
						    	 	 global_feature[15] = temp_chunk.getValue()[34]*FRConstants.FEATURE_WEIGHT_SIMILARITY_MISC_GLOBAL;
						    	 	 global_feature[16] = temp_chunk.getValue()[35]*FRConstants.FEATURE_WEIGHT_SIMILARITY_MISC_GLOBAL;
						    	 	 global_feature[17] = temp_chunk.getValue()[36]*FRConstants.FEATURE_WEIGHT_SIMILARITY_MISC_GLOBAL;
						    	 	 global_feature[18] = temp_chunk.getValue()[37]*FRConstants.FEATURE_WEIGHT_SIMILARITY_MISC_GLOBAL;
						    	 	 global_feature[19] = temp_chunk.getValue()[38]*FRConstants.FEATURE_WEIGHT_SIMILARITY_MISC_GLOBAL;
						    	 	 */
						    	 	
						    	 	 
						    	 	 /*
						    	 	  * 
						    	 	  * chandan
						    	 	  * global_feature[1] = temp_chunk.getValue()[FRConstants.TTR_21]*FRConstants.FEATURE_WEIGHT_LESS;
						    	 	 global_feature[2] = temp_chunk.getValue()[FRConstants.NUM_CHARS_20]*FRConstants.FEATURE_WEIGHT_LEAST;						    	 	 
						    	 	 global_feature[3] = temp_chunk.getValue()[22]*FRConstants.FEATURE_WEIGHT_SIMILARITY_MISC_GLOBAL;
						    	 	 global_feature[4] = temp_chunk.getValue()[23]*FRConstants.FEATURE_WEIGHT_SIMILARITY_MISC_GLOBAL;
						    	 	 global_feature[5] = temp_chunk.getValue()[24]*FRConstants.FEATURE_WEIGHT_SIMILARITY_MISC_GLOBAL;
						    	 	 global_feature[6] = temp_chunk.getValue()[25]*FRConstants.FEATURE_WEIGHT_SIMILARITY_MISC_GLOBAL;
						    	 	 global_feature[7] = temp_chunk.getValue()[26]*FRConstants.FEATURE_WEIGHT_SIMILARITY_MISC_GLOBAL;
						    	 	 global_feature[8] = temp_chunk.getValue()[27]*FRConstants.FEATURE_WEIGHT_SIMILARITY_MISC_GLOBAL;
						    	 	 System.out.println(" ====== ======== ========= =======");
						    	 	 System.out.println(global_feature[1] + "|" + global_feature[2] + "|" + global_feature[3] + "|" + 
						    	 	 global_feature[4] + "|" + global_feature[5] + "|" + global_feature[6] +"|" + global_feature[7] + "|" + global_feature[8] );
						    	 	System.out.println(" ====== ======== ========= =======");
						    	 	*/
						    	 	 /*
						    	 	System.out.println(global_feature[1] + "|" + global_feature[2] + "|" + global_feature[3] + "|" + 
								    	 	 global_feature[4] + "|" + global_feature[5] + "|" + global_feature[6] +"|" + global_feature[7] + "|" + global_feature[8] );
								    	 	System.out.println(" ====== ======== ========= =======");
								    	 	*/
						    	 	
						     }
						   
					   }
				   }
				//@suraj: avoiding adding of query book back to search
				/*@suraj: Bug - But if this is the case, then how are we getting query book back in search result???
				 * 
				 */
				if(!global_books.getValue().equals(qryBookId))
				{// dont_add_query_vector_which_is_specially_created
					global_corpus.put(global_books.getValue(),global_feature);
				}
				else {
					//@suraj: added below line
					global_corpus.put(global_books.getValue(),global_feature);
				}
			}
			
			//qry_vector = [0.85, 0.10, 0.05]
			/*@suraj commenting this block as we are adding the query book too to global corpus in above loop if else condityion
			double [] global_qry_vector = new double[FRConstants.LEAVE_LAST_K_ELEMENTS_OF_FEATURE+1];
			global_qry_vector[0] = 1   * FRConstants.FEATURE_WEIGHT_SIMILARITY_WITHOUT_GLOBAL;
			global_qry_vector[1] = FRConstants.FEATURE_WEIGHT_LESS;
			global_qry_vector[2] = FRConstants.FEATURE_WEIGHT_LEAST;
			global_qry_vector[3] = FRConstants.FEATURE_WEIGHT_SIMILARITY_MISC_GLOBAL;
			global_qry_vector[4] = FRConstants.FEATURE_WEIGHT_SIMILARITY_MISC_GLOBAL;
			global_qry_vector[5] = FRConstants.FEATURE_WEIGHT_SIMILARITY_MISC_GLOBAL;
			global_qry_vector[6] = FRConstants.FEATURE_WEIGHT_SIMILARITY_MISC_GLOBAL;
			global_qry_vector[7] = FRConstants.FEATURE_WEIGHT_SIMILARITY_MISC_GLOBAL;
			global_qry_vector[8] = FRConstants.FEATURE_WEIGHT_SIMILARITY_MISC_GLOBAL;
			global_qry_vector[9] = FRConstants.FEATURE_WEIGHT_SIMILARITY_MISC_GLOBAL;
			global_qry_vector[10] = FRConstants.FEATURE_WEIGHT_SIMILARITY_MISC_GLOBAL;
			global_qry_vector[11] = FRConstants.FEATURE_WEIGHT_SIMILARITY_MISC_GLOBAL;
			global_qry_vector[12] = FRConstants.FEATURE_WEIGHT_SIMILARITY_MISC_GLOBAL;
			global_qry_vector[13] = FRConstants.FEATURE_WEIGHT_SIMILARITY_MISC_GLOBAL;
			global_qry_vector[14] = FRConstants.FEATURE_WEIGHT_SIMILARITY_MISC_GLOBAL;
			global_qry_vector[15] = FRConstants.FEATURE_WEIGHT_SIMILARITY_MISC_GLOBAL;
			global_qry_vector[16] = FRConstants.FEATURE_WEIGHT_SIMILARITY_MISC_GLOBAL;
			global_qry_vector[17] = FRConstants.FEATURE_WEIGHT_SIMILARITY_MISC_GLOBAL;
			global_qry_vector[18] = FRConstants.FEATURE_WEIGHT_SIMILARITY_MISC_GLOBAL;
			global_qry_vector[19] = FRConstants.FEATURE_WEIGHT_SIMILARITY_MISC_GLOBAL;
			
			global_corpus.put(qryBookId, global_qry_vector); // add the global_query to corpus
			*/
			
			/*
			 * @suraj: Set the similar contributyions to query vector also
			 * If niot set then query vector will have different length / contribution and corpus vector will have different
			 * 
			 * Bug - We sawcode like below where there were only weights but no actual values of query book multiplied with weights. We have changed the implementation
			 * to handle it
			 * global_qry_vector[0] = FRConstants.FEATURE_WEIGHT_SIMILARITY_WITHOUT_GLOBAL;
			 * global_qry_vector[1] = FRConstants.FEATURE_WEIGHT_LESS;
			 */
			
			
			
			
			//@suraj: Yet another monstrosity
			sorted_results_mit_TTR = simUtils.getSingleNaiveSimilarityDummy(global_corpus, qryBookId, FRConstants.TOP_K_RESULTS, FRConstants.SIMILARITY_L2);
			
			System.out.println("");
			System.out.println("For Global Feature based Similarity, QBE Book = " + qryBookId + " printing top " + FRConstants.TOP_K_RESULTS + " results");
			sorted_results_mit_TTR = printTopKResults(sorted_results_mit_TTR);
			return sorted_results_mit_TTR;
			
		}
		
		
		
		
	}
	
	private static SortedMap<Double, String> printTopKResults(SortedMap<Double, String> sorted_results){
		int count = 0;
		double topWeight = 0;
		SortedMap<Double, String> printed_results = new TreeMap<Double, String>(Collections.reverseOrder());
			for (Map.Entry<Double, String> print_res : sorted_results.entrySet()) {
				count++;
				if (count == 1)
					topWeight = print_res.getKey();
				if (count <= FRConstants.TOP_K_RESULTS) {
					if (count == 1){
						System.out.println("Rank " + count + " is  Book = " + print_res.getValue() + " weight = 1 ");
						printed_results.put(1.00,  print_res.getValue());
					}
					else{
						System.out.println("Rank " + count + " is  Book = " + print_res.getValue() + " weight = "
								+ Math.round((print_res.getKey() / topWeight) * 1000.000) / 1000.000);
						printed_results.put(Math.round((print_res.getKey() / topWeight) * 1000.000) / 1000.000,  print_res.getValue());
					}
				}
			}
			return printed_results;
	}

	private static Map<String, Map<String, double[]>> generateChunkFeatureMapForAChunk(String[] instances,
			Map<String, Map<String, double[]>> bookFeatureMap) {
		String bookName = instances[0].split("-")[0];
		String chunkNo = instances[0].split("-")[1];
		double[] feature_array = new double[FRConstants.FEATURE_NUMBER];

		for (int j = 1; j < instances.length; j++) {// start from index 1, skip chunk
													// num as chunk number starts from 1
			feature_array[j - 1] = Double.parseDouble(instances[j]);
		}

		Map<String, double[]> chunkFeatureMap = bookFeatureMap.containsKey(bookName) ? bookFeatureMap.get(bookName) : new HashMap<>();
		chunkFeatureMap.put(chunkNo, feature_array);
		bookFeatureMap.put(bookName, chunkFeatureMap);
		return bookFeatureMap;
	}

	/*
     * @suraj: Lucene search
     * Index BOW based feature file was precomputed using python script.
     * We would be searching in the lucene file, which has all features at global/book level, hence one row per book.
     * Top results are sorted in descending order and put into a map and returned
     *
     */
	
	//Read LUCENE File
	private static void preprocessBOWfile() throws IOException {
    	BufferedReader br = new BufferedReader(new FileReader(pathToBOW));
        String line = "";
        int header_flag = 1;  
        String bookid = "";
        
        while ((line = br.readLine()) != null){
        	if(header_flag == 0) {
        		String[] data = line.split(",");
        		Double[] bow_feature = new Double[51];
        		bookid = data[0];
        		
        		//from column F0 to F50,--> 51 column leaving out the first column as it has pgid
        		for(int col = 1; col < FRConstants.FEATURE_NUMBER + 1; col++) 
        			bow_feature[col-1] = Double.parseDouble(data[col]);
        		
        		bow_featuremap.put(bookid, bow_feature);
        		//System.out.println(Arrays.toString(data));
        	}
        	else
        		header_flag = 0;        	
        }
        
        //printing the read file
        /*
        bow_featuremap.entrySet().forEach(row->{
            System.out.println(row.getKey() + " " + Arrays.toString(row.getValue()));  
         });
         */
         
        
        br.close();
        return ;
    }

	//Calculate L2 similarity
    private static double calculateL2Similarity(Double[] query_feature, Double[] other_book_feature) {
    	
    	double dist_meas_values = 0.0;

		for (int i = 0; i < query_feature.length; i++) 
		{
			dist_meas_values = dist_meas_values + Math.pow(query_feature[i] - other_book_feature[i], 2); 
		}

		dist_meas_values = Math.sqrt(dist_meas_values);
		dist_meas_values = 1 / (1 + dist_meas_values);
			
		return dist_meas_values;
    }
    
    //Search retrieval for BOW
    private static TopKResults bagofwordsSearch(String query, String search_engine_lang) throws IOException {
    	TopKResults topKResults = new TopKResults();
    	
    	SortedMap<Double, String> sorted_results = new TreeMap<Double, String>(Collections.reverseOrder());
    	
    	//pathToBOW = FRGeneralUtils.getPropertyVal(FRConstants.LUCENE_FEATURE_FILE);
    	
    	if(search_engine_lang.equals(FRConstants.SEARCH_ENGINE_LANG_COMBINED)) {
    		System.out.println("Using combined file of lucene");
    		pathToBOW = FRGeneralUtils.getPropertyVal(FRConstants.LUCENE_FEATURE_COMBINED_FILE); //Use the default file.feature having combined explanations
		}
		else if(search_engine_lang.equals(FRConstants.SEARCH_ENGINE_LANG_DE)) {
			System.out.println("Using german file of lucene");
			pathToBOW = FRGeneralUtils.getPropertyVal(FRConstants.LUCENE_FEATURE_DE_FILE);//Use german simfic feature file 
		}
		else if(search_engine_lang.equals(FRConstants.SEARCH_ENGINE_LANG_EN)) {
			System.out.println("Using english file of lucene");
			pathToBOW = FRGeneralUtils.getPropertyVal(FRConstants.LUCENE_FEATURE_EN_FILE);//Use english simfic feature file 
		}
		else {
			System.out.println("Using combined file of lucene");
			pathToBOW = FRGeneralUtils.getPropertyVal(FRConstants.LUCENE_FEATURE_COMBINED_FILE);//Use default combined, if any other parameter is passed
		}
		
    	try {
			preprocessBOWfile();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Cannot read the Lucene index file");
			System.exit(1);
		}
    	
		Map<String, Double[]> valueMap = bow_featuremap; //FRFileOperationUtils.readCsvForBOW();

		// Calculate l2 similarity
		for (String book : valueMap.keySet()) {
			Double l2Similarity = calculateL2Similarity(valueMap.get(query), valueMap.get(book));
			sorted_results.put(l2Similarity, book);
		}

		topKResults.setResults_topK(sorted_results);

		//Printing the results
		Map<Object, Object> tmp_15_res = topKResults.getResults_topK().entrySet().stream().limit(15).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		tmp_15_res.entrySet().forEach(row->{
            System.out.println(row.getKey() + " " + row.getValue());  
		});
    	
    	return topKResults;
    }
      
    /*
     * @suraj: Random Baseline search
     * Since results have to be stable across different users and sessions
     * We would be picking the query book as usual from lucene index file.
     * Later, we will generate random similarity values using query books pgid as seed so that results are consistent.
     * This result is then offloaded to topk results
     */ 
    
    private static TopKResults randomsearch(String query, String search_engine_lang) throws IOException {
    	
    	TopKResults topKResults = new TopKResults();
    	
    	SortedMap<Double, String> sorted_results = new TreeMap<Double, String>(Collections.reverseOrder());
    	
    	if(search_engine_lang.equals(FRConstants.SEARCH_ENGINE_LANG_COMBINED)) {
    		System.out.println("Using combined file of random");
    		pathToBOW = FRGeneralUtils.getPropertyVal(FRConstants.LUCENE_FEATURE_COMBINED_FILE); //Use the default file.feature having combined explanations
		}
		else if(search_engine_lang.equals(FRConstants.SEARCH_ENGINE_LANG_DE)) {
			System.out.println("Using german file of random");
			pathToBOW = FRGeneralUtils.getPropertyVal(FRConstants.LUCENE_FEATURE_DE_FILE);//Use german simfic feature file 
		}
		else if(search_engine_lang.equals(FRConstants.SEARCH_ENGINE_LANG_EN)) {
			System.out.println("Using english file of random");
			pathToBOW = FRGeneralUtils.getPropertyVal(FRConstants.LUCENE_FEATURE_EN_FILE);//Use english simfic feature file 
		}
		else {
			System.out.println("Using combined file of random");
			pathToBOW = FRGeneralUtils.getPropertyVal(FRConstants.LUCENE_FEATURE_COMBINED_FILE);//Use default combined, if any other parameter is passed
		}
    	
    	int book_random_seed = Integer.parseInt(query.replace("pg",""));
    	
    	Random rnd = new Random();
    	rnd.setSeed(book_random_seed);
    	double randNum = rnd.nextDouble();
		
    	try {
			preprocessBOWfile();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Cannot read the Lucene index file");
			System.exit(1);
		}
    	
		Map<String, Double[]> valueMap = bow_featuremap; //FRFileOperationUtils.readCsvForBOW();

		// Calculate l2 similarity
		for (String book : valueMap.keySet()) {
			Double book_similarity = rnd.nextDouble();
			sorted_results.put(book_similarity, book);
		}

		topKResults.setResults_topK(sorted_results);

		//Printing the results
		/*
		topKResults.getResults_topK().entrySet().forEach(row->{
            System.out.println(row.getKey() + " " + row.getValue());  
         });
         */
		
		Map<Object, Object> tmp_15_res = topKResults.getResults_topK().entrySet().stream().limit(15).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		tmp_15_res.entrySet().forEach(row->{
            System.out.println(row.getKey() + " " + row.getValue());  
		});
    	
    	return topKResults;
	}


}
