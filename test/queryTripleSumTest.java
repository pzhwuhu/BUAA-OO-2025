import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import com.oocourse.spec1.main.PersonInterface;

import static org.junit.Assert.*;

import java.util.*;

@RunWith(Parameterized.class)
public class queryTripleSumTest {
    private final TestCase testCase;

    public queryTripleSumTest(TestCase testCase) {
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
        final int TEST_CASE_NUM = 1000;
        // 创建一个二维数组，用于存储测试数据
        Object[][] testCases = new Object[TEST_CASE_NUM][];
        Random rand = new Random(System.currentTimeMillis());

        // 生成多种测试场景
        for (int i = 0; i < TEST_CASE_NUM; i++) {
            TestCase testCase;
            switch (i % 5) {
                case 0:
                    testCase = emptyNetwork();
                    break;
                case 1:
                    testCase = singleTriangle();
                    break;
                case 2:
                    testCase = multipleTriangles(rand.nextInt(12) + 200);
                    break;
                case 3:
                    testCase = overlappingTriangles(rand.nextInt(8) + 200);
                    break;
                case 4:
                    testCase = noTriangleChain(rand.nextInt(12) + 200);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + i % 5);
            }
            // 将 TestCase 对象存储到二维数组中
            testCases[i] = new Object[]{testCase};
        }

        return Arrays.asList(testCases);
    }

    @Test
    public void testQueryTripleSum() throws Exception {
        // 执行测试方法
        int actual = testCase.processedNetwork.queryTripleSum();

        // 验证结果正确性
        assertEquals("Triple count mismatch", testCase.expected, actual);

        // 验证状态不变性（比较原始网络和处理后的网络）
        verifyStateUnchanged(testCase.originalNetwork, testCase.processedNetwork);
    }

    /* 状态验证方法 */
    public void verifyStateUnchanged(Network original, Network processed) {
        PersonInterface[] originalPersons = original.getPersons();
        PersonInterface[] processedPersons = processed.getPersons();
        assertEquals("Person count changed", originalPersons.length, processedPersons.length);
        for (int i = 0; i < originalPersons.length; i++) {
            // 假设 Person 类实现了严格相等检查方法 strictEquals()
            assertTrue("Person state changed",
                    ((Person) originalPersons[i]).strictEquals((Person) processedPersons[i])
            );
        }
    }

    /* 测试用例生成方法 */
    public static TestCase emptyNetwork() throws Exception {
        Network original = new Network();
        Network processed = new Network();
        return new TestCase(original, processed, 0);
    }

    public static TestCase singleTriangle() throws Exception {
        Network original = new Network();
        Network processed = new Network();
        addTriangle(original, 1, 2, 3);
        addTriangle(processed, 1, 2, 3);
        return new TestCase(original, processed, 1);
    }

    public static TestCase multipleTriangles(int count) throws Exception {
        Network original = new Network();
        Network processed = new Network();
        int baseId = 1;

        // 添加第一个三元环
        addTriangle(original, baseId, baseId + 1, baseId + 2);
        addTriangle(processed, baseId, baseId + 1, baseId + 2);
        baseId += 2; // 下一个三元环的起始节点是 baseId + 2

        // 添加后续三元环，每个三元环与前一个三元环共享一个节点
        for (int i = 1; i < count; i++) {
            addTriangle(original, baseId, baseId + 1, baseId + 2);
            addTriangle(processed, baseId, baseId + 1, baseId + 2);
            baseId += 1; // 下一个三元环的起始节点是 baseId + 1
        }

        return new TestCase(original, processed, count);
    }


    public static TestCase overlappingTriangles(int sharedEdges) throws Exception {
        Network original = new Network();
        Network processed = new Network();
        addPerson(original, 1);
        addPerson(original, 2);
        addPerson(original, 999);
        addPerson(processed, 1);
        addPerson(processed, 2);
        addPerson(processed, 999);
        original.addRelation(1, 2, 10);
        original.addRelation(1, 999, 100);
        processed.addRelation(1, 2, 10);
        processed.addRelation(1, 999, 100);
        for (int i = 0; i < sharedEdges; i++) {
            int nodeId = 3 + i;
            addPerson(original, nodeId);
            addPerson(processed, nodeId);
            original.addRelation(2, nodeId, 5);
            processed.addRelation(2, nodeId, 5);
            original.addRelation(nodeId, 1, 5);
            processed.addRelation(nodeId, 1, 5);
        }
        return new TestCase(original, processed, sharedEdges);
    }

    public static TestCase noTriangleChain(int length) throws Exception {
        Network original = new Network();
        Network processed = new Network();
        for (int i = 1; i <= length; i++) {
            addPerson(original, i);
            addPerson(processed, i);
            if (i > 1) {
                original.addRelation(i-1, i, 5);
                processed.addRelation(i-1, i, 5);
            }
        }
        return new TestCase(original, processed, 0);
    }

    /* 工具方法 */
    public static void addTriangle(Network network, int a, int b, int c) throws Exception {
        addPerson(network, a);
        addPerson(network, b);
        addPerson(network, c);
        network.addRelation(a, b, 10);
        network.addRelation(b, c, 10);
        network.addRelation(c, a, 10);
    }

    public static void addPerson(Network network, int id) throws Exception {
        if (!network.containsPerson(id)) {
            network.addPerson(new Person(id, "Person"+id, 20));
        }
    }
}
