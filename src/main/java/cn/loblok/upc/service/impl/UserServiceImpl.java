package cn.loblok.upc.service.impl;

import cn.loblok.upc.entity.User;
import cn.loblok.upc.mapper.UserMapper;
import cn.loblok.upc.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author loblok
 * @since 2025-11-30
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}
