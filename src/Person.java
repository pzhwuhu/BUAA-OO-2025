import com.oocourse.spec1.main.PersonInterface;
import com.oocourse.spec1.main.TagInterface;
import java.util.HashMap;

public class Person implements PersonInterface {
    private final int id;
    private final String name;
    private final int age;
    private HashMap<Integer, PersonInterface> acquaintance = new HashMap<>();
    private HashMap<Integer, Integer> values = new HashMap<>();
    private HashMap<Integer, TagInterface> tags = new HashMap<>();

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

    /// //////////////////////////////////////////////////////////////
    public void addRelation(Person person, int value) {
        acquaintance.put(person.getId(), person);
        values.put(person.getId(), value);
    }

    public void modifyRelation(Person person, int newValue) {
        values.put(person.getId(), newValue);
    }

    public void delRelation(Person person) {
        int delId = person.getId();
        acquaintance.remove(delId);
        values.remove(delId);
        for (TagInterface tag : tags.values()) {
            if (tag.hasPerson(person)) { tag.delPerson(person); }
        }
    }

    public HashMap<Integer, PersonInterface> getAcquaintance() { return acquaintance; }

    public HashMap<Integer, Integer> getValues() { return values; }
}
