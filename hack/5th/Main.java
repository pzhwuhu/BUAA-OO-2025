import com.oocourse.elevator1.TimableOutput;

import java.util.HashMap;

public class Main {
    private static boolean DEBUG;
    
    public static void main(String[] args) {
        DEBUG = false;
        TimableOutput.initStartTimestamp();
        
        RequestQuene requests = new RequestQuene();
        HashMap<Integer, ProcessingQueue> processingQueues = new HashMap<>();
        Dispatcher dispatcher = new Dispatcher(requests, processingQueues);
        dispatcher.start();
        for (int i = 1; i <= 6; i++) {
            ProcessingQueue processingQueue = new ProcessingQueue();
            processingQueues.put(i, processingQueue);
            Elevator elevator = new Elevator(i, processingQueue, new LookStragety());
            elevator.start();
        }
        Input input = new Input(requests);
        input.start();
    }
    
    public static boolean isDebug() {
        return DEBUG;
    }
}
