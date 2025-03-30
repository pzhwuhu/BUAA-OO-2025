import com.oocourse.elevator1.TimableOutput;
import javafx.util.Pair;

import java.util.ArrayList;

public class Elevator extends Thread {
    
    private final int id;
    private final int maxCapacity;
    private final ProcessingQueue passengers;
    private final ProcessingQueue requests; //共享对象
    private final Strategy strategy;
    
    private int direction = 0; // 1: up, -1: down, 0: stop
    private Floor currentFloor;
    private int nowNum = 0;
    private long lastTime = 0;
    
    public Elevator(int id, ProcessingQueue requests, Strategy srategy) {
        super("Elevator-" + id);
        this.id = id;
        this.currentFloor = Floor.F1;
        this.maxCapacity = 6;
        this.passengers = new ProcessingQueue();
        this.requests = requests;
        this.strategy = srategy;
    }
    
    public void run() {
        while (true) {
            boolean needMove = false;
            synchronized (requests) {
                if (overWork()) {
                    if (Main.isDebug()) {
                        System.out.println(getName() + " end");
                    }
                    break;
                }
                
                
                Pair<ArrayList<Person>, ArrayList<Person>> exitAndEnter =
                    strategy.getExitAndEnter(requests, passengers, currentFloor,
                    direction, maxCapacity,nowNum);
                ArrayList<Person> toExit = exitAndEnter.getKey();
                ArrayList<Person> toEnter = exitAndEnter.getValue();
                if (!toExit.isEmpty() || !toEnter.isEmpty()) {
                    openAtFloor(toExit, toEnter);
                }
                
                Pair<Integer, Boolean> res = strategy.getDirection(direction, requests, passengers,
                    currentFloor, maxCapacity, nowNum);
                direction = res.getKey();
                needMove = res.getValue();
                if (direction == 0) {
                    // no passengers and requests
                    // stop at current floor
                    if (requests.isEnd()) {
                        continue;
                    }
                    // wait for requests
                    try {
                        if (Main.isDebug()) {
                            System.out.println(getName() + " wait");
                        }
                        requests.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    if (Main.isDebug()) {
                        System.out.println(getName() + " wake up");
                    }
                    res = strategy.getDirection(direction, requests, passengers,
                            currentFloor, maxCapacity, nowNum);
                    direction = res.getKey();
                    needMove = res.getValue();
                }
            }
            if (needMove) {
                move();
            }
        }
    }
    
    public void enterAndExit(ArrayList<Person> toExit, ArrayList<Person> toEnter) {
        // Exit
        for (Person person : toExit) {
            person.exit(currentFloor, id);
            nowNum--;
        }
        // Enter
        for (Person person : toEnter) {
            person.enter(currentFloor, id);
            nowNum++;
        }
    }
    
    public void openAtFloor(ArrayList<Person> toExit, ArrayList<Person> toEnter) {
        // open door
        lastTime = TimableOutput.println("OPEN-" + currentFloor + "-" + id);
        // Passengers entering and exiting the elevator
        enterAndExit(toExit, toEnter);
        // close door
        long time = System.currentTimeMillis() - lastTime;
        if (time < 400) {
            try {
                sleep(400 - time);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        lastTime = TimableOutput.println("CLOSE-" + currentFloor + "-" + id);
    }
    
    public void move() {
        if (direction == 1) {
            currentFloor = currentFloor.getUpper();
        } else if (direction == -1) {
            currentFloor = currentFloor.getLower();
        } else {
            throw new RuntimeException("direction error");
        }
        long time = System.currentTimeMillis() - lastTime;
        if (time < 400) {
            try {
                sleep(400 - time);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        lastTime = TimableOutput.println("ARRIVE-" + currentFloor + "-" + id);
    }
    
    public boolean overWork() {
        return passengers.isEmpty() && requests.isEmpty() && requests.isEnd();
    }
}
