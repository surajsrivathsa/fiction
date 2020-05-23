# fiction
Core Java  Application for Processioning, Feature Extraction and Query functionalities using terminal

Instructions to run the code for extraction of existing features along with Feature 2:

1.	Clone the code from the branch named ‘feature2’ in the repository surajsrivathsa/fiction.
2.	Build maven project from the file pom.xml
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
