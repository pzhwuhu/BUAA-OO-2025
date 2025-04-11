import com.oocourse.elevator3.PersonRequest;
import com.oocourse.elevator3.Request;
import com.oocourse.elevator3.ScheRequest;
import com.oocourse.elevator3.UpdateRequest;
import com.oocourse.elevator3.ElevatorInput;
import java.util.HashMap;

public class InputThread extends Thread {
    private final Requests mainRequests;
    private final HashMap<Integer, Requests> subRequestMap;
    private HashMap<Integer, ElevatorThread> elevatorMap = new HashMap<>();

    public InputThread(Requests mainRequests, HashMap<Integer, Requests> subRequestMap,
        HashMap<Integer, ElevatorThread> elevatorMap) {
        this.mainRequests = mainRequests;
        this.subRequestMap = subRequestMap;
        this.elevatorMap = elevatorMap;
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
                mainRequests.push(request);
            } else if (request instanceof ScheRequest) {
                ScheRequest scheRequest = (ScheRequest) request;
                subRequestMap.get(scheRequest.getElevatorId()).setScheRequest(scheRequest);
                //TimableOutput.println("SCHE-Set-" + scheRequest.getElevatorId());
            } else {
                UpdateRequest upRequest = (UpdateRequest) request;
                int idA = upRequest.getElevatorAId();
                int idB = upRequest.getElevatorBId();
                Coordinate coordinate = new Coordinate(upRequest);
                elevatorMap.get(idA).setCoordinate(coordinate, true);
                elevatorMap.get(idB).setCoordinate(coordinate, false);
                subRequestMap.get(idA).setUpdateRequest(upRequest);
                subRequestMap.get(idB).setUpdateRequest(upRequest);
            }
        }
    }
}
