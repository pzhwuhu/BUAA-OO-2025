import com.oocourse.elevator1.PersonRequest;
import java.util.HashMap;

public class Dispatcher extends Thread {
    private final RequestQuene requests;
    private final HashMap<Integer, ProcessingQueue> processingQueues;
    
    public Dispatcher(RequestQuene requests, HashMap<Integer, ProcessingQueue> processingQueues) {
        this.requests = requests;
        this.processingQueues = processingQueues;
    }
    
    public void run() {
        while (true) {
            if (requests.isEmpty() && requests.isEnd()) {
                for (ProcessingQueue queue : processingQueues.values()) {
                    queue.setEnd();
                }
                if (Main.isDebug()) {
                    System.out.println("Dispatcher end");
                }
                break;
            }
            
            PersonRequest request = requests.getRequest();
            if (request == null) {
                continue;
            }
            dispatch(request);
        }
    }
    
    public int bestElevator(PersonRequest request) {
        return request.getElevatorId();
    }
    
    public void dispatch(PersonRequest request) {
        int elevatorId = bestElevator(request);
        ProcessingQueue processingQueue = processingQueues.get(elevatorId);
        if (Main.isDebug()) {
            System.out.println("Dispatcher dispatch " + request + " to Elevator-" + elevatorId);
        }
        Person person = new Person(request);
        processingQueue.addRequest(person);
    }
}
