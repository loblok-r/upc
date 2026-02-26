package cn.loblok.upc.ai.service.assist;

import cn.loblok.upc.common.enums.AppMode;
import org.springframework.stereotype.Component;

@Component
public class AiCostCalculator {
    public int calculate(AppMode mode, String prompt, String refImage) {
        return switch (mode) {
            case TEXT_CHAT -> Math.max(1, (int) Math.ceil(prompt.length() / 100.0));
            case AI_DRAWING -> 10 + (refImage != null ? 5 : 0);
            case SMART_PRESENTATION -> 15;
            case PODCAST -> 20;
            default -> 5;
        };
    }
}