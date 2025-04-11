import com.oocourse.elevator3.TimableOutput;
import com.oocourse.elevator3.UpdateRequest;

public class Coordinate {
    private UpdateRequest updateRequest;
    private boolean readyA = false;
    private boolean readyB = false;
    private boolean endA = false;
    private boolean endB = false;
    private boolean isbusy = false;
    private int idA;
    private int idB;

    public Coordinate(UpdateRequest updateRequest) {
        this.updateRequest = updateRequest;
        idA = updateRequest.getElevatorAId();
        idB = updateRequest.getElevatorBId();
    }

    public synchronized void inShared() {
        if (isbusy) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        isbusy = true;
    }

    public synchronized void outShared() {
        isbusy = false;
        notifyAll();
    }

    public synchronized void setReady(boolean isA) {
        if (isA) { readyA = true; }
        else { readyB = true; }
        if (readyA && readyB) {
            TimableOutput.println("UPDATE-BEGIN-" + idA + "-" + idB);
            notifyAll();
        }
    }

    public boolean isReady() {
        return readyA & readyB;
    }

    public boolean isEnd() {
        return endA & endB;
    }

    public synchronized void endUpdate(boolean isA) {
        if (isA) { endA = true; }
        else { endB = true; }
        if (endA && endB) {
            TimableOutput.println("UPDATE-END-" + idA + "-" + idB);
            notifyAll();
        }
    }

}
