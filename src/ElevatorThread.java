import com.oocourse.elevator3.PersonRequest;
import com.oocourse.elevator3.Request;
import com.oocourse.elevator3.ScheRequest;
import com.oocourse.elevator3.UpdateRequest;
import com.oocourse.elevator3.TimableOutput;

import java.util.ArrayList;
import java.util.Iterator;

public class ElevatorThread extends Thread {
    private final int elevatorId;
    private final Requests mainRequests;
    private final Requests subRequests;
    private final ArrayList<Integer> peopleInEle = new ArrayList<>();
    private Coordinate coordinate;
    private boolean isA = false;

    private int direction;
    private int floor;
    private int people = 0;
    private Strategy strategy;
    private int sharedFloor = 100;
    private int speed = 400;

    public int getSharedFloor() {
        return sharedFloor;
    }

    public boolean isA() {
        return isA;
    }

    public ElevatorThread(int elevatorId, Requests mainRequests, Requests subRequests) {
        this.elevatorId = elevatorId;
        this.mainRequests = mainRequests;
        this.subRequests = subRequests;
        this.direction = 1;
        this.floor = 5; //5 -> F1
    }

    public void setCoordinate(Coordinate coordinate, boolean isA) {
        this.coordinate = coordinate;
        this.isA = isA;
    }

    @Override
    public void run() {
        Strategy strategy = new Strategy(subRequests);
        while (true) {
            Action action = strategy.getAction(people, floor, direction,
                peopleInEle, sharedFloor, isA);

            if (action == Action.SCHE) {
                tmpShedule();
            }
            else if (action == Action.UPD) {
                updateBegin();
            }
            else if (action == Action.MOVE) {
                move();
            }
            else if (action == Action.WAIT) {
                synchronized (subRequests) {
                    try {
                        subRequests.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            else if (action == Action.REVERSE) {
                direction = -direction;
                //TimableOutput.println(elevatorId + "Reverse direction: " + direction);
            }
            else if (action == Action.END) {
                break;
            }
            else if (action == Action.OPEN) {
                openAndClose();
            }
        }
    }

    public void updateBegin() {
        subRequests.setFree(false);
        if (!subRequests.isEmpty()) {
            TimableOutput.println("OPEN-" + Strategy.toStr(floor) + "-" + elevatorId);
            scheOutPerson();
            TimableOutput.println("CLOSE-" + Strategy.toStr(floor) + "-" + elevatorId);
        }
        coordinate.setReady(isA);
        synchronized (coordinate) {
            if (!coordinate.isReady()) {
                try {
                    coordinate.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        reSchedule();
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        UpdateRequest upReq = subRequests.getUpdateRequest();
        speed = 200;
        if (isA) {
            sharedFloor = Strategy.toInt(upReq.getTransferFloor());
            floor = sharedFloor + 1;
        } else {
            sharedFloor = Strategy.toInt(upReq.getTransferFloor());
            floor = sharedFloor - 1;
        }
        coordinate.endUpdate(isA);
        synchronized (coordinate) {
            if (!coordinate.isEnd()) {
                try {
                    coordinate.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        subRequests.setUpdateRequest(null);
        subRequests.setFree(true);
    }

    public void tmpShedule() {
        subRequests.setFree(false);
        TimableOutput.println("SCHE-BEGIN-" + elevatorId);
        reSchedule();
        ScheRequest scheRequest = subRequests.getScheRequest();
        int toFloor = Strategy.toInt(scheRequest.getToFloor());
        if ((toFloor - floor) * direction < 0) {
            direction = -direction;
        }
        while (floor != toFloor) {
            try {
                sleep((int)(1000 * scheRequest.getSpeed()));
                floor += direction;
                TimableOutput.println("ARRIVE-" + Strategy.toStr(floor) + "-" + elevatorId);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        TimableOutput.println("OPEN-" + Strategy.toStr(floor) + "-" + elevatorId);
        scheOutPerson();
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        TimableOutput.println("CLOSE-" + Strategy.toStr(floor) + "-" + elevatorId);
        TimableOutput.println("SCHE-END-" + elevatorId);
        subRequests.setScheRequest(null);
        subRequests.setFree(true);
    }

    public void reSchedule() {
        synchronized (subRequests) {
            Iterator<Request> iterator = subRequests.getRequests().iterator();
            while (iterator.hasNext()) {
                PersonRequest preq = (PersonRequest)iterator.next();
                int id = preq.getPersonId();
                if (!peopleInEle.contains(id)) {
                    mainRequests.push(preq);
                    iterator.remove();
                    //TimableOutput.println(personId + " need to be redispatched");
                }
            }
        }
    }

    public void scheOutPerson() {
        synchronized (subRequests) {
            Iterator<Request> iterator = subRequests.getRequests().iterator();
            while (iterator.hasNext()) {
                PersonRequest preq = (PersonRequest)iterator.next();
                String strFloor = Strategy.toStr(floor);
                int toFloor = Strategy.toInt(preq.getToFloor());
                int id = preq.getPersonId();
                if (peopleInEle.contains(id)) {
                    people--;
                    peopleInEle.remove(Integer.valueOf(id));
                    if (toFloor == floor) {
                        iterator.remove();
                        mainRequests.addArrive();
                        TimableOutput.println("OUT-S-" + id + "-" + strFloor + "-" + elevatorId);
                    } else {
                        ReArrangeRequest raq = new ReArrangeRequest(floor, preq);
                        TimableOutput.println("OUT-F-" + id + "-" + strFloor + "-" + elevatorId);
                        mainRequests.push(raq);
                        iterator.remove();
                    }
                }
            }
        }
    }

    public void move() {
        try {
            sleep(speed);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        floor += direction;
        if (floor == sharedFloor) {
            coordinate.inShared();
        }
        TimableOutput.println("ARRIVE-" + Strategy.toStr(floor) + "-" + elevatorId);
        if (floor == sharedFloor + direction) {
            coordinate.outShared();
        }
    }

    public void openAndClose() {
        TimableOutput.println("OPEN-" + Strategy.toStr(floor) + "-" + elevatorId);
        outPerson();
        try {
            sleep(400);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        inPerson();
        TimableOutput.println("CLOSE-" + Strategy.toStr(floor) + "-" + elevatorId);
    }

    public void inPerson() {
        if (people == 6) {
            return;
        }
        synchronized (subRequests) {
            subRequests.sortByPriority();
            for (Request req : subRequests.getRequests()) {
                if (people >= 6) {
                    break;
                }
                PersonRequest preq = (PersonRequest) req;
                if (floor == Strategy.toInt(preq.getFromFloor())) {
                    int move = Strategy.toInt(preq.getToFloor()) - floor;
                    if (move * direction > 0 && !peopleInEle.contains(preq.getPersonId())) {
                        peopleInEle.add(preq.getPersonId());
                        TimableOutput.println("IN-" + preq.getPersonId() + "-"
                            + Strategy.toStr(floor) + "-" + elevatorId);
                        people++;
                    }
                }
            }
        }
    }

    public void outPerson() {
        if (people == 0) {
            return;
        }
        synchronized (subRequests) {
            Iterator<Request> iterator = subRequests.getRequests().iterator();
            if (floor == sharedFloor) {
                scheOutPerson();
                direction = -direction;
                //TimableOutput.println(elevatorId + "Reverse direction: " + direction);
                return;
            }
            while (iterator.hasNext()) {
                PersonRequest preq = (PersonRequest)iterator.next();
                if (Strategy.toInt(preq.getToFloor()) == floor
                    && peopleInEle.contains(preq.getPersonId())) {
                    mainRequests.addArrive();
                    peopleInEle.remove(Integer.valueOf(preq.getPersonId()));
                    TimableOutput.println("OUT-S-" + preq.getPersonId() + "-"
                        + Strategy.toStr(floor) + "-" + elevatorId);
                    people--;
                    iterator.remove();
                }
            }
        }
    }

}
