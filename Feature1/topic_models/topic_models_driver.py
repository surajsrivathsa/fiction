import topic_models_file_utils
import topic_models_feature_utils
import topic_models_constants as constants
import sys
import time
import argparse
import logging
import time
import os
import errno
import re
import numpy as np
import pandas as pd
from pprint import pprint
# spacy for lemmatization
import spacy
import warnings
import json
import pickle

class Driver:
    
    def __init__(self, feature_file_path = "/Users/surajshashidhar/git/fiction/bkp/Features_Extracted.csv", 
    book_file_path = "/Users/surajshashidhar/git/fiction/Batch1_extracted", 
    stopwords_file_path = "/Users/surajshashidhar/Desktop/ovgu/semester_2/XAI_project/reasearched_code_and_data/all_language_emotions.csv",
    book_start_percentage = 0.1, book_end_percentage = 0.1,
    new_feature_file_path = "", book_list_file_path = "/Users/surajshashidhar/git/fiction/Final_Booklist.xlsx",
    logging_flag = False):
        self.feature_file_path = feature_file_path
        self.book_file_path = book_file_path
        self.stopwords_file_path = stopwords_file_path
        self.feature_fields = None
        self.book_start_percentage = book_start_percentage
        self.book_end_percentage = book_end_percentage
        self.similarity_file_path = None
        self.similarity_type = None
        self.new_feature_file_path = new_feature_file_path
        self.book_list_file_path = book_list_file_path
        self.logging_flag = logging_flag

        if(self.logging_flag):
            logging.basicConfig(filename= constants.FILE_PATH_LOG, level = constants.LOGGING_LEVEL, filemode=constants.FILE_MODE, format='%(asctime)s - s%(name)s - %(levelname)s - %(message)s')
            self.log = logging.getLogger()
            self.log.info("Logging has been enabled")
            print("Logging has been enabled")
            print()
        else:
            print("Logging has been disabled")
            self.log = None
            print()
    
    def run_extraction(self):
        
        book_paths_dict = None
        fileutils_obj = topic_models_file_utils.FileUtils(self.feature_file_path, self.book_file_path, self.stopwords_file_path, self.book_list_file_path, 
                                                        logging_flag = self.logging_flag, log = self.log)
        #Read the book list excel file to get the book name and its language, Language would be used to dynamically choose different nlp pipelines further
        books_lang_dict = fileutils_obj.read_booklist_and_preprocess()
        
        #Extract the paths of the books and filter out any unwanted files that are non-html
        books_path_dict = fileutils_obj.read_bookpath_and_extract_pgid()
        # print(books_path_dict)

        #Convert each html book to raw text book, remove all the zero sized empty books(extremely short books with like 10-20 sentences are not removed here).
        books_text_dict = fileutils_obj.read_html_and_strip_tags(books_path_dict, books_lang_dict)
        # print(books_text_dict.keys())
        # print(books_text_dict["pg16909"])

        #Read our custom stopwords file and put it in a set
        misc_stopwords_set = fileutils_obj.generate_stopwords_set()
        
        
        topicutils_obj = topic_models_feature_utils.TopicUtils(self.feature_file_path, self.book_file_path, self.stopwords_file_path, 
                                                                logging_flag = self.logging_flag, log = self.log)
        
        #Initialize nlp pipelines, For now its gonna initialize english and german
        topicutils_obj.initialize_nlp_pipeline()

        #Extend the existing english and german stopwords with our custom stopwords list
        topicutils_obj.extend_stopwords_list(misc_stopwords_set)

        """
        Important and time consuming function that would do the following
        a) For each book generate appropriate start and end percentages of lemmatized list, corpus and index(id2word)
        """
        books_sentences_dict = topicutils_obj.get_corpus_dict(books_text_dict)

        #Dump this dictionary for saving it, incase topic models fails below we would still have the dictionary serialized
        #We can reload the dictionary and start generating topic from this point using read method in file utils , no need to generate corpus again
        fileutils_obj.write_serialized_sentences_dict(books_sentences_dict)

        #Here you can collect the garbage and free up python memory using gc.collect() or del command to deletea all the intrim memory.We have commented this out as we ran on server of 64GB RAM , on our local we implemented this

        #Reading back the dumped sentence dict file
        books_sentences_dict = fileutils_obj.read_serialized_sentences_dict()

        #Generated topics or each book start and book end using corpus, index and lemmatized list
        topics_dict = topicutils_obj.generate_topics(books_sentences_dict)

        #Dump the serizlized topics dictionary for safer side
        fileutils_obj.write_serialized_topics_dict(topics_dict)

        #Post processing of topics and putting them in csv and json files
        pandas_dict = topicutils_obj.generate_pandas_dict(topics_dict)

        fileutils_obj.write_csv_topics_df(pandas_dict)

        topics_dict_json_str = json.dumps(topics_dict)
        loaded_topics_dict = json.loads(topics_dict_json_str)

        #Write the topics and their scres to json file
        fileutils_obj.write_topics_json(topics_dict)

