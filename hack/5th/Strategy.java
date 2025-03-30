import javafx.util.Pair;

import java.util.ArrayList;

public interface Strategy {
    public Pair<ArrayList<Person>, ArrayList<Person>> getExitAndEnter(ProcessingQueue requests,
        ProcessingQueue passengers, Floor currentFloor, int direction,int maxCapacity, int nowNum);
    
    public Pair<Integer, Boolean> getDirection(int direction, ProcessingQueue requests,
        ProcessingQueue passengers, Floor currentFloor, int maxCapacity, int nowNum);
}
