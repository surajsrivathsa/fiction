import os
import re
import errno
import pandas as pd
import topic_models_constants as constants
import pickle
import json

class FileUtils:
    def __init__(self, feature_file_path, book_file_path, stopwords_file_path,  book_list_file_path,
    logging_flag=False, log = None):
        self.FILENAME_PARSE_REGEX = constants.FILENAME_PARSE_REGEX
        self.HTML_TAGS_REGEX = constants.HTML_TAGS_REGEX
        self.ffp = feature_file_path
        self.bfp = book_file_path
        self.sfp = stopwords_file_path
        self.ff = None
        self.language = "en"
        self.HTML_FILE_EXTENSION = constants.HTML_FILE_EXTENSION
        self.EMPTY = constants.EMPTY
        self.blfp = book_list_file_path
        self.SHEETNAME=constants.SHEETNAME
        self.logging_flag = logging_flag
        self.log = log
        self.serialized_sentence_file_path = constants.FILE_PATH_SERIALIZED_SENTENCE_DICT
        self.serialized_topics_file_path = constants.FILE_PATH_SERIALIZED_TOPICS_DICT
        self.json_topics_file_path = constants.FILE_PATH_JSON_TOPICS_DICT
        self.csv_topics_file_path = constants.FILE_PATH_CSV_TOPICS_DF


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
                bname = constants.EMPTY
                f = open(filepath, "r")
                html_content = f.read()            
                text = re.sub(self.HTML_TAGS_REGEX, self.EMPTY, html_content)
                books_text_dict[pgid] = [text, language, bname] 
                print("Book {} found in filepath but not in book list file, but processing it anyway".format(pgid))   
                #raise Exception("Book {} found in filepath but not in book list file".format(pgid))

            elif (pgid in book_paths_dict):
                language = books_lang_dict[pgid][1]
                bname = books_lang_dict[pgid][2]
                f = open(filepath, "r")
                html_content = f.read()            
                text = re.sub(self.HTML_TAGS_REGEX, self.EMPTY, html_content)
                books_text_dict[pgid] = [text, language, bname]        
        
        print("Number of html books extracted to dict: {}".format(len(books_text_dict.keys())))
        print()
        if(self.logging_flag):
            self.log.info("Number of html books extracted to dict: {}".format(len(books_text_dict.keys())))
        return books_text_dict;

    
    def generate_stopwords_set(self):
        stopwords_df = pd.read_csv(self.sfp)
        
        stopwords_columns = stopwords_df.columns
        all_stopwords = []
        for col in stopwords_columns:
            tmp_lst = stopwords_df[col].tolist()
            cleaned_list = [x for x in tmp_lst if x == x]
            all_stopwords.extend(cleaned_list)
        
        cleaned_set = set(cleaned_list)
        print("Number of extra stopwords added: {}".format(len(cleaned_set)))
        print()
        if(self.logging_flag):
            self.log.info("Number of extra stopwords added: {}".format(len(cleaned_set)))

        return cleaned_set;


    """
    fnc: create_emoticon_dict
    Input: None
    Output: emoticon_dict 

    Description: Read the NRC emoticon file and create a dictionary

    Extension: Some other emoticon can be used if it follows existing format
    """

    def create_emoticon_dict(self):
        emoticon_df = pd.read_csv(self.sfp, header=0)
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
        df.drop_duplicates(subset = [constants.BID], keep = "last", inplace = True)
        if(self.logging_flag):
            self.log.info("Reading and printing book list file")
            self.log.info(df.head(20))
            self.log.info("")
            self.log.info(df.describe())

        tmp_dict = df.to_dict(orient="list")
        
        book_lang_dict = {} 
        for pgid, lang, bname in list(zip(tmp_dict[constants.BID], tmp_dict[constants.BLANG], tmp_dict[constants.BNAME])):
            book_lang_dict[constants.PG + str(pgid)] = [pgid, lang, bname]
        
        x = list(book_lang_dict.keys())
        print(x[1:3])
        print("==== ========= ======== ======== ========== ========= ")
        print()
        if(self.logging_flag):
            self.log.info(x[1:3])
        return book_lang_dict;


    """
    fnc: save_feature_vectors
    Input: books_feature_vectors
    Output: None

    Description: Save the feature vectors onto a csv file for later analysis.
    This method is optional and can be stopped running by commenting this method call from driver

    Extension: None
    """

    def save_feature_vectors(self,books_feature_vectors):
        flattened_feature_vector = {}
        #Including only two feature vectors while flattening, If book name is also included then it causes flattening issues as elements are of different shapes
        for key, val in books_feature_vectors.items():
            flat_list = [item for sublist in val[0:2] for item in sublist]

            #Appending book name separately
            flat_list.append(val[2])

            flattened_feature_vector[key] = flat_list

        df = pd.DataFrame.from_dict(flattened_feature_vector, orient=constants.INDEX,columns=constants.FEATURE_VECTOR_COLS)

        df[constants.BID] = df.index
        df.index = list(range(1, len(df.index) + 1))

        df.to_csv(path_or_buf= constants.FILE_PATH_FEATURES, index=False)
        if(self.logging_flag):
            self.log.info("Saving the feature vectors to file ")
            self.log.info(df.head(20))
            self.log.info(df.describe())



    def read_serialized_sentences_dict(self):
        with open(self.serialized_sentence_file_path, 'rb') as f:
            data = pickle.load(f)
        
        return data;
    
    def write_serialized_sentences_dict(self, books_sentences_dict):
        with open(self.serialized_sentence_file_path, 'bw') as f:
            pickle.dump(books_sentences_dict, f)
        return

    def read_serialized_topics_dict(self):
        with open(self.serialized_topics_file_path, 'rb') as f:
            data = pickle.load(f)
        
        return data;

    
    def write_serialized_topics_dict(self, topics_dict):
        with open(self.serialized_topics_file_path, 'bw') as f:
            pickle.dump(topics_dict, f)
        return


    def write_topics_json(self, topics_dict):
        with open(self.json_topics_file_path, 'w') as fp:
            json.dump(topics_dict, fp)

    def write_csv_topics_df(self, pandas_dict):
        topics_df = pd.DataFrame.from_dict(pandas_dict, orient=constants.INDEX,columns=constants.TOPICS_DF_COLUMNS)
        print(topics_df.head(10))
        if(self.logging_flag):
            self.log.info(topics_df.head(10))
            self.log.info(topics_df.describe())

        topics_df.to_csv(self.csv_topics_file_path)