if __name__ == "__main__":
    ap = argparse.ArgumentParser()

    # Add the arguments to the parser and parse the arguments from command line
    ap.add_argument( "--feature_file_path", nargs= "?", required=False, help=" feature_file_path", default = "/Users/surajshashidhar/git/fiction/Features_extracted_milestone3.csv")
    ap.add_argument("--book_file_path", nargs= "?", required=False, help="book_file_path", default = "/Users/surajshashidhar/git/fiction/Batch1_extracted")
    ap.add_argument( "--stopwords_file_path", nargs= "?", required=False, help=" stopwords_file_path", default="/Users/surajshashidhar/Desktop/ovgu/semester_2/XAI_project/reasearched_code_and_data/english_stopwords_verbs_nicknames.csv")
    ap.add_argument("--feature_fields", nargs= "?", required=False, help="feature_fields", default = constants.FEATURE_FIELD)
    ap.add_argument( "--language", nargs= "?", required=False, help=" language", default = constants.ENGLISH)
    ap.add_argument("--encoding", nargs= "?", required=False, help="encoding", default = "utf-8")
    ap.add_argument( "--book_start_percentage", nargs= "?", required=False, help=" book_start_percentage", default = constants.DEFAULT_BOOK_START_PERCENTAGE)
    ap.add_argument("--book_end_percentage", nargs= "?", required=False, help="book_end_percentage", default = constants.DEFAULT_BOOK_END_PERCENTAGE)
    ap.add_argument( "--similarity_type", nargs= "?", required=False, help=" similarity_type", default = constants.L2)
    ap.add_argument("--new_feature_file_path", nargs= "?", required=False, help="new_feature_file_path", default = "/Users/surajshashidhar/git/fiction/Features_Extracted_milestone3_withf3.csv")
    ap.add_argument("--book_list_file_path", nargs= "?", required=False, help="book_list_file_path", default = "/Users/surajshashidhar/git/fiction/Final_Booklist.xlsx")
    ap.add_argument("--logging_flag", nargs= "?", required=False, help="logging_flag", default = "True")

    args = vars(ap.parse_args())

    feature_file_path = args["feature_file_path"]
    book_file_path = args["book_file_path"]
    stopwords_file_path = args["stopwords_file_path"]
    feature_fields = [int(args["feature_fields"])]
    language = args["language"]
    encoding = args["encoding"]
    book_start_percentage = float(args["book_start_percentage"])
    book_end_percentage = float(args["book_end_percentage"])
    similarity_type = args["similarity_type"]
    new_feature_file_path = args["new_feature_file_path"]
    book_list_file_path = args["book_list_file_path"]
    logging_flag = args["logging_flag"]
    if(logging_flag == "True"):
        logging_flag = True
    else:
        logging_flag = False
    
    start_time = time.time()

    driver_obj = Driver(feature_file_path, book_file_path, stopwords_file_path, \
         book_start_percentage, book_end_percentage,  new_feature_file_path, book_list_file_path, logging_flag)
    
    print("Language in driver: {}".format(language))
    driver_obj.run_extraction()
    print("--- %s seconds ---" % (time.time() - start_time))
    sys.exit(0)