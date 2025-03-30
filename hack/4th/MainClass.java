import com.oocourse.elevator1.TimableOutput;

import java.util.HashMap;

public class MainClass {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();  // 初始化时间戳
        RequestQueue requestQueue = new RequestQueue();
        InputThread inputThread = new InputThread(requestQueue);
        HashMap<Integer,WaitingQueue> waitingQueueMap = new HashMap<>();
        for (int i = 1; i <= 6; i++) {
            WaitingQueue waitingQueue = new WaitingQueue();
            ElevatorThread elevatorThread = new ElevatorThread(i, waitingQueue);
            elevatorThread.start();
            waitingQueueMap.put(i, waitingQueue);
        }
        SchedulerThread schedulerThread = new SchedulerThread(requestQueue, waitingQueueMap);
        inputThread.start();
        schedulerThread.start();
    }

}