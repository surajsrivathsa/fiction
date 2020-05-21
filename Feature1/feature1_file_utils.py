import pandas as pd
import re
import spacy
import os

class FileUtils:
    def __init__(self,book_file_path,feature_file_path,new_feature_file_path,chunk_size ,master_file_path, encoding='utf-8'):
        self.FILENAME_PARSE_REGEX = "(pg)([0-9]*).*?-content(.html)"
        self.FEATURE_FILENAME_PARSE_REGEX = "(pg)([0-9]*).*"
        self.bfp = book_file_path
        self.feature_file_path = feature_file_path
        self.new_feature_file_path = new_feature_file_path
        self.HTML_TAGS_REGEX = "<.*?>"
        self.master_file_path = master_file_path
        self.encoding = encoding
        self.HTML_FILE_EXTENSION = ".html"
        self.EMPTY = ""
        self.lang_file = {}
        new_file = pd.read_csv(self.master_file_path)
        for bid in new_file.bid:
            self.lang_file[bid] = new_file[new_file.bid == bid].blang.to_list()


    
    def read_bookpath_and_extract_pgid(self):
        #Reading the pgids from the filename
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
        
    
    def read_html_files(self , books_path_dict):
        #Reading the html files
        book_data = {}
        for file_name in books_path_dict:
            book_data[file_name] = []
            with open(books_path_dict[file_name][1],encoding='latin-1') as opened_file:
                file_data = opened_file.read()
                if(len(file_data)!=0):
                    book_data[file_name].append(file_data)
                    print("Filename "+file_name)
                    print("File length "+ str(len(book_data[file_name][0])))
                else:
                    print("Empty file present")
        return book_data

    
    def Perform_preprocess(self,books,lang):
        if(lang == ["en"]):
            nlp = spacy.load('en_core_web_sm')
            spacy_stopwords = spacy.lang.en.stop_words.STOP_WORDS
        else:
            nlp = spacy.load('de_core_news_sm')
            spacy_stopwords = spacy.lang.de.stop_words.STOP_WORDS
        
        new_list = []
        new_sent = ""
        for sentence in books:
            introduction_doc = nlp(sentence)
            tokens = [token.text for token in introduction_doc]
            words = [w.lower() for w in tokens]
            words = [word for word in words if len(word)>2]
            words = [w for w in words if not w in spacy_stopwords]
        for w in words:
            new_sent = new_sent +' '+ w
        new_list.append(new_sent)
        return words,new_list

    def get_preprocessed_books(self,books):
    #Get pre-processed books in both BOW model and sentence model
        data_pre_sentences={}
        data_pre_words={}
        for file_name in books:
            data_pre_sentences[file_name] = []
            data_pre_words[file_name] = []
            lang = self.get_lang(file_name)
            words,new_list = self.Perform_preprocess(books[file_name],lang)
            data_pre_sentences[file_name].append(new_list)
            data_pre_words[file_name].append(words)
        return (data_pre_sentences,data_pre_words)
    
    def get_lang(self,file_name):
        filename_pattern = re.compile(self.FEATURE_FILENAME_PARSE_REGEX)
        arr = filename_pattern.search(file_name)
        return self.lang_file[int(arr.group(2))] 

    def write_feature_file(self,chunk_vector_df):
        feat_file = pd.read_csv(self.feature_file_path)
        files = feat_file['bookId-chunkNo']
        filename_pattern = re.compile(self.FEATURE_FILENAME_PARSE_REGEX)
        names= []
        for file_name in files:
            arr = filename_pattern.search(file_name)
            names.append(arr.group(1) + arr.group(2))
        feat_file['filenames']=names
        merged_df = feat_file.merge(chunk_vector_df,left_on='filenames',right_on = chunk_vector_df.index)
        merged_df.index = merged_df['bookId-chunkNo']
        merged_df = merged_df.drop("bookId-chunkNo",axis = 1)
        merged_df = merged_df.drop("filenames",axis = 1)
        print(self.new_feature_file_path)
        print(merged_df)
        merged_df.to_csv(self.new_feature_file_path)
        return merged_df






    




