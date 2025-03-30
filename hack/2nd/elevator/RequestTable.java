package elevator;

import com.oocourse.elevator1.PersonRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class RequestTable {
    private HashMap<String, HashSet<PersonRequest>> fromMap;
    private HashMap<Integer, HashSet<PersonRequest>> priMap;

    private boolean inputEnd;

    public RequestTable() {
        fromMap = new HashMap<>();
        priMap = new HashMap<>();
        inputEnd = false;
    }

    public synchronized void setEnd() {
        this.inputEnd = true;
        notifyAll();
    }

    public synchronized boolean inputEnd() {
        notifyAll();
        return inputEnd;
    }

    public synchronized PersonRequest getMainRequest(String from) {
        if (isEmpty()) {
            return null;
        }

        int prior = Collections.max(priMap.keySet());

        int earliest = Integer.MAX_VALUE;
        PersonRequest mainRequest = null;
        for (PersonRequest p : priMap.get(prior)) {
            int distance = FloorService.distance(from, p.getFromFloor()) +
                FloorService.distance(p.getFromFloor(), p.getToFloor());
            if (distance < earliest) {
                earliest = distance;
                mainRequest = p;
            }
        }
        notifyAll();
        return mainRequest;
    }

    public synchronized boolean isEmpty() {
        notifyAll();
        return fromMap.isEmpty() && priMap.isEmpty();
    }

    public synchronized void addRequest(PersonRequest personRequest) {
        if (!fromMap.containsKey(personRequest.getFromFloor())) {
            fromMap.put(personRequest.getFromFloor(), new HashSet<>());
        }
        if (!priMap.containsKey(personRequest.getPersonId())) {
            priMap.put(personRequest.getPersonId(), new HashSet<>());
        }
        fromMap.get(personRequest.getFromFloor()).add(personRequest);
        priMap.get(personRequest.getPersonId()).add(personRequest);
        notifyAll();
    }

    public synchronized void deleteRequest(PersonRequest personRequest) {
        fromMap.get(personRequest.getFromFloor()).remove(personRequest);
        priMap.get(personRequest.getPersonId()).remove(personRequest);
        if (fromMap.get(personRequest.getFromFloor()).isEmpty()) {
            fromMap.remove(personRequest.getFromFloor());
        }
        if (priMap.get(personRequest.getPersonId()).isEmpty()) {
            priMap.remove(personRequest.getPersonId());
        }
        notifyAll();
    }

    public synchronized void waitForRequest() {
        if (isEmpty() && !inputEnd) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        notifyAll();
    }

    public synchronized boolean canCarry(String curFloor, String goalFloor) {
        for (PersonRequest p : fromMap.getOrDefault(curFloor, new HashSet<>())) {
            if (FloorService.getDirection(curFloor, p.getToFloor()) ==
                FloorService.getDirection(curFloor, goalFloor)) {
                notifyAll();
                return true;
            }
        }
        notifyAll();
        return false;
    }

    public synchronized List<PersonRequest> carryPeople(
        String curFloor, String goalFloor, int allowCnt) {
        List<PersonRequest> people = new ArrayList<>(fromMap.get(curFloor));
        people.removeIf(p -> (FloorService.getDirection(curFloor, p.getToFloor()) !=
            FloorService.getDirection(curFloor, goalFloor)));
        people = people.stream()
            .sorted(Comparator.comparing(PersonRequest::getPriority))
            .collect(Collectors.toList());
        List<PersonRequest> res = people.subList(0, Math.min(people.size(), allowCnt));
        for (PersonRequest p : res) {
            deleteRequest(p);
        }
        notifyAll();
        return res;
    }
}
