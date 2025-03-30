import com.oocourse.elevator1.PersonRequest;

import java.util.Comparator;
import java.util.PriorityQueue;

public class RequestPool {

    private PriorityQueue<PersonRequest> persons;
    private boolean inputEnd;

    public RequestPool() {
        Comparator<PersonRequest> myCmp = Comparator.comparing(PersonRequest::getPriority);
        persons = new PriorityQueue<>(myCmp);
    }

    public synchronized void add(PersonRequest person) {
        notifyAll();
        persons.add(person);
    }

    public synchronized PersonRequest poll() {
        if (persons.isEmpty() && !inputEnd) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (persons.isEmpty()) {
            return null;
        }
        notifyAll();
        return persons.poll();
    }

    public synchronized boolean isEmpty() {
        notifyAll();
        return persons.isEmpty();
    }

    public synchronized boolean isEnd() {
        notifyAll();
        return inputEnd;
    }

    public synchronized void setEnd() {
        notifyAll();
        this.inputEnd = true;
    }
}
