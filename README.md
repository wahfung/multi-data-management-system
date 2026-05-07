# 多数据源对接管理系统

基于 Spring Boot + Vue 3 的多数据源对接管理平台，支持对接多个不同API结构的数据源厂商，并完整记录每次数据传输操作。

## 🛠 技术栈

- **Frontend**: Vue 3 + Vite + Element Plus + Pinia + ECharts
- **Backend**: Spring Boot 3.2 + MyBatis Plus + Spring Security
- **Database**: MySQL 8.0


## 🔗 服务地址 (Services)

| 服务            | 地址                                      |
| --------------- | ----------------------------------------- |
| Frontend        | http://localhost:3000                     |
| Backend Swagger | http://localhost:8080/api/swagger-ui.html |
| Database        | localhost:3306                            |

## 🧪 测试账号

| 角色   | 用户名 | 密码     |
| ------ | ------ | -------- |
| 管理员 | admin  | admin123 |

## 📋 功能特性

### 核心功能

1. **多数据源厂商管理**
   - 支持不同API风格：REST API、JSON-RPC、SOAP
   - 支持多种认证方式：无认证、API Key、Basic、Bearer Token、OAuth2
   - 适配器模式统一处理不同厂商的API结构差异

2. **数据同步操作**
   - 支持数据拉取（Pull）、推送（Push）、双向同步（Sync）
   - 自定义请求方法、端点、请求头和请求体
   - 实时展示同步结果和响应数据

3. **操作记录管理**
   - 完整记录每次数据传输的详细信息
   - 支持按厂商、操作类型、状态、时间范围筛选
   - 查看请求/响应详情，支持JSON格式化展示

4. **仪表盘统计**
   - 实时展示厂商数量、同步统计
   - 近7天同步趋势图表
   - 最近操作记录快速查看

### 技术特性

- 🔐 BCrypt 密码加密，登录与数据库密码保持一致
- 🌐 UTF-8 编码，支持中文无乱码
- 📝 标准日志输出
- ⚡ 统一错误处理和参数校验
- 🎨 Element Plus 现代化 UI 组件库

## 🏗 项目结构

```
674/
├── backend/                 # Spring Boot 后端
│   ├── src/main/java/com/datasource/
│   │   ├── config/         # 配置类
│   │   ├── controller/     # 控制器
│   │   ├── dto/           # 数据传输对象
│   │   ├── entity/        # 实体类
│   │   ├── enums/         # 枚举类
│   │   ├── mapper/        # MyBatis Mapper
│   │   ├── service/       # 服务层
│   │   │   ├── adapter/   # 适配器模式实现
│   │   │   └── impl/      # 服务实现
│   │   └── util/          # 工具类
│   ├── src/main/resources/
│   │   ├── application.yml # 配置文件
│   │   └── schema.sql     # 数据库初始化脚本

│   └── pom.xml
├── frontend/               # Vue 3 前端
│   ├── src/
│   │   ├── api/           # API 接口
│   │   ├── assets/        # 静态资源
│   │   ├── router/        # 路由配置
│   │   ├── store/         # Pinia 状态管理
│   │   ├── utils/         # 工具函数
│   │   └── views/         # 页面组件
│   ├── nginx.conf
│   └── package.json

└── README.md
```

## 🔧 API 适配器说明

系统采用适配器模式处理不同厂商的 API 差异：

| 适配器类型 | 说明            | 适用场景          |
| ---------- | --------------- | ----------------- |
| default    | 默认适配器      | 标准HTTP接口      |
| rest_api   | REST API 适配器 | RESTful 风格接口  |
| json_rpc   | JSON-RPC 适配器 | JSON-RPC 2.0 协议 |
| soap       | SOAP 适配器     | SOAP WebService   |

## 📊 数据库表结构

- `sys_user` - 系统用户表
- `ds_vendor` - 数据源厂商表
- `ds_sync_record` - 数据同步记录表
- `ds_sync_data` - 同步数据表

## ⚠️ 注意事项

1. 首次启动时数据库会自动初始化，包含示例厂商数据
2. 密码采用 BCrypt 加密，与登录密码加密方式一致
3. 所有接口采用 UTF-8 编码，确保中文正常显示
4. 生产环境请修改数据库密码和 JWT 密钥
