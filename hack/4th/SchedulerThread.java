import com.oocourse.elevator1.PersonRequest;

import java.util.HashMap;

public class SchedulerThread extends Thread {
    private RequestQueue requestQueue;
    private HashMap<Integer,WaitingQueue> waitingQueueMap;

    public SchedulerThread(RequestQueue requestQueue,
                           HashMap<Integer,WaitingQueue> waitingQueueMap) {
        this.requestQueue = requestQueue;
        this.waitingQueueMap = waitingQueueMap;
    }

    @Override
    public void run() {
        while (true) {
            if (requestQueue.isEnd() && requestQueue.isEmpty()) {
                for (WaitingQueue queue : waitingQueueMap.values()) {
                    queue.setEnd();
                }
                break;
            }
            PersonRequest personRequest = requestQueue.getPersonRequest();
            if (personRequest == null) {
                continue;
            }
            Integer elevatorId = personRequest.getElevatorId();
            waitingQueueMap.get(elevatorId).putPersonRequest(personRequest);
        }
    }
}
