public class ElevatorThread extends Thread {
    private Elevator elevator;

    public ElevatorThread(Elevator elevator) {
        this.elevator = elevator;
    }

    public void run() {
        while (true) {
            Advice advice = elevator.getAdvice();
            if (advice == Advice.OVER) {
                break;                              //电梯线程结束
            }
            else if (advice == Advice.MOVE) {
                elevator.move();                             //电梯沿着原方向移动一层
            }
            else if (advice == Advice.REVERSE) {
                elevator.setDirection(-elevator.getDirection());    //电梯转向
            }
            else if (advice == Advice.WAIT) {
                elevator.getRequestTable().waitRequest();         //电梯等待
            }
            else if (advice == Advice.OPEN) {
                elevator.openAndClose();                     //电梯开门
            }
        }
    }
}
