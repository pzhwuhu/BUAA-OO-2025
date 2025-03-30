import com.oocourse.elevator1.ElevatorInput;
import com.oocourse.elevator1.PersonRequest;
import com.oocourse.elevator1.Request;

public class InputThread extends Thread {
    private RequestQueue requestQueue;

    public InputThread(RequestQueue requestQueue) {

        this.requestQueue = requestQueue;
    }

    @Override
    public void run() {
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        while (true) {
            Request request = elevatorInput.nextRequest();
            //System.out.println(request);
            if (request == null) {
                requestQueue.setEnd();
                break;
            } else {
                if (request instanceof PersonRequest) {
                    PersonRequest personRequest = (PersonRequest) request;
                    requestQueue.putPersonRequest(personRequest);
                }
            }
        }
    }
}