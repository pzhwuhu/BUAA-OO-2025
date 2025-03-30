import com.oocourse.elevator1.PersonRequest;

import java.util.ArrayList;
import java.util.Hashtable;

public class ProcessingQueue {
    private final Hashtable<String,ArrayList<PersonRequest>> userRequests = new Hashtable<>();
    private boolean isEnd;

    public synchronized void offer(PersonRequest personRequest, String fromFloor) {
        if (userRequests.containsKey(fromFloor)) {
            ArrayList<PersonRequest> requests = userRequests.get(fromFloor);
            requests.add(personRequest);
            userRequests.replace(fromFloor,requests);
        }
        else {
            ArrayList<PersonRequest> requests = new ArrayList<>();
            requests.add(personRequest);
            userRequests.put(fromFloor,requests);
        }
        notifyAll();
    }

    public synchronized boolean isEnd() {
        notifyAll();
        return isEnd;
    }

    public synchronized void markEnd() {
        isEnd = true;
        notifyAll();
    }

    public synchronized boolean isEmpty() {
        notifyAll();
        return userRequests.isEmpty();
    }

    public synchronized Hashtable<String,ArrayList<PersonRequest>> getUserRequests() {
        notifyAll();
        return this.userRequests;
    }

    public synchronized void waitForRequest() {
        if (userRequests.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        notifyAll();
    }
}
