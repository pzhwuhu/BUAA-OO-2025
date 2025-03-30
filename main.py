import os
import subprocess
import time

def run_test(num_tests):
    for i in range(num_tests):
        print(f"Running test {i+1}/{num_tests}\n")
        
        # 生成新的 stdin.txt
        print("Generating test data...")
        subprocess.run(["python", "dataMaker.py"], check=True)
        
        # 调用 quickInput_with_calc.py 进行测试
        print("Running test...")
        subprocess.run(["python", "quickInput_with_calc.py"], check=True)
        
        # 读取 output.txt 文件，检查是否所有乘客都到达
        all_arrived = False
        with open("output.txt", "r") as f:
            for line in f:
                if "All passengers arrived!" in line:
                    all_arrived = True
                    break
        
        if not all_arrived:
            result_filename = f"result_{i+1}.txt"
            with open(result_filename, "w") as result_file:
                # 写入 stdin.txt 的内容
                with open("stdin.txt", "r") as stdin_file:
                    result_file.write("========================== stdin.txt ===========================\n\n")
                    result_file.write(stdin_file.read())
                    result_file.write("\n\n\n\n\n")
                
                # 写入 output.txt 的内容
                with open("output.txt", "r") as output_file:
                    result_file.write("=========================== output.txt ==========================\n\n")
                    result_file.write(output_file.read())
            
            print(f"\033[31mNot all passengers arrived! Merged stdin.txt and output.txt into {result_filename}\033[0m")
        
        # 等待一段时间，确保所有资源都被释放
        time.sleep(1)

if __name__ == "__main__":
    num_tests = 200  # 设置测试次数
    run_test(num_tests)
