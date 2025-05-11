import com.oocourse.spec3.main.RedEnvelopeMessageInterface;
import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.TagInterface;

public class RedEnvelopeMessage extends Message implements RedEnvelopeMessageInterface {

    private final int money;

    public RedEnvelopeMessage(int id, int money, PersonInterface person1, PersonInterface person2) {
        super(id, money * 5, person1, person2); // socialValue is money * 5
        this.money = money;
    }

    public RedEnvelopeMessage(int id, int money, PersonInterface person1, TagInterface tag) {
        super(id, money * 5, person1, tag);  // socialValue is money * 5
        this.money = money;
    }

    @Override
    public int getMoney() {
        return money;
    }
}