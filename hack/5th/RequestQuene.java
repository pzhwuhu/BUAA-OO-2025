import com.oocourse.elevator1.PersonRequest;

import java.util.ArrayList;

public class RequestQuene {
    private final ArrayList<PersonRequest> requests;
    private boolean isEnd = false;
    
    public RequestQuene() {
        this.requests = new ArrayList<>();
    }
    
    public synchronized void addRequest(PersonRequest request) {
        requests.add(request);
        notifyAll();
    }
    
    public synchronized PersonRequest getRequest() {
        if (requests.isEmpty() && !isEnd()) {
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
    
    public synchronized void setEnd() {
        isEnd = true;
        notifyAll();
    }
    
    public synchronized boolean isEnd() {
        notifyAll();
        return isEnd;
    }
    
    public synchronized boolean isEmpty() {
        notifyAll();
        return requests.isEmpty();
    }
}
