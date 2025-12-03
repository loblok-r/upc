package cn.loblok.upc.entitlement;

public interface DailyQuotaService {


        int getUsedToday(Long userId, String plan); // 如 getUsedToday(123, "HD") → 2

        void incrementUsed(Long userId, String plan);

}
