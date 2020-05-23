import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from collections import defaultdict
import math
import itertools
import spacy
import extract_emotions_constants as constants


class EmotionUtils:
    def __init__(self, feature_file_path, book_file_path, emoticon_filepath, feature_fields,  language, encoding='utf-8', logging_flag = False, log = None):
        self.ffp = feature_file_path
        self.bfp = book_file_path
        self.efp = emoticon_filepath
        self.ff = feature_fields
        self.language = language
        self.encoding = encoding
        self.stopword_remover = None
        self.stemmer = None
        self.lemmatizer = None
        self.book_start_feature_vector_init = constants.DEFAULT_EMOTIONS
        self.book_end_feature_vector_init = constants.DEFAULT_EMOTIONS
        self.COLNAME1 = constants.SIMILARITY
        self.COLNAME2 = constants.BOOK_ID
        self.nlp = None
        self.english_nlp = None
        self.german_nlp = None
        self.logging_flag = logging_flag
        self.log = log

    
    
    """
    fnc: initilaize_nlp_pipeline
    Input: None
    Output: None

    Description: Initilaizes english and german language SPACY NLP pipeline,
    We have disabled NER and POS parser as these take lot of time to parse and are not required for our purpose.
    Pipelines are set as object variables so that they can be accessed everywhere in program easilt.

    Extension: In future, if more languages are added we can change this function to include other language pipelines
    and set them as object variables.
    """

    def initialize_nlp_pipeline(self):
        self.english_nlp = spacy.load(constants.ENGLISH, disable = constants.DISABLE_LIST)
        self.english_nlp.add_pipe(self.english_nlp.create_pipe(constants.SENTENCE_TOKENIZER))
        self.english_nlp.max_length = constants.DOC_MAX_LENGTH
        self.german_nlp = spacy.load(constants.GERMAN, disable = constants.DISABLE_LIST)
        self.german_nlp.add_pipe(self.german_nlp.create_pipe(constants.SENTENCE_TOKENIZER))
        self.german_nlp.max_length = constants.DOC_MAX_LENGTH
        
        print("NLP Pipelines are initialized")
        print()
        if(self.logging_flag):
            self.log.info("NLP pipelines are initialized")
        
        """
        if(self.language == "en"):
            self.stopword_remover = set(stopwords.words('english'))
            self.stemmer = SnowballStemmer("english")
            self.lemmatizer = WordNetLemmatizer()
            self.nlp = None
        elif(self.language == "de"):
            print("Initializing German NLP pipeline")
            self.nlp = spacy.load('de', disable = ['ner', 'parser'])
            self.nlp.add_pipe(self.nlp.create_pipe('sentencizer'))
            self.stopword_remover = set(stopwords.words('german'))
        else:
            raise Exception("Cannot initialize nlp pipeline due to unknown language, allowed language codes are en and de")
        """
        return;

    """
    fnc: get_sentencetokens_and_remove_stopwords
    Input: book text (Ex: "It is cleared out. The steamer is ready now."), language string, book start and end percentages
    Output: sentence dictionary Ex: {s1: ["clear", "out"], s2: ["steamer", "ready"]}

    Description: Take a text of entire book and first sentence tokenize them. Select only first and last N percentage of the tokenized sentences
    as per book_start and book_end percentage. remove stopwords and lemmatize each token. Return the final dictionary and also index where 
    bookstart and bookend is partitioned. We will also filter out any texts that do not have some minimum number of start and end sentences according 
    to MINIMUM_SENTENCE_LIMIT variable. Again while applying lemmatizer and stopword removal we use the corresponding NLP pipeline for
    each language text using variable lang. 

    Extension: In future, if more languages are added we can change this function and add to existing if/else code another language processing.
    """
    # add book start and book end functionality here itself

    def get_sentencetokens_and_remove_stopwords( self, text, lang, book_start_percentage=constants.DEFAULT_BOOK_START_PERCENTAGE, book_end_percentage=constants.DEFAULT_BOOK_END_PERCENTAGE):
        sentence_list = None
        sentence_dict = {}
        partition_idx = None
        if (lang == constants.ENGLISH):
            processed_tokens = []
            sentence_dict = {}

            # Find the book start and end indices usng percentages
            doc = self.english_nlp(text)
            sentence_list = [sent for sent in doc.sents]
            book_length = len(sentence_list)
            bsp_idx = int(book_length * book_start_percentage)
            bep_idx = int(book_length - book_length * book_end_percentage)
            sentence_idx = [i for i in range(len(sentence_list))]

            # Filter out any books that are under minimum sentence limit
            if(bsp_idx > constants.MINIMUM_SENTENCE_LIMIT and bep_idx > constants.MINIMUM_SENTENCE_LIMIT):

                #Select the book start and end sentences according to index
                book_start_sentences = sentence_list[:bsp_idx]
                book_end_sentences = sentence_list[bep_idx:]
                if(self.logging_flag):
                    self.log.info(" ======= ======== ======= ========")
                    self.log.info(str(book_start_sentences[0:2]) + " : " + str(book_end_sentences[0: 2]))

                book_start_sentence_idx = [i for i in range(len(book_start_sentences))]
                book_end_sentence_idx = [(len(book_start_sentences) + i) for i in range(len(book_end_sentences))]
                partition_idx = bsp_idx

                # For each token in a sentence remove stop words and get its lemma
                # Add this for each sentence in a list
                for i, s in zip(book_start_sentence_idx, book_start_sentences):
                    word_tokens = [token for token in s]
                    filtered_tokens = [w for w in word_tokens if not w.is_stop]

                    processed_tokens = [t.lemma_ for t in filtered_tokens]                   
                    sentence_dict["s" + str(i)] = processed_tokens
                
                for i, s in zip(book_end_sentence_idx, book_end_sentences):
                    word_tokens = [token for token in s]
                    filtered_tokens = [w for w in word_tokens if not w.is_stop]

                    processed_tokens = [t.lemma_ for t in filtered_tokens]                   
                    sentence_dict["s" + str(i)] = processed_tokens
                print(" ===== ======== =========== ========= =========")
                print("Number of book start sentences {} Number of book end sentences {}: ".format(len(book_start_sentences), len(book_start_sentences)))
                print()
                if(self.logging_flag):
                    self.log.info(" ===== ======== =========== ========= =========")
                    self.log.info("Number of book start sentences {} Number of book end sentences {}: ".format(len(book_start_sentences), len(book_start_sentences)))

        elif (lang == constants.GERMAN):
            processed_tokens = []
            sentence_dict = {}
            doc = self.german_nlp(text)
            sentence_list = [sent for sent in doc.sents]
            book_length = len(sentence_list)
            bsp_idx = int(book_length * book_start_percentage)
            bep_idx = int(book_length - book_length * book_end_percentage)
            sentence_idx = [i for i in range(len(sentence_list))]
            partition_idx = bsp_idx

            if(bsp_idx > 10 and bep_idx > 10):
                book_start_sentences = sentence_list[:bsp_idx]
                book_end_sentences = sentence_list[bep_idx:]
                book_start_sentence_idx = [i for i in range(len(book_start_sentences))]
                book_end_sentence_idx = [(len(book_start_sentences) + i) for i in range(len(book_end_sentences))]

                for i, s in zip(book_start_sentence_idx, book_start_sentences):
                    word_tokens = [token for token in s]
                    filtered_tokens = [w for w in word_tokens if not w.is_stop]

                    processed_tokens = [t.lemma_ for t in filtered_tokens]                   
                    sentence_dict["s" + str(i)] = processed_tokens
                
                for i, s in zip(book_end_sentence_idx, book_end_sentences):
                    word_tokens = [token for token in s]
                    filtered_tokens = [w for w in word_tokens if not w.is_stop]

                    processed_tokens = [t.lemma_ for t in filtered_tokens]                   
                    sentence_dict["s" + str(i)] = processed_tokens
                print(" ===== ======== =========== ========= =========")
                print("Number of book start sentences {} Number of book end sentences {}: ".format(len(book_start_sentences), len(book_start_sentences)))
                print()
                if(self.logging_flag):
                    self.log.info("Number of book start sentences {} Number of book end sentences {}: ".format(len(book_start_sentences), len(book_start_sentences)))
        
        return [sentence_dict, partition_idx];

    
    """
    fnc: get_books_to_sentences_dict
    Input: book text dictionary that has each book pgid as key and complete text of the book as value 
    (Ex: {"pg123": "It is cleared out. The steamer is ready now.", "pg234": "This is a sample text of entire book"

    Output: books to sentence dictionary 
    Ex: {"pg123" : {s1: ["clear", "out"], s2: ["steamer", "ready"]}, "pg234": {s1: ["Justice", "denied"], s2: ["delayed", "already"]}}

    Description: For each book, call  function to pick only required number of filtered and lemmatized tokens. 
    Associate each book with its sentence tokens in a dictionary.
    If book has less than minimum sentences it would return empty dictionary and we just log it and ignore it

    Extension: Adding new language doesn't change this function.
    """
    def get_books_to_sentences_dict(self, books_text_dict): 
        books_sentences_dict = {}      
        for pgid, lst in books_text_dict.items():
            text = lst[0]
            lang = lst[1]
            sentence_dict, partition_idx = self.get_sentencetokens_and_remove_stopwords(text, lang)
            if(sentence_dict):
                print("Sentences extracted for {} having language {}".format(pgid, lang))
                books_sentences_dict[pgid] = [sentence_dict, partition_idx]

                if(self.logging_flag):
                    self.log.info("Sentences extracted for {} having language {}".format(pgid, lang))
            else:
                print("Skipping book {} as it has less than minimum start or end sentences".format(pgid))
                print( "========= ========== ============ ============= ")
                print() 

                if(self.logging_flag):
                    self.log.warning("Skipping book {} as it has less than minimum start or end sentences".format(pgid))    
        return books_sentences_dict;

    """
    fnc: get_emotion_per_sentence
    Input: one single sentence token list
    (Ex: ["clear", "out"])

    Output: Aggregated emotion values across 8 emotions for a single sentence tokens
    Ex: [0.0, 2.0, 1.0, 0.0, 3.0, 0.0, 0.0, 1.0]

    Description: A single sentence token list is its input, each token is looked up in NRC emotion dictionary.
    Emotion of each token is aggregated at sentence level and finally one emotion vector is returned. If no values are fount in
    NRC dictionary then we return default value/

    Problems: We encountered some problems while setting default_emotions to constants.DEFAULT_EMOTIONS we saw lot of errors
    where we got all feature vectors with almost same values, hence similarity > 0.95 for all books.
    This was due to pass by reference. Hence we hardcoded it to the actual value

    Extension: Adding new language doesn't change this function. Although you need to change the emotion dict in other function
    to add emotions of other language
    """

    def get_emotion_per_sentence(self, sen_val, emoticon_dict):
        # Hardcoding this to avoid an issue with similarity calculation, check the problem statement above in comments
        default_emotions = [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]
        sentence_emotions = constants.SENTENCE_EMOTIONS
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

    """
    fnc: run_emotion_analysis
    Input: books_sentences_dict and emotion lookup
    (Ex:  {"pg123" : {s1: ["clear", "out"], s2: ["steamer", "ready"]}, "pg234": {s1: ["Justice", "denied"], s2: ["delayed", "already"]}})

    Output: emotion value of each sentence of each book
    Ex:  {"pg123" : {s1: [0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0], s2: [0.0, 2.0, 1.0, 0.0, 3.0, 0.0, 0.0, 1.0]}, 
    "pg234": {s1: [1.0, 2.0, 1.0, 2.0, 3.0, 0.0, 3.0, 1.0], s2: [1.0, 2.0, 1.0, 1.0, 3.0, 1.0, 0.0, 4.0]}}

    Description: for each books each sentence we will retireve its agregated emotion and store it in a dictionary

    Extension: Adding new language doesn't change this function. Although you need to change the emotion dict in other function
    to add emotions of other language
    """

    def run_emotion_analysis(self, books_sentences_dict, emoticon_dict):
        
        books_emotions_dict = {}
        
        for book_key,lst in books_sentences_dict.items(): 
            sentences = lst[0]
            partition_idx = lst[1]       
            sentence_emotions_dict = {}
            for sen_key, sen_val in sentences.items():
                
                emotion_per_sentence_list = self.get_emotion_per_sentence(sen_val, emoticon_dict)
                sentence_emotions_dict[sen_key] = emotion_per_sentence_list
            
            books_emotions_dict[book_key] = [sentence_emotions_dict, partition_idx]
        
        return books_emotions_dict;


    """
    fnc: create_feature_vector
    Input: books_emotions_dict and start and end percentage
    {"pg123" : {s1: [0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0], s2: [0.0, 2.0, 1.0, 0.0, 3.0, 0.0, 0.0, 1.0], s3: [some values], s4:[some values] ....}, 
    "pg234": {s1: [1.0, 2.0, 1.0, 2.0, 3.0, 0.0, 3.0, 1.0], s2: [1.0, 2.0, 1.0, 1.0, 3.0, 1.0, 0.0, 4.0], s3:[some values], s4:[some values] ....}}

    Output: two aggregated emotion feature vectors one representing book start and another book end for each book
    Ex:  {"pg123" : [[0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0], [0.0, 2.0, 1.0, 0.0, 3.0, 0.0, 0.0, 1.0]], 
    "pg234": [[1.0, 2.0, 1.0, 2.0, 3.0, 0.0, 3.0, 1.0], [1.0, 2.0, 1.0, 1.0, 3.0, 1.0, 0.0, 4.0]]}

    Description: for each book first get the list of book start and book end vectors using partition index.
    Aggregate each emotion of book start and book end vectors separately in each group.
    Normalize this aggregated vectors by dividing them by length of book start and book end respectively.
    Also we handle cases where no sentences of the book has any emotion to remove the zero/zero issue during normalizing


    Extension: Adding new language doesn't change this function. Although book start , end percentages and emperical 
    multiplier values can be tweaked to get better results
    """

    def create_feature_vector(self, books_emotions_dict, book_start_percentage, book_end_percentage):
        books_feature_vectors = {}
        for key, lst in books_emotions_dict.items():
            emo_dict = lst[0]
            partition_idx = lst[1]
            
            book_start_feature_vector = self.book_start_feature_vector_init
            book_end_feature_vector = self.book_end_feature_vector_init
            book_length = len(emo_dict)
            bsp_idx = partition_idx
            bep_idx = partition_idx
            
            print()
            if(bsp_idx > constants.MINIMUM_SENTENCE_LIMIT and bep_idx > constants.MINIMUM_SENTENCE_LIMIT):
                if(self.logging_flag):
                    self.log.info(emo_dict["s0"])
                    self.log.info(emo_dict["s1"])
                    
                for i in range(0,bsp_idx):
                    tmp = emo_dict["s" + str(i)]
                    book_start_feature_vector = [a + b for a, b in zip(book_start_feature_vector, tmp)]

                if(self.logging_flag):
                    
                    self.log.info(emo_dict["s"+str(bep_idx)])
                    self.log.info(emo_dict["s"+str(bep_idx + 1)])

                for i in range(bep_idx, book_length):
                    tmp = emo_dict["s" + str(i)]
                    book_end_feature_vector = [a + b for a, b in zip(book_end_feature_vector, tmp)]

                #Normalizing by length of sentences
                book_start_feature_vector = [x/bsp_idx for x in book_start_feature_vector]
                book_end_feature_vector = [x/ bep_idx for x in book_end_feature_vector]
                
                if(self.logging_flag):
                    self.log.info(" ========= =========== ========== ========== ")
                    self.log.info(book_start_feature_vector)
                    self.log.info(book_end_feature_vector)
                    self.log.info("")

                #Normalizing by emotions to make the sum of emotions sum upto one
                sum_book_start_feature_vector = sum(book_start_feature_vector)
                sum_book_end_feature_vector = sum(book_end_feature_vector)
                
                if(self.logging_flag):
                    self.log.info("sum of vector are {} , {}".format(sum_book_start_feature_vector, sum_book_end_feature_vector))
                # To handle scenarios where none of the sentences in a book has any emotion and everything is zero
                # Hence the sum would be zero this causes zero/zero divide by rror in next step, hence just set it to one to get 0/1 in such cases
                if(sum_book_start_feature_vector == 0):
                    sum_book_start_feature_vector = 1
                
                if(sum_book_end_feature_vector == 0):
                    sum_book_end_feature_vector = 1
                # Scale vectors by empericasl value of 10
                book_start_feature_vector = [x * 10.0/ sum_book_start_feature_vector for x in book_start_feature_vector]
                book_end_feature_vector = [x * 10.0/ sum_book_end_feature_vector for x in book_end_feature_vector]
                
                books_feature_vectors[key] = [book_start_feature_vector, book_end_feature_vector]
                print()
                print("Book: {} || Book start vector: {}, Book end vector: {}".format(key, book_start_feature_vector, book_end_feature_vector))

                if(self.logging_flag):
                    self.log.info("Book: {} || Book start vector: {}, Book end vector: {}".format(key, book_start_feature_vector, book_end_feature_vector))
            else:
                print("Skipping book {} as it has less than 10 start and end sentences".format(key))
                print( "= ==================== ============ ============= ")
                print()
                if(self.logging_flag):
                    self.log.warning("Skipping book {} as it has less than 10 start and end sentences".format(key))
        
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

    """
    fnc: find_book_similarities
    Input: feature vectors ,  book/books to be compared and similarity type
    {"pg123" : [[0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0], [0.0, 2.0, 1.0, 0.0, 3.0, 0.0, 0.0, 1.0]], 
    "pg234": [[1.0, 2.0, 1.0, 2.0, 3.0, 0.0, 3.0, 1.0], [1.0, 2.0, 1.0, 1.0, 3.0, 1.0, 0.0, 4.0]]}

    Output: A single similarity score to compare same books start and end or compare two different books start and end
    Ex:  {"pg123" : [[0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0], [0.0, 2.0, 1.0, 0.0, 3.0, 0.0, 0.0, 1.0]], 
    "pg234": [[1.0, 2.0, 1.0, 2.0, 3.0, 0.0, 3.0, 1.0], [1.0, 2.0, 1.0, 1.0, 3.0, 1.0, 0.0, 4.0]]}

    Description: We can compare between two different books using "L2_between_books" or between same books start and end
    usinf "L2" or "cosine". We choose L2 after some trials as cosine either gives too high or too low similarity score and 
    scores are not in the middle. By using emperical multiplier and L2, we get a good spread of similarity.

    Extension: You can add any other type of similarity calculation in here and call the similarity function(example: L1 similarity)
    """

    def find_book_similarities(self, feature_vector, book1, book2="", type=constants.L2_BETWEEN_BOOKS):
        if type == constants.L2_BETWEEN_BOOKS:
            book1_start = feature_vector[book1][0]; book1_end = feature_vector[book1][1]; 
            book2_start = feature_vector[book2][0]; book2_end = feature_vector[book2][1]; 
            start_similarity = self.find_L2_similarity(book1_start, book2_start)
            end_similarity = self.find_L2_similarity(book1_end, book2_end)
            if(self.logging_flag):
                self.log.info("Start similarity and End similarity between {} and {} are : {} and {}".format(book1, book2, start_similarity, end_similarity))
            #print("Start similarity and End similarity between {} and {} are : {} and {}".format(book1, book2, start_similarity, end_similarity))
            #print()
            #print(" ======== =========== ============= ============ =========== ")
            return [ start_similarity, end_similarity];
        elif type == constants.COSINE:
            book1_start = feature_vector[book1][0]; book1_end = feature_vector[book1][1];
            book_similarity = self.find_Cosine_similarity(book1_start, book1_end)
            if(self.logging_flag):
                self.log.info("Similarity between start and end for the book {} is : {}".format(book1, book_similarity))
            #print("Similarity between start and end for the book {} is : {}".format(book1, book_similarity))
            #print()
            #print(" ======== =========== ============= ============ =========== ")
            return [ book_similarity]
        elif type == constants.L2:
            book1_start = feature_vector[book1][0]; book1_end = feature_vector[book1][1]; 
            book_similarity = self.find_L2_similarity(book1_start, book1_end)
            if(self.logging_flag):
                self.log.info("Similarity between start and end for the book {} is : {}".format(book1, book_similarity))
            #print("Similarity between start and end for the book {} is : {}".format(book1, book_similarity))
            #print()
            #print(" ======== =========== ============= ============ =========== ")
            return [book_similarity];
        else:
            if(self.logging_flag):
                self.log.critical("Can only run for cosine and L2 similarity")
            raise Exception("Can only run for cosine and L2 similarity")
            return [None];


    def run_similarity_for_all_books(self, feature_vector, type = constants.L2_BETWEEN_BOOKS):
        if type == constants.L2_BETWEEN_BOOKS:
            book1 = None; book2 = None; similarity_dict = {}
            permutation_list = list(itertools.permutations(feature_vector.keys(), 2));
            for i in range(len(permutation_list)):
                book1 = permutation_list[i][0]
                book2 = permutation_list[i][1]
                start_sim , end_sim = self.find_book_similarities(feature_vector, book1, book2, type = constants.L2_BETWEEN_BOOKS)
                similarity_dict[book1 + "|" + book2] = [start_sim, end_sim]

            return similarity_dict
        elif type == constants.COSINE:
            book = None; similarity_dict = {};
            for key in feature_vector.keys():            
                similarity= self.find_book_similarities(feature_vector, book1 = key, type = constants.COSINE)
                similarity_dict[key] = similarity
            
            return similarity_dict;
        
        elif type == constants.L2:
            book = None; similarity_dict = {};
            for key in feature_vector.keys():            
                similarity= self.find_book_similarities(feature_vector, book1 = key, type = constants.L2)
                similarity_dict[key] = similarity
            
            return similarity_dict;
            
        else:
            if(self.logging_flag):
                self.log.critical("Can only run for L2 and cosine similarity")
            raise Exception("Can only run for L2 and cosine similarity")
            return {};

    # Create a temporary dataframe and write the feature3 similarity scores. Currently this is not configured for L2 between books
    # and only works for L2 and cosine
    def create_and_save_similarity_dataframe(self, books_similarity_dict, similarity_file_path):
        df = pd.DataFrame.from_dict(books_similarity_dict, orient = constants.INDEX, columns = [self.COLNAME1])
        df = df.sort_values([self.COLNAME1], ascending = (False))
        df[self.COLNAME2] = df.index
        df.index = [x for x in range(len(df.index))]
        df.to_csv( path_or_buf = similarity_file_path, index=False)
        print(df.head(20))
        if(self.logging_flag):
            self.log.info(df.head(20))
        return df;
    
