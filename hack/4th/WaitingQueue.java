import com.oocourse.elevator1.PersonRequest;

import java.util.ArrayList;

public class WaitingQueue {
    private boolean isEnd = false;
    private ArrayList<PersonRequest> personRequests = new ArrayList<>();

    public synchronized void putPersonRequest(PersonRequest personRequest) {
        personRequests.add(personRequest);
        notifyAll();
    }

    public synchronized ArrayList<PersonRequest>
        getPersonRequests(ArrayList<PersonRequest> passengers) {
        if (personRequests.isEmpty() && !isEnd && passengers.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (personRequests.isEmpty()) {
            return null;
        }
        ArrayList<PersonRequest> result = new ArrayList<>();
        for (PersonRequest personRequest : personRequests) {
            result.add(personRequest);
        }
        notifyAll();
        return result;
    }

    public synchronized void removePersonRequest(PersonRequest personRequest) {
        if (!personRequests.contains(personRequest)) {
            System.out.println("error: not contain this request");
        }
        personRequests.remove(personRequest);
        notifyAll();
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
