import lombok.Getter;
import org.opencv.core.*;
import org.opencv.features2d.BFMatcher;
import org.opencv.features2d.ORB;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ImageMatcher {

    private static final HashMap<String, File[]> images = new HashMap<>();
    private static File[] imageFiles;

    public static Card findBestMatch(Mat frame, String folderPath) {
        String set = folderPath.substring(folderPath.lastIndexOf("/")+1);
        imageFiles = images.get(set);

        if(imageFiles == null){
            File folder = new File(folderPath);
            imageFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg"));
            images.put(set,imageFiles);
        }

        if (imageFiles == null) {
            System.out.println("No images found in directory.");
            return null;
        }

        ORB orb = ORB.create(1000);
        MatOfKeyPoint keypointsFrame = new MatOfKeyPoint();
        Mat descriptorsFrame = new Mat();
        orb.detectAndCompute(frame, new Mat(), keypointsFrame, descriptorsFrame);

        System.out.println("Frame - Keypoints: " + keypointsFrame.size() + ", Descriptors: " + descriptorsFrame.size() + ", Type: " + descriptorsFrame.type());

        Card bestMatch = null;
        int maxMatches = 0;

        for (File imageFile : imageFiles) {
            Mat img = Imgcodecs.imread(imageFile.getAbsolutePath(), Imgcodecs.IMREAD_GRAYSCALE);
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

                String name = imageFile.getName().substring(0,imageFile.getName().lastIndexOf("_")).replace("_"," ");
                String cardNumber = imageFile.getName().substring(imageFile.getName().lastIndexOf("_")+1,imageFile.getName().lastIndexOf("."));
//                String set = folderPath.substring(folderPath.lastIndexOf('/')+1);
                bestMatch = new Card(set,name,cardNumber);
//                bestMatch = imageFile.getName();
            }
        }
        return bestMatch;
    }

    static class MatchResult {
        Card card;
        @Getter
        int matches;

        MatchResult(Card card, int matches) {
            this.card = card;
            this.matches = matches;
        }
        MatchResult(){
            this.card = null;
            matches = -1;
        }

    }

    public static Card findBestMatchParallel(Mat frame, String folderPath) {
        String set = folderPath.substring(folderPath.lastIndexOf("/")+1);
        imageFiles = images.get(set);

        if(imageFiles == null){
            File folder = new File(folderPath);
            imageFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg"));
            images.put(set,imageFiles);
        }

        if (imageFiles == null) {
            System.out.println("No images found in directory.");
            return null;
        }

        ORB orb = ORB.create(1000);
        MatOfKeyPoint keypointsFrame = new MatOfKeyPoint();
        Mat descriptorsFrame = new Mat();
        orb.detectAndCompute(frame, new Mat(), keypointsFrame, descriptorsFrame);

        System.out.println("Frame - Keypoints: " + keypointsFrame.size() + ", Descriptors: " + descriptorsFrame.size() + ", Type: " + descriptorsFrame.type());

        var results = Arrays.stream(imageFiles).parallel().map(img -> getMatchResult(img, orb, descriptorsFrame, folderPath));

        var matchResult = results.max(Comparator.comparingInt(MatchResult::getMatches)).orElse(new MatchResult());
        System.out.println("Score: "+matchResult.matches);
        return matchResult.card;
    }

    private static MatchResult getMatchResult(File imageFile, ORB orb, Mat descriptorsFrame, String folderPath){
        Mat img = Imgcodecs.imread(imageFile.getAbsolutePath(), Imgcodecs.IMREAD_GRAYSCALE);
        if (img.empty()){
            return new MatchResult(new Card(),-1);
        }

        MatOfKeyPoint keypointsImg = new MatOfKeyPoint();
        Mat descriptorsImg = new Mat();
        orb.detectAndCompute(img, new Mat(), keypointsImg, descriptorsImg);

        System.out.println("Comparing with: " + imageFile.getName());
        System.out.println("Image - Keypoints: " + keypointsImg.size() + ", Descriptors: " + descriptorsImg.size() + ", Type: " + descriptorsImg.type());

        int matches = matchFeatures(descriptorsFrame, descriptorsImg);

        String name = imageFile.getName().substring(0,imageFile.getName().lastIndexOf("_")).replace("_"," ");
        String cardNumber = imageFile.getName().substring(imageFile.getName().lastIndexOf("_")+1,imageFile.getName().lastIndexOf("."));
        String set = folderPath.substring(folderPath.lastIndexOf('/')+1);
        var card = new Card(set,name,cardNumber);

        return new MatchResult(card,matches);
    }

    private static int matchFeaturesParallel(Mat descriptors1, Mat descriptors2) {
        if (descriptors1.empty() || descriptors2.empty()) return 0;

        BFMatcher matcher = BFMatcher.create(BFMatcher.BRUTEFORCE_HAMMING, true);
        List<MatOfDMatch> knnMatches = new ArrayList<>();
        matcher.knnMatch(descriptors1, descriptors2, knnMatches, 2);

        double ratioThreshold = 0.65; // Make this stricter (was 0.75)
        List<DMatch> goodMatches = new ArrayList<>();

        for (MatOfDMatch mat : knnMatches) {
            if (mat.toArray().length == 2) {
                DMatch[] matches = mat.toArray();
                if (matches[0].distance < ratioThreshold * matches[1].distance) {
                    goodMatches.add(matches[0]);
                }
            }
        }

        return goodMatches.size();
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
