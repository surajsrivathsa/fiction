import os
import re
import errno
import pandas as pd

class FileUtils:
    def __init__(self, feature_file_path, book_file_path, emoticon_filepath, feature_fields, language = "en", encoding='utf-8'):
        self.FILENAME_PARSE_REGEX = "(pg)([0-9]*).*?-content(.html)"
        self.HTML_TAGS_REGEX = "<.*?>"
        self.ffp = feature_file_path
        self.bfp = book_file_path
        self.efp = emoticon_filepath
        self.ff = feature_fields
        self.language = language
        self.encoding = encoding
        self.HTML_FILE_EXTENSION = ".html"
        self.EMPTY = ""
    
    def read_bookpath_and_extract_pgid(self):
        books_path_dict = {}
        filename_pattern = re.compile(self.FILENAME_PARSE_REGEX)
        for root, dirs, files in os.walk(self.bfp):
            print("Parsing directory {} for html files".format(root))
            for file in files:
                if file.endswith(self.HTML_FILE_EXTENSION) and os.stat(os.path.join(root, file)).st_size > 0:
                    arr = filename_pattern.search(file)
                    if arr:
                        books_path_dict[arr.group(1) + arr.group(2)] = [file, os.path.join(root, file)]
                elif os.stat(os.path.join(root, file)).st_size == 0:
                    print("Empty file found: {}".format(file))
        return books_path_dict


    def read_html_and_strip_tags(self, book_paths_dict):
        text = None
        html_content = None
        books_text_dict = {}
        for pgid, vals in book_paths_dict.items():
            filepath = vals[1]
            if not os.path.isfile(filepath):
                raise FileNotFoundError(errno.ENOENT, os.strerror(errno.ENOENT), filepath)

            else:
                f = open(filepath, "r")
                html_content = f.read()            
                text = re.sub(self.HTML_TAGS_REGEX, self.EMPTY, html_content)
                books_text_dict[pgid] = text        
        return books_text_dict;


    def create_emoticon_dict(self):
        emoticon_df = pd.read_csv(self.efp, header=0)
        emoticon_df.drop(columns=['Unnamed: 0'], axis = 1, inplace=True)
        emoticon_df.index = emoticon_df.word
        # print(emoticon_df.head())
        emoticon_dict = emoticon_df.to_dict(orient = "index")
        # print(emoticon_dict["abate"])
        return emoticon_dict;
    

    def read_feature_and_similarity_file_and_preprocess(self, feature_file_path, similarity_file_path, feature_fields, new_feature_file_path):
        feature_df = pd.read_csv(feature_file_path, header = 0)
        tmp_df = feature_df["bookId-chunkNo"].str.split("-", n = 1, expand = True) 
        
        # making separate first name column from new data frame 
        feature_df["book_id"]= tmp_df[0] 
        
        # making separate last name column from new data frame 
        feature_df["chunk_id"]= tmp_df[1] 

        similarity_df = pd.read_csv(similarity_file_path, header = 0)

        out_df = pd.merge(left = feature_df, right = similarity_df, how = "inner", left_on = ["book_id"], right_on = "book_id")
        out_df["similarity"] = out_df["similarity"].round(4)

        out_df.drop(columns =["book_id", "chunk_id"], inplace = True)
        out_df.rename(columns = {'similarity':'F' + str(feature_fields[0])}, inplace = True) 

        out_df.to_csv(path_or_buf = new_feature_file_path, index = False)

        print(out_df.head(10))
        return;

    