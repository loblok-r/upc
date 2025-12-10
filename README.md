# UPC 综合用户成长与商城系统

本项目是一个基于Spring Boot和MyBatis-Plus的综合用户成长与商城系统，不仅包含用户签到、积分系统、等级体系、会员系统、优惠券系统，还包括一个完整的商城系统，支持商品展示、秒杀活动、抽奖等功能。

## 功能特性

- 用户注册与登录（JWT Token认证）
- 用户每日签到
- 多租户隔离
- 幂等性签到操作
- 签到记录存储
- 积分奖励机制
- 连续签到计算
- 连续签到奖励机制
- 积分排行榜
- 积分流水查询
- 用户经验值系统
- 用户等级系统
- 权限控制拦截器
- 会员系统（月度、永久、临时会员）
- 优惠券系统（注册礼包、体验券等）
- 商城系统（商品浏览、兑换）
- 秒杀系统
- 抽奖系统
- AI绘图功能

## 技术栈

- Spring Boot 3.1.5
- Java 17
- MyBatis-Plus 3.5.5
- MySQL 8.0+
- Redis
- Redisson（分布式锁）
- JWT（身份认证）
- Lombok（简化代码）
- Maven（项目管理）

## 系统架构与设计

### 核心领域模型

1. **用户系统**
   - 用户基本信息管理
   - 用户等级体系（LEVEL1-LEVEL5）
   - 用户经验值计算
   - 用户积分管理
   - 用户资源管理（算力值、抽奖次数等）

2. **签到系统**
   - 每日签到功能
   - 连续签到统计
   - 签到奖励机制
   - 多租户支持

3. **积分系统**
   - 积分获取与消费
   - 积分排行榜
   - 积分流水记录
   - 积分异步落库

4. **经验系统**
   - 经验值获取与等级计算
   - 经验值流水记录
   - 等级升级事件

5. **会员系统**
   - 多种会员类型（月度、永久、临时）
   - 会员权益管理
   - 微信支付接入
   - 订单管理

6. **优惠券系统**
   - 优惠券模板管理
   - 用户优惠券管理
   - 优惠券发放策略
   - 分布式锁防止超发

7. **商城系统**
   - 商品管理
   - 秒杀活动
   - 抽奖系统
   - 虚拟商品和实物商品分发

8. **AI绘图系统**
   - AI绘图功能
   - 算力值消耗
   - 用户额度管理

### 幂等性保证

通过biz_key确保同一用户同一天只能签到一次，biz_key格式：
```
checkin_{tenantId}_{userId}_{checkinDate}
```

### 连续签到计算策略

为了减少数据库压力，系统采用Redis缓存连续签到天数：

1. 使用Redis Hash结构存储用户连续签到信息：
   - `current_streak`: 当前连续签到天数
   - `last_checkin_date`: 上次签到日期
2. 签到时通过对比上次签到日期来判断是否连续签到
3. 若连续签到则天数+1，否则重置为1
4. 定期（如30天）清理过期的Redis键

### 连续签到奖励机制

- 基础积分：每次签到获得10积分
- 连续签到奖励：连续签到天数为7的倍数时，额外获得50积分

### 排行榜实现策略

为了优化内存使用和提高性能，系统采用以下策略实现排行榜：

1. 使用Redis ZSet数据结构存储排行榜信息
2. 按月分榜，避免单个ZSet过大
3. 用户ID格式为"user:{userId}"，分数为用户积分
4. 签到时实时更新用户积分到排行榜

### 积分流水异步记录

为了提升签到接口的响应速度，积分流水记录采用异步方式写入数据库：

1. 签到成功后立即更新Redis中的积分
2. 通过@Async注解异步执行积分流水记录的数据库写入操作
3. 保证了签到接口的高性能，同时确保积分流水数据的完整性

### 用户等级系统

系统实现了用户等级功能，根据用户积分自动计算用户等级：

- LEVEL1: 0-499积分
- LEVEL2: 500-999积分
- LEVEL3: 1000-1999积分
- LEVEL4: 2000-4999积分
- LEVEL5: 5000+积分

