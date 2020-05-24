import feature1_file_utils
import feature1_genre_utils
import argparse
import sys

class Driver:
    
    def __init__(self, feature_file_path , book_file_path,master_file_path ,new_feature_file_path , encoding='utf-8'):
        self.feature_file_path = feature_file_path
        self.book_file_path = book_file_path
        self.master_file_path = master_file_path
        self.encoding = encoding
        self.new_feature_file_path = new_feature_file_path
        self.chunk_size = 3
    
    def get_book_vec(self):
        gen_utils_obj = feature1_genre_utils.GenreUtils(self.book_file_path,self.chunk_size,self.master_file_path)
        print("Generated object for gen_utils")
        fileutils_obj = feature1_file_utils.FileUtils(self.book_file_path,self.feature_file_path,self.new_feature_file_path,self.chunk_size,self.master_file_path)
        print("Generated object for file_utils")
        books = fileutils_obj.read_bookpath_and_extract_pgid()
        print("Retrieved books pgid")
        read_books = fileutils_obj.read_html_files(books)
        print("Read html files")
        words_books, sentence_books = fileutils_obj.get_preprocessed_books(read_books)
        print("Got the pre-processed books")
        chunk_vector_df = gen_utils_obj.chunk_vector_gen(words_books, sentence_books)
        print("Got chunkk vecs")
        output_feature_vecs = fileutils_obj.write_feature_file(chunk_vector_df)
        print("Successfully written the file")

if __name__ == "__main__":
    ap = argparse.ArgumentParser()

    # Add the arguments to the parser and parse the arguments from command line
    ap.add_argument( "--feature_file_path", required=True, help="feature_file_path")
    ap.add_argument("--book_file_path", required=True, help="book_file_path")
    ap.add_argument( "--master_file_path", required=True, help="master_file_path")
    ap.add_argument("--encoding", required=True, help="encoding")
    ap.add_argument("--new_feature_file_path", required=True, help="new_feature_file_path")

    args = vars(ap.parse_args())

    feature_file_path = args["feature_file_path"]
    book_file_path = args["book_file_path"]
    master_file_path = args["master_file_path"]
    encoding = args["encoding"]
    new_feature_file_path = args["new_feature_file_path"]
    print("Creating driver object")
    driver_obj = Driver(feature_file_path,book_file_path,master_file_path,new_feature_file_path,encoding)
    print("Calling book vecs")
    driver_obj.get_book_vec()
    sys.exit(0)
