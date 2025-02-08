import org.opencv.core.*;
import org.opencv.features2d.BFMatcher;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FlannBasedMatcher;
import org.opencv.features2d.ORB;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageMatcher {

    public static String findBestMatch(Mat frame, String folderPath) {
        File folder = new File(folderPath);
        File[] imageFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg"));

        if (imageFiles == null) {
            System.out.println("No images found in directory.");
            return "No Match";
        }

        ORB orb = ORB.create(1000);
        MatOfKeyPoint keypointsFrame = new MatOfKeyPoint();
        Mat descriptorsFrame = new Mat();
        orb.detectAndCompute(frame, new Mat(), keypointsFrame, descriptorsFrame);

        System.out.println("Frame - Keypoints: " + keypointsFrame.size() + ", Descriptors: " + descriptorsFrame.size() + ", Type: " + descriptorsFrame.type());

        String bestMatch = "No Match";
        int maxMatches = 0;

        for (File imageFile : imageFiles) {
            Mat img = Imgcodecs.imread(imageFile.getAbsolutePath());
            if (img.empty()) continue;

            MatOfKeyPoint keypointsImg = new MatOfKeyPoint();
            Mat descriptorsImg = new Mat();
            orb.detectAndCompute(img, new Mat(), keypointsImg, descriptorsImg);

            System.out.println("Comparing with: " + imageFile.getName());
            System.out.println("Image - Keypoints: " + keypointsImg.size() + ", Descriptors: " + descriptorsImg.size() + ", Type: " + descriptorsImg.type());

            int matches = matchFeatures(descriptorsFrame, descriptorsImg);
            System.out.println("Matches found: " + matches);

            if (matches > maxMatches) {
                maxMatches = matches;
                bestMatch = imageFile.getName();
            }
        }

        return bestMatch;
    }

    private static int matchFeatures(Mat descriptors1, Mat descriptors2) {
        if (descriptors1.empty() || descriptors2.empty()) {
            System.out.println("Error: One or both descriptor matrices are empty!");
            return 0;
        }

        System.out.println("Matching Features...");

        try {
            BFMatcher matcher = BFMatcher.create(BFMatcher.BRUTEFORCE_HAMMING);
            List<MatOfDMatch> knnMatches = new ArrayList<>();
            matcher.knnMatch(descriptors1, descriptors2, knnMatches, 2); // Get 2 best matches

            // Apply Lowe’s Ratio Test
            double ratioThreshold = 0.75;
            int goodMatches = 0;
            for (MatOfDMatch matOfDMatch : knnMatches) {
                if (matOfDMatch.rows() > 1) {
                    DMatch[] matches = matOfDMatch.toArray();
                    if (matches[0].distance < ratioThreshold * matches[1].distance) {
                        goodMatches++;
                    }
                }
            }

            System.out.println("Good Matches: " + goodMatches);
            return goodMatches;
        } catch (Exception e) {
            System.out.println("Error during feature matching: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }






}
