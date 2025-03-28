import com.oocourse.elevator1.PersonRequest;
import com.oocourse.elevator1.Request;

import java.util.HashMap;

public class DispatchThread extends Thread {
    private final Requests mainRequests;
    private final HashMap<Integer, Requests> subRequestMap;

    public DispatchThread(Requests mainRequests, HashMap<Integer, Requests> subRequestMap) {
        this.mainRequests = mainRequests;
        this.subRequestMap = subRequestMap;
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
            }
        }
    }

    public void personDispatch(PersonRequest request) {
        Integer elevatorId = request.getElevatorId();
        subRequestMap.get(elevatorId).push(request);
    }
}
