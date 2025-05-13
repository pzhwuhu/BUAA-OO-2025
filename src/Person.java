import com.oocourse.spec3.main.MessageInterface;
import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.TagInterface;

import java.util.HashMap;
import java.util.List;

public class Person implements PersonInterface {
    private final int id;
    private final String name;
    private final int age;
    private HashMap<Integer, PersonInterface> acquaintance = new HashMap<>();
    private HashMap<Integer, Integer> values = new HashMap<>();
    private HashMap<Integer, TagInterface> tags = new HashMap<>();
    private MultiLinkedMap<Integer> receivedArticles = new MultiLinkedMap();
    private int bestAcquaintance = -99999;
    private int bestValue = -99999;
    private boolean bestDirty = true;
    private int socialValue = 0; //新增
    private int money = 0; //新增
    private LinkedMap<MessageInterface> messages = new LinkedMap();

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
    public void addSocialValue(int num) { socialValue += num; }

    @Override
    public int getSocialValue() { return socialValue; }

    @Override
    public List<MessageInterface> getMessages() { return messages.toList(); }

    @Override
    public List<MessageInterface> getReceivedMessages() { return messages.to5List(); }

    @Override
    public void addMoney(int num) { money += num; }

    @Override
    public int getMoney() { return money; }

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
    public List<Integer> queryReceivedArticles() { return receivedArticles.to5List(); }

    /// //////////////////////////////////////////////////////////////
    public void addRelation(Person person, int value) {
        acquaintance.put(person.getId(), person);
        values.put(person.getId(), value);
        if (!bestDirty) {
            if (value > bestValue || (value == bestValue && person.getId() < bestAcquaintance)) {
                bestAcquaintance = person.getId();
                bestValue = value;
            }
        }
    }

    public void modifyRelation(Person person, int newValue) {
        values.put(person.getId(), newValue);
        if (!bestDirty) {
            if (bestAcquaintance == person.getId()) {
                if (newValue < bestValue) { bestDirty = true; }
                else { bestValue = newValue; }
            } else {
                if (newValue > bestValue
                    || (newValue == bestValue && person.getId() < bestAcquaintance)) {
                    bestAcquaintance = person.getId();
                    bestValue = newValue;
                }
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
            }
        }
        if (!bestDirty) {
            if (bestAcquaintance == delId) { bestDirty = true; }
        }
    }

    public void reSetBestAcquaintance() {
        bestAcquaintance = -99999;
        bestValue = -99999;
        for (PersonInterface bro: acquaintance.values()) {
            if (values.get(bro.getId()) > bestValue
                || (values.get(bro.getId()) == bestValue && bro.getId() < bestAcquaintance)) {
                bestValue = values.get(bro.getId());
                bestAcquaintance = bro.getId();
            }
        }
    }

    public int queryBestAcquaintance() {
        if (bestDirty) {
            bestDirty = false;
            reSetBestAcquaintance();
        }
        return bestAcquaintance;
    }

    public HashMap<Integer, PersonInterface> getAcquaintance() { return acquaintance; }

    public HashMap<Integer, Integer> getValues() { return values; }

    public boolean strictEquals(Person person) {
        return id == person.getId() && name.equals(person.getName()) && age == person.getAge() &&
            (acquaintance.equals(person.getAcquaintance()) && values.equals(person.getValues()));
    }

    public void addReceived(int articleId) {
        receivedArticles.insertHead(String.valueOf(articleId), articleId);
    }

    public void removeReceived(int articleId) {
        receivedArticles.delete(String.valueOf(articleId));
    }

    public void receiveMessage(MessageInterface message) {
        messages.insertHead(String.valueOf(message.getId()), message);
    }

    public void removeMessage(int id) { messages.delete(String.valueOf(id)); }
}
