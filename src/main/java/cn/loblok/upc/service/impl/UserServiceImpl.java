package cn.loblok.upc.service.impl;

import cn.loblok.upc.dto.AuthResponseDTO;
import cn.loblok.upc.entity.User;
import cn.loblok.upc.event.UserRegisteredEvent;
import cn.loblok.upc.mapper.UserMapper;
import cn.loblok.upc.service.UserService;
import cn.loblok.upc.util.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author loblok
 * @since 2025-11-30
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;


    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    // 可选：加本地缓存（如 Caffeine）或 Redis 缓存
    @Override
    public User getById(Long userId) {
        if (userId == null) return null;
        return userMapper.selectById(userId); // 返回 User 对象，含 level 字段
    }

    @Override
    public AuthResponseDTO register(String username, String password, String tenantId) {
        // 检查用户名是否已存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        if (userMapper.selectCount(queryWrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password)); // 加密密码
        user.setTenantId(tenantId);
        user.setCreatedAt(LocalDateTime.now());
        userMapper.insert(user);

        // 发布用户注册事件
        UserRegisteredEvent event = new UserRegisteredEvent(this, user.getId(), username, tenantId);
        eventPublisher.publishEvent(event);

        // 生成token
        String token = jwtUtil.generateToken(user.getId(), username);

        // 将token存储到Redis中，设置过期时间
        redisTemplate.opsForValue().set("token:" + token, String.valueOf(user.getId()), 24, TimeUnit.HOURS);

        // 返回认证响应
        AuthResponseDTO authResponse = new AuthResponseDTO();
        authResponse.setToken(token);
        authResponse.setUserId(user.getId());
        authResponse.setUsername(username);

        return authResponse;
    }

    @Override
    public AuthResponseDTO login(String username, String password) {
        // 查找用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 生成token
        String token = jwtUtil.generateToken(user.getId(), username);

        // 将token存储到Redis中，设置过期时间
        redisTemplate.opsForValue().set("token:" + token, String.valueOf(user.getId()), 24, TimeUnit.HOURS);

        // 返回认证响应
        AuthResponseDTO authResponse = new AuthResponseDTO();
        authResponse.setToken(token);
        authResponse.setUserId(user.getId());
        authResponse.setUsername(username);

        return authResponse;
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
}