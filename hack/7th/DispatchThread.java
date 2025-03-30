import com.oocourse.elevator1.PersonRequest;
import com.oocourse.elevator1.Request;

import java.util.HashMap;

public class DispatchThread extends Thread {
    private final RequestQueue requestQueue;
    private final HashMap<Integer,ProcessingQueue> queueMap;

    public DispatchThread(RequestQueue requestQueue,HashMap<Integer,ProcessingQueue> queueMap) {
        this.requestQueue = requestQueue;
        this.queueMap = queueMap;
    }

    @Override
    public void run() {
        while (true) {
            if (requestQueue.isEnd() && requestQueue.isEmpty()) {
                for (ProcessingQueue queue: queueMap.values()) {
                    queue.markEnd();
                }
                break;
            }
            Request request = requestQueue.poll();
            if (request == null) {
                continue;
            }
            dispatch(request);
        }
    }

    public void dispatch(Request request) {
        int elevatorId;
        String fromFloor;
        elevatorId = ((PersonRequest)request).getElevatorId();
        fromFloor = ((PersonRequest)request).getFromFloor();
        ProcessingQueue queue = queueMap.get(elevatorId);
        queue.offer((PersonRequest) request, fromFloor);
    }
}
