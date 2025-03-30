import com.oocourse.elevator1.PersonRequest;
import com.oocourse.elevator1.TimableOutput;

import java.util.ArrayList;
import java.util.Hashtable;

public class DispStrat {
    public static Suggestion carryPassengers(ProcessingQueue queue,
                                             String floor, Direction direction,
                                             ArrayList<PersonRequest> pasInLift) {
        Hashtable<String,ArrayList<PersonRequest>> userRequests;
        synchronized (queue) {
            userRequests = new Hashtable<>(queue.getUserRequests());
        }
        if (userRequests.isEmpty()) {
            if (havePasToOut(pasInLift,floor)) {
                return Suggestion.procPas;
            }
            switch (direction) {
                case UP: return Suggestion.goUp;
                case DOWN: return Suggestion.goDown;
                default: return Suggestion.stay;
            }
        }
        else {
            if (userRequests.containsKey(floor) && !fullLoaded(pasInLift,floor)) {
                if (validRequestSize(userRequests.get(floor),direction) > 0) {
                    return Suggestion.procPas;
                }
                else {
                    if (pasInLift.isEmpty()) {
                        return Suggestion.turn;
                    }
                    else if (havePasToOut(pasInLift,floor)) {
                        return Suggestion.procPas;
                    }
                    else {
                        return followCurDir(direction);
                    }
                }

            }
            else {
                if (direction == Direction.STOP) {
                    if (haveUpperFloor(userRequests,floor)) {
                        return Suggestion.goUp;
                    }
                    else {
                        return Suggestion.goDown;
                    }
                }
                else {
                    if (havePasToOut(pasInLift,floor)) {
                        return Suggestion.procPas;
                    }
                    else {
                        return followCurDir(direction);
                    }
                }
            }
        }
    }

    public static int getFloorIndex(String floor) {
        switch (floor) {
            case "F1": return 1;
            case "F2": return 2;
            case "F3": return 3;
            case "F4": return 4;
            case "F5": return 5;
            case "F6": return 6;
            case "F7": return 7;
            case "B1": return 0;
            case "B2": return -1;
            case "B3": return -2;
            case "B4": return -3;
            default: throw new IllegalArgumentException("Invalid floor number!");
        }
    }

    public static String realFloor(int floorIndex) {
        switch (floorIndex) {
            case 1: return "F1";
            case 2: return "F2";
            case 3: return "F3";
            case 4: return "F4";
            case 5: return "F5";
            case 6: return "F6";
            case 7: return "F7";
            case 0: return "B1";
            case -1: return "B2";
            case -2: return "B3";
            case -3: return "B4";
            default: throw new IllegalArgumentException("Invalid floor index!");
        }
    }

    private static boolean haveUpperFloor(Hashtable<String,ArrayList<PersonRequest>>
                                           userRequests, String currentFloor) {
        if (!userRequests.isEmpty()) {
            for (String s: userRequests.keySet()) {
                if (getFloorIndex(s) > getFloorIndex(currentFloor)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean haveLowerFloor(Hashtable<String,ArrayList<PersonRequest>>
                                                  userRequests, String currentFloor) {
        if (!userRequests.isEmpty()) {
            for (String s: userRequests.keySet()) {
                if (getFloorIndex(s) < getFloorIndex(currentFloor)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean fullLoaded(ArrayList<PersonRequest> requests,
                                       String currentFloor) {
        int pasToOut = 0;
        if (!requests.isEmpty()) {
            for (PersonRequest request: requests) {
                if (request.getToFloor().equals(currentFloor)) {
                    pasToOut++;
                }
            }
            return (requests.size() - pasToOut) >= 6;
        }
        else {
            return false;
        }
    }

    private static boolean havePasToOut(ArrayList<PersonRequest> pasInLift,
                                        String currentFloor) {
        if (!pasInLift.isEmpty()) {
            for (PersonRequest request: pasInLift) {
                if (request.getToFloor().equals(currentFloor)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Suggestion followCurDir(Direction direction) {
        if (direction == Direction.UP) {
            return Suggestion.goUp;
        }
        else if (direction == Direction.DOWN) {
            return Suggestion.goDown;
        }
        else {
            throw new IllegalArgumentException("Undefined direction!");
        }
    }

    public static void pasOnAndOff(ArrayList<PersonRequest> pasInLift, String floor,
                                   ProcessingQueue queue, int id, Direction direction) {
        for (int i = 0; i < pasInLift.size(); i++) {
            if (pasInLift.get(i).getToFloor().equals(floor)) {
                int personId = pasInLift.remove(i).getPersonId();
                TimableOutput.println(String.format("OUT-%d-%s-%d",
                        personId,floor,id));
                i--;
            }
        }
        synchronized (queue) {
            Hashtable<String,ArrayList<PersonRequest>> userRequests
                    = queue.getUserRequests();
            ArrayList<PersonRequest> requests = userRequests.get(floor);
            if (requests == null) {
                return;
            }
            if (validRequestSize(requests,direction) + pasInLift.size() < 6) {
                for (int i = 0; i < requests.size(); i++) {
                    PersonRequest request = requests.get(i);
                    if (canTakeRide(request,direction)) {
                        pasInLift.add(request);
                        int personId = requests.remove(i).getPersonId();
                        TimableOutput.println(String.format("IN-%d-%s-%d",
                                personId,floor,id));
                        i--;
                    }
                }
            }
            else {
                for (int i = pasInLift.size(); i <= 6; i++) {
                    int highPriIndex = getHighPriIndex(requests, direction);
                    if (highPriIndex >= 0) {
                        PersonRequest request = requests.remove(highPriIndex);
                        pasInLift.add(request);
                        int personId = request.getPersonId();
                        TimableOutput.println(String.format("IN-%d-%s-%d",
                                personId,floor,id));
                    }
                }
            }
            if (requests.isEmpty()) {
                userRequests.remove(floor);
            }
            else {
                userRequests.replace(floor,requests);
            }
        }
    }

    private static int getHighPriIndex(ArrayList<PersonRequest> requests, Direction dir) {
        int priority = 0;
        int index = -1;
        for (int i = 0; i < requests.size(); i++) {
            if (requests.get(i).getPriority() > priority && canTakeRide(requests.get(i), dir)) {
                priority = requests.get(i).getPriority();
                index = i;
            }
        }
        return index;
    }

    private static boolean canTakeRide(PersonRequest request, Direction direction) {
        if (getFloorIndex(request.getToFloor()) > getFloorIndex(request.getFromFloor())) {
            return direction != Direction.DOWN;
        }
        else if (getFloorIndex(request.getToFloor()) < getFloorIndex(request.getFromFloor())) {
            return direction != Direction.UP;
        }
        else {
            return false;
        }
    }

    private static int validRequestSize(ArrayList<PersonRequest> requests, Direction dir) {
        int size = 0;
        for (PersonRequest request: requests) {
            if (canTakeRide(request,dir)) {
                size++;
            }
        }
        return size;
    }
}
