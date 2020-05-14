import os
import shutil
import pandas as pd

"""
parameters are 
1: book list excel file path
2: book list excel sheet name
assumption: expected that book list ids have a column by name bid

3: base folder of the gutenberg extract
4: destintion folder where epubs should be copied
"""
booklist_excel_file_path = "/Users/surajshashidhar/Desktop/ovgu/semester_2/XAI_project/reasearched_code_and_data/gutenberg_extract_copy_experiments/Final_Booklist.xlsx"
booklist_excel_sheet_name = "Final_Booklist"
gutenberg_src_base = "/Users/surajshashidhar/Desktop/ovgu/semester_2/XAI_project/reasearched_code_and_data/gutenberg_extract_copy_experiments/gutenberg_extracts/"
gutenberg_epub_dst = "/Users/surajshashidhar/Desktop/ovgu/semester_2/XAI_project/reasearched_code_and_data/gutenberg_extract_copy_experiments/gutenberg_final_epubs/"


df = pd.read_excel(io=booklist_excel_file_path, sheet_name=booklist_excel_sheet_name,header=0)
print(df.head(10))

#convert book data frame to dictionary so that we can loop over
book_dict = df.to_dict(orient="list")
print(book_dict["bid"][1:5])

#initialize list to hold book ids that doesnt exist in src folder
epub_not_exists = []

#loop over each book id from excel
for id in book_dict["bid"]:

    src_final_dir = gutenberg_src_base + str(id)
    
    #check if directory exists
    if(os.path.isdir(src_final_dir)):

        for filename in os.listdir(src_final_dir):
            #copy only epub files
            if (filename.endswith(".epub")):
                shutil.copy(os.path.join(src_final_dir, filename), gutenberg_epub_dst)
                print()
                print("file {} was copied successfully".format(filename))
                print(" ===== ====== ======= ======= ===== ====== =====")

    else:
        #append bookids that are not present in src folder
        epub_not_exists.append(id)

print()
print("Book pd {} doesn't exist".format(epub_not_exists))
print(" ===== ====== ======= ======= ===== ====== =====")
