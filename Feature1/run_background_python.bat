::======================================================================
::CREATED BY : CHANDAN RADHAKRISHNA
::CREATED ON : MAY-2020
::DESCRIPTION: BATCH SCRIPT TO RUN PYTHON JOBS IN BACKGROUND 
::ACTIVATE THE REQUIRED ENVIRONMENT, RUN THE PYTHON PROGRAM BY PASSING RELEVANT PARAMETERS
::PARAMETERS: $1 - ENVIRONMENT_NAME  , $2 - FEATURE_FILE_PATH, $3 - BOOK_FILE_PATH,$4 - LANGUAGE, $5 - $NEW_FEATURE_FILE_PATH
::======================================================================

@echo off

set FEATURE_FILE_PATH=%1
set BOOK_FILE_PATH=%2
set LANGUAGE=%3
set ENCODING=%4
set NEW_FEATURE_FILE_PATH=%5

echo Printing the parameters passed to shell
for %%i in (%*) do echo %%i

echo Running python program driver file
python C:\\Users\\Chandan\\git\\fiction\\Feature1\\feature1_driver.py --feature_file_path %FEATURE_FILE_PATH% --book_file_path %BOOK_FILE_PATH%  --language %LANGUAGE% --encoding %ENCODING% --new_feature_file_path %NEW_FEATURE_FILE_PATH%


echo "Exit Code is" %errorlevel%
exit 0
