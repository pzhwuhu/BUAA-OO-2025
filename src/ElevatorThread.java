import com.oocourse.elevator2.PersonRequest;
import com.oocourse.elevator2.Request;
import com.oocourse.elevator2.ScheRequest;
import com.oocourse.elevator2.TimableOutput;

import java.util.ArrayList;
import java.util.Iterator;

public class ElevatorThread extends Thread {
    private final int elevatorId;
    private final Requests mainRequests;
    private final Requests subRequests;
    private final ArrayList<Integer> peopleInEle = new ArrayList<>();

    private int direction;
    private int floor;
    private int people;
    private Strategy strategy;

    public ElevatorThread(int elevatorId, Requests mainRequests, Requests subRequests) {
        this.elevatorId = elevatorId;
        this.mainRequests = mainRequests;
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

            if (action == Action.SCHE) {
                tmpShedule();
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
            }
            else if (action == Action.END) {
                break;
            }
            else if (action == Action.OPEN) {
                openAndClose();
            }
        }
    }

    public void tmpShedule() {
        TimableOutput.println("SCHE-BEGIN-" + elevatorId);
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
    }

    public void scheOutPerson() {
        synchronized (subRequests) {
            Iterator<Request> iterator = subRequests.getRequests().iterator();
            while (iterator.hasNext()) {
                PersonRequest preq = (PersonRequest)iterator.next();
                String str = preq.getToFloor();
                int toFloor = Strategy.toInt(str);
                int personId = preq.getPersonId();
                if (peopleInEle.contains(personId)) {
                    people--;
                    peopleInEle.remove(Integer.valueOf(personId));
                    if (toFloor == floor) {
                        TimableOutput.println("OUT-S-" + personId + "-" + str + "-" + elevatorId);
                        iterator.remove();
                    } else {
                        mainRequests.push(preq);
                        iterator.remove();
                        TimableOutput.println("OUT-F-" + personId + "-" + str + "-" + elevatorId);
                    }
                } else {
                    mainRequests.push(preq);
                    iterator.remove();
                    //TimableOutput.println(personId + " need to be redispatched");
                }
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
        TimableOutput.println("ARRIVE-" + Strategy.toStr(floor) + "-" + elevatorId);
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
            while (iterator.hasNext()) {
                PersonRequest preq = (PersonRequest)iterator.next();
                if (Strategy.toInt(preq.getToFloor()) == floor
                    && peopleInEle.contains(preq.getPersonId())) {
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
