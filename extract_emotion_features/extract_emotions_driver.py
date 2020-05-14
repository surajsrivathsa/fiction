import extract_emotions_file_utils
import extract_emotions_feature_utils
import os
import sys
import time
import argparse

class ComplexNumber:
    def __init__(self,r = 0,i = 0):
        self.real = r
        self.imag = i

    def getData(self):
        print("{0}+{1}j".format(self.real,self.imag))

class Driver:
    
    def __init__(self, feature_file_path = "/Users/surajshashidhar/git/fiction/bkp/Features_Extracted.csv", 
    book_file_path = "/Users/surajshashidhar/git/fiction/Short_epubs_extracted", 
    emoticon_file_path = "/Users/surajshashidhar/Desktop/ovgu/semester_2/XAI_project/reasearched_code_and_data/english_emotions.txt",
    feature_fields = [22], language = "en", encoding='utf-8',
    book_start_percentage = 0.1, book_end_percentage = 0.1,
    similarity_type = "L2", new_feature_file_path = ""):
        self.feature_file_path = feature_file_path
        self.book_file_path = book_file_path
        self.emoticon_file_path = emoticon_file_path
        self.feature_fields = feature_fields
        self.language = language
        self.encoding = encoding
        self.book_start_percentage = book_start_percentage
        self.book_end_percentage = book_end_percentage
        self.similarity_file_path = os.path.join(os.getcwd(), "simialrity_feature.csv")
        self.similarity_type = similarity_type
        self.new_feature_file_path = "/Users/surajshashidhar/git/fiction/Short_epubs_extracted/new_Features_Extracted.csv"

    
    def run_extraction(self):
        book_paths_dict = None
        fileutils_obj = extract_emotions_file_utils.FileUtils(self.feature_file_path, self.book_file_path, self.emoticon_file_path, self.feature_fields)
        
        
        books_path_dict = fileutils_obj.read_bookpath_and_extract_pgid()
        # print(books_path_dict)
        books_text_dict = fileutils_obj.read_html_and_strip_tags(books_path_dict)
        # print(books_text_dict.keys())
        # print(books_text_dict["pg16909"])
        emoticon_dict = fileutils_obj.create_emoticon_dict()

        emotionutils_obj = extract_emotions_feature_utils.EmotionUtils(self.feature_file_path, self.book_file_path, self.emoticon_file_path, self.feature_fields)
        emotionutils_obj.initialize_nlp_pipeline()
        books_sentences_dict = emotionutils_obj.get_books_to_sentences_dict(books_text_dict)

        for key, val in books_sentences_dict.items():
            print()
            print("book name: {}, length of sentences : {}".format(key, len(val)))
            print(" ========== ============ ============ ")

        books_emotions_dict = emotionutils_obj.run_emotion_analysis(books_sentences_dict, emoticon_dict)

        books_feature_vectors = emotionutils_obj.create_feature_vector(books_emotions_dict, self.book_start_percentage, self.book_end_percentage)

        books_similarity_dict= emotionutils_obj.run_similarity_for_all_books(books_feature_vectors, type=self.similarity_type)

        # books_similarity_dict_cosine = emotionutils_obj.run_similarity_for_all_books(books_feature_vectors, type="cosine")

        similarity_df = emotionutils_obj.create_and_save_similarity_dataframe(books_similarity_dict, self.similarity_file_path)
        
        fileutils_obj.read_feature_and_similarity_file_and_preprocess(self.feature_file_path, self.similarity_file_path, 
                                                                      self.feature_fields, self.new_feature_file_path)


if __name__ == "__main__":
    ap = argparse.ArgumentParser()

    # Add the arguments to the parser and parse the arguments from command line
    ap.add_argument( "--feature_file_path", required=True, help=" feature_file_path")
    ap.add_argument("--book_file_path", required=True, help="book_file_path")
    ap.add_argument( "--emoticon_file_path", required=True, help=" emoticon_file_path")
    ap.add_argument("--feature_fields", required=True, help="feature_fields")
    ap.add_argument( "--language", required=True, help=" language")
    ap.add_argument("--encoding", required=True, help="encoding")
    ap.add_argument( "--book_start_percentage", required=True, help=" book_start_percentage")
    ap.add_argument("--book_end_percentage", required=True, help="book_end_percentage")
    ap.add_argument( "--similarity_type", required=True, help=" similarity_type")
    ap.add_argument("--new_feature_file_path", required=True, help="new_feature_file_path")

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

    start_time = time.time()

    driver_obj = Driver(feature_file_path, book_file_path, emoticon_file_path, feature_fields, \
        language, encoding, book_start_percentage, book_end_percentage, similarity_type, new_feature_file_path)
    
    driver_obj.run_extraction()
    print("--- %s seconds ---" % (time.time() - start_time))
    sys.exit(0)