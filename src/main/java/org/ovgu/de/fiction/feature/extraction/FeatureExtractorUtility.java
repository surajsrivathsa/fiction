package org.ovgu.de.fiction.feature.extraction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;

import org.ovgu.de.fiction.model.BookDetails;
import org.ovgu.de.fiction.model.Chunk;
import org.ovgu.de.fiction.model.Concept;
import org.ovgu.de.fiction.model.Feature;
import org.ovgu.de.fiction.model.Word;
import org.ovgu.de.fiction.utils.FRConstants;
import org.ovgu.de.fiction.utils.FRGeneralUtils;

import com.google.common.base.Strings;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

/**
 * @author Suhita
 *         The class contains the methods for feature extraction
 */
public class FeatureExtractorUtility {

	/**
	 * @param chunks
	 * @return
	 * 		The method calculates average TTR over equal sized tokens
	 */
	public BigDecimal getAverageTTR(List<Chunk> chunks) {

		int chunkNo = chunks.size();
		float ratio = 0;
		for (Chunk chunk : chunks) {
			float totalWordsCount = chunk.getTokenListWithoutStopwordAndPunctuation().size();
			float distinctWordsCount = getDistinctWordCount(chunk.getTokenListWithoutStopwordAndPunctuation());
			chunk.setTokenListWithoutStopwordAndPunctuation(null);
			ratio = ratio + (distinctWordsCount / totalWordsCount);
		}
		return new BigDecimal((ratio * 100) / chunkNo).setScale(3, RoundingMode.CEILING);
	}

	private long getDistinctWordCount(List<String> tokens) {
		return tokens.parallelStream().distinct().count();

	}

	/**
	 * @param chunkNo
	 * @param paragraphCount
	 * @param raw
	 * @param stpwrdRmvd
	 * @param stpwrdPuncRmvd
	 * @param intrjctnCount
	 * @param hyphenCount
	 * @param semiColonCount
	 * @param colonCount
	 * @param periodCount
	 * @param commaCount
	 * @param coordConj
	 * @param locativePrepositionCount
	 * @param possPronounCount
	 * @param personalPronounCount
	 * @param femalePrpPosPronounCount
	 * @param malePrpPosPronounCount
	 * @param wordCountList
	 * @param numOfSyllables
	 * @param properWordCount
	 * @return
	 * 		The method generates the features for a chunk
	 */
	public Feature generateFeature(Integer chunkNo, Integer paragraphCount, Integer sentenceCount, List<Word> raw, List<Word> stpwrdRmvd,
			List<String> stpwrdPuncRmvd, double malePrpPosPronounCount, double femalePrpPosPronounCount, double personalPronounCount,
			double possPronounCount, double locativePrepositionCount, double coordConj, double commaCount, double periodCount,
			double colonCount, double semiColonCount, double hyphenCount, double intrjctnCount, double quoteCount,
			Map<Integer, Integer> wordCountList, int senti_negetiv, int senti_positiv, int senti_neutral, int properWordCount,
			int numOfSyllables) {

		Feature feature = new Feature();

		int totalCount = raw.size();
		double totalSenti = 0d;
		totalSenti = senti_negetiv + senti_positiv + senti_neutral;

		double fleschReadingScore = 206.835 - (1.015 * ((double) properWordCount / (double) sentenceCount))
				- 84.6 * ((double) numOfSyllables / (double) properWordCount); // as per wiki
		fleschReadingScore = fleschReadingScore/100; // normalise over 100
		if (paragraphCount != null)
			feature.setParagraphCount(new Double(paragraphCount) / new Double(totalCount));

		feature.setPersonalPronounRatio(personalPronounCount / totalCount);
		feature.setPossPronounRatio(possPronounCount / totalCount);
		feature.setFemalePrpPosPronounRatio(femalePrpPosPronounCount / (personalPronounCount + possPronounCount));
		feature.setMalePrpPosPronounRatio(malePrpPosPronounCount / (personalPronounCount + possPronounCount));
		feature.setLocativePrepositionRatio(locativePrepositionCount / totalCount);
		feature.setCoordConjunctionRatio(coordConj / totalCount);
		feature.setCommaRatio(commaCount / totalCount);
		feature.setPeriodRatio(periodCount / totalCount);
		feature.setColonRatio(colonCount / totalCount);
		feature.setSemiColonRatio(semiColonCount / totalCount);
		feature.setHyphenRatio(hyphenCount / totalCount);
		feature.setInterjectionRatio(intrjctnCount / totalCount);
		feature.setConversationRatio(quoteCount / totalCount); // considering 1 open 1 losing quote
																 // -a conversation
		feature.setConjunctionPunctuationRatio((coordConj + commaCount + colonCount + semiColonCount + hyphenCount) / totalCount);
		feature.setSentimentNegetiv((double) senti_neutral / totalSenti);
		feature.setSentimentPositiv((double) senti_positiv / totalSenti);
		feature.setSentimentNeutral((double) senti_negetiv / totalSenti);
		feature.setFleshReadingScore(fleschReadingScore);
		int count = wordCountList.values().parallelStream().mapToInt(Integer::intValue).sum();
		int product = 0;
		for (Integer key : wordCountList.keySet()) {
			product += key * wordCountList.get(key);
		}
		feature.setAverageSentenceLength(new Double(product) / new Double(count));
		return feature;
	}
	
	
	static List<String> splitAtNthOccurrence(String input, int n, String delimiter) {
	    List<String> pieces = new ArrayList<>();
	    // *? is the reluctant quantifier
	    String regex = Strings.repeat(".*?" + delimiter, n);
	    Matcher matcher = Pattern.compile(regex).matcher(input);

	    int lastEndOfMatch = -1;
	    while (matcher.find()) {
	        pieces.add(matcher.group());
	        lastEndOfMatch = matcher.end();
	    }
	    if (lastEndOfMatch != -1) {
	        pieces.add(input.substring(lastEndOfMatch));
	    }
	    return pieces;
	}

