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
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class Main {
    private static final int WEBCAM_ID = 1;
    private static final String SET = "SOR";

    private static final int ZERO = 96;
    private static final int ONE = 97;
    private static final int TWO = 98;
    private static final int THREE = 99;
    private static final int FOUR = 100;
    private static final int FIVE = 101;
    private static final int SIX = 102;
    private static final int SEVEN = 103;
    private static final int EIGHT = 104;
    private static final int NINE = 105;
    private static final int S = 83;

    private static final String SCANNING_TEXT = "Scanning...";
    private static final String OPTIONS_TEXT = "? \n1(N)/2(H)/3(F)/4(HF) to add. \n0 to reset. 5 to add bulk";

    private static final List<Card> currentCards = new ArrayList<>();

    private static final CollectionUtil collectionUtil = new CollectionUtil();
    private static final String BULK_TEXT = "Add how many cards?";
    private static String tempDisplayText = "Added X cards.";

    private static Instant timeToDisplay = Instant.MIN;

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

                if (timeToDisplay.isAfter(Instant.now())) {
                    addTextToFrame(frame, tempDisplayText, 10, 60, 0.75, 1);
                }

                // Display the frame using HighGui
                HighGui.imshow("Webcam", frame);


                if (HighGui.waitKey(500) == 'q') {
                    break;
                } else {
                    if (displayText.equals(SCANNING_TEXT)) {
                        var starttime = System.currentTimeMillis();
                        foundCard = ImageMatcher.findBestMatchParallel(frame, "src/main/resources/images/" + SET);
                        System.out.println("Time taken: " + (System.currentTimeMillis() - starttime));
                        if (foundCard != null) {
                            displayText = foundCard.getUniqueDisplayName() + OPTIONS_TEXT;
                        }
                    }
                }

                var key = HighGui.pressedKey;
                if (key == ONE && foundCard != null) {
                    logTempText("Added regular " + foundCard.getUniqueDisplayName() + " to csv");
                    saveCard(foundCard, false, 1);
                    foundCard = null;
                    displayText = SCANNING_TEXT;
                }
                if (key == TWO && foundCard != null) {
                    if (displayText.equals(BULK_TEXT)) {
                        logTempText("Added 2 regular " + foundCard.getUniqueDisplayName() + " to csv");
                        saveCard(foundCard, false, 2);
                        foundCard = null;
                        displayText = SCANNING_TEXT;
                    } else if (saveHyperspaceCard(foundCard, false)) {
                        logTempText("Added hyperspace " + foundCard.getUniqueDisplayName() + " to csv");
                        foundCard = null;
                        displayText = SCANNING_TEXT;
                    }
                }
                if (key == THREE && foundCard != null) {
                    if (displayText.equals(BULK_TEXT)) {
                        logTempText("Added 3 regular " + foundCard.getUniqueDisplayName() + " to csv");
                        saveCard(foundCard, false, 3);
                        foundCard = null;
                        displayText = SCANNING_TEXT;
                    } else {
                        logTempText("Added foil " + foundCard.getUniqueDisplayName() + " to csv");
                        saveCard(foundCard, true, 1);
                        foundCard = null;
                        displayText = SCANNING_TEXT;
                    }
                }
                if (key == FOUR && foundCard != null) {
                    if (displayText.equals(BULK_TEXT)) {
                        logTempText("Added 4 regular " + foundCard.getUniqueDisplayName() + " to csv");
                        saveCard(foundCard, false, 4);
                        foundCard = null;
                        displayText = SCANNING_TEXT;
                    } else if (saveHyperspaceCard(foundCard, true)) {
                        logTempText("Added hyperspace foil " + foundCard.getUniqueDisplayName() + " to csv");
                        saveCard(foundCard, false, 1);
                        foundCard = null;
                        displayText = SCANNING_TEXT;
                    }
                }
                if (key == FIVE && foundCard != null) {
                    if (displayText.equals(BULK_TEXT)) {
                        logTempText("Added 5 regular " + foundCard.getUniqueDisplayName() + " to csv");
                        saveCard(foundCard, false, 5);
                        foundCard = null;
                        displayText = SCANNING_TEXT;
                    } else {
                        displayText = BULK_TEXT;
                    }
                }
                if (key == SIX && foundCard != null) {
                    if (displayText.equals(BULK_TEXT)) {
                        logTempText("Added 6 regular " + foundCard.getUniqueDisplayName() + " to csv");
                        saveCard(foundCard, false, 6);
                        foundCard = null;
                        displayText = SCANNING_TEXT;
                    }
                }
                if (key == SEVEN && foundCard != null) {
                    if (displayText.equals(BULK_TEXT)) {
                        logTempText("Added 7 regular " + foundCard.getUniqueDisplayName() + " to csv");
                        saveCard(foundCard, false, 7);
                        foundCard = null;
                        displayText = SCANNING_TEXT;
                    }
                }
                if (key == EIGHT && foundCard != null) {
                    if (displayText.equals(BULK_TEXT)) {
                        logTempText("Added 8 regular " + foundCard.getUniqueDisplayName() + " to csv");
                        saveCard(foundCard, false, 8);
                        foundCard = null;
                        displayText = SCANNING_TEXT;
                    }
                }
                if (key == NINE && foundCard != null) {
                    if (displayText.equals(BULK_TEXT)) {
                        logTempText("Added 9 regular " + foundCard.getUniqueDisplayName() + " to csv");
                        saveCard(foundCard, false, 9);
                        foundCard = null;
                        displayText = SCANNING_TEXT;
                    }
                }
                if (key == ZERO) {
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

    private static void logTempText(String text) {
        timeToDisplay = Instant.now().plusSeconds(3);
        tempDisplayText = text;
    }

    private static void saveCard(Card card, boolean isHyperspace, boolean isFoil, int count) {
        currentCards.add(new Card(card.getSet(), card.getCardName(), card.getCardNumber(), count, isFoil));
        collectionUtil.saveToCsv(currentCards);
    }

    private static void saveCard(Card card, boolean isFoil, int count) {
        saveCard(card, false, isFoil, count);
    }

    private static String getHyperspaceNumber(String number, String set) {
        if (set == "SOR") {
            int offset;
            int nr = -1;
            if (number.startsWith("00")) {
                nr = Integer.parseInt(number.substring(2));
            } else if (number.startsWith("0")) {
                nr = Integer.parseInt(number.substring(1));
            } else {
                nr = Integer.parseInt(number);
            }

            if(1 <= nr && nr <= 4) offset = 268;
            if(nr == 5){offset = -1;}// LUKE GC23/1
            if(6 <= nr && nr <= 9) offset = 267;
            if(nr == 10){offset = -1;}// Vader GC23/2
            if(11 <= nr && nr <= 52) offset = 267;
            if(nr == 53){offset = -1;}// Luke's Lightsaber SOROP/08
            if(54 <= nr && nr <= 84) offset = 267;



            return "M";
        }
        return "";
    }

    private static boolean saveHyperspaceCard(Card card, boolean isFoil) {
        Card hyperspaceCard = collectionUtil.getHyperspaceCardFromName(card.getCardName());
        if (hyperspaceCard == null) {
            System.out.println("No hyperspace available for card");
            return false;
        }
        currentCards.add(new Card(hyperspaceCard.getSet(), hyperspaceCard.getCardName(), hyperspaceCard.getCardNumber(), 1, isFoil));
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
        addTextToFrame(frame, text, 10, frame.rows() - 50, 1.0, 2);
    }

    private static void addTextToFrame(Mat frame, String text, int x, int y, double fontScale, int thickness) {
        int fontFace = 0;
        int[] baseLine = new int[1];
        Point textOrg = new Point(x, y);

        // Split the text into lines
        String[] lines = text.split("\n");

        // Calculate the text size for each line and find the maximum width
        double maxWidth = 0;
        int totalHeight = 0;
        int lineSpacing = 30; // Additional space between lines
        int hSpace = 8;

        for (String line : lines) {
            Size textSize = Imgproc.getTextSize(line, fontFace, fontScale, thickness, baseLine);
            maxWidth = Math.max(maxWidth, textSize.width) + hSpace;
            totalHeight += textSize.height + lineSpacing; // Adding space between lines
        }

        // Draw the black rectangle for all lines
        Imgproc.rectangle(frame, new Point(textOrg.x - hSpace, textOrg.y - totalHeight),
                new Point(textOrg.x + maxWidth, textOrg.y + baseLine[0]),
                new Scalar(0, 0, 0), -1);

        // Draw each line of text with additional spacing
        for (int i = 0; i < lines.length; i++) {
            Point lineOrg = new Point(textOrg.x, textOrg.y - totalHeight + (i + 1) * (baseLine[0] + lineSpacing));
            Imgproc.putText(frame, lines[i], lineOrg, fontFace, fontScale, new Scalar(255, 255, 255), thickness);
        }
    }
}
