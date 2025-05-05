import com.oocourse.spec2.exceptions.AcquaintanceNotFoundException;
import com.oocourse.spec2.exceptions.ArticleIdNotFoundException;
import com.oocourse.spec2.exceptions.ContributePermissionDeniedException;
import com.oocourse.spec2.exceptions.DeleteArticlePermissionDeniedException;
import com.oocourse.spec2.exceptions.DeleteOfficialAccountPermissionDeniedException;
import com.oocourse.spec2.exceptions.EqualArticleIdException;
import com.oocourse.spec2.exceptions.EqualOfficialAccountIdException;
import com.oocourse.spec2.exceptions.EqualPersonIdException;
import com.oocourse.spec2.exceptions.EqualRelationException;
import com.oocourse.spec2.exceptions.EqualTagIdException;
import com.oocourse.spec2.exceptions.OfficialAccountIdNotFoundException;
import com.oocourse.spec2.exceptions.PathNotFoundException;
import com.oocourse.spec2.exceptions.PersonIdNotFoundException;
import com.oocourse.spec2.exceptions.RelationNotFoundException;
import com.oocourse.spec2.exceptions.TagIdNotFoundException;
import com.oocourse.spec2.main.NetworkInterface;
import com.oocourse.spec2.main.PersonInterface;
import com.oocourse.spec2.main.TagInterface;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Network implements NetworkInterface {
    private final HashMap<Integer, PersonInterface> persons;
    private UnionFind unionFind;
    private Boolean unionDirty = false;
    private int tripleCount = 0;//三元环数量
    private int coupleCount = 0;//互为最好朋友的对数
    private HashMap<Integer, Integer> bestAcquaintanceMap = new HashMap<>();
    private final HashMap<Pair<Integer, Integer>, Integer> shortestPathCache = new HashMap<>();
    private HashMap<Integer, OfficialAccount> accounts = new HashMap<>();
    private final HashSet<Integer> articles = new HashSet<>(); // 全局文章ID集合
    private HashMap<Integer, Integer> articleContributors = new HashMap<>();

    public Network() {
        persons = new HashMap<>();
        unionFind = new UnionFind();
    }

    @Override
    public boolean containsPerson(int id) { return persons.containsKey(id); }

    @Override
    public boolean containsAccount(int id) { return accounts.containsKey(id); }

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
        updateCoupleCount(person1);
        updateCoupleCount(person2);
        shortestPathCache.clear();
    }

    public void updateCoupleCount(Person person) {
        int id = person.getId();
        int oldBest = bestAcquaintanceMap.getOrDefault(id, -1);
        int newBest = person.queryBestAcquaintance();
        // 旧关系失效
        if (oldBest != -1 && bestAcquaintanceMap.getOrDefault(oldBest, -1) == id) {
            coupleCount--;
        }
        // 更新最佳熟人
        bestAcquaintanceMap.put(id, newBest);
        // 新关系生效
        if (bestAcquaintanceMap.getOrDefault(newBest, -1) == id) {
            coupleCount++;
        }
    }

    @Override
    public int queryCoupleSum() { return coupleCount; }

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
            shortestPathCache.clear();
        }
        updateCoupleCount(person1);
        updateCoupleCount(person2);
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
    public int queryTagValueSum(int personId, int tagId)
        throws PersonIdNotFoundException, TagIdNotFoundException {
        if (!persons.containsKey(personId)) { throw new PersonIdNotFoundException(personId); }
        if (!persons.get(personId).containsTag(tagId)) { throw new TagIdNotFoundException(tagId); }

        return persons.get(personId).getTag(tagId).getValueSum();
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
        return person.queryBestAcquaintance();
    }

    public PersonInterface[] getPersons() {
        return persons.values().toArray(new PersonInterface[persons.size()]); }

    @Override
    public void createOfficialAccount(int personId, int accountId, String name)
        throws PersonIdNotFoundException, EqualOfficialAccountIdException {
        if (!containsPerson(personId)) { throw new PersonIdNotFoundException(personId); }
        if (accounts.containsKey(accountId)) {
            throw new EqualOfficialAccountIdException(accountId); }
        OfficialAccount account = new OfficialAccount(accountId, personId, name);
        account.addFollower(getPerson(personId)); // 拥有者自动关注
        accounts.put(accountId, account);
    }

    @Override
    public void deleteOfficialAccount(int personId, int accountId)
        throws PersonIdNotFoundException, OfficialAccountIdNotFoundException,
        DeleteOfficialAccountPermissionDeniedException {
        if (!containsPerson(personId)) { throw new PersonIdNotFoundException(personId); }
        if (!accounts.containsKey(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId); }
        OfficialAccount account = accounts.get(accountId);
        if (account.getOwnerId() != personId) {
            throw new DeleteOfficialAccountPermissionDeniedException(personId, accountId);
        }
        accounts.remove(accountId);
    }

    @Override
    public boolean containsArticle(int id) { return articles.contains(id); }

    @Override
    public void contributeArticle(int personId, int accountId, int articleId)
        throws PersonIdNotFoundException, OfficialAccountIdNotFoundException,
        EqualArticleIdException, ContributePermissionDeniedException {
        if (!containsPerson(personId)) { throw new PersonIdNotFoundException(personId); }
        if (!accounts.containsKey(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        }
        if (articles.contains(articleId)) { throw new EqualArticleIdException(articleId); }
        OfficialAccount account = accounts.get(accountId);
        PersonInterface person = persons.get(personId);
        if (!account.containsFollower(person)) {
            throw new ContributePermissionDeniedException(personId, articleId);
        }
        account.addArticle(person, articleId);
        articleContributors.put(articleId, personId);
        articles.add(articleId);
    }

    @Override
    public void deleteArticle(int personId, int accountId, int articleId)
        throws PersonIdNotFoundException, OfficialAccountIdNotFoundException,
        ArticleIdNotFoundException, DeleteArticlePermissionDeniedException {
        if (!containsPerson(personId)) { throw new PersonIdNotFoundException(personId); }
        if (!accounts.containsKey(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId); }
        OfficialAccount account = accounts.get(accountId);
        if (!account.containsArticle(articleId)) {
            throw new ArticleIdNotFoundException(articleId);
        }
        if (account.getOwnerId() != personId) {
            throw new DeleteArticlePermissionDeniedException(personId, articleId);
        }
        account.removeArticle(articleId);
    }

    @Override
    public void followOfficialAccount(int personId, int accountId)
        throws PersonIdNotFoundException,
        OfficialAccountIdNotFoundException, EqualPersonIdException {
        if (!containsPerson(personId)) { throw new PersonIdNotFoundException(personId); }
        if (!accounts.containsKey(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        }
        OfficialAccount account = accounts.get(accountId);
        PersonInterface person = persons.get(personId);
        if (account.containsFollower(person)) { throw new EqualPersonIdException(personId); }
        account.addFollower(getPerson(personId));
    }

    @Override
    public int queryBestContributor(int id) throws OfficialAccountIdNotFoundException {
        if (!accounts.containsKey(id)) {
            throw new OfficialAccountIdNotFoundException(id);
        }
        return accounts.get(id).getBestContributor();
    }

    @Override
    public List<Integer> queryReceivedArticles(int id) throws PersonIdNotFoundException {
        if (!containsPerson(id)) { throw new PersonIdNotFoundException(id); }
        PersonInterface person = persons.get(id);
        return person.queryReceivedArticles();
    }

    @Override
    public int queryShortestPath(int id1, int id2)
        throws PersonIdNotFoundException, PathNotFoundException {
        if (!containsPerson(id1)) { throw new PersonIdNotFoundException(id1); }
        if (!containsPerson(id2)) { throw new PersonIdNotFoundException(id2); }
        if (id1 == id2) { return 0; }

        Pair<Integer, Integer> key = getCacheKey(id1, id2);
        if (shortestPathCache.containsKey(key)) { return shortestPathCache.get(key); }
        if (!isCircle(id1, id2)) { throw new PathNotFoundException(id1, id2); }

        int pathLen = biDirectionalBfs(id1, id2);
        shortestPathCache.put(key, pathLen);
        return pathLen;
    }

    private Pair<Integer, Integer> getCacheKey(int id1, int id2) {
        return id1 < id2 ? new Pair<>(id1, id2) : new Pair<>(id2, id1);
    }

    private int biDirectionalBfs(int startId, int endId) {
        // 初始化两个方向的队列和距离映射
        HashMap<Integer, Integer> startDist = new HashMap<>();
        startDist.put(startId, 0);
        HashMap<Integer, Integer> endDist = new HashMap<>();
        endDist.put(endId, 0);
        Queue<Integer> startQueue = new LinkedList<>();
        Queue<Integer> endQueue = new LinkedList<>();
        startQueue.add(startId);
        endQueue.add(endId);

        while (!startQueue.isEmpty() && !endQueue.isEmpty()) {
            // 优先扩展较小的队列以优化性能
            int pathLen = expandQueue(startQueue, startDist, endDist, true);
            if (pathLen != -1) { return pathLen; }

            pathLen = expandQueue(endQueue, endDist, startDist, false);
            if (pathLen != -1) { return pathLen; }
        }
        return -1;
    }

    private int expandQueue(Queue<Integer> queue, HashMap<Integer, Integer> distMap,
        HashMap<Integer, Integer> otherDistMap, boolean isForward) {
        int size = queue.size();
        for (int i = 0; i < size; i++) {
            int current = queue.poll();
            int currentDist = distMap.get(current);

            Person person = (Person) persons.get(current);
            for (int neighborId : person.getAcquaintance().keySet()) {
                // 如果在对方距离映射中找到，返回总路径长度
                if (otherDistMap.containsKey(neighborId)) {
                    return currentDist + 1 + otherDistMap.get(neighborId);
                }

                // 更新当前方向的距离映射
                if (!distMap.containsKey(neighborId)) {
                    distMap.put(neighborId, currentDist + 1);
                    queue.add(neighborId);
                }
            }
        }
        return -1;
    }

}
