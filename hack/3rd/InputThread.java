import com.oocourse.elevator1.ElevatorInput;
import com.oocourse.elevator1.PersonRequest;
import com.oocourse.elevator1.Request;

import java.io.IOException;
import java.util.HashMap;

public class InputThread extends Thread {
    private HashMap<Integer, RequestTable> requestTables = new HashMap<>();

    public InputThread() {
        for (int i = 1; i < 7; i++) {
            RequestTable requestTable = new RequestTable();
            requestTables.put(i, requestTable);
            Elevator eleveator = new Elevator(String.valueOf(i), requestTable);
            ElevatorThread elevatorThread = new ElevatorThread(eleveator); // 创建电梯线程
            elevatorThread.start();
        }
    }

    @Override
    public void run() {
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        while (true) {
            Request request = elevatorInput.nextRequest();
            // when request == null
            // it means there are no more lines in stdin
            if (request == null) {
                //告诉所有电梯线程结束
                for (int i = 1; i < 7; i++) {
                    requestTables.get(i).setOver(true);
                }
                break;
            } else {
                // a new valid request
                if (request instanceof PersonRequest) {
                    PersonRequest personRequest = (PersonRequest) request;
                    dispatch(personRequest);
                }
            }
        }
        try {
            elevatorInput.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void dispatch(PersonRequest personRequest) {
        Person person = new Person(personRequest.getPersonId(), personRequest.getPriority()
                , new Floor(personRequest.getFromFloor()), new Floor(personRequest.getToFloor()));

        requestTables.get(personRequest.getElevatorId())
                .addPerson(new Floor(personRequest.getFromFloor()), person);
    }
}
