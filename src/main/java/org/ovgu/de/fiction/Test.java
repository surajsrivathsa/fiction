package org.ovgu.de.fiction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.ovgu.de.fiction.utils.FRConstants;
import org.ovgu.de.fiction.utils.FRGeneralUtils;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

public class Test {

	public Test() {
		// TODO Auto-generated constructor stub
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
	
	public static void main(String[] args) throws IOException {
		
		String regex = "^\\s+|\\s+$";
		String s = "   Mr.sad";
		System.out.println(s.replaceAll(regex, "+"));
		String s1 = "Mr.sad    ";
		System.out.println(s1.replaceAll(regex, "+"));
		String s2 = "   Mr.sad   ";
		System.out.println(s2.replaceAll(regex, "+"));
		
		Random rnd = new Random();
		int min =1;
		int max =10000;
		int randomKey=-1;
		randomKey = (rnd.nextInt(max-min)+min);
		int cnt=0;
//		while(cnt<10){
//			
//			randomKey = (rnd.nextInt(max-min)+min);
//			System.out.println("random num ="+randomKey+" i ="+cnt);
//			cnt++;
//		}
		String FEATURE_CSV_FILE = FRGeneralUtils.getPropertyVal("file.feature");
		String FEATURE_ARFF_FILE = FRGeneralUtils.getPropertyVal("file.wekafeature");
		 // load CSV
		 CSVLoader loader = new CSVLoader();
		 loader.setSource(new File(FEATURE_CSV_FILE));//source
		 Instances data = loader.getDataSet();
		 
		 // save ARFF
		 ArffSaver saver = new ArffSaver();
		 saver.setInstances(data);
		 saver.setFile(new File(FEATURE_ARFF_FILE));//destination
		 //saver.setDestination(new File(FEATURE_ARFF_FILE));
		 saver.writeBatch();
		
		
		
		/*
		String a = "MRs.";
		String regex = "(?i)(mr|mrs|dr|Madame|Monsieur|mister|miss|ms|sir|madam|sire)\\.*+"; //mr
		System.out.println(a.matches(regex));
		
		System.out.println("rounded = "+String.valueOf(Math.round(0.006151*10000.0000)/10000.0000));
		
		double len=166.666;
		System.out.println("rounded 2 = "+String.format("%.4f",Math.round(16.66/len*10000.0000)/10000.0000));
		System.out.println("rounded 3 = "+String.format("%.4f",Math.round(0.906151*10000.0000)/10000.0000));
				
		String first="Polley";
		String second ="Sayantan Polley";
		String third = "Samir yyyPolleyyy..";
		
		System.out.println("1"+first.regionMatches(0, third, 0, first.length()));
		System.out.println("2"+third.contains(first));
		//regionMatches(toffset, other, ooffset, len));
		//Tests whether the specified region of this string matches the specified region of the String argument.
		//Region is of length len and begins at the index toffset for this string and ooffset for the other string
		
		String [] temp1 = first.split(" ");
		String [] temp2 = second.split("");
		
		List<String> items = new ArrayList<>();
		items.add("A");
		items.add("B");items.add("B1");items.add("B2");
		items.add("C");
		items.add("D");
		items.add("E");
		
		System.out.println(items);
		
		items.stream()
		.filter(s1->s1.contains("B"))
		.forEach(System.out::println);
		
		*/
		
		Map<String, Integer> charMap = new HashMap<>();
		String charName = " Suhita ";
		String charNameStr = charName.toString().replaceAll(FRConstants.REGEX_ALL_PUNCTUATION, " ")
				.replaceAll(FRConstants.REGEX_TRAILING_SPACE, "");
		System.out.println("charNameStr = "+charNameStr);
		
		
		charMap.put("clifton", 1);
		charMap.put("benwick", 1);
		charMap.put("charles", 1);
		charMap.put("hayter", 1);
		charMap.put("c. hayter", 1);
		charMap.put("charles hayter", 1);
		
		
		charMap.put("Sayantan Polley", 1);
		charMap.put("Sayanta", 2);
		charMap.put("Sayantan", 3);
		charMap.put("S. Polley", 4);
		charMap.put("Polley", 2);
		
		/*

		charMap.put("Suhita", 4);
		charMap.put("Chimki", 4);
		
		
		charMap.put("Suhita Ghosh", 1);
		charMap.put("Suhitaa", 1);
		charMap.put("Suhi Ghosh", 1);
		charMap.put("Suhii", 1);
		charMap.put("Suhi Ghosh", 1);
		charMap.put("Suhi G.", 1);
		charMap.put("S. Ghosh", 1);
		charMap.put("Kumari Ghosh", 1);
		
		charMap.put("W. Ghosh", 1);
		charMap.put("W. Gh", 1);
		
		charMap.put("antoine vaudore", 1);
		charMap.put("vaudore", 1);
		
		charMap.put("Sayan Ghoshal", 1);
		
		charMap.put("Suhita Ghosh", 1);
		charMap.put("Ghosh", 1);
		
		charMap.put("S. Thiel", 1);
		charMap.put("S. Thiele", 1);
		charMap.put("W. Ghosh", 1);
		charMap.put("W. Gh", 1);
		
		charMap.put("Suhita", 1);
		charMap.put("Suhita Ghosh", 1);
		charMap.put("Suhitaa", 1);
		charMap.put("Suhi Ghosh", 1);
		charMap.put("Suhii", 1);
		charMap.put("Suhi Ghosh", 1);
		charMap.put("Suhi G.", 1);
		charMap.put("S. Ghosh", 1);
		
		charMap.put("Kumari Ghosh", 1);
		
		charMap.put("Sayan Ghoshal", 1);
		
		charMap.put("antoine vaudore", 1);
		charMap.put("vaudore", 1);*/
		
		getUniqueCharacterMapEdit(charMap);
		getUniqueCharacterMapOld(charMap);

	}
	
	
	public static void getUniqueCharacterMapEdit(Map<String, Integer> charMap){

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
					  // case 1a: {Sayantan Polley, Sayan Polley} | op = Sayantan Polley
						if (FNAME_OUTER.length() >= FNAME_INNER.length() && FNAME_OUTER.contains(FNAME_INNER) && LNAME_OUTER.equals(LNAME_INNER)) {
							if (names.length() > inputMap.getKey().length())
								charMapClone.remove(inputMap.getKey()); // remove smaller - inner- 'Sayan Polley'
							else
								charMapClone.remove(names);
						}
						/*
						 * case 3: {Sayantan Polley, Polley} | op = Sayantan Polley
						 * Surname Absorbed into Single "char", if SNAME = FNAME,
						 * i.e delete "Polley" from charMap
						 */
						if (outerName.length >= 2 && innerName.length == 1 && LNAME_OUTER.contains(FNAME_INNER)) { // &&
																													 // !LNAME_INNER.equals("")
							charMapClone.remove(inputMap.getKey()); // Note:innerName.length==1,
																	 // doesnt combine/delete : Sayan Ghosh and Suhita Ghosh
						}
						
						/**
						 * case 3: {Sayantan Polley, Sayan} | op = Sayantan Polley
						 */
						if(outerName.length >= 2 && innerName.length == 1 && FNAME_OUTER.contains(FNAME_INNER)){
							charMapClone.remove(inputMap.getKey());
						}
					} else { // we deal with "." dots here below
						/*
						 * case 2: { S. Ghoshh, Suhita Ghosh, Suhita Ghosh} | op
						 * = Suhita Ghoshh combine these dot names, if both of the first
						 * name starts with same alphabet and last name is same
						 */
						if (String.valueOf(FNAME_OUTER.charAt(0)).equals(String.valueOf(FNAME_INNER.charAt(0)))) {
							if ((LNAME_OUTER.contains(LNAME_INNER) || LNAME_INNER.contains(LNAME_OUTER))  //if (LNAME_OUTER.length() >= LNAME_INNER.length() && LNAME_OUTER.contains(LNAME_INNER)
									&& !LNAME_INNER.equals("") && !LNAME_OUTER.equals("")) {
								if (names.length() < inputMap.getKey().length()) {// remove smaller
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
		System.out.println(" out edits ="+charMapClone);
	//	return charMapClone;
	}
	
	public static Map<String, Integer> getUniqueCharacterMapOld(Map<String, Integer> charMap) {

		List<String> all_names = new ArrayList<String>(charMap.keySet());

		Map<String, Integer> charMapClone = new HashMap<>(charMap);

		for (String names : all_names) {

			for (Map.Entry<String, Integer> inputMap : charMap.entrySet()) {
				if (!names.equals(inputMap.getKey())) {
					String[] outerName = names.split(" ");
					String[] innerName = inputMap.getKey().split(" ");
					String FNAME_OUTER = outerName[0];
					String LNAME_OUTER = "";
					for (int i = 1; i < outerName.length; i++) { // start after
																 // first
																 // index,
																 // mind it!
						LNAME_OUTER = LNAME_OUTER + outerName[i];
					}

					String FNAME_INNER = innerName[0];
					String LNAME_INNER = "";
					for (int i = 1; i < innerName.length; i++) { // start after
																 // first
																 // index,
																 // mind it!
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
						if (FNAME_OUTER.length() >= FNAME_INNER.length() && FNAME_OUTER.contains(FNAME_INNER)) {
							if (names.length() > inputMap.getKey().length())
								charMapClone.remove(inputMap.getKey()); // remove
																		 // the
																		 // smaller
																		 // of
																		 // the
																		 // two!
							else
								charMapClone.remove(names);
						}
						if (FNAME_OUTER.length() <= FNAME_INNER.length() && FNAME_INNER.contains(FNAME_OUTER)) {
							if (names.length() < inputMap.getKey().length()) {// remove
																				// the
																				// smaller
																				// of
																				// the
																				// two!
								charMapClone.remove(names);
							} else {
								charMapClone.remove(inputMap.getKey());
							}
						}
					} else { // we deal with "." dots here below
						/*
						 * case 2: { S. Ghoshh, Suhita Ghosh, Suhita Ghosh} | op
						 * = Suhita Ghoshh combine these dot names, if the first
						 * name starts with same alphabet and last name is same
						 */
						if (FNAME_OUTER.charAt(0) == FNAME_INNER.charAt(0)) {
							if (LNAME_OUTER.length() >= LNAME_INNER.length() && LNAME_OUTER.contains(LNAME_INNER)
									&& !LNAME_INNER.equals("")) {
								if (names.length() < inputMap.getKey().length()) {// remove
																					// the
																					// smaller
																					// of
																					// the
																					// two!
									charMapClone.remove(names);
								} else {
									charMapClone.remove(inputMap.getKey());
								}
							}

						}

					}

					/*
					 * case 3: {Sayantan Polley, Polley} | op = Sayantan Polley
					 * Surname Absorbed into Single "char", if SNAME = FNAME,
					 * i.e delete "Polley" from charMap
					 */
					if (outerName.length >= 2 && innerName.length == 1 && LNAME_OUTER.contains(LNAME_INNER)) { // &&
																												 // !LNAME_INNER.equals("")
						charMapClone.remove(inputMap.getKey()); // Note:
																 // innerName.length==1,
																 // doesnt
																 // combine/delete
																 // : Sayan Ghosh
																 // and Suhita
																 // Ghosh
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
		System.out.println("out old ="+charMapClone);
		return charMapClone;
	}


	
}
