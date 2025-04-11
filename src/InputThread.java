import com.oocourse.elevator2.ElevatorInput;
import com.oocourse.elevator2.PersonRequest;
import com.oocourse.elevator2.Request;
import com.oocourse.elevator2.ScheRequest;

import java.util.HashMap;

public class InputThread extends Thread {
    private final Requests mainRequests;
    private final HashMap<Integer, Requests> subRequestMap;

    public InputThread(Requests mainRequests, HashMap<Integer, Requests> subRequestMap) {
        this.mainRequests = mainRequests;
        this.subRequestMap = subRequestMap;
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

            if (request instanceof PersonRequest) {
                PersonRequest personRequest = (PersonRequest) request;
                mainRequests.addNeed(personRequest.getPersonId());
            } else {
                ScheRequest scheRequest = (ScheRequest) request;
                subRequestMap.get(scheRequest.getElevatorId()).setScheRequest(scheRequest);
                //TimableOutput.println("SCHE-Set-" + scheRequest.getElevatorId());
                continue;
            }
            mainRequests.push(request);
        }
    }
}
