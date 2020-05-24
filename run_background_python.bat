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
set MASTER_FILE_PATH=%3
set ENCODING=%4
set NEW_FEATURE_FILE_PATH=%5
set MY_ENVIRON=%6
set MY_PYTHON_PATH=%7



echo Printing the parameters passed to CMD
for %%i in (%*) do echo %%i

echo Activating the environment
call activate %MY_ENVIRON%

echo Running python program driver file
python %MY_PYTHON_PATH% --feature_file_path %FEATURE_FILE_PATH% --book_file_path %BOOK_FILE_PATH%  --master_file_path %MASTER_FILE_PATH% --encoding %ENCODING% --new_feature_file_path %NEW_FEATURE_FILE_PATH%

if %ERRORLEVEL% EQU 0 ( 
exit %ERRORLEVEL%) else ( 
exit %ERRORLEVEL%)





