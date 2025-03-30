public class Person implements Comparable<Person> {
    private Integer id;
    private Integer priority;
    private Floor fromFloor;
    private Floor toFloor;

    public Person(Integer id, Integer priority, Floor fromFloor, Floor toFloor) {
        this.id = id;
        this.priority = priority;
        this.fromFloor = fromFloor;
        this.toFloor = toFloor;
    }

    public void setFromFloor(Floor fromFloor) {
        this.fromFloor = fromFloor;
    }

    public Integer getId() {
        return id;
    }

    public Integer getPriority() {
        return priority;
    }

    public Floor getFromFloor() {
        return fromFloor;
    }

    public Floor getToFloor() {
        return toFloor;
    }

    public boolean atDirection(int direction, Floor currentFloor) {
        if (direction == 1) {
            return currentFloor.getLevel() < fromFloor.getLevel();
        } else {
            return currentFloor.getLevel() > fromFloor.getLevel();
        }
    }

    public int getDirection() {
        if (fromFloor.getLevel() < toFloor.getLevel()) {
            return 1;
        } else if (fromFloor.getLevel() > toFloor.getLevel()) {
            return -1;
        } else {
            return 0;
        }
    }

    // 实现 Comparable 接口，按照 priority 从大到小排序
    @Override
    public int compareTo(Person other) {
        // 从大到小排序：priority 大的排前面
        return other.priority.compareTo(this.priority);
        // 如果要从小到大排序，改为：
        // return this.priority.compareTo(other.priority);
    }

    // 为了调试，添加 toString 方法
    @Override
    public String toString() {
        return "Person{id=" + id + ", priority=" + priority
                + ", from=" + fromFloor + ", to=" + toFloor + "}";
    }

}
