import paho.mqtt.client as mqtt
import json, time, random

# 连接 EMQX
client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2, "simulator-01")
client.connect("localhost", 1883, 60)
client.loop_start() # ！！！极其关键，启动后台网络循环 ！！！

devices = ["motor-01", "pump-02", "fan-03"]

print("模拟器启动，开始发送数据...")
while True:
    for did in devices:
        temp = round(random.uniform(40, 95), 2)
        if did == "motor-01" and random.random() < 0.2:
            temp = round(random.uniform(85, 95), 2)

        payload = {
            "temperature": temp,
            "vibration": round(random.uniform(1, 8), 2),
            "current": round(random.uniform(5, 15), 2),
            "speed": random.randint(1400, 3000)
        }
        client.publish(f"factory/{did}/telemetry", json.dumps(payload), qos=1)
        print(f"已发送: {did} -> 温度:{payload['temperature']}") # 新增：方便你看到数据在发
    time.sleep(2)