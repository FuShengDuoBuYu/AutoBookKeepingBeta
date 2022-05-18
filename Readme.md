# 自动记账(for android)
---

- 新增月度报告中种类详情查看
- 修复同一短信频繁弹出的bug

## 介绍

### ==使用场景==

- 该app基于java开发,主要功能为通过系统发送的==新短信==广播,读取手机中的短信,匹配得到账单数据,达到自动记账的效果
- 该app支持自动记账和手动记账,如果出现支付后接收短信未弹窗,可以*手动添加*,同时还支持丰富的图形化数据展示和查询功能
---
### ==用前必读==
1. 由于本软件是基于读取银行短信账单实现,因此**请务必开启银行的短信通知**
2. 为了让该软件能够正常运行,请开启本软件的**获取短信**和**后台弹出**权限,在首次启动app时,会对这些权限进行申请(由于国内UI定制,可能会出现不弹窗等问题,请手动赋予权限)
	示例：<img src="C:\Users\fengchuiyusan\Desktop\AutoBookKeepingBeta\ReadmePic\permission.jpg" alt="permission" style="zoom: 25%;" />
3. 由于该app并非系统应用,所以请在后台设置其**锁住不被内存清理**,同时设置电量限制为**无限制**
4. 目前已经适配的银行:农业银行,交通银行,工商银行,招商银行,郑州银行等
---
### ==使用指导==
- 本app基本逻辑为显示某段时间的支出和收入的总和,当*总和为正(负*),代表该段时间收*支为正(负*).
- **首页**:
  - 账单信息只显示本月的信息,将每一天和月的收支进行总结并显示
    
    - 示例:
    
      <img src="C:\Users\fengchuiyusan\Desktop\AutoBookKeepingBeta\ReadmePic\Mainpage.jpg" alt="Mainpage" style="zoom:25%;" />
  - 每一条记录项目,可以进行==单击==进入**编辑页面**,对金额,时间等一系列信息重新编辑
    
       - 示例:<img src="C:\Users\fengchuiyusan\Desktop\AutoBookKeepingBeta\ReadmePic\AddOrder.jpg" alt="AddOrder" style="zoom:25%;" />
  - 每一条记录项目可以==长按==,进行**删除该项目**,删除的同时页面会进行同步刷新
    
      - 示例:<img src="C:\Users\fengchuiyusan\Desktop\AutoBookKeepingBeta\ReadmePic\deleteOrder.jpg" alt="deleteOrder" style="zoom:25%;" />
  
- **添加账单页**:
	- 每一个**按钮**,**点击后都可以进行相对选项的选择**,如时间,支付方式,消费类型,支出/消费等等
		- 示例:<img src="C:\Users\fengchuiyusan\Desktop\AutoBookKeepingBeta\ReadmePic\ChooseCostType.jpg" alt="ChooseCostType" style="zoom:25%;" />
- **月度报告页**

  - 月度报告共分三个部分构成,分别是**总收支各月柱状图**,**本月总支出饼状图**以及**本月总支出排行榜**.
  - ==总收支各月柱状图==
    - 本柱状图可以读取最近**6个月**的总收支情况,形成一个柱状图,根据柱状图可以清晰的看出各个月的盈亏情况
  	  - 示例:  <img src="C:\Users\fengchuiyusan\Desktop\AutoBookKeepingBeta\ReadmePic\monthReport (2).jpg" alt="monthReport (2)" style="zoom:25%;" />
   - ==本月支出饼状图==
     - 本饼状图默认显示**本月的支出详情**,如果需要显示其他月份的支出详情,可以**点击柱状图上的按钮进行月份切换**,本柱状图和下**方的排行榜**会实时刷新
        - 注:**按钮选择时间不可选择未发生的时间**
     - 当点击饼状图的饼块时,会显示出该项目在支出中的总花费值.
     	- 示例:<img src="C:\Users\fengchuiyusan\Desktop\AutoBookKeepingBeta\ReadmePic\MonthReport.jpg" alt="MonthReport" style="zoom:25%;" />
  - ==本月支出排行榜==
  	- 根据各项目在本月中的花费,会有一个进度条形式的排行榜在显示,他们的**内条颜色与饼状图的饼块颜色对应**.其所有内条的总和,即为100％
  		- 示例:<img src="C:\Users\fengchuiyusan\Desktop\AutoBookKeepingBeta\ReadmePic\Ranking.jpg" alt="Ranking" style="zoom:25%;" />

- **设置页**
	- 指定时间段查找:
		- 可以通过选择**按日查询**和**按月查询**分别显示**月度账单和日度账单**,其中都会显示该时间段的**总收支和总支出**
		- 示例:<img src="C:\Users\fengchuiyusan\Desktop\AutoBookKeepingBeta\ReadmePic\SettingSearch.jpg" alt="SettingSearch" style="zoom:25%;" />
	- 目前还有与云相关的功能待开发,敬请期待
	
- **小组件**
	- 该小组件可以在桌面中的小组件设置添加,其依旧是包含了**本日的总收支**和**本月的总收支**
	- 由于android相关限制以及省电的需求,该小组件*每10min进行一次刷新,*因此其==更新并不会与新建的账单信息完全同步,但会在10min内刷新同步==
	- 示例:<img src="C:\Users\fengchuiyusan\Desktop\AutoBookKeepingBeta\ReadmePic\widget.jpg" alt="widget" style="zoom:25%;" />



---

github地址

```
https://github.com/FuShengDuoBuYu/AutoBookKeepingBeta/
```



