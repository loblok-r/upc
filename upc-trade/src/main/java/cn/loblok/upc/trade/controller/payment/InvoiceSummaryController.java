package cn.loblok.upc.trade.controller.payment;


import cn.loblok.upc.common.annotation.CurrentUser;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.trade.dto.pay.InvoiceSummaryRequest;
import cn.loblok.upc.trade.dto.pay.SummaryResponse;
import cn.loblok.upc.trade.strategy.InvoiceSummarService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@AllArgsConstructor
@Slf4j
public class InvoiceSummaryController {

    private final InvoiceSummarService invoiceSummarService;

    @PostMapping("/generateInvoiceSummary")
    public Result<SummaryResponse> generateInvoiceSummary(
            @RequestBody @Valid InvoiceSummaryRequest request,
            @CurrentUser Long userId) {

        log.info("用户ID: {}, 创建发票摘要请求参数: {}", userId, request);


        // 1. 获取数据
        String userName = request.getFormData().getLastName() + request.getFormData().getFirstName();
        String planName = request.getOrderDetails().getPlanName();


        String summary = invoiceSummarService.generateInvoiceSummary(userId, request);
        // 模拟返回
//        String mockSummary = String.format("尊贵的 %s，您正在订阅 %s，价格为 %s %s。",
//                userName,
//                planName,
//                request.getOrderDetails().getPrice(),
//                request.getOrderDetails().getCurrency());
        SummaryResponse summaryResponse = new SummaryResponse();

        summaryResponse.setSummary(summary);
        return Result.success(summaryResponse);

    }
}