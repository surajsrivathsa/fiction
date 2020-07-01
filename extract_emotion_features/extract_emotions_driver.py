import extract_emotions_file_utils
import extract_emotions_feature_utils
import extract_emotions_constants as constants
import os
import sys
import time
import argparse
import logging
import time

class Driver:
    
    def __init__(self, feature_file_path = "/Users/surajshashidhar/git/fiction/bkp/Features_Extracted.csv", 
    book_file_path = "/Users/surajshashidhar/git/fiction/Short_epubs_extracted", 
    emoticon_file_path = "/Users/surajshashidhar/Desktop/ovgu/semester_2/XAI_project/reasearched_code_and_data/all_language_emotions.csv",
    feature_fields = [22], language = "de", encoding='utf-8',
    book_start_percentage = 0.1, book_end_percentage = 0.1,
    similarity_type = "L2", new_feature_file_path = "", book_list_file_path = "/Users/surajshashidhar/git/fiction/Final_Booklist.xlsx",
    logging_flag = False):
        self.feature_file_path = feature_file_path
        self.book_file_path = book_file_path
        self.emoticon_file_path = emoticon_file_path
        self.feature_fields = feature_fields
        self.language = language
        self.encoding = encoding
        self.book_start_percentage = book_start_percentage
        self.book_end_percentage = book_end_percentage
        self.similarity_file_path = constants.FILE_PATH_SIMILARITY
        self.similarity_type = similarity_type
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
        fileutils_obj = extract_emotions_file_utils.FileUtils(self.feature_file_path, self.book_file_path, self.emoticon_file_path, self.feature_fields, 
        self.book_list_file_path, logging_flag = self.logging_flag, log = self.log)
        books_lang_dict = fileutils_obj.read_booklist_and_preprocess()
        
        books_path_dict = fileutils_obj.read_bookpath_and_extract_pgid()
        # print(books_path_dict)
        books_text_dict = fileutils_obj.read_html_and_strip_tags(books_path_dict, books_lang_dict)
        # print(books_text_dict.keys())
        # print(books_text_dict["pg16909"])
        emoticon_dict = fileutils_obj.create_emoticon_dict()
        
        emotionutils_obj = extract_emotions_feature_utils.EmotionUtils(self.feature_file_path, self.book_file_path, self.emoticon_file_path, 
        self.feature_fields, self.language, logging_flag = self.logging_flag, log = self.log)
        emotionutils_obj.initialize_nlp_pipeline()
        books_sentences_dict = emotionutils_obj.get_books_to_sentences_dict(books_text_dict)
        """
        for key, val in books_sentences_dict.items():
            print()
            print("book name: {}, length of sentences : {}".format(key, len(val)))
            print(" ========== ============ ============ ")
        """
        
        books_emotions_dict = emotionutils_obj.run_emotion_analysis(books_sentences_dict, emoticon_dict)

        books_feature_vectors = emotionutils_obj.create_feature_vector(books_emotions_dict, self.book_start_percentage, self.book_end_percentage)

        #Saving Feature vectors in a file for analysis
        fileutils_obj.save_feature_vectors(books_feature_vectors)

        books_similarity_dict= emotionutils_obj.run_similarity_for_all_books(books_feature_vectors, type=self.similarity_type)

        # books_similarity_dict_cosine = emotionutils_obj.run_similarity_for_all_books(books_feature_vectors, type="cosine")

        similarity_df = emotionutils_obj.create_and_save_similarity_dataframe(books_similarity_dict, self.similarity_file_path)
        
        #fileutils_obj.read_feature_and_similarity_file_and_preprocess(self.feature_file_path, self.similarity_file_path, self.feature_fields, self.new_feature_file_path)
        
#

if __name__ == "__main__":
    ap = argparse.ArgumentParser()

    # Add the arguments to the parser and parse the arguments from command line
    ap.add_argument( "--feature_file_path", nargs= "?", required=False, help=" feature_file_path", default = "/Users/surajshashidhar/git/fiction/Features_extracted_milestone3.csv")
    ap.add_argument("--book_file_path", nargs= "?", required=False, help="book_file_path", default = "/Users/surajshashidhar/git/fiction/German_B1_extracted")
    ap.add_argument( "--emoticon_file_path", nargs= "?", required=False, help=" emoticon_file_path", default="/Users/surajshashidhar/git/fiction/extract_emotion_features/all_language_emotions.csv")
    ap.add_argument("--feature_fields", nargs= "?", required=False, help="feature_fields", default = constants.FEATURE_FIELD)
    ap.add_argument( "--language", nargs= "?", required=False, help=" language", default = constants.ENGLISH)
    ap.add_argument("--encoding", nargs= "?", required=False, help="encoding", default = "utf-8")
    ap.add_argument( "--book_start_percentage", nargs= "?", required=False, help=" book_start_percentage", default = constants.DEFAULT_BOOK_START_PERCENTAGE)
    ap.add_argument("--book_end_percentage", nargs= "?", required=False, help="book_end_percentage", default = constants.DEFAULT_BOOK_END_PERCENTAGE)
    ap.add_argument( "--similarity_type", nargs= "?", required=False, help=" similarity_type", default = constants.L2)
    ap.add_argument("--new_feature_file_path", nargs= "?", required=False, help="new_feature_file_path", default = "/Users/surajshashidhar/git/fiction/Features_Extracted_milestone3_withf3.csv")
    ap.add_argument("--book_list_file_path", nargs= "?", required=False, help="book_list_file_path", default = "/Users/surajshashidhar/git/fiction/miscellaneous_python/Final_Booklist.xlsx")
    ap.add_argument("--logging_flag", nargs= "?", required=False, help="logging_flag", default = "True")

    args = vars(ap.parse_args())

    feature_file_path = args["feature_file_path"]
    book_file_path = args["book_file_path"]
    emoticon_file_path = args["emoticon_file_path"]
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

    driver_obj = Driver(feature_file_path, book_file_path, emoticon_file_path, feature_fields, \
        language, encoding, book_start_percentage, book_end_percentage, similarity_type, new_feature_file_path, book_list_file_path, logging_flag)
    
    print("Language in driver: {}".format(language))
    driver_obj.run_extraction()
    print("--- %s seconds ---" % (time.time() - start_time))
    sys.exit(0)