import java.util.HashMap;

public class MainClass {
    public static void main(String[] args) {
        OrderQueue orderQueue = new OrderQueue();
        String[] chefTypes = {"A", "B", "C"};
        HashMap<String, ProcessingQueue> queueMap = new HashMap<>();
        // TODO: 创建调度器DispatchThread，并在合适位置启动代码中创建的线程对象
        for (String chefType : chefTypes) {
            ProcessingQueue queue = new ProcessingQueue();
            queueMap.put(chefType, queue);
            ChefThread chefThread = new ChefThread(chefType, queue);
        }
        InputThread inputThread = new InputThread(orderQueue);
        inputThread.start();
    }
}
