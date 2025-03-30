import com.oocourse.elevator1.PersonRequest;
import com.oocourse.elevator1.TimableOutput;

import java.util.ArrayList;

public class ElevatorThread extends Thread {
    private int elevatorId;
    private WaitingQueue waitingQueue;
    private int floor;
    private ArrayList<PersonRequest> passengers;
    private boolean isOpen = false;
    private int direction = 0;

    public ElevatorThread(int id, WaitingQueue queue) {
        this.elevatorId = id;
        this.waitingQueue = queue;
        this.floor = 1;
        passengers = new ArrayList<>();
    }

    @Override
    public void run() {
        while (true) {
            if (waitingQueue.isEmpty() && waitingQueue.isEnd() && passengers.isEmpty()) {
                break;
            }
            ArrayList<PersonRequest> requests = waitingQueue.getPersonRequests(passengers);
            if (requests == null && passengers.isEmpty()) {
                continue;
            }
            //运行方向1向上-1向下
            direction = nextDirection(requests);
            //出入电梯
            leavePassenger();
            enterPassenger(requests);
            if (isOpen) {
                try {
                    sleep(400);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                requests = waitingQueue.getPersonRequests(passengers);
                enterPassenger(requests);
                TimableOutput.println("CLOSE-" + getFloorName(floor) + "-" + elevatorId);
                isOpen = false;
            }
            //再次判断是否需要移动
            if (waitingQueue.isEmpty() && waitingQueue.isEnd() && passengers.isEmpty()) {
                break;
            }
            requests = waitingQueue.getPersonRequests(passengers);
            if (requests == null && passengers.isEmpty()) {
                continue;
            }
            direction = nextDirection(requests);
            //移动
            try {
                sleep(400);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            floor += direction;
            TimableOutput.println("ARRIVE-" + getFloorName(floor) + "-" + elevatorId);
        }
    }

    private int nextDirection(ArrayList<PersonRequest> requests) {
        PersonRequest main = null;
        int maxPri = 0;
        if (!passengers.isEmpty()) {
            for (PersonRequest personRequest : passengers) {
                if (personRequest.getPriority() > maxPri) {
                    maxPri = personRequest.getPriority();
                    main = personRequest;
                }
            }
            if (main != null) {
                return getFloorNum(main.getToFloor()) - getFloorNum(main.getFromFloor()) > 0
                        ? 1 : -1;
            }
        }
        for (PersonRequest personRequest : requests) {
            if (personRequest.getPriority() > maxPri) {
                maxPri = personRequest.getPriority();
                main = personRequest;
            }
        }
        return getFloorNum(main.getFromFloor()) - floor > 0 ? 1 : -1;
    }

    private void leavePassenger() {
        ArrayList<PersonRequest> temp = new ArrayList<>();
        for (PersonRequest personRequest : passengers) {
            temp.add(personRequest);
        }
        for (PersonRequest personRequest : temp) {
            if (getFloorNum(personRequest.getToFloor()) == floor) {
                if (!isOpen) {
                    TimableOutput.println("OPEN-" + personRequest.getToFloor() + "-" + elevatorId);
                }
                isOpen = true;
                TimableOutput.println("OUT-" + personRequest.getPersonId() +
                        "-" + personRequest.getToFloor() + "-" + elevatorId);
                passengers.remove(personRequest);
            }
        }
    }

    private void enterPassenger(ArrayList<PersonRequest> requests) {
        if (requests == null) {
            return;
        }
        for (PersonRequest personRequest : requests) {
            int direction = getFloorNum(personRequest.getToFloor()) -
                    getFloorNum(personRequest.getFromFloor()) > 0 ? 1 : -1;
            if (getFloorNum(personRequest.getFromFloor()) == floor &&
                    (direction == this.direction || passengers.isEmpty()) &&
                    passengers.size() < 6) {
                if (!isOpen) {
                    TimableOutput.println("OPEN-" + personRequest.getFromFloor() +
                            "-" + elevatorId);
                }
                isOpen = true;
                TimableOutput.println("IN-" + personRequest.getPersonId() + "-" +
                        personRequest.getFromFloor() + "-" + elevatorId);
                waitingQueue.removePersonRequest(personRequest);
                passengers.add(personRequest);
            }
        }
    }

    private int getFloorNum(String floorName) {
        switch (floorName) {
            case "B4":
                return -3;
            case "B3":
                return -2;
            case "B2":
                return -1;
            case "B1":
                return 0;
            case "F1":
                return 1;
            case "F2":
                return 2;
            case "F3":
                return 3;
            case "F4":
                return 4;
            case "F5":
                return 5;
            case "F6":
                return 6;
            case "F7":
                return 7;
            default:
                System.out.println("error: Unknown floor");
                return 8;
        }
    }

    private String getFloorName(int floorNum) {
        switch (floorNum) {
            case -3:
                return "B4";
            case -2:
                return "B3";
            case -1:
                return "B2";
            case 0:
                return "B1";
            case 1:
                return "F1";
            case 2:
                return "F2";
            case 3:
                return "F3";
            case 4:
                return "F4";
            case 5:
                return "F5";
            case 6:
                return "F6";
            case 7:
                return "F7";
            default:
                System.out.println("error: Unknown floor");
                return "";
        }
    }
}
