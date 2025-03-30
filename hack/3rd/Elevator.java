import com.oocourse.elevator1.TimableOutput;

import java.util.HashSet;
import java.util.Iterator;

public class Elevator {
    private String id;
    private RequestTable requestTable;
    public static final int capacity = 6;
    //TODO:可能存在找过容量的bug
    private Floor currentFloor = new Floor(0);
    private int direction = 1;
    private HashSet<Person> peopleInElevator = new HashSet<Person>();
    //Optimize: record lastTime
    private long lastTime = -1;

    public Elevator(String id, RequestTable requestTable) {
        this.id = id;
        this.requestTable = requestTable;
    }

    int getDirection() {
        return direction;
    }

    void setDirection(int newDirection) {
        direction = newDirection;
    }

    RequestTable getRequestTable() {
        return requestTable;
    }

    Advice getAdvice() {
        //如果有人可以进出，则打开门
        if (needOpenDoorIn() || needOpenDoorOut()) {
            return Advice.OPEN;
        }
        //否则，如果有人在电梯里，则需要移动
        if (!peopleInElevator.isEmpty()) {
            return Advice.MOVE;
        } else {
            //如果这个电梯的等待队列为空
            if (requestTable.isEmpty()) {
                if (requestTable.isOver()) {
                    return Advice.OVER;
                } else {
                    return Advice.WAIT;
                }
            }
            //如果这个电梯还有人在等
            else {
                if (requestTable.hasReqInDirection(direction, currentFloor)) {
                    return Advice.MOVE;
                } else {
                    return Advice.REVERSE;
                }
            }
        }
        // TODO: implement logic to get advice based on situation

    }

    boolean needOpenDoorIn() {
        for (Person person : requestTable.getPersonQueue(currentFloor)) {
            if (person.getFromFloor().equals(currentFloor) && person.getDirection() == direction) {
                return true;
            }
        }
        return false;
    }

    boolean needOpenDoorOut() {
        for (Person person : peopleInElevator) {
            if (person.getToFloor().equals(currentFloor)) {
                return true;
            }
        }
        return false;
    }

    void move() {
        long currentTime = System.currentTimeMillis();
        // 如果第一次移动，那一定得400ms
        if (lastTime == -1) {
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else if (currentTime - lastTime < 400) {
            try {
                Thread.sleep(400 - currentTime + lastTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        if (direction == 1) {
            currentFloor.up();
        } else if (direction == -1) {
            currentFloor.down();
        }
        lastTime = TimableOutput.println("ARRIVE-" + currentFloor.toString() + "-" + id);
    }

    void openAndClose() {
        TimableOutput.println("OPEN-" + currentFloor.toString() + "-" + id);
        //把所有人都赶下去
        Iterator<Person> iteratorIn = peopleInElevator.iterator();
        while (iteratorIn.hasNext()) {
            Person person = iteratorIn.next();
            TimableOutput.println("OUT-" + person.getId()
                    + "-" + currentFloor.toString() + "-" + id);
            iteratorIn.remove();
            person.setFromFloor(currentFloor);
            requestTable.addPerson(currentFloor, person);
        }
        //对于电梯外的人
        Iterator<Person> iterator = requestTable.getPersonQueue(currentFloor).iterator();
        int isFirst = 1;
        //以优先级最高的人为准方向，同方向的人一起上
        while (iterator.hasNext()) {
            Person person = iterator.next();
            if (person.getToFloor().equals(currentFloor)) {
                iterator.remove();
                continue;
            }
            if (isFirst == 1 && person.getFromFloor().equals(currentFloor)
                    && peopleInElevator.size() < capacity) {
                TimableOutput.println("IN-" + person.getId()
                        + "-" + currentFloor.toString() + "-" + id);
                peopleInElevator.add(person);
                iterator.remove();
                direction = person.getDirection();
                isFirst = 0;
            } else if (person.getFromFloor().equals(currentFloor)
                    && person.getDirection() == direction
                    && peopleInElevator.size() < capacity) {
                TimableOutput.println("IN-" + person.getId()
                        + "-" + currentFloor.toString() + "-" + id);
                peopleInElevator.add(person);
                iterator.remove();
            }
        }
        try {
            Thread.sleep(400);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        lastTime = TimableOutput.println("CLOSE-" + currentFloor.toString() + "-" + id);
    }

}
