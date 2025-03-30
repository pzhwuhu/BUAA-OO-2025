package elevator;

import java.util.HashMap;

public class FloorService {
    private static final String[] floors = new String[] {
        "B4", "B3", "B2", "B1",
        "F1", "F2", "F3", "F4", "F5", "F6", "F7",
    };

    private static final HashMap<String, Integer> floorIndex = new HashMap<String, Integer>() {
        {
            for (int i = 0; i < floors.length; i++) {
                put(floors[i], i);
            }
        }
    };

    // 得到当前楼层的上一层
    public static String above(String floor) {
        if (!floorIndex.containsKey(floor) || floorIndex.get(floor) + 1 >= floors.length) {
            return "ERROR_FLOOR";
        }
        return floors[floorIndex.get(floor) + 1];
    }

    // 得到当前楼层的下一层
    public static String below(String floor) {
        if (!floorIndex.containsKey(floor) || floorIndex.get(floor) - 1 < 0) {
            return "ERROR_FLOOR";
        }
        return floors[floorIndex.get(floor) - 1];
    }

    // 得到从出发楼层到目的楼层的方向，-1为向下，1为向上
    public static int getDirection(String from, String to) {
        int res = floorIndex.get(to) - floorIndex.get(from);
        return res <= 0 ? -1 : 1;
    }

    // 判断是否为顶层
    public static boolean isTop(String floor) {
        return floorIndex.get(floor) == floors.length - 1;
    }

    // 判断是否为底层
    public static boolean isBottom(String floor) {
        return floorIndex.get(floor) == 0;
    }

    // 根据当前楼层与方向，得到下一楼层
    public static String nextFloor(String floor, String goal) {
        return getDirection(floor, goal) == 1 ? above(floor) : below(floor);
    }

    // 得到两个楼层之间的距离
    public static int distance(String floor1, String floor2) {
        return Math.abs(floorIndex.get(floor1) - floorIndex.get(floor2));
    }
}
