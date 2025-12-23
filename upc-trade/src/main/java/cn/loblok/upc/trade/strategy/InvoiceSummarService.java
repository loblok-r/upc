package cn.loblok.upc.trade.strategy;

import cn.loblok.upc.trade.dto.pay.InvoiceSummaryRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Primary
@AllArgsConstructor
public class InvoiceSummarService {
    public String generateInvoiceSummary(Long userId, InvoiceSummaryRequest request) {

            return "Invoice summary generated successfully";

    }
}