import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import nu.pattern.OpenCV;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.highgui.HighGui;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.imageio.ImageIO;

public class Main {
    private static final int ZERO = 96;
    private static final int ONE = 97;
    private static final int TWO = 98;
    private static final int THREE = 99;
    private static final int FOUR = 100;

    static {
        // Load the OpenCV native library
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        OpenCV.loadLocally();
    }

    public static void main(String[] args) throws InterruptedException {
        // Open a connection to the webcam (usually device 0)
        VideoCapture capture = new VideoCapture(1);

        // Set the resolution to 1920x1080 (or whatever the webcam supports)
        capture.set(3, 1920); // Set width
        capture.set(4, 1080); // Set height

        if (!capture.isOpened()) {
            System.out.println("Error: Could not open video capture.");
            return;
        }

        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath("C:/Program Files/Tesseract-OCR/tessdata");
        tesseract.setTessVariable("user_defined_dpi", "96");

        Mat frame = new Mat();
        String displayText = "Scanning";
        String cardName = "";

        while (true) {
            // Read a frame from the webcam
            if (capture.read(frame)) {
                // Perform OCR every second
                // Add the display text to the frame with a black box behind it
                addTextToFrame(frame, displayText);

                // Display the frame using HighGui
                HighGui.imshow("Webcam", frame);



                if (HighGui.waitKey(1000) == 'q') {
                    break;
                } else {
                    String recognizedText = performOCR(frame, tesseract);
                    if (!recognizedText.isEmpty()) {
//                        System.out.println("Recognized Text: " + recognizedText);
                        if (recognizedText.toLowerCase().contains("Incinerator Trooper".toLowerCase())) {
                            displayText = "Incinerator Trooper? 1/2/3/4 to add.";
                            cardName = "Incinerator Trooper";

                        }
                    }
                }

                var key = HighGui.pressedKey;
                if(key == ONE && cardName.length() != 0){
                    System.out.println("Added regular "+cardName+" to csv");
                    cardName = "";
                    displayText = "Scanning";
                }
                if(key == TWO && cardName.length() != 0){
                    System.out.println("Added hyperspace "+cardName+" to csv");
                    cardName = "";
                    displayText = "Scanning";
                }
                if(key == THREE && cardName.length() != 0){
                    System.out.println("Added foil "+cardName+" to csv");
                    cardName = "";
                    displayText = "Scanning";
                }
                if(key == FOUR && cardName.length() != 0){
                    System.out.println("Added hyperspace foil "+cardName+" to csv");
                    cardName = "";
                    displayText = "Scanning";
                }if(key == ZERO && cardName.length() != 0){
                    System.out.println("Reset");
                    cardName = "";
                    displayText = "Scanning";
                }


            } else {
                System.out.println("Error: Could not read frame.");
                break;
            }
        }

        // Release the capture and close all HighGui windows
        capture.release();
        HighGui.destroyAllWindows();
    }

    private static String performOCR(Mat frame, ITesseract tesseract) {
        try {
            // Convert Mat to byte array
            MatOfByte matOfByte = new MatOfByte();
            Imgcodecs.imencode(".jpg", frame, matOfByte);
            byte[] byteArray = matOfByte.toArray();

            // Convert byte array to BufferedImage
            InputStream in = new ByteArrayInputStream(byteArray);
            BufferedImage bufferedImage = ImageIO.read(in);

            // Perform OCR on the BufferedImage
            return tesseract.doOCR(bufferedImage);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static void addTextToFrame(Mat frame, String text) {
        int fontFace = 0;
        double fontScale = 1.0;
        int thickness = 2;
        Point textOrg = new Point(10, frame.rows() - 10);

        // Calculate the text size to create a black box
        int baseline = 0;
        int[] baseLine = {baseline};
        Size textSize = Imgproc.getTextSize(text, fontFace, fontScale, thickness, baseLine);

        // Draw the black rectangle
        Imgproc.rectangle(frame, new Point(textOrg.x, textOrg.y - textSize.height),
                new Point(textOrg.x + textSize.width, textOrg.y + baseLine[0]),
                new Scalar(0, 0, 0), -1);

        // Put the text over the rectangle
        Imgproc.putText(frame, text, textOrg, fontFace, fontScale, new Scalar(255, 255, 255), thickness);
    }
}