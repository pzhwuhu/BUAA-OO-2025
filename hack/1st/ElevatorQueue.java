

import com.oocourse.elevator1.PersonRequest;
import com.oocourse.elevator1.Request;

import java.util.ArrayList;
import java.util.Comparator;

public class ElevatorQueue {
    private final ArrayList<Request> requests = new ArrayList<>();
    private boolean isEnd = false;

    public synchronized void offer(Request request) {
        requests.add(request);
        requests.sort(new Comparator<Request>() {
            @Override
            public int compare(Request a, Request b) {
                PersonRequest c = (PersonRequest)a;
                PersonRequest d = (PersonRequest)b;
                return d.getPriority() - c.getPriority(); //按优先级降序排序
            }
        });
        notifyAll();
    }

    public synchronized Request poll(int direction,int floor) {
        if (isEmpty() && !isEnd()) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        Request request = null;

        if (direction != 0) { //捎带
            for (Request value : requests) {
                PersonRequest a = (PersonRequest) value;
                int toFloor = translate(a.getToFloor());
                int fromFloor = translate(a.getFromFloor());
                if ((toFloor - fromFloor) * direction > 0 && fromFloor == floor) { //同方向
                    request = a;
                    requests.remove(a);
                    break;
                }
            }
        }
        else { //取主请求
            request = requests.remove(0);
        }
        notifyAll();
        return request;
    }

    public synchronized int getTopDirect(int floor) {
        if (isEmpty() && !isEnd()) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        notifyAll();
        if (!isEmpty()) {
            return goFor(requests.get(0),floor);
        }
        else {
            return 0;
        }
    }

    public int goFor(Request request,int floor) {
        PersonRequest a = (PersonRequest) request;
        if (translate(a.getFromFloor()) - floor > 0) {
            return 1;
        }
        else if (translate(a.getFromFloor()) - floor < 0) {
            return -1;
        }
        else {
            return 0;
        }
    }

    public synchronized boolean hasIn(int direction,int floor) {
        notifyAll();
        Request request = null;

        if (requests.isEmpty()) {
            return false;
        }

        if (direction != 0) { //捎带
            for (Request value : requests) {
                PersonRequest a = (PersonRequest) value;
                int toFloor = translate(a.getToFloor());
                int fromFloor = translate(a.getFromFloor());
                if ((toFloor - fromFloor) * direction > 0 && fromFloor == floor) { //同方向
                    request = a;
                    break;
                }
            }
        }
        else { //取主请求
            request = requests.get(0);
        }
        return request != null;
    }

    public int translate(String floor) {
        if (floor.charAt(0) == 'B') {
            return -1 * (floor.charAt(1) - '0');
        }
        else {
            return floor.charAt(1) - '1';
        }
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
