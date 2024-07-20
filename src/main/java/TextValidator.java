import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.List;
import java.util.stream.Collectors;

public class TextValidator {
    public static Card findCard(String scannedText, List<Card> collection){

        List<Card> longCollection = collection.stream().filter(c -> c.getCardName().length()>=5).collect(Collectors.toList());
        List<Card> shortCollection = collection.stream().filter(c -> c.getCardName().length()<5).collect(Collectors.toList());

        Card exactMatchLong = findExactMatch(scannedText, longCollection);
        if(exactMatchLong!=null){
            return exactMatchLong;
        }

        Card closestMatchLong = findClosestMatch(scannedText,longCollection);
        if(closestMatchLong != null){
            return closestMatchLong;
        }
        return findExactMatch(scannedText,shortCollection);
    }

    private static Card findExactMatch(String scannedText, List<Card> collection){
        for (Card card : collection){

            if (scannedText.toLowerCase().contains(card.getCardName().toLowerCase())) {
                return card;

            }
        }
        return null;
    }

    public static Card findClosestMatch(String longString, List<Card> cards) {
        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
        Card closestMatch = null;
        int smallestDistance = Integer.MAX_VALUE;
        int threshold = 2;  // Define a threshold for acceptable match


        // Create substrings using a sliding window approach
//        int windowSize = cards.stream().map(Card::getCardName).mapToInt(String::length).max().orElse(0);
        for (int i = 0; i <= longString.length(); i++) {

            for (Card card : cards) {
                String substring = longString.substring(i, Math.min(i + card.getCardName().length(),longString.length()));
                int distance = levenshteinDistance.apply(substring, card.getCardName());
                if (distance < smallestDistance) {
                    smallestDistance = distance;
                    closestMatch = card;
                }
            }
        }

        // Check if the closest match is within the acceptable threshold
        if (smallestDistance <= threshold) {
            return closestMatch;
        } else {
            return null;  // No match found within the threshold
        }
    }

    public static void main(String[] args) {
        String longString = "Enumerating objects: 17, done.\n" +
                "Counting objects: 100% (17/17), done.\n" +
                "Delta compression using up to 12 threads\n" +
                "Supercommand Aquad: 100% (7/7), done.\n" +
                "Writing objects: 100% (9/9), 3.43 KiB | 3.43 MiB/s, done.\n" +
                "Total 9 (delta 5), reused 0 (delta 0), pack-reused 0\n" +
                "remote: Resolving deltas: 100% (5/5), completed with 5 local objects.";

        CollectionUtil collectionUtil = new CollectionUtil();

        Card closestMatch = findClosestMatch(longString, collectionUtil.getCollectionCards());
        System.out.println("Closest match: " + (closestMatch != null ? closestMatch.getUniqueDisplayName() : "No close match found"));
    }
}
