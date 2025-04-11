import com.oocourse.elevator3.TimableOutput;

import java.util.HashMap;

public class MainClass {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();
        Requests mainRequests = new Requests(0);
        HashMap<Integer, Requests> subRequestMap = new HashMap<>();
        HashMap<Integer, ElevatorThread> elevatorMap = new HashMap<>();
        for (int i = 1;i <= 6;i++) {
            Requests subRequests = new Requests(i);
            subRequestMap.put(i, subRequests);
            ElevatorThread elevatorThread = new ElevatorThread(i, mainRequests, subRequests);
            elevatorMap.put(i, elevatorThread);
            elevatorThread.start();
        }
        DispatchThread dispatchThread = new DispatchThread(mainRequests,
            subRequestMap, elevatorMap);
        dispatchThread.start();
        InputThread inputThread = new InputThread(mainRequests, subRequestMap, elevatorMap);
        inputThread.start();
    }
}
