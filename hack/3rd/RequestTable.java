import java.util.HashMap;
import java.util.PriorityQueue;

public class RequestTable {
    private HashMap<Floor, PriorityQueue<Person>> requestMap;
    private boolean isOver = false;

    public RequestTable() {
        this.requestMap = new HashMap<>();
        for (int i = -4; i < 8; i++) {
            requestMap.put(new Floor(i), new PriorityQueue<Person>());

        }
    }

    public synchronized boolean hasReqInDirection(int direction, Floor curFloor) {
        if (direction == 1) {
            int level = curFloor.getLevel() + 1;
            if (level > 6) {
                return false;
            }
            for (; level < 7; level++) {
                if (!requestMap.get(new Floor(level)).isEmpty()) {
                    return true;
                }
            }
        } else if (direction == -1) {
            int level = curFloor.getLevel() - 1;
            if (level < -4) {
                return false;
            }
            for (; level > -5; level--) {
                if (!requestMap.get(new Floor(level)).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isOver() {
        return isOver;
    }

    public synchronized void waitRequest() {
        try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized void setOver(boolean over) {
        isOver = over;
        notifyAll();
    }

    public synchronized void addPerson(Floor floor, Person person) {
        requestMap.get(floor).add(person);
        notifyAll();
    }

    public synchronized PriorityQueue<Person> getPersonQueue(Floor floor) {
        return requestMap.get(floor);
    }

    public Boolean isEmpty() {
        for (PriorityQueue<Person> queue : requestMap.values()) {
            if (!queue.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public Person getMostPriorityPerson() {
        Person mostPriorityPerson = null;
        for (PriorityQueue<Person> queue : requestMap.values()) {
            if (!queue.isEmpty()) {
                if (mostPriorityPerson == null ||
                        queue.peek().getPriority() > mostPriorityPerson.getPriority()) {
                    mostPriorityPerson = queue.peek();
                }
            }
        }
        return mostPriorityPerson;
    }
}
