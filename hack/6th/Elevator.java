import com.oocourse.elevator1.PersonRequest;
import com.oocourse.elevator1.TimableOutput;

import java.util.ArrayList;

public class Elevator extends  Thread {
    private int id; // 电梯ID
    private RequestsPool requestPool; // 请求池
    private int capacity; // 电梯容量
    private String atFloor; // 电梯所在楼层
    private boolean doorOpen; // 电梯门状态
    private ArrayList<PersonRequest> insidePerson; // 电梯中的人
    private int direction; // 电梯运行方向（1为向上，-1为向下, 0为静止）
    private LookDecision lookDecision;

    public Elevator(int id, RequestsPool requestsPool, LookDecision lookDecision) {
        this.id = id;
        this.requestPool = requestsPool;
        this.lookDecision = lookDecision;
        capacity = 6;
        atFloor = "F1";
        doorOpen = false; //初始状态下在一楼，门处于关闭状态
        insidePerson = new ArrayList<>();
        direction = 0; //默认静止
    }

    @Override
    public void run() {
        while (true) {
            if (requestPool.getIsClose() && requestPool.isEmpty() && insidePerson.isEmpty()) {
                break;
            } //当请求池为空且结束输入且电梯内部无请求则停止运行
            lookDecision.operate(this);
        }
        //电梯运行策略
    }

    public RequestsPool getRequestPool() {
        return requestPool;
    }

    public int getElevatorId() {
        return this.id;
    }

    public String getAtFloor() {
        return this.atFloor;
    }

    public boolean getDoorOpen() {
        return this.doorOpen;
    }

    public ArrayList<PersonRequest> getInsidePerson() {
        return this.insidePerson;
    }

    public int getDirection() {
        return this.direction;
    }

    public void setAtFloor(String atFloor) {
        this.atFloor = atFloor;
    }

    public void setDoorOpen(boolean doorOpen) {
        this.doorOpen = doorOpen;
    }

    public void setInsidePerson(ArrayList<PersonRequest> insidePerson) {
        this.insidePerson = insidePerson;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public void move() {
        if (this.getDirection() != 0) {
            int num = shiftFloorToNum(this.getAtFloor());
            if (num == 1) {
                if (this.getDirection() == 1) {
                    num++;
                } else if (this.getDirection() == -1) {
                    num = -1;
                }
            } else if (num == -1) {
                if (this.getDirection() == 1) {
                    num = 1;
                } else if (this.getDirection() == -1) {
                    num--;
                }
            } else if (num == 7) {
                if (this.getDirection() == 1) {
                    throw new RuntimeException("can't get higher");
                } else if (this.getDirection() == -1) {
                    num--;
                }
            } else if (num == -4) {
                if (this.getDirection() == 1) {
                    num++;
                } else if (this.getDirection() == -1) {
                    throw new RuntimeException("can't get lower");
                }
            } else {
                if (this.getDirection() == 1) {
                    num++;
                } else if (this.getDirection() == -1) {
                    num--;
                }
            }
            String nextFloor = shiftNumToFloor(num);
            this.setAtFloor(nextFloor);
            try {
                sleep(400);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            TimableOutput.println(String.format("ARRIVE-%s-%d",
                this.getAtFloor(), this.getElevatorId()));
        }
    }

    public int shiftFloorToNum(String floor) {
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

    public String shiftNumToFloor(int num) {
        String result = null;
        switch (num) {
            case 1 :
                result = "F1";
                break;
            case 2 :
                result = "F2";
                break;
            case 3 :
                result = "F3";
                break;
            case 4 :
                result = "F4";
                break;
            case 5 :
                result = "F5";
                break;
            case 6 :
                result = "F6";
                break;
            case 7 :
                result = "F7";
                break;
            case -1 :
                result = "B1";
                break;
            case -2 :
                result = "B2";
                break;
            case -3 :
                result = "B3";
                break;
            case -4 :
                result = "B4";
                break;
            default:
                result = null;
        }
        return result;
    }

    public void open() {
        this.setDoorOpen(true);
        TimableOutput.println(String.format("OPEN-%s-%d", this.getAtFloor(), this.getElevatorId()));
    }

    public void close() {
        try {
            sleep(400);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        TimableOutput.println(String.format("CLOSE-%s-%d",
            this.getAtFloor(), this.getElevatorId()));
        this.setDoorOpen(false);
    }
}
