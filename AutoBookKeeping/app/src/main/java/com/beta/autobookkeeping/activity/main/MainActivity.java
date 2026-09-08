package com.beta.autobookkeeping.activity.main;

import static Util.ProjectUtil.getCurrentDay;
import static Util.ProjectUtil.getCurrentMonth;
import static Util.ProjectUtil.getCurrentYear;
import static Util.ProjectUtil.toastMsg;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.beta.autobookkeeping.activity.main.checking.FamilyChecking;
import com.beta.autobookkeeping.activity.main.checking.PermissonChecking;
import com.beta.autobookkeeping.activity.main.checking.UserRegister;
import com.beta.autobookkeeping.activity.main.entity.OrderDayItems;
import com.beta.autobookkeeping.activity.main.entity.OrderInfo;
import com.beta.autobookkeeping.activity.monthReport.MonthReportActivity;
import com.beta.autobookkeeping.activity.orderDetail.OrderDetailActivity;
import com.beta.autobookkeeping.activity.settings.SettingsActivity;
import com.beta.autobookkeeping.activity.web.AiBillActivity;
import com.beta.autobookkeeping.R;
import com.beta.autobookkeeping.fragment.orderDetail.FamilyOrderDetailFragment;
import com.beta.autobookkeeping.fragment.orderDetail.PersonalOrderDetailFragment;
import com.beta.autobookkeeping.fragment.orderDetail.TabOrderDetailFragmentPagerAdapter;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import Util.ImageUtil;
import Util.ProjectUtil;
import Util.SpUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 tabViewPager;
    private List<Fragment> fragments;
    private TabLayout tabLayout;
    private TabOrderDetailFragmentPagerAdapter tabOrderDetailFragmentPagerAdapter;
    private ImageView btnPlusNewOrder,btnSettings;
    private Button btnSearchMonthlyReport,btnAiBill;
    private TextView tvAllTodayOrder,tvAllMonthOrder,tv_title;
    private LinearLayout lvOrderDetail,llCostTitle;
    private ScrollView svOrderDetail;
    private AlertDialog loginDialog;
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

        findViewByIdAndInit();
        //初始化页面布局
        initFragmentAndViewPage();

        //设置手机号
        setPhoneNum();
        if(!"".equals(SpUtils.get(this,"phoneNum",""))){
            PermissonChecking.ifGetPermission(this);
        }
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
            cursor.close();
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
        btnAiBill = findViewById(R.id.btnAiBill);
        //设置该按钮的监听事件
        btnSearchMonthlyReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MonthReportActivity.class);
                startActivity(intent);
            }
        });
        btnAiBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNum = (String) SpUtils.get(MainActivity.this,"phoneNum","");
                if(phoneNum == null || phoneNum.trim().equals("")){
                    toastMsg(MainActivity.this,"请先登录后再查看AI账单");
                    return;
                }
                Intent intent = new Intent(MainActivity.this, AiBillActivity.class);
                intent.putExtra("phoneNum", phoneNum.trim());
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
        db = openOrCreateDatabase(MainActivity.this.getFilesDir()+"/orderInfo.db",MODE_PRIVATE,null);
        if(!ifContainTable(db,"orderInfo")){
            String sql = "create table orderInfo(id int(8),year int(4),month int(2),day int(2),clock varchar(20),money numeric(10,2),bankName varchar(255),orderRemark varchar(255),costType varchar(255),userId varchar(255))";
            db.execSQL(sql);
            Log.d("MainActivity","创建orderInfo表成功");
        }
    }

    //判断是否有orderInfo表
    private boolean ifContainTable(SQLiteDatabase db,String tableName){
        String searchTable = "select name from sqlite_master where type='table' order by name;";
        Cursor cursor = db.rawQuery(searchTable,null);
        while (cursor.moveToNext()){
            if(cursor.getString(0).equals(tableName)){
                cursor.close();
                return true;
            }
        }
        cursor.close();
        return false;
    }

    //登录或注册账号。两个动作必须显式区分，避免输错手机号时创建新账号。
    private void setPhoneNum(){
        if(
                SpUtils.get(this,"phoneNum","")==null || SpUtils.get(this,"phoneNum","").equals("")
        ){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("登录账号");
            builder.setMessage("已有账号请选择登录；首次使用请选择注册。换新设备登录后会从云端恢复个人资料和账单。 ");
            final EditText etPhoneNum = new EditText(this);
            etPhoneNum.setInputType(InputType.TYPE_CLASS_PHONE);
            etPhoneNum.setHint("请输入手机号");
            final EditText etPassword = new EditText(this);
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            etPassword.setHint("请输入密码");
            LinearLayout ll = new LinearLayout(this);
            ll.setOrientation(LinearLayout.VERTICAL);
            ll.addView(etPhoneNum);
            ll.addView(etPassword);
            builder.setView(ll);
            builder.setCancelable(false);
            builder.setPositiveButton("登录", null);
            builder.setNeutralButton("注册", null);
            loginDialog = builder.create();
            loginDialog.setOnShowListener(ignored -> {
                loginDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                    String phoneNum = etPhoneNum.getText().toString().trim();
                    String password = etPassword.getText().toString();
                    if(!phoneNum.matches("^1[3-9][0-9]{9}$") || password.isEmpty()){
                        toastMsg(MainActivity.this,"请输入正确的手机号和密码");
                        return;
                    }
                    UserRegister.login(phoneNum,password,MainActivity.this);
                });
                loginDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(view -> {
                    String phoneNum = etPhoneNum.getText().toString().trim();
                    String password = etPassword.getText().toString();
                    if(!phoneNum.matches("^1[3-9][0-9]{9}$")){
                        toastMsg(MainActivity.this,"请输入正确的手机号");
                        return;
                    }
                    if(password.length() < 6){
                        toastMsg(MainActivity.this,"注册密码至少需要6位");
                        return;
                    }
                    UserRegister.register(phoneNum,password,MainActivity.this);
                });
            });
            loginDialog.show();
        }else {
            UserRegister.syncAccountOnLaunch(this, (String) SpUtils.get(this,"phoneNum",""), new UserRegister.SyncCallback() {
                @Override
                public void onSuccess() {
                    refreshAfterCloudOrderSync();
                    FamilyChecking.checkFamily(MainActivity.this);
                }

                @Override
                public void onFailure(String message) {
                    toastMsg(MainActivity.this, message);
                    refreshAfterCloudOrderSync();
                    FamilyChecking.checkFamily(MainActivity.this);
                }
            });
        }
    }

    @Override
    protected void onStart() {
        //显示账单状态
        tv_title.setText("我的收支");
        //获取本日和本月累计收支
        showDayAndMonthMoney();
        pullRemoteNoticeAndShow();
        //获取并显示所有账单详情
//        showOrderDetailList();
        super.onStart();
    }

    private void pullRemoteNoticeAndShow(){
        new Thread(() -> {
            try {
                PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                int versionCode = packageInfo.versionCode;

                String url = Util.ConstVariable.IP + "/notices/global/latest/" + versionCode;
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).get().build();
                try(Response response = client.newCall(request).execute()){
                    if(response.code() != 200 || response.body() == null){
                        return;
                    }

                    JSONObject jsonResponse = new JSONObject(response.body().string());
                    if(!jsonResponse.getBoolean("success") || jsonResponse.isNull("data")){
                        return;
                    }

                    JSONObject data = jsonResponse.getJSONObject("data");
                    String noticeId = data.optString("noticeId", "");
                    if(noticeId.isEmpty()){
                        return;
                    }

                    String noticeKey = "remote_notice_shown_" + noticeId;
                    if(SpUtils.contains(MainActivity.this, noticeKey)){
                        return;
                    }

                    String title = data.optString("title", "通知");
                    String content = data.optString("content", "");
                    runOnUiThread(() -> new AlertDialog.Builder(MainActivity.this)
                            .setTitle(title)
                            .setMessage(content)
                            .setCancelable(false)
                            .setPositiveButton("我知道了", (dialog, which) -> {
                                SpUtils.put(MainActivity.this, noticeKey, true);
                                dialog.dismiss();
                            })
                            .show());
                }
            } catch (Exception ignored) {
            }
        }).start();
    }

    //为今日和本月累计赋值刷新(个人)
    public void showDayAndMonthMoney(){
        //重新给月和日开销赋值
        tvAllMonthOrder.setText(String .format("%.2f",ProjectUtil.getMonthMoney(this)));
        tvAllTodayOrder.setText(String .format("%.2f",ProjectUtil.getDayMoney(getCurrentYear(),getCurrentMonth(),getCurrentDay(),this)));
    }

    public void showDayAndMonthMoney(String dayMoney,String monthMoney){
        tvAllTodayOrder.setText(dayMoney);
        tvAllMonthOrder.setText(monthMoney);
    }

    public void refreshAfterCloudOrderSync(){
        com.beta.autobookkeeping.widget.OrderWidget.refreshAll(getApplicationContext());
        if (isFinishing() || isDestroyed()) return;
        showDayAndMonthMoney();
        if(fragments == null || fragments.isEmpty()){
            return;
        }
        Fragment personalFragment = fragments.get(0);
        if(personalFragment instanceof PersonalOrderDetailFragment && personalFragment.getView() != null){
            ((PersonalOrderDetailFragment) personalFragment).addViewByData(this);
        }
        if(fragments.size() > 1 && fragments.get(1) instanceof FamilyOrderDetailFragment){
            ((FamilyOrderDetailFragment) fragments.get(1)).refreshFamilyOrders();
        }
    }

    public void onAuthenticationSuccess(){
        if(loginDialog != null && loginDialog.isShowing()){
            loginDialog.dismiss();
        }
        refreshAfterCloudOrderSync();
        PermissonChecking.ifGetPermission(this);
    }

    public void clickFamilyItemToShowDetail(View v, Pair<View, String>... pairs){


        Intent intent = new Intent(MainActivity.this, DialogActivity.class);

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, pairs);
        intent.putExtra(pairs[0].second,ImageUtil.drawableToBitamp(((ImageView)pairs[0].first).getDrawable()));
        intent.putExtra(pairs[1].second, ImageUtil.drawableToBitamp(((ImageView)pairs[1].first).getDrawable()));
        intent.putExtra(pairs[2].second, ((TextView)pairs[2].first).getText().toString());
        intent.putExtra(pairs[3].second, ((TextView)pairs[3].first).getText().toString());
        intent.putExtra(pairs[4].second, ((TextView)pairs[4].first).getText().toString());
        intent.putExtra(pairs[5].second, ((TextView)pairs[5].first).getText().toString());
        //获取截图
        Bitmap bitmap = ImageUtil.getWindowScreenShot(MainActivity.this);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 1, stream);
        byte[] byteArray = stream.toByteArray();
        intent.putExtra("background",byteArray);
        startActivity(intent, options.toBundle());
    }

    @Override
    protected void onDestroy() {
        if(db != null && db.isOpen()){
            db.close();
        }
        super.onDestroy();
    }

    @Override protected void onResume() {
        super.onResume();
        com.beta.autobookkeeping.widget.OrderWidget.refreshAll(getApplicationContext());
    }
}