### 权限控制系统

系统实现了基于注解的权限控制拦截器：

1. 使用@RequireLevel注解标记需要特定等级才能访问的接口
2. LevelAuthInterceptor拦截器检查用户等级是否满足要求
3. 用户等级信息缓存在Redis中，有效期24小时

### 错误处理

- 用户不存在：返回错误信息"用户不存在"
- 重复签到：返回错误信息"今日已签到"
- 系统异常：返回错误信息"签到失败"
- 权限不足：返回403错误

## 接口文档

### 1. 用户相关接口

#### 1.1 用户注册接口

##### 接口地址
```
POST /api/user/register
```

##### 请求体
```json
{
  "username": "testuser",
  "password": "password123",
  "email": "test@example.com",
  "confirmPassword": "password123",
  "code": "123456"
}
```

##### 响应示例
```
{
  "code": 200,
  "msg": "ok",
  "data": null
}
```

#### 1.2 用户登录接口

##### 接口地址
```
POST /api/user/login
```

##### 请求体
```json
{
  "email": "test@example.com",
  "password": "password123",
  "code": "123456"
}
```

##### 响应示例
```
{
  "code": 200,
  "msg": "ok",
  "data": {
    "token": "eyJhbGciOiJIUzUxNiJ9.xxxx",
    "userId": 123,
    "expiresIn": 86400000
  }
}
```

#### 1.3 获取用户资料接口

##### 接口地址
```
GET /api/user/profile
```

##### 请求头
| 参数名 | 必须 | 说明 |
| --- | --- | --- |
| Authorization | 是 | Bearer Token |

##### 响应示例
```json
{
  "code": 200,
  "msg": "ok",
  "data": {
    "userId": 1,
    "username": "testuser",
    "email": "test@example.com",
    "exp": 150,
    "points": 250,
    "stats": {
      "works": 0,
      "followers": 0,
      "likes": 0
    },
    "userLevel": "LEVEL2",
    "computingPower": 100,
    "memberExpireAt": null,
    "permanentMember": false,
    "checkedIn": false,
    "streakDays": 3,
    "lotteryCounts": 2,
    "dailyUsage": {
      "aiDrawingCounts": 0,
      "textChatCounts": 0
    },
    "avatar": "/avatars/default1.png"
  }
}
```

#### 1.4 获取用户资源接口

##### 接口地址
```
GET /api/user/resources
```

##### 请求头
| 参数名 | 必须 | 说明 |
| --- | --- | --- |
| Authorization | 是 | Bearer Token |

##### 响应示例
```json
{
  "code": 200,
  "msg": "ok",
  "data": {
    "dailyUsage": {
      "textChat": 0,
      "aiDrawing": 0,
      "lastResetDate": "2025-12-11"
    },
    "computingPower": 100,
    "maxComputingPower": 1000
  }
}
```

#### 1.5 重置密码接口

##### 接口地址
```
POST /api/user/resetPassword
```

##### 请求体
```json
{
  "email": "test@example.com",
  "newPassword": "newpassword123",
  "confirmNewPassword": "newpassword123",
  "code": "123456"
}
```

##### 响应示例
```
{
  "code": 200,
  "msg": "ok",
  "data": "密码重置成功"
}
```

### 2. 签到相关接口

#### 2.1 用户签到接口

##### 接口地址
```
POST /api/checkin/checkin
```

##### 请求头
| 参数名 | 必须 | 说明 |
| --- | --- | --- |
| X-Tenant-ID | 是 | 租户ID |
| Authorization | 是 | Bearer Token |

##### 响应示例
```json
{
  "code": 200,
  "msg": "ok",
  "data": {
    "points": 25,
    "streakDays": 3
  }
}
```

##### 错误响应示例
```
{
  "code": 500,
  "msg": "今日已签到",
  "data": null
}
```

##### 业务流程

