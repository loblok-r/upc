package cn.loblok.upc.service.impl;

import cn.loblok.upc.dto.*;
import cn.loblok.upc.entity.DailyUsage;
import cn.loblok.upc.enums.CommonStatusEnum;
import cn.loblok.upc.enums.UserItemType;
import cn.loblok.upc.enums.VerificationCodeType;
import cn.loblok.upc.event.UserRegisteredEvent;
import cn.loblok.upc.mapper.UserMapper;
import cn.loblok.upc.service.*;
import cn.loblok.upc.entity.User;
import cn.loblok.upc.util.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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


    private final JwtUtil jwtUtil;


    private final StringRedisTemplate redisTemplate;


    private final ApplicationEventPublisher eventPublisher;


    private final VerificationCodeService verificationCodeService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    private final UserItemsService userItemsService;

    private final DailyUsageService dailyUsageService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result register(String username, String password, String email) {


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

        log.info("用户注册成功：{}", username);

        // 发布用户注册事件
//        UserRegisteredEvent event = new UserRegisteredEvent(this, user.getId(), username);
//        eventPublisher.publishEvent(event);

        return Result.success(null);
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

        // 验证密码
//        if (!passwordEncoder.matches(password, user.getPassword())) {
//            return Result.error(CommonStatusEnum.USER_PASSWORD_ERROR.getCode(), CommonStatusEnum.USER_PASSWORD_ERROR.getMessage());
//        }

        // 生成token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        // 将token存储到Redis中，设置过期时间
        redisTemplate.opsForValue().set("token:" + token, String.valueOf(user.getId()), 24, TimeUnit.HOURS);

        StatsData statsData = new StatsData();
        statsData.setWorks(user.getWorks());
        statsData.setFollowers(user.getFollowers());
        statsData.setLikes(user.getLikes());

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

        StatsData statsData = new StatsData();
        statsData.setWorks(user.getWorks());
        statsData.setFollowers(user.getFollowers());
        statsData.setLikes(user.getLikes());


        userProfileDTO.setUserId(user.getId());
        userProfileDTO.setUsername(user.getUsername());
        userProfileDTO.setEmail(user.getEmail());
        userProfileDTO.setExp(user.getExp());
        userProfileDTO.setPoints(user.getPoints());
        userProfileDTO.setStats(statsData);
        userProfileDTO.setUserLevel(user.getUserLevel());
        userProfileDTO.setComputingPower(user.getComputingPower());
        userProfileDTO.setMemberExpireAt(user.getMemberExpireAt());
        userProfileDTO.setPermanentMember(user.getIsPermanentMember());
        userProfileDTO.setCheckedIn(user.getIschickined());
        userProfileDTO.setStreakDays(user.getStreakdays());
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
    public void updateUserPoints(Long userId, int newvalue) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setPoints(newvalue);
            userMapper.updateById(user);
        } else {
            throw new RuntimeException("用户不存在");
        }
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
    public void addComputePower(Long userId, Integer amount) {
        if (userId == null || amount == null || amount <= 0) {
            log.warn("参数无效：userId={}, amount={}", userId, amount);
            return;
        }

        try {
            User user = getById(userId);
            if (user != null) {
                user.setComputingPower(user.getComputingPower() + amount);
                userMapper.updateById(user);
                log.info("用户{}算力增加{}，当前算力：{}", userId, amount, user.getComputingPower());
            } else {
                log.warn("用户不存在，userId={}", userId);
            }
        } catch (Exception e) {
            log.error("增加用户算力失败，userId={}, amount={}", userId, amount, e);
        }
    }


@Override
public void extendVipDays(Long userId, Integer days) {
    if (userId == null || days == null || days <= 0) {
        log.warn("参数无效：userId={}, days={}", userId, days);
        return;
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
        }
    } catch (Exception e) {
        log.error("延长用户VIP天数失败，userId={}, days={}", userId, days, e);
    }
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


    // 本地缓存（如 Caffeine）或 Redis 缓存
    @Override
    public User getById(Long userId) {
        if (userId == null) return null;
        return userMapper.selectById(userId);
    }
}