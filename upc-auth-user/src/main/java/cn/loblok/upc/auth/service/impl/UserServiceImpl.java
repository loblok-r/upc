package cn.loblok.upc.auth.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.loblok.upc.api.user.dto.UserPublicInfoDTO;
import cn.loblok.upc.api.user.dto.UserStatsDTO;
import cn.loblok.upc.auth.dto.*;
import cn.loblok.upc.auth.event.UserRegisteredEvent;
import cn.loblok.upc.auth.mapper.DailyUsageMapper;
import cn.loblok.upc.auth.entity.UserItems;
import cn.loblok.upc.auth.service.UserItemsService;
import cn.loblok.upc.auth.enums.DailyLimitEnum;
import cn.loblok.upc.auth.service.DailyUsageService;
import cn.loblok.upc.auth.service.UserService;
import cn.loblok.upc.auth.service.VerificationCodeService;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.api.ai.dto.DailyUsageResponse;
import cn.loblok.upc.auth.entity.DailyUsage;
import cn.loblok.upc.common.enums.AppMode;
import cn.loblok.upc.common.enums.CommonStatusEnum;
import cn.loblok.upc.common.enums.UserItemType;
import cn.loblok.upc.common.enums.VerificationCodeType;
import cn.loblok.upc.auth.mapper.UserMapper;
import cn.loblok.upc.auth.entity.User;
import cn.loblok.upc.common.utils.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static cn.hutool.core.lang.Validator.isEmail;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author loblok
 * @since 2025-11-30
 */
