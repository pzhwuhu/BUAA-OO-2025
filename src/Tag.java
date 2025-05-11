import com.oocourse.spec3.main.MessageInterface;
import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.TagInterface;

import java.util.HashMap;
import java.util.HashSet;

public class Tag implements TagInterface {
    private final int id;
    private int ageSum;
    private int agePowSum;
    private int valueSum = 0;
    private boolean dirty = false;
    private HashMap<Integer, PersonInterface> persons = new HashMap();

    public Tag(int id) {
        this.id = id;
        agePowSum = 0;
        ageSum = 0;
    }

    @Override
    public int getId() { return id; }

    @Override
    public int getSize() { return persons.size(); }

    @Override
    public void addPerson(PersonInterface person) {
        persons.put(person.getId(), person);
        ageSum += person.getAge();
        agePowSum += person.getAge() * person.getAge();
        for (PersonInterface existedPerson : persons.values()) {
            if (existedPerson.isLinked(person)) {
                valueSum += 2 * existedPerson.queryValue(person);
            }
        }
    }

    @Override
    public boolean hasPerson(PersonInterface person) { return persons.containsValue(person); }

    @Override
    public void delPerson(PersonInterface person) {
        ageSum -= person.getAge();
        agePowSum -= person.getAge() * person.getAge();
        persons.remove(person.getId());
        for (PersonInterface existedPerson : persons.values()) {
            if (existedPerson.isLinked(person)) {
                valueSum -= 2 * existedPerson.queryValue(person);
            }
        }
    }

    @Override
    public int getValueSum() {
        //dirty = true;
        if (dirty) {
            int half = 0;
            HashSet<Integer> visited = new HashSet<>();
            for (PersonInterface person : persons.values()) {
                int id1 = person.getId();
                Person p1 = (Person) person;
                visited.add(id1);
                HashMap<Integer, PersonInterface> p1Map = p1.getAcquaintance();
                if (p1Map.size() < persons.size()) {
                    for (int id2 : p1Map.keySet()) {
                        if (!visited.contains(id2) && persons.containsKey(id2)) {
                            half += p1.queryValue(persons.get(id2));
                        }
                    }
                } else {
                    for (int id2 : persons.keySet()) {
                        if (!visited.contains(id2)) {
                            half += p1.queryValue(persons.get(id2));
                        }
                    }
                }
            }
            valueSum = 2 * half;
            dirty = false;
        }
        return valueSum; }

    @Override
    public int getAgeMean() {
        if (persons.isEmpty()) { return 0; }
        return ageSum / persons.size();
    }

    @Override
    public int getAgeVar() {
        if (persons.isEmpty()) { return 0; }
        int n = persons.size();
        int ave = getAgeMean();
        int var = n * ave * ave + agePowSum - 2 * ave * ageSum;
        return var / n;
    }

    public void updateValueSum() {
        dirty = true;
    }

    public void addSocialValue(int socialValue) {
        for (PersonInterface person : persons.values()) {
            person.addSocialValue(socialValue);
        }
    }

    public void addMoney(int money) {
        for (PersonInterface person : persons.values()) {
            person.addMoney(money);
        }
    }

    public void addArticle(int articleId) {
        for (PersonInterface person : persons.values()) {
            Person p = (Person) person;
            p.addReceived(articleId);
        }
    }

    public void addMessage(MessageInterface message) {
        for (PersonInterface person : persons.values()) {
            Person p = (Person) person;
            p.receiveMessage(message);
        }
    }
}
