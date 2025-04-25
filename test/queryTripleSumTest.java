import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import utils.PersonInterface;

import static org.junit.Assert.*;

import java.util.*;

@RunWith(Parameterized.class)
public class queryTripleSumTest {
    private final TestCase testCase;

    public queryTripleSumTest(TestCase testCase) {
        this.testCase = testCase;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> prepareData() throws Exception {
        final int TEST_CASE_NUM = 20;
        List<TestCase> cases = new ArrayList<>();
        Random rand = new Random(System.currentTimeMillis());

        // 生成多种测试场景
        for (int i = 0; i < TEST_CASE_NUM; i++) {
            switch (i % 5) {
                case 0:
                    cases.add(emptyNetwork());
                    break;
                case 1:
                    cases.add(singleTriangle());
                    break;
                case 2:
                    cases.add(multipleTriangles(rand.nextInt(3) + 2));
                    break;
                case 3:
                    cases.add(overlappingTriangles(rand.nextInt(2) + 2));
                    break;
                case 4:
                    cases.add(noTriangleChain(rand.nextInt(5) + 3));
                    break;
            }
        }

        return Arrays.asList(cases.stream().map(tc -> new Object[]{tc}).toArray(Object[][]::new));
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

        Map<Integer, PersonInterface> afterMap = new HashMap<>();
        for (PersonInterface p : after) {
            afterMap.put(p.getId(), p);
        }

        for (PersonInterface orig : before) {
            PersonInterface current = afterMap.get(orig.getId());
            assertNotNull("Person missing: " + orig.getId(), current);
            assertTrue("State changed for person: " + orig.getId(),
                    strictEquals(orig, current));
        }
    }

    // 严格相等比较
    public boolean strictEquals(PersonInterface a, PersonInterface b) {
        if (a.getId() != b.getId()) return false;
        if (a.getAge() != b.getAge()) return false;
        if (!a.getName().equals(b.getName())) return false;

        // 比较联系人
        Set<Integer> aLinks = getLinkedIds(a);
        Set<Integer> bLinks = getLinkedIds(b);
        return aLinks.equals(bLinks);
    }

    public Set<Integer> getLinkedIds(PersonInterface p) {
        Set<Integer> links = new HashSet<>();
        Person pp = (Person) p;
        for (PersonInterface a : pp.getAcquaintance().values()) {
            links.add(a.getId());
        }
        return links;
    }

    /* 测试用例生成方法 */
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

    /* 测试用例容器 */
    static class TestCase {
        final Network network;
        final int expected;

        TestCase(Network network, int expected) {
            this.network = network;
            this.expected = expected;
        }
    }
}
