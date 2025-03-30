import com.oocourse.elevator1.PersonRequest;
import com.oocourse.elevator1.TimableOutput;

public class Person {
    private final int id;
    private  Floor from;
    private final Floor to;
    private final int priority;
    
    public Person(PersonRequest request) {
        this.id = request.getPersonId();
        this.from = Floor.valueOf(request.getFromFloor());
        this.to = Floor.valueOf(request.getToFloor());
        this.priority = request.getPriority();
    }
    
    public synchronized Floor getFromFloor() {
        return this.from;
    }
    
    public synchronized int getPriority() {
        return this.priority;
    }
    
    public synchronized Floor getToFloor() {
        return this.to;
    }
    
    public synchronized void setFromFloor(Floor floor) {
        this.from = floor;
    }
    
    public synchronized int getDirection() {
        if (from.compareTo(to) < 0) {
            return 1;
        } else if (from.compareTo(to) > 0) {
            return -1;
        } else {
            throw new RuntimeException("from == to");
        }
    }
    
    public synchronized void exit(Floor currentFloor, int id) {
        TimableOutput.println("OUT-" + this.id + "-" + currentFloor + "-" + id);
    }
    
    public synchronized void enter(Floor currentFloor, int id) {
        TimableOutput.println("IN-" + this.id + "-" + currentFloor + "-" + id);
    }
    
}
