import pandas as pd
import numpy as np
import time
from pprint import pprint
import spacy
import gensim
import gensim.corpora as corpora
from gensim.utils import simple_preprocess
import topic_models_constants as constants


class TopicUtils:
    def __init__(self, feature_file_path, book_file_path, stopwords_file_path,logging_flag = False, log = None):
        self.ffp = feature_file_path
        self.bfp = book_file_path
        self.sfp = stopwords_file_path
        self.ff = None
        self.language = None
        self.encoding = None
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
        self.mallet_path = constants.FILEPATH_MALLET_LIB
        self.stopwords = set()
        #update this path
        print(self.mallet_path) 

    
    
    """
    fnc: initilaize_nlp_pipeline
    Input: None
    Output: None....

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
    We extend the stopword list with our own set of stopwords that were extracted from character maps.
    Without this topic models would give character names in topics.
    """

    def extend_stopwords_list(self, misc=[]):
        spacy_english_stopwords = spacy.lang.en.stop_words.STOP_WORDS
        spacy_german_stopwords = spacy.lang.de.stop_words.STOP_WORDS
        miscellaneous_stopwords = misc
        self.stopwords.update(misc)
        self.stopwords.update(spacy_german_stopwords)
        self.stopwords.update(spacy_english_stopwords)
        print('Number of stop words: %d' % len(self.stopwords))
        print('First ten stop words: %s' % list(self.stopwords)[:10])
        return;

    """
    For each book do the following
    a) Check if the book is empty or too small, if yes then discard it
    b) Get appropriate slice of tokens from book start and end according to the percentage
    c) Remove the stopwords from our extended stopwords set ---> remove_stopwords
    d) Estimate the bigram/trigram threshold dynamically using length of tokens ---> get_dynamic_bigram_param
    e) Find bigrams/trigrams ---> make_bigrams
    f) Lemmatize the tokens and filter out all the tokens taht are NOT Nouns and Verbs. Including adverbs/adjectives etc would distort topic models ---> lemmatization
    g) Return book start and end lemmatized tokens
    """
    # add book start and book end functionality here itself

    def get_sentencetokens_and_remove_stopwords(self, text, lang="en", 
                                                book_start_percentage=0.25, book_end_percentage=0.25, 
                                                allowed_postags = ["NOUN", "VERB"]):
        sentence_list = None
        sentence_dict = {}
        partition_idx = None
        book_start_nostop_token_list = []
        book_stop_nostop_token_list = []
        book_start_lemmatized_token_list = []
        book_stop_lemmatized_token_list = []
        
        if (lang == "en"):
            processed_tokens = []
            sentence_dict = {}

            # Find the book start and end indices usng percentages
            doc = self.english_nlp(text)
            nested_sentence_list = [sent for sent in doc.sents]
            sentence_list = [item for sublist in nested_sentence_list for item in sublist]
            book_length = len(sentence_list)
            
            #Set minimum 300 sentences required for topic minimg irrespective of the percentage considered
            #This is assumed that any book las more than 600 sentences, else change in constant file
           
            bsp_idx = int(book_length * book_start_percentage)
            bep_idx = int(book_length - book_length * book_end_percentage)
            
            sentence_idx = [i for i in range(len(sentence_list))]
            print("bsp: {}, bep: {}".format(bsp_idx, bep_idx))
            
            
            # Filter out any books that are under minimum sentence limit
            if(bsp_idx > 10 and bep_idx > 10):
                
                #Select the book start and end sentences according to index
                book_start_sentences = sentence_list[:bsp_idx]
                book_end_sentences = sentence_list[bep_idx:]

                book_start_sentence_idx = [i for i in range(len(book_start_sentences))]
                book_end_sentence_idx = [(len(book_start_sentences) + i) for i in range(len(book_end_sentences))]
                partition_idx = bsp_idx
                
                start = time.clock()
                book_start_data_words = list(self.sent_to_words(book_start_sentences))
                book_end_data_words = list(self.sent_to_words(book_end_sentences))
                elapsed = time.clock()
                elapsed = elapsed - start
                print("Time spent in (sentence to wrds tokenize) is: {}".format(elapsed))
                
                start = time.clock()
                book_start_data_words_for_ngrams = self.remove_stopwords(book_start_data_words)
                book_end_data_words_for_ngrams = self.remove_stopwords(book_end_data_words)
                elapsed = time.clock()
                elapsed = elapsed - start
                print("Time spent in (removing stopwords) is: {}".format(elapsed))
                
                # Remove Stop Words
                book_start_data_words_nostops = book_start_data_words_for_ngrams 
                book_end_data_words_nostops = book_end_data_words_for_ngrams
                
                
                #Set the trheshold for bigrams based on token list length
                book_start_bigram_threshold = self.get_dynamic_bigram_param(book_start_data_words_for_ngrams)
                book_end_bigram_threshold = self.get_dynamic_bigram_param(book_end_data_words_for_ngrams)
                
                #Build bigram and trigram model
                start = time.clock()
                book_start_bigram_mod, book_start_trigram_mod = self.build_bigram_trigram_models(book_start_data_words_for_ngrams, bigram_min_count = 5, bigram_threshold=book_start_bigram_threshold, trigram_min_count = 2, trigram_threshold = 5)
                book_end_bigram_mod, book_end_trigram_mod = self.build_bigram_trigram_models(book_end_data_words_nostops, bigram_min_count = 5, bigram_threshold=book_end_bigram_threshold, trigram_min_count = 2, trigram_threshold = 5)
                elapsed = time.clock()
                elapsed = elapsed - start
                print("Time spent in (bigram trigram) is: {}".format(elapsed))
                
                # Extract Bigrams from model
                book_start_data_words_bigrams = self.make_bigrams(book_start_data_words_for_ngrams, book_start_bigram_mod)
                book_end_data_words_bigrams = self.make_bigrams(book_end_data_words_nostops, book_end_bigram_mod)
                
                # Do lemmatization keeping only noun, adj, vb, adv, ,  "VB", "ADJ", "ADV"
                start = time.clock()
                book_start_data_lemmatized = self.lemmatization(book_start_data_words_bigrams, allowed_postags=['NOUN', "VERB"], nlp=self.english_nlp)
                book_end_data_lemmatized = self.lemmatization(book_end_data_words_bigrams, allowed_postags=['NOUN', "VERB"], nlp=self.english_nlp)
                
                elapsed = time.clock()
                elapsed = elapsed - start
                print("Time spent in (lemmatization) is: {}".format(elapsed))
                #print(len(book_start_data_lemmatized[0]))
                
                
                print(" ===== ======== =========== ========= =========")
                print("Number of book start sentences {} Number of book end sentences {}: ".format(len(book_start_sentences), len(book_start_sentences)))
                
                print("Token lengths of book start and ends with stopwords removed: {}, {}".format(len(book_start_data_words_nostops), len(book_end_data_words_nostops)))
                print("Number of book start lemmatized tokens {} Number of book end lemmatized tokens {}: ".format(len(book_start_data_lemmatized), len(book_end_data_lemmatized)))
                print("Bigrams thresholds used for book start and ends are: {}, {}".format(book_start_bigram_threshold, book_end_bigram_threshold))
                print("Length of book start and end bigrams: {} , {}".format(len(book_start_data_words_bigrams), len(book_end_data_words_bigrams)))
                print()
                if(self.logging_flag):
                    self.log.info(" ===== ======== =========== ========= =========")
                    self.log.info("Number of book start sentences {} Number of book end sentences {}: ".format(len(book_start_sentences), len(book_start_sentences)))
                
                    self.log.info("Token lengths of book start and ends with stopwords removed: {}, {}".format(len(book_start_data_words_nostops), len(book_end_data_words_nostops)))
                    self.log.info("Number of book start lemmatized tokens {} Number of book end lemmatized tokens {}: ".format(len(book_start_data_lemmatized), len(book_end_data_lemmatized)))
                    self.log.info("Bigrams thresholds used for book start and ends are: {}, {}".format(book_start_bigram_threshold, book_end_bigram_threshold))
                    self.log.info("Length of book start and end bigrams: {} , {}".format(len(book_start_data_words_bigrams), len(book_end_data_words_bigrams)))
                
        elif (lang == "de"):
            processed_tokens = []
            sentence_dict = {}

            # Find the book start and end indices usng percentages
            doc = self.german_nlp(text)
            nested_sentence_list = [sent for sent in doc.sents]
            sentence_list = [item for sublist in nested_sentence_list for item in sublist]
            book_length = len(sentence_list)
            
            #Set minimum 300 sentences required for topic minimg irrespective of the percentage considered
            #This is assumed that any book las more than 600 sentences, else change in constant file
           
            bsp_idx = int(book_length * book_start_percentage)
            bep_idx = int(book_length - book_length * book_end_percentage)
            
            sentence_idx = [i for i in range(len(sentence_list))]
            print("bsp: {}, bep: {}".format(bsp_idx, bep_idx))
            
            
            # Filter out any books that are under minimum sentence limit
            if(bsp_idx > 10 and bep_idx > 10):
                
                #Select the book start and end sentences according to index
                book_start_sentences = sentence_list[:bsp_idx]
                book_end_sentences = sentence_list[bep_idx:]

                book_start_sentence_idx = [i for i in range(len(book_start_sentences))]
                book_end_sentence_idx = [(len(book_start_sentences) + i) for i in range(len(book_end_sentences))]
                partition_idx = bsp_idx
                
                start = time.clock()
                book_start_data_words = list(self.sent_to_words(book_start_sentences))
                book_end_data_words = list(self.sent_to_words(book_end_sentences))
                elapsed = time.clock()
                elapsed = elapsed - start
                print("Time spent in (sentence to wrds tokenize) is: {}".format(elapsed))
                
                start = time.clock()
                book_start_data_words_for_ngrams = self.remove_stopwords(book_start_data_words)
                book_end_data_words_for_ngrams = self.remove_stopwords(book_end_data_words)
                elapsed = time.clock()
                elapsed = elapsed - start
                print("Time spent in (removing stopwords) is: {}".format(elapsed))
                
                # Remove Stop Words
                book_start_data_words_nostops = book_start_data_words_for_ngrams 
                book_end_data_words_nostops = book_end_data_words_for_ngrams
                
                
                #Set the trheshold for bigrams based on token list length
                book_start_bigram_threshold = self.get_dynamic_bigram_param(book_start_data_words_for_ngrams)
                book_end_bigram_threshold = self.get_dynamic_bigram_param(book_end_data_words_for_ngrams)
                
                #Build bigram and trigram model
                start = time.clock()
                book_start_bigram_mod, book_start_trigram_mod = self.build_bigram_trigram_models(book_start_data_words_for_ngrams, bigram_min_count = 5, bigram_threshold=book_start_bigram_threshold, trigram_min_count = 2, trigram_threshold = 5)
                book_end_bigram_mod, book_end_trigram_mod = self.build_bigram_trigram_models(book_end_data_words_nostops, bigram_min_count = 5, bigram_threshold=book_end_bigram_threshold, trigram_min_count = 2, trigram_threshold = 5)
                elapsed = time.clock()
                elapsed = elapsed - start
                print("Time spent in (bigram trigram) is: {}".format(elapsed))
                
                # Extract Bigrams from model
                book_start_data_words_bigrams = self.make_bigrams(book_start_data_words_for_ngrams, book_start_bigram_mod)
                book_end_data_words_bigrams = self.make_bigrams(book_end_data_words_nostops, book_end_bigram_mod)
                
                # Do lemmatization keeping only noun, adj, vb, adv, ,  "VB", "ADJ", "ADV"
                start = time.clock()
                book_start_data_lemmatized = self.lemmatization(book_start_data_words_bigrams, allowed_postags=['NOUN', "VERB"], nlp=self.german_nlp)
                book_end_data_lemmatized = self.lemmatization(book_end_data_words_bigrams, allowed_postags=['NOUN', "VERB"], nlp=self.german_nlp)
                
                elapsed = time.clock()
                elapsed = elapsed - start
                print("Time spent in (lemmatization) is: {}".format(elapsed))
                #print(len(book_start_data_lemmatized[0]))
                
                
                print(" ===== ======== =========== ========= =========")
                print("Number of book start sentences {} Number of book end sentences {}: ".format(len(book_start_sentences), len(book_start_sentences)))
                
                print("Token lengths of book start and ends with stopwords removed: {}, {}".format(len(book_start_data_words_nostops), len(book_end_data_words_nostops)))
                print("Number of book start lemmatized tokens {} Number of book end lemmatized tokens {}: ".format(len(book_start_data_lemmatized), len(book_end_data_lemmatized)))
                print("Bigrams thresholds used for book start and ends are: {}, {}".format(book_start_bigram_threshold, book_end_bigram_threshold))
                print("Length of book start and end bigrams: {} , {}".format(len(book_start_data_words_bigrams), len(book_end_data_words_bigrams)))
                print()
                if(self.logging_flag):
                    self.log.info(" ===== ======== =========== ========= =========")
                    self.log.info("Number of book start sentences {} Number of book end sentences {}: ".format(len(book_start_sentences), len(book_start_sentences)))
                
                    self.log.info("Token lengths of book start and ends with stopwords removed: {}, {}".format(len(book_start_data_words_nostops), len(book_end_data_words_nostops)))
                    self.log.info("Number of book start lemmatized tokens {} Number of book end lemmatized tokens {}: ".format(len(book_start_data_lemmatized), len(book_end_data_lemmatized)))
                    self.log.info("Bigrams thresholds used for book start and ends are: {}, {}".format(book_start_bigram_threshold, book_end_bigram_threshold))
                    self.log.info("Length of book start and end bigrams: {} , {}".format(len(book_start_data_words_bigrams), len(book_end_data_words_bigrams)))
        
        return [book_start_data_lemmatized, book_end_data_lemmatized];


    def remove_stopwords(self,texts):   
        return [[word for word in simple_preprocess(str(doc)) if word not in self.stopwords] for doc in texts]


    def make_bigrams(self,texts, bigram_mod):
        return [bigram_mod[doc] for doc in texts]

    def make_trigrams(self,texts, trigram_mod, bigram_mod):
        return [trigram_mod[bigram_mod[doc]] for doc in texts]

    #, 'ADJ', 'VERB', 'ADV'
    def lemmatization(self,texts, nlp , allowed_postags=["NOUN", "VERB"]):
        texts_out = []
        for sent in texts:
            doc = nlp(" ".join(sent)) 
            texts_out.append([token.lemma_ for token in doc if token.pos_ in allowed_postags])
        return texts_out


    def sent_to_words(self, sentences):
        for sentence in sentences:
            yield(gensim.utils.simple_preprocess(str(sentence), deacc=True, min_len=3, max_len=20))  # deacc=True removes punctuations

    # Build the bigram and trigram models
    def build_bigram_trigram_models(self, data_words, bigram_min_count = 5, bigram_threshold=40, trigram_min_count = 2, trigram_threshold = 4):
        bigram = gensim.models.Phrases(data_words, min_count=bigram_min_count, threshold=bigram_threshold) # higher threshold fewer phrases.
        trigram = gensim.models.Phrases(bigram[data_words], threshold=trigram_threshold)  

        # Faster way to get a sentence clubbed as a trigram/bigram
        bigram_mod = gensim.models.phrases.Phraser(bigram)
        trigram_mod = gensim.models.phrases.Phraser(trigram)
        
        return [bigram_mod, trigram_mod]


    def get_dynamic_bigram_param(self, word_list):
        len_word_list = len(word_list)
        bigram_threshold = 10
        
        if(len_word_list > 30000):
            bigram_threshold = 30
        elif(len_word_list <= 30000 and len_word_list > 15000):
            bigram_threshold = 20
        else:
            bigram_threshold = 10
            
        return bigram_threshold;
    
    """
    For every lemmatized list create the corpus and index(id2word) to be used in topic models
    """
    def create_corpus_per_book(self, data_lemmatized):
        # Create Dictionary
        id2word = corpora.Dictionary(data_lemmatized)

        # Create Corpus
        texts = data_lemmatized

        # Term Document Frequency
        corpus = [id2word.doc2bow(text) for text in texts]
        
        return [id2word, texts, corpus]

    """
    Iterate over each books raw text and extract its corpus, index, lemmatized list which would be used in topic models
    """

    def get_corpus_dict( self, books_text_dict): 
        books_sentences_dict = {}
        counter = 0; 
        for pgid, lst in books_text_dict.items():
            text = lst[0]
            lang = lst[1]
            bname = lst[2]

            print("Starting extraction for book number and id: {} and {}".format(counter, pgid))
            if(self.logging_flag):
                self.log.info("Starting extraction for book number and id: {} and {}".format(counter, pgid))

            book_start_lemmatized_token_list, book_stop_lemmatized_token_list = self.get_sentencetokens_and_remove_stopwords(text, lang)
            if(book_start_lemmatized_token_list and book_stop_lemmatized_token_list):
                print("Sentences extracted for {} having language {}".format(pgid, lang))
                if(self.logging_flag):
                    self.log.info("Sentences extracted for {} having language {}".format(pgid, lang))
                
                
                book_start_id2word, book_start_texts, book_start_corpus = self.create_corpus_per_book(book_start_lemmatized_token_list)
                book_stop_id2word, book_stop_texts, book_stop_corpus = self.create_corpus_per_book(book_stop_lemmatized_token_list)
                #print(" Start and stop bigram counts")
                
                books_sentences_dict[pgid] = [bname, book_start_lemmatized_token_list,
                                              book_stop_lemmatized_token_list, book_start_id2word, book_start_texts, book_start_corpus, 
                                              book_stop_id2word, book_stop_texts, book_stop_corpus]
                
            else:
                print("Skipping book {} as it has less than minimum start or end sentences".format(pgid))
                print( "========= ========== ============ ============= ")
                if(self.logging_flag):
                    self.log.info("Skipping bookid {}  and number {} as it has less than minimum start or end sentences".format(pgid, counter))
                print()
            
            counter = counter + 1
        
        return books_sentences_dict;
        
        
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

    """
    For each book we will generate the topics twice separately(book start and end) . We do the following
    a) iterate over each book and extract its indices to variables of corpus/index/lemmatized list
    b) Even after so much cleaning sometimes the bigram counts are too low hence no topics would be formed, this results
        in an error, to resolve this filter again using length of the bigram/trigram index id2word.
    c) Call Mallet for topic modelling with appropriate parameters.
    d) Process the generated topics appropriately and put them in another dict against their book id for later processing.
    """
    
    def generate_topics(self, books_sentences_dict):
        topics_dict = {}
        lst = []
        counter = 0
        for key, val in books_sentences_dict.items():
            lst = []
            bname = val[0]
            book_start_lemmatized_token_list = val[1]
            book_stop_lemmatized_token_list = val[2]
            book_start_id2word = val[3]
            book_start_texts = val[4]
            book_start_corpus = val[5]
            book_stop_id2word = val[6]
            book_stop_texts = val[7]
            book_stop_corpus = val[8]

            print("Generating topics for bookid and book number: {} and {}".format(key, counter))
            if(self.logging_flag):
                self.log.info("Generating topics for bookid and book number: {} and {}".format(key, counter))
            
            """
            Sometimes even after filtering out empty books, some weird books seeps through which has no bigrams/trigrams.
            It results in below error. So we ignore such books using continue and won't process them further
            raise ValueError("cannot compute LDA over an empty collection (no terms)")
            ValueError: cannot compute LDA over an empty collection (no terms)
            """
            if(len(book_stop_id2word) == 0 or len(book_start_id2word) == 0):
                print("Ignoring bookid and number due to emptyness of its id2word bigram trigram: {} and {}".format(key, counter))
                if(self.logging_flag):
                    self.log.info("Ignoring bookid and number due to emptyness of its id2word bigram trigram: {} and {}".format(key, counter))
                continue

            start = time.clock()
            
            """
            We add random seed for reproducability, increase workers to 8 for more faster processing and do 2k iterations
            """
            book_start_ldamallet = gensim.models.wrappers.LdaMallet(self.mallet_path, corpus=book_start_corpus, num_topics= constants.NUM_TOPICS, id2word=book_start_id2word, alpha = constants.ALPHA, iterations = constants.ITERATIONS, random_seed = constants.RANDOM_SEED, workers = constants.WORKERS)
            book_stop_ldamallet = gensim.models.wrappers.LdaMallet(self.mallet_path, corpus=book_stop_corpus, num_topics= constants.NUM_TOPICS, id2word=book_stop_id2word, alpha = constants.ALPHA, iterations = constants.ITERATIONS, random_seed=constants.RANDOM_SEED, workers = constants.WORKERS)
            
            # Show Topics
            pprint(book_start_ldamallet.show_topics(num_topics=1000, formatted=False))
            pprint(book_stop_ldamallet.show_topics(num_topics=1000, formatted=False))

            if(self.logging_flag):
                self.log.info(book_stop_ldamallet.show_topics(num_topics=1000, formatted=False))
                self.log.info(book_stop_ldamallet.show_topics(num_topics=1000, formatted=False))
            
            lst.append(bname)
            lst.append(book_start_ldamallet.show_topics(num_topics=1000, formatted=False))
            lst.append(book_stop_ldamallet.show_topics(num_topics=1000, formatted=False))
            
            for topic in sorted(book_start_ldamallet.show_topics(num_topics=1000, num_words=10, formatted=False), key=lambda x: x[0]):
                print('Topic {}: {}'.format(topic[0], [item[0] for item in topic[1]]))
                lst.append([item[0] for item in topic[1]])
                if(self.logging_flag):
                    self.log.info('Topic {}: {}'.format(topic[0], [item[0] for item in topic[1]]))
            
            for topic in sorted(book_stop_ldamallet.show_topics(num_topics=1000, num_words=10, formatted=False), key=lambda x: x[0]):
                print('Topic {}: {}'.format(topic[0], [item[0] for item in topic[1]]))
                lst.append([item[0] for item in topic[1]])
                if(self.logging_flag):
                    self.log.info('Topic {}: {}'.format(topic[0], [item[0] for item in topic[1]]))
            
            topics_dict[key] = lst

            elapsed = time.clock()
            elapsed = elapsed - start
            print("Time consumed is {}".format(elapsed))

            if(self.logging_flag):
                self.log.info("Time consumed is {}".format(elapsed))

            counter = counter + 1
        
        return topics_dict;

      
    def generate_pandas_dict(self, topics_dict):
        pandas_dict = {}
        for key, val in topics_dict.items():
            #bname = val[0]
            book_start_topic_0 = " ".join(val[3]) 
            book_start_topic_1 = " ".join(val[4]) 
            book_start_topic_2 = " ".join(val[5]) 
            book_start_topic_3 = " ".join(val[6]) 
            book_start_topic_4 = " ".join(val[7]) 
            
            book_end_topic_0 = " ".join(val[8]) 
            book_end_topic_1 = " ".join(val[9]) 
            book_end_topic_2 = " ".join(val[10]) 
            book_end_topic_3 = " ".join(val[11]) 
            book_end_topic_4 = " ".join(val[12]) 
            
            pandas_dict[key] = [book_start_topic_0, book_start_topic_1, book_start_topic_2, book_start_topic_3, 
                                book_start_topic_4, book_end_topic_0, book_end_topic_1, book_end_topic_2,
                                book_end_topic_3, book_end_topic_4]
        
        return pandas_dict;