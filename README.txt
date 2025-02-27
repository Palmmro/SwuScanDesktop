App for scanning cards in Star wars unlimited.
Will create a CSV file at output_collection.csv that can be uploaded to swudb.com

Install:
To run
* Java - https://learn.microsoft.com/en-us/java/openjdk/download#openjdk-21
* Maven - https://phoenixnap.com/kb/install-maven-windows

To develop:
* Python
* pip install requests
* pip install pillow

Using a smartphone as webcam makes it easier to use:
* Android: https://droidcam.app/
* Ios https://reincubate.com/camo/downloads/

Note: only tested on windows

If webcam not working try changing WEBCAM_ID in config to something else
(0 = first cam, 1 = second cam etc)

----------------
How add a new set
* cd scripts
* Change "SET" in and run generate_swudb_input.py
* Go to swudb.com (account with empty collection)
* Take src/main/resources/<set>_full.csv file and import bulk in swudb.com
* Download human readable collection and name it "human_readable_full_collection_<set>.csv"
* Change "SET" in and run download_images.py

============================
Build: mvn clean install
Run: mvn exec:java
============================