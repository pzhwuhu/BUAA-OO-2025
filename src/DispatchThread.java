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
            } else {
                ScheRequest scheRequest = (ScheRequest) request;
                subRequestMap.get(scheRequest.getElevatorId()).setScheRequest(scheRequest);
                //TimableOutput.println("SCHE-Set-" + scheRequest.getElevatorId());
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
                    synchronized (subRequestMap.get(i)) {
                        if (subRequestMap.get(i).getFree() && subRequestMap.get(i).getSize() < 20) {
                            TimableOutput.println("RECEIVE-" + request.getPersonId() + "-" + i);
                            subRequestMap.get(i).push(request);
                            return; }
                    }
                }
            } else {
                TimableOutput.println("RECEIVE-" + request.getPersonId() + "-" + (counter + 1));
                subRequestMap.get(counter + 1).push(request);
                return;
                //+ "-" + subRequestMap.get(elevatorId).getFree()
            }
        }
        int random = random();
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

    public int inOrder() {
        return counter + 1;
    }
}
