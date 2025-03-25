import com.oocourse.exp.Order;

public class ChefThread extends Thread {
    private final String type;
    private final ProcessingQueue queue;

    public ChefThread(String type, ProcessingQueue queue) {
        this.type = type;
        this.queue = queue;
    }

    @Override
    public void run() {
        while (true) {
            if (queue.isEmpty() && queue.isEnd()) {
                System.out.printf("ChefThread %s ends\n", type);
                break;
            }
            Order order = queue.poll();
            if (order == null) {
                continue;
            }
            switch (order.getDish()) {
                // TODO: 完成order
                default:
                    throw new IllegalArgumentException("Unknown order type: " + order.getType());
            }
        }
    }

    private void makeAppetizer(Order order) {
        try {
            System.out.printf("working-%d-by-%s\n", order.getId(), type);
            sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        
    }

    private void makeMainCourse(Order order) {
        try {
            System.out.printf("working-%d-by-%s\n", order.getId(), type);
            sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void makeDessert(Order order) {
        try {
            System.out.printf("working-%d-by-%s\n", order.getId(), type);
            sleep(1500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
