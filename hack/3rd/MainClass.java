import com.oocourse.elevator1.TimableOutput;

public class MainClass {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();  // 初始化时间戳
        InputThread inputThread = new InputThread();  // 创建输入线程
        inputThread.start();  // 启动输入线程

    }
}
