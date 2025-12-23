package cn.loblok.upc.worker.service;

import cn.hutool.core.util.IdUtil;
import cn.loblok.upc.common.enums.AppMode;
import cn.loblok.upc.common.enums.MessageContentType;
import cn.loblok.upc.worker.entity.AiGenerationLogs;
import cn.loblok.upc.worker.mapper.AiGenerationLogsMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
@AllArgsConstructor
public class AiLogService {

    private final AiGenerationLogsMapper aiGenerationLogsMapper;
    public void saveLog(Long userId, AppMode mode, String prompt,
                        int cost,String referenceImg,String content,
                        String cosPath, String sessionId) {


        //记录日志
        AiGenerationLogs aiGenerationLogs = new AiGenerationLogs();
        aiGenerationLogs.setId(IdUtil.randomUUID());
        aiGenerationLogs.setUserId(userId);
        aiGenerationLogs.setSessionId(sessionId);
        aiGenerationLogs.setPrompt(prompt);
        aiGenerationLogs.setContentType(mode == AppMode.TEXT_CHAT ? MessageContentType.TEXT : MessageContentType.IMAGE);
        aiGenerationLogs.setCost(cost);
        aiGenerationLogs.setParams(referenceImg);
        aiGenerationLogs.setStatus("SUCCESS");
        if(mode == AppMode.TEXT_CHAT){
            aiGenerationLogs.setResultUrl(content);
        }else {
            aiGenerationLogs.setResultUrl(cosPath);
        }
        aiGenerationLogs.setCreatedAt(LocalDateTime.now());

        try{
            aiGenerationLogsMapper.insert(aiGenerationLogs);
        }catch (Exception e){
            log.error("保存日志失败: {}", e.getMessage());
        }


    }
}