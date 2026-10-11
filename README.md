\# 基于RAG大模型的工业设备智能运维与监测报警系统



面向中小型制造工厂的工业物联网运维平台，实现设备数据实时采集、阈值报警、AI 智能处置建议等功能。



\## 技术栈



\- \*\*后端\*\*：SpringBoot 3.5、Spring AI、MyBatis-Plus、MySQL、Redis、PgVector、MQTT(EMQX)、通义千问、WebSocket

\- \*\*前端\*\*：Vue3、Vite、Element Plus、ECharts、SockJS/STOMP

\- \*\*工具\*\*：Docker Compose、Python（设备模拟器）



\## 目录结构

iiot-project/

├── iiot-monitor/ # 后端 SpringBoot 项目

├── iiot-frontend/ # 前端 Vue 项目

├── simulator/ # Python 设备模拟器

├── docker-compose.yml # 中间件编排

└── README.md



\## 快速开始

\### 1. 启动中间件



```bash

cd iiot-project    #cd D:\\study\_files\\iiot-project

docker-compose up -d



启动 MySQL、Redis、PgVector、EMQX 四个容器。



2\. 配置 API Key

前往 阿里云百炼控制台 申请通义千问 API Key。



在 IDEA 运行配置里设置环境变量：



text

DASHSCOPE\_API\_KEY=sk-你的真实Key



3\. 启动后端

运行 IiotMonitorApplication。



4\. 启动设备模拟器

bash

cd simulator     # cd D:\\study\_files\\iiot-project\\simulator

pip install paho-mqtt

python simulator.py

5\. 启动前端

bash

cd iiot-frontend     # cd D:\\study\_files\\iiot-project\\iiot-frontend

npm install

npm run dev

浏览器打开 http://localhost:5173



核心功能



1\.MQTT 实时数据接入

2\.阈值报警 + Redis 滑动窗口去重

3\.RAG 知识库智能问答

4\.报警联动 AI 生成处置建议

5\.WebSocket 实时推送

6\.四大可视化页面



安全说明

API Key 通过环境变量 DASHSCOPE\_API\_KEY 读取，不硬编码；敏感文件已在 .gitignore 中排除。

