import com.oocourse.spec2.main.PersonInterface;
import com.oocourse.spec2.main.TagInterface;
import java.util.HashMap;
import java.util.List;

public class Person implements PersonInterface {
    private final int id;
    private final String name;
    private final int age;
    private HashMap<Integer, PersonInterface> acquaintance = new HashMap<>();
    private HashMap<Integer, Integer> values = new HashMap<>();
    private HashMap<Integer, TagInterface> tags = new HashMap<>();
    private LinkedMap receivedArticles = new LinkedMap();
    private int bestAcquaintance = -99999;

    public Person(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    @Override
    public int getId() { return id; }

    @Override
    public String getName() { return name; }

    @Override
    public int getAge() { return age; }

    @Override
    public boolean containsTag(int id) { return tags.containsKey(id); }

    @Override
    public TagInterface getTag(int id) {
        if (tags.containsKey(id)) {
            return tags.get(id);
        }
        return null;
    }

    @Override
    public void addTag(TagInterface tag) { tags.put(tag.getId(), tag); }

    @Override
    public void delTag(int id) { tags.remove(id); }

    @Override
    public boolean isLinked(PersonInterface person) {
        return id == person.getId() || acquaintance.containsKey(person.getId()); }

    @Override
    public int queryValue(PersonInterface person) {
        if (acquaintance.containsKey(person.getId())) { return values.get(person.getId()); }
        return 0;
    }

    public boolean equals(Object object) {
        if (object == null || object.getClass() != this.getClass()) { return false; }
        return id == ((PersonInterface) object).getId();
    }

    @Override
    public List<Integer> getReceivedArticles() {
        return receivedArticles.toList();
    }

    @Override
    public List<Integer> queryReceivedArticles() {
        return receivedArticles.toList().subList(0, Math.min(5, receivedArticles.getSize()));
    }

    /// //////////////////////////////////////////////////////////////
    public void addRelation(Person person, int value) {
        acquaintance.put(person.getId(), person);
        values.put(person.getId(), value);
        if (bestAcquaintance == -99999) {
            bestAcquaintance = person.getId();
            return;
        }
        int bestValue = values.get(bestAcquaintance);
        if (value > bestValue || (value == bestValue && person.getId() < bestAcquaintance)) {
            bestAcquaintance = person.getId();
        }
    }

    public void modifyRelation(Person person, int newValue) {
        int bestValue = values.get(bestAcquaintance);
        values.put(person.getId(), newValue);
        if (bestAcquaintance == person.getId()) {
            if (newValue < bestValue) { reSetBestAcquaintance(); }
        } else {
            if (newValue > bestValue
                || (newValue == bestValue && person.getId() < bestAcquaintance)) {
                bestAcquaintance = person.getId();
            }
        }
        this.updateTagValueSum(person);
    }

    public void updateTagValueSum(Person person) {
        for (TagInterface tag : tags.values()) {
            if (tag.hasPerson(person)) {
                Tag myTag = (Tag) tag;
                myTag.updateValueSum();
            }
        }
    }

    public void delRelation(Person person) {
        int delId = person.getId();
        acquaintance.remove(delId);
        values.remove(delId);
        for (TagInterface tag : tags.values()) {
            if (tag.hasPerson(person)) {
                tag.delPerson(person);
                Tag myTag = (Tag) tag;
                myTag.updateValueSum();
            }
        }
        if (bestAcquaintance == delId) { reSetBestAcquaintance(); }

    }

    public void reSetBestAcquaintance() {
        int bestValue = -1;
        int bestId = -99999;
        for (PersonInterface bro: acquaintance.values()) {
            if (values.get(bro.getId()) > bestValue
                || (values.get(bro.getId()) == bestValue && bro.getId() < bestId)) {
                bestValue = values.get(bro.getId());
                bestId = bro.getId();
            }
        }
        bestAcquaintance = bestId;
    }

    public int queryBestAcquaintance() { return bestAcquaintance; }

    public HashMap<Integer, PersonInterface> getAcquaintance() { return acquaintance; }

    public HashMap<Integer, Integer> getValues() { return values; }

    public boolean strictEquals(Person person) {
        return id == person.getId() && name.equals(person.getName()) && age == person.getAge() &&
            (acquaintance.equals(person.getAcquaintance()) && values.equals(person.getValues()));
    }

    public void addReceived(int articleId) {
        receivedArticles.insertHead(articleId);
    }

    public void removeReceived(int articleId) {
        receivedArticles.delete(articleId);
    }
}
