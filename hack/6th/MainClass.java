import com.oocourse.elevator1.TimableOutput;

public class MainClass {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();

        Controller controller = new Controller();

        Thread inputThread = new InputThread(controller);
        inputThread.start();
    }
}
