import com.oocourse.elevator1.PersonRequest;

import java.util.ArrayList;

public class Controller {
    private ArrayList<RequestsPool> requestsPools;
    private ArrayList<Elevator> elevators;
    private ArrayList<LookDecision> lookDecisions;
    //六个电梯分开

    public Controller() {
        requestsPools = new ArrayList<>();
        elevators = new ArrayList<>();
        lookDecisions = new ArrayList<>();
        for (int id = 1; id <= 6; id++) {
            addRequestPool(id);
        }
        for (int id = 1; id <= 6; id++) {
            addElevator(id);
        }
    }

    public void addRequest(PersonRequest request) {
        requestsPools.get(request.getElevatorId() - 1).addRequest(request);
    }

    private void addElevator(int id) {
        LookDecision lookDecision = new LookDecision(id, requestsPools.get(id - 1));
        lookDecisions.add(lookDecision);
        Elevator elevator = new Elevator(id, requestsPools.get(id - 1), lookDecision);
        elevator.start();
        elevators.add(elevator);
    }

    private void addRequestPool(int id) {
        RequestsPool requestsPool = new RequestsPool(id);
        requestsPools.add(requestsPool);
    }

    public void close() {
        for (RequestsPool requestsPool : requestsPools) {
            requestsPool.close(elevators.get(requestsPool.getElevatorId() - 1));
        }
    }
}
