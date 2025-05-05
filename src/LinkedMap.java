import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LinkedMap {

    public static class Node {
        private int value;
        private Node next;
        private Node prev;

        public Node(int value) {
            this.value = value;
            next = prev = null;
        }
    }

    private Node head;
    private HashMap<Integer, Node> maps = new HashMap<>();
    private int size = 0;

    public LinkedMap() {
        head = null;
    }

    public int getSize() { return size; }

    public void insertHead(int value) {
        Node node = new Node(value);
        if (head == null) {
            head = node;
        } else {
            node.next = head;
            head.prev = node;
            head = node;
        }
        maps.put(value, node);
        size++;
    }

    public void delete(int value) {
        Node node = maps.get(value);
        if (node == null) { return; }

        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next;
        }

        if (node.next != null) {
            node.next.prev = node.prev;
        }
        maps.remove(value);
        size--;
    }

    public List<Integer> toList() {
        List<Integer> list = new ArrayList<>();
        Node current = head;
        while (current != null) {
            list.add(current.value);
            current = current.next;
        }
        return list;
    }
}
