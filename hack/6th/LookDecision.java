import com.oocourse.elevator1.PersonRequest;
import com.oocourse.elevator1.TimableOutput;

import java.util.ArrayList;

public class LookDecision {
    private int elevatorId;
    private RequestsPool requestsPool;
    private ArrayList<PersonRequest> needToDown = new ArrayList<>(); //需要下电梯的人
    private ArrayList<PersonRequest> needToUp = new ArrayList<>(); //需要上电梯的人
    private boolean openFlag;

    public LookDecision(int elevatorId, RequestsPool requestsPool) {
        this.elevatorId = elevatorId;
        this.requestsPool = requestsPool;
    }

    public void operate(Elevator elevator) {
        elevator.move();
        elevatorUpAndDown(elevator);
        if (!elevator.getInsidePerson().isEmpty()) { //电梯内部仍然有请求
            // 判断方向
            boolean flag = false;
            for (PersonRequest personRequest : elevator.getInsidePerson()) {
                int personDirection;
                personDirection = getPersonDirection(elevator.getAtFloor(),
                        personRequest.getToFloor());
                if (personDirection == elevator.getDirection()) {
                    flag = true;
                    break;
                }
            }
            if (!flag) { //没有同向请求则反向
                if (elevator.getDirection() != 0) {
                    elevator.setDirection(-elevator.getDirection());
                } else {
                    int personDirection;
                    personDirection = getPersonDirection(elevator.getAtFloor(),
                            elevator.getInsidePerson().get(0).getToFloor());
                    elevator.setDirection(personDirection);
                }
            }
        } else {
            if (requestsPool.isEmpty()) {
                elevator.setDirection(0);
            } else {
                boolean flag = false;
                ArrayList<PersonRequest> personRequests1 = new ArrayList<>();
                personRequests1.addAll(requestsPool.getRequest());
                for (PersonRequest personRequest : personRequests1) {
                    int personDirection;
                    personDirection = getPersonDirection(elevator.getAtFloor(),
                            personRequest.getFromFloor());
                    if (personDirection == elevator.getDirection()) {
                        flag = true;
                        break;
                    }
                }
                if (!flag) { //没有同向请求则反向
                    if (elevator.getDirection() != 0) {
                        elevator.setDirection(-elevator.getDirection());
                    } else {
                        int personDirection;
                        personDirection = getPersonDirection(elevator.getAtFloor(),
                                personRequests1.get(0).getFromFloor());
                        elevator.setDirection(personDirection);
                    }
                }
            }
        }
    }

    private void elevatorUpAndDown(Elevator elevator) {
        openFlag = false;
        needToDown.clear();
        for (PersonRequest personRequest : elevator.getInsidePerson()) {
            if (personRequest.getToFloor().equals(elevator.getAtFloor())) {
                needToDown.add(personRequest);
                openFlag = true;
            }
        } //下电梯
        needToUp.clear();
        ArrayList<PersonRequest> personRequestsUp = new ArrayList<>();
        personRequestsUp.addAll(requestsPool.getRequest());
        for (PersonRequest personRequest : personRequestsUp) {
            if (personRequest.getFromFloor().equals(elevator.getAtFloor())) {
                int personDirection;
                String s1 = personRequest.getFromFloor();
                String s2 = personRequest.getToFloor();
                int f1 = shiftFloor(s1);
                int f2 = shiftFloor(s2);
                if (f1 > f2) {
                    personDirection = -1;
                } else {
                    personDirection = 1;
                }
                if (((personDirection == elevator.getDirection() || elevator.getDirection() == 0)
                    || elevator.getInsidePerson().isEmpty())
                    && elevator.getInsidePerson().size() <= 5
                    && !elevator.getInsidePerson().contains(personRequest)) {
                    needToUp.add(personRequest);
                    openFlag = true;
                }
            }
        } //上电梯 判断capacity
        if (openFlag) { //开门
            elevator.open();
        }
        openFlag = false;//恢复
        ArrayList<PersonRequest> personRequests = new ArrayList<>();
        personRequests.addAll(elevator.getInsidePerson());
        for (PersonRequest personRequest : needToDown) {
            TimableOutput.println(String.format("OUT-%d-%s-%d",
                personRequest.getPersonId(), personRequest.getToFloor(),
                personRequest.getElevatorId()));
            personRequests.remove(personRequest);
            requestsPool.clearRequests(personRequest);
        }
        for (PersonRequest personRequest : needToUp) {
            TimableOutput.println(String.format("IN-%d-%s-%d",
                personRequest.getPersonId(), personRequest.getFromFloor(),
                personRequest.getElevatorId()));
            personRequests.add(personRequest);
        }
        elevator.setInsidePerson(personRequests);
        if (elevator.getDoorOpen()) {
            elevator.close();
        }
    }

    private int getPersonDirection(String floor1, String floor2) {
        int personDirection = 0;
        int f1;
        int f2;
        f1 = shiftFloor(floor1);
        f2 = shiftFloor(floor2);
        if (f1 > f2) {
            personDirection = -1;
        } else if (f1 < f2) {
            personDirection = 1;
        }
        return personDirection;
    }

    public int shiftFloor(String floor) {
        int result = 0;
        switch (floor) {
            case "F1" :
                result = 1;
                break;
            case "F2" :
                result = 2;
                break;
            case "F3" :
                result = 3;
                break;
            case "F4" :
                result = 4;
                break;
            case "F5" :
                result = 5;
                break;
            case "F6" :
                result = 6;
                break;
            case "F7" :
                result = 7;
                break;
            case "B1" :
                result = -1;
                break;
            case "B2" :
                result = -2;
                break;
            case "B3" :
                result = -3;
                break;
            case "B4" :
                result = -4;
                break;
            default:
                result = 0;
        }
        return result;
    }

}
