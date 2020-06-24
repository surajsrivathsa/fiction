package org.ovgu.de.fiction.feature.extraction;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.Writer;
import java.io.BufferedReader;
import java.io.FileReader;
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

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentAnnotatedTree;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

/**
 * @author Aditya
 */
public class ChunkDetailsGenerator {

	final static Logger LOG = Logger.getLogger(ChunkDetailsGenerator.class);

	private static Set<String> LOCATIVE_PREPOSITION_LIST;
	protected static Integer CHUNK_SIZE;
	protected static Integer TTR_CHUNK_SIZE;
	protected static String OUT_FOLDER_TOKENS;
	private static long TIME ;
	private int BOOK_NO;
	private int NUM_OF_CHARS_PER_BOOK = -1;
	private String CONTENT_EXTRCT_FOLDER;
	StanfordCoreNLP SENTI_PIPELINE_EN, SENTI_PIPELINE_DE;
	private int max = 0, sum = 0;
	private double ratio = 0.0;
	private double dialogratio = 0.0;
	private Map<String, String> bigcharMap = new HashMap<>();
	//@suraj: added hashmap to read and store polarity dictionary
	private static HashMap<String, Object> polarity_clues_dict = new HashMap<String, Object>();
	private static String pathToPCS = FRConstants.GERMAN_POLARITY_FILE;
	private static GermanSentimentData dummy_sentiment_object = new GermanSentimentData("dummy", "dummy", "dummy", "neutral", 0.0, "D");
	protected void init() throws NumberFormatException, IOException {
		
		CHUNK_SIZE = Integer.parseInt(FRGeneralUtils.getPropertyVal(FRConstants.CHUNK_SIZE));
		TTR_CHUNK_SIZE = Integer.parseInt(FRGeneralUtils.getPropertyVal(FRConstants.CHUNK_SIZE_FOR_TTR));

		OUT_FOLDER_TOKENS = FRGeneralUtils.getPropertyVal(FRConstants.OUT_FOLDER_TOKENS);
		CONTENT_EXTRCT_FOLDER = FRGeneralUtils.getPropertyVal(FRConstants.OUT_FOLDER_CONTENT);

		LOCATIVE_PREPOSITION_LIST = FRGeneralUtils.getPrepositionList();
		BOOK_NO = 0;
		TIME = System.currentTimeMillis();

		SENTI_PIPELINE_EN = StanfordPipeline.getPipeline(FRConstants.STNFRD_SENTI_ANNOTATIONS,"");
		SENTI_PIPELINE_DE = StanfordPipeline.getPipeline(FRConstants.STNFRD_SENTI_ANNOTATIONS,"");
	}

	/**
	 * @return
	 * @throws IOException
	 */
	public List<BookDetails> getChunksFromAllFiles() throws IOException {
		init();

		List<BookDetails> books = new ArrayList<>();
		FeatureExtractorUtility feu = new FeatureExtractorUtility();
		//read the german polarity file and store it in class variable
		createGermanPolarityMap();
		// following loop runs, over path of each book
		FRFileOperationUtils.getFileNames(CONTENT_EXTRCT_FOLDER).stream().forEach(file -> {
			String fileName = file.getFileName().toString().replace(FRConstants.CONTENT_FILE, FRConstants.NONE);

			try {
				BookDetails book = new BookDetails();
				book.setBookId(fileName);
				System.out.println(" ======================== ======================= ========================");
				System.out.println("Book language: " + FRGeneralUtils.getMetadata(fileName).getLanguage() + " Authors: " + FRGeneralUtils.getMetadata(fileName).getAuthors() + " Book name: " + FRGeneralUtils.getMetadata(fileName).getTitles());
				System.out.println(" ======================== ======================= ========================");
				book.setMetadata(FRGeneralUtils.getMetadata(fileName));
				//Added below 3 lines to retrieve book language from metadata, set default language to english for other unknown language books
				
				String book_lang = book.getMetadata().getLanguage();
				
				if(!book_lang.equals(FRConstants.BOOK_LANG_DE) && !book_lang.equals(FRConstants.BOOK_LANG_EN))
					book_lang = FRConstants.BOOK_LANG_EN;
				book.setChunks(getChunksFromFile(file.toString(),book, book_lang)); // this is a
																	 // list of
																	 // chunks,
																	 // each
																	 // chunk
																	 // again has
																	 // a feature
																	 // object/vector
				book.setAverageTTR(feu.getAverageTTR(getEqualChunksFromFile(getTokensFromAllChunks(book.getChunks()))));
				book.setNumOfChars(NUM_OF_CHARS_PER_BOOK == 0 ? 1 : NUM_OF_CHARS_PER_BOOK);
				//System.out.println("xd"+max+" " +NUM_OF_CHARS_PER_BOOK+ " " +ratio);
				book.setmax(max);
				book.setratio(ratio);
				book.setdialogratio(dialogratio);
				books.add(book);

				 //LOG.debug("End Generate token for : " + fileName + " " + ((System.currentTimeMillis() - TIME) / 1000));
				TIME = System.currentTimeMillis();

				System.out.println(++BOOK_NO + " > " + book.toString());

			} catch (IOException e) {
				LOG.error("IOException in generating chunks -" + e.getMessage() + " for book " + fileName);
			} catch (ArrayIndexOutOfBoundsException ai) {
				LOG.error("ArrayIndexOutOfBoundsException in generating chunks -" + ai.getMessage() + " for book " + fileName);
			} catch (Exception e) {
				// LOG.error("Error in generating chunks -" + e.getMessage() + " for book " +
				// fileName);
				e.printStackTrace();
			}

		});
		
		for (Entry<String,String> c : bigcharMap.entrySet()) {
			LOG.info(c.getKey()+" "+c.getValue());
			
		}
		
		String eol = System.getProperty("line.separator");

		try (Writer writer = new FileWriter("/Users/surajshashidhar/git/fiction/milestone4/charmap_m4_b10.csv")) {
		  for (Map.Entry<String, String> entry : bigcharMap.entrySet()) {
		    writer.append(entry.getKey())
		          .append(',')
		          .append(entry.getValue())
		          .append(eol);
		  }//pg19225
		} catch (IOException ex) {
		  ex.printStackTrace(System.err);
		}
		
		return books;
	}

	/**
	 * @author Suhita, Sayantan
	 * @see - The method generates List of Chunk out of the file passed in the
	 *      signature. this path is a path to a single book, mind it!
	 * @param path
	 *            to book location
	 * @return : List of Chunks, Chunk has a feature vector object
	 * @throws IOException
	 */
	public List<Chunk> getChunksFromFile(String path, BookDetails cbook, String book_lang) throws IOException {
		BookDetails book = new BookDetails();
		
		String fileName = path.replace(FRConstants.CONTENT_FILE, FRConstants.NONE);
		//String filename = path.replace("[^\\\\]+(?=$)","");
		int m= fileName.lastIndexOf('\\');
		String filename = fileName.substring(m+1);
		//System.out.println(filename);
		book.setBookId(filename);
		System.out.println("Current file is: " +fileName);
		int batchNumber;
		max = 0;
		sum = 0;
		ratio = 0;
		List<Chunk> chunksList = new ArrayList<>();
		Annotation annotation = null;
		
		//System.out.println("Language inside getchunksfromfile is: " + book_lang);
		
		Map<String, String> bigcharMap = new HashMap<>();
		WordAttributeGenerator wag = new WordAttributeGenerator();
		FeatureExtractorUtility feu = new FeatureExtractorUtility();
		List<String> stopwords = Arrays.asList(FRGeneralUtils.getPropertyVal(FRConstants.STOPWORD_FICTION).split("\\|"));
		Concept cncpt = wag.generateWordAttributes(Paths.get(path)); // this is
																	 // a
																	 // "word-token-pos-ner"
																	 // list
																	// of
																	 // whole
																	 // book!

		// dummu
		//Concept cncpt2 = wag.generateWordAttributes2(Paths.get(path));
		for (Entry<String,Integer> c : cncpt.getCharacterMap().entrySet()) {
			LOG.info(c.getKey()+" "+c.getValue());
			bigcharMap.put(c.getKey(), book.getBookId());
		}
		//System.exit(0);
		NUM_OF_CHARS_PER_BOOK = cncpt.getCharacterMap().size();
		cncpt.getCharacterMap().entrySet().forEach(entry->{
			sum = sum + entry.getValue();
		    if(entry.getValue()>max)
		    	max = entry.getValue();
		 });
		ratio = (double)max/sum;
		LOG.info("Count of highest occurring character:" + max + "; Total character counts: " + sum + "; Main character presence ratio: " + ratio);
		List<Word> wordList = cncpt.getWords();
		int numOfSntncPerBook  = cncpt.getNumOfSentencesPerBook();
		
		ConvDetails conv = new ConvDetails();
		dialogratio = conv.convRatio(wordList);
		//LOG.info("Dialog Ratio = "+dialogratio);
		
		// String fileName =
		// Paths.get(path).getFileName().toString().replace(Constants.CONTENT_FILE, Constants.NONE);

		ParagraphPredicate filter = new ParagraphPredicate();
		List<Word> copy = new ArrayList<>(wordList);
		copy.removeIf(filter);
		int length = copy.size();

		int remainder = 0;

		if (length < CHUNK_SIZE) {
			batchNumber = 0;
			remainder = length;
		} else {
			batchNumber = length / CHUNK_SIZE;
			remainder = (length % CHUNK_SIZE);
			if (remainder <= CHUNK_SIZE / 2) {
				batchNumber--;
				remainder = CHUNK_SIZE + remainder;
			}
		}

		List<Word> raw = new ArrayList<>();
		List<String> stpwrdPuncRmvd = new ArrayList<>();
		int wordcntr = 0;
		int chunkNo = 1;
		int paragraphCount = 0;
		int chunkSize = 0;
		Map<Integer, Integer> wordCountPerSntncMap = null;
		int senti_negetiv_cnt = 0;
		int senti_positiv_cnt = 0;
		int senti_neutral_cnt = 0;
		int wordCountPerSntnc = 0;
		int countselfnarr = 0;
		double totalNumOfRandomSntnPerChunk =0; // sentiment_calculated_over_these_randm_sentences_per_chunk
		
		
		if(batchNumber==0) //very_small_book
			totalNumOfRandomSntnPerChunk =  (FRConstants.PERCTG_OF_SNTNC_FOR_SENTIM * numOfSntncPerBook);
		else
			totalNumOfRandomSntnPerChunk = FRConstants.PERCTG_OF_SNTNC_FOR_SENTIM * ((numOfSntncPerBook)/(batchNumber));//10%_of_sentences_per_chunk
			
		for (int batchCtr = 0; batchCtr <= batchNumber; batchCtr++) { //loop_over_number_of_chunks_of_a_book

			chunkSize = batchCtr < batchNumber ? CHUNK_SIZE : remainder;

			double malePrpPosPronounCount = 0;
			double femalePrpPosPronounCount = 0;
			double personalPronounCount = 0;
			double possPronounCount = 0;
			double locativePrepositionCount = 0;
			double coordConj = 0;
			double commaCount = 0;
			double periodCount = 0;
			double colonCount = 0;
			double semiColonCount = 0;
			double hyphenCount = 0;
			double intrjctnCount = 0;
			double convCount = 0;
			int sentenceCount = 0;
			wordCountPerSntnc = 0;
			wordCountPerSntncMap = new HashMap<>();
			senti_negetiv_cnt = 0;
			senti_positiv_cnt = 0;
			senti_neutral_cnt = 0;
			int properWordCount = 0;
			int numOfSyllables = 0;
			int randomSntnCount =0;
			int icountnonquote = 0, icount = 0;
			int flag = 0;
			double dialogratiochunk = 0.0;
			StringBuffer sentenceSbf = new StringBuffer();
			
			//calculate ratio of quoted to total words and return
			
			//System.exit(0);
			
			for (int index = 0; index < chunkSize; index++) {// loop_over_tokens_of_a_given_chunk
				Word token = wordList.get(wordcntr);
				String l = token.getLemma();
				//System.out.println(token + " " + l);
				if (l.equals(FRConstants.P_TAG)) {
					paragraphCount++;
					wordcntr++;
					index--;
					continue;
				}
				if (l.equals(FRConstants.S_TAG)) {
					/**
					 * calculate sentiment for the previous formed sentence,
					 * the choice of selecting a sentence is totally random
					 * example, when the random number >5k and the num of selected sentences have not crossed , sampling bound = 10%_of_sentences_per_chunk
					 */
                    Random rnd = new Random();
                    int randNum = rnd.nextInt(FRConstants.RANDOM_SENTENCES_SENTIM_TOP_VAL); //get_an_INT_less_than_10k
                    
					
                    if (book_lang.equals(FRConstants.BOOK_LANG_EN) && sentenceSbf.toString().length()>0 && randNum<FRConstants.RANDOM_SENTENCES_SENTIM_MID_VAL && randomSntnCount<totalNumOfRandomSntnPerChunk) { // making_a_random_choice_here
						// calculateSenti as=>
						annotation = SENTI_PIPELINE_EN.process(sentenceSbf.toString());
						//System.out.println("Currently inside english sentiment pipeline");	
						int score = 2; // Default as Neutral. 1 = Negative, 2 =
						// Neutral, 3 = Positive
						//System.out.println(annotation);
						
						
						for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class))// ideally
						// this
						// loop
						// runs
						// once!
						{
							//System.out.println(sentence);
							Tree tree = sentence.get(SentimentAnnotatedTree.class);
							score = RNNCoreAnnotations.getPredictedClass(tree);
						}
						
						//@suraj: we recieve 5 types of score 0,1,2,3,4 from rnn/polarity back. Hence added the count capture condition for 0, 4 that was only looking for 1,2,3 previously
						if (score == 2)
							senti_neutral_cnt++;
						if (score <= 1)
							senti_negetiv_cnt++;
						if (score >= 3)
							senti_positiv_cnt++;
						
						randomSntnCount++;
						
						//System.out.println("English Sentiment Sentence is: " + sentenceSbf.toString() + " score: " + score);
					}
					
					
					//@suraj: handle german sentiment
					else if (book_lang.equals(FRConstants.BOOK_LANG_DE) && sentenceSbf.toString().length()>0 && randNum<FRConstants.RANDOM_SENTENCES_SENTIM_MID_VAL && randomSntnCount<totalNumOfRandomSntnPerChunk * 1.5) { // making_a_random_choice_here
						//System.out.println("Currently inside german sentiment pipeline");	
						//System.out.println("German Sentiment Sentence is: " + sentenceSbf.toString());
						int score = 2; // Default as Neutral. 1 = Negative, 2 =
						
						//@suraj: call the functions added at the bottom to get the sentiment score
						score = calculateSentimentperSentence(SentenceTokenizer(sentenceSbf.toString()));
						//@suraj: we recieve 5 types of score 0,1,2,3,4 from rnn/polarity back. Hence added the count capture condition for 0, 4 that was only looking for 1,2,3 previously
						if (score == 2)
							senti_neutral_cnt++;
						if (score <= 1)
							senti_negetiv_cnt++;
						if (score >= 3)
							senti_positiv_cnt++;
						
						randomSntnCount++;
						//System.out.println("German Sentiment Sentence is: " + sentenceSbf.toString() + " score: " + score);
					}
					//System.out.println(book_lang + " : " + sentenceSbf.toString().length() + " > 0 " + " : " + randNum + " < " + FRConstants.RANDOM_SENTENCES_SENTIM_MID_VAL + " : " + randomSntnCount + " < " + totalNumOfRandomSntnPerChunk);
					
					// reset the sentence buffer for next sentence
					sentenceSbf = new StringBuffer();

					/* partly end of sentiment calculation */
					if (wordCountPerSntnc != 0) {
						addToWordCountMap(raw, wordCountPerSntncMap, wordCountPerSntnc);
					}
					wordCountPerSntnc = 0;
					sentenceCount++;
					wordcntr++;
					index--;
					continue;
				}
				
				
				
				
				/* append the token to form sentence. Below part needed for sentiment calculation */

				else if (!l.equals(FRConstants.S_TAG) && !l.equals(FRConstants.P_TAG) && book_lang.equals(FRConstants.BOOK_LANG_EN)) {
					sentenceSbf.append(" ").append(token.getOriginal());
				}
				
				
				//@suraj: For german, we would need lemma for each token as sentiment classification is dictionary based and polarity dictionary has only lemmas.
				//@suraj: regex would exclude all the non word characters, it would only include alfabets and numbers.
				//@suraj: changed token.getLemma back to token.getOriginal as getLemma was returning no value back as pipeline was configured for english during the run
				else if (!l.equals(FRConstants.S_TAG) && !l.equals(FRConstants.P_TAG) && book_lang.equals(FRConstants.BOOK_LANG_DE)) { 
					//token.getOriginal().matches(FRConstants.REGEX_ONLY_ALFABETS_AND_NUMBERS)
					sentenceSbf.append(" ").append(token.getOriginal());
				}

				if (!FRGeneralUtils.hasPunctuation(l) && !stopwords.contains(l))
					stpwrdPuncRmvd.add(l);

				raw.add(token);
				/* calculate Flesch reading score */
				if (token.getNumOfSyllables() > 0) {
					numOfSyllables += token.getNumOfSyllables();
					properWordCount++;
				}
				/* calculate pos stats */
				if (token.getPos().equals(FRConstants.PERSONAL_P) || token.getPos().equals(FRConstants.PERSONAL_P_DE)) {
					personalPronounCount++;
					if (l.equals(FRConstants.HE) || l.equals(FRConstants.ER))
						malePrpPosPronounCount++;
					else if (l.equals(FRConstants.SHE) || l.equals(FRConstants.SIE))
						femalePrpPosPronounCount++;

				} else if (token.getPos().equals(FRConstants.POSSESIV_P) || token.getPos().equals(FRConstants.POSSESIV_P_DE)) {
					possPronounCount++;
					if (l.equals(FRConstants.HE) || l.equals(FRConstants.ER) ||
							l.equals(FRConstants.SEINER) || l.equals(FRConstants.SEINES) || l.equals(FRConstants.SEINE))
						malePrpPosPronounCount++;
					else if (l.equals(FRConstants.SHE) || l.equals(FRConstants.SIE) ||
							l.equals(FRConstants.IHRE) || l.equals(FRConstants.IHRER) || l.equals(FRConstants.IHRES))
						femalePrpPosPronounCount++;

				}
				/*
				else if (l.equals(FRConstants.ER))
					malePrpPosPronounCount++;
				else if(l.equals(FRConstants.SIE))
					femalePrpPosPronounCount++;
				*/
				
				
				
				
				else if (token.getPos().equals(FRConstants.PREPOSITION) || token.getPos().equals(FRConstants.PREPOSITION_DE)) {
					if (LOCATIVE_PREPOSITION_LIST.contains(l))
						locativePrepositionCount++;
					if (l.equals(FRConstants.IN)) {
						int temp = wordcntr;
						if ((l.equals(FRConstants.IN) && wordList.get(++temp).getLemma().equals(FRConstants.FRONT)
								&& wordList.get(++temp).getLemma().equals(FRConstants.OF)))
							locativePrepositionCount++;
					}
					if (l.equals(FRConstants.VOR)) {
						int temp = wordcntr;
						if ((l.equals(FRConstants.VOR) && (wordList.get(++temp).getLemma().equals(FRConstants.DEM)
								|| wordList.get(temp).getLemma().equals(FRConstants.DEN) || wordList.get(temp).getLemma().equals(FRConstants.DER))))
							locativePrepositionCount++;
					}
					

				} else if (l.equals(FRConstants.NEXT)) {
					int temp = wordcntr;
					if (l.equals(FRConstants.NEXT) && wordList.get(++temp).getLemma().equals(FRConstants.TO))
						locativePrepositionCount++;
				} else if (l.equals(FRConstants.NEBEN)) {
					int temp = wordcntr;
					if (l.equals(FRConstants.NEBEN) && (wordList.get(++temp).getLemma().equals(FRConstants.DEN) || 
							wordList.get(temp).getLemma().equals(FRConstants.DEM) || wordList.get(temp).getLemma().equals(FRConstants.DER)))
						locativePrepositionCount++;
				} else if (token.getPos().equals(FRConstants.THERE_EX) || token.getLemma().equals(FRConstants.COME) || token.getLemma().equals(FRConstants.KOMMEN))
					locativePrepositionCount++;
				else if (token.getPos().equals(FRConstants.INTERJECTION))
					intrjctnCount++;
				else if (token.getPos().equals(FRConstants.COORD_CONJUNCTION) || token.getPos().equals(FRConstants.COORD_CONJUNCTION_DE))
					coordConj++;
				else if (token.getLemma().equals(FRConstants.COMMA))
					commaCount++;
				else if (token.getLemma().equals(FRConstants.PERIOD)) {
					periodCount++;
				} else if (token.getLemma().equals(FRConstants.COLON))
					colonCount++;
				else if (token.getLemma().equals(FRConstants.SEMI_COLON))
					semiColonCount++;
				else if (token.getLemma().equals(FRConstants.HYPHEN))
					hyphenCount++;
				else if (token.getLemma().equals(FRConstants.EXCLAMATION))
					intrjctnCount++;
				else if (token.getLemma().equals(FRConstants.DOUBLE_QUOTES) 
						|| token.getOriginal().equals("'") || token.getOriginal().equals("\"")) {
					convCount++;
					flag++;
				}
				if(flag % 2 == 0) {
					 if (token.getOriginal().equals("I") || token.getOriginal().equals("i")) {
					
						icountnonquote++;
						}
				}
				if (token.getOriginal().equals("I") || token.getOriginal().equals("i"))
					icount++;
				//System.out.println(icountnonquote + "hello there" + icount);
				wordcntr++;
				wordCountPerSntnc++;
			}
			addToWordCountMap(raw, wordCountPerSntncMap, wordCountPerSntnc);
			
			ConvDetails conv2 = new ConvDetails();
			dialogratiochunk = conv2.convRatio(raw);
			LOG.info("Dialog Ratio for chunk = "+dialogratiochunk);
			
			//System.out.println("counts = " + 	malePrpPosPronounCount + "female = " + femalePrpPosPronounCount +"pper" + personalPronounCount + "locp"+locativePrepositionCount);
			Chunk chunk = new Chunk();
			chunk.setChunkNo(chunkNo);
			// String chunkFileName = OUT_FOLDER_TOKENS + fileName + "-" +
			// chunkNo + Constants.CHUNK_FILE;
			// try (Writer contentWtr = new BufferedWriter(new
			// OutputStreamWriter(new FileOutputStream(chunkFileName)));) {
			// contentWtr.write(Chunk.getOriginalText(raw));
			// chunk.setChunkFileLocation(chunkFileName);
			// }
			System.out.println("numbr of sentences for sentiment  ="+randomSntnCount+" for chunknum ="+chunkNo+", and total sentc  ="+numOfSntncPerBook+" for book path "+path);
			chunk.setTokenListWithoutStopwordAndPunctuation(stpwrdPuncRmvd);
			Feature feature = feu.generateFeature(chunkNo, paragraphCount, sentenceCount, raw, null, stpwrdPuncRmvd, malePrpPosPronounCount,
					femalePrpPosPronounCount, personalPronounCount, possPronounCount, locativePrepositionCount, coordConj, commaCount,
					periodCount, colonCount, semiColonCount, hyphenCount, intrjctnCount, convCount, wordCountPerSntncMap, senti_negetiv_cnt,
					senti_positiv_cnt, senti_neutral_cnt, properWordCount, numOfSyllables, dialogratiochunk);
			chunk.setFeature(feature);
			chunksList.add(chunk);
			chunkNo++;

			// reset all var for next chunk
			raw = new ArrayList<>();
			stpwrdPuncRmvd = new ArrayList<>();
			paragraphCount = 0;
			//max = 0;
			//System.out.println(icountnonquote + "hello there" + icount);
			//System.out.println("narritive or not" + ((double)icountnonquote/icount));
			if((double)icountnonquote/icount >= 0.5)
				countselfnarr++;
		}
		System.out.println(countselfnarr + "heehoo" + batchNumber);	
		
		return chunksList;
	}

	public void addToWordCountMap(List<Word> raw, Map<Integer, Integer> wordCountPerSntncMap, int wordCount) {
		wordCountPerSntncMap.put(wordCount, !wordCountPerSntncMap.containsKey(wordCount) ? 1 : wordCountPerSntncMap.get(wordCount) + 1);
	}

	/**
	 * @param path
	 * @param stopwords
	 * @return List of Equal Chunks per file
	 * @throws IOException
	 *             The method generates List of equal sized Chunk from the
	 *             tokens list passed in the signature.It has been developed
	 *             especially for ttr
	 */
	public List<Chunk> getEqualChunksFromFile(List<String> tokens) throws Exception {

		int batchNumber;
		int remainder;
		List<Chunk> chunksList = new ArrayList<>();
		int length = tokens.size();

		if (length < TTR_CHUNK_SIZE) {
			batchNumber = 0;
			remainder = TTR_CHUNK_SIZE - length;

		} else {
			batchNumber = length / TTR_CHUNK_SIZE;
			remainder = length % TTR_CHUNK_SIZE;
		}

		List<String> textTokens = new ArrayList<>();
		List<String> appendAtEnd = new ArrayList<>();

		int chunkNo = 1;
		int wordcntr = 0;
		int chunkSize = 0;

		for (int batchCtr = 0; batchCtr <= batchNumber; batchCtr++) {

			chunkSize = batchCtr < batchNumber ? TTR_CHUNK_SIZE : remainder;
			for (int index = 0; index < chunkSize; index++) {

				String token = tokens.get(wordcntr);
				textTokens.add(token);

				if (batchCtr == 0 && wordcntr < (TTR_CHUNK_SIZE - remainder)) // tokens
																				 // to
																				 // be
																				 // appendedpg18679
																				 // to
																				 // the
																				 // last
																				 // chunk,
																				 // to
																				 // make
																				 // it
																				 // equal
																				 // sized
																				 // as
																				 // CHUNK_SIZE
				{
					appendAtEnd.add(token);
				}
				wordcntr++;
			}

			if (remainder != 0 && batchCtr == batchNumber)
				textTokens.addAll(appendAtEnd);

			Chunk chunk = new Chunk();
			chunk.setChunkNo(chunkNo);
			chunk.setTokenListWithoutStopwordAndPunctuation(textTokens);
			chunksList.add(chunk);
			textTokens = new ArrayList<>();
			chunkNo++;
		}
		
		return chunksList;
	}

	/**
	 * @param chunks
	 * @return Returns list of tokens from all chunks
	 */
	private List<String> getTokensFromAllChunks(List<Chunk> chunks) {

		List<String> tokens = new ArrayList<>();
		chunks.forEach(c -> tokens.addAll(c.getTokenListWithoutStopwordAndPunctuation()));
		return tokens;

	}
	
	// Added below 5 methods as part of calculating german sentiments
    public static void createGermanPolarityMap() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(FRGeneralUtils.getPropertyVal(FRConstants.GERMAN_POLARITY_FILE)));
        String line = "";
        
        while ((line = br.readLine()) != null){
            //System.out.println(line);
            String[] data = line.split("\t");
            //System.out.println(Arrays.toString(data));
            //System.out.println(data.length);
            //System.out.println(Arrays.toString(data));
            //Double score = Double.parseDouble(data[2])-Double.parseDouble(data[3]);
            //System.out.println(score);
            String[] words = data[4].split("/");
            //System.out.println(Arrays.toString(words));
            
            String actual_word = data[0].toLowerCase(Locale.GERMAN); String lemmatized_word = data[1].toLowerCase(Locale.GERMAN); 
            String pos_tag = data[2];  String sentiment_type = data[3]; String flag = data[5];
            
            if(!sentiment_type.equals("neutral") && !sentiment_type.equals("positive") && !sentiment_type.equals("negative")) {
            	sentiment_type = "neutral";
            }
            
            //System.out.println(cleanProbabilityValues(words, sentiment_type));
            Double prob = cleanProbabilityValues(words, sentiment_type);
            //System.out.println(new GermanSentimentData(actual_word, lemmatized_word, pos_tag, sentiment_type, prob, flag));
            polarity_clues_dict.put(actual_word, new GermanSentimentData(actual_word, lemmatized_word, pos_tag, sentiment_type, prob, flag));
            
        }
        System.out.println("Total words in german polarity dictionary are : " + polarity_clues_dict.size());
        br.close();
    	
    }
    
    public static int calculateSentimentperSentence(String[] sentence_tokens) {
    	Double negative_sentiment_score = 0.0;
    	Double positive_sentiment_score = 0.0;
    	Double neutral_sentiment_score = 0.0;
    	int positive_counter = 1;
    	int negative_counter = 1;
    	int neutral_counter = 1;
    	String tmp_sentiment_type = "neutral";
    	Double tmp_sentiment_val = 0.0;
    	GermanSentimentData tmp_obj ;
    	
    	for(int i = 0; i < sentence_tokens.length; i++) {
    		tmp_obj = (GermanSentimentData) polarity_clues_dict.getOrDefault(sentence_tokens[i], dummy_sentiment_object);
    		tmp_sentiment_val = tmp_obj.sentiment_value;
    		tmp_sentiment_type = tmp_obj.sentiment_type;
    		
    		//System.out.println(tmp_obj.toString());
    		
    		if(tmp_sentiment_type.equals("neutral")) {
    			neutral_counter++;
    			neutral_sentiment_score += tmp_sentiment_val;
    		}
    		else if(tmp_sentiment_type.equals("positive")) {
    			positive_counter++;
    			positive_sentiment_score += tmp_sentiment_val;
    		}
    			
    		else if(tmp_sentiment_type.equals("negative")) {
    			negative_counter++;
    			negative_sentiment_score += tmp_sentiment_val;
    		}
    		else {
    			neutral_counter++;
    			neutral_sentiment_score += tmp_sentiment_val;   		
    		}
    	}
    	//System.out.println("Negative sentiment: " + negative_sentiment_score + " | Neutral sentiment: " + neutral_sentiment_score + " | Positive sentiment: " + positive_sentiment_score);
    	Double overall_sentiment_score = negative_sentiment_score * FRConstants.GERMAN_NEGATIVE_POLARITY_WEIGHT + neutral_sentiment_score/neutral_counter + positive_sentiment_score * FRConstants.GERMAN_POSITIVE_POLARITY_WEIGHT;
    	//System.out.println("Overall sentiment is: " + overall_sentiment_score);  
    	int scaled_sentiment_score = scaleGermanSentimentScoretoEnglishSentiment(overall_sentiment_score);
    	return scaled_sentiment_score;
    }
    
    public static Double cleanProbabilityValues(String[] probabilities, String sentiment_type) {
    	Double[] probs = new Double[probabilities.length];
    	for(int i = 0; i < probabilities.length; i++) {
    		
    		if(probabilities[i].equals("-")) {
    			probs[i] = 0.0;   			
    		}
    		else {
    			probs[i] = Double.parseDouble(probabilities[i] );
    		}
    			
    	}
    	
    	Arrays.sort(probs);
    	Double required_prob = 0.0;
    	
    	if(sentiment_type.equals("neutral")){
    			required_prob = probs[0];
    	}
    	else if(sentiment_type.equals("negative")) {
    		required_prob = -1.0 * Math.abs(probs[0]);
    	}
    	else {
    		required_prob = probs[2];
    	}
    	
    	return required_prob ;
    }

    public static String[] SentenceTokenizer(String sentence) {
    	String[] tmp = sentence.split(" ");
    	String[] tokenized_sentences = new String[tmp.length];
    	for(int i = 0; i < tmp.length; i++) {
    		tokenized_sentences[i] = tmp[i].replaceAll(FRConstants.REGEX_ONLY_ALFABETS_AND_NUMBERS, "").trim().toLowerCase(Locale.GERMAN);
    	}
    	//System.out.println(Arrays.toString(tokenized_sentences));
    	return tokenized_sentences;
    }

    
	public static int scaleGermanSentimentScoretoEnglishSentiment(Double sentiment_value) {
	    	
	    	if(sentiment_value >= FRConstants.GERMAN_POSITIVE_SENTIMENT_CUTOFF)
	    		return 3;
	    	else if(sentiment_value <= FRConstants.GERMAN_NEGATIVE_SENTIMENT_CUTOFF)
	    		return 1;
	    	else
	    		return 2;
	    }
	

}
