package elevator;

import com.oocourse.elevator1.PersonRequest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class ElevatorContext {

    public enum State {
        OPEN, CLOSED
    }

    // 电梯参数
    private int id;
    private int speed;
    private int capacity;
    private int intervalOfDoor;

    // 电梯状态
    private String curFloor;
    private int personCnt;
    private State doorState;

    // 正在处理的请求
    private HashSet<PersonRequest> dealingRequests;

    public ElevatorContext(int id) {
        this.id = id;
        this.speed = 400;
        this.capacity = 6;
        this.intervalOfDoor = 400;

        this.curFloor = "F1";
        this.personCnt = 0;
        this.doorState = State.CLOSED;
        this.dealingRequests = new HashSet<>();
    }

    public void inPerson(PersonRequest personRequest) {
        this.personCnt++;
        dealingRequests.add(personRequest);
    }

    public void outPerson(PersonRequest personRequest) {
        this.personCnt--;
        dealingRequests.remove(personRequest);
    }

    public boolean isEmpty() {
        return this.personCnt == 0;
    }

    public boolean isFull() {
        return this.personCnt == this.capacity;
    }

    public PersonRequest getMainRequest() {
        int prior = Integer.MIN_VALUE;
        int distance = Integer.MAX_VALUE;
        PersonRequest mainRequest = null;
        for (PersonRequest p : dealingRequests) {
            if (p.getPriority() > prior) {
                prior = p.getPriority();
                mainRequest = p;
            } else if (p.getPriority() == prior &&
                FloorService.distance(curFloor, p.getToFloor()) < distance) {
                distance = FloorService.distance(curFloor, p.getToFloor());
                mainRequest = p;
            }
        }
        return mainRequest;
    }

    public String getCurFloor() {
        return curFloor;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setIntervalOfDoor(int intervalOfDoor) {
        this.intervalOfDoor = intervalOfDoor;
    }

    public void setCurFloor(String curFloor) {
        this.curFloor = curFloor;
    }

    public int getPersonCnt() {
        return personCnt;
    }

    public void setPersonCnt(int personCnt) {
        this.personCnt = personCnt;
    }

    public HashSet<PersonRequest> getDealingRequests() {
        return dealingRequests;
    }

    public void setDealingRequests(HashSet<PersonRequest> dealingRequests) {
        this.dealingRequests = dealingRequests;
    }

    public boolean isOpen() {
        return doorState == State.OPEN;
    }

    public boolean hasArrival() {
        for (PersonRequest p : dealingRequests) {
            if (p.getToFloor().equals(curFloor)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<PersonRequest> arrive() {
        ArrayList<PersonRequest> arrivals = new ArrayList<>();
        Iterator<PersonRequest> iterator = dealingRequests.iterator();
        while (iterator.hasNext()) {
            PersonRequest personRequest = iterator.next();
            if (personRequest.getToFloor().equals(curFloor)) {
                arrivals.add(personRequest);
                personCnt--;
                iterator.remove();
            }
        }
        return arrivals;
    }

    public int getLeftCnt() {
        return capacity - personCnt;
    }

    public int getIntervalOfDoor() {
        return intervalOfDoor;
    }

    public int getSpeed() {
        return speed;
    }

    public void openDoor() {
        this.doorState = State.OPEN;
    }

    public void closeDoor() {
        this.doorState = State.CLOSED;
    }
}
