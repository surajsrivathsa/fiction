package org.ovgu.de.fiction.model;

import java.util.List;

public class BookList {
	private String globalFeature;
	private List<BookUI> bookUI;

	
	public BookList() {
		super();
	}
	//
	public BookList(String globalFeature, List<BookUI> bookUI) {
		super();
		this.globalFeature = globalFeature;
		this.bookUI = bookUI;
		
	}
	
	 public String getglobalFeature() {
	        return globalFeature;
	    }
	    public void setglobalFeature(String feature) {
	        this.globalFeature = feature;
	    }
	    
	    public List<BookUI> getbookUI() {
	        return bookUI;
	    }
	    public void setbookUI(List<BookUI> bookUI) {
	    	this.bookUI = bookUI;
	    }
}
