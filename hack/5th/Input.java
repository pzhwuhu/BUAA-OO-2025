import com.oocourse.elevator1.ElevatorInput;
import com.oocourse.elevator1.Request;
import com.oocourse.elevator1.PersonRequest;

import java.io.IOException;

public class Input extends Thread {
    private final RequestQuene requests;
    
    public Input(RequestQuene requests) {
        this.requests = requests;
    }
    
    @Override
    public void run() {
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        while (true) {
            Request request = elevatorInput.nextRequest();
            // 当request == null时，输入结束。
            if (request == null) {
                requests.setEnd();
                if (Main.isDebug()) {
                    System.out.println("Input end");
                }
                break;
            } else {
                if (request instanceof PersonRequest) {
                    if (Main.isDebug()) {
                        System.out.println("Input " + request);
                    }
                    requests.addRequest((PersonRequest) request);
                }
            }
        }
        try {
            elevatorInput.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
