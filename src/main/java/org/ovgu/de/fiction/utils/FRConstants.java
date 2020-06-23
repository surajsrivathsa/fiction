package org.ovgu.de.fiction.utils;

/**
 * @author Aditya The interface contains constants used in the project
 */
public interface FRConstants {

	/* property file related constants */
	// public static final String PROPERTIES_FILE_LOC = "src/main/resources/";
	public static final String PROPERTIES_FILE_LOC = "WEB-INF/";
	public static final String CONFIG_FILE = "config.properties";
	public static final String STOPWORD_CONFIG_FILE = "stopwords.properties";
	public static final String EPUB_FOLDER = "folder.in.epub";
	public static final String OUT_FOLDER_LOCATION = "folder.out.content";
	public static final String WRITE_TOTAL_CONTENT_CONFIG = "write.totalcontent";
	public static final String OUT_FOLDER_CONTENT = "folder.out.content";
	public static final String CHUNK_SIZE = "chunk.size";
	public static final String CHUNK_SIZE_FOR_TTR = "chunk.ttr.size";
	public static final String OUT_FOLDER_TOKENS = "folder.out.chunks";
	public static final String STOPWORD_FICTION = "stopword.fiction";

	public static final String REP_FN = "C:\\OvGU_DKe\\Project\\GutenbergDataset\\testing_epubs_extracted\\";

	/* regex */
	public static final String REGEX_ALL_PUNCTUATION = "[\\s\"\\.\\,\\?\\!\\:\\;\\'-]";
	public static final String CHARACTER_NOISE = "[\"\\.\\,\\?\\!\\:\\;\\'-]";
	public static final String REGEX_SPLIT_WORD = "[\\s\"\\.\\,\\?\\!\\:\\;'“”-]";
	public static final String REGEX_NON_WORD = "[^\\w]";
	public static final String REGEX_SPACE_QUOTE_PUNCTUATION = "[\\s\"“”]";
	public static final String REGEX_SPLIT_ON_PERIOD = "(?<!mr|mrs|dr|ph|sr|sra|ms|mx|et al)\\.";
	public static final String REGEX_FOOTNOTES = "^footnote.*";
	public static final String REGEX_GUTEN_END = ".*end.*gutenberg.*";
	public static final String REGEX_TRAILING_SPACE = "^\\s+|\\s+$";
	public static final String P_TAG = "<p>";
	public static final String S_TAG = "<s>";

	/* Stanford api related */
	public static final String STNFRD_LEMMA_ANNOTATIONS_EN = "tokenize,ssplit,pos,lemma,ner"; // "tokenize,ssplit,pos,lemma,ner";

	public static final String STNFRD_LEMMA_ANNOTATIONS_DE = "tokenize,ssplit,pos,lemma,ner"; // "tokenize,ssplit,pos,lemma,ner";

	public static final String STNFRD_SENTI_ANNOTATIONS = "tokenize,ssplit,pos,depparse,parse,sentiment";

	public static final String STNFRD_ANNOTATOR = "annotators";

	/* Preprocessing related constants */
	public static final String EPUB_EXTN = ".epub";
	public static final String HTML_EXTN = ".html";
	public static final String SPACE = " ";
	public static final String NONE = "";
	public static final String GUTEN_PRJCT = "project gutenberg";
	public static final String GUTEN_EBOOK = "gutenberg ebook";
	public static final String GUTEN_LICENSE = "gutenberg license";
	public static final String PERMISSION_TEXT = "permission is granted to share this book as an electronic text";
	public static final String RSRV_TXT = "all rights reserved";
	public static final String TRANSCRIBE = "transcribed from";
	public static final String EDITION = "The text follows that of the Definitive Edition";
	public static final String TRANSCRIBER_NOTES = "transcriber's notes";
	public static final String METADATA_TITLE = "title:";
	public static final String METADATA_AUTHR = "author:";
	public static final String METADATA_CE = "character set encoding:";
	public static final String METADATA_LNG = "language: english";
	public static final String METADATA_1ST_RLS = "first released:";
	public static final String METADATA_DATE = "date:";
	public static final String REPRINTED = "reprinted";
	public static final String COPYWRIGHT = "copyright";
	public static final String PREFACE = "preface";
	public static final String GUTEN_14 = "a table of contents has been added to this ebook";
	public static final String GUTEN_13 = "with illustrations";
	public static final String GUTEN_12 = "produced by";
	public static final String GUTEN_11 = "by";
	public static final String GUTEN_10 = "introduction";
	public static final String GUTEN_9 = "indemnity";
	public static final String GUTEN_8 = "some states do not allow disclaimers of implied warranties";
	public static final String GUTEN_7 = "if you discover a defect in this etext within";
	public static final String GUTEN_6 = "we would prefer to send you this information by email";
	public static final String GUTEN_5 = "please mail to:";
	public static final String GUTEN_4 = "we need your donations more than ever";
	public static final String GUTEN_3 = "we are now trying to release all our books";
	public static final String GUTEN_2 = "translated from";
	public static final String GUTEN_1 = "please take a look at the important information in this header";
	public static final String STAR_CHAR = "*";
	public static final String ORG = ".org";
	public static final String EMAIL_CHAR = "@";
	public static final String PRINTED = "printed";
	public static final String AUTHOR_OF = "author of";
	public static final String CONTENTS = "Contents";
	public static final String CHAPTER = "Chapter";
	public static final String CHAPTER_CAPS = "CHAPTER";
	public static final String A_ID = "<a id";
	public static final String A_HREF = "<a href";
	public static final char NEW_LINE = '\n';
	public static final String ILLUSTRATED = "Illustrated.";
	public static final String TRUE = "true";

	public static final String FULL_HTML = "-FULL.html";
	public static final String CONTENT_FILE = "-content.html";
	public static final String CHUNK_FILE = "-chunk.html";

	public static final String REGEX_ONLY_ALFABETS_AND_NUMBERS = "[^0-9a-zA-ZÀ-ž]";
	//added constants defining language of each book and sentiment cutoffs for german
	public static final String BOOK_LANG_EN = "en";
	public static final String BOOK_LANG_DE = "de";
	public static final Double GERMAN_POSITIVE_SENTIMENT_CUTOFF = 0.3;
	public static final Double GERMAN_NEGATIVE_SENTIMENT_CUTOFF = -0.1;
	public static final Double GERMAN_POSITIVE_POLARITY_WEIGHT = 0.85;
	public static final Double GERMAN_NEGATIVE_POLARITY_WEIGHT = 1.0;
	
	//added below constants and linked to file path in config.properties
	public static final String GERMAN_POLARITY_FILE = "file.german.polarity";
	
	public static final String TO = "to";

	public static final String ZU = "zu";

	public static final String DOUBLE_QUOTES = "``";

	public static final String EXCLAMATION = "!";

	public static final String HYPHEN = "-";

	public static final String SEMI_COLON = ";";

	public static final String COLON = ":";

	public static final String PERIOD = ".";

	public static final String COMMA = ",";

	public static final String FILE_HEADER = "bookId-chunkNo,F0,F1,F2,F3,F4,F5,F6,F7,F8,F9,F10,F11,F12,F13,F14,F15,F16,F17,F18,F19,F20,F21,F22,F23";
	//,F24,F25,F26,F27,F28,F29,F30,F31,F32,F33,F34,F35,F36,F37,F38,F39,F40,F41,F42,F43,F44,F45,F46,F47,F48,F49,F50";

	public static final String FILE_HEADER_RES_CSV = "bookId_RowNum,F0,F1,F2,F3,F4,F5,F6,F7,F8,F9,F10,F11,F12,F13,F14,F15,F16,F17,F18,F19,F20,F21,F22,F23,Class";
	//F24,F25,F26,F27,F28,F29,F30,F31,F32,F33,F34,F35,F36,F37,F38,F39,F40,F41,F42,F43,F44,F45,F46,F47,F48,F49,F50,Class";

	public static final String COORD_CONJUNCTION = "CC";

	public static final String COORD_CONJUNCTION_DE = "KON";

	public static final String INTERJECTION = "UH";

	public static final String COME = "come";

	public static final String KOMMEN = "kommen";

	public static final String THERE_EX = "EX ";

	public static final String NEXT = "next";

	public static final String NEBEN = "neben";

	public static final String OF = "of";

	public static final String AN = "an";

	public static final String AUF = "auf";

	public static final String FRONT = "front";

	public static final String VOR = "vor";

	public static final String DEM = "dem";

	public static final String DEN = "den";

	public static final String DER = "der";

	public static final String IN = "in";

	public static final String IM = "im";

	public static final String PREPOSITION = "IN";

	public static final String PREPOSITION_DE = "APPR";

	public static final String POSSESIV_P = "PRP$";

	public static final String POSSESIV_P_DE = "PPOSAT";

	public static final String SHE = "she";

	public static final String SIE = "sie";

	public static final String HE = "he";

	public static final String ER = "er";

	public static final String SEINER = "seiner";

	public static final String SEINE = "seine";

	public static final String SEINES = "seines";

	public static final String IHRE = "ihre";

	public static final String IHRES = "ihres";

	public static final String IHRER = "ihrer";

	public static final String PERSONAL_P = "PRP";

	public static final String PERSONAL_P_DE = "PPER";

	public static final String NER_CHARACTER = "PERSON";

	public static final String NER_CHARACTER_DE = "I-PER";

	public static final String MISC_CHAR = "MISC";

	public static final String CHARACTER_STOPWORD_REGEX = "(?i)(mr|mrs|dr|doctor|duke|duchess|lady|Madame|madam|Monsieur|Mademoiselle|mister|miss|ms|sir|sire|Ru|de|<p>|-)\\.*+";

	/* Similarity related */
	public static final String SIMILARITY_COSINE = "cosine";

	public static final String SIMILARITY_L1 = "L1";

	public static final String SIMILARITY_L2 = "L2";

	public static final String SIMI_PENALISE_BY_CHUNK_NUMS = "LEN";
	public static final String SIMI_PENALISE_BY_NOTHING = "NOT";
	public static final String SIMI_PENALISE_BY_CHUNK_SQR_ROOT = "SQR";
	public static final String SIMI_ROLLUP_BY_ADDTN = "ADD";
	public static final String SIMI_ROLLUP_BY_MULPN = "MUL";
	public static final String SIMI_INCLUDE_TTR_NUMCHARS = "IN";
	public static final String SIMI_EXCLUDE_TTR_NUMCHARS = "EX";

	public static final double SIMILARITY_CUTOFF = 0.7;
	public static final int FEATURE_NUMBER = 51;
	public static final int FEATURE_NUMBER_LOCAL = 21;
	public static final int FEATURE_NUMBER_GLOBAL = 31;
	public static final double FEATURE_WEIGHT_MORE = 0.2;
	public static final double FEATURE_WEIGHT_LESS = 0.10;
	public static final double FEATURE_WEIGHT_LEAST = 0.05;
	
	public static final double[] CHUNK_WEIGHT = {0.3,.65,.5,0,.2,.4,0,3.0};
	public static final double[] EMO_WEIGHT = {0.5/17,.1/17,.1/17,.4/17,.3/17,.2/17,0,17/17};
	public static final double[] CHAR_WEIGHT = {0.1,.05,.2,.2,.2,.2,0,2.0};
	public static final double[] GENRE_WEIGHT = {0/10,.1/10,.1/10,.4/10,.3/10,.2/10,1.0,10/10};
	public static final double[] NUMCHAR_WEIGHT = {0.05,.05,.05,0.1,0,0,0,0.2};
	public static final double[] TTR_WEIGHT = {0.05,.05,.05,0.1,0,0,0,0.2};
	
	
	public static final double PERCTG_OF_SNTNC_FOR_SENTIM = 0.05;
	public static final int RANDOM_SENTENCES_SENTIM_MID_VAL = 5000;
	public static final int RANDOM_SENTENCES_SENTIM_TOP_VAL = 10000;

	public static final double SIMIL_TOPMATCH_CLASS = 1;
	public static final double SIMIL_FAIRMATCH_CLASS = 0;
	public static final double SIMIL_POORMATCH_CLASS = -1;

	public static final String DATA_DISTRIB_SKEW_1 = "SKEWED_TOWARDS_1";
	public static final String DATA_DISTRIB_SKEW_0 = "SKEWED_TOWARDS_0";
	public static final String DATA_DISTRIB_AT_CENTR = "MANY_AT_CENTER";
	public static final double DATA_DISTRIB_50_PERCENT = 0.5;
	public static final double DATA_DISTRIB_40_PERCENT = 0.4;
	public static final double DATA_DISTRIB_DIFFER_CUTOFF = 0.01;

	public static final int TOP_K_RESULTS = 25;

	/* feature related */
	public static final int LEAVE_LAST_K_ELEMENTS_OF_FEATURE = 30; // exclude 2 global elements (TTR, NUM_Chars) from

	public static final int CHAR_RAT = 21;

	//public static final int DIALOG_RAT = 22;

	// public static final int MAX_NUM = 20;

	public static final int DIALOG_RAT_CHUNK = 20;

	public static final int TTR_21 = 23;

	public static final int NUM_CHARS_20 = 22;

	public static final int FLSH_RS_19 = 19;

	public static final int SENTI_NEU_18 = 18;

	public static final int SENTI_P_17 = 17;

	public static final int SENTI_N_16 = 16;

	public static final int QUOTES_15 = 15;

	public static final int SENTENCE_L_14 = 14;

	public static final int CONJ_PUNC_13 = 13;

	public static final int INTERJECTION_12 = 12;

	public static final int HYPHEN_11 = 11;

	public static final int SCOLON_10 = 10;

	public static final int COLON_9 = 9;

	public static final int PERIOD_8 = 8;

	public static final int COMMA_7 = 7;

	public static final int CONJUNCTION_6 = 6;

	public static final int PREPOSITION_5 = 5;

	public static final int POSS_PR_4 = 4;

	public static final int PERSONAL_PR_3 = 3;

	public static final int MALE_PR_2 = 2;

	public static final int FEMALE_PR_1 = 1;

	public static final int PARAGRAPH_COUNT_0 = 0;

}