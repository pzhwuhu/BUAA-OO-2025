import com.oocourse.elevator1.PersonRequest;
import com.oocourse.elevator1.Request;

import java.util.ArrayList;

public class Strategy {
    private Requests requests;

    public Strategy(Requests requests) {
        this.requests = requests;
    }

    public static String convertToStr(int floor) {
        if (floor >= 5) {
            return "F" + String.valueOf(floor - 4);
        } else {
            return "B" + String.valueOf(5 - floor);
        }
    }

    public static int convertToInt(String str) {
        if (str.charAt(0) == 'F') {
            return str.charAt(1) - '0' + 4;
        }
        else {
            return 5 + '0' - str.charAt(1);
        }
    }

    public Action getAction(int people, int floor, int direction, ArrayList<Integer> man) {
        if (inElevator(people, floor, direction, man) || outElevator(people, floor, man)) {
            return Action.OPEN;
        }
        if (people > 0) {
            return Action.MOVE;
        }
        //电梯为空
        else {
            if (requests.isEmpty()) {
                if (requests.isDone()) {
                    return Action.END;//结束力
                } else {
                    return Action.WAIT;//等待输入
                }
            }
            //还有请求
            if(keepDirection(floor, direction)) {
                return Action.MOVE;
            } else {
                return Action.REVERSE;
            }
        }
    }

    public boolean inElevator(int people, int floor, int direction, ArrayList<Integer> man) {
        if (people == 6) {
            return false;
        }
        for (Request req : requests.getRequests()) {
            PersonRequest preq = (PersonRequest) req;
            if (floor == convertToInt(preq.getFromFloor()) && !man.contains(preq.getPersonId())) {
                int move = convertToInt(preq.getToFloor()) - floor;
                if (move * direction > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean outElevator(int people, int floor, ArrayList<Integer> man) {
        if (people == 0) {
            return false;
        }
        for (Request req : requests.getRequests()) {
            PersonRequest preq = (PersonRequest) req;
            if (convertToInt(preq.getToFloor()) == floor && man.contains(preq.getPersonId())) {
                return true;
            }
        }
        return false;
    }

    public boolean keepDirection(int floor, int direction) {
        for (Request req : requests.getRequests()) {
            PersonRequest preq = (PersonRequest) req;
            int need = convertToInt(preq.getFromFloor()) - floor;
            if (need * direction > 0) {
                return true;
            }
        }
        return false;
    }
}
