import com.oocourse.elevator2.TimableOutput;

import java.util.HashMap;

public class MainClass {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();
        Requests mainRequests = new Requests();
        HashMap<Integer, Requests> subRequestMap = new HashMap<>();
        DispatchThread dispatchThread = new DispatchThread(mainRequests, subRequestMap);
        dispatchThread.start();
        for (int i = 1;i <= 6;i++) {
            Requests subRequests = new Requests();
            subRequestMap.put(i, subRequests);
            ElevatorThread elevatorThread = new ElevatorThread(i, mainRequests, subRequests);
            elevatorThread.start();
        }
        InputThread inputThread = new InputThread(mainRequests);
        inputThread.start();
    }
}
