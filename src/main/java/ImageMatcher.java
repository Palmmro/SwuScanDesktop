import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
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

    private static final HashMap<String, List<File>> images = new HashMap<>();

    static {
        Main.PLAYABLE_SETS.forEach(set -> {
            File folder = new File(Main.RESOURCE_PATH+set);
            File[] all = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg"));
            if(all == null){
                throw new RuntimeException("Cannot find files");
            }
            images.put(set, Arrays.stream(all).toList());
        });
        precomputeDescriptors();

    }
    public static void precomputeDescriptors() {
        ORB orb = ORB.create(1000);
        for (String set : Main.PLAYABLE_SETS) {
            for (File imgFile : images.get(set)) {
                if (!DescriptorCache.contains(imgFile.getAbsolutePath())) {
                    Mat img = Imgcodecs.imread(imgFile.getAbsolutePath(), Imgcodecs.IMREAD_GRAYSCALE);
                    if (!img.empty()) {
                        MatOfKeyPoint keypointsImg = new MatOfKeyPoint();
                        Mat descriptorsImg = new Mat();
                        orb.detectAndCompute(img, new Mat(), keypointsImg, descriptorsImg);
                        DescriptorCache.addDescriptor(imgFile.getAbsolutePath(), descriptorsImg);
                    }
                }
            }
        }
    }


//    public static Card findBestMatch(Mat frame, String folderPath) {
//        String set = folderPath.substring(folderPath.lastIndexOf("/")+1);
//        imageFiles = images.get(set);
//
//        if(imageFiles == null){
//            File folder = new File(folderPath);
//            imageFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg"));
//            images.put(set,imageFiles);
//        }
//
//        if (imageFiles == null) {
//            System.out.println("No images found in directory.");
//            return null;
//        }
//
//        ORB orb = ORB.create(1000);
//        MatOfKeyPoint keypointsFrame = new MatOfKeyPoint();
//        Mat descriptorsFrame = new Mat();
//        orb.detectAndCompute(frame, new Mat(), keypointsFrame, descriptorsFrame);
//
//        System.out.println("Frame - Keypoints: " + keypointsFrame.size() + ", Descriptors: " + descriptorsFrame.size() + ", Type: " + descriptorsFrame.type());
//
//        Card bestMatch = null;
//        int maxMatches = 0;
//
//        for (File imageFile : imageFiles) {
//            Mat img = Imgcodecs.imread(imageFile.getAbsolutePath(), Imgcodecs.IMREAD_GRAYSCALE);
//            if (img.empty()) continue;
//
//            MatOfKeyPoint keypointsImg = new MatOfKeyPoint();
//            Mat descriptorsImg = new Mat();
//            orb.detectAndCompute(img, new Mat(), keypointsImg, descriptorsImg);
//
//            System.out.println("Comparing with: " + imageFile.getName());
//            System.out.println("Image - Keypoints: " + keypointsImg.size() + ", Descriptors: " + descriptorsImg.size() + ", Type: " + descriptorsImg.type());
//
//            int matches = matchFeatures(descriptorsFrame, descriptorsImg);
//            System.out.println("Matches found: " + matches);
//
//            if (matches > maxMatches) {
//
//                maxMatches = matches;
//
//                String name = imageFile.getName().substring(0,imageFile.getName().lastIndexOf("_")).replace("_"," ");
//                String cardNumber = imageFile.getName().substring(imageFile.getName().lastIndexOf("_")+1,imageFile.getName().lastIndexOf("."));
////                String set = folderPath.substring(folderPath.lastIndexOf('/')+1);
//                bestMatch = new Card(set,name,cardNumber);
////                bestMatch = imageFile.getName();
//            }
//        }
//        return bestMatch;
//    }

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

    public static Card findBestMatchParallel(Mat frame, String set) {
        String folderPath = Main.RESOURCE_PATH + set;
        List<File> imageFiles;
    var start = System.currentTimeMillis();
        if(Objects.equals(set, "ALL")){
            imageFiles = Main.PLAYABLE_SETS.parallelStream()
                    .map(images::get)
                    .flatMap(Collection::parallelStream).toList();

        } else {
            imageFiles = images.get(set);
        }
        var end = System.currentTimeMillis();

        if (imageFiles.isEmpty()) {
            System.out.println("No images found in directory.");
            return null;
        }

        ORB orb = ORB.create(1000);
        MatOfKeyPoint keypointsFrame = new MatOfKeyPoint();
        Mat descriptorsFrame = new Mat();
        orb.detectAndCompute(frame, new Mat(), keypointsFrame, descriptorsFrame);

        System.out.println("Frame - Keypoints: " + keypointsFrame.size() + ", Descriptors: " + descriptorsFrame.size() + ", Type: " + descriptorsFrame.type());

        var results = imageFiles.stream().parallel().map(img -> getMatchResult(img, orb, descriptorsFrame, folderPath));

        var matchResult = results.max(Comparator.comparingInt(MatchResult::getMatches)).orElse(new MatchResult());
        System.out.println("Time taken init: "+(end - start));
        System.out.println("Score: "+matchResult.matches);
        return matchResult.card;
    }

    private static MatchResult getMatchResult(File imageFile, ORB orb, Mat descriptorsFrame, String folderPath) {
    String filePath = imageFile.getAbsolutePath();

    Mat descriptorsImg;
    if (DescriptorCache.contains(filePath)) {
        descriptorsImg = DescriptorCache.getDescriptor(filePath);
    } else {
        Mat img = Imgcodecs.imread(filePath, Imgcodecs.IMREAD_GRAYSCALE);
        if (img.empty()) {
            return new MatchResult(new Card(), -1);
        }

        MatOfKeyPoint keypointsImg = new MatOfKeyPoint();
        descriptorsImg = new Mat();
        orb.detectAndCompute(img, new Mat(), keypointsImg, descriptorsImg);

        // Store descriptors in cache
        DescriptorCache.addDescriptor(filePath, descriptorsImg);
    }

    int matches = matchFeatures(descriptorsFrame, descriptorsImg);

    String name = imageFile.getName().substring(0, imageFile.getName().lastIndexOf("_")).replace("_", " ");
    String cardNumber = imageFile.getName().substring(imageFile.getName().lastIndexOf("_") + 1, imageFile.getName().lastIndexOf("."));
    String set = folderPath.substring(folderPath.lastIndexOf('/') + 1);

    return new MatchResult(new Card(set, name, cardNumber), matches);
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
            return 0;
        }
    }






}