@Service
@Primary
@Slf4j
@AllArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


    private static final String[] DEFAULT_AVATARS = {
            "/avatars/default1.png",
            "/avatars/default2.png",
            "/avatars/default3.png",
            "/avatars/default4.png",
            "/avatars/default5.png",
    };

    private final UserMapper userMapper;


    private final StringRedisTemplate redisTemplate;


    private final ApplicationEventPublisher eventPublisher;


    private final VerificationCodeService verificationCodeService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    private final UserItemsService userItemsService;

    private final DailyUsageService dailyUsageService;

    private final DailyUsageMapper dailyUsageMapper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<RegisterResponse> register(String username, String password, String email) {


        // 检查用户名是否已存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.or().eq("email", email);

        if (userMapper.selectCount(queryWrapper) > 0) {
            return Result.error(CommonStatusEnum.USER_EXIST.getCode(), CommonStatusEnum.USER_EXIST.getMessage());
        }

        log.info("用户注册：{}", username);


        String defaultAvatar = DEFAULT_AVATARS[new java.util.Random().nextInt(DEFAULT_AVATARS.length)];
        // 创建新用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password)); // 加密密码
        user.setEmail(email);
        user.setTenantId("default");
        user.setIsPermanentMember(false);
        user.setAvatarUrl(defaultAvatar);
        user.setCreatedAt(LocalDateTime.now());

        userMapper.insert(user);
        RegisterResponse registerResponse = new RegisterResponse();
        registerResponse.setMsg("注册成功");

        log.info("用户注册成功：{}", username);

        //发布用户注册事件
        UserRegisteredEvent event = new UserRegisteredEvent(this, user.getId(), username, defaultAvatar);
        eventPublisher.publishEvent(event);

        return Result.success(registerResponse);
    }


    @Override
    public Result login(String email, String password) {


        // 查找用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.or().eq("email", email);


        User user = userMapper.selectOne(queryWrapper);


        if (user == null) {
            throw new RuntimeException("用户不存在");
        }


        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Result.error(CommonStatusEnum.USER_PASSWORD_ERROR.getCode(), CommonStatusEnum.USER_PASSWORD_ERROR.getMessage());
        }

        // 生成token
        String token = JwtUtil.generateToken(user.getId(), user.getUsername());

        // 将token存储到Redis中，设置过期时间
        redisTemplate.opsForValue().set("token:" + token, String.valueOf(user.getId()), 24, TimeUnit.HOURS);

        UserStatsDTO userStatsDTO = new UserStatsDTO();
        userStatsDTO.setWorks(user.getWorks());
        userStatsDTO.setFollowers(user.getFollowers());
        userStatsDTO.setLikes(user.getLikes());

        // 返回认证响应
        AuthResponseDTO authResponse = new AuthResponseDTO();
        authResponse.setToken(token);
        authResponse.setUserId(user.getId());
        authResponse.setExpiresIn(24 * 60 * 60 * 1000L);

        return Result.success(authResponse);
    }

    @Override
    public Result<UserProfileDTO> getUserInfo(Long userId) {

        User user = userMapper.selectById(userId);

        if (user == null) {
            return Result.error(
                    CommonStatusEnum.USER_NOT_FOUND.getCode(),
                    CommonStatusEnum.USER_NOT_FOUND.getMessage()
            );
        }

        DailyUsage dailyUsage = dailyUsageService.selectByUserId(userId);
        DailyUsageResponse dailyUsageResponse = new DailyUsageResponse();

        if (dailyUsage == null) {
            dailyUsage = new DailyUsage();
            dailyUsage.setUserId(String.valueOf(userId));
            dailyUsage.setDate(LocalDate.now());
            dailyUsageResponse.setTextChatCounts(0);
            dailyUsageResponse.setAiDrawingCounts(0);
        } else {
            dailyUsageResponse.setAiDrawingCounts(dailyUsage.getAiDrawingCount());
            dailyUsageResponse.setTextChatCounts(dailyUsage.getTextChatCount());
        }
        //查询抽奖次数
        int totolLotteryChances = userItemsService.getTotalChances(userId, UserItemType.LOTTERY_TICKET);

        int retroCounts = userItemsService.getTotalChances(userId, UserItemType.RESIGN_CARD);

        UserProfileDTO userProfileDTO = new UserProfileDTO();

        UserStatsDTO userStatsDTO = new UserStatsDTO();
        BeanUtils.copyProperties(user, userStatsDTO);
        userStatsDTO.setFollowing(user.getFollowings());


        userProfileDTO.setUserId(user.getId());
        BeanUtils.copyProperties(user, userProfileDTO);
        userProfileDTO.setStats(userStatsDTO);
        userProfileDTO.setPermanentMember(user.getIsPermanentMember());
        userProfileDTO.setCheckedIn(user.getIschickined());
        userProfileDTO.setLotteryCounts(totolLotteryChances);
        userProfileDTO.setDailyUsage(dailyUsageResponse);
        userProfileDTO.setAvatar(user.getAvatarUrl());

        userProfileDTO.setRetroCounts(retroCounts);

        return Result.success(userProfileDTO);
    }

    @Override
    public Result forgotPassword(String email, String newPassword, String code) {
        // 1. 验证邮箱格式
        if (!isEmail(email)) {
            return Result.error(CommonStatusEnum.PARAM_ERROR.getCode(), CommonStatusEnum.PARAM_ERROR.getMessage());
        }

        // 2. 验证密码一致性（这里应该验证新密码和确认密码是否一致，但在service层我们只接收一个密码参数）
        // 这个验证应该在controller层完成

        // 3. 验证用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            return Result.error(CommonStatusEnum.USER_NOT_FOUND.getCode(), CommonStatusEnum.USER_NOT_FOUND.getMessage());
        }

        // 4. 验证验证码
        boolean isCodeValid = verificationCodeService.checkCode(email, code, VerificationCodeType.forget.getType());
        if (!isCodeValid) {
            return Result.error(CommonStatusEnum.CODE_ERROR.getCode(), CommonStatusEnum.CODE_ERROR.getMessage());
        }

        // 5. 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);

        log.info("用户 {} 密码重置成功", email);
        return Result.success("密码重置成功");
    }


    @Override
    public void updateUserExp(Long userId, int newvalue) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setExp(newvalue);
            userMapper.updateById(user);
        } else {
            throw new RuntimeException("用户不存在");
        }
    }


    @Override
    public Result<UserPublicInfoDTO> getUserPublicInfo(Long userId) {
                User user = userMapper.selectById(userId);
        UserPublicInfoDTO userPublicInfoDTO = convertToDTO(user);
        return Result.success(userPublicInfoDTO);
    }

    @Override
    public void updateUserExpAndLevel(Long userId, int newPointValue, String newLevel) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setPoints(newPointValue);
            user.setUserLevel(newLevel);
            userMapper.updateById(user);
        } else {
            throw new RuntimeException("用户不存在");
        }
    }

    @Override
    public Result<Boolean> addComputingPower(Long userId, Integer amount) {
        // 1. 参数校验
        if (userId == null || amount == null || amount <= 0) {
            log.warn("参数无效：userId={}, amount={}", userId, amount);
            return Result.error(CommonStatusEnum.PARAM_ERROR.getCode(),
                    CommonStatusEnum.PARAM_ERROR.getMessage());
        }

        try {
            // 2. 原子性增加算力（一次 SQL）
            int updated = userMapper.addComputingPower(userId, amount);

            // 3. 判断是否成功（updated == 0 表示用户不存在）
            if (updated == 0) {
                log.warn("用户不存在，无法增加算力：userId={}", userId);
                return Result.error(CommonStatusEnum.USER_NOT_FOUND.getCode(),
                        CommonStatusEnum.USER_NOT_FOUND.getMessage());
            }

            log.info("用户{}算力成功增加{}点", userId, amount);
            return Result.success(true);

        } catch (Exception e) {
            log.error("增加用户算力失败，userId={}, amount={}", userId, amount, e);
            return Result.error(CommonStatusEnum.ADD_COMPUTEPOWER_ERROR.getCode(),
                    CommonStatusEnum.ADD_COMPUTEPOWER_ERROR.getMessage());
        }
    }


@Override
public Result<Void> extendVipDays(Long userId, Integer days) {
    if (userId == null || days == null || days <= 0) {
        log.warn("参数无效：userId={}, days={}", userId, days);
        return Result.error(CommonStatusEnum.PARAM_ERROR.getCode(), CommonStatusEnum.PARAM_ERROR.getMessage());
    }

    try {
        User user = getById(userId);
        if (user != null) {
            LocalDateTime currentExpireTime = user.getMemberExpireAt();
            LocalDateTime newExpireTime;

            if (currentExpireTime == null || currentExpireTime.isBefore(LocalDateTime.now())) {
                // 如果当前会员已过期或从未开通会员，则从现在开始计算
                newExpireTime = LocalDateTime.now().plusDays(days);
            } else {
                // 如果当前会员仍在有效期内，则在原有基础上延长
                newExpireTime = currentExpireTime.plusDays(days);
            }

            user.setMemberExpireAt(newExpireTime);
            userMapper.updateById(user);
            log.info("用户{}VIP天数延长{}天，新的到期时间：{}", userId, days, newExpireTime);
        } else {

            log.warn("用户不存在，userId={}", userId);
            return Result.error(CommonStatusEnum.USER_NOT_FOUND.getCode(),
                    CommonStatusEnum.USER_NOT_FOUND.getMessage());
        }
    } catch (Exception e) {
        log.error("延长用户VIP天数失败，userId={}, days={}", userId, days, e);
        return Result.error(CommonStatusEnum.EXTEND_VIP_ERROR.getCode(),
                CommonStatusEnum.EXTEND_VIP_ERROR.getMessage());
    }
    return Result.success("延长成功");
}


    @Override
    public Boolean isMember(Long userId) {
        log.info("检查用户{}是否会员...", userId);
        User user = getById(userId);
        Boolean isPermanentMember = user.getIsPermanentMember();
        if (isPermanentMember) {
            return true;
        } else {
            LocalDateTime memberExpireAt = user.getMemberExpireAt();
            if (memberExpireAt == null) {
                return false;
            }
            return memberExpireAt.isAfter(LocalDateTime.now());
        }
    }

    @Override
    public Result<UserResourcesDTO> getResources(Long userId) {
        log.info("获取用户{}的资源信息...", userId);
        if (userId == null) {
            return Result.error(CommonStatusEnum.UNAUTHORIZED.getCode(),
                    CommonStatusEnum.UNAUTHORIZED.getMessage());
        }

        // TODO: 从数据库或服务中获取真实数据
        // 这里先 mock 示例数据

        User user = getById(userId);
        if (user == null) {
            return Result.error(CommonStatusEnum.USER_NOT_FOUND.getCode(),
                    CommonStatusEnum.USER_NOT_FOUND.getMessage());
        }
        DailyUsage dailyUsage = dailyUsageService.selectByUserId(userId);

        if (dailyUsage == null) {
            dailyUsage = new DailyUsage();
            dailyUsage.setUserId(String.valueOf(userId));
            dailyUsage.setDate(LocalDate.now());
            dailyUsage.setTextChatCount(0);
            dailyUsage.setAiDrawingCount(0);
            dailyUsageService.insert(dailyUsage);
        }
        DailyUsageDTO dailyUsageDTO = new DailyUsageDTO();
        dailyUsageDTO.setTextChat(dailyUsage.getTextChatCount());
        dailyUsageDTO.setAiDrawing(dailyUsage.getAiDrawingCount());
        dailyUsageDTO.setLastResetDate(dailyUsage.getDate().toString());

        UserResourcesDTO resources = new UserResourcesDTO();
        resources.setDailyUsage(dailyUsageDTO);
        resources.setComputingPower(user.getComputingPower());     // 当前算力
        resources.setMaxComputingPower(1000); // 最大算力
        return Result.success(resources);
    }


    @Override
    public void updateUserWorkCounts(Long userId, Integer delta) {

        String sqlSet = "works = GREATEST(0, works + " + delta + ")";
        //更新用户 user：works
        this.update(
                null,
                new UpdateWrapper<User>()
                        .eq("id", userId)
                        .setSql(sqlSet)
        );
    }

    @Override
    public void updateLikeCounts(Long userId, Integer delta) {
        String sqlSet = "likes = GREATEST(0, likes + " + delta + ")";
        //更新用户 user：likes
        this.update(
                null,
                new UpdateWrapper<User>()
                        .eq("id", userId)
                        .setSql(sqlSet)
        );
    }

    @Override
    public void updateFollowingsCounts(Long userId, Integer delta) {

        String sqlSet = "followings = GREATEST(0, followings + " + delta + ")";
        //更新用户 user：followings
        this.update(
                null,
                new UpdateWrapper<User>()
                        .eq("id", userId)
                        .setSql(sqlSet)
        );
    }

    @Override
    public void updateFollowersCounts(Long userId, Integer delta) {
        String sqlSet = "followers = GREATEST(0, followers + " + delta + ")";
        //更新用户 user：followers
        this.update(
                null,
                new UpdateWrapper<User>()
                        .eq("id", userId)
                        .setSql(sqlSet)
        );
    }

    @Override
    public Result<Void> checkAiAccess(Long userId, AppMode mode, int cost) {

        // 获取用户（实时 DB 查询）
        User user = userMapper.selectById(userId);
        if (user == null) {throw new RuntimeException("User not found");}

        //校验算力
        if (user.getComputingPower() < cost) {
            return Result.error(CommonStatusEnum.INSUFFICIENT_COMPUTING_POWER.getCode(),
                    CommonStatusEnum.INSUFFICIENT_COMPUTING_POWER.getMessage());
        }

            // 校验日限额（仅非会员）
        return  checkAndIncrementDailyUsage(userId, mode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Integer> consumeLotteryTicket(Long userId) {
        List<UserItems> chances =  userItemsService.getByUserId(userId, UserItemType.LOTTERY_TICKET);

        // 检查抽奖次数
        if (chances.isEmpty()) {
            return Result.error("抽奖次数不足");
        }

        // todo 扣减次数，原子操作，生产环境 需要换数据库乐观锁或 Redis 分布式锁

        for (UserItems chance : chances) {
            // 尝试乐观锁扣减（推荐）
            int updated = userItemsService.consumeOneChanceWithOptimisticLock(chance.getId());
            if (updated > 0) {
                // 扣减成功，退出
                break;
            }
            // 如果失败（quantity 被别人改了），继续下一条
        }
        //获取扣减后的总次数
        int totalLotteryChances = userItemsService.getTotalChances(userId, UserItemType.LOTTERY_TICKET);

        return Result.success(totalLotteryChances);
    }

    @Override
    public Result<Map<Long, UserPublicInfoDTO>> getUserPublicInfoBatch(List<Long> userIds) {
        log.info("批量获取用户信息...");
        List<User> userlist = userMapper.selectBatchIds(userIds);
        Map<Long, UserPublicInfoDTO> userPublicInfoMap = userlist.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toMap(UserPublicInfoDTO::getId, userPublicInfoDTO -> userPublicInfoDTO));

        return Result.success(userPublicInfoMap);
    }

    @Override
    public Result<Void> consumeComputerPower(Long userId, Integer amount) {

        try {
            int updated = userMapper.deductComputingPower(userId, amount);

            if (updated == 0) {
                // 扣减失败：要么用户不存在，要么算力不足
                log.warn("用户{}算力不足或不存在，无法扣除算力: {}", userId, amount);
                return Result.error(CommonStatusEnum.INSUFFICIENT_COMPUTING_POWER.getCode(),
                        CommonStatusEnum.INSUFFICIENT_COMPUTING_POWER.getMessage());
            }

            log.info("已成功扣除用户{}的算力: {}", userId, amount);
            return Result.success(null);

        } catch (Exception e) {
            log.error("扣除用户{}的算力时发生异常：{}", userId, e.getMessage(), e);
            return Result.error(CommonStatusEnum.CONSUME_COMPUTINGPOWER_FAIL.getCode(),
                    CommonStatusEnum.CONSUME_COMPUTINGPOWER_FAIL.getMessage());
        }

    }

    public Result<Void> checkAndIncrementDailyUsage(Long userId, AppMode mode) {

        log.info("检查用户{}的日限额...{}", userId);
        if (Boolean.TRUE.equals(this.isMember(userId))) {
            // 会员不限制，但仍需记录次数（用于统计）
            log.info("用户{}是会员，不检查日限额", userId);
            incrementDailyUsage(userId, mode);
            return Result.success(null);
        }
        log.info("用户{}非会员，开始检查日限额...", userId);

        LocalDate today = LocalDate.now();
        DailyUsage usage = dailyUsageMapper.selectByUserIdAndDate(String.valueOf(userId), today);
        if (usage == null) {
            usage = new DailyUsage();
            usage.setUserId(String.valueOf(userId));
            usage.setDate(today);
            usage.setTextChatCount(0);
            usage.setAiDrawingCount(0);
            try {
                dailyUsageMapper.insert(usage);
            } catch (Exception e) {
                log.error("插入日用量失败: {}", e.getMessage());
            }

        }

        int current = getCurrentCount(usage, mode);
        int limit = getDailyLimit(mode);

        log.info("用户{}的日限额情况: {}", userId, usage);
        if (current >= limit) {
            return Result.error(CommonStatusEnum.DAILY_LIMIT_EXCEEDED.getCode(),
                    CommonStatusEnum.DAILY_LIMIT_EXCEEDED.getMessage());
        }

        incrementDailyUsage(userId, mode);

        return Result.success(null);
    }

    private int getCurrentCount(DailyUsage usage, AppMode mode) {
        return switch (mode) {
            case TEXT_CHAT -> usage.getTextChatCount();
            case AI_DRAWING -> usage.getAiDrawingCount();
            default -> 0;
        };
    }

    private int getDailyLimit(AppMode mode) {
        return switch (mode) {
            case TEXT_CHAT -> DailyLimitEnum.TEXT_CHAT.getLimit();
            case AI_DRAWING -> DailyLimitEnum.AI_DRAWING.getLimit();
            default -> DailyLimitEnum.Default.getLimit();
        };
    }

    private void incrementDailyUsage(Long userId, AppMode mode) {
        dailyUsageMapper.incrementUsage(String.valueOf(userId), LocalDate.now(), mode.name());
    }

    @Override
    public Result<List<UserPublicInfoDTO>> getRecommendedUsers(Integer limit, List<Long> excludeIds) {
        // 构造查询：找出不在 excludeIds 中，且按 (粉丝*0.6 + 点赞*0.4) 降序排列的前 N 名
        // 注意：如果 excludeIds 为空，MyBatis Plus 的 notIn 会报错，需要处理
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        if (CollUtil.isNotEmpty(excludeIds)) {
            query.notIn(User::getId, excludeIds);
        }

        // 使用 MyBatis Plus 的 last 手写排序逻辑，或者使用自定义 SQL
        query.last("ORDER BY (followers * 0.6 + likes * 0.4) DESC LIMIT " + limit);

        List<User> list = this.list(query);
        // 转换为 DTO 返回
        return Result.success(list.stream().map(this::convertToDTO).collect(Collectors.toList()));
    }




    private UserPublicInfoDTO convertToDTO(User user) {
        UserPublicInfoDTO userPublicInfoDTO = new UserPublicInfoDTO();
        userPublicInfoDTO.setId(user.getId());
        userPublicInfoDTO.setName(user.getUsername());
        userPublicInfoDTO.setAvatar(user.getAvatarUrl());
        userPublicInfoDTO.setHandle(user.getUsername());
        userPublicInfoDTO.setFollowers(user.getFollowers());
        userPublicInfoDTO.setIsMember(isMember(user.getId()));
        UserStatsDTO userStatsDTO = new UserStatsDTO();

        userStatsDTO.setWorks(user.getWorks());
        userStatsDTO.setFollowers(user.getFollowers());
        userStatsDTO.setFollowing(user.getFollowings());
        userStatsDTO.setLikes(user.getLikes());
        userPublicInfoDTO.setStats(userStatsDTO);

        return userPublicInfoDTO;
    }

    // 本地缓存（如 Caffeine）或 Redis 缓存
    @Override
    public User getById(Long userId) {
        if (userId == null) {return null;}
        return userMapper.selectById(userId);
    }
}