import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Card {
    private String set;
    private String cardName;
    private String cardNumber;
    private int count;
    private boolean isFoil;

    @Override
    public String toString(){
        return "["+set+", "+cardName+ ", "+cardNumber+", "+count+", "+isFoil+"]";
    }
}
