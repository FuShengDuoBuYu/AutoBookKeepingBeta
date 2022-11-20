# 自动记账(for android)  v1.2

---

#### 获奖信息

- 华为云智慧校园应用创新大赛 优胜奖
- 姑苏杯 智慧校园暨复旦大学校园赛 三等奖

#### 课程信息
- 智能移动平台应用开发[SOFT130067.01]

#### v1.2全新功能
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

#### v1.3 展望
- 每次加入账单时,记录位置信息,根据LBS引入地图SDK后绘制消费地图
- 增加开屏动画,引入相关SDK
- 参与硬件传感器信息,加入水滴波纹状的预设花费目标
- ...

#### 使用依赖

> [[绘制图表](https://github.com/PhilJay/MPAndroidChart)](https://github.com/PhilJay/MPAndroidChart)
> implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'

> [[圆角图表](https://github.com/Tencent/QMUI_Android)](https://github.com/Tencent/QMUI_Android)
> implementation 'com.qmuiteam:qmui:2.0.0-alpha10'

> [[前后端交互](https://github.com/square/okhttp)](https://github.com/square/okhttp)
> implementation("com.squareup.okhttp3:okhttp:4.9.3")

> [[类IOS弹窗](https://github.com/hss01248/DialogUtil)](https://github.com/hss01248/DialogUtil)
> implementation ('com.github.hss01248:DialogUtil:2.0.2'){exclude group: 'com.android.support'}

> [[图片选择器](https://github.com/LuckSiege/PictureSelector)](https://github.com/LuckSiege/PictureSelector)
> implementation 'io.github.lucksiege:pictureselector:v3.10.5'
> implementation 'io.github.lucksiege:compress:v3.10.4'
> implementation 'io.github.lucksiege:ucrop:v3.10.4'

> [[下拉选择菜单](https://github.com/wdeo3601/DropdownMenu)](https://github.com/wdeo3601/DropdownMenu)
> implementation 'com.wdeo3601:drop-down-menu:1.0.5'

> [[日期选择器](https://github.com/limxing/DatePickerView)](https://github.com/limxing/DatePickerView)
> implementation 'com.github.limxing:DatePickerView:1.1.0'

> [[图标展示](https://www.iconfont.cn/)](https://www.iconfont.cn/)
> 阿里巴巴矢量图库

> [[开屏动画](https://github.com/wongzy/FancyView)](https://github.com/wongzy/FancyView)
> implementation 'site.gemus:openingstartanimation:1.0.0'

> [[状态栏沉浸](https://github.com/gyf-dev/ImmersionBar)](https://github.com/gyf-dev/ImmersionBar)
> implementation 'com.geyifeng.immersionbar:immersionbar:3.2.2'

> [[高德地图](https://developer.amap.com/demo/list/sdk/)](https://developer.amap.com/demo/list/sdk/)
> implementation 'com.amap.api:map2d:6.0.0'
> implementation 'com.amap.api:location:6.2.0'

> [[水波浪](https://github.com/gelitenight/WaveView)](https://github.com/gelitenight/WaveView)
> implementation 'com.gelitenight.waveview:waveview:1.0.0'

## 介绍

### 使用场景

- 该app基于java开发,主要功能为通过系统发送的**新短信**广播,读取手机中的短信,匹配得到账单数据,达到自动记账的效果
- 该app支持自动记账和手动记账,可以在读取到银行短信弹窗后自动记账,也可以*手动添加*,同时还支持丰富的图形化数据展示和查询功能
---
### 用前必读
1. 由于本软件是基于读取银行短信账单实现,因此**请务必开启银行的短信通知**
2. 为了让该软件能够正常运行,请开启本软件的**获取短信**和**后台弹出**权限,在首次启动app时,会对这些权限进行申请(由于国内UI定制,可能会出现不弹窗等问题,请手动赋予权限)
	<div style="display:flex;">
    <img src="https://github.com/FuShengDuoBuYu/AutoBookKeepingBeta/raw/master/ReadmeImage/permission/permission1.jpg" style="width:24%"/>
    <img src="https://github.com/FuShengDuoBuYu/AutoBookKeepingBeta/raw/master/ReadmeImage/permission/permission2.jpg" style="width:24%;margin-left:1%"/>
    <img src="https://github.com/FuShengDuoBuYu/AutoBookKeepingBeta/raw/master/ReadmeImage/permission/permission3.jpg" style="width:24%;margin-left:1%" />
   <img src="https://github.com/FuShengDuoBuYu/AutoBookKeepingBeta/raw/master/ReadmeImage/permission/permission4.jpg" style="width:24%;margin-left:1%" />
    </div>
	
3. 由于该app并非系统应用,所以请在后台设置其**锁住不被内存清理**,同时设置电量限制为**无限制**
4. 目前已经适配的银行:农业银行,建设银行,工商银行,招商银行,郑州银行等
---
### 使用指导

- 本app基本逻辑为显示某段时间的支出和收入的总和,当*总和为正(负*),代表该段时间收*支为正(负*).
- **首页**:
  
  - 首页展示本月的收支和本日的收支,而滑动可以查看家庭版/个人版的账单信息
  - 每一条记录项目,可以进行单击进入**编辑页面**,对金额,时间等一系列信息重新编辑
  - 每一条记录项目可以**长按**,进行**删除该项目**,删除的同时页面会进行同步刷新
  <div style="display:flex;">
    <img src="https://github.com/FuShengDuoBuYu/AutoBookKeepingBeta/raw/master/ReadmeImage/main_activity/1.gif" width="30%"/>
    <img src="https://github.com/FuShengDuoBuYu/AutoBookKeepingBeta/raw/master/ReadmeImage/main_activity/2.gif" style="width:30%;margin-left:5%"/>
    <img src="https://github.com/FuShengDuoBuYu/AutoBookKeepingBeta/raw/master/ReadmeImage/main_activity/3.gif" style="width:30%;margin-left:5%" />
  </div>
- **添加账单页**:
	
	- 每一个**按钮**,**点击后都可以进行相对选项的选择**,如时间,支付方式,消费类型,支出/消费等等
  <div style="display:flex;">
    <img src="https://github.com/FuShengDuoBuYu/AutoBookKeepingBeta/raw/master/ReadmeImage/add_order_activity/1.jpg" style="width:30%"/>
    <img src="https://github.com/FuShengDuoBuYu/AutoBookKeepingBeta/raw/master/ReadmeImage/add_order_activity/2.jpg" style="width:30%;margin-left:5%"/>
    <img src="https://github.com/FuShengDuoBuYu/AutoBookKeepingBeta/raw/master/ReadmeImage/add_order_activity/3.jpg" style="width:30%;margin-left:5%" />
  </div>
- **月度报告页**

  - 月度报告共分三个部分构成,分别是**总收支各月柱状图**,**本月总支出饼状图**以及**本月总支出排行榜**.
  - 总收支各月柱状图
    - 本柱状图可以读取最近**12个月**的总收支情况,形成一个柱状图,根据柱状图的颜色可以清晰的看出各个月的盈亏情况
    - 可以同样滑动查看家庭和个人报告
   - 本月支出饼状图
     - 本饼状图默认显示**本月的支出详情**,如果需要显示其他月份的支出详情,可以**点击柱状图上的按钮进行月份切换**,本柱状图和下**方的排行榜**会实时刷新
        - 注:**按钮选择时间不可选择未发生的时间**
     - 当点击饼状图的饼块时,会显示出该项目在支出中的总花费值.
  - 本月支出排行榜
  	- 根据各项目在本月中的花费,会有一个进度条形式的排行榜在显示,他们的**内条颜色与饼状图的饼块颜色对应**.其所有内条的总和,即为100％
  	- 同时支持点击条柱后查看具体详情(进入详情搜索页面)
  <div style="display:flex;">
    <img src="https://github.com/FuShengDuoBuYu/AutoBookKeepingBeta/raw/master/ReadmeImage/month_report_activity/1.jpg" style="width:30%"/>
    <img src="https://github.com/FuShengDuoBuYu/AutoBookKeepingBeta/raw/master/ReadmeImage/month_report_activity/2.jpg" style="width:30%;margin-left:5%"/>
    <img src="https://github.com/FuShengDuoBuYu/AutoBookKeepingBeta/raw/master/ReadmeImage/month_report_activity/3.jpg" style="width:30%;margin-left:5%" />
  </div>
- **设置页**
	
  - **进入个人中心**
  	- 在个人中心页面里可以设置昵称,家庭id,头像等个人信息
  <div style="display:flex;">
    <img src="https://github.com/FuShengDuoBuYu/AutoBookKeepingBeta/raw/master/ReadmeImage/settings_activity/personal_activity/2.gif" style="width:30%"/>
    <img src="https://github.com/FuShengDuoBuYu/AutoBookKeepingBeta/raw/master/ReadmeImage/settings_activity/personal_activity/1.jpg" style="width:30%;margin-left:5%"/>
    <img src="https://github.com/FuShengDuoBuYu/AutoBookKeepingBeta/raw/master/ReadmeImage/settings_activity/personal_activity/3.jpg" style="width:30%;margin-left:5%" />
  </div>


  - **添加银行号码**
  - 银行号码输入后可以根据**号码进行过滤银行短信**,提高短信读取的准确性
  - 如果没有设置,默认是**所有号码都会被放行**
  <div style="display:flex;">
    <img src="https://github.com/FuShengDuoBuYu/AutoBookKeepingBeta/raw/master/ReadmeImage/settings_activity/1.jpg" style="width:40%"/>
    <img src="https://github.com/FuShengDuoBuYu/AutoBookKeepingBeta/raw/master/ReadmeImage/settings_activity/2.jpg" style="width:40%;margin-left:20%"/>
  </div>


  - **查找账单**
	  - 用户可以根据模式,账单时间,账单类别,关键字等信息进行账单的查询
	  - 在账单查询顶部,会给出用户查到的所有数据的**支出,收入和收支**
  <div style="display:flex;">
    <img src="https://github.com/FuShengDuoBuYu/AutoBookKeepingBeta/raw/master/ReadmeImage/settings_activity/search_order_activity/1.gif" style="width:30%"/>
    <img src="https://github.com/FuShengDuoBuYu/AutoBookKeepingBeta/raw/master/ReadmeImage/settings_activity/search_order_activity/2.gif" style="width:30%;margin-left:5%"/>
    <img src="https://github.com/FuShengDuoBuYu/AutoBookKeepingBeta/raw/master/ReadmeImage/widget/1.jpg" style="width:30%;margin-left:5%" />
  </div>


- **小组件**
	- 该小组件可以在桌面中的小组件设置添加,其依旧是包含了**本日的总收支**和**本月的总收支**
	- 由于android相关限制以及省电的需求,该小组件*每10min进行一次刷新*,因此其**更新并不会与新建的账单信息完全同步,但会在10min内刷新同步**
	- 图片见上



---

### 其他
#### 特别感谢

##### 在这里,想要特别感谢我的女朋友,她是我这个软件的第二个用户(第一个是我自己hhh),正是因为她的督促,才会有1.2版本的大改动以及家庭版联网支持的大更新,本次版本的更新功不可没
##### 同时,还要感谢陈老师关于创新点的支持和对于该项目作为PJ的认可

### github地址

```
https://github.com/FuShengDuoBuYu/AutoBookKeepingBeta/
```



