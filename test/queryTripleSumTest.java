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
        final Network network;
        final int expected;

        TestCase(Network network, int expected) {
            this.network = network;
            this.expected = expected;
        }
    }

    @Parameterized.Parameters
    public static Collection<Object[]> prepareData() throws Exception {
        final int TEST_CASE_NUM = 40;
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
                    testCase = multipleTriangles(rand.nextInt(12) + 4);
                    break;
                case 3:
                    testCase = overlappingTriangles(rand.nextInt(8) + 4);
                    break;
                case 4:
                    testCase = noTriangleChain(rand.nextInt(12) + 6);
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
        // 记录调用前状态
        PersonInterface[] before = testCase.network.getPersons();

        // 执行测试方法
        int actual = testCase.network.queryTripleSum();

        // 验证结果正确性
        assertEquals("Triple count mismatch", testCase.expected, actual);

        // 验证状态不变性
        verifyStateUnchanged(before, testCase.network.getPersons());
    }

    // 状态一致性验证
    public void verifyStateUnchanged(PersonInterface[] before, PersonInterface[] after) {
        assertEquals("Person count changed", before.length, after.length);
        for (int i = 0; i < before.length; i++) {
            assertEquals(true, ((Person)before[i]).strictEquals((Person)after[i]));
        }
    }

    /* 测试用例生成 */
    public static TestCase emptyNetwork() throws Exception {
        return new TestCase(new Network(), 0);
    }

    public static TestCase singleTriangle() throws Exception {
        Network network = new Network();
        addTriangle(network, 1, 2, 3);
        return new TestCase(network, 1);
    }

    public static TestCase multipleTriangles(int count) throws Exception {
        Network network = new Network();
        int baseId = 1;
        for (int i = 0; i < count; i++) {
            addTriangle(network, baseId, baseId+1, baseId+2);
            baseId += 3;
        }
        return new TestCase(network, count);
    }

    public static TestCase overlappingTriangles(int sharedEdges) throws Exception {
        Network network = new Network();
        // 共享边1-2
        addPerson(network, 1);
        addPerson(network, 2);
        network.addRelation(1, 2, 10);

        // 添加重叠三角形
        for (int i = 0; i < sharedEdges; i++) {
            int nodeId = 3 + i;
            addPerson(network, nodeId);
            network.addRelation(2, nodeId, 5);
            network.addRelation(nodeId, 1, 5);
        }
        return new TestCase(network, sharedEdges);
    }

    public static TestCase noTriangleChain(int length) throws Exception {
        Network network = new Network();
        for (int i = 1; i <= length; i++) {
            addPerson(network, i);
            if (i > 1) {
                network.addRelation(i-1, i, 5);
            }
        }
        return new TestCase(network, 0);
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
