import com.oocourse.elevator1.ElevatorInput;
import com.oocourse.elevator1.PersonRequest;
import com.oocourse.elevator1.Request;

import java.io.IOException;

public class InputThread extends Thread {
    private RequestPool requestPool;

    public InputThread(RequestPool requestPool) {
        this.requestPool = requestPool;
    }

    @Override
    public void run() {
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        while (true) {
            Request request = elevatorInput.nextRequest();
            if (request == null) {
                requestPool.setEnd();
                try {
                    elevatorInput.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                // System.out.println("input thread end");
                break;
            } else {
                requestPool.add((PersonRequest) request);
            }
        }
    }
}
