Install:

* Java - https://learn.microsoft.com/en-us/java/openjdk/download#openjdk-21
* Maven - https://phoenixnap.com/kb/install-maven-windows
* Tesseract-OCR - https://github.com/UB-Mannheim/tesseract/wiki
    Make sure to install to "C:/Program Files/Tesseract-OCR" (or update the code)

If webcam not working try changing WEBCAM_ID in main to something else
(0 = first cam, 1 = second cam etc)

Build: mvn clean install
Run: mvn exec:java