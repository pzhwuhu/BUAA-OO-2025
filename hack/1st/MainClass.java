

import com.oocourse.elevator1.TimableOutput;

import java.util.HashMap;

public class MainClass {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();// 初始化时间戳

        RequestQueue requestQueue = new RequestQueue();
        int[] elevatorTypes = {1,2,3,4,5,6};
        HashMap<Integer, ElevatorQueue> queueMap = new HashMap<>();
        DispatchThread dispatchThread = new DispatchThread(requestQueue,queueMap);
        for (int elevatorType : elevatorTypes) {
            ElevatorQueue queue = new ElevatorQueue();
            queueMap.put(elevatorType, queue);
            ElevatorThread elevatorThread = new ElevatorThread(elevatorType, queue);
            elevatorThread.start();
        }
        InputThread inputThread = new InputThread(requestQueue);
        inputThread.start();
        dispatchThread.start();
    }
}