1. 用户发起签到请求
2. 服务端从Header中获取租户ID和用户ID
3. 构造唯一的业务键(biz_key)，格式为："checkin_{租户ID}_{用户ID}_{日期}"
4. 检查用户是否已经签到，防止重复签到
5. 如果未签到，则插入签到记录到数据库
6. 更新用户积分（+10分基础积分）
7. 计算并更新连续签到天数
8. 如果连续签到天数为7的倍数，额外奖励50积分
9. 更新排行榜积分
10. 异步记录积分流水到数据库
11. 返回签到结果及最新积分和连续签到天数

#### 2.2 查询今日是否已签到接口

##### 接口地址
```
GET /api/checkin/status
```

##### 请求头
| 参数名 | 必须 | 说明 |
| --- | --- | --- |
| X-Tenant-ID | 是 | 租户ID |
| Authorization | 是 | Bearer Token |

##### 响应示例
```
{
  "code": 200,
  "msg": "ok",
  "data": true
}
```

#### 2.3 获取签到历史接口

##### 接口地址
```
GET /api/checkin/history
```

##### 请求头
| 参数名 | 必须 | 说明 |
| --- | --- | --- |
| Authorization | 是 | Bearer Token |

##### 响应示例
```json
{
  "code": 200,
  "msg": "ok",
  "data": {
    "dates": [
      "2025-12-01",
      "2025-12-02",
      "2025-12-03"
    ]
  }
}
```

### 3. 积分相关接口

#### 3.1 积分排行榜接口

##### 接口地址
```
GET /api/leaderboard
```

##### 请求头
| 参数名 | 必须 | 说明 |
| --- | --- | --- |
| X-Tenant-ID | 是 | 租户ID |
| Authorization | 是 | Bearer Token |

##### 请求参数
| 参数名 | 必须 | 说明 |
| --- | --- | --- |
| userId | 是 | 用户ID |

##### 响应示例
```json
{
  "code": 200,
  "msg": "ok",
  "data": {
    "topList": [
      { 
        "userId": "user:101", 
        "score": 250.0, 
        "rank": 1 
      },
      { 
        "userId": "user:102", 
        "score": 240.0, 
        "rank": 2 
      }
    ],
    "myRank": 15,
    "myScore": 180.0
  }
}
```

##### 业务流程

1. 用户请求排行榜信息
2. 服务端从Header中获取租户ID
3. 根据租户ID和当前月份确定排行榜键
4. 查询排行榜Top 10用户信息
5. 查询当前用户的排名和积分
6. 返回排行榜信息

#### 3.2 用户积分流水查询接口

##### 接口地址
```
GET /api/point-transactions
```

##### 请求头
| 参数名 | 必须 | 说明 |
| --- | --- | --- |
| X-Tenant-ID | 是 | 租户ID |
| Authorization | 是 | Bearer Token |

##### 请求参数
| 参数名 | 必须 | 说明 |
| --- | --- | --- |
| userId | 是 | 用户ID |
| page | 否 | 页码，默认为1 |
| size | 否 | 每页条数，默认为20 |
| bizType | 否 | 业务类型过滤，支持checkin_daily、checkin_streak_bonus |

##### 响应示例
```json
{
  "code": 200,
  "msg": "ok",
  "data": {
    "list": [
      {
        "id": 1001,
        "bizType": "checkin_daily",
        "bizTypeDesc": "每日签到",
        "deltaPoints": 10,
        "balanceAfter": 150,
        "createdAt": "2025-12-01T08:30:00"
      }
    ],
    "total": 45,
    "pageNum": 1,
    "pageSize": 20
  }
}
```

##### 业务流程

1. 用户请求积分流水记录，携带用户ID和其他可选参数
2. 系统根据用户ID查询其积分流水记录
3. 支持分页查询和业务类型过滤
4. 返回格式化的积分流水记录列表

### 4. 经验值相关接口

#### 4.1 用户经验值流水查询接口

##### 接口地址
```
GET /api/exp-transactions
```

##### 请求头
| 参数名 | 必须 | 说明 |
| --- | --- | --- |
| X-Tenant-ID | 是 | 租户ID |
| Authorization | 是 | Bearer Token |

