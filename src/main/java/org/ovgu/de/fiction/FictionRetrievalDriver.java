package org.ovgu.de.fiction;

import org.apache.commons.exec.*;
import org.apache.commons.exec.launcher.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
		
		
		 */
		
		
		
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
		
		
		
		//String qryBookNum = "pg108DoyleReturnSherlk"; 
		/**/
		
		//pg11CarolAlice,  pg1400DickensGreatExp,pg766DickensDavidCopfld
		// pg2701HermanMobyDick,pg537DoyleTerrorTales
		// pg13720HermanVoyage1, pg2911Galsw2, pg1155Agatha2,pg2852DoyleHound, pg2097DoyleSignFour

		// read from csv features and prints ranked relevant books, run after CSV is written
		
		/* Commenting below block as i needed to extract only html output. Un comment when required*/
		//String FEATURE_CSV_FILE = FRGeneralUtils.getPropertyVal("file.feature");
		/**/
		
		//Config 1: three possible setting similarity objective penalization: divide chunks by (1) OR (number_of_chunks) OR sqr_root(number_of_chunks)
		//Config 2: two possible settings for similarity roll up : add_chunks (default) OR multipl_chunks
		//Config 3: Include or exclude TTR and Numbr of Chars
		
		/* Commenting below block as i needed to extract only html output. Un comment when required
		TopKResults topKResults = FictionRetrievalSearch.findRelevantBooks(qryBookNum, FEATURE_CSV_FILE, 
				FRConstants.SIMI_PENALISE_BY_CHUNK_NUMS, FRConstants.SIMI_ROLLUP_BY_ADDTN, 
				FRConstants.SIMI_EXCLUDE_TTR_NUMCHARS,FRConstants.TOP_K_RESULTS,FRConstants.SIMILARITY_L2);
		*/
		
		/* * 5> Perform some machine learning over the results
		 
		*/
		
		/* Commenting below block as i needed to extract only html output. Un comment when required
		InterpretSearchResults interp = new InterpretSearchResults();
		interp.performStatiscalAnalysis(topKResults, qery_bk);
		For Global Feature based Similarity, QBE Book = pg158 printing top 10 results
Rank 1 is  Book = pg766 weight = 1 
Rank 2 is  Book = pg1342 weight = 0.992
Rank 3 is  Book = pg730 weight = 0.975
Rank 4 is  Book = pg1400 weight = 0.97
Rank 5 is  Book = pg108 weight = 0.963
Rank 6 is  Book = pg141 weight = 0.956
Rank 7 is  Book = pg834 weight = 0.955
Rank 8 is  Book = pg2852 weight = 0.939
Rank 9 is  Book = pg98 weight = 0.938
Rank 10 is  Book = pg105 weight = 0.936

For Global Feature based Similarity, QBE Book = pg158 printing top 10 results
Rank 1 is  Book = pg2852 weight = 1 
Rank 2 is  Book = pg108 weight = 0.993
Rank 3 is  Book = pg1342 pg834 weight = 0.99
Rank 4 is  Book = pg98 weight = 0.976
Rank 5 is  Book = pg105 weight = 0.975
Rank 6 is  Book = pg730 weight = 0.968
Rank 7 is  Book = pg1400 weight = 0.963
Rank 8 is  Book = pg766 weight = 0.953
Rank 9 is  Book = pg141 weight = 0.944
Rank 10 is  Book = pg2097 weight = 0.931
May 29, 2020 1:31:43 AM com.github.fommil.jni.JniLoader liberalLoad
INFO: successfully loaded /var/folders/tf/2pswl45d7gxghml4qfn86s3r0000gn/T/jniloader7354101386306657182netlib-native_system-osx-x86_64.jnilib
May 29, 2020 1:31:43 AM com.github.fommil.jni.JniLoader load
INFO: already loaded netlib-native_system-osx-x86_64.jnilib
May 29, 2020 1:31:43 AM com.github.fommil.jni.JniLoader load
INFO: already loaded netlib-native_system-osx-x86_64.jnilib
RMSE for regression on 5 fold cross validation 0.010235512734960284

Linear Regression Model

Class label =

      1.3698 * Feature 0 +
      0.2518 * Feature 3 +
     -0.4037 * Feature 4 +
      1.2325 * Feature 5 +
      0.5976 * Feature 7 +
     -0.6448 * Feature 8 +
     -1.6492 * Feature 9 +
      0.2107 * Feature 13 +
     -0.0417 * Feature 14 +
     -0.7272 * Feature 15 +
     -0.0392 * Feature 19 +
     -0.0791 * Feature 20 +
     -0.0039 * Feature 21 +
      1.0805

 * Printing Top Features from regression ******
Rank 1 =  Feature 9
Rank 2 =  Feature 0
Rank 3 =  Feature 5
Rank 4 =  Feature 15
Rank 5 =  Feature 8

		*/
		
		//findLuceneRelevantBooks(qryBookNum);
	}

	public static List<BookDetails> generateOtherFeatureForAll() throws IOException {
		ChunkDetailsGenerator chunkImpl = new ChunkDetailsGenerator();
		List<BookDetails> books = chunkImpl.getChunksFromAllFiles();
		return books;
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