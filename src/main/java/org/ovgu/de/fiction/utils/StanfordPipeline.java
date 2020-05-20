package org.ovgu.de.fiction.utils;

import java.util.Properties;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;

/**
 * @author Suhita
 *
 */
public class StanfordPipeline {

	private static StanfordCoreNLP pipeline;

	/**
	 * @return
	 */
	public static StanfordCoreNLP getPipeline(String annotations, String lang) {
		if (pipeline != null)
			return pipeline;

		Properties properties = new Properties();
		if (annotations == null  && lang.equals("en")) {
			properties.put(FRConstants.STNFRD_ANNOTATOR, FRConstants.STNFRD_LEMMA_ANNOTATIONS_EN);
			//properties.put("tokenize.language","en");
		}
		else if(annotations == null  && lang.equals("de")) {
			properties.put(FRConstants.STNFRD_ANNOTATOR, FRConstants.STNFRD_LEMMA_ANNOTATIONS_DE);
			properties.put("tokenize.language","de");
			properties.put("tokenize.postProcessor","edu.stanford.nlp.international.german.process.GermanTokenizerPostProcessor");
			properties.put("pos.model","edu/stanford/nlp/models/pos-tagger/german/german-hgc.tagger");
			properties.put("ner.model", "edu/stanford/nlp/models/ner/german.conll.hgc_175m_600.crf.ser.gz");
			properties.put("parse.model","edu/stanford/nlp/models/lexparser/germanFactored.ser.gz");
			
		}
		
		else {
			properties.put(FRConstants.STNFRD_ANNOTATOR, annotations);
		}
		properties.put("ner.useSUTime", "false ");
		properties.put("ner.applyNumericClassifiers", "false");
		if(annotations!=null && annotations.contains("parse")){
			properties.put("depparse.model", "edu/stanford/nlp/models/parser/nndep/english_SD.gz");
			//properties.put("parse.maxlen", "30");
		}
		else if(annotations!=null && annotations.contains("parse") && lang.equals("en")){
			properties.put("depparse.model", "edu/stanford/nlp/models/parser/nndep/english_SD.gz");
			//properties.put("parse.maxlen", "30");
		}
		else if(annotations!=null && annotations.contains("parse") && lang.equals("de")) {
			properties.put("depparse.model", "edu/stanford/nlp/models/parser/nndep/german_SD.gz");
			//properties.put("parse.maxlen", "30");
		}
		
		return new StanfordCoreNLP(properties);
	}

	public static void resetPipeline() {
		pipeline = null;
	}
}


/*
public static StanfordCoreNLP getPipeline(String annotations, String lang) {
if (pipeline != null)
	return pipeline;

Properties properties = new Properties();
if (annotations == null  && lang.equals("en")) {
	properties.put(FRConstants.STNFRD_ANNOTATOR, FRConstants.STNFRD_LEMMA_ANNOTATIONS);
	properties.put()
}
else {
	properties.put(FRConstants.STNFRD_ANNOTATOR, annotations);
}
properties.put("ner.useSUTime", "false ");
properties.put("ner.applyNumericClassifiers", "false");
if(annotations!=null && annotations.contains("parse")){
	properties.put("depparse.model", "edu/stanford/nlp/models/parser/nndep/english_SD.gz");
	//properties.put("parse.maxlen", "30");
}
else if(annotations!=null && annotations.contains("parse") && lang.equals("en")){
	properties.put("depparse.model", "edu/stanford/nlp/models/parser/nndep/english_SD.gz");
	//properties.put("parse.maxlen", "30");
}
else if(annotations!=null && annotations.contains("parse") && lang.equals("de")) {
	properties.put("depparse.model", "edu/stanford/nlp/models/parser/nndep/german_SD.gz");
	//properties.put("parse.maxlen", "30");
}

return new StanfordCoreNLP(properties);
}
*/