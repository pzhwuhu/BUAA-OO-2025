import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import com.oocourse.spec2.main.PersonInterface;

import static org.junit.Assert.*;

import java.util.*;

@RunWith(Parameterized.class)
public class queryCoupleSumTest {
    private final TestCase testCase;

    public queryCoupleSumTest(TestCase testCase) {
        this.testCase = testCase;
    }

    /* 测试用例容器 */
    static class TestCase {
        final Network originalNetwork;  // 原始未执行方法的网络
        final Network processedNetwork; // 执行过方法的网络
        final int expected;             // 预期结果（基于原始网络计算）

        TestCase(Network original, Network processed, int expected) {
            this.originalNetwork = original;
            this.processedNetwork = processed;
            this.expected = expected;
        }
    }

    @Parameterized.Parameters
    public static Collection<Object[]> prepareData() throws Exception {
        final int TEST_CASE_NUM = 200;
        Object[][] testCases = new Object[TEST_CASE_NUM][];
        Random rand = new Random(System.currentTimeMillis());

        for (int i = 0; i < TEST_CASE_NUM; i++) {
            TestCase testCase;
            switch (i % 9) {
                case 0:
                    testCase = emptyNetwork();
                    break;
                case 1:
                    testCase = singleCouple();
                    break;
                case 2:
                    testCase = multipleCouples(rand.nextInt(12) + 40);
                    break;
                case 3:
                    testCase = zeroMap(rand.nextInt(12) + 40);
                    break;
                case 4:
                    testCase = noCoupleChain(rand.nextInt(12) + 40);
                    break;
                case 5:
                    testCase = completeGraphCouples(rand.nextInt(20) + 20);
                    break;
                case 6:
                case 7:
                case 8:
                    testCase = randomCouples(rand.nextInt(100) + 10);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + i % 5);
            }
            testCases[i] = new Object[]{testCase};
        }

        return Arrays.asList(testCases);
    }

    @Test
    public void testQueryCoupleSum() throws Exception {
        int actual = testCase.processedNetwork.queryCoupleSum();
        assertEquals("Couple count mismatch", testCase.expected, actual);
        verifyStateUnchanged(testCase.originalNetwork, testCase.processedNetwork);
    }

    /* 状态验证方法（与原始测试相同） */
    public void verifyStateUnchanged(Network original, Network processed) {
        PersonInterface[] originalPersons = original.getPersons();
        PersonInterface[] processedPersons = processed.getPersons();
        assertEquals("Person count changed", originalPersons.length, processedPersons.length);
        for (int i = 0; i < originalPersons.length; i++) {
            assertTrue("Person state changed",
                    ((Person) originalPersons[i]).strictEquals((Person) processedPersons[i])
            );
        }
    }

    /* 新的测试用例生成方法 */
    public static TestCase emptyNetwork() throws Exception {
        Network original = new Network();
        Network processed = new Network();
        return new TestCase(original, processed, 0);
    }

    // 单个互为最好朋友的对
    public static TestCase singleCouple() throws Exception {
        Network original = new Network();
        Network processed = new Network();
        addPerson(original, 1);
        addPerson(original, 2);
        original.addRelation(1, 2, 10); // 确保互为最高
        addPerson(processed, 1);
        addPerson(processed, 2);
        processed.addRelation(1, 2, 10);
        return new TestCase(original, processed, 1);
    }

    // 多个独立的互为对
    public static TestCase multipleCouples(int count) throws Exception {
        Network original = new Network();
        Network processed = new Network();
        int baseId = 1;

        for (int i = 0; i < count; i++) {
            int id1 = baseId++;
            int id2 = baseId++;
            addPerson(original, id1);
            addPerson(original, id2);
            original.addRelation(id1, id2, 10); // 每对独立
            addPerson(processed, id1);
            addPerson(processed, id2);
            processed.addRelation(id1, id2, 10);
        }

        return new TestCase(original, processed, count);
    }

    // 只有节点没有边
    public static TestCase zeroMap(int num) throws Exception {
        Network original = new Network();
        Network processed = new Network();
        for(int i=1;i<=num;i++) {
            addPerson(original, i);
            addPerson(processed, i);
        }

        return new TestCase(original, processed, 0);
    }

    // 没有互为对的链状结构
    public static TestCase noCoupleChain(int length) throws Exception {
        Network original = new Network();
        Network processed = new Network();
        int prevId = 1;
        addPerson(original, prevId);
        addPerson(processed, prevId);

        for (int i = 2; i <= length; i++) {
            addPerson(original, i);
            original.addRelation(prevId, i, i); // 权重递增，确保单方向选择
            addPerson(processed, i);
            processed.addRelation(prevId, i, i);
            prevId = i;
        }

        return new TestCase(original, processed, 1);
    }

    // 完全图中的互为对（需要特殊设置权重）
    public static TestCase completeGraphCouples(int nodeCount) throws Exception {
        Network original = new Network();
        Network processed = new Network();
        Random rand = new Random();

        // 添加节点
        for (int i = 1; i <= nodeCount; i++) {
            addPerson(original, i);
            addPerson(processed, i);
        }

        // 创建环形最高权重关系
        for (int i = 1; i <= nodeCount; i++) {
            int j = (i % nodeCount) + 1;
            original.addRelation(i, j, 100); // 确保每对相邻节点互为最好朋友
            processed.addRelation(i, j, 100);
        }

        return new TestCase(original, processed, 1); // 环形结构形成nodeCount对
    }

    // 随机生成互为对
    public static TestCase randomCouples(int personNum) throws Exception {
        Network original = new Network();
        Network processed = new Network();
        Set<Integer> validIds = new HashSet<>();
        Random rand = new Random();

        // 生成节点
        for (int i = 0; i < personNum; i++) {
            int id = rand.nextInt(1000);
            while (validIds.contains(id)) {
                id = rand.nextInt(1000);
            }
            validIds.add(id);
            addPerson(original, id);
            addPerson(processed, id);
        }

        // 生成随机互为对
        ArrayList<Integer> idList = new ArrayList<>(validIds);
        int expectedCount = 0;
        while (idList.size() >= 2) {
            int idx1 = rand.nextInt(idList.size());
            int id1 = idList.get(idx1);
            idList.remove(idx1);

            int idx2 = rand.nextInt(idList.size());
            int id2 = idList.get(idx2);
            idList.remove(idx2);

            // 添加双向最高权重关系
            original.addRelation(id1, id2, 100);
            processed.addRelation(id1, id2, 100);
            expectedCount++;
        }

        return new TestCase(original, processed, expectedCount);
    }

    /* 工具方法（保持与原始测试相同） */
    public static void addPerson(Network network, int id) throws Exception {
        if (!network.containsPerson(id)) {
            network.addPerson(new Person(id, "Person"+id, 20));
        }
    }
}
