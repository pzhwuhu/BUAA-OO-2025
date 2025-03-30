import com.oocourse.elevator1.Request;

import java.util.ArrayList;

public class RequestQueue {
    private final ArrayList<Request> requests = new ArrayList<>();
    private boolean isEnd = false;

    public synchronized void addRequest(Request request) {
        requests.add(request);
        notifyAll();
    }

    public synchronized Request poll() {
        if (requests.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (requests.isEmpty()) {
            return null;
        }
        notifyAll();
        return requests.remove(0);
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
        return requests.isEmpty();
    }
}
