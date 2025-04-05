import com.oocourse.elevator2.PersonRequest;

public class ReArrangeRequest extends PersonRequest {
    private final int newFloor;

    public ReArrangeRequest(int newFloor, PersonRequest pr) {
        super(pr.getFromFloor(), pr.getToFloor(), pr.getPersonId(), pr.getPriority());
        this.newFloor = newFloor;
    }

    @Override
    public String getFromFloor() {
        return Strategy.toStr(newFloor);
    }
}
