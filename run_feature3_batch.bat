  
::======================================================================
::CREATED BY : CHANDAN RADHAKRISHNA
::CREATED ON : MAY-2020
::DESCRIPTION: BATCH SCRIPT TO RUN PYTHON JOBS IN BACKGROUND 
::ACTIVATE THE REQUIRED ENVIRONMENT, RUN THE PYTHON PROGRAM BY PASSING RELEVANT PARAMETERS
::PARAMETERS: $1 - ENVIRONMENT_NAME  , $2 - FEATURE_FILE_PATH, $3 - BOOK_FILE_PATH, $4 - EMOTICON_FILE_PATH,
::$5 - FEATURE_FIELDS, $6 - LANGUAGE, $7 - BSP, $8 - BEP, $9 - SIMILARITY_TYPE, $10 - $NEW_FEATURE_FILE_PATH
::======================================================================

@echo off

set ENVIRONMENT_NAME=%1
set FEATURE_FILE_PATH=%2
set BOOK_FILE_PATH=%3
set EMOTICON_FILE_PATH=%4
set FEATURE_FIELDS=%5
set LANGUAGE=%6
set ENCODING=%7
set BSP=%8
set BEP=%9
set SIMILARITY_TYPE=%10
set NEW_FEATURE_FILE_PATH=%11
set BOOK_LIST_FILE_PATH=%12
set LOGGING_FLAG=%13
set PYTHON_CODE_FILE_PATH=%14

echo Printing the parameters passed to shell
for %%i in (%*) do echo %%i

echo Activate virtual environment of python
.\%ENVIRONMENT_NAME%\Scripts\activate

echo Running python program driver file
python %PYTHON_CODE_FILE_PATH% --feature_file_path %FEATURE_FILE_PATH% --book_file_path %BOOK_FILE_PATH%  --language %LANGUAGE% --encoding %ENCODING% --new_feature_file_path %NEW_FEATURE_FILE_PATH% --book_list_file_path %BOOK_LIST_FILE_PATH% --logging_flag %LOGGING_FLAG%


echo "Exit Code is" %errorlevel%
exit 0