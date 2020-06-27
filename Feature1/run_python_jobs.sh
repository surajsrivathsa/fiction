########################################################################
#CREATED BY : CHANDAN RADHAKRISHNA
#CREATED ON : MAY-2020
#DESCRIPTION: SHELL SCRIPT TO RUN PYTHON JOBS IN BACKGROUND 
#ACTIVATE THE REQUIRED ENVIRONMENT, RUN THE PYTHON PROGRAM BY PASSING RELEVANT PARAMETERS
#PARAMETERS: PARAMETERS: $1 - ENVIRONMENT_NAME  , $2 - FEATURE_FILE_PATH, $3 - BOOK_FILE_PATH,$4 - LANGUAGE, $5 - $NEW_FEATURE_FILE_PATH
########################################################################


#!/bin/bash

FEATURE_FILE_PATH=$1;
BOOK_FILE_PATH=$2;
LANGUAGE=$3;
ENCODING=$4;
NEW_FEATURE_FILE_PATH=$5;


echo "Printing the parameters passed to shell"
for i in $*; do 
   echo $i 
done


echo ""
echo "Activating python environment: $ENVIRONMENT_NAME"
source activate $ENVIRONMENT_NAME

echo "Running python program driver file"
python /Users/Chandan/git/fiction/Feature1/feature1_driver.py --feature_file_path $FEATURE_FILE_PATH --book_file_path $BOOK_FILE_PATH --emoticon_file_path  --encoding $ENCODING --new_feature_file_path $NEW_FEATURE_FILE_PATH

status=$?

if [ $status -ne 0 ]; then
	echo "Error while running python job, exiting shell"
	exit 1;
else
	echo "Python program successful, exiting shell"
	exit 0;

fi

