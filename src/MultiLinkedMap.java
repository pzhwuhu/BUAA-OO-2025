import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiLinkedMap<T> {

    public static class Node<T> {
        private T value;
        private Node<T> next;
        private Node<T> prev;
        private String id; // 存储 key，方便删除时判断

        public Node(String id, T value) {
            this.id = id;
            this.value = value;
            this.next = null;
            this.prev = null;
        }
    }

    private Node<T> head;
    private Map<String, List<Node<T>>> maps = new HashMap<>(); // key → List<Node>
    private int size = 0;

    public MultiLinkedMap() {
        head = null;
    }

    public int getSize() { return size; }

    //插入头部（允许重复 key）
    public void insertHead(String id, T value) {
        Node<T> node = new Node<>(id, value);

        if (head == null) {
            head = node;
        } else {
            node.next = head;
            head.prev = node;
            head = node;
        }

        maps.computeIfAbsent(id, k -> new ArrayList<>()).add(node);
        size++;
    }

    //删除所有匹配 key 的节点
    public void delete(String id) {
        List<Node<T>> nodes = maps.get(id);
        if (nodes == null || nodes.isEmpty()) {
            return; // key 不存在
        }

        // 1. 从哈希表移除该 key 的所有节点
        maps.remove(id);

        // 2. 从链表中删除所有匹配的节点
        for (Node<T> node : nodes) {
            // 处理前驱节点
            if (node.prev != null) {
                node.prev.next = node.next;
            } else {
                head = node.next; // 如果删除的是头节点，更新 head
            }

            // 处理后继节点
            if (node.next != null) {
                node.next.prev = node.prev;
            }

            // 清理引用
            node.next = null;
            node.prev = null;
            size--;
        }
    }

    //转换为列表（按插入顺序）
    public List<T> toList() {
        List<T> list = new ArrayList<>();
        Node<T> current = head;
        while (current != null) {
            list.add(current.value);
            current = current.next;
        }
        return list;
    }

    //获取前 5 个元素
    public List<T> to5List() {
        List<T> list = new ArrayList<>(5);
        Node<T> current = head;
        int count = 0;
        while (current != null && count < 5) {
            list.add(current.value);
            current = current.next;
            count++;
        }
        return list;
    }
}
