public class Floor {
    private int level; // 内部用 int 表示楼层高度

    // 构造函数：从字符串解析楼层
    public Floor(String floorStr) {
        if (!floorStr.matches("^(B|F)\\d+$")) {
            throw new IllegalArgumentException("Invalid floor format: " + floorStr);
        }
        int num = Integer.parseInt(floorStr.substring(1));
        if (floorStr.startsWith("B")) {
            this.level = -num; // 地下楼层为负数
        } else {
            this.level = num - 1;  // 地上楼层为正数
            //TODO:注意可能出bug，在输出的时候
        }
    }

    // 构造方法 2：直接从 int 创建
    public Floor(int level) {
        this.level = level;
    }

    // 获取 int 表示的楼层高度
    public int getLevel() {
        return level;
    }

    // 上一层楼
    public void up() {
        this.level++;
    }

    // 下一层楼
    public void down() {
        this.level--;
    }

    // 计算两个楼层之间的差值（绝对距离）
    public int distanceTo(Floor other) {
        return Math.abs(this.level - other.level);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Floor)) {
            return false;
        }
        Floor other = (Floor) obj;
        return this.level == other.level;
    }

    // 输出为字符串
    @Override
    public String toString() {
        if (level < 0) {
            return "B" + Math.abs(level); // 负数转为 B 格式
        } else {
            return "F" + (level + 1); // 正数转为 F 格式
        }
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(level); // 直接使用 level 的哈希值
    }

}
