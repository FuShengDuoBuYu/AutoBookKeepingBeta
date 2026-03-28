# 自动记账(for android)  v1.4

---

## 项目简介

自动记账是一款专为Android平台设计的智能记账应用，通过监听微信和支付宝的支付通知，实现自动识别并记录账单信息。该应用采用前后端分离架构，支持云端数据备份，让用户无需手动输入即可完成日常收支记录。

### 核心功能
- **自动识别账单**：监听微信支付、支付宝支付通知，自动提取交易信息并记录
- **支持支付宝小荷包**：可识别支付宝小荷包账单
- **月度报告**：提供详细的月度收支统计和可视化图表（饼状图、柱状图）
- **账单搜索**：支持按时间、类别、关键字等多维度查询账单详情
- **自定义账单类型**：用户可自定义账单分类
- **家庭版/个人版**：支持家庭成员管理和家庭账单统计
- **云端备份**：前后端分离架构，账单数据云端存储

### 技术特点
- **通知读取**：基于Android NotificationListenerService，实时监听支付通知
- **前后端分离**：采用云端数据存储，需要联网使用
- **组件化设计**：使用Fragment等组件化技术，降低代码耦合度
- **美观的UI**：采用仿iOS风格的对话框和动画效果，使用SVG图标
- **快捷功能**：支持桌面小部件和快速设置磁贴

### 支持的支付平台
- 微信支付（WeChat Pay）
- 支付宝（Alipay）
- 支付宝小荷包

---

## Project Overview (English)

AutoBookKeeping is an intelligent bookkeeping application designed for Android platforms. It automatically recognizes and records transaction information by monitoring payment notifications from WeChat and Alipay. The app uses a front-end and back-end separation architecture with cloud data backup, allowing users to complete daily income and expense records without manual input.

### Key Features
- **Automatic Bill Recognition**: Monitors WeChat and Alipay payment notifications, automatically extracts and records transaction information
- **Alipay Xiaohebao Support**: Recognizes Alipay Xiaohebao (small wallet) transactions
- **Monthly Reports**: Provides detailed monthly income and expense statistics with visualization charts (pie charts, bar charts)
- **Bill Search**: Supports multi-dimensional queries by time, category, keywords, etc.
- **Custom Bill Types**: Users can customize bill categories
- **Family/Personal Mode**: Supports family member management and family bill statistics
- **Cloud Backup**: Front-end and back-end separation architecture with cloud data storage

### Technical Highlights
- **Notification Listening**: Based on Android NotificationListenerService for real-time payment notification monitoring
- **Front-End/Back-End Separation**: Cloud data storage, requires internet connection
- **Component-Based Design**: Uses Fragment and other component technologies to reduce code coupling
- **Beautiful UI**: iOS-style dialogs and animations, SVG icons
- **Quick Access**: Supports home screen widgets and quick settings tiles

### Supported Payment Platforms
- WeChat Pay
- Alipay
- Alipay Xiaohebao

---

## 使用说明

本软件的所有使用说明已更新至wiki,还请移步wiki查看

https://github.com/FuShengDuoBuYu/AutoBookKeepingBeta/wiki

> 注 : 因为阿里云服务器到期(一核一G一年居然收我一千多块钱...),所以换用免费的Render后端和MongoDB atlas部署了,由于该后端服务器可能被自动挂起,所以可能会出现网络卡顿(约1min)左右的情况,属于正常.
---

## 版本更新日志

### v1.4

#### 新增
- 新增`全局公告`能力: 首页可接收后端下发公告弹窗
- 新增后端公告管理能力: 支持按版本号控制展示范围
- 新增登录后`用户资料+账单云端同步`流程,首次进入可自动拉取云端账单

#### 架构与接口升级
- 后端升级为模块化`FastAPI`路由架构(认证/用户资料/家庭/账单写入/账单查询/公告)
- 新增接口请求模型,`Swagger/OpenAPI`文档可读性增强
- 订单数据层对齐数据库字段规范(`user_id/date/way/text/category`),并保持前端兼容返回
- 用户数据层对齐数据库字段规范(`phone_num/family_id/family_identity`),统一服务层映射

#### 安全与稳定性
- 登录鉴权从明文密码比对升级为哈希校验,并兼容历史明文账号自动迁移
- 账单下载流程改为`成功拉取后事务覆盖本地`,避免网络异常导致本地数据被清空
- 补充多处网络资源释放与异常分支处理,降低卡死与连接泄漏风险

#### 前端体验优化
- 登录/注册弹窗增加输入提示,并优化自动注册文案
- 家庭检查时机后置到用户资料同步后,减少误触发提醒
- 头像展示增加无效Base64兜底,避免头像解码异常引发崩溃
- 个人页/家庭页/设置页的云端同步后刷新逻辑优化,账单展示更及时

#### 关键修复
- 修复月度报告中历史月份切换异常,支持查看过往月份
- 修复部分页面数据库/游标资源未及时关闭的问题
- 修复账单修改接口返回文案错误(由“添加成功”改为“修改成功”)

---

### v1.3 

#### 移除
- 移除短信读取接口,全面改为通知读取接口
- 移除`LBS`定位功能,回归纯粹记账功能
- 移除`银行号码`功能
- 移除~~影响开屏速度~~的开屏动画
- 移除`家庭代办`功能
- 移除家庭代办`通知`

#### 新增
- 新增支持`支付宝`小荷包账单识别
- 新增饼状图选择自定义条目
- 新增`自定义账单类型`功能
- 新增支持删除和修改过往任意时间的账单

#### 优化
- 优化`账单详细查找功能`,支持排序和筛选
- 优化饼状图展示效果
- 优化部分代码结构
- 优化后台弹窗逻辑,改为通知栏提出通知

#### 修复
- 修复`收入进度条`一直显示为`1元`的bug

---

### v1.2

##### 本次更新带来了巨大的更新变化,希望大家用的更加高效便捷

###### 家庭版更新
- 用户可以在首页通过滑动快速切换家庭版/个人版
- 用户可以在月度报告滑动查看家庭/个人的月度报告
- 新增家庭信息,可以灵活添加家庭成员
###### UI
- 所有UI图标重绘svg格式,更加清晰生动
- 新增部分动画,更加流畅美观
- 设置页面重新布局,更加符合开发使用逻辑
- 所有提示框,dialog等采用**仿IOS格式**,更加友好
###### 架构
- 完全采用**前后端分离**,账单信息云端备份
  - **需要联网才能正常使用本应用**
- 重构前端代码,降低耦合度,使用fragment等进行组件化设计
###### 个人信息
- 新增**个人信息**完善,可以设置头像,账号,家庭成员,昵称等信息
###### 功能
- 新增**银行号码**功能,设置银行号码可以精确识别账单
- 新增详细账单查询,可以根据时间,版本,类别,关键字等信息**联合查询**账单详情
- **进程隐藏**,用户进入多任务栏时,不会看到本应用运行在后台

---

## 技术栈 / Tech Stack

### Android开发框架
- **最低SDK版本**: Android 10 (API 29)
- **目标SDK版本**: Android 12 (API 31)
- **编程语言**: Java 8
- **构建工具**: Gradle

### 核心依赖库
- **Material Design**: Material Components for Android
- **数据可视化**: MPAndroidChart (图表库)
- **UI组件**: QMUI (腾讯UI组件库)
- **网络请求**: OkHttp
- **图片选择**: PictureSelector
- **数据库**: MySQL Connector

### 主要功能模块
1. **通知监听服务** (`NotificationReceiver`)
   - 继承NotificationListenerService
   - 监听微信和支付宝的支付通知
   - 实时解析交易信息

2. **账单管理**
   - 主界面 (`MainActivity`)
   - 账单详情 (`OrderDetailActivity`)
   - 账单搜索 (`OrderItemSearchActivity`)

3. **数据统计**
   - 月度报告 (`MonthReportActivity`)
   - 饼状图和柱状图展示
   - 收入/支出进度条

4. **系统集成**
   - 桌面小部件 (`OrderWidget`)
   - 快速设置磁贴 (`StartAutoBookTileService`)
   - 开机自启动 (`BootBroadcastReceiver`)

### 权限要求
- 通知访问权限（用于监听支付通知）
- 网络访问权限（用于云端数据同步）
- 存储权限（用于数据备份）
- 前台服务权限（用于后台监听）

---

## 应用截图 / Screenshots

应用截图请查看 `ReadmeImage` 目录，包含以下功能的展示：
- 主界面和UI动画效果
- 月度报告和数据可视化
- 账单搜索和详情页面
- 设置页面和个人信息
- 家庭模式和家庭待办
- 深色模式支持

---

## 开发说明 / Development

### 环境要求
- Android Studio (推荐最新版本)
- JDK 8 或更高版本
- Android SDK (API 29+)

### 构建项目
```bash
# 克隆仓库
git clone https://github.com/FuShengDuoBuYu/AutoBookKeepingBeta.git

# 进入项目目录
cd AutoBookKeepingBeta/AutoBookKeeping

# 使用Gradle构建
./gradlew build

# 安装到设备
./gradlew installDebug
```

### 注意事项
- 本应用需要**通知访问权限**才能正常工作，首次使用需要在系统设置中授予权限
- 应用采用**云端存储**，需要联网才能正常使用
- 部分功能需要**辅助功能权限**和**悬浮窗权限**

---

## 贡献指南 / Contributing

欢迎提交Issue和Pull Request来改进本项目！

---

## 许可证 / License

本项目的许可证信息请查看仓库根目录的LICENSE文件。

---

## 联系方式 / Contact

如有问题或建议，请通过以下方式联系：
- GitHub Issues: https://github.com/FuShengDuoBuYu/AutoBookKeepingBeta/issues
- Wiki文档: https://github.com/FuShengDuoBuYu/AutoBookKeepingBeta/wiki

---





