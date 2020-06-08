package org.ovgu.de.fiction.web;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.*;



import org.apache.log4j.Logger;
import org.ovgu.de.fiction.model.BookList;
import org.ovgu.de.fiction.model.BookUI;
import org.ovgu.de.fiction.model.TopKResults;
//import org.ovgu.de.fiction.preprocess.ContentExtractor;
import org.ovgu.de.fiction.search.FictionRetrievalSearch;
import org.ovgu.de.fiction.search.InterpretSearchResults;
import org.ovgu.de.fiction.utils.FRConstants;
import org.ovgu.de.fiction.utils.FRGeneralUtils;
import org.ovgu.de.fiction.utils.FRWebUtils;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.Service;
import nl.siegmann.epublib.domain.Metadata;

@Service
@Configuration 
public class SearchServices {
	private static final long serialVersionUID = 462006850003220169L;
	
	private static final String WEB_CONTEXT_PATH = "web.ctx.path";
	private static final String USER_CLICK_EVENT_PATH = "user.event.path";
	private static long TIME;
	//final static Logger LOG = Logger.getLogger(ContentExtractor.class);
	private Map<String, List<String>> data = new HashMap<String, List<String>>();


	
	
	public BookList displayBook(String queryBookId, String topK) throws Exception {
		List<BookUI> simBooks = new ArrayList<BookUI>();
		BookList bookList = new BookList();
	
		TIME= System.currentTimeMillis();
		if (queryBookId == null || queryBookId.trim().equals("")) {
			return bookList;
		}

		if (queryBookId != null) {

			String similarity = "L2";

			FRWebUtils utils = new FRWebUtils();
			Map<String, String> book_master = utils.getAllMasterBooks(); // key = bookId, Value = Book_Name
			String FEATURE_CSV_FILE = FRGeneralUtils.getPropertyVal("file.feature");

			Map<String,String> stats_Of_results =new TreeMap<String,String>(); 

			int TOP_K = Integer.parseInt(topK);
			TopKResults topKResults = FictionRetrievalSearch.findRelevantBooks(queryBookId, FEATURE_CSV_FILE,
						FRConstants.SIMI_PENALISE_BY_NOTHING, FRConstants.SIMI_ROLLUP_BY_ADDTN,
						FRConstants.SIMI_INCLUDE_TTR_NUMCHARS, TOP_K, similarity);

			InterpretSearchResults interp = new InterpretSearchResults();

				try {
					stats_Of_results = interp.performStatiscalAnalysisUsingRegression(topKResults);
				} catch (Exception e) {
					throw new Exception("analysis cannot be done!");
				}

				int rank = 0;
				for (Map.Entry<Double, String> res : topKResults.getResults_topK().entrySet()) {
					System.out.println("BookTest:"+res.getValue());

					String[] bookArr = utils.getMasterBookName(book_master, String.valueOf(res.getValue())).split("#");
					if (bookArr.length < 2)
						continue;

					String bookName = bookArr[0];
					bookName = bookName.contains("|") ? bookName.substring(bookName.indexOf("#") + 1).replace("|", ",")
							: bookName.substring(bookName.indexOf("#") + 1);
					String bookId = utils.getMasterBookId(book_master, bookName);
					Metadata metadata = FRGeneralUtils.getMetadata(bookId);

				if (bookId.equals(queryBookId))
						continue;

				rank++;

					if (rank == TOP_K + 1)
						break;
					
					String summary = "";
					String authName = bookArr[1].contains("|") ? bookArr[1].replace("|", ",") : bookArr[1];
					String language = (metadata.getLanguage()).toString().equals("en") ? "English" :"Deutsch" ;
					String publisheddate = (metadata.getDates().subList(0, 1)).toString().replace("[publication:", "").replace("]", "");
					if((metadata.getContributors()).size() == 0) {
					summary = "Language: "+language +". It is written by " + authName + ". Published in the year: " + publisheddate ;
					}
					else{
					summary = "Language: "+language +". It is written by " + authName + ". Contributors of this book are " + (metadata.getContributors().toString().replace("[","").replace("]","")) + ". Published in the year:" + publisheddate ;
					}
					BookUI book = new BookUI();
					book.setId(bookId);
					book.setName(bookName);
					System.out.println(bookId);
					book.setAuthor(authName);
					book.setRank(rank);
					StringBuffer sbf = new StringBuffer(FRGeneralUtils.getPropertyVal(WEB_CONTEXT_PATH));
					sbf.append(bookId).append(".epub");
					book.setEpubPath(sbf.toString());
					book.setScore(String.valueOf(res.getKey()));
					book.setSummary(summary);					
					simBooks.add(book);
				}
				bookList.setbookUI(simBooks);

				//LOG.debug("books added " + simBooks.size());

				if (stats_Of_results.size() > 0) {
					Map<String, String> reduced_features = new HashMap<>();

					reduced_features = stats_Of_results;
					for (Map.Entry<String, String> reduced_fe : stats_Of_results.entrySet()) {
						System.out.println(reduced_fe.getKey());
						System.out.println(reduced_fe.getValue());
					}
					StringBuffer reducedFe = new StringBuffer(
							"  Why these books are related?  ");

					reducedFe.append(FRWebUtils.getHighLevelFeatures(reduced_features));

					System.out.println("Time taken -"+((System.currentTimeMillis()-TIME)/1000));
					TIME= System.currentTimeMillis();
					System.out.println(reducedFe);
					if (reducedFe != null) {
						bookList.setglobalFeature(reducedFe.toString().replaceAll(",$",""));
					}
					else {
						bookList.setglobalFeature("Analysis could not be done");
					}
				}
				
		}
		return bookList;
	}
	
	public void saveUserClicktoCsv(String userevent) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(FRGeneralUtils.getPropertyVal(USER_CLICK_EVENT_PATH), true))){
			writer.append('\n');
		    writer.append(userevent);
		    writer.flush();
			
		}
		catch(Exception e)
		{
			System.out.print(e);
		}
	}

}
