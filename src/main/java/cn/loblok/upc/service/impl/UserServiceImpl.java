package cn.loblok.upc.service.impl;

import cn.loblok.upc.dto.AuthResponseDTO;
import cn.loblok.upc.dto.Result;
import cn.loblok.upc.enums.CommonStatusEnum;
import cn.loblok.upc.enums.VerificationCodeType;
import cn.loblok.upc.event.UserRegisteredEvent;
import cn.loblok.upc.mapper.UserMapper;
import cn.loblok.upc.service.UserService;
import cn.loblok.upc.entity.User;
import cn.loblok.upc.service.VerificationCodeService;
import cn.loblok.upc.util.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

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
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


    private static final String[] DEFAULT_AVATARS = {
            "/avatars/default1.png",
            "/avatars/default2.png",
            "/avatars/default3.png",
            "/avatars/default4.png",
            "/avatars/default5.png",
    };
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @Autowired
    private VerificationCodeService verificationCodeService;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();





    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result register(String username, String password, String email) {


        // 检查用户名是否已存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.or().eq("email", email);

        if (userMapper.selectCount(queryWrapper) > 0) {
            return Result.error(CommonStatusEnum.USER_EXIST.getCode(),CommonStatusEnum.USER_EXIST.getMessage());
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
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Result.error(CommonStatusEnum.USER_PASSWORD_ERROR.getCode(),CommonStatusEnum.USER_PASSWORD_ERROR.getMessage());
        }

        // 生成token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        // 将token存储到Redis中，设置过期时间
        redisTemplate.opsForValue().set("token:" + token, String.valueOf(user.getId()), 24, TimeUnit.HOURS);

        // 返回认证响应
        AuthResponseDTO authResponse = new AuthResponseDTO();
        authResponse.setToken(token);
        authResponse.setUserId(user.getId());
        authResponse.setUsername(user.getUsername());
        authResponse.setAvatar(user.getAvatarUrl());
        authResponse.setExpiresIn(24 * 60 * 60 * 1000L);

        return Result.success(authResponse);
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

    // 可选：加本地缓存（如 Caffeine）或 Redis 缓存
    @Override
    public User getById(Long userId) {
        if (userId == null) return null;
        return userMapper.selectById(userId); // 返回 User 对象，含 level 字段
    }
}