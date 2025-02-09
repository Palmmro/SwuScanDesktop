import requests
import pandas as pd
import time
from pathlib import Path
from PIL import Image

SET="TWI"

URL="https://swudb.com/images/cards/"+SET+"/"
CSV="src/main/resources/human_readable_full_collection_"+SET.lower()+".csv"
# CSV="human_readable_test.csv"
DELAY=0.8

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
        savepath = "src/main/resources/images/"+ set_string + row["CardName"].replace(" ","_") + "_" + number
        Path("src/main/resources/images/"+ set_string).mkdir(parents=True, exist_ok=True)
        # filenr = 1
        # while Path(savepath+".jpg").is_file():
        #     filenr = filenr + 1
        #     savepath = savepath + "("+str(filenr)+")"
        #     if filenr > 10:
        #         print("Too many similar files")
        #         break

        download_image(url, savepath+".jpg")
        time.sleep(DELAY)

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