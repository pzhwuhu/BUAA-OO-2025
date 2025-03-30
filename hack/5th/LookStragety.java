import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

public class LookStragety implements Strategy {
    
    public Pair<ArrayList<Person>, ArrayList<Person>> getExitAndEnter(ProcessingQueue requests,
        ProcessingQueue passengers, Floor currentFloor, int direction,int maxCapacity, int nowNum) {
        
        ArrayList<Person> toExit = passengers.getSameDirByFloor(currentFloor, direction);
        ArrayList<Person> toEnter = requests.getSameDirByFloor(currentFloor, direction);
        
        final ArrayList<Person>  exit = new ArrayList<>(toExit);
        final ArrayList<Person> enter = new ArrayList<>();
        toExit.clear();
        
        ArrayList<Person> sortedPassengers = passengers.getAllPassenger();
        sortedPassengers.sort(Comparator.comparingInt(Person::getPriority));
        
        toEnter.sort(Comparator.comparingInt(Person::getPriority).reversed());
        Iterator<Person> it = toEnter.iterator();
        int capacity = maxCapacity - nowNum;
        int tempExitNo = 0;
        ArrayList<Person> toEnterLater = new ArrayList<>();
        while (it.hasNext()) {
            Person person = it.next();
            if (capacity > 0) {
                enter.add(person);
                it.remove();
                passengers.addPassenger(person);
                capacity--;
            } else if (tempExitNo < sortedPassengers.size()) {
                Person temp = sortedPassengers.get(tempExitNo);
                if (person.getPriority() > temp.getPriority()) {
                    exit.add(temp);
                    enter.add(person);
                    
                    it.remove();
                    passengers.addPassenger(person);
                    
                    passengers.removePassenger(temp);
                    temp.setFromFloor(currentFloor);
                    toEnterLater.add(temp);
                    tempExitNo++;
                }
            }
        }
        toEnter.addAll(toEnterLater);
        
        return new Pair<>(exit, enter);
    }
    
    public Pair<Integer, Boolean> getDirection(int direction, ProcessingQueue requests,
        ProcessingQueue passengers, Floor currentFloor, int maxCapacity, int nowNum) {
        // check if elevator is running
        if (direction == 0) { // There are no passengers at this time
            // but there may be requests
            if (requests.isEmpty()) {
                return new Pair<>(0, false);
            } else {
                Person person = requests.getPriotizedAll().get(0);
                Floor fromFloor = person.getFromFloor();
                if (fromFloor.compareTo(currentFloor) > 0) {
                    return new Pair<>(1, false);
                } else if (fromFloor.compareTo(currentFloor) < 0) {
                    return new Pair<>(-1, false);
                } else {
                    return new Pair<>(person.getDirection(), false);
                }
            }
        }
        
        // check if there is a passenger in the elevator
        int passengerDirection = passengers.getDirection();
        if (passengerDirection != 0) {
            return new Pair<>(passengerDirection, true);
        }
        
        // if no passenger, check requests
        if (requests.isEmpty()) {
            return new Pair<>(0, false);
        }
        
        // requests is not empty
        // if there is a request ahead
        
        // same direction
        ArrayList<Person> sameDir = requests.getAllSameDirByFloor(currentFloor, direction);
        // different direction
        ArrayList<Person> diffDir = requests.getAllDiffDirByFloor(currentFloor, direction);
        if (!sameDir.isEmpty() || !diffDir.isEmpty()) {
            return new Pair<>(direction, true);
        }
        
        // need reverse
        return new Pair<>(-direction, false);
    }
}
