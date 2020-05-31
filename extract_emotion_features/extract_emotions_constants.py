import logging
import os
import sys
import pandas as pd
import time

# File path and name constants
FILE_PATH_SIMILARITY = os.path.join(os.getcwd(), "simialrity_feature_1emp_20pcnt.csv")
FILE_PATH_LOG = os.path.join(os.getcwd(), 'SIMFIC_2.0_Feature3.log')
FILE_MODE = "w"
FILE_PATH_FEATURES = os.path.join(os.getcwd(), "feature3_vectors_1emp_20pcnt.csv")

# To change logging level(WARNING, ERROR, CRITICAL) to other than INFO use other integer values mentioned in https://www.loggly.com/ultimate-guide/python-logging-basics/
LOGGING_LEVEL = 20
HTML_FILE_EXTENSION = ".html"
SHEETNAME="Final_Booklist"
EMPTY = ""

# NLP Pipeline constants
ENGLISH = "en"
GERMAN = "de"
DISABLE_LIST = ["ner", "parser"]
DOC_MAX_LENGTH = 10000000
SENTENCE_TOKENIZER = "sentencizer"

#Regular expressions
FILENAME_PARSE_REGEX = "(pg)([0-9]*).*?-content(.html)"
HTML_TAGS_REGEX = "<.*?>"

# Emotion Names.
ANGER = "anger"
ANTICIPATION = "anticipation"
DISGUST = "disgust"
FEAR = "fear"
JOY = "joy"
SADNESS = "sadness"
SURPRISE = "surprise"
TRUST = "trust"

# Default feature vectors , similarity types and end file
DEFAULT_BOOK_START_PERCENTAGE = 0.2
DEFAULT_BOOK_END_PERCENTAGE = 0.2
EMPERICAL_MULTIPLIER = 1
MINIMUM_SENTENCE_LIMIT = 10
DEFAULT_EMOTIONS = [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]
SENTENCE_EMOTIONS = [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]
L2_BETWEEN_BOOKS = "L2_between_books"
COSINE = "cosine"
L2 = "L2"
INDEX = "index"
DASH = "-"
S = "s"
BOOKID_CHUNKNO = "bookId-chunkNo"
BOOK_ID = "book_id"
CHUNK_ID = "chunk_id"
ZERO = 0
ONE = 1

FEATURE_FIELD = 24
JOIN_TYPE = "inner"
SIMILARITY = "similarity"
BID = "bid"
BLANG = "blang"
BNAME = "bname"
PG = "pg"
test = "TEST"
FEATURE_VECTOR_COLS = ["start_anger", "start_anticipation", "start_disgust", "start_fear", 
        "start_joy", "start_sadness", "start_surprise", "start_trust",
       "end_anger", "end_anticipation", "end_disgust", "end_fear", "end_joy", 
        "end_sadness", "end_surprise", "end_trust", "bname"]

# logging testing

"""
logging.basicConfig(filename=os.path.join(os.getcwd(), 'SIMFIC_2.0_Feature3.log'), level = logging.INFO, filemode='w', format='%(asctime)s - s%(name)s - %(levelname)s - %(message)s')

log = logging.getLogger()
log.warning('This warning get logged to a file')
log.info("This info will be logged")
log.debug("This debug won't be logged")
log.critical(" Logged through log object")
log.info("Number of book start sentences {} Number of book end sentences {}: ".format(10, len(FEATURE_FIELD)))
"""