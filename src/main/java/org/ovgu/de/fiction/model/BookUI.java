package org.ovgu.de.fiction.model;

public class BookUI {
	private String id;
	private String name;
	private String genre;
	private int rank;
	private String score;
	private String author;
	private String epubPath;
	private String htmlPath;
	private String summary;
//
	public BookUI() {
		super();
	}
	
	public BookUI(String id, String name, String genre, int rank, String score, String author, String epubPath,
			String htmlPath, String summary) {
		super();
		this.id = id;
		this.name = name;
		this.genre = genre;
		this.rank = rank;
		this.score = score;
		this.author = author;
		this.epubPath = epubPath;
		this.htmlPath = htmlPath;
		this.summary = summary;
		
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public String getScore() {
		return score;
	}

	public void setScore(String score) {
		this.score = score;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getEpubPath() {
		return epubPath;
	}

	public void setEpubPath(String epubPath) {
		this.epubPath = epubPath;
	}

	public String getHtmlPath() {
		return htmlPath;
	}

	public void setHtmlPath(String htmlPath) {
		this.htmlPath = htmlPath;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}
	
	public String getSummary() {
		return summary;
	}
	
	public void setSummary(String summary) {
		this.summary = summary;
	}
}
