import com.oocourse.elevator3.PersonRequest;
import com.oocourse.elevator3.Request;
import com.oocourse.elevator3.TimableOutput;

import java.util.HashMap;
import java.util.Random;

public class DispatchThread extends Thread {
    private final Requests mainRequests;
    private final HashMap<Integer, Requests> subRequestMap;
    private HashMap<Integer, ElevatorThread> elevatorMap = new HashMap<>();
    private int counter;

    public DispatchThread(Requests mainRequests, HashMap<Integer, Requests> subRequestMap,
        HashMap<Integer, ElevatorThread> elevatorMap) {
        this.mainRequests = mainRequests;
        this.subRequestMap = subRequestMap;
        this.elevatorMap = elevatorMap;
        counter = -1;
    }

    @Override
    public void run() {
        while (true) {
            if (mainRequests.isEmpty() && mainRequests.isDone()) {
                for (Requests subRequest : subRequestMap.values()) {
                    subRequest.setDone();
                }
                //TimableOutput.println("DispatchThread is over");
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
        counter++;
        counter %= 6;
        Requests requests = subRequestMap.get(counter + 1);
        synchronized (requests) {
            if (!requests.getFree()) {
                for (int i = 1;i <= 6;i++) {
                    if (!isInRange(i, request)) { continue; }
                    synchronized (subRequestMap.get(i)) {
                        if (subRequestMap.get(i).getFree() && subRequestMap.get(i).getSize() < 20) {
                            TimableOutput.println("RECEIVE-" + request.getPersonId() + "-" + i);
                            subRequestMap.get(i).push(request);
                            return; }
                    }
                }
            } else if (isInRange(counter + 1, request)) {
                TimableOutput.println("RECEIVE-" + request.getPersonId() + "-" + (counter + 1));
                subRequestMap.get(counter + 1).push(request);
                return;
                //+ "-" + subRequestMap.get(elevatorId).getFree()
            }
        }
        int random = random();
        while (!isInRange(random, request)) {
            random = random();
        }
        requests = subRequestMap.get(random);
        synchronized (requests) {
            while (!requests.getFree()) {
                //TimableOutput.println("elevator" + counter + "is wait-" + requests.getFree());
                try {
                    requests.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e); }
            }
            TimableOutput.println("RECEIVE-" + request.getPersonId() + "-" + random);
            subRequestMap.get(random).push(request);
            //TimableOutput.println("elevator-" + counter + "-is off-" + requests.getFree());
        }
    }

    public int random() {
        Random random = new Random();
        return random.nextInt(6) + 1;
    }

    public boolean isInRange(int eleId, PersonRequest request) {
        ElevatorThread ele = elevatorMap.get(eleId);
        int sharedFloor = ele.getSharedFloor();
        if (sharedFloor == 100) {
            return true;
        }
        else {
            int fromFloor = Strategy.toInt(request.getFromFloor());
            int toFloor = Strategy.toInt(request.getToFloor());
            if (ele.isA()) {
                if (fromFloor > sharedFloor) {
                    return true;
                } else if (fromFloor == sharedFloor) {
                    return toFloor > sharedFloor;
                }
            } else {
                if (fromFloor < sharedFloor) {
                    return true;
                } else if (fromFloor == sharedFloor) {
                    return toFloor < sharedFloor;
                }
            }
            return false;
        }
    }
}
