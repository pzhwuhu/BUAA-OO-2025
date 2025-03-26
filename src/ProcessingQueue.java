import com.oocourse.exp.Order;

import java.util.ArrayList;

public class ProcessingQueue {
    private final ArrayList<Order> takeAwayOrders = new ArrayList<>();
    private final ArrayList<Order> eatInOrders = new ArrayList<>();
    private boolean isEnd = false;

    public synchronized void offer(Order order, String chef) {
        System.out.printf("scheduled-%d-to-%s\n", order.getId(), chef);
        if (order.getType().equals("Eat In")) {
            eatInOrders.add(order);
        } else {
            takeAwayOrders.add(order);
        }
        notifyAll();
    }

    public synchronized Order poll(String type) {
        if (isEmpty() && !isEnd()) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        notifyAll();
        Order order = null;
        if(!eatInOrders.isEmpty()) {
            order = eatInOrders.remove(0);
        }
        else if(!takeAwayOrders.isEmpty()) {
            order = takeAwayOrders.remove(0);
        }
        if(order != null) {
            System.out.printf("working-%d-by-%s\n", order.getId(), type);
            return order;
        }
        // TODO：按照优先级取出需要处理的订单，在这里输出 working
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