	/**
	 * @param books
	 * @throws IOException
	 *             The method writes the generated features to a csv,which will be later referenced.
	 *             Step 1: Loop thru the chunks containing features,
	 *             Step 2: Extract a numeric vector per chunk in an array
	 *             Step 3: Create a map with Key=BookId, Values = Feature Array of Doubles (per
	 *             chunk), mind
	 *             it!
	 *             Step 4: Compare the query book i-th chunk with all possible chunks of the the
	 *             corpus ,
	 *             and store the top 20 results, per chunk
	 *             Step 5: So, if query book has 12 chunks we have 12 results list (in a class)
	 */
	public static void writeFeaturesToCsv(List<BookDetails> books) throws IOException {

		Map<String, double[]> corpus = new HashMap<>(); // this corpus is (N*M) space, (each
														 // book of
														 // a corpus * #chunks of the book)
		double max_avg_senten_len = 1.00;
		double max_TTR = 1.00;
		double max_NUM_of_CHARS=1.00;

		double min_avg_senten_len = 1.00;
		double min_TTR = 1.00;
		double min_NUM_of_CHARS=1.00;
		
		double AVG_avg_senten_len = 1.00;
		double AVG_TTR = 1.00;
		double AVG_NUM_of_CHARS=1.00;
		
		double RUNNINGSUM_avg_senten_len = 0.00;
		double RUNNINGSUM_TTR = 0.00;
		double RUNNINGSUM_NUM_of_CHARS= 0.00;
		
		int row_count=0;
		
		for (BookDetails book : books) { // enter all features of all books (including
										 // the query book)
			List<Chunk> chunks = book.getChunks();
			String bookId = book.getBookId();

			for (Chunk chunk : chunks) { // enter a chunk of a book - each chunk is a "doc" for us!
										 // (including qry book)
							
				Feature feature = chunk.getFeature();
				/*
				String str = Chunk.getStrings(chunk.getTokenListWithoutStopwordAndPunctuation());
				//List<String> strArray = new ArrayList<>();
				/*List.partition(List.newArrayList(str.split("-")), 3)
			    .stream().map(strings -> strings.stream().collect(Collectors.joining("-")))
			    .forEach(System.out::println);
				String[] strArray = str.split(("(?<=\\G.* .* .* .* .* .* .* .* .* .* .*) "));
				System.out.println(strArray[0]);*/
				/*System.out.println(str);
				//List<String> strArray = new ArrayList<>();
				String[] strArray = str.split(" ");
				String temp = "";
				WordAttributeGenerator wag = new WordAttributeGenerator();
				for(int i=0; i<strArray.length;i++) {
					
					temp = temp + " " + strArray[i];
					if(i % 50 == 0)
					{
						temp = temp + ".";
						System.out.println(i + " " + temp);
						Concept cncpt2 = wag.generateWordAttributes2(temp);
						System.out.println("Size" + cncpt2.getCharacterMap().size());
						temp = "";
					}
				}*/
				//strArray = splitAtNthOccurrence(str, 50, " ");
				//System.out.println(strArray.get(0));
				/*List<String> appendedList = new ArrayList<String>();
				for (String s : strArray) {
				    appendedList.add(s + ".");
				}
				System.out.println(appendedList.get(0));
				*///WordAttributeGenerator wag = new WordAttributeGenerator();
				//Concept cncpt2 = wag.generateWordAttributes2(appendedList.get(0));
				//System.out.println("Size" + cncpt2.getCharacterMap().size());
				
				double[] feature_array = new double[FRConstants.FEATURE_NUMBER];
				
				row_count++; //row_count of all records, required for finding AVG values
				
				if(row_count==1){//Initialize MIN and AVG values
				min_avg_senten_len = feature.getAverageSentenceLength();	
				min_NUM_of_CHARS   = book.getNumOfChars();
				min_TTR            = book.getAverageTTR().doubleValue();
				}
							
				
				feature_array[FRConstants.PARAGRAPH_COUNT_0] = feature.getParagraphCount();
				feature_array[FRConstants.FEMALE_PR_1] = feature.getMalePrpPosPronounRatio();
				feature_array[FRConstants.MALE_PR_2] = feature.getFemalePrpPosPronounRatio();
				feature_array[FRConstants.PERSONAL_PR_3] = feature.getPersonalPronounRatio();
				feature_array[FRConstants.POSS_PR_4] = feature.getPossPronounRatio();
				feature_array[FRConstants.PREPOSITION_5] = feature.getLocativePrepositionRatio();
				feature_array[FRConstants.CONJUNCTION_6] = feature.getCoordConjunctionRatio();
				feature_array[FRConstants.COMMA_7] = feature.getCommaRatio();
				feature_array[FRConstants.PERIOD_8] = feature.getPeriodRatio();
				feature_array[FRConstants.COLON_9] = feature.getColonRatio();
				feature_array[FRConstants.SCOLON_10] = feature.getSemiColonRatio();
				feature_array[FRConstants.HYPHEN_11] = feature.getHyphenRatio();
				feature_array[FRConstants.INTERJECTION_12] = feature.getInterjectionRatio();
				feature_array[FRConstants.CONJ_PUNC_13] = feature.getConjunctionPunctuationRatio();
				feature_array[FRConstants.SENTENCE_L_14] = feature.getAverageSentenceLength(); // need
																								 // to
				// normalize,
				// across
				// corpus
				feature_array[FRConstants.QUOTES_15] = feature.getConversationRatio();
				feature_array[FRConstants.SENTI_N_16] = feature.getSentimentNegetiv();
				feature_array[FRConstants.SENTI_P_17] = feature.getSentimentPositiv();
				feature_array[FRConstants.SENTI_NEU_18] = feature.getSentimentNeutral();
				feature_array[FRConstants.FLSH_RS_19] = feature.getFleshReadingScore();
				if(max_avg_senten_len < feature_array[FRConstants.SENTENCE_L_14])
					max_avg_senten_len = feature_array[FRConstants.SENTENCE_L_14]; // max Avg. sentence length across corpus
				if(min_avg_senten_len > feature_array[FRConstants.SENTENCE_L_14])
					min_avg_senten_len = feature_array[FRConstants.SENTENCE_L_14];
				RUNNINGSUM_avg_senten_len = RUNNINGSUM_avg_senten_len+feature_array[FRConstants.SENTENCE_L_14];
				
				feature_array[FRConstants.NUM_CHARS_20] = book.getNumOfChars(); //global_vector_element, normalize_NUM_of_Chars
				if (max_NUM_of_CHARS < feature_array[FRConstants.NUM_CHARS_20])
					max_NUM_of_CHARS = feature_array[FRConstants.NUM_CHARS_20]; 
				if (min_NUM_of_CHARS > feature_array[FRConstants.NUM_CHARS_20])
					min_NUM_of_CHARS = feature_array[FRConstants.NUM_CHARS_20]; 
				RUNNINGSUM_NUM_of_CHARS = RUNNINGSUM_NUM_of_CHARS+feature_array[FRConstants.NUM_CHARS_20];
				
				feature_array[FRConstants.TTR_21] = book.getAverageTTR().doubleValue(); //global_vector_element, normalize_TTR
				if (max_TTR < feature_array[FRConstants.TTR_21])
					max_TTR = feature_array[FRConstants.TTR_21]; 
				if (max_TTR > feature_array[FRConstants.TTR_21])
					max_TTR = feature_array[FRConstants.TTR_21];
				RUNNINGSUM_TTR = RUNNINGSUM_TTR+feature_array[FRConstants.TTR_21];
				//feature_array[FRConstants.MAX_NUM] = book.getmax();
				feature_array[FRConstants.CHAR_RAT] = book.getratio();
				feature_array[FRConstants.DIALOG_RAT] = book.getdialogratio();
				//System.out.println("Test" + book.getdialogratio());
				// add each "doc" and its feature vector array
				corpus.put(bookId + "-" + chunk.getChunkNo(), feature_array);
			}
		}

		AVG_avg_senten_len = RUNNINGSUM_avg_senten_len/row_count;
		AVG_NUM_of_CHARS = RUNNINGSUM_NUM_of_CHARS/row_count;
		AVG_TTR = RUNNINGSUM_TTR/row_count;
		
		// Note: corpus is now normalized, and the same is written into csv file
		write(corpus, max_avg_senten_len,max_NUM_of_CHARS,max_TTR, min_avg_senten_len,min_NUM_of_CHARS,min_TTR,AVG_avg_senten_len,AVG_NUM_of_CHARS,AVG_TTR);

	}

	private static Map<String, double[]> write(Map<String, double[]> corpus, double max_avg_senten_len, double max_NUM_of_CHARS, double max_TTR, double min_avg_senten_len, double min_NUM_of_CHARS, double min_TTR, double aVG_avg_senten_len, double aVG_NUM_of_CHARS, double aVG_TTR) throws IOException {
		Map<String, double[]> corpus_normalized = new HashMap<>();
		double dummy = 10000.0000;
		String FEATURE_CSV_FILE = FRGeneralUtils.getPropertyVal("file.feature");
		String FEATURE_ARFF_FILE = FRGeneralUtils.getPropertyVal("file.wekafeature");
		try (FileWriter fileWriter = new FileWriter(FEATURE_CSV_FILE);) {

			fileWriter.append(FRConstants.FILE_HEADER.toString());
			fileWriter.append(FRConstants.NEW_LINE);

			for (Map.Entry<String, double[]> chunk_features : corpus.entrySet()) {
				fileWriter.append(String.valueOf(chunk_features.getKey()) + FRConstants.COMMA);// 'pg547-1'
																								// -
																								// First
																								// element,
																								// Primary
																								// Key
				double[] feature_vector = chunk_features.getValue(); // feature vector per book
																	 // chunk
				for (int j = 0; j < feature_vector.length; j++) {// loop over a vector

					if (j != feature_vector.length - 1) {// for all Feature vector's -'j'th-element F1, F2..except last index
						if (j == FRConstants.SENTENCE_L_14) {// normalize Avg. Sentence Length ,
																// 14th element of array
							fileWriter.append(String.format("%.4f", Math.round(((feature_vector[j]-aVG_avg_senten_len) / (max_avg_senten_len-min_avg_senten_len)) * dummy) / dummy)+ FRConstants.COMMA);
							//fileWriter.append(String.format("%.4f", Math.round((feature_vector[j] / max_avg_senten_len) * dummy) / dummy)+ FRConstants.COMMA);
							feature_vector[j] = Math.round((feature_vector[j] / max_avg_senten_len) * dummy) / dummy;
						}
						if(j == FRConstants.NUM_CHARS_20){//normlize
							fileWriter.append(String.format("%.4f", Math.round(((feature_vector[j]-aVG_NUM_of_CHARS) / (max_NUM_of_CHARS-min_NUM_of_CHARS)) * dummy) / dummy)	+ FRConstants.COMMA);
							//fileWriter.append(String.format("%.4f", Math.round((feature_vector[j] / max_NUM_of_CHARS) * dummy) / dummy)	+ FRConstants.COMMA);
							feature_vector[j] = Math.round((feature_vector[j] / max_NUM_of_CHARS) * dummy) / dummy;
						}
						if(j != FRConstants.NUM_CHARS_20 && j != FRConstants.SENTENCE_L_14){  // do not normalize others
							fileWriter.append(String.format("%.4f", Math.round(feature_vector[j] * dummy) / dummy) + FRConstants.COMMA);
						}
					} else{// for last element, no comma!, but normalize, and then put '\n'
						fileWriter.append(String.format("%.4f", Math.round(((feature_vector[j]-aVG_TTR)/ (max_TTR-min_TTR))* dummy) / dummy) + FRConstants.NEW_LINE);
						//fileWriter.append(String.format("%.4f", Math.round((feature_vector[j]/ max_TTR)* dummy) / dummy) + FRConstants.NEW_LINE);
						feature_vector[j] = Math.round((feature_vector[j] / max_TTR) * dummy) / dummy;
					}
				}
				corpus_normalized.put(chunk_features.getKey(), feature_vector);
			}
			//now write the csv to ARFF format for weka
			//writeCSVtoARFF(FEATURE_CSV_FILE,FEATURE_ARFF_FILE);
			fileWriter.flush();
		}
		return corpus_normalized;

	}

