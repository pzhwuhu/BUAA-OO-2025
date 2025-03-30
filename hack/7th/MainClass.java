import com.oocourse.elevator1.TimableOutput;

import java.util.HashMap;

public class MainClass {
    public static void main(String[] args) {
        // initialize start timestamp at the beginning
        TimableOutput.initStartTimestamp();
        RequestQueue requestQueue = new RequestQueue();
        HashMap<Integer,ProcessingQueue> queueMap = new HashMap<>();
        for (int i = 1; i <= 6; i++) {
            ProcessingQueue processingQueue = new ProcessingQueue();
            ElevatorThread elevatorThread = new ElevatorThread(i, processingQueue);
            queueMap.put(i, processingQueue);
            elevatorThread.start();
        }
        InputThread inputThread = new InputThread(requestQueue);
        DispatchThread dispatchThread = new DispatchThread(requestQueue, queueMap);
        inputThread.start();
        dispatchThread.start();
    }
}
