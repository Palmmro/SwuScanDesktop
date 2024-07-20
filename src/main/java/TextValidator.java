import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.List;

public class TextValidator {
    public static Card findCard(String scannedText, List<Card> collection){
        for (Card card : collection){

            if (scannedText.toLowerCase().contains(card.getCardName().toLowerCase())) {
                return card;

            }
        }
        return new Card();
    }

    public static String findClosestMatch(String longString, List<String> strings) {
        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
        String closestMatch = null;
        int smallestDistance = Integer.MAX_VALUE;
        int threshold = 3;  // Define a threshold for acceptable match

        for (String str : strings) {
            int distance = levenshteinDistance.apply(longString, str);
            if (distance < smallestDistance) {
                smallestDistance = distance;
                closestMatch = str;
            }
        }

        // Check if the closest match is within the acceptable threshold
        if (smallestDistance <= threshold) {
            return closestMatch;
        } else {
            return "";  // No match found within the threshold
        }
    }

    public static void main(String[] args) {
        String longString = "Enumerating objects: 17, done.\n" +
                "Counting objects: 100% (17/17), done.\n" +
                "Delta compression using up to 12 threads\n" +
                "Compressing objects: 100% (7/7), done.\n" +
                "Writing objects: 100% (9/9), 3.43 KiB | 3.43 MiB/s, done.\n" +
                "Total 9 (delta 5), reused 0 (delta 0), pack-reused 0\n" +
                "remote: Resolving deltas: 100% (5/5), completed with 5 local objects.";

        List<String> strings = List.of("Delt compression", "Crocodile", "Panda bear");

        String closestMatch = findClosestMatch(longString, strings);
        System.out.println("Closest match: " + (closestMatch != null ? closestMatch : "No close match found"));
    }
}