##### 请求参数
| 参数名 | 必须 | 说明 |
| --- | --- | --- |
| userId | 是 | 用户ID |
| page | 否 | 页码，默认为1 |
| size | 否 | 每页条数，默认为20 |
| bizType | 否 | 业务类型过滤 |

##### 响应示例
```json
{
  "code": 200,
  "msg": "ok",
  "data": {
    "list": [
      {
        "id": 1001,
        "bizType": "checkin_daily",
        "bizTypeDesc": "每日签到",
        "deltaExps": 5,
        "balanceAfter": 150,
        "createdAt": "2025-12-01T08:30:00"
      }
    ],
    "total": 45,
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 5. 会员相关接口

#### 5.1 创建会员订单接口

##### 接口地址
```
POST /api/membership-orders
```

##### 请求头
| 参数名 | 必须 | 说明 |
| --- | --- | --- |
| Authorization | 是 | Bearer Token |

##### 请求参数
| 参数名 | 必须 | 说明 |
| --- | --- | --- |
| userId | 是 | 用户ID |
| membershipType | 是 | 会员类型（MONTHLY、PERMANENT、MONEDAY）|

##### 响应示例
```json
{
  "code": 200,
  "msg": "ok",
  "data": {
    "orderNo": "MEM20251203120001",
    "amount": 19.90,
    "type": "MONTHLY"
  }
}
```

#### 5.2 获取支付参数接口

##### 接口地址
```
GET /api/membership-orders/{orderNo}/pay-params
```

##### 请求头
| 参数名 | 必须 | 说明 |
| --- | --- | --- |
| Authorization | 是 | Bearer Token |

##### 响应示例
```json
{
  "code": 200,
  "msg": "ok",
  "data": {
    "appId": "wx1234567890",
    "timeStamp": "1701234567",
    "nonceStr": "randomstring",
    "package": "prepay_id=wx20141027164856abcdef",
    "signType": "RSA",
    "paySign": "signaturestring"
  }
}
```

### 6. 优惠券相关接口

#### 6.1 创建优惠券模板接口

##### 接口地址
```
POST /api/coupon-templates
```

##### 请求头
| 参数名 | 必须 | 说明 |
| --- | --- | --- |
| Authorization | 是 | Bearer Token |

##### 请求体
```json
{
  "templateCode": "WELCOME_2025",
  "name": "新用户注册礼包",
  "type": 1,
  "discountValue": 10,
  "totalStock": 1000,
  "perUserLimit": 1,
  "validDays": 7,
  "status": 1
}
```

##### 响应示例
```
{
  "code": 200,
  "msg": "ok",
  "data": null
}
```

#### 6.2 查询优惠券模板接口

##### 接口地址
```
GET /api/coupon-templates/{templateCode}
```

##### 请求头
| 参数名 | 必须 | 说明 |
| --- | --- | --- |
| Authorization | 是 | Bearer Token |

##### 响应示例
```json
{
  "code": 200,
  "msg": "ok",
  "data": {
    "id": 1,
    "templateCode": "WELCOME_2025",
    "name": "新用户注册礼包",
    "type": 1,
    "discountValue": 10,
    "totalStock": 1000,
    "perUserLimit": 1,
    "validDays": 7,
    "status": 1,
    "createdAt": "2025-12-01T08:30:00"
  }
}
```

#### 6.3 抢AI绘图免费体验券接口

##### 接口地址
```
POST /api/user-coupon/grab-free-ai-voucher
```

##### 请求头
| 参数名 | 必须 | 说明 |
| --- | --- | --- |
| Authorization | 是 | Bearer Token |

##### 响应示例
```json
{
  "code": 200,
  "msg": "ok",
  "data": null
}
```

### 7. 商城相关接口

#### 7.1 获取商品列表接口

##### 接口地址
```
GET /api/mall/products
```

##### 请求参数
| 参数名 | 必须 | 说明 |
| --- | --- | --- |
| page | 否 | 页码，默认为1 |
| size | 否 | 每页条数，默认为20 |

##### 响应示例
```json
{
  "code": 200,
  "msg": "ok",
  "data": {
    "list": [
      {
        "id": "prod_001",
        "name": "算力值充值包",
        "description": "100点算力值",
        "price": 100,
        "iconName": "power_icon",
        "displayColor": "#FF0000",
        "tag": "热销"
      }
    ],
    "total": 10,
    "pageNum": 1,
    "pageSize": 20
  }
}
```

#### 7.2 获取抽奖奖品列表接口

##### 接口地址
```
GET /api/mall/lotteryPrizes
```

##### 响应示例
```json
{
  "code": 200,
  "msg": "ok",
  "data": [
    {
      "id": "prize_001",
      "title": "算力值大礼包",
      "subtitle": "1000算力值",
      "icon": "gift_icon",
      "color": "#FF0000",
      "badge": "稀有"
    }
  ]
}
```

#### 7.3 获取用户积分信息接口

##### 接口地址
```
GET /api/mall/user/points
```

##### 请求头
| 参数名 | 必须 | 说明 |
| --- | --- | --- |
| Authorization | 是 | Bearer Token |

##### 响应示例
```json
{
  "code": 200,
  "msg": "ok",
  "data": {
    "points": 250,
    "totalEarned": 500,
    "totalSpent": 250
  }
}
```

### 8. 秒杀相关接口

#### 8.1 获取秒杀活动列表接口

##### 接口地址
```
GET /api/mall/flash-sales
```

##### 请求参数
| 参数名 | 必须 | 说明 |
| --- | --- | --- |
| page | 否 | 页码，默认为1 |
| size | 否 | 每页条数，默认为10 |
| date | 否 | 查询指定日期的活动 |

##### 响应示例
```json
{
  "code": 200,
  "msg": "ok",
  "data": {
    "list": [
      {
        "id": "flash_001",
        "productId": "prod_001",
        "productName": "算力值充值包",
        "originalPrice": 100,
        "salePrice": 50,
        "totalStock": 100,
        "remainingStock": 50,
        "startTime": "2025-12-11T10:00:00",
        "endTime": "2025-12-11T12:00:00",
        "status": "active"
      }
    ],
    "total": 5,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

#### 8.2 秒杀抢购接口

##### 接口地址
```
POST /api/mall/flash/grab
```

##### 请求头
| 参数名 | 必须 | 说明 |
| --- | --- | --- |
| Authorization | 是 | Bearer Token |

##### 请求体
```json
{
  "flashSaleId": "flash_001"
}
```

##### 响应示例
```
{
  "code": 200,
  "msg": "ok",
  "data": "抢购成功"
}
```

### 9. 抽奖相关接口

#### 9.1 抽奖接口

##### 接口地址
```
POST /api/lottery/draw
```

##### 请求头
| 参数名 | 必须 | 说明 |
| --- | --- | --- |
| Authorization | 是 | Bearer Token |

##### 响应示例
```
{
  "code": 200,
  "msg": "ok",
  "data": {
    "prizeId": "prize_001",
    "user": {
      "id": 123
    }
  }
}
```

### 10. AI绘图接口

#### 10.1 生成图片接口

##### 接口地址
```
POST /api/ai/generate
```

##### 请求头
| 参数名 | 必须 | 说明 |
| --- | --- | --- |
| Authorization | 是 | Bearer Token |

##### 请求体
```json
{
  "plan": "BASIC" // 可选值: BASIC, HD, PRO
}
```

##### 响应示例
```
{
  "code": 200,
  "msg": "ok",
  "data": {
    "taskId": "mock-1701234567890",
    "imageUrl": "https://mock-cdn.com/avatar_basic.jpg",
    "thumbnailUrl": "https://mock-cdn.com/avatar_basic.jpg?x-oss-process=image/resize,w_100",
    "commercialAllowed": false,
    "message": "Mock: BASIC avatar generated"
  }
}