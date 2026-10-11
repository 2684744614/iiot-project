package com.example.iiotmonitor.service;

import com.example.iiotmonitor.entity.AlarmRecord;
import com.example.iiotmonitor.mapper.AlarmRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/*处理"报警 + AI"联动逻辑的服务：*/
@Service
public class AlarmAiService {

    @Autowired
    private RagService ragService;

    @Autowired
    private AlarmRecordMapper alarmRecordMapper;

    /**
     * 异步为报警记录生成 AI 处置建议
     * 使用 @Async 避免阻塞 MQTT 消息处理线程
     */
    @Async
    public void generateAiSuggestion(Long alarmRecordId, String deviceName,
                                     String metric, double value) {
        try {
            // 构造自然语言问题，交给 RAG 知识库检索 + 大模型生成
            String question = String.format(
                    "%s 的 %s 值达到 %.2f，超过了安全阈值，应如何处理？",
                    deviceName, metric, value
            );

            System.out.println("🤖 正在为报警生成 AI 建议: " + question);

            // 调用 RAG 问答
            String suggestion = ragService.chat(question);

            // 写入数据库
            AlarmRecord update = new AlarmRecord();
            update.setId(alarmRecordId);
            update.setAiSuggestion(suggestion);
            alarmRecordMapper.updateById(update);

            System.out.println("✅ AI 建议已写入报警记录 ID=" + alarmRecordId);
            System.out.println("📝 建议内容: " + suggestion);

        } catch (Exception e) {
            System.err.println("❌ 生成 AI 建议失败: " + e.getMessage());
        }
    }
}
