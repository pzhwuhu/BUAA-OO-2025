
import com.oocourse.elevator1.PersonRequest;
import com.oocourse.elevator1.Request;

import java.util.HashMap;

public class DispatchThread extends Thread {
    private final RequestQueue requestQueue;
    private final HashMap<Integer, ElevatorQueue> queueMap;

    public DispatchThread(RequestQueue requestQueue, HashMap<Integer, ElevatorQueue> queueMap) {
        this.requestQueue = requestQueue;
        this.queueMap = queueMap;
    }

    @Override
    public void run() {
        while (true) {
            if (requestQueue.isEmpty() && requestQueue.isEnd()) {
                for (ElevatorQueue queue : queueMap.values()) {
                    //向电梯线程发出结束信号
                    queue.setEnd();
                }
                break;
            }
            // TODO: 从orderQueue中取出订单
            Request request = requestQueue.poll();
            if (request == null) {
                continue;
            }
            dispatch(request);
        }
    }

    private void dispatch(Request request) {
        PersonRequest a = (PersonRequest) request;
        int elevatorID = a.getElevatorId();
        queueMap.get(elevatorID).offer(a);
    }
}
