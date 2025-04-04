import com.oocourse.elevator2.ElevatorInput;
import com.oocourse.elevator2.Request;

public class InputThread extends Thread {
    private final Requests mainRequests;

    public InputThread(Requests mainRequests) {
        this.mainRequests = mainRequests;
    }

    @Override
    public void run() {
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        while (true) {
            Request request = elevatorInput.nextRequest();
            if (request == null) {
                mainRequests.setDone();
                break;
            }

            mainRequests.push(request);
        }
    }
}
