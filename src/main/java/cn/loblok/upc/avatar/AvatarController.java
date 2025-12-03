package cn.loblok.upc.avatar;

import cn.loblok.upc.common.annotation.CurrentUser;
import cn.loblok.upc.common.dto.AvatarResult;
import cn.loblok.upc.common.dto.Result;
import cn.loblok.upc.common.dto.GenerateRequest;
import cn.loblok.upc.common.exception.BizException;
import cn.loblok.upc.entitlement.UserEntitlementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

/**
 * 图片生成控制器
 */
@RestController
@RequestMapping("/api/avatar")
public class AvatarController {

    @Autowired
    private AvatarGenerationService avatarService;

    @Autowired
    private UserEntitlementService entitlementService; // 权益检查

    @PostMapping("/generate")
    public Result<AvatarResult> generate(@CurrentUser Long userId,
                                         @RequestBody GenerateRequest request) {
        // 1. 校验 plan 合法性
        if (!Arrays.asList("BASIC", "HD", "PRO").contains(request.getPlan())) {
            throw new BizException("无效的生成套餐");
        }

        // 2. 检查用户是否有权限使用该 plan
        if (!entitlementService.canUsePlan(userId, request.getPlan())) {
            throw new BizException("权限不足，请升级或开通会员");
        }

        // 4. 调用生成服务（当前是 Mock）
        AvatarResult result = avatarService.generate(userId, request);

        // 3. 扣减权益（日常额度 or 不限次体验期）
        entitlementService.consumeQuota(userId, request.getPlan());

        return Result.success(result);
    }
}