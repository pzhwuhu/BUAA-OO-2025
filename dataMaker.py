import random
import json

# 定义常量
MAX_INT = (1 << 31) - 1

# 定义电梯池和楼层池
elevator_pool = [1, 2, 3, 4, 5, 6]
floor_pool = ['B4', 'B3', 'B2', 'B1', 'F1', 'F2', 'F3', 'F4', 'F5', 'F6', 'F7']
id_dirt = {}

# 读取配置文件
with open('config.json', 'r') as f:
    config = json.load(f)

command_limit = config['command_limit']
time_limit = config['time_limit']
request_count_range = config['request_count_range']
increase_same_elevator_prob = config['increase_same_elevator_prob']

# 生成唯一ID
def get_id():
    id = random.randint(1, MAX_INT)
    while id_dirt.get(id) == True:
        id = random.randint(1, MAX_INT)
    id_dirt[id] = True
    return id

# 生成时间间隔
def get_time_gap():
    chance = random.randint(0, MAX_INT) % 100
    if chance < 2:
        return 10
    elif chance >= 98:
        return 5
    elif chance >= 5 and chance < 10 or chance >= 90 and chance < 95:
        return random.uniform(1.0, 5.0)
    elif chance >= 10 and chance < 45 or chance >= 55 and chance < 90:
        return 0
    else:
        return random.uniform(0, 1.0)

# 随机选择楼层
def get_floor():
    return random.choice(floor_pool)

# 随机选择电梯
def get_elevator(last_elevator=None):
    if increase_same_elevator_prob and last_elevator is not None:
        # 提高选择上一次电梯的概率
        if random.random() < 0.7:  # 70% 的概率选择上一次的电梯
            return last_elevator
    return random.choice(elevator_pool)

# 生成优先级指数（1-100 的正整数）
def get_priority():
    return random.randint(1, 100)

# 生成电梯请求数据
def generate_input():
    realNum = 0
    stdin_string = ""
    test_string = ""
    maxNum = random.randint(request_count_range[0], request_count_range[1])
    time = 1.0
    last_elevator = None

    for _ in range(maxNum):
        time += get_time_gap()
        if time > time_limit:
            break

        id = str(get_id())
        priority = str(get_priority()) 
        from_floor = str(get_floor())
        to_floor = str(get_floor())
        while to_floor == from_floor:
            to_floor = str(get_floor())
        elevator_id = str(get_elevator(last_elevator))
        last_elevator = elevator_id

        realNum = realNum + 1
        # 生成带时间戳的请求（写入 stdin.txt）
        stdin_string += (
            '[' + str(format(time, '.1f')) + ']' +
            id + '-PRI-' + priority + '-FROM-' +
            from_floor + '-TO-' + to_floor + '-BY-' +
            elevator_id + '\n'
        )
        # 生成不带时间戳的请求（写入 test.txt）
        test_string += (
            id + '-PRI-' + priority + '-FROM-' +
            from_floor + '-TO-' + to_floor + '-BY-' +
            elevator_id + '\n'
        )

    # 将带时间戳的请求写入 stdin.txt
    with open('stdin.txt', 'w', encoding='utf-8') as f:
        f.write(stdin_string)

    # 将不带时间戳的请求写入 test.txt
    with open('test.txt', 'w', encoding='utf-8') as f:
        f.write(test_string)

    return stdin_string, test_string, realNum

# 调用函数生成数据
generate_input()