App for scanning cards in Star wars unlimited.
Will create a CSV file at src/main/resources/output_collection.csv that can be uploaded to swudb.com

Install:

* Java - https://learn.microsoft.com/en-us/java/openjdk/download#openjdk-21
* Maven - https://phoenixnap.com/kb/install-maven-windows
* Tesseract-OCR - https://github.com/UB-Mannheim/tesseract/wiki
    Make sure to install to "C:/Program Files/Tesseract-OCR" (or update the code)

Using a smartphone as webcam makes it easier to use:
* Android: https://droidcam.app/
* Ios https://reincubate.com/camo/downloads/ (not tested)

Note: only tested on windows

If webcam not working try changing WEBCAM_ID in main to something else
(0 = first cam, 1 = second cam etc)

Build: mvn clean install
Run: mvn exec:java

----------------
How to get new collection of cards:
* Go to swudb.com (account with empty collection)
* Create a file like twi_full.csv with number from 001 - 600
* Import bulk in swudb
* Download human readable collection