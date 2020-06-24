	package org.ovgu.de.fiction;

import java.io.IOException;
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

import me.xdrop.fuzzywuzzy.FuzzySearch;

/**
 * @author Aditya
 */
public class FictionRetrievalDriver {

	

	public static void main(String[] args) throws Exception {
		
		/*
		System.out.println(FuzzySearch.ratio("fry","try"));
		System.out.println(FuzzySearch.ratio("prison","prisoner"));
		System.out.println(FuzzySearch.ratio("mutiny","mutineer"));
		System.out.println(FuzzySearch.ratio("bounty","bountiful"));
		System.out.println(FuzzySearch.ratio("except","accept"));
		System.out.println(FuzzySearch.ratio("release","realize"));
		System.out.println(FuzzySearch.ratio("doom","mood"));
		System.out.println(FuzzySearch.ratio("reason","treason"));
		*/
		
		long start = System.currentTimeMillis();

		/* 1> Extract content from Gutenberg corpus - one time */
		//ContentExtractor.generateContentFromAllEpubs();
		System.out.println("Time taken for generating content (min)-" + (System.currentTimeMillis() - start) / (1000 * 60));

		start = System.currentTimeMillis();
		/* 2> Generate features from the extracted content - one time */
		List<BookDetails> features = generateOtherFeatureForAll();
		System.out.println("Time taken for feature extraction and chunk generation (min)-" + (System.currentTimeMillis() - start) / (1000 * 60));
		start = System.currentTimeMillis();

		/* 3> Write features to CSV - one time */
		FeatureExtractorUtility.writeFeaturesToCsv(features);
		start = System.currentTimeMillis();
		System.out.println("Time taken for writing to CSV (min)-" + (System.currentTimeMillis() - start) / (1000 * 60));
		
		//  4> Query 
		String query_book = "pg11";
		int len = FRConstants.EMO_WEIGHT.length;
		/*
		for(int i = 0; i < len; i++)
		{
			String qryBookNum = query_book; 
			System.out.println("Querying for book : " + qryBookNum);
			String FEATURE_CSV_FILE = FRGeneralUtils.getPropertyVal("file.feature");
			System.out.println("==========================================================================");
			System.out.println("Running for index" + i);
			
			TopKResults topKResults = FictionRetrievalSearch.findRelevantBooks(qryBookNum, FEATURE_CSV_FILE, 
					FRConstants.SIMI_PENALISE_BY_CHUNK_NUMS, FRConstants.SIMI_ROLLUP_BY_ADDTN, 
					FRConstants.SIMI_INCLUDE_TTR_NUMCHARS,FRConstants.TOP_K_RESULTS,FRConstants.SIMILARITY_L2,i);

			System.out.println("==========================================================================");		
			
			//InterpretSearchResults interp = new InterpretSearchResults();
			//interp.performStatiscalAnalysis(topKResults);
			
			//@suraj: 
			InterpretSearchResults interp = new InterpretSearchResults();
			//interp.performStatiscalAnalysis(topKResults);
			interp.performStatiscalAnalysisUsingRegression(topKResults,i, FRConstants.SIMI_INCLUDE_TTR_NUMCHARS);
			//findLuceneRelevantBooks(qryBookNum);
			 
			
		}
		*/
	
		
		// 5> Perform some machine learning over the results
		 
		
		//InterpretSearchResults interp = new InterpretSearchResults();
		//interp.performStatiscalAnalysis(topKResults);
		//findLuceneRelevantBooks(qryBookNum);
		
	}

	public static List<BookDetails> generateOtherFeatureForAll() throws IOException {
		ChunkDetailsGenerator chunkImpl = new ChunkDetailsGenerator();
		List<BookDetails> books = chunkImpl.getChunksFromAllFiles();
		return books;
	}

	

	

	

	
	
}
