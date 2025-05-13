import com.oocourse.spec3.exceptions.AcquaintanceNotFoundException;
import com.oocourse.spec3.exceptions.ArticleIdNotFoundException;
import com.oocourse.spec3.exceptions.ContributePermissionDeniedException;
import com.oocourse.spec3.exceptions.DeleteArticlePermissionDeniedException;
import com.oocourse.spec3.exceptions.DeleteOfficialAccountPermissionDeniedException;
import com.oocourse.spec3.exceptions.EmojiIdNotFoundException;
import com.oocourse.spec3.exceptions.EqualArticleIdException;
import com.oocourse.spec3.exceptions.EqualEmojiIdException;
import com.oocourse.spec3.exceptions.EqualMessageIdException;
import com.oocourse.spec3.exceptions.EqualOfficialAccountIdException;
import com.oocourse.spec3.exceptions.EqualPersonIdException;
import com.oocourse.spec3.exceptions.EqualRelationException;
import com.oocourse.spec3.exceptions.EqualTagIdException;
import com.oocourse.spec3.exceptions.MessageIdNotFoundException;
import com.oocourse.spec3.exceptions.OfficialAccountIdNotFoundException;
import com.oocourse.spec3.exceptions.PathNotFoundException;
import com.oocourse.spec3.exceptions.PersonIdNotFoundException;
import com.oocourse.spec3.exceptions.RelationNotFoundException;
import com.oocourse.spec3.exceptions.TagIdNotFoundException;
import com.oocourse.spec3.main.MessageInterface;
import com.oocourse.spec3.main.NetworkInterface;
import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.TagInterface;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class Network implements NetworkInterface {
    private final HashMap<Integer, PersonInterface> persons;
    private UnionFind unionFind;
    private Boolean unionDirty = false;
    private int tripleCount = 0;//三元环数量
    private final HashMap<Pair<Integer, Integer>, Integer> shortestPathCache = new HashMap<>();
    private HashMap<Integer, OfficialAccount> accounts = new HashMap<>();
    private HashMap<String, Tag> allTags = new HashMap<>();
    private final HashSet<Integer> articles = new HashSet<>(); // 全局文章ID集合
    private HashMap<Integer, Integer> articleContributors = new HashMap<>();
    private HashMap<Integer, MessageInterface> messages = new HashMap<>(); //全局消息
    private HashMap<Integer, Integer> emojiIdMap = new HashMap<>();

    public Network() {
        persons = new HashMap<>();
        unionFind = new UnionFind();
    }

    @Override
    public boolean containsMessage(int id) { return messages.containsKey(id); }

    @Override
    public void addMessage(MessageInterface message) throws EqualMessageIdException,
        EmojiIdNotFoundException, EqualPersonIdException, ArticleIdNotFoundException {
        int id = message.getId();
        if (messages.containsKey(id)) { throw new EqualMessageIdException(id); }
        if (message instanceof EmojiMessage) {
            int emojiId = ((EmojiMessage) message).getEmojiId();
            if (!containsEmojiId(emojiId)) { throw new EmojiIdNotFoundException(emojiId); }
        }
        else if (message instanceof ForwardMessage) {
            int articleId = ((ForwardMessage) message).getArticleId();
            if (!articles.contains(articleId)) { throw new ArticleIdNotFoundException(articleId); }
            PersonInterface person1 = message.getPerson1();
            if (!person1.getReceivedArticles().contains(articleId)) {
                throw new ArticleIdNotFoundException(articleId); }
        }
        if (message.getType() == 0 && message.getPerson1().equals(message.getPerson2())) {
            throw new EqualPersonIdException(message.getPerson1().getId());
        }
        messages.put(id, message);
    }

    @Override
    public MessageInterface getMessage(int id) { return messages.get(id); }

    public MessageInterface[] getMessages() {
        MessageInterface[] messageArray = new MessageInterface[messages.size()];
        messageArray = messages.values().toArray(messageArray);
        return messageArray;
    }

    public int[] getEmojiIdList() { return null; }

    public int[] getEmojiHeatList() { return null; }

    @Override
    public void sendMessage(int id) throws RelationNotFoundException,
        MessageIdNotFoundException, TagIdNotFoundException {
        if (!containsMessage(id)) { throw new MessageIdNotFoundException(id); }
        Message message = (Message) messages.get(id);
        int type = message.getType();
        if (type == 0) {
            Person p1 = (Person) message.getPerson1();
            Person p2 = (Person) message.getPerson2();
            if (!p1.isLinked(p2)) { throw new RelationNotFoundException(p1.getId(), p2.getId()); }
            if (p1.equals(p2)) { return; }

            int socialValue = message.getSocialValue();
            p1.addSocialValue(socialValue);
            p2.addSocialValue(socialValue);
            p2.receiveMessage(message);

            if (message instanceof RedEnvelopeMessage) {
                int money = ((RedEnvelopeMessage) message).getMoney();
                p1.addMoney(-money);
                p2.addMoney(money);
            }
            else if (message instanceof EmojiMessage) {
                int emojiId = ((EmojiMessage) message).getEmojiId();
                if (containsEmojiId(emojiId)) {
                    emojiIdMap.put(emojiId, emojiIdMap.get(emojiId) + 1);
                }
            } else if (message instanceof ForwardMessage) {
                int articleId = ((ForwardMessage) message).getArticleId();
                p2.addReceived(articleId);
            }
        } else if (type == 1) {
            TagInterface tag = message.getTag();
            PersonInterface sender = message.getPerson1();
            if (!sender.containsTag(tag.getId())) { throw new TagIdNotFoundException(tag.getId()); }

            int socialValue = message.getSocialValue();
            sender.addSocialValue(socialValue);
            ((Tag) tag).addSocialValue(socialValue);
            ((Tag) tag).addMessage(message);

            if (message instanceof RedEnvelopeMessage) {
                if (tag.getSize() > 0) {
                    int totalMoney = ((RedEnvelopeMessage) message).getMoney();
                    int average = totalMoney / tag.getSize();
                    sender.addMoney(- average * tag.getSize());
                    ((Tag) tag).addMoney(average);
                }
            } else if (message instanceof EmojiMessage) {
                int emojiId = ((EmojiMessage) message).getEmojiId();
                if (containsEmojiId(emojiId)) {
                    emojiIdMap.put(emojiId, emojiIdMap.get(emojiId) + 1);
                }
            } else if (message instanceof ForwardMessage) {
                int articleId = ((ForwardMessage) message).getArticleId();
                ((Tag) tag).addArticle(articleId);
            }
        }
        messages.remove(id); // Remove message after processing
    }

    @Override
    public int querySocialValue(int id) throws PersonIdNotFoundException {
        if (!containsPerson(id)) { throw new PersonIdNotFoundException(id); }
        return persons.get(id).getSocialValue();
    }

    @Override
    public List<MessageInterface> queryReceivedMessages(int id) throws PersonIdNotFoundException {
        if (!containsPerson(id)) { throw new PersonIdNotFoundException(id); }
        return persons.get(id).getReceivedMessages();
    }

    @Override
    public boolean containsEmojiId(int id) { return emojiIdMap.containsKey(id); }

    @Override
    public void storeEmojiId(int id) throws EqualEmojiIdException {
        if (containsEmojiId(id)) { throw new EqualEmojiIdException(id); }
        emojiIdMap.put(id, 0);
    }

    @Override
    public int queryMoney(int id) throws PersonIdNotFoundException {
        if (!containsPerson(id)) { throw new PersonIdNotFoundException(id); }
        return getPerson(id).getMoney();
    }

    @Override
    public int queryPopularity(int id) throws EmojiIdNotFoundException {
        if (!containsEmojiId(id)) { throw new EmojiIdNotFoundException(id); }
        return emojiIdMap.get(id);
    }

    @Override
    public int deleteColdEmoji(int limit) {
        HashMap<Integer, Integer> remainingEmojis = new HashMap<>();
        Set<Integer> removedEmojis = new HashSet<>();

        for (Map.Entry<Integer, Integer> entry : emojiIdMap.entrySet()) {
            int emojiId = entry.getKey();
            int heat = entry.getValue();

            if (heat >= limit) {
                remainingEmojis.put(emojiId, heat);
            } else {
                removedEmojis.add(emojiId);
            }
        }

        emojiIdMap = remainingEmojis;

        // 过滤消息，删除关联了被移除表情的消息
        Iterator<Map.Entry<Integer, MessageInterface>> it = messages.entrySet().iterator();
        while (it.hasNext()) {
            MessageInterface msg = it.next().getValue();
            if (msg instanceof EmojiMessage &&
                removedEmojis.contains(((EmojiMessage) msg).getEmojiId())) {
                it.remove();
            }
        }

        return emojiIdMap.size();
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
        shortestPathCache.clear();
        updateValueSum(person1, person2);
    }

    public void updateValueSum(Person p1, Person p2) {
        for (Tag tag : allTags.values()) {
            if (tag.hasPerson(p1) && tag.hasPerson(p2)) {
                tag.updateValueSum();
            }
        }
    }

    @Override
    public int queryCoupleSum() {
        int num = 0;
        ArrayList<PersonInterface> personList = new ArrayList<>(persons.values());
        for (PersonInterface personInterface : personList) {
            Person p1 = (Person) personInterface;
            if (!p1.getAcquaintance().isEmpty()) {
                int best1 = p1.queryBestAcquaintance();
                Person p2 = (Person) persons.get(best1);
                if (!p2.getAcquaintance().isEmpty()) {
                    if (p2.queryBestAcquaintance() == p1.getId()) {
                        num++;
                    }
                }
            }
        }
        return num / 2; }

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
        updateValueSum(person1, person2);
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
        allTags.put(personId + "-" + tag.getId(), (Tag) tag);
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

        //Pair<Integer, Integer> key = getCacheKey(id1, id2);
        //if (shortestPathCache.containsKey(key)) { return shortestPathCache.get(key); }
        if (!isCircle(id1, id2)) { throw new PathNotFoundException(id1, id2); }

        int pathLen = biDirectionalBfs(id1, id2);
        //shortestPathCache.put(key, pathLen);
        return pathLen;
    }

    private Pair<Integer, Integer> getCacheKey(int id1, int id2) {
        return id1 < id2 ? new Pair<>(id1, id2) : new Pair<>(id2, id1);
    }

    private int bfs(int startId, int endId) {
        HashMap<Integer, Integer> distance = new HashMap<>();
        distance.put(startId, 0);

        Queue<Integer> queue = new LinkedList<>();
        queue.add(startId);

        while (!queue.isEmpty()) {
            int currentId = queue.poll();
            int currentDist = distance.get(currentId);

            if (currentId == endId) { return currentDist; }

            Person person = (Person) persons.get(currentId);
            for (int neighborId : person.getAcquaintance().keySet()) {
                if (!distance.containsKey(neighborId)) {
                    distance.put(neighborId, currentDist + 1);
                    queue.add(neighborId);
                }
            }
        }
        return -1;
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
