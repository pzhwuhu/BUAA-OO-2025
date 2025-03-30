import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class ProcessingQueue {
    private final HashMap<Floor, ArrayList<Person>> upPersonMap;
    private final HashMap<Floor, ArrayList<Person>> downPersonMap;
    private boolean isEnd = false;
    
    public ProcessingQueue() {
        this.upPersonMap = new HashMap<>();
        this.downPersonMap = new HashMap<>();
    }
    
    public synchronized void addRequest(Person person) {
        Floor fromFloor = person.getFromFloor();
        Floor toFloor = person.getToFloor();
        if (fromFloor.compareTo(toFloor) < 0) {
            upPersonMap.putIfAbsent(fromFloor, new ArrayList<>());
            upPersonMap.get(fromFloor).add(person);
        }  else {
            downPersonMap.putIfAbsent(fromFloor, new ArrayList<>());
            downPersonMap.get(fromFloor).add(person);
        }
        notifyAll();
    }
    
    public synchronized void addPassenger(Person person) {
        //add passenger to elevator
        Floor fromFloor = person.getFromFloor();
        Floor toFloor = person.getToFloor();
        if (fromFloor.compareTo(toFloor) < 0) {
            upPersonMap.putIfAbsent(toFloor, new ArrayList<>());
            upPersonMap.get(toFloor).add(person);
        }  else {
            downPersonMap.putIfAbsent(toFloor, new ArrayList<>());
            downPersonMap.get(toFloor).add(person);
        }
        notifyAll();
    }
    
    public synchronized void setEnd() {
        isEnd = true;
        notifyAll();
    }
    
    public synchronized boolean isEnd() {
        notifyAll();
        return isEnd;
    }
    
    public synchronized boolean isUpEmpty() {
        notifyAll();
        for (Floor floor : upPersonMap.keySet()) {
            if (!upPersonMap.get(floor).isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    public synchronized boolean isDownEmpty() {
        notifyAll();
        for (Floor floor : downPersonMap.keySet()) {
            if (!downPersonMap.get(floor).isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    public synchronized boolean isEmpty() {
        notifyAll();
        return isUpEmpty() && isDownEmpty();
    }
    
    public synchronized ArrayList<Person> getAllPassenger() {
        ArrayList<Person> result = new ArrayList<>();
        if (isEmpty()) {
            return result;
        } else if (isUpEmpty()) {
            for (Floor floor : downPersonMap.keySet()) {
                result.addAll(downPersonMap.get(floor));
            }
        } else if (isDownEmpty()) {
            for (Floor floor : upPersonMap.keySet()) {
                result.addAll(upPersonMap.get(floor));
            }
        } else {
            throw new RuntimeException("Different direction in passengers");
        }
        return result;
    }
    
    public synchronized void removePassenger(Person person) {
        //remove passenger from elevator
        Floor toFloor = person.getToFloor();
        ArrayList<Person> up = upPersonMap.getOrDefault(toFloor, new ArrayList<>());
        ArrayList<Person> down = downPersonMap.getOrDefault(toFloor, new ArrayList<>());
        up.remove(person);
        down.remove(person);
    }
    
    public synchronized int getDirection() {
        // get direction of passengers
        if (isUpEmpty() && isDownEmpty()) {
            return 0;
        } else if (isUpEmpty()) {
            return -1;
        } else if (isDownEmpty()) {
            return 1;
        } else {
            throw new RuntimeException("Different direction in passengers");
        }
    }
    
    public synchronized ArrayList<Person> getSameDirByFloor(Floor currentFloor, int direction) {
        switch (direction) {
            case 1:
                return upPersonMap.getOrDefault(currentFloor, new ArrayList<>());
            case -1:
                return downPersonMap.getOrDefault(currentFloor, new ArrayList<>());
            case 0:
                return new ArrayList<>();
            default:
                throw new RuntimeException("Invalid direction");
        }
    }
    
    public synchronized ArrayList<Person> getDiffDirByFloor(Floor currentFloor, int direction) {
        switch (direction) {
            case 1:
                return downPersonMap.getOrDefault(currentFloor, new ArrayList<>());
            case -1:
                return upPersonMap.getOrDefault(currentFloor, new ArrayList<>());
            case 0:
                return new ArrayList<>();
            default:
                throw new RuntimeException("Invalid direction");
        }
    }
    
    public synchronized ArrayList<Person> getAllSameDirByFloor(Floor currentFloor, int direction) {
        ArrayList<Person> result = new ArrayList<>();
        switch (direction) {
            case 1:
                for (Floor floor : upPersonMap.keySet()) {
                    if (floor.compareTo(currentFloor) > 0) {
                        result.addAll(upPersonMap.get(floor));
                    }
                }
                break;
            case -1:
                for (Floor floor : downPersonMap.keySet()) {
                    if (floor.compareTo(currentFloor) < 0) {
                        result.addAll(downPersonMap.get(floor));
                    }
                }
                break;
            case 0:
                for (Floor floor : upPersonMap.keySet()) {
                    result.addAll(upPersonMap.get(floor));
                }
                for (Floor floor : downPersonMap.keySet()) {
                    result.addAll(downPersonMap.get(floor));
                }
                break;
            default:
                throw new RuntimeException("Invalid direction");
        }
        return result;
    }
    
    public synchronized ArrayList<Person> getAllDiffDirByFloor(Floor currentFloor, int direction) {
        ArrayList<Person> result = new ArrayList<>();
        switch (direction) {
            case 1:
                for (Floor floor : downPersonMap.keySet()) {
                    if (floor.compareTo(currentFloor) > 0) {
                        result.addAll(downPersonMap.get(floor));
                    }
                }
                break;
            case -1:
                for (Floor floor : upPersonMap.keySet()) {
                    if (floor.compareTo(currentFloor) < 0) {
                        result.addAll(upPersonMap.get(floor));
                    }
                }
                break;
            case 0:
                for (Floor floor : upPersonMap.keySet()) {
                    result.addAll(upPersonMap.get(floor));
                }
                for (Floor floor : downPersonMap.keySet()) {
                    result.addAll(downPersonMap.get(floor));
                }
                break;
            default:
                throw new RuntimeException("Invalid direction");
        }
        return result;
    }
    
    public synchronized ArrayList<Person> getPriotizedEnters(Floor currentFloor,
        int direction, int num) {
        ArrayList<Person> result;
        if (-1 <= direction && direction <= 1) {
            result = getAllSameDirByFloor(currentFloor, direction);
            if (direction != 0) {
                result.addAll(getSameDirByFloor(currentFloor, direction));
            }
        } else {
            throw new RuntimeException("Invalid direction");
        }
        result.sort(Comparator.comparingInt(Person::getPriority).reversed());
        if (result.size() > num && num != 0) {
            result = new ArrayList<>(result.subList(0, num));
        }
        return result;
    }
    
    public synchronized ArrayList<Person> getPriotizedAll() {
        return getPriotizedEnters(Floor.F1, 0, 0);
    }
}
