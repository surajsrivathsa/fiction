package org.ovgu.de.fiction.feature.extraction;

public class GermanSentimentData{
	public String actual_token;
	public String lemmatized_token;
	public String pos_tag;
	public String sentiment_type;
	public Double sentiment_value;
	public String flag;

    public GermanSentimentData(String actual_token,String lemmatized_token, String pos_tag, String sentiment_type, Double sentiment_value, String flag){
        this.actual_token = actual_token;
        this.lemmatized_token = lemmatized_token;
        this.pos_tag = pos_tag;
        this.sentiment_type = sentiment_type;
        this.sentiment_value = sentiment_value;
        this.flag = flag;
    }
    
    @Override
    public String toString() {
        return "[actual_token=" + actual_token + ", lemmatized_token=" + lemmatized_token
                + ", pos_tag=" + pos_tag + ", sentiment_type=" + sentiment_type + ", sentiment_val=" + sentiment_value + ", flag=" + flag + "]";
    }

}

