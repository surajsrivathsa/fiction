from gensim.models import Word2Vec
from sklearn.decomposition import PCA
import itertools
import pandas as pd
from nltk.tokenize import word_tokenize
import feature1_file_utils
import warnings
warnings.filterwarnings("ignore")

class GenreUtils:
    def __init__(self, book_file_path,chunk_size, master_file_path, encoding='utf-8'):
        self.bfp = book_file_path
        self.master_file_path = master_file_path
        self.encoding = encoding
        self.window_size = 5
        self.vector_size = 100
        self.num_epoch = 100
        self.chunk_size  = chunk_size
        self.start_feature = 24
        self.feature_size = chunk_size * 2

    def split_chunks(self, books, size, name):
    #Splitting the book into chunks
        try:
            book={}
            cuts1 = len(books[0])/size
            cuts1 = round(cuts1)
            cuts = round(cuts1)
            begin = 0
            for i in range(size):
                book[name + "-" + str(i)] = []
                book[name + "-" + str(i)].append(books[0][begin:cuts])
                begin = cuts
                cuts = cuts1 * (i+2)
            return book
        except:
            print("Unable to chunk")
            sys.exit(1)
    

    
    def gen_word2vec(self, info):
    #Function to create Word2Vec vectors
        try:
            model = Word2Vec(window = self.window_size,size = self.vector_size,sg=1,min_count=1)
            model.build_vocab(info)
            model.train(info, total_examples = model.corpus_count, epochs=self.num_epoch)
            return model
        except:
            print("Unable to generate word vectors")
            sys.exit(1)

    
    def chunk_vector_gen(self, words_books, sentence_books):
    #Generate chunk vectors for all the books
        try:
            book_vecs = {}
            files = words_books.keys()
            new_chunk_vecs = pd.DataFrame()
            print(files)
            for file_name in words_books:
                book_vecs[file_name] = []
                books_list = list(itertools.chain(*words_books[file_name]))
                book = self.split_chunks(books_list,self.chunk_size,file_name)
                model = self.gen_word2vec(sentence_books[file_name])
                word_vec_tog = pd.DataFrame(data = model[model.wv.vocab], index = model.wv.vocab)
                all_chunk_vecs = pd.DataFrame()
                for chunk in book.keys():
                    tokens = word_tokenize(book[chunk][0])
                    all_words_df = pd.DataFrame(tokens)
                    all_words_df.columns=["Names"]
                    all_words_df.index = all_words_df.Names
                    merged_df = pd.merge(all_words_df,word_vec_tog,how='inner',left_index=True,right_index=True)
                    merged_df = merged_df.drop("Names",axis = 1)
                    merged_df = merged_df.drop_duplicates(keep='first')
                    merged_data = merged_df.sum(axis = 0)/len(merged_df.index)
                    merged_data = pd.DataFrame(merged_data).T
                    merged_data.index = [chunk]
                    all_chunk_vecs = all_chunk_vecs.append(merged_data)
                new_chunk_vecs = new_chunk_vecs.append((all_chunk_vecs.sum(axis = 0)/5).T,ignore_index=True)
            pca = PCA(n_components=10)
            result = pca.fit_transform(new_chunk_vecs)
            result = pd.DataFrame(result)
            result.index = files 
            f_names = self.get_feature_names()
            print(f_names)
            result.columns = f_names
            print(result)
            return  result
        except:
            print("Unable to call the vector generating function")
            sys.exit(1)

    def get_feature_names(self):
    #Name the new feature columns
        try:
            f_vals = []
            for nums in range(self.start_feature,self.start_feature+self.feature_size,1):
                f_vals.append('F'+ str(nums))
            return f_vals
        except:
            print("Unable to generate feature names")
            sys.exit(1)

