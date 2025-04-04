import com.oocourse.elevator2.PersonRequest;
import com.oocourse.elevator2.Request;
import com.oocourse.elevator2.ScheRequest;
import com.oocourse.elevator2.TimableOutput;

import java.util.HashMap;
import java.util.Random;

public class DispatchThread extends Thread {
    private final Requests mainRequests;
    private final HashMap<Integer, Requests> subRequestMap;
    private int counter;

    public DispatchThread(Requests mainRequests, HashMap<Integer, Requests> subRequestMap) {
        this.mainRequests = mainRequests;
        this.subRequestMap = subRequestMap;
        counter = -1;
    }

    @Override
    public void run() {
        while (true) {
            if (mainRequests.isEmpty() && mainRequests.isDone()) {
                for (Requests subRequest : subRequestMap.values()) {
                    subRequest.setDone();
                }
                break;
            }

            Request request = mainRequests.pop();
            if (request == null) {
                continue;
            }
            if (request instanceof PersonRequest) {
                PersonRequest personRequest = (PersonRequest) request;
                personDispatch(personRequest);
            } else {
                ScheRequest scheRequest = (ScheRequest) request;
                subRequestMap.get(scheRequest.getElevatorId()).setScheRequest(scheRequest);
                //TimableOutput.println("SCHE-Set-" + scheRequest.getElevatorId());
            }
        }
    }

    public void personDispatch(PersonRequest request) {
        int elevatorId = inOrder();
        subRequestMap.get(elevatorId).push(request);
        TimableOutput.println("RECEIVE-" + request.getPersonId() + "-" + elevatorId);
    }

    public int random() {
        Random random = new Random();
        return random.nextInt(6) + 1;
    }

    public int inOrder() {
        counter++;
        counter %= 6;
        return counter + 1;
    }
}
