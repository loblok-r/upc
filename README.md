# UPC 签到系统

本项目是一个基于Spring Boot和MyBatis-Plus的签到系统，支持多租户签到功能、用户管理系统、积分排行榜和权限控制。

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
- 用户等级系统
- 权限控制拦截器

## 接口文档

### 1. 用户注册接口

#### 接口地址
```
POST /user/register
```

#### 请求体
```json
{
  "username": "testuser",
  "password": "password123",
  "tenantId": "tenant1"
}
```

#### 响应示例
```json
{
  "code": 200,
  "msg": "ok",
  "data": {
    "token": "eyJhbGciOiJIUzUxNiJ9.xxxx",
    "userId": 123,
    "username": "testuser"
  }
}
```

### 2. 用户登录接口

#### 接口地址
```
POST /user/login
```

#### 请求体
```json
{
  "username": "testuser",
  "password": "password123"
}
```

#### 响应示例
```json
{
  "code": 200,
  "msg": "ok",
  "data": {
    "token": "eyJhbGciOiJIUzUxNiJ9.xxxx",
    "userId": 123,
    "username": "testuser"
  }
}
```

### 3. 用户签到接口

#### 接口地址
```
POST /api/checkin/checkin
```

#### 请求头
| 参数名 | 必须 | 说明 |
| --- | --- | --- |
| X-Tenant-ID | 是 | 租户ID |
| Authorization | 是 | Bearer Token |

#### 请求体
```json
{
  "userId": 123
}
```

#### 响应示例
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

#### 错误响应示例
```json
{
  "code": 500,
  "msg": "今日已签到",
  "data": {
    "points": 25,
    "streakDays": 3
  }
}
```

#### 业务流程

1. 用户发起签到请求，携带用户ID
2. 服务端从Header中获取租户ID
3. 构造唯一的业务键(biz_key)，格式为："checkin_{租户ID}_{用户ID}_{日期}"
4. 检查用户是否已经签到，防止重复签到
5. 如果未签到，则插入签到记录到数据库
6. 更新用户积分（+10分基础积分）
7. 计算并更新连续签到天数
8. 如果连续签到天数为7的倍数，额外奖励50积分
9. 更新排行榜积分
10. 异步记录积分流水到数据库
11. 返回签到结果及最新积分和连续签到天数

### 4. 查询今日是否已签到接口

#### 接口地址
```
GET /api/checkin/status?userId={userId}
```

#### 请求头
| 参数名 | 必须 | 说明 |
| --- | --- | --- |
| X-Tenant-ID | 是 | 租户ID |
| Authorization | 是 | Bearer Token |

#### 请求参数
| 参数名 | 必须 | 说明 |
| --- | --- | --- |
| userId | 是 | 用户ID |

#### 响应示例
```json
{
  "code": 200,
  "msg": "ok",
  "data": true
}
```

### 5. 积分排行榜接口

#### 接口地址
```
GET /api/leaderboard?userId={userId}
```

#### 请求头
| 参数名 | 必须 | 说明 |
| --- | --- | --- |
| X-Tenant-ID | 是 | 租户ID |
| Authorization | 是 | Bearer Token |

#### 请求参数
| 参数名 | 必须 | 说明 |
| --- | --- | --- |
| userId | 是 | 用户ID |

#### 响应示例
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

#### 业务流程

1. 用户请求排行榜信息，携带用户ID
2. 服务端从Header中获取租户ID
3. 根据租户ID和当前月份确定排行榜键
4. 查询排行榜Top 10用户信息
5. 查询当前用户的排名和积分
6. 返回排行榜信息

### 6. 用户积分流水查询接口

#### 接口地址
```
GET /api/point-transactions?userId={userId}&page={page}&size={size}&bizType={bizType}
```

#### 请求头
| 参数名 | 必须 | 说明 |
| --- | --- | --- |
| X-Tenant-ID | 是 | 租户ID |
| Authorization | 是 | Bearer Token |

#### 请求参数
| 参数名 | 必须 | 说明 |
| --- | --- | --- |
| userId | 是 | 用户ID |
| page | 否 | 页码，默认为1 |
| size | 否 | 每页条数，默认为20 |
| bizType | 否 | 业务类型过滤，支持checkin_daily、checkin_streak_bonus |

#### 响应示例
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

#### 业务流程

1. 用户请求积分流水记录，携带用户ID和其他可选参数
2. 系统根据用户ID查询其积分流水记录
3. 支持分页查询和业务类型过滤
4. 返回格式化的积分流水记录列表

## 系统架构与设计

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