import com.oocourse.elevator1.PersonRequest;
import com.oocourse.elevator1.Request;
import com.oocourse.elevator1.TimableOutput;

import java.util.ArrayList;
import java.util.Iterator;

public class ElevatorThread extends Thread {
    private final int elevatorId;
    private final Requests subRequests;
    private final ArrayList<Integer> peopleInEle = new ArrayList<>();

    private int direction;
    private int floor;
    private int people;
    private Strategy strategy;

    public ElevatorThread(int elevatorId, Requests subRequests) {
        this.elevatorId = elevatorId;
        this.subRequests = subRequests;
        this.direction = 1;
        this.floor = 5; //5 -> F1
        this.people = 0;
    }

    @Override
    public void run() {
        Strategy strategy = new Strategy(subRequests);
        while (true) {
            Action action = strategy.getAction(people, floor, direction, peopleInEle);

            if (action == Action.MOVE) {
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
            }
            else if (action == Action.END) {
                break;
            }
            else if (action == Action.OPEN) {
                openAndClose();
            }
        }
    }

    public void move() {
        try {
            sleep(400);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        floor += direction;
        TimableOutput.println("ARRIVE-" + Strategy.convertToStr(floor) + "-" + elevatorId);
    }

    public void openAndClose() {
        TimableOutput.println("OPEN-" + Strategy.convertToStr(floor) + "-" + elevatorId);
        outPerson();
        try {
            sleep(400);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        inPerson();
        TimableOutput.println("CLOSE-" + Strategy.convertToStr(floor) + "-" + elevatorId);
    }

    public void inPerson() {
        if (people == 6) {
            return;
        }
        for (Request req : subRequests.getRequests()) {
            PersonRequest preq = (PersonRequest) req;
            if (floor == Strategy.convertToInt(preq.getFromFloor())) {
                int move = Strategy.convertToInt(preq.getToFloor()) - floor;
                if (move * direction > 0 && !peopleInEle.contains(preq.getPersonId())) {
                    peopleInEle.add(preq.getPersonId());
                    TimableOutput.println("IN-" + preq.getPersonId() + "-"
                        + Strategy.convertToStr(floor) + "-" + elevatorId);
                    people++;
                }
            }
        }
    }

    public void outPerson() {
        if (people == 0) {
            return;
        }
        Iterator<Request> iterator = subRequests.getRequests().iterator();
        while (iterator.hasNext()) {
            PersonRequest preq = (PersonRequest)iterator.next();
            if (Strategy.convertToInt(preq.getToFloor()) == floor
                && peopleInEle.contains(preq.getPersonId())) {
                peopleInEle.remove(Integer.valueOf(preq.getPersonId()));
                TimableOutput.println("OUT-" + preq.getPersonId() + "-"
                    + Strategy.convertToStr(floor) + "-" + elevatorId);
                people--;
                iterator.remove();
            }
        }
    }

}
