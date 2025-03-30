import com.oocourse.elevator1.PersonRequest;
import com.oocourse.elevator1.TimableOutput;
import elevator.ElevatorContext;
import elevator.FloorService;
import elevator.RequestTable;

import java.util.ArrayList;
import java.util.List;

public class ElevatorThread extends Thread {
    private ElevatorContext elevator;
    private RequestTable requestTable;
    private PersonRequest mainRequest;
    private boolean holdMain;

    public ElevatorThread(ElevatorContext elevator, RequestTable requestTable) {
        this.elevator = elevator;
        this.requestTable = requestTable;
        holdMain = false;
        mainRequest = null;
    }

    @Override
    public void run() {
        while (true) {
            // 所有请求已完成且停止输入，终止线程
            if (requestTable.isEmpty() && requestTable.inputEnd() && elevator.isEmpty()) {
                return;
            }
            // 若主请求为空，查询主请求
            if (mainRequest == null) {
                mainRequest = getMainRequest();
            }
            // 若还是找不到主请求，等待请求输入
            if (mainRequest == null) {
                requestTable.waitForRequest();
                continue;
            }
            // 处理当前主请求，退出条件：主请求到达或者捎带的请求变成主请求
            while (true) {
                if (!holdMain && elevator.getCurFloor().equals(mainRequest.getFromFloor())) {
                    pickMainRequest();
                }
                if (hasArrival()) {
                    personArrive();
                }
                if (mainRequest == null) {
                    if (elevator.isOpen()) {
                        close(elevator.getCurFloor(), elevator.getId());
                    }
                    break;
                }
                if (requestTable.canCarry(elevator.getCurFloor(), mainRequest.getToFloor())) {
                    if (carryExMain()) {
                        if (elevator.isOpen()) {
                            close(elevator.getCurFloor(), elevator.getId());
                        }
                        break;
                    }
                }
                if (elevator.isOpen()) {
                    close(elevator.getCurFloor(), elevator.getId());
                }
                if (mainRequest == null) {
                    break;
                }
                move();
            }

        }
    }

    private void pickMainRequest() {
        if (!elevator.isOpen()) {
            open(elevator.getCurFloor(), elevator.getId());
        }
        holdMain = true;
        in(mainRequest.getPersonId(), mainRequest.getFromFloor(), mainRequest.getElevatorId());
        elevator.inPerson(mainRequest);
        requestTable.deleteRequest(mainRequest);
    }

    private PersonRequest getMainRequest() {
        if (elevator.isEmpty()) {
            holdMain = false;
            return requestTable.getMainRequest(elevator.getCurFloor());
        } else {
            holdMain = true;
            return elevator.getMainRequest();
        }
    }

    private void in(int personId, String floor, int elevatorId) {
        TimableOutput.println(String.format("IN-%d-%s-%d", personId, floor, elevatorId));
    }

    private void out(int personId, String floor, int elevatorId) {
        TimableOutput.println(String.format("OUT-%d-%s-%d", personId, floor, elevatorId));
    }

    private boolean hasArrival() {
        return elevator.hasArrival();
    }

    private void personArrive() {
        if (!elevator.isOpen()) {
            open(elevator.getCurFloor(), elevator.getId());
        }
        ArrayList<PersonRequest> arrivePeople = elevator.arrive();
        for (PersonRequest p : arrivePeople) {
            if (p.equals(mainRequest)) {
                mainRequest = null;
            }
            out(p.getPersonId(), p.getToFloor(), p.getElevatorId());
        }
    }

    private void open(String floor, int elevatorId) {
        TimableOutput.println(String.format("OPEN-%s-%d", floor, elevatorId));
        elevator.openDoor();
    }

    private void close(String floor, int elevatorId) {
        try {
            sleep(elevator.getIntervalOfDoor());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        TimableOutput.println(String.format("CLOSE-%s-%d", floor, elevatorId));
        elevator.closeDoor();
    }

    private boolean carryExMain() {
        List<PersonRequest> in =  requestTable.carryPeople(
            elevator.getCurFloor(), mainRequest.getToFloor(), elevator.getLeftCnt());
        if (!in.isEmpty() && !elevator.isOpen()) {
            open(elevator.getCurFloor(), elevator.getId());
        }
        boolean mainChanged = false;
        for (PersonRequest p : in) {
            if (p.getPriority() > mainRequest.getPriority()) {
                mainChanged = true;
            }
            in(p.getPersonId(), p.getFromFloor(), p.getElevatorId());
            elevator.inPerson(p);
        }
        return mainChanged;
    }

    public void move() {
        String goal = holdMain ? mainRequest.getToFloor() : mainRequest.getFromFloor();
        try {
            sleep(elevator.getSpeed());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        String next = FloorService.nextFloor(elevator.getCurFloor(), goal);
        elevator.setCurFloor(next);
        TimableOutput.println(
            String.format("ARRIVE-%s-%d", elevator.getCurFloor(), elevator.getId()));
    }
}
