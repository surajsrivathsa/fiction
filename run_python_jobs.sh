########################################################################
#CREATED BY : SURAJ B S
#CREATED ON : MAY-2020
#DESCRIPTION: SHELL SCRIPT TO RUN PYTHON JOBS IN BACKGROUND 
#ACTIVATE THE REQUIRED ENVIRONMENT, RUN THE PYTHON PROGRAM BY PASSING RELEVANT PARAMETERS
#PARAMETERS: $1 - ENVIRONMENT_NAME  , $2 - FEATURE_FILE_PATH, $3 - BOOK_FILE_PATH, $4 - EMOTICON_FILE_PATH,
# $5 - FEATURE_FIELDS, $6 - LANGUAGE, $7 - BSP, $8 - BEP, $9 - SIMILARITY_TYPE, $10 - $NEW_FEATURE_FILE_PATH
#
########################################################################


#!/bin/bash
 

ENVIRONMENT_NAME=${1};
FEATURE_FILE_PATH=$2;
BOOK_FILE_PATH=$3;
EMOTICON_FILE_PATH=$4
FEATURE_FIELDS=$5
LANGUAGE=$6
ENCODING=$7
BSP=$8
BEP=$9
SIMILARITY_TYPE="${10}"
NEW_FEATURE_FILE_PATH="${11}"


echo "Printing the parameters passed to shell"
for i in $*; do 
   echo $i 
done

#activating homes bash profile
#if not activated then we cannot find conda path, hence required environment cannot be activated
source $HOME/.bash_profile

echo ""
echo "Activating python environment: $ENVIRONMENT_NAME"
source activate $ENVIRONMENT_NAME

echo "Running python program driver file"
python /Users/surajshashidhar/git/fiction/extract_emotion_features/extract_emotions_driver.py --feature_file_path $FEATURE_FILE_PATH --book_file_path $BOOK_FILE_PATH --emoticon_file_path $BOOK_FILE_PATH --emoticon_file_path $EMOTICON_FILE_PATH --feature_fields $FEATURE_FIELDS --language $LANGUAGE --encoding $ENCODING --book_start_percentage $BSP --book_end_percentage $BEP --similarity_type $SIMILARITY_TYPE --new_feature_file_path $NEW_FEATURE_FILE_PATH

status=$?

if [ $status -ne 0 ]; then
	echo "Error while running python job, exiting shell"
	exit 1;
else
	echo "Python program successful, exiting shell"
	exit 0;

fi

