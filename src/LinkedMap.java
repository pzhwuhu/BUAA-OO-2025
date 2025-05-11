import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LinkedMap<T> {

    public static class Node<T> {
        private T value;  // 存储任意类型的值
        private Node<T> next;
        private Node<T> prev;

        public Node(T value) {
            this.value = value;
            next = prev = null;
        }
    }

    private Node<T> head;
    private HashMap<String, Node<T>> maps = new HashMap<>(); // 用String作为ID类型
    private int size = 0;

    public LinkedMap() {
        head = null;
    }

    public int getSize() { return size; }

    // 插入头部：直接传入对象和它的唯一ID
    public void insertHead(String id, T value) {
        if (maps.containsKey(id)) {
            delete(id); // 如果ID已存在，先删除旧节点
        }

        Node<T> node = new Node<>(value);
        if (head == null) {
            head = node;
        } else {
            node.next = head;
            head.prev = node;
            head = node;
        }
        maps.put(id, node);
        size++;
    }

    // 删除：直接通过ID删除（不再需要传入对象）
    public void delete(String id) {
        Node<T> node = maps.get(id);
        if (node == null) {
            return;
        }

        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next;
        }

        if (node.next != null) {
            node.next.prev = node.prev;
        }

        node.next = null;
        node.prev = null;

        maps.remove(id);
        size--;
    }

    // 转换为列表
    public List<T> toList() {
        List<T> list = new ArrayList<>();
        Node<T> current = head;
        while (current != null) {
            list.add(current.value);
            current = current.next;
        }
        return list;
    }

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
