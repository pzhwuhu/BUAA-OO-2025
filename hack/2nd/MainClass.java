import com.oocourse.elevator1.TimableOutput;
import elevator.ElevatorContext;
import elevator.RequestTable;

import java.util.HashMap;

public class MainClass {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();

        RequestPool requestPool = new RequestPool();

        // 输入线程，启动！
        InputThread inputThread = new InputThread(requestPool);
        inputThread.start();

        HashMap<Integer, RequestTable> requestTables = new HashMap<>();
        for (int i = 1;i <= 6;i++) {
            requestTables.put(i, new RequestTable());
        }

        // 调度器，启动！
        DispatchThread dispatchThread = new DispatchThread(requestPool, requestTables);
        dispatchThread.start();

        // 电梯，启动！
        for (int i = 1;i <= 6;i++) {
            ElevatorContext elevator = new ElevatorContext(i);
            ElevatorThread elevatorThread = new ElevatorThread(elevator, requestTables.get(i));
            elevatorThread.start();
        }

    }
}
