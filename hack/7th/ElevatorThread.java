import com.oocourse.elevator1.PersonRequest;
import com.oocourse.elevator1.TimableOutput;

import java.util.ArrayList;

public class ElevatorThread extends Thread {
    private final int id;
    private final ProcessingQueue queue;
    private ArrayList<PersonRequest> pasInLift = new ArrayList<>();
    private String currentFloor = "F1";
    private Direction direction = Direction.STOP;

    public ElevatorThread(int id, ProcessingQueue queue)
    {
        this.id = id;
        this.queue = queue;
    }

    @Override
    public void run() {
        while (true) {
            if (queue.isEmpty() && pasInLift.isEmpty()) {
                if (queue.isEnd()) {
                    break;
                }
            }
            Suggestion suggestion = DispStrat.carryPassengers(
                    queue,currentFloor,direction,pasInLift);
            switch (suggestion) {
                case goUp: {
                    direction = Direction.UP;
                    move(direction);
                    break;
                }
                case goDown: {
                    direction = Direction.DOWN;
                    move(direction);
                    break;
                }
                case procPas: {
                    openDoor();
                    DispStrat.pasOnAndOff(pasInLift, currentFloor, queue, id, direction);
                    closeDoor();
                    if (pasInLift.isEmpty()) {
                        direction = Direction.STOP;
                    }
                    else if (DispStrat.getFloorIndex(pasInLift.get(0).getToFloor())
                            > DispStrat.getFloorIndex(currentFloor)) {
                        direction = Direction.UP;
                    }
                    else if (DispStrat.getFloorIndex(pasInLift.get(0).getToFloor())
                            < DispStrat.getFloorIndex(currentFloor)) {
                        direction = Direction.DOWN;
                    }
                    break;
                }
                case turn: {
                    turnDirection();
                    openDoor();
                    DispStrat.pasOnAndOff(pasInLift, currentFloor, queue, id, direction);
                    closeDoor();
                    break;
                }
                default: queue.waitForRequest();
            }
        }
    }

    public void openDoor() {
        TimableOutput.println(String.format("OPEN-%s-%d",this.currentFloor,this.id));
    }

    public void closeDoor() {
        try {
            sleep(400);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        TimableOutput.println(String.format("CLOSE-%s-%d",this.currentFloor,this.id));
    }

    public void move(Direction direction) {
        try {
            sleep(400);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        int floorIndex;
        floorIndex = DispStrat.getFloorIndex(currentFloor);
        if (direction == Direction.UP) {
            floorIndex++;
        }
        else if (direction == Direction.DOWN) {
            floorIndex--;
        }
        currentFloor = DispStrat.realFloor(floorIndex);
        TimableOutput.println(String.format("ARRIVE-%s-%d",this.currentFloor,this.id));
    }

    private void turnDirection() {
        if (this.direction == Direction.UP) {
            this.direction = Direction.DOWN;
        }
        else if (this.direction == Direction.DOWN) {
            this.direction = Direction.UP;
        }
    }
}
