import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.ImageWindow;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.highgui.HighGui;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class Main {
    private static final int WEBCAM_ID = 1;

    private static final int ZERO = 96;
    private static final int ONE = 97;
    private static final int TWO = 98;
    private static final int THREE = 99;
    private static final int FOUR = 100;
    private static final int FIVE = 101;
    private static final int S = 83;

    private static final String SCANNING_TEXT = "Scanning...";
    private static final String OPTIONS_TEXT = "? \n1(N)/2(H)/3(F)/4(HF) to add. \n0 to reset. 5 to swap unit/leader.";

    private static final List<Card> currentCards = new ArrayList<>();

    private static final CollectionUtil collectionUtil = new CollectionUtil();


    static {
        // Load the OpenCV native library
        OpenCV.loadLocally();
    }

    public static void main(String[] args) throws InterruptedException {
        // Open a connection to the webcam (usually device 0)
        VideoCapture capture = new VideoCapture(WEBCAM_ID);

        if (!capture.isOpened()) {
            System.out.println("Error: Could not open video capture.");
            return;
        }

        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath("C:/Program Files/Tesseract-OCR/tessdata");
        tesseract.setTessVariable("user_defined_dpi", "70");

        Mat frame = new Mat();
        String displayText = SCANNING_TEXT;
        Card foundCard = null;

        boolean shouldRun = true;

        System.out.println("Started SWU scan");
        while (shouldRun) {
            // Read a frame from the webcam
            if (capture.read(frame)) {
                // Perform OCR every second
                // Add the display text to the frame with a black box behind it
                addTextToFrame(frame, displayText);

                // Display the frame using HighGui
                HighGui.imshow("Webcam", frame);


                if (HighGui.waitKey(500) == 'q') {
                    break;
                } else {
                    if(displayText.equals(SCANNING_TEXT)){
                        String recognizedText = performOCR(frame, tesseract);
                        if (!recognizedText.isEmpty()) {
                            List<Card> collection = collectionUtil.getCollectionCards();
                                foundCard = TextMatcher.findCard(recognizedText,collection);
                                if(foundCard != null){
                                    displayText = foundCard.getUniqueDisplayName()+OPTIONS_TEXT;
                                }
                        }
                    }
                }

                var key = HighGui.pressedKey;
                if(key == ONE && foundCard != null){
                    System.out.println("Added regular "+foundCard.getUniqueDisplayName()+" to csv");
                    saveCard(foundCard, false);
                    foundCard = null;
                    displayText = SCANNING_TEXT;
                }
                if(key == TWO && foundCard != null){
                    if(saveHyperspaceCard(foundCard, false)){
                        System.out.println("Added hyperspace "+foundCard.getUniqueDisplayName()+" to csv");
                        foundCard = null;
                        displayText = SCANNING_TEXT;
                    }
                }
                if(key == THREE && foundCard != null){
                    System.out.println("Added foil "+foundCard.getUniqueDisplayName()+" to csv");
                    saveCard(foundCard, true);
                    foundCard = null;
                    displayText = SCANNING_TEXT;
                }
                if(key == FOUR && foundCard != null){
                    if(saveHyperspaceCard(foundCard, true)) {
                        saveCard(foundCard, false);
                        System.out.println("Added hyperspace foil " + foundCard.getUniqueDisplayName() + " to csv");
                        foundCard = null;
                        displayText = SCANNING_TEXT;
                    }
                }
                if(key == FIVE){
                    if(foundCard != null && !foundCard.getCardName().equals(foundCard.getUniqueDisplayName())){

                        //find other card
                        boolean isExistingLeader = foundCard.getUniqueDisplayName().contains("(Leader)");
                        if(isExistingLeader){
                            foundCard = collectionUtil.getCardFromName(foundCard.getCardName() + " (Unit)");
                        } else {
                            foundCard = collectionUtil.getCardFromName(foundCard.getCardName() + " (Leader)");
                        }
                        if(foundCard != null){
                            displayText = foundCard.getUniqueDisplayName()+OPTIONS_TEXT;
                        }
                    }
                }
                if(key == ZERO){
                    System.out.println("Reset");
                    foundCard = null;
                    displayText = SCANNING_TEXT;
                }

            } else {
                System.out.println("Error: Could not read frame.");
                break;
            }
            shouldRun = ((ImageWindow) HighGui.windows.values().toArray()[0]).frame.isVisible();

        }

        // Release the capture and close all HighGui windows
        capture.release();
        HighGui.destroyAllWindows();
        System.exit(0);
    }



    private static void saveCard(Card card, boolean isFoil){
        currentCards.add(new Card(card.getSet(),card.getCardName(),card.getCardNumber(),1,isFoil));
        collectionUtil.saveToCsv(currentCards);

    }
    private static boolean saveHyperspaceCard(Card card, boolean isFoil){
        Card hyperspaceCard = collectionUtil.getHyperspaceCardFromName(card.getCardName());
        if(hyperspaceCard == null){
            System.out.println("No hyperspace available for card");
            return false;
        }
        currentCards.add(new Card(hyperspaceCard.getSet(),hyperspaceCard.getCardName(),hyperspaceCard.getCardNumber(),1,isFoil));
        collectionUtil.saveToCsv(currentCards);
        return true;

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
        int[] baseLine = new int[1];
        Point textOrg = new Point(10, frame.rows() - 50);

        // Split the text into lines
        String[] lines = text.split("\n");

        // Calculate the text size for each line and find the maximum width
        double maxWidth = 0;
        int totalHeight = 0;
        int lineSpacing = 30; // Additional space between lines

        for (String line : lines) {
            Size textSize = Imgproc.getTextSize(line, fontFace, fontScale, thickness, baseLine);
            maxWidth = Math.max(maxWidth, textSize.width);
            totalHeight += textSize.height + lineSpacing; // Adding space between lines
        }

        // Draw the black rectangle for all lines
        Imgproc.rectangle(frame, new Point(textOrg.x, textOrg.y - totalHeight),
                new Point(textOrg.x + maxWidth, textOrg.y + baseLine[0]),
                new Scalar(0, 0, 0), -1);

        // Draw each line of text with additional spacing
        for (int i = 0; i < lines.length; i++) {
            Point lineOrg = new Point(textOrg.x, textOrg.y - totalHeight + (i + 1) * (baseLine[0] + lineSpacing));
            Imgproc.putText(frame, lines[i], lineOrg, fontFace, fontScale, new Scalar(255, 255, 255), thickness);
        }
    }
}
