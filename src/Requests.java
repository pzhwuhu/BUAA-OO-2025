import com.oocourse.elevator3.Request;
import com.oocourse.elevator3.UpdateRequest;
import com.oocourse.elevator3.ScheRequest;
import com.oocourse.elevator3.PersonRequest;

import java.util.ArrayList;
import java.util.Comparator;

public class Requests {
    private final ArrayList<Request> requests = new ArrayList<>();
    private ScheRequest scheRequest = null;
    private boolean done = false;
    private boolean free = true;
    private final int elevatorId;
    private final ArrayList<Integer> needRequests = new ArrayList<>();
    private int arrivedRequests = 0;
    private UpdateRequest updateRequest = null;

    public synchronized int getSize() {
        return requests.size();
    }

    public synchronized void addNeed(int id) {
        needRequests.add(id);
    }

    public synchronized void addArrive() {
        arrivedRequests++;
        notifyAll();
    }

    public synchronized int getElevatorId() {
        return elevatorId;
    }

    public Requests(int elevatorId) {
        this.elevatorId = elevatorId;
    }

    public synchronized boolean getFree() {
        return free;
    }

    public synchronized void setFree(boolean free) {
        this.free = free;
        if (free) {
            notifyAll();
        }
    }

    public synchronized ArrayList<Request> getRequests() {
        notifyAll();
        return requests;
    }

    public synchronized void setUpdateRequest(UpdateRequest updateRequest) {
        this.updateRequest = updateRequest;
        notifyAll();
    }

    public synchronized UpdateRequest getUpdateRequest() { return updateRequest; }

    public synchronized void setScheRequest(ScheRequest scheRequest) {
        this.scheRequest = scheRequest;
        notifyAll();
    }

    public synchronized ScheRequest getScheRequest() {
        return scheRequest;
    }

    public synchronized void sortByPriority() {
        requests.sort(Comparator.comparingInt(r -> ((PersonRequest) r).getPriority()).reversed());
    }

    public synchronized void push(Request request) {
        //TimableOutput.println("from-" + elevatorId + "-request need
        // to be dispatched-" + isDone() + "-" + isEmpty());
        requests.add(request);
        notifyAll();
    }

    public synchronized Request pop() {
        if (!isDone() && isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (isEmpty()) {
            return null;
        }
        notifyAll();
        return requests.remove(0);
    }

    public synchronized void setDone() {
        done = true;
        notifyAll();
    }

    public synchronized boolean isDone() {
        return done & (arrivedRequests == needRequests.size());
        //return done;
    }

    public synchronized boolean isEmpty() {
        return requests.isEmpty();
    }
}
