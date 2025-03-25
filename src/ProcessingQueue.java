import com.oocourse.exp.Order;

import java.util.ArrayList;

public class ProcessingQueue {
    private final ArrayList<Order> takeAwayOrders = new ArrayList<>();
    private final ArrayList<Order> eatInOrders = new ArrayList<>();
    private boolean isEnd = false;

    public synchronized void offer(Order order) {
        if (order.getType().equals("Eat In")) {
            eatInOrders.add(order);
        } else {
            takeAwayOrders.add(order);
        }
        notifyAll();
    }

    public synchronized Order poll() {
        if (isEmpty() && !isEnd()) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        notifyAll();
        // TODO：按照优先级取出需要处理的订单
        else {
            return null;
        }
    }

    public synchronized void setEnd() {
        isEnd = true;
        notifyAll();
    }

    public synchronized boolean isEnd() {
        notifyAll();
        return isEnd;
    }

    public synchronized boolean isEmpty() {
        notifyAll();
        return takeAwayOrders.isEmpty() && eatInOrders.isEmpty();
    }
}
