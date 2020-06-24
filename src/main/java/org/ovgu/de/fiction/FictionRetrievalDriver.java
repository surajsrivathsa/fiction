package org.ovgu.de.fiction;

import java.io.IOException;
import java.util.List;

//import org.ovgu.de.fiction.feature.extraction.ChunkDetailsGenerator;
import org.ovgu.de.fiction.feature.extraction.FeatureExtractorUtility;
import org.ovgu.de.fiction.model.BookDetails;
import org.ovgu.de.fiction.model.TopKResults;
//import org.ovgu.de.fiction.preprocess.ContentExtractor;
import org.ovgu.de.fiction.search.FictionRetrievalSearch;
import org.ovgu.de.fiction.search.InterpretSearchResults;
import org.ovgu.de.fiction.utils.FRConstants;
import org.ovgu.de.fiction.utils.FRGeneralUtils;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.*;

/**
 * @author Suhita, Sayantan
 */
@SpringBootApplication(scanBasePackages = {"org.ovgu.de.fiction.web", "org.ovgu.de.fiction.utils"}) 

public class FictionRetrievalDriver extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(FictionRetrievalDriver.class).web(WebApplicationType.NONE);
	}


	public static void main(String[] args) throws Exception {

		long start = System.currentTimeMillis();

		SpringApplication.run(FictionRetrievalDriver.class, args);

		/* 1> Extract content from Gutenberg corpus - one time */
		//ContentExtractor.generateContentFromAllEpubs();
		System.out.println(
				"Time taken for generating content (min)-" + (System.currentTimeMillis() - start) / (1000 * 60));

		start = System.currentTimeMillis();
		/* 2> Generate features from the extracted content - one time */
		//List<BookDetails> features = generateOtherFeatureForAll();
		System.out.println("Time taken for feature extraction and chunk generation (min)-"
				+ (System.currentTimeMillis() - start) / (1000 * 60));
		start = System.currentTimeMillis();

		/* 3> Write features to CSV - one time */
		// FeatureExtractorUtility.writeFeaturesToCsv(features);
		start = System.currentTimeMillis();
		System.out.println("Time taken for writing to CSV (min)-" + (System.currentTimeMillis() - start) / (1000 * 60));

		/* 4> Query */
		String qryBookNum = "pg11"; // pg11CarolAlice, pg1400DickensGreatExp,pg766DickensDavidCopfld
														// pg2701HermanMobyDick,pg537DoyleTerrorTales
		// pg13720HermanVoyage1, pg2911Galsw2, pg1155Agatha2,pg2852DoyleHound,
		// pg2097DoyleSignFour

		// read from csv features and prints ranked relevant books, run after CSV is
		// written
		String FEATURE_CSV_FILE = FRGeneralUtils.getPropertyVal("file.feature");
		// Config 1: three possible setting similarity objective penalization: divide
		// chunks by (1) OR (number_of_chunks) OR sqr_root(number_of_chunks)
		// Config 2: two possible settings for similarity roll up : add_chunks (default)
		// OR multipl_chunks
		// Config 3: Include or exclude TTR and Numbr of Chars

		TopKResults topKResults = FictionRetrievalSearch.findRelevantBooks(qryBookNum, FEATURE_CSV_FILE,
				FRConstants.SIMI_PENALISE_BY_CHUNK_NUMS, FRConstants.SIMI_ROLLUP_BY_ADDTN,
				FRConstants.SIMI_INCLUDE_TTR_NUMCHARS, FRConstants.TOP_K_RESULTS, FRConstants.SIMILARITY_L2,FRConstants.CONFIGINDEX);
		System.out.println("Books from AI :" + topKResults.getResults_topK());
	
		/*
		 * * 5> Perform some machine learning over the results
		 * 
		 */

		InterpretSearchResults interp = new InterpretSearchResults();
		//interp.performStatiscalAnalysis(topKResults);
		interp.performStatiscalAnalysisUsingRegression(topKResults,4, FRConstants.SIMI_INCLUDE_TTR_NUMCHARS);
		// findLuceneRelevantBooks(qryBookNum);

	}

	//public static List<BookDetails> generateOtherFeatureForAll() throws IOException {
		//ChunkDetailsGenerator chunkImpl = new ChunkDetailsGenerator();
		//List<BookDetails> books = chunkImpl.getChunksFromAllFiles();
		//return books;
//	}

}
