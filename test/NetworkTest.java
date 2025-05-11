import com.oocourse.spec3.main.MessageInterface;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import com.oocourse.spec3.main.PersonInterface;

import static org.junit.Assert.*;

import java.util.*;

public class NetworkTest {
    private Network network;
    private Network networkCopy;
    private Random rand = new Random();

    private void generateNetwork() {
        network = new Network();
        networkCopy = new Network();

        for (int i = 0; i < 50; i++) {
            try {
                network.addPerson(new Person(i, "name" + i, 50));
                networkCopy.addPerson(new Person(i, "name" + i, 50));
            } catch (Exception e) {
            }
        }

        int emojiSize = 20;
        for (int i = 0; i < emojiSize; i++) {
            try {
                network.storeEmojiId(i);
                networkCopy.storeEmojiId(i);
            } catch (Exception e) {
            }
        }

        int messageSize = 1000;

        for (int i = 0; i < messageSize; i++) {
            int person1 = rand.nextInt(50);
            int person2 = rand.nextInt(50);
            int emoji = rand.nextInt(emojiSize);
            try {
                network.addRelation(person1, person2, 1);
                networkCopy.addRelation(person1, person2, 1);
            } catch (Exception e) {

            }
            try {
                network.addMessage(new EmojiMessage(i, emoji, network.getPerson(person1), network.getPerson(person2)));
                networkCopy.addMessage(new EmojiMessage(i, emoji, networkCopy.getPerson(person1), networkCopy.getPerson(person2)));
                if (rand.nextBoolean()) {
                    network.sendMessage(i);
                    networkCopy.sendMessage(i);
                }
            } catch (Exception e) {
            }
        }

        

        for (int i = 0; i < messageSize / 10; i++) {
            int person1 = rand.nextInt(50);
            int person2 = rand.nextInt(50);
            try {
                network.addRelation(person1, person2, 1);
                networkCopy.addRelation(person1, person2, 1);
            } catch (Exception e) {

            }
            try {
                network.addMessage(new RedEnvelopeMessage(i + 500000, 111, network.getPerson(person1), network.getPerson(person2)));
                networkCopy.addMessage(new RedEnvelopeMessage(i + 500000, 111, networkCopy.getPerson(person1), networkCopy.getPerson(person2)));
            } catch (Exception e) {
            }
        }

        for (int i = 1000; i < messageSize * 2; i++) {
            int person1 = rand.nextInt(50);
            int person2 = rand.nextInt(50);
            int emoji = rand.nextInt(emojiSize);
            try {
                network.addRelation(person1, person2, 1);
                networkCopy.addRelation(person1, person2, 1);
            } catch (Exception e) {

            }
            try {
                network.addMessage(new EmojiMessage(i, emoji, network.getPerson(person1), network.getPerson(person2)));
                networkCopy.addMessage(new EmojiMessage(i, emoji, networkCopy.getPerson(person1), networkCopy.getPerson(person2)));
                if (rand.nextBoolean()) {
                    network.sendMessage(i);
                    networkCopy.sendMessage(i);
                }
            } catch (Exception e) {
            }
        }

    }

    private int[] convertToIntArray(ArrayList<Integer> list) {
        return list.stream().mapToInt(i -> i).toArray();
    }

    @org.junit.Test
    public void testDeleteColdEmoji() {
        int testSize = 100;
        for (int kk = 0; kk < testSize; kk++) {
            generateNetwork();
            int limit = kk * 3;
            int res = network.deleteColdEmoji(limit);
            int [] oldEmojiId = convertToIntArray(networkCopy.getEmojiIdList());
            int [] oldEmojiHeat = convertToIntArray(networkCopy.getEmojiHeatList());
            int [] emojiId = convertToIntArray(network.getEmojiIdList());
            int [] emojiHeat = convertToIntArray(network.getEmojiHeatList());
            MessageInterface[] messages = network.getMessages().toArray(new Message[0]);
            MessageInterface[] oldMessages = networkCopy.getMessages().toArray(new Message[0]);
            
            for (int i = 0; i < oldEmojiId.length; i++) {
                if (oldEmojiHeat[i] >= limit) {
                    boolean find = false;
                    for (int j = 0; j < emojiId.length; j++) {
                        if (emojiId[j] == oldEmojiId[i]) {
                            find = true;
                            break;
                        }
                    }
                    assertTrue(find);
                }
            }

            for (int i = 0; i < emojiId.length; i++) {
                boolean find = false;
                for (int j = 0; j < oldEmojiId.length; j++) {
                    if (emojiId[i] == oldEmojiId[j] && emojiHeat[i] == oldEmojiHeat[j]) {
                        find = true;
                        break;
                    }
                }
                assertTrue(find);
            }

            /*
            @ ensures emojiIdList.length ==
      @          (\numiof int i; 0 <= i && i < \old(emojiIdList.length); \old(emojiHeatList[i] >= limit));
             */
            int lenExp = 0;
            for (int i = 0; i < oldEmojiId.length; i++) {
                if (oldEmojiHeat[i] >= limit) {
                    lenExp++;
                }
            }
            assertEquals(lenExp, emojiId.length);

            /*
            @ ensures emojiIdList.length == emojiHeatList.length;
             */
            assertEquals(emojiId.length, emojiHeat.length);

            for (int i = 0; i < oldMessages.length; i++) {
                if (oldMessages[i] instanceof EmojiMessage && network.containsEmojiId(((EmojiMessage) oldMessages[i]).getEmojiId())) {
                    assertTrue(messageEquals(oldMessages[i], networkCopy.getMessage(oldMessages[i].getId())));
                    boolean find = false;
                    for (int j = 0; j < messages.length; j++) {
                        if ((messages[j].getId()) == (oldMessages[i].getId())) {
                            find = true;
                            break;
                        }
                    }
                    assertTrue(find);
                }
            }

            for (int i = 0; i < oldMessages.length; i++) {
                if (!(oldMessages[i] instanceof EmojiMessage)) {
                    assertTrue(messageEquals(oldMessages[i], networkCopy.getMessage(oldMessages[i].getId())));
                    boolean find = false;
                    for (int j = 0; j < messages.length; j++) {
                        if (messages[j].getId() == (oldMessages[i].getId())) {
                            find = true;
                            break;
                        }
                    }
                    assertTrue(find);
                }
            }
            
            int mesLenExp = 0;
            for (int i = 0; i < oldMessages.length; i++) {
                if (!(oldMessages[i] instanceof EmojiMessage)) {
                    mesLenExp++;
                } else if (network.containsEmojiId(((EmojiMessage) oldMessages[i]).getEmojiId())) {
                    mesLenExp++;
                }
            }
            assertEquals(mesLenExp, messages.length);
            assertEquals(res, emojiId.length);


        }

    }

    private boolean messageEquals(MessageInterface m1, MessageInterface m2) {
        if (m1 == null || m2 == null) {
            return false;
        }
        return m1.getId() == m2.getId()
                && (m1.getTag() == null || (m1.getTag() != null && m1.getTag().equals(m2.getTag())))
                && m1.getPerson1().equals(m2.getPerson1())
                && (m1.getPerson2() == null || (m1.getPerson2() != null && m1.getPerson2().equals(m2.getPerson2())))
                && m1.getType() == m2.getType();
    }
}