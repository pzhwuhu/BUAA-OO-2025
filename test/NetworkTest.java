import com.oocourse.spec3.main.*;
import org.junit.Test;

import java.util.HashSet;
import java.util.Random;

import static org.junit.Assert.*;

public class NetworkTest {
    private final int TEST_CASES = 50;
    private final int PERSON_COUNT = 50;
    private final int EMOJI_COUNT = 50;
    private final int MESSAGE_COUNT = 1000;

    static class TestDataSet {
        Network network;
        Network networkCopy;
    }

    @Test
    public void testDeleteColdEmoji() throws Exception {
        for (int testCase = 0; testCase < TEST_CASES; testCase++) {
            int limit = testCase * 2; // 生成不同测试阈值

            // 准备测试数据
            TestDataSet data = prepareTestData();

            // 执行删除操作
            int result = data.network.deleteColdEmoji(limit);

            // 验证结果
            validateResult(data, limit, result);
        }
    }

    private TestDataSet prepareTestData() throws Exception {
        TestDataSet data = new TestDataSet();
        data.network = new Network();
        data.networkCopy = new Network();

        // 添加人员
        for (int i = 0; i < PERSON_COUNT; i++) {
            Person person = new Person(i, "person_" + i, 50);
            data.network.addPerson(person);
            data.networkCopy.addPerson(new Person(i, "person_" + i, 50));
        }

        // 初始化表情
        for (int i = 0; i < EMOJI_COUNT; i++) {
            data.network.storeEmojiId(i);
            data.networkCopy.storeEmojiId(i);
        }

        // 创建消息
        Random rand = new Random();
        for (int i = 0; i < MESSAGE_COUNT; i++) {
            createMessages(data, i, rand);
        }
        return data;
    }

    private void createMessages(TestDataSet data, int id, Random rand) throws Exception {
        int p1 = rand.nextInt(PERSON_COUNT);
        int p2 = rand.nextInt(PERSON_COUNT);
        while(p1 == p2) {
            p1 = rand.nextInt(PERSON_COUNT);
        }
        int emojiId = rand.nextInt(EMOJI_COUNT);

        // 建立关系
        if (!data.network.getPerson(p1).isLinked(data.network.getPerson(p2))) {
            data.network.addRelation(p1, p2, 1);
            data.networkCopy.addRelation(p1, p2, 1);
        }

        // 添加表情消息
        MessageInterface msg = new EmojiMessage(id, emojiId,
                data.network.getPerson(p1), data.network.getPerson(p2));
        data.network.addMessage(msg);
        data.networkCopy.addMessage(new EmojiMessage(id, emojiId,
                data.networkCopy.getPerson(p1), data.networkCopy.getPerson(p2)));

        // 随机发送消息
        if (rand.nextBoolean()) {
            data.network.sendMessage(id);
            data.networkCopy.sendMessage(id);
        }
    }

    private void validateResult(TestDataSet data, int limit, int result) {
        // 获取操作前后的数据
        int[] oldEmojiIds = data.networkCopy.getEmojiIdList();
        int[] oldEmojiHeats = data.networkCopy.getEmojiHeatList();
        int[] newEmojiIds = data.network.getEmojiIdList();
        int[] newEmojiHeats = data.network.getEmojiHeatList();

        // 验证保留的表情
        for (int i = 0; i < oldEmojiIds.length; i++) {
            int id = oldEmojiIds[i];
            int heat = oldEmojiHeats[i];

            if (heat >= limit) {
                assertTrue("Should retain emoji:" + id, contains(newEmojiIds, id));
            }
        }

        // 验证删除剩下的表情
        for (int i = 0; i < newEmojiIds.length; i++) {
            int id = newEmojiIds[i];
            int index = indexOf(oldEmojiIds, id);
            assertTrue("Invalid emoji found:" + id, index != -1);
            assertEquals("Heat value mismatch",
                    oldEmojiHeats[index], newEmojiHeats[i]);
        }

        // 验证消息过滤
        HashSet<Integer> remainingEmojis = new HashSet<>();
        for (int id : newEmojiIds) {
            remainingEmojis.add(id);
        }

        MessageInterface[] messages = data.network.getMessages();
        MessageInterface[] originalMessages = data.networkCopy.getMessages();

        for (MessageInterface msg : originalMessages) {
            boolean shouldExist = true;

            if (msg instanceof EmojiMessage) {
                int emojiId = ((EmojiMessage) msg).getEmojiId();
                shouldExist = remainingEmojis.contains(emojiId);
            }

            assertEquals("Message state error:" + msg.getId(),
                    shouldExist, containsMessage(messages, msg));
        }

        assertEquals("Result count mismatch", newEmojiIds.length, result);
    }

    private boolean containsMessage(MessageInterface[] array, MessageInterface target) {
        for (MessageInterface msg : array) {
            if (msg.getId() == target.getId()) {
                return true;
            }
        }
        return false;
    }

    private boolean contains(int[] array, int value) {
        for (int item : array) {
            if (item == value) {
                return true;
            }
        }
        return false;
    }

    private int indexOf(int[] array, int value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }
}
