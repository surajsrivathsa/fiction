import os
import re
import errno
import pandas as pd
import extract_emotions_constants as constants

class FileUtils:
    def __init__(self, feature_file_path, book_file_path, emoticon_filepath, feature_fields, book_list_file_path, language = "en", encoding='utf-8',
    logging_flag=False, log = None):
        self.FILENAME_PARSE_REGEX = constants.FILENAME_PARSE_REGEX
        self.HTML_TAGS_REGEX = constants.HTML_TAGS_REGEX
        self.ffp = feature_file_path
        self.bfp = book_file_path
        self.efp = emoticon_filepath
        self.ff = feature_fields
        self.language = language
        self.encoding = encoding
        self.HTML_FILE_EXTENSION = constants.HTML_FILE_EXTENSION
        self.EMPTY = constants.EMPTY
        self.blfp = book_list_file_path
        self.SHEETNAME=constants.SHEETNAME
        self.logging_flag = logging_flag
        self.log = log

    """
    fnc: read_bookpath_and_extract_pgid
    Input: None
    Output: books_path_dict Ex: {"pg123": "/Usr/myhome/pg123-content.html", "pg234": "/Usr/myhome/pg234-content.html"}

    Description: parse the extracted epub directory and make a dictionary of pgid ---> file path. Reject empty files or non-html files

    Extension: NA
    """
    
    def read_bookpath_and_extract_pgid(self):
        books_path_dict = {}
        filename_pattern = re.compile(self.FILENAME_PARSE_REGEX)
        print(self.bfp)
        for root, dirs, files in os.walk(self.bfp):
            print("Parsing directory {} for html files".format(root))
            if(self.logging_flag):
                self.log.info("Parsing directory {} for html files".format(root))
            for file in files:
                if file.endswith(self.HTML_FILE_EXTENSION) and os.stat(os.path.join(root, file)).st_size > 0:
                    arr = filename_pattern.search(file)
                    if arr:
                        books_path_dict[arr.group(1) + arr.group(2)] = [file, os.path.join(root, file)]
                elif os.stat(os.path.join(root, file)).st_size == 0:
                    print("Empty file found: {}".format(file))
                    if(self.logging_flag):
                        self.log.warning("Empty file found: {}".format(file))
        return books_path_dict

    """
    fnc: read_html_and_strip_tags
    Input: book_paths_dict, books_lang_dict
    Output: books_text_dict 

    Description: read the html file and strip the html tags and convert it to text. Also lookup the pgid in book list excel file.
    If not present then reject it. If present then fetch the language of the book.
    Later language would be used to choose nlp pipeline in get_sentencetokens_and_remove_stopwords

    Extension: NA.
    """

    def read_html_and_strip_tags(self, book_paths_dict, books_lang_dict):
        text = None
        html_content = None
        books_text_dict = {}
        for pgid, vals in book_paths_dict.items():
            filepath = vals[1]
            if not os.path.isfile(filepath):
                if(self.logging_flag):
                    self.log.error("File not found in path : {}".format(filepath))
                raise FileNotFoundError(errno.ENOENT, os.strerror(errno.ENOENT), filepath)
            
            elif (pgid not in books_lang_dict):
                if(self.logging_flag):
                    self.log.warning("Book {} found in filepath but not in book list file, but processing it anyway".format(pgid))
                # if not found in book list then set english as default language
                language = constants.ENGLISH
                f = open(filepath, "r")
                html_content = f.read()            
                text = re.sub(self.HTML_TAGS_REGEX, self.EMPTY, html_content)
                books_text_dict[pgid] = [text, language] 
                print("Book {} found in filepath but not in book list file, but processing it anyway".format(pgid))   
                #raise Exception("Book {} found in filepath but not in book list file".format(pgid))

            elif (pgid in book_paths_dict):
                language = books_lang_dict[pgid][1]
                f = open(filepath, "r")
                html_content = f.read()            
                text = re.sub(self.HTML_TAGS_REGEX, self.EMPTY, html_content)
                books_text_dict[pgid] = [text, language]        
        
        print("Number of html books extracted to dict: {}".format(len(books_text_dict.keys())))
        print()
        if(self.logging_flag):
            self.log.info("Number of html books extracted to dict: {}".format(len(books_text_dict.keys())))
        return books_text_dict;

    """
    fnc: create_emoticon_dict
    Input: None
    Output: emoticon_dict 

    Description: Read the NRC emoticon file and create a dictionary

    Extension: Some other emoticon can be used if it follows existing format
    """

    def create_emoticon_dict(self):
        emoticon_df = pd.read_csv(self.efp, header=0)
        # emoticon_df.drop(columns=['Unnamed: 0'], axis = 1, inplace=True)
        emoticon_df.index = emoticon_df.word
        # print(emoticon_df.head())
        emoticon_dict = emoticon_df.to_dict(orient = constants.INDEX)
        # print(emoticon_dict["abate"])
        print(emoticon_df.describe())
        print(" == ====== ======== =======")
        print()
        if(self.logging_flag):
            self.log.info(emoticon_df.describe())
            self.log.info(emoticon_df.head())
        return emoticon_dict;
    
    """
    fnc: read_feature_and_similarity_file_and_preprocess
    Input: feature_file_path, similarity_file_path, feature_fields, new_feature_file_path
    Output: None 

    Description: Join the feature3 similarity file with existing feature file on pgid using inner join and write out to file

    Extension: Could be changed based on file format changes , new column inclusion etc
    """

    def read_feature_and_similarity_file_and_preprocess(self, feature_file_path, similarity_file_path, feature_fields, new_feature_file_path):
        feature_df = pd.read_csv(feature_file_path, header = 0)
        tmp_df = feature_df[constants.BOOKID_CHUNKNO].str.split(constants.DASH, n = 1, expand = True) 
        
        # making separate first name column from new data frame 
        feature_df[constants.BOOK_ID]= tmp_df[0] 
        
        # making separate last name column from new data frame 
        feature_df[constants.CHUNK_ID]= tmp_df[1] 

        similarity_df = pd.read_csv(similarity_file_path, header = constants.ZERO)

        out_df = pd.merge(left = feature_df, right = similarity_df, how = constants.JOIN_TYPE, left_on = [constants.BOOK_ID], right_on = constants.BOOK_ID)
        out_df[constants.SIMILARITY] = out_df[constants.SIMILARITY].round(4)

        out_df.drop(columns =[constants.BOOK_ID, constants.CHUNK_ID], inplace = True)
        out_df.rename(columns = {constants.SIMILARITY:'F' + str(feature_fields[0])}, inplace = True) 

        out_df.to_csv(path_or_buf = new_feature_file_path, index = False)

        print(out_df.head(10))

        if(self.logging_flag):
            self.log.info(out_df.head(20))
        return;
    
    """
    fnc: read_booklist_and_preprocess
    Input: None
    Output: book_lang_dict Ex: {pg123: "en", pg234: "de"} 

    Description: Read the booklist excel file and extract pgid and language of each book into a dictionary

    Extension: Could be changed based on file format changes , new column inclusion etc
    """
    
    def read_booklist_and_preprocess(self):
        df = pd.read_excel(io=self.blfp, sheet_name=self.SHEETNAME,header=constants.ZERO)
        print("Reading and printing book list file")
        print(df.head(10))

        if(self.logging_flag):
            self.log.info("Reading and printing book list file")
            self.log.info(df.head(20))

        tmp_dict = df.to_dict(orient="list")
        
        book_lang_dict = {}
        for pgid, lang in list(zip(tmp_dict[constants.BID], tmp_dict[constants.BLANG])):
            book_lang_dict[constants.PG + str(pgid)] = [pgid, lang]
        
        x = list(book_lang_dict.keys())
        print(x[1:3])
        print("==== ========= ======== ======== ========== ========= ")
        print()
        if(self.logging_flag):
            self.log.info(x[1:3])
        return book_lang_dict;