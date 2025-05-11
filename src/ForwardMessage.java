import com.oocourse.spec3.main.ForwardMessageInterface;
import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.TagInterface;

public class ForwardMessage extends Message implements ForwardMessageInterface {

    private final int articleId;

    public ForwardMessage(int id, int articleId, PersonInterface person1, PersonInterface person2) {
        super(id, Math.abs(articleId) % 200, person1, person2); //socialValue is abs(articleId)% 200
        this.articleId = articleId;
    }

    public ForwardMessage(int id, int articleId, PersonInterface person1, TagInterface tag) {
        super(id, Math.abs(articleId) % 200, person1, tag); // socialValue is abs(articleId) % 200
        this.articleId = articleId;
    }

    @Override
    public int getArticleId() {
        return articleId;
    }
}