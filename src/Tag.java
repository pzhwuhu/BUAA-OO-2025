import utils.PersonInterface;
import utils.TagInterface;

import java.util.HashMap;

public class Tag implements TagInterface {
    private final int id;
    private int ageSum;
    private int agePowSum;
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
    }

    @Override
    public boolean hasPerson(PersonInterface person) { return persons.containsValue(person); }

    @Override
    public void delPerson(PersonInterface person) {
        ageSum -= person.getAge();
        agePowSum -= person.getAge() * person.getAge();
        persons.remove(person.getId());
    }

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
        //System.out.println("var = " + var + ", ave = " + ave + ",
        // ageSum = " + ageSum + ", agePowSum = " + agePowSum);
        return var / n;
    }
}
