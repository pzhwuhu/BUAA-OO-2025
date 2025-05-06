import com.oocourse.spec2.main.PersonInterface;
import com.oocourse.spec2.main.TagInterface;

import java.util.HashMap;

public class Tag implements TagInterface {
    private final int id;
    private int ageSum;
    private int agePowSum;
    private int valueSum = 0;
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
        updateValueSum();
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
        int sum = 0;
        for (PersonInterface p1 : persons.values()) {
            for (PersonInterface p2 : persons.values()) {
                if (p1.isLinked(p2)) {
                    sum += p1.queryValue(p2);
                }
            }
        }
        valueSum = sum;
    }
}
