import com.oocourse.elevator1.Request;

import java.util.ArrayList;

public class Requests {
    private final ArrayList<Request> requests = new ArrayList<>();
    private boolean done = false;

    public synchronized ArrayList<Request> getRequests() {
        notifyAll();
        return requests;
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
