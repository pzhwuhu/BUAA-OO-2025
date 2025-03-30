import com.oocourse.elevator1.PersonRequest;

import java.util.ArrayList;

public class RequestsPool {
    private int elevatorId;
    private ArrayList<PersonRequest> requests;
    private boolean isClose;

    public RequestsPool(int elevatorId) {
        this.elevatorId = elevatorId;
        requests = new ArrayList<>();
        isClose = false;
    }

    public synchronized void addRequest(PersonRequest personRequest) {
        requests.add(personRequest);
        notifyAll();
    }

    public synchronized ArrayList<PersonRequest> getRequest() { //i对应ArrayList中的值
        if (requests.isEmpty() && !isClose) {
            try {
                wait();
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        ArrayList<PersonRequest> personRequests = new ArrayList<>();
        personRequests.addAll(requests);
        notifyAll();
        return personRequests;
    }

    public synchronized void clearRequests(PersonRequest personRequest) {
        requests.remove(personRequest);
        notifyAll();
    }

    public synchronized void close(Elevator elevator) {
        this.isClose = true;
        notifyAll();
    }

    public synchronized boolean getIsClose() {
        notifyAll();
        return isClose;
    }

    public synchronized int getElevatorId() {
        return this.elevatorId;
    }

    public synchronized boolean isEmpty() {
        notifyAll();
        return requests.isEmpty();
    }
}
