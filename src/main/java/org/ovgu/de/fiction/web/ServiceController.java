package org.ovgu.de.fiction.web;

import java.util.*;


import org.maltparser.core.helper.HashSet;
import org.ovgu.de.fiction.model.BookList;
import org.ovgu.de.fiction.model.BookUI;
import org.ovgu.de.fiction.model.TopKResults;
import org.ovgu.de.fiction.preprocess.ContentExtractor;
import org.ovgu.de.fiction.search.FictionRetrievalSearch;
import org.ovgu.de.fiction.search.InterpretSearchResults;
import org.ovgu.de.fiction.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;



@CrossOrigin(origins = "http://localhost:4200")
@RestController
public class ServiceController {
	

    @Autowired
	private SearchServices searchService;
	
	@RequestMapping(path = "/simbooks/{queryBookId}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	//@GetMapping(produces = "application/json")
	public BookList simbooks(@PathVariable String queryBookId) throws Exception {
    	System.out.println("QueryId:"+ queryBookId );
    	BookList data = new BookList();
		data = searchService.displayBook(queryBookId);
		return data;
	}
	
	//@PostMapping("/userClickData")
	@RequestMapping(value="/userClickData", method=RequestMethod.POST)
	public String UpdateUserClick(@RequestBody String userEvent) {
		System.out.println(userEvent);
		searchService.saveUserClicktoCsv(userEvent);
		return userEvent;
	}
	

}
