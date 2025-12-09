package cn.loblok.upc.service.impl;

import cn.loblok.upc.entity.Products;
import cn.loblok.upc.mapper.ProductsMapper;
import cn.loblok.upc.service.ProductsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author loblok
 * @since 2025-12-09
 */
@Service
public class ProductsServiceImpl extends ServiceImpl<ProductsMapper, Products> implements ProductsService {

}
