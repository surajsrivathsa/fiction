package org.ovgu.de.fiction;

import org.apache.commons.exec.*;
import org.apache.commons.exec.launcher.*;
import java.io.*;
import java.util.ArrayList;
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
		

		/* 1> Extract content from Gutenberg corpus - one time */
		
		ContentExtractor.generateContentFromAllEpubs();
		System.out.println("Time taken for generating content (min)-" + (System.currentTimeMillis() - start) / (1000 * 60));

		start = System.currentTimeMillis();
		
		
		/* 2> Generate features from the extracted content - one time */
		
		/* Commenting below block as i needed to extract only html output. Un comment when required
		List<BookDetails> features = generateOtherFeatureForAll();
		System.out.println("Time taken for feature extraction and chunk generation (min)-" + (System.currentTimeMillis() - start) / (1000 * 60));
		start = System.currentTimeMillis();
		
		*/

		/* 3> Write features to CSV - one time */
		
		/* Commenting below block as i needed to extract only html output. Un comment when required
		FeatureExtractorUtility.writeFeaturesToCsv(features);
		 start = System.currentTimeMillis();
		System.out.println("Time taken for writing to CSV (min)-" + (System.currentTimeMillis() - start) / (1000 * 60));
		*/
		
		
		/*
		 * @Suraj: Adding feature 3 extraction program calling from java to python
		 */
		
		extract_Feature3();
		
		
		/* 4> Query */
		/* Commenting below block as i needed to extract only html output. Un comment when required
		String qryBookNum = "pg1400DickensGreatExp"; 
		*/
		
		//pg11CarolAlice,  pg1400DickensGreatExp,pg766DickensDavidCopfld
		// pg2701HermanMobyDick,pg537DoyleTerrorTales
		// pg13720HermanVoyage1, pg2911Galsw2, pg1155Agatha2,pg2852DoyleHound, pg2097DoyleSignFour

		// read from csv features and prints ranked relevant books, run after CSV is written
		
		/* Commenting below block as i needed to extract only html output. Un comment when required
		String FEATURE_CSV_FILE = FRGeneralUtils.getPropertyVal("file.feature");
		*/
		
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
		interp.performStatiscalAnalysis(topKResults);
		*/
		
		//findLuceneRelevantBooks(qryBookNum);
	}

	public static List<BookDetails> generateOtherFeatureForAll() throws IOException {
		ChunkDetailsGenerator chunkImpl = new ChunkDetailsGenerator();
		List<BookDetails> books = chunkImpl.getChunksFromAllFiles();
		return books;
	}

	public static void extract_Feature3() throws ExecuteException, IOException, InterruptedException {
		 CommandLine cmdLine = new CommandLine("sh");
		 cmdLine.addArgument("run_python_jobs.sh");
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
		 DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

		 // ExecuteWatchdog watchdog = new ExecuteWatchdog(60*1000);
		 Executor executor = new DefaultExecutor();
		
		 executor.setExitValue(1);
		 //executor.setWatchdog(watchdog);
		 executor.execute(cmdLine, resultHandler);

		 // some time later the result handler callback was invoked so we
		 // can safely request the exit value
		 resultHandler.waitFor();	 
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
