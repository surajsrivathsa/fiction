########################################################################
#CREATED BY : CHANDAN RADHAKRISHNA
#CREATED ON : MAY-2020
#DESCRIPTION: SHELL SCRIPT TO RUN PYTHON JOBS IN BACKGROUND 
#ACTIVATE THE REQUIRED ENVIRONMENT, RUN THE PYTHON PROGRAM BY PASSING RELEVANT PARAMETERS
#PARAMETERS: $1 - ENVIRONMENT_NAME  , $2 - FEATURE_FILE_PATH, $3 - BOOK_FILE_PATH,$4 - LANGUAGE, $5 - $NEW_FEATURE_FILE_PATH $6 - PYTHON ENVIRONMENT
########################################################################


#!/bin/bash

FEATURE_FILE_PATH=$1;
BOOK_FILE_PATH=$2;
LANGUAGE=$3;
ENCODING=$4;
NEW_FEATURE_FILE_PATH=$5;
MY_ENVIRON=$6
MY_PYTHON_PATH=$7

echo "Printing the parameters passed to shell"
for i in $*; do 
   echo $i 
done


echo ""
echo "Activating python environment: $MY_ENVIRON"
source activate $MY_ENVIRON

echo "Running python program driver file"
python $MY_PYTHON_PATH --feature_file_path $FEATURE_FILE_PATH --book_file_path $BOOK_FILE_PATH --emoticon_file_path  --encoding $ENCODING --new_feature_file_path $NEW_FEATURE_FILE_PATH

status=$?

if [ $status -ne 0 ]; then
	echo "Error while running python job, exiting shell"
	exit 1;
else
	echo "Python program successful, exiting shell"
	exit 0;

fi

