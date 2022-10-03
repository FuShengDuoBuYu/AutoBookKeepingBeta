package com.beta.autobookkeeping.activity.main;

import static Util.ProjectUtil.getCurrentDay;
import static Util.ProjectUtil.getCurrentMonth;
import static Util.ProjectUtil.getCurrentYear;
import static Util.ProjectUtil.toastMsg;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.animation.Animator;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.beta.autobookkeeping.activity.main.checking.FamilyChecking;
import com.beta.autobookkeeping.activity.main.checking.PermissonChecking;
import com.beta.autobookkeeping.activity.main.checking.UserRegister;
import com.beta.autobookkeeping.activity.main.entity.OrderDayItems;
import com.beta.autobookkeeping.activity.main.entity.OrderInfo;
import com.beta.autobookkeeping.activity.monthReport.MonthReportActivity;
import com.beta.autobookkeeping.activity.orderDetail.OrderDetailActivity;
import com.beta.autobookkeeping.activity.settings.SettingsActivity;
import com.beta.autobookkeeping.R;
import com.beta.autobookkeeping.fragment.orderDetail.FamilyOrderDetailFragment;
import com.beta.autobookkeeping.fragment.orderDetail.PersonalOrderDetailFragment;
import com.beta.autobookkeeping.fragment.orderDetail.TabOrderDetailFragmentPagerAdapter;
import com.beta.autobookkeeping.BaseApplication;
import com.beta.autobookkeeping.receiver.NotificationReceiver;
import com.beta.autobookkeeping.smsTools.SMSDataBase;
import com.beta.autobookkeeping.smsTools.SMSService;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.hss01248.dialog.StyledDialog;
import com.hss01248.dialog.interfaces.MyDialogListener;
import com.willowtreeapps.spruce.Spruce;
import com.willowtreeapps.spruce.SpruceAnimator;
import com.willowtreeapps.spruce.animation.DefaultAnimations;
import com.willowtreeapps.spruce.sort.DefaultSort;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import Util.ProjectUtil;
import Util.SpUtils;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 tabViewPager;
    private List<Fragment> fragments;
    private TabLayout tabLayout;
    private TabOrderDetailFragmentPagerAdapter tabOrderDetailFragmentPagerAdapter;
    private Button btnPlusNewOrder,btnSettings,btnSearchMonthlyReport;
    private TextView tvAllTodayOrder,tvAllMonthOrder,tv_title;
    private LinearLayout lvOrderDetail,llCostTitle;
    private ScrollView svOrderDetail;
    Bundle bundle;
    int currentViewPageFragmentIndex = 0;
    //数据库实例
    SQLiteDatabase db;
    //所有账单信息的list
    private List<OrderInfo> orderInfos = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //设置初始偏好数据
        initSpAndSqlLiteData();
        //先检查短信等权限是否获取
        PermissonChecking.ifGetPermission(MainActivity.this,MainActivity.this);
        findViewByIdAndInit();
        //初始化页面布局
        initFragmentAndViewPage();
        //开启读取短信线程
        startService(new Intent(MainActivity.this, SMSService.class));
        startService(new Intent(MainActivity.this, NotificationReceiver.class));
        //设置手机号
        setPhoneNum();
        FamilyChecking.checkFamily(this);
    }
    //初始化fragment和viewpage
    private void initFragmentAndViewPage(){
        fragments = new LinkedList<>();
        fragments.add(new PersonalOrderDetailFragment());
        fragments.add(FamilyOrderDetailFragment.newInstance("home"));
        tabOrderDetailFragmentPagerAdapter = new TabOrderDetailFragmentPagerAdapter(getSupportFragmentManager(),getLifecycle(), fragments);
        tabViewPager.setAdapter(tabOrderDetailFragmentPagerAdapter);
        //初始化显示第一个页面(个人)
        tabViewPager.setCurrentItem(0);
        //设置监听
        tabViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }
            @Override
            public void onPageSelected(int position) {
                //更新头部信息
                if(position==0){
                    showDayAndMonthMoney();
                }
                //家庭信息由对应的fragment负责更新
                super.onPageSelected(position);
            }
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });
        TabLayoutMediator mediator = new TabLayoutMediator(tabLayout, tabViewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                if(position==0){
                    tab.setText("个人版");
                }
                else{
                    tab.setText("家庭版");
                }
            }
        });
        mediator.attach();

    }

    //获取要展示的数据
    public ArrayList<ArrayList> getShowOrdersInfo(){
        ArrayList<ArrayList> res = new ArrayList<>();
        ArrayList<OrderInfo> orders = new ArrayList<>();
        ArrayList<OrderDayItems> orderDayItems = new ArrayList<>();
        //先获取本月都有哪些天有数据
        ArrayList<Integer> hasOrderDays = ProjectUtil.getHasOrderDays(getCurrentYear(),getCurrentMonth(),this);
        //依次查询这些天的账单
        for(int i = 0;i < hasOrderDays.size();i++) {
            //再加入每天的账单
            String sql = "select * from orderInfo where year = " + String.valueOf(getCurrentYear()) + " and month = " + String.valueOf(getCurrentMonth()) + " and day= " + String.valueOf(hasOrderDays.get(i));
            Cursor cursor = db.rawQuery(sql, null);
            int orderNums = 0;
            double dayMoney = 0.0;
            while (cursor.moveToNext()) {
                OrderInfo orderInfo = new OrderInfo(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getInt(2),
                        cursor.getInt(3),
                        cursor.getString(4),
                        cursor.getDouble(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getString(8),
                        cursor.getString(9)
                );
                orderNums++;
                dayMoney += cursor.getDouble(5);
                orders.add(orderInfo);
            }
            orderDayItems.add(new OrderDayItems(
                    getCurrentYear(),
                    getCurrentMonth(),
                    hasOrderDays.get(i),
                    dayMoney>0?"收入":"支出",
                    orderNums,
                    dayMoney
                    ));
        }
        res.add(orderDayItems);
        res.add(orders);
        return res;
    }

    //为各个组件设置事件
    private void findViewByIdAndInit(){
        //顶部导航栏
        tabLayout = findViewById(R.id.order_detail_tab);
        //viewpage
        tabViewPager = (ViewPager2) findViewById(R.id.orders_detail_view_page);
        tvAllTodayOrder = findViewById(R.id.tvAllTodayOrder);
        tvAllMonthOrder = findViewById(R.id.tvAllMonthOrder);
        //找到不同日期并显示
        lvOrderDetail = findViewById(R.id.lvOrderDetail);
        svOrderDetail = findViewById(R.id.svOrderDetail);
        tv_title = findViewById(R.id.tv_title);
        llCostTitle = findViewById(R.id.ll_cost_title);
        //找到新增和设置两个按钮
        btnPlusNewOrder = findViewById(R.id.btnPlusNewOrder);
        btnSettings = findViewById(R.id.btnSettings);
        //设置两个新增和设置按钮的两个监听事件
        btnPlusNewOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //跳转到新增界面
                Intent intent = new Intent(MainActivity.this, OrderDetailActivity.class);
                startActivity(intent);
            }
        });
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //跳转到设置界面
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
        //找到<查找月度报告>的按钮
        btnSearchMonthlyReport = findViewById(R.id.btnSearchMonthlyReport);
        //设置该按钮的监听事件
        btnSearchMonthlyReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MonthReportActivity.class);
                startActivity(intent);
            }
        });
    }

    //初始化数据Sp和sqlite
    private void initSpAndSqlLiteData(){
        //sq数据
        if(!SpUtils.contains(this,"OrderStatus")){
            SpUtils.put(this,"OrderStatus","个人版");
        }
        //初始化数据库
        SMSDataBase smsDb = new SMSDataBase(this,"orderInfo",null,1);
        db = smsDb.getWritableDatabase();
        if(!ifContainTable(db,"orderInfo")){
            String sql = "create table orderInfo(id int(8),year int(4),month int(2),day int(2),clock varchar(20),money numeric(10,2),bankName varchar(255),orderRemark varchar(255),costType varchar(255),userId varchar(255))";
            db.execSQL(sql);
        }
    }

    //判断是否有orderInfo表
    private boolean ifContainTable(SQLiteDatabase db,String tableName){
        String searchTable = "select name from sqlite_master where type='table' order by name;";
        Cursor cursor = db.rawQuery(searchTable,null);
        while (cursor.moveToNext()){
            if(cursor.getString(0).equals(tableName)){
                return true;
            }
        }
        return false;
    }

    //注册手机号
    private void setPhoneNum(){
        //用户设置电话号码和昵称
        if(
                SpUtils.get(this,"phoneNum","")==null ||
                SpUtils.get(this,"phoneNum","").equals("")
        ){
            StyledDialog.buildNormalInput("用户注册", "请输入手机号", "请输入密码", "确定", "取消", new MyDialogListener() {
                @Override
                public void onFirst() {}
                @Override
                public void onSecond() {}
                @Override
                public boolean onInputValid(CharSequence input1, CharSequence input2, EditText editText1, EditText editText2) {
                    if(input1.toString().matches("^[1][3,4,5,7,8,9][0-9]{9}$")&&(!(input2.toString()==null))&&(!input2.toString().equals(""))){
                        UserRegister.userRegister(input1.toString(),input2.toString(),MainActivity.this);
                        return true;
                    }else{
                        toastMsg(MainActivity.this,"输入有误,请重试");
                        return false;
                    }
                }
            }).show();
        }
    }

    @Override
    protected void onStart() {
        //显示账单状态
        tv_title.setText("我的收支");
        //获取本日和本月累计收支
        showDayAndMonthMoney();
        //获取并显示所有账单详情
//        showOrderDetailList();
        super.onStart();
    }

    //为今日和本月累计赋值刷新(个人)
    public void showDayAndMonthMoney(){
        //重新给月和日开销赋值
        tvAllMonthOrder.setText(String .format("%.2f",ProjectUtil.getMonthMoney(this)));
        tvAllTodayOrder.setText(String .format("%.2f",ProjectUtil.getDayMoney(getCurrentYear(),getCurrentMonth(),getCurrentDay(),this)));
//        SpruceAnimator spruceAnimator = new Spruce
//                .SpruceBuilder(llCostTitle)
//                .sortWith(new DefaultSort(/*interObjectDelay=*/50L))
//                .animateWith(new Animator[] {DefaultAnimations.fadeInAnimator(llCostTitle, /*duration=*/1500)})
//                .start();
    }

    public void showDayAndMonthMoney(String dayMoney,String monthMoney){
        tvAllTodayOrder.setText(dayMoney);
        tvAllMonthOrder.setText(monthMoney);
//        SpruceAnimator spruceAnimator = new Spruce
//                .SpruceBuilder(llCostTitle)
//                .sortWith(new DefaultSort(/*interObjectDelay=*/50L))
//                .animateWith(new Animator[] {DefaultAnimations.fadeInAnimator(llCostTitle, /*duration=*/1500)})
//                .start();
    }
}