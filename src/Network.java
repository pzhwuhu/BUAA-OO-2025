import com.oocourse.spec1.exceptions.AcquaintanceNotFoundException;
import com.oocourse.spec1.exceptions.EqualPersonIdException;
import com.oocourse.spec1.exceptions.EqualRelationException;
import com.oocourse.spec1.exceptions.EqualTagIdException;
import com.oocourse.spec1.exceptions.PersonIdNotFoundException;
import com.oocourse.spec1.exceptions.RelationNotFoundException;
import com.oocourse.spec1.exceptions.TagIdNotFoundException;
import com.oocourse.spec1.main.NetworkInterface;
import com.oocourse.spec1.main.PersonInterface;
import com.oocourse.spec1.main.TagInterface;

import java.util.ArrayList;
import java.util.HashMap;

public class Network implements NetworkInterface {
    private final HashMap<Integer, PersonInterface> persons;
    private UnionFind unionFind;
    private Boolean unionDirty = false;
    private int tripleCount = 0;

    public Network() {
        persons = new HashMap<>();
        unionFind = new UnionFind();
    }

    @Override
    public boolean containsPerson(int id) { return persons.containsKey(id); }

    @Override
    public PersonInterface getPerson(int id) {
        if (persons.containsKey(id)) {
            return persons.get(id);
        }
        return null;
    }

    @Override
    public void addPerson(PersonInterface person) throws EqualPersonIdException {
        int id = person.getId();
        if (persons.containsKey(id)) {
            throw new EqualPersonIdException(id);
        } else {
            persons.put(id, person);
            unionFind.addNode(id);
        }
    }

    @Override
    public void addRelation(int id1, int id2, int value)
        throws PersonIdNotFoundException, EqualRelationException {
        if (!persons.containsKey(id1)) { throw new PersonIdNotFoundException(id1); }
        if (!persons.containsKey(id2)) { throw new PersonIdNotFoundException(id2); }
        if (persons.get(id1).isLinked(persons.get(id2))) {
            throw new EqualRelationException(id1, id2); }

        Person person1 = (Person)persons.get(id1);
        Person person2 = (Person)persons.get(id2);
        person1.addRelation(person2, value);
        person2.addRelation(person1, value);
        tripleCount += countCommonNeighbors(person1, person2);
        unionFind.merge(id1, id2);
    }

    @Override
    public void modifyRelation(int id1, int id2, int value)
        throws PersonIdNotFoundException, EqualPersonIdException, RelationNotFoundException {
        if (!persons.containsKey(id1)) { throw new PersonIdNotFoundException(id1); }
        if (!persons.containsKey(id2)) { throw new PersonIdNotFoundException(id2); }
        if (id1 == id2) { throw new EqualPersonIdException(id1); }
        if (!persons.get(id1).isLinked(persons.get(id2))) {
            throw new RelationNotFoundException(id1, id2); }

        Person person1 = (Person)persons.get(id1);
        Person person2 = (Person)persons.get(id2);
        int oldValue = person1.queryValue(person2);
        int newValue = oldValue + value;

        if (newValue > 0) {
            person1.modifyRelation(person2, newValue);
            person2.modifyRelation(person1, newValue);
        } else {
            person1.delRelation(person2);
            person2.delRelation(person1);
            tripleCount -= countCommonNeighbors(person1, person2);
            unionDirty = true;
        }
    }

    @Override
    public int queryValue(int id1, int id2)
        throws PersonIdNotFoundException, RelationNotFoundException {
        if (!persons.containsKey(id1)) { throw new PersonIdNotFoundException(id1); }
        if (!persons.containsKey(id2)) { throw new PersonIdNotFoundException(id2); }
        if (!persons.get(id1).isLinked(persons.get(id2))) {
            throw new RelationNotFoundException(id1, id2); }

        Person person1 = (Person)persons.get(id1);
        Person person2 = (Person)persons.get(id2);
        return person1.queryValue(person2);
    }

    @Override
    public boolean isCircle(int id1, int id2) throws PersonIdNotFoundException {
        if (!persons.containsKey(id1)) { throw new PersonIdNotFoundException(id1); }
        if (!persons.containsKey(id2)) { throw new PersonIdNotFoundException(id2); }
        ArrayList<Integer> visited = new ArrayList<>();

        if (unionDirty) {
            reMakeUnion();
            unionDirty = false;
        }
        return unionFind.find(id1) == unionFind.find(id2);
    }

    public void reMakeUnion() {
        unionFind = new UnionFind();
        for (Integer id : persons.keySet()) { unionFind.addNode(id); }
        for (PersonInterface p : persons.values()) {
            for (PersonInterface neighbor : ((Person) p).getAcquaintance().values()) {
                int pid = p.getId();
                int nid = neighbor.getId();
                if (pid < nid) { // 避免重复合并
                    unionFind.merge(pid, nid);
                }
            }
        }
    }

    public boolean dfs(int begin, int end, ArrayList<Integer> visited) {
        if (begin == end) {
            return true;
        }
        visited.add(begin);
        Person current = (Person) persons.get(begin);
        for (Integer linkedId: current.getAcquaintance().keySet()) {
            if (!visited.contains(linkedId)) {
                if (dfs(linkedId, end, visited)) {
                    return true;
                }
            }
        }
        return false;
    }

    //计算共同好友
    private int countCommonNeighbors(Person p1, Person p2) {
        HashMap<Integer, PersonInterface> neighbors1 = p1.getAcquaintance();
        HashMap<Integer, PersonInterface> neighbors2 = p2.getAcquaintance();
        int count = 0;
        for (int id : neighbors1.keySet()) {
            if (neighbors2.containsKey(id)) { count++; }
        }
        return count;
    }

    @Override
    public int queryTripleSum() { return tripleCount; }

    @Override
    public void addTag(int personId, TagInterface tag)
        throws PersonIdNotFoundException, EqualTagIdException {
        if (!persons.containsKey(personId)) { throw new PersonIdNotFoundException(personId); }
        if (persons.get(personId).containsTag(tag.getId())) {
            throw new EqualTagIdException(tag.getId()); }

        persons.get(personId).addTag(tag);
    }

    @Override
    public void addPersonToTag(int personId1, int personId2, int tagId) throws
        PersonIdNotFoundException, RelationNotFoundException,
        TagIdNotFoundException, EqualPersonIdException {
        if (!persons.containsKey(personId1)) { throw new PersonIdNotFoundException(personId1); }
        if (!persons.containsKey(personId2)) { throw new PersonIdNotFoundException(personId2); }
        if (personId1 == personId2) { throw new EqualPersonIdException(personId1); }
        Person p1 = (Person)persons.get(personId1);
        Person p2 = (Person)persons.get(personId2);
        if (!p1.isLinked(p2)) { throw new RelationNotFoundException(personId1, personId2); }
        if (!p2.containsTag(tagId)) { throw new TagIdNotFoundException(tagId); }
        if (p2.getTag(tagId).hasPerson(p1)) { throw new EqualPersonIdException(personId1); }

        Tag tag2 = (Tag)p2.getTag(tagId);
        if (tag2.getSize() <= 999) { tag2.addPerson(p1); }
    }

    @Override
    public int queryTagAgeVar(int personId, int tagId)
        throws PersonIdNotFoundException, TagIdNotFoundException {
        if (!persons.containsKey(personId)) { throw new PersonIdNotFoundException(personId); }
        if (!persons.get(personId).containsTag(tagId)) { throw new TagIdNotFoundException(tagId); }

        return persons.get(personId).getTag(tagId).getAgeVar();
    }

    @Override
    public void delPersonFromTag(int personId1, int personId2, int tagId)
        throws PersonIdNotFoundException, TagIdNotFoundException {
        if (!persons.containsKey(personId1)) { throw new PersonIdNotFoundException(personId1); }
        if (!persons.containsKey(personId2)) { throw new PersonIdNotFoundException(personId2); }
        Person p1 = (Person)persons.get(personId1);
        Person p2 = (Person)persons.get(personId2);
        if (!p2.containsTag(tagId)) { throw new TagIdNotFoundException(tagId); }
        if (!p2.getTag(tagId).hasPerson(p1)) { throw new PersonIdNotFoundException(personId1); }

        p2.getTag(tagId).delPerson(p1);
    }

    @Override
    public void delTag(int personId, int tagId)
        throws PersonIdNotFoundException, TagIdNotFoundException {
        if (!persons.containsKey(personId)) { throw new PersonIdNotFoundException(personId); }
        if (!persons.get(personId).containsTag(tagId)) { throw new TagIdNotFoundException(tagId); }

        persons.get(personId).delTag(tagId);
    }

    @Override
    public int queryBestAcquaintance(int id)
        throws PersonIdNotFoundException, AcquaintanceNotFoundException {
        if (!persons.containsKey(id)) { throw new PersonIdNotFoundException(id); }
        Person person = (Person)persons.get(id);
        if (person.getAcquaintance().isEmpty()) { throw new AcquaintanceNotFoundException(id); }

        int bestValue = -1;
        int bestId = 0;
        HashMap<Integer, Integer> values = person.getValues();
        for (PersonInterface bro: person.getAcquaintance().values()) {
            if (values.get(bro.getId()) > bestValue
                || (values.get(bro.getId()) == bestValue && bro.getId() < bestId)) {
                bestValue = values.get(bro.getId());
                bestId = bro.getId();
            }
        }
        return bestId;
    }

    public PersonInterface[] getPersons() {
        return persons.values().toArray(new PersonInterface[persons.size()]); }
}
