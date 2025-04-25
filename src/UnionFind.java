import java.util.HashMap;

// discard
public class UnionFind {
    private HashMap<Integer, Integer> son2dad;//son.id -> dad.id
    private HashMap<Integer, Integer> height;//id -> height

    public UnionFind() {
        son2dad = new HashMap<>();
        height = new HashMap<>();
    }

    public void addNode(int id) {
        if (!son2dad.containsKey(id)) {
            son2dad.put(id, id);//初始代表元就是自己
            height.put(id, 0);
        }
    }

    //查询代表元
    public int find(int id) {
        int root = id;
        while (root != son2dad.get(root)) {
            root = son2dad.get(root);
        }

        //路径压缩
        int begin = id;
        while (begin != root) {
            int next = son2dad.get(begin);
            son2dad.put(begin, root);
            begin = next;
        }
        return root;
    }

    public int merge(int id1, int id2) {
        int root1 = son2dad.get(id1);
        int root2 = son2dad.get(id2);
        if (root1 == root2) { return -1; }

        int height1 = height.get(root1);
        int height2 = height.get(root2);
        if (height1 <= height2) {
            if (height1 == height2) {
                height.put(root2, height2 + 1);
            }
            son2dad.put(root1, root2);
        } else {
            son2dad.put(root2, root1);
        }
        return 0;
    }
}
