package org.ovgu.de.fiction.web;

import java.util.*;



import org.ovgu.de.fiction.model.BookList;
import org.ovgu.de.fiction.model.BookUI;
import org.ovgu.de.fiction.model.TopKResults;
import org.ovgu.de.fiction.search.FictionRetrievalSearch;
import org.ovgu.de.fiction.search.InterpretSearchResults;
import org.ovgu.de.fiction.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;



@CrossOrigin(origins = "*")
@RestController
public class ServiceController {
	

    @Autowired
	private SearchServices searchService;
	
	@RequestMapping(path = "/simbooks/{queryBookId}/{topK}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	//@GetMapping(produces = "application/json")
	public BookList simbooks(@PathVariable String queryBookId, @PathVariable String topK, @PathVariable String system) throws Exception {
    	System.out.println("topK:" + topK );
    	BookList data = new BookList();
		data = searchService.displayBook(queryBookId,topK, system);
		return data;
	}
	
	//@PostMapping("/userClickData")
	@RequestMapping(value="/userClickData", method=RequestMethod.POST)
	public ResponseEntity <String> UpdateUserClick(@RequestBody String userEvent) {
		try {
		System.out.println(userEvent);
		searchService.saveUserClicktoCsv(userEvent);
		return new ResponseEntity<String>(HttpStatus.CREATED);
		}
		catch( Exception e)
		{
			throw e;
		}
	}
	
	@RequestMapping(path = "/hello", method = RequestMethod.GET, produces = "application/json")
	public String Hello() {
		String text = "Hello Java";
		return text;
	}
	

}
