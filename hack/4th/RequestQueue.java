import com.oocourse.elevator1.PersonRequest;

import java.util.ArrayList;

public class RequestQueue {
    private boolean isEnd = false;
    private ArrayList<PersonRequest> personRequests = new ArrayList<>();

    public synchronized void putPersonRequest(PersonRequest personRequest) {
        personRequests.add(personRequest);
        notifyAll();
    }

    public synchronized PersonRequest getPersonRequest() {
        if (personRequests.isEmpty() && !isEnd) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (personRequests.isEmpty()) {
            return null;
        }
        notifyAll();
        return personRequests.remove(0);
    }

    public synchronized boolean isEnd() {
        notifyAll();
        return isEnd;
    }

    public synchronized void setEnd() {
        isEnd = true;
        notifyAll();
    }

    public synchronized boolean isEmpty() {
        notifyAll();
        return personRequests.isEmpty();
    }
}
