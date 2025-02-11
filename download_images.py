import requests
import pandas as pd
import time
from pathlib import Path
from PIL import Image
import os

SET="SOR"

URL="https://swudb.com/images/cards/"+SET+"/"
CSV="src/main/resources/human_readable_full_collection_"+SET.lower()+".csv"
# CSV="human_readable_test.csv"
DELAY=0.9

def get_image():
    df = pd.read_csv(CSV)
    for index, row in df.iterrows():
        if row["VariantType"] != "Normal":
            break
        print(row["CardName"], row["CardNumber"])
        number = str(row["CardNumber"])
        if len(number) == 1:
            number = "00"+number
        elif len(number) == 2:
            number = "0"+number
        url = URL + number +".png"
        set_string = URL[-4:]
        # set_string = "TEST/"
        path = "src/main/resources/images/"+ set_string
        name = row["CardName"].replace(" ","_")
        savepath = "src/main/resources/images/"+ set_string + row["CardName"].replace(" ","_")
        Path("src/main/resources/images/"+ set_string).mkdir(parents=True, exist_ok=True)
        # filenr = 1

        matching_files = get_nr_matching_files(name, path)
        if len(matching_files) > 0 :
            savepath = savepath + "_(Unit)"
            # rename old file to (Leader)
            old_name_start = matching_files[0][:-8]
            old_name_end = matching_files[0][-8:]
            new_name = old_name_start + "_(Leader)" + old_name_end
            os.rename(path+matching_files[0], path+new_name)

        # while Path(savepath+".jpg").is_file():
        #     filenr = filenr + 1
        #     savepath = savepath + "("+str(filenr)+")"
        #     if filenr > 10:
        #         print("Too many similar files")
        #         break

        savepath = savepath + "_" + number

        download_image(url, savepath+".jpg")
        time.sleep(DELAY)

def get_nr_matching_files(search_term, path):
    matches = 0
    result = []
    for root, dirs, files in os.walk(path):
        for name in files:
            if name[:-8] == search_term:
                result.append(name)
    return result

def resize_image(save_path):
    max_width = 359
    max_height = 500
    quality = 85
    with Image.open(save_path) as img:
        img.thumbnail((max_width, max_height))  # Resize while maintaining aspect ratio
        # Convert RGBA (with transparency) to RGB to save as JPEG
        if img.mode == "RGBA":
            img = img.convert("RGB")

        img.save(save_path, "JPEG", optimize=True, quality=quality)  # Save with compression


def download_image(image_url, save_path):
    try:
        response = requests.get(image_url, stream=True)
        response.raise_for_status()  # Raise an error for bad responses (4xx and 5xx)

        with open(save_path, 'wb') as file:
            for chunk in response.iter_content(1024):
                file.write(chunk)

        print(f"Image downloaded successfully: {save_path}")
        resize_image(save_path)


    except requests.exceptions.RequestException as e:
        print(f"Error: {e}")


if __name__ == "__main__":
    get_image()