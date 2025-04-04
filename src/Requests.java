import com.oocourse.elevator2.PersonRequest;
import com.oocourse.elevator2.Request;
import com.oocourse.elevator2.ScheRequest;

import java.util.ArrayList;
import java.util.Comparator;

public class Requests {
    private final ArrayList<Request> requests = new ArrayList<>();
    private ScheRequest scheRequest = null;
    private boolean done = false;

    public synchronized ArrayList<Request> getRequests() {
        notifyAll();
        return requests;
    }

    public synchronized void setScheRequest(ScheRequest scheRequest) {
        this.scheRequest = scheRequest;
    }

    public synchronized ScheRequest getScheRequest() {
        return scheRequest;
    }

    public synchronized void sortByPriority() {
        requests.sort(Comparator.comparingInt(r -> ((PersonRequest) r).getPriority()).reversed());
    }

    public synchronized void push(Request request) {
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
        return done;
    }

    public synchronized boolean isEmpty() {
        return requests.isEmpty();
    }
}