	public static void writeCSVtoARFF(String source_CSV_FILE, String target_ARFF_FILE) throws IOException {
		 // load CSV
		try{
			CSVLoader loader = new CSVLoader();
			 loader.setSource(new File(source_CSV_FILE));//source_csv
			 Instances data = loader.getDataSet();
			 
			 // save ARFF
			 ArffSaver saver = new ArffSaver();
			 saver.setInstances(data);
			 saver.setFile(new File(target_ARFF_FILE));//target_arff
			 saver.writeBatch();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		 
		 
		
	}

	/**
	 * @author Sayantan, changes for character feature
	 * @param charMap
	 * @return
	 */
	public Map<String, Integer> getUniqueCharacterMap(Map<String, Integer> charMap) {

		List<String> all_names = new ArrayList<String>(charMap.keySet());

		Map<String, Integer> charMapClone = new HashMap<>(charMap);

		for (String names : all_names) {

			for (Map.Entry<String, Integer> inputMap : charMap.entrySet()) {
				if (!names.equals(inputMap.getKey())) {
					String[] outerName = names.split(" ");
					String[] innerName = inputMap.getKey().split(" ");
					String FNAME_OUTER = outerName[0];
					String LNAME_OUTER = "";
					for (int i = 1; i < outerName.length; i++) { // start_at_index_1!
						LNAME_OUTER = LNAME_OUTER + outerName[i];
					}

					String FNAME_INNER = innerName[0];
					String LNAME_INNER = "";
					for (int i = 1; i < innerName.length; i++) { // start_at_index_1!
						LNAME_INNER = LNAME_INNER + innerName[i];
					}

					/*
					 * case 1 : {Sayan, Sayantan, Sayantan Polley} w/o "." | op
					 * = Sayantan Polley All abosrbed into Single output(op)
					 * "char", i.e. remove "Sayan" and "Sayantan" from the
					 * charMap object and retain "Sayantan Polley" the biggest
					 * not that this sequence shud be avoided : {M. Prosper, M.
					 * Ferdinand}, here even though "M." region matches!
					 */
					if (!FNAME_OUTER.contains(".")) {
					  // case 1: {Sayantan Polley, Sayan Polley} | op = Sayantan Polley
						if (FNAME_OUTER.length() >= FNAME_INNER.length() && FNAME_OUTER.contains(FNAME_INNER) && LNAME_OUTER.equals(LNAME_INNER)) {
							if (names.length() > inputMap.getKey().length()) {
								Integer old = charMapClone.get(names);
								Integer newval = inputMap.getValue();
								charMapClone.put(names, old!=null ? old + newval : newval);
								charMapClone.remove(inputMap.getKey()); // remove smaller - inner- 'Sayan Polley'
							}
							else
								charMapClone.remove(names);
						}
						/*
						 * case 2: {Sayantan Polley, Polley} | op = Sayantan Polley
						 * Surname Absorbed into Single "char", if SNAME = FNAME,
						 * i.e delete "Polley" from charMap
						 */
						if (outerName.length >= 2 && innerName.length == 1 && LNAME_OUTER.contains(FNAME_INNER)) { // &&
							/*																						 // !LNAME_INNER.equals("")
							Integer old = charMapClone.get(names);
							Integer newval = inputMap.getValue();
							charMapClone.put(names, old!=null ? old + newval : newval);
							*/
							charMapClone.remove(inputMap.getKey()); // Note:innerName.length==1,
																	 // doesnt combine/delete : Sayan Ghosh and Suhita Ghosh
						}
						
						/**
						 * case 3: {Sayantan Polley, Sayan} | op = Sayantan Polley
						 */
						if(outerName.length >= 2 && innerName.length == 1 && FNAME_OUTER.contains(FNAME_INNER)){
							Integer old = charMapClone.get(names);
							Integer newval = inputMap.getValue();
							charMapClone.put(names, old!=null ? old + newval : newval);
							charMapClone.remove(inputMap.getKey());
						}
					} else { // we deal with "." dots here below
						/*
						 * case 4: { S. Ghoshh, Suhita Ghosh, Suhita Ghosh} | op
						 * = Suhita Ghoshh combine these dot names, if both of the first
						 * name starts with same alphabet and last name is same
						 */
						if (String.valueOf(FNAME_OUTER.charAt(0)).equals(String.valueOf(FNAME_INNER.charAt(0)))) {
							if ((LNAME_OUTER.contains(LNAME_INNER) || LNAME_INNER.contains(LNAME_OUTER))  //if (LNAME_OUTER.length() >= LNAME_INNER.length() && LNAME_OUTER.contains(LNAME_INNER)
									&& !LNAME_INNER.equals("") && !LNAME_OUTER.equals("")) {
								if (names.length() < inputMap.getKey().length()) {
									/*
									Integer old = charMapClone.get(names);
									Integer newval = inputMap.getValue();
									charMapClone.put(names, old!=null ? old + newval : newval);// remove smaller
									*/
									charMapClone.remove(names);
								} else {
									charMapClone.remove(inputMap.getKey());
								}
							}

						}

					}

					

					/*
					 * case 4: {S. Polley, S. Kumar Ghosh} | op = S. Polley, S.
					 * Kumar Ghosh Not absorbed, both different, although FNAME
					 * is same, SNAME diff, so do NOTHING!
					 */

					/*
					 * case 5 : {Sayantan Polley, Suhita Polley, Suhita Kumari
					 * Polley} | op = Sayantan Polley, Suhita Polley Not
					 * absorbed, both different, although SNAME is same, but
					 * FNAME is diff so do NOTHING!
					 */

				}
			}
		}
	//	System.out.println(" out edits ="+charMapClone);
		return charMapClone;
	}

}
