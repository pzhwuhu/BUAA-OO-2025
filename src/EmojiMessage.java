import com.oocourse.spec3.main.EmojiMessageInterface;
import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.TagInterface;

public class EmojiMessage extends Message implements EmojiMessageInterface {

    private final int emojiId;

    public EmojiMessage(int id, int emojiId, PersonInterface person1, PersonInterface person2) {
        super(id, emojiId, person1, person2); // socialValue is emojiId
        this.emojiId = emojiId;
    }

    public EmojiMessage(int id, int emojiId, PersonInterface person1, TagInterface tag) {
        super(id, emojiId, person1, tag); // socialValue is emojiId
        this.emojiId = emojiId;
    }

    @Override
    public int getEmojiId() {
        return emojiId;
    }
}