import com.oocourse.elevator1.PersonRequest;
import com.oocourse.elevator1.Request;
import com.oocourse.elevator1.TimableOutput;

import java.util.ArrayList;

public class ElevatorThread extends Thread {
    private final int id;
    private final long speed = 400;
    private final int capacity = 6;
    private int floor = 0; //0 is the first floor
    private int direction = 0; //0 is stop,1 is up,-1 is down
    private final ArrayList<Request> inside = new ArrayList<>();
    private final ElevatorQueue elevatorQueue;

    public ElevatorThread(int id,ElevatorQueue elevatorQueue) {
        this.id = id;
        this.elevatorQueue = elevatorQueue;
    }

    public int translate(String floor) {
        if (floor.charAt(0) == 'B') {
            return -1 * (floor.charAt(1) - '0');
        }
        else {
            return floor.charAt(1) - '1';
        }
    }

    public int goTo(Request request) {
        PersonRequest a = (PersonRequest) request;
        if (translate(a.getToFloor()) - translate(a.getFromFloor()) > 0) {
            return 1;
        }
        else if (translate(a.getToFloor()) - translate(a.getFromFloor()) < 0) {
            return -1;
        }
        else {
            return 0;
        }
    }

    public String reTranslate(int floor) {
        switch (floor) {
            case -4: return "B4";
            case -3: return "B3";
            case -2: return "B2";
            case -1: return "B1";
            case 0: return "F1";
            case 1: return "F2";
            case 2: return "F3";
            case 3: return "F4";
            case 4: return "F5";
            case 5: return "F6";
            case 6: return "F7";
            default: return "";
        }
    }

    @Override
    public void run() {
        //找主请求的方向
        while (!elevatorQueue.isEmpty() || !elevatorQueue.isEnd() || !inside.isEmpty()) {
            if (inside.isEmpty()) { //无人取等待队列中优先级最高的
                direction = elevatorQueue.getTopDirect(floor);
            }
            //检查到达/捎带/进入//如果有进出就开关门并处理客人
            if (elevatorQueue.hasIn(direction, floor) || isLeave(inside, floor)) {
                Open_Leave_In_Close();
            } //走一层
            int origin1 = floor;
            floor = floor + direction;
            try {
                sleep(speed);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            int origin2 = origin1;
            if (floor < -4) {
                floor = -4;
            } else if (floor > 6) {
                floor = 6;
            }
            if (origin2 != floor) {
                TimableOutput.println(String.format("ARRIVE-%s-%d", reTranslate(floor), id));
            }
        }
    }

    private void Open_Leave_In_Close() {
        TimableOutput.println(String.format("OPEN-%s-%d",reTranslate(floor),id));
        leave(inside,floor);//下客
        try {
            sleep(400);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        for (int i = 1;i <= capacity - inside.size();i++) {
            if (!elevatorQueue.hasIn(direction,floor)) {
                break;
            } else {
                Request request = elevatorQueue.poll(direction,floor);
                inside.add(request);
                direction = goTo(request);
                PersonRequest a = (PersonRequest)request;
                TimableOutput.println(
                    String.format("IN-%d-%s-%d",a.getPersonId(),reTranslate(floor),id));
            }
        }
        TimableOutput.println(String.format("CLOSE-%s-%d",reTranslate(floor),id));
    }

    public boolean isLeave(ArrayList<Request> inside,int floor) {
        for (Request request:inside) {
            PersonRequest a = (PersonRequest) request;
            if (translate(a.getToFloor()) == floor) {
                return true;
            }
        }
        return false;
    }

    public void leave(ArrayList<Request> inside,int floor) {
        ArrayList<Request> delete = new ArrayList<>();
        for (Request request:inside) {
            PersonRequest a = (PersonRequest) request;
            if (translate(a.getToFloor()) == floor) {
                delete.add(a);
                TimableOutput.println(
                    String.format("OUT-%d-%s-%d",a.getPersonId(),reTranslate(floor),id));
            }
        }
        for (Request request:delete) {
            inside.remove(request);
        }
    }
}
