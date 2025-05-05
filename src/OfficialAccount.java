import com.oocourse.spec2.main.OfficialAccountInterface;
import com.oocourse.spec2.main.PersonInterface;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class OfficialAccount implements OfficialAccountInterface {
    private final int ownerId;
    private int id;
    private String name;
    private HashMap<Integer, PersonInterface> followers = new HashMap<>(); // Key: personId
    private HashMap<Integer, Integer> contributions = new HashMap<>();
    private HashSet<Integer> articles = new HashSet<>(); // 存储文章ID
    private HashMap<Integer, Integer> articleContributors = new HashMap<>();

    public OfficialAccount(int id, int ownerId, String name) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
    }

    @Override
    public int getOwnerId() { return ownerId; }

    @Override
    public void addFollower(PersonInterface person) {
        int personId = person.getId();
        followers.put(personId, person);
        contributions.put(personId, 0);
    }

    @Override
    public boolean containsFollower(PersonInterface person) {
        return followers.containsKey(person.getId());
    }

    @Override
    public void addArticle(PersonInterface person, int articleId) {
        if (!articles.contains(articleId)) {
            articles.add(articleId);
            int contributorId = person.getId();
            articleContributors.put(articleId, contributorId);
            contributions.put(contributorId, contributions.get(contributorId) + 1);
            for (PersonInterface follower : followers.values()) {
                Person p = (Person) follower;
                p.addReceived(articleId);
            }
        }
    }

    @Override
    public boolean containsArticle(int id) { return articles.contains(id); }

    @Override
    public void removeArticle(int id) {
        if (articles.contains(id)) {
            int contributorId = articleContributors.get(id);
            contributions.put(contributorId, contributions.get(contributorId) - 1);
            articles.remove(id);
            articleContributors.remove(id);
            for (PersonInterface follower : followers.values()) {
                Person p = (Person) follower;
                p.removeReceived(id);
            }
        }
    }

    @Override
    public int getBestContributor() {
        int bestId = -1;
        int maxContribution = -1;
        for (Map.Entry<Integer, Integer> entry : contributions.entrySet()) {
            int currentId = entry.getKey();
            int currentContribution = entry.getValue();
            if (currentContribution > maxContribution ||
                (currentContribution == maxContribution && currentId < bestId)) {
                maxContribution = currentContribution;
                bestId = currentId;
            }
        }
        return bestId;
    }

}
