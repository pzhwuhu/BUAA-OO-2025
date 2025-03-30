import com.oocourse.elevator1.PersonRequest;
import com.oocourse.elevator1.ElevatorInput;
import com.oocourse.elevator1.Request;

import java.io.IOException;

public class InputThread extends Thread {
    private Controller controller;

    public InputThread(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        while (true) {
            Request request = elevatorInput.nextRequest();
            if (request == null) {
                break;
            } else {
                PersonRequest personRequest = (PersonRequest) request;
                controller.addRequest(personRequest);
            }
        }

        controller.close();
        try {
            elevatorInput.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
