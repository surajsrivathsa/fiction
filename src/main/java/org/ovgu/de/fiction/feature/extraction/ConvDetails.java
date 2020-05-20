package org.ovgu.de.fiction.feature.extraction;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.Writer;
import java.io.FileWriter;

import org.apache.log4j.Logger;
import org.ovgu.de.fiction.model.BookDetails;
import org.ovgu.de.fiction.model.Chunk;
import org.ovgu.de.fiction.model.Concept;
import org.ovgu.de.fiction.model.Feature;
import org.ovgu.de.fiction.model.Word;
import org.ovgu.de.fiction.utils.FRConstants;
import org.ovgu.de.fiction.utils.FRFileOperationUtils;
import org.ovgu.de.fiction.utils.FRGeneralUtils;
import org.ovgu.de.fiction.utils.StanfordPipeline;




public class ConvDetails {
	
	
	public static int isNull(String str) {
        return str == null ? -1 : 0;
    }
	
	
	public int calcConv(List<Word> wordList) {
		int convocount = 0, i = 0;
		for(i = 0; i < wordList.size(); i++) {
			if(wordList.get(i).getOriginal().equals("\"") || wordList.get(i).getOriginal().equals("»")) {
				for(int m = i-7; m <= i+7 ; m++)
					{
					//flag++;
					if( m >=0 && m < wordList.size())
					{
						//System.out.println("word = " + wordList.get(m).getNer() +"   " + wordList.get(m).getOriginal());
						if( isNull(wordList.get(m).getNer()) == 0  && (wordList.get(m).getNer().equals("PERSON")
								|| wordList.get(m).getLemma().equals("he")
								|| wordList.get(m).getLemma().equals("she"))) 
							{
							convocount++;
							m = i + 8;
							}
					}
					}
				
				}
				}
		//System.out.println("word count = " + wc);
		return convocount;
	}
	
	//calculate quoted word count
	
	public int quotedwordcount(List<Word> wordList) {
		int i, wc =0, flag = 0 ;
		for(i = 0; i < wordList.size(); i++) {
			if(wordList.get(i).getOriginal().equals("\"") || wordList.get(i).getOriginal().equals("»") || wordList.get(i).getOriginal().equals("«")) 
			{
				flag ++;
				//wc++;
			}
			if(flag % 2 != 0  && flag != 0 && !wordList.get(i).getOriginal().equals("'")
					&& !wordList.get(i).getOriginal().equals(",")
					&& !wordList.get(i).getOriginal().equals(".")
					&& !wordList.get(i).getOriginal().equals("<s>")
					&& !wordList.get(i).getOriginal().equals("<p>")
					&& !wordList.get(i).getOriginal().equals(";")
					&& !wordList.get(i).getOriginal().equals("'"))
				wc++;
		}
		return wc;
	}
	
	public int wordcount(List<Word> wordList) {
		int i, wc =0;
		for(i = 0; i < wordList.size(); i++) {
			if(!wordList.get(i).getOriginal().equals("'")
				&& !wordList.get(i).getOriginal().equals(",")
				&& !wordList.get(i).getOriginal().equals(".")
				&& !wordList.get(i).getOriginal().equals("<s>")
				&& !wordList.get(i).getOriginal().equals("<p>")
				&& !wordList.get(i).getOriginal().equals(";")
				&& !wordList.get(i).getOriginal().equals("'")
				&& !wordList.get(i).getOriginal().equals("\"")
				&& !wordList.get(i).getOriginal().equals("-")
				&& !wordList.get(i).getOriginal().equals("_")
				&& !wordList.get(i).getOriginal().equals("'s"))
				wc++;
		}
		return wc;
	}
	
	public double convRatio(List<Word> wordList) {
		//double ratio = 0.0;
		int m1 = calcConv(wordList);
		int m2 = quotedwordcount(wordList);
		int m3 = wordcount(wordList);
		
		//System.out.println("dialog count = "+ m1 + "ratio=" + (double)m2/m3);
		return ((double)m2/m3);
	}
	

}
