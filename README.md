# SIMFIC 2.0 Milestone 2 - Team D
Core Java  Application for Processioning, Feature Extraction and Query functionalities using terminal

# Instructions to run the code for extraction of existing features along with Feature 2:

1.	Clone the code from the branch named ‘feature2’ in the repository surajsrivathsa/fiction.
2.	Build maven project by right clicking the file pom.xml > Run As > Maven Build and specify the goal as 'clean install'.
3.	Replace the paths for the following folders in the ‘config.properties’ file :
  a.	folder.in.epub– The path to the folder where the source epubs files are present.
  b.	folder.out.content – The path to the folder where the HTML content of the epub files will be extracted.
  c.	folder.out.chunks- The path to the folder where the chunks would be stored.
  d.	file.feature– The location of the file containing the features extracted for all the books.
4.	Replace the path for the logger file in the ‘log4j.properties’ file:
  a.	log4j.appender.file.File - The path where the log file will be stored.
5.	Place your desired source epub files in the folder.in.epub folder
6.	Run the code by right clicking on the ‘FictionRetrievalDriver.java’ file in the Project Explorer in Eclipse and select ‘Run As’ -> ‘Java Application’.


Once the program is successfully executed, the extracted feature file will be present in the specified location. The columns for the new features : 'main character presence' and 'dialog interaction ratio' are columns F20 and F21 respectively. 

While running the code from our end, since we did not have the computing resources needed for the entire list of roughly 1700 books, the program was run on 6 batches of around 300 books each. For an 8 GB i5 processor system, each batch took around 4 hours. The final feature file is the file named 'Features_Extracted_English.csv'.

# Note - 
1.Due to the way NUM_OF_CHARS and TTR features are normalized, we are aware that these two columns have currently been extracted incorrectly. However, once the program is run for all books at once, this will be handled automatically.
2. In order to run the code for branches 'feature1' and 'feature3' please have a look at the documents named 'Feature 1 Run Instructions.pdf' and 'Feature 3 Run Document.pdf'/.docx respectively.
