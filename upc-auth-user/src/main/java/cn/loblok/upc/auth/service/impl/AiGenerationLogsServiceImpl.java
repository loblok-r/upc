package cn.loblok.upc.auth.service.impl;

import cn.loblok.upc.auth.mapper.AiGenerationLogsMapper;
import cn.loblok.upc.auth.dto.AiHistoryResponse;
import cn.loblok.upc.auth.dto.GenerateMessage;
import cn.loblok.upc.auth.entity.AiGenerationLogs;
import cn.loblok.upc.auth.enums.AllowHistoryDaysEnum;
import cn.loblok.upc.auth.service.AiGenerationLogsService;
import cn.loblok.upc.auth.service.UserService;
import cn.loblok.upc.common.enums.MessageContentType;
import cn.loblok.upc.common.enums.SenderType;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * AI 生成请求日志表 服务实现类
 * </p>
 *
 * @author loblok
 * @since 2025-12-12
 */
@Service
@Slf4j
@AllArgsConstructor
public class AiGenerationLogsServiceImpl extends ServiceImpl<AiGenerationLogsMapper, AiGenerationLogs> implements AiGenerationLogsService {


    private final UserService userService;
    @Override
    public IPage<AiHistoryResponse> getHistoryList(Long userId, Integer page, Integer pageSize) {


        if(userId == null){
            log.error("用户ID不能为空");
            return null;
        }


        // 获取用户允许查看的历史记录天数
        int days = allowedHistoryDays(userId);


        IPage<AiHistoryResponse> ipage = new Page<>(page, pageSize);


        // 根据用户ID查询所有记录并按sessionId分组
        List<AiGenerationLogs> aiGenerationLogsList = this.lambdaQuery()
                .eq(AiGenerationLogs::getUserId, userId)
                .orderByDesc(AiGenerationLogs::getCreatedAt)
                .list();
        Map<String, List<AiGenerationLogs>> aiGenerationLogsMap = aiGenerationLogsList.stream()
                .collect(Collectors.groupingBy(AiGenerationLogs::getSessionId));

        //过滤
        aiGenerationLogsMap.entrySet().removeIf(entry -> {
            return entry.getValue().get(0).getCreatedAt().isBefore(java.time.LocalDateTime.now().minusDays(days));
        });

        log.info("分页处理 -先获取当前页的sessionIds");
        // 分页处理 - 先获取当前页的sessionIds
        List<String> sessionIds = aiGenerationLogsMap.keySet().stream().toList();
        int totalSessions = sessionIds.size();
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalSessions);
        
        // 如果起始索引超出范围，则返回空列表
        if (startIndex >= totalSessions) {
            ipage.setRecords(List.of());
            ipage.setTotal(0);
            return ipage;
        }

        log.info("分页处理 - 获取当前页的sessionIds");
        // 获取当前页的sessionIds
        List<String> pagedSessionIds = sessionIds.subList(startIndex, endIndex);

        // 构建当前页的AiHistoryResponse列表
        List<AiHistoryResponse> aiHistoryResponses = pagedSessionIds.stream()
                .map(sessionId -> {
                    List<AiGenerationLogs> logsInSession = aiGenerationLogsMap.get(sessionId);
                    

                    //一条记录拆分两个message返回
                    List<GenerateMessage> messages = logsInSession.stream()
                            .flatMap(item -> {
                                GenerateMessage message = new GenerateMessage();
                                GenerateMessage message2 = new GenerateMessage();
                                //封装用户消息
                                message.setId(item.getId().toString());
                                message.setType(item.getContentType());
                                message.setSender(SenderType.USER);
                                message.setContent(item.getPrompt());
                                message.setImageUrl(item.getParams());
                                message.setTimestamp(item.getCreatedAt());
                                //封装AI消息
                                message2.setId(item.getId().toString());
                                message2.setType(item.getContentType());
                                message2.setSender(SenderType.AI);
                                if(item.getContentType().equals(MessageContentType.IMAGE)){
                                    message2.setContent(null);
                                    message2.setImageUrl(item.getResultUrl());
                                }else if(item.getContentType().equals(MessageContentType.TEXT)){
                                    message2.setContent(item.getResultUrl());
                                    message2.setImageUrl(null);
                                }
                                message2.setTimestamp(item.getCreatedAt());
                               
                                return Stream.of(message, message2);
                            })
                            .collect(Collectors.toList());

                    AiHistoryResponse aiHistoryResponse = new AiHistoryResponse();
                    aiHistoryResponse.setId(sessionId);
                    aiHistoryResponse.setTitle(logsInSession.get(0).getPrompt());
                    aiHistoryResponse.setType(logsInSession.get(0).getContentType().getValue());
                    aiHistoryResponse.setTimestamp(logsInSession.get(0).getCreatedAt());
                    aiHistoryResponse.setMessages(messages);
                    
                    return aiHistoryResponse;
                })
                .toList();

        ipage.setRecords(aiHistoryResponses);
        ipage.setTotal(totalSessions);
        log.info("分页处理 - 构建当前页的AiHistoryResponse列表:ipage.getRecords():{},ipage.getTotal():{}",ipage.getRecords(),ipage.getTotal());

        return ipage;
    }

    /**
     * 获取历史记录天数
     * @param userId
     * @return
     */
    private int allowedHistoryDays(Long userId) {

        if(userService.isMember(userId)){
            return AllowHistoryDaysEnum.VIP.getDays();
        }
        return AllowHistoryDaysEnum.COMMON.getDays();
    }


}
