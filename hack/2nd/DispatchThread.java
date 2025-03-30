import com.oocourse.elevator1.PersonRequest;
import elevator.RequestTable;

import java.util.HashMap;

public class DispatchThread extends Thread {
    private RequestPool requestPool;
    private HashMap<Integer, RequestTable> requestTables;

    public DispatchThread(RequestPool requestPool, HashMap<Integer, RequestTable> requestTables) {
        this.requestPool = requestPool;
        this.requestTables = requestTables;
    }

    @Override
    public void run() {
        while (true) {
            if (requestPool.isEmpty() && requestPool.isEnd()) {
                for (RequestTable requestTable : requestTables.values()) {
                    requestTable.setEnd();
                }
                // System.out.println("dispatch thread end");
                break;
            }

            PersonRequest request = requestPool.poll();

            if (request == null) {
                continue;
            }

            requestTables.get(request.getElevatorId()).addRequest(request);
        }
    }
}
