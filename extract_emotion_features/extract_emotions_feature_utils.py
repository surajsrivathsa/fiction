from nltk import word_tokenize
from nltk import tokenize
from nltk.corpus import stopwords 
from nltk.stem.snowball import SnowballStemmer
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from collections import defaultdict
import math
import itertools
from nltk.stem import WordNetLemmatizer 

class EmotionUtils:
    def __init__(self, feature_file_path, book_file_path, emoticon_filepath, feature_fields, language = "en", encoding='utf-8'):
        self.ffp = feature_file_path
        self.bfp = book_file_path
        self.efp = emoticon_filepath
        self.ff = feature_fields
        self.language = language
        self.encoding = encoding
        self.stopword_remover = None
        self.stemmer = None
        self.lemmatizer = None
        self.book_start_feature_vector_init = [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]
        self.book_end_feature_vector_init = [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]
        self.COLNAME1 = "similarity"
        self.COLNAME2 = "book_id"


    def initialize_nlp_pipeline(self):
        if(self.language == "en"):
            self.stopword_remover = set(stopwords.words('english'))
            self.stemmer = SnowballStemmer("english")
            self.lemmatizer = WordNetLemmatizer()
        elif(self.language == "de"):
            self.stopword_remover = set(stopwords.words('german'))
            self.stemmer = SnowballStemmer("german")
            self.lemmatizer = WordNetLemmatizer()
        else:
            raise Exception("Cannot initialize nlp pipeline due to unknown language, allowed language codes are en and de")

        return;


    def get_sentencetokens_and_remove_stopwords( self, text):
        sentence_list = None
        sentence_list = tokenize.sent_tokenize(text)
        processed_tokens = []
        sentence_idx = [i for i in range(len(sentence_list))]
        sentence_dict = {}
        
        for i, s in zip(sentence_idx, sentence_list):
            word_tokens = tokenize.word_tokenize(s)
            filtered_tokens = [w for w in word_tokens if not w in self.stopword_remover]
            processed_tokens = []
            for token in filtered_tokens:
                    token = self.lemmatizer.lemmatize(token.lower())
                    #self.stemmer.stem(token.lower())
                    processed_tokens.append(token)
            
            sentence_dict["s" + str(i)] = processed_tokens
            #complete_sentence = " ".join(filtered_sentence)
        
        return sentence_dict;

    
    def get_books_to_sentences_dict(self, books_text_dict): 
        books_sentences_dict = {}      
        for pgid, text in books_text_dict.items():
            sentence_dict = self.get_sentencetokens_and_remove_stopwords(text)
            books_sentences_dict[pgid] = sentence_dict        
        return books_sentences_dict;


    def get_emotion_per_sentence(self, sen_val, emoticon_dict):
        default_emotions = [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]
        sentence_emotions = [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]
        for v in sen_val:
            if  v in emoticon_dict:
                tmp_dict = emoticon_dict[v]
                sentence_emotions[0] += tmp_dict["anger"]
                sentence_emotions[1] += tmp_dict["anticipation"]
                sentence_emotions[2] += tmp_dict["disgust"]
                sentence_emotions[3] += tmp_dict["fear"]
                sentence_emotions[4] += tmp_dict["joy"]
                sentence_emotions[5] += tmp_dict["sadness"]
                sentence_emotions[6] += tmp_dict["surprise"]
                sentence_emotions[7] += tmp_dict["trust"]
            else:
                sentence_emotions = default_emotions
        
        sumlst = sum(sentence_emotions)
        
        #Normalize the scores across all emotions
        if (sumlst > 0):
            sentence_emotions = [x/sumlst for x in sentence_emotions]
        return sentence_emotions;

    
    def run_emotion_analysis(self, books_sentences_dict, emoticon_dict):
        
        books_emotions_dict = {}
        
        for book_key,sentences in books_sentences_dict.items():        
            sentence_emotions_dict = {}
            for sen_key, sen_val in sentences.items():
                
                emotion_per_sentence_list = self.get_emotion_per_sentence(sen_val, emoticon_dict)
                sentence_emotions_dict[sen_key] = emotion_per_sentence_list
            
            books_emotions_dict[book_key] = sentence_emotions_dict
        
        return books_emotions_dict;


    def create_feature_vector(self, books_emotions_dict, book_start_percentage, book_end_percentage):
        books_feature_vectors = {}
        for key, emo_dict in books_emotions_dict.items():
            print()
            print("======= {} ======".format(key))
            book_start_feature_vector = self.book_start_feature_vector_init
            book_end_feature_vector = self.book_end_feature_vector_init
            book_length = len(emo_dict)
            bsp_idx = int(book_length * book_start_percentage)
            bep_idx = int(book_length * book_end_percentage)
            print("Num of start sentences: {} ; num of ending sentences: {}".format(bsp_idx, bep_idx))
            
            if(bsp_idx > 10 and bep_idx > 10):

                for i in range(0,bsp_idx):
                    tmp = emo_dict["s" + str(i)]
                    book_start_feature_vector = [a + b for a, b in zip(book_start_feature_vector, tmp)]
                
                for i in range(bep_idx, book_length):
                    tmp = emo_dict["s" + str(i)]
                    book_end_feature_vector = [a + b for a, b in zip(book_end_feature_vector, tmp)]

                #Normalizing by length of sentences
                book_start_feature_vector = [x/bsp_idx for x in book_start_feature_vector]
                book_end_feature_vector = [x/ bep_idx for x in book_end_feature_vector]
                
                #Normalizing by emotions to make the sum of emotions sum upto one
                sum_book_start_feature_vector = sum(book_start_feature_vector)
                sum_book_end_feature_vector = sum(book_end_feature_vector)

                if(sum_book_start_feature_vector == 0):
                    sum_book_start_feature_vector = 1
                
                if(sum_book_end_feature_vector == 0):
                    sum_book_end_feature_vector = 1

                book_start_feature_vector = [x * 10/ sum_book_start_feature_vector for x in book_start_feature_vector]
                book_end_feature_vector = [x * 10/ sum_book_end_feature_vector for x in book_end_feature_vector]
                
                books_feature_vectors[key] = [book_start_feature_vector, book_end_feature_vector]
            
            else:
                print("Skipping book {} as it has less than 10 start and end sentences".format(key))
                print( "= ==================== ============ ============= ")
                print()
        
        return books_feature_vectors;


    def find_Cosine_similarity(self, X, Y):
        x_distance = math.sqrt(sum([(a) ** 2 for a in X]))
        y_distance = math.sqrt(sum([(b) ** 2 for b in Y]))
        
        dot_product = sum([(a * b) for a, b in zip(X, Y)])
        print("x: {}, y: {}, dotproduct: {}".format(x_distance, y_distance, dot_product))
        similarity = dot_product / (x_distance * y_distance)
        
        return similarity;


    def find_L2_similarity(self, X, Y):
        distance = math.sqrt(sum([(a - b) ** 2 for a, b in zip(X, Y)]))
        # print("Euclidean distance from x to y: {}".format(distance))
        similarity = 1 / (1 + distance)
        # print("Similarity is: {}".format(similarity))
        return similarity;


    def find_book_similarities(self, feature_vector, book1, book2="", type="L2_between_books"):
        if type == "L2_between_books":
            book1_start = feature_vector[book1][0]; book1_end = feature_vector[book1][1]; 
            book2_start = feature_vector[book2][0]; book2_end = feature_vector[book2][1]; 
            start_similarity = self.find_L2_similarity(book1_start, book2_start)
            end_similarity = self.find_L2_similarity(book1_end, book2_end)
            #print("Start similarity and End similarity between {} and {} are : {} and {}".format(book1, book2, start_similarity, end_similarity))
            #print()
            #print(" ======== =========== ============= ============ =========== ")
            return [ start_similarity, end_similarity];
        elif type == "cosine":
            book1_start = feature_vector[book1][0]; book1_end = feature_vector[book1][1];
            book_similarity = self.find_Cosine_similarity(book1_start, book1_end)
            #print("Similarity between start and end for the book {} is : {}".format(book1, book_similarity))
            #print()
            #print(" ======== =========== ============= ============ =========== ")
            return [ book_similarity]
        elif type == "L2":
            book1_start = feature_vector[book1][0]; book1_end = feature_vector[book1][1]; 
            book_similarity = self.find_L2_similarity(book1_start, book1_end)
            #print("Similarity between start and end for the book {} is : {}".format(book1, book_similarity))
            #print()
            #print(" ======== =========== ============= ============ =========== ")
            return [book_similarity];
        else:
            raise Exception("Can only run for cosine and L2 similarity")
            return [None];


    def run_similarity_for_all_books(self, feature_vector, type = "L2_between_books"):
        if type == "L2_between_books":
            book1 = None; book2 = None; similarity_dict = {};
            permutation_list = list(itertools.permutations(feature_vector.keys(), 2))
            for i in range(len(permutation_list)):
                book1 = permutation_list[i][0]
                book2 = permutation_list[i][1]
                start_sim , end_sim = self.find_book_similarities(feature_vector, book1, book2, type = "L2_between_books")
                similarity_dict[book1 + "|" + book2] = [start_sim, end_sim]

            return similarity_dict
        elif type == "cosine":
            book = None; similarity_dict = {};
            for key in feature_vector.keys():            
                similarity= self.find_book_similarities(feature_vector, book1 = key, type = "cosine")
                similarity_dict[key] = similarity
            
            return similarity_dict;
        
        elif type == "L2":
            book = None; similarity_dict = {};
            for key in feature_vector.keys():            
                similarity= self.find_book_similarities(feature_vector, book1 = key, type = "L2")
                similarity_dict[key] = similarity
            
            return similarity_dict;
            
        else:
            raise Exception("Can only run for L2 and cosine similarity")
            return {};

      
    def create_and_save_similarity_dataframe(self, books_similarity_dict, similarity_file_path):
        df = pd.DataFrame.from_dict(books_similarity_dict, orient = "index", columns = [self.COLNAME1])
        df = df.sort_values([self.COLNAME1], ascending = (False))
        df[self.COLNAME2] = df.index
        df.index = [x for x in range(len(df.index))]
        df.to_csv( path_or_buf = similarity_file_path, index=False)
        return df;
    
    






