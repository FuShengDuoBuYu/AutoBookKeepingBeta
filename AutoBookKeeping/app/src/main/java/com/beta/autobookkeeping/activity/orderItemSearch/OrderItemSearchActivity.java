package com.beta.autobookkeeping.activity.orderItemSearch;

import static Util.ConstVariable.IP;
import static Util.ProjectUtil.setDayOrderItem;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.beta.autobookkeeping.R;
import com.beta.autobookkeeping.activity.main.entity.OrderInfo;
import com.beta.autobookkeeping.activity.orderItemSearch.items.SearchConditionEntity;
import com.beta.autobookkeeping.activity.orderItemSearch.items.SearchOrderListViewAdapter;
import com.hss01248.dialog.StyledDialog;
import com.wdeo3601.dropdownmenu.DropDownMenu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Util.ProjectUtil;
import Util.SpUtils;
import Util.StringUtil;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OrderItemSearchActivity extends AppCompatActivity {

    private LinearLayout llDropMenu,llOrderList;
    private SearchConditionEntity searchConditionEntity = SearchConditionEntity.getINSTANCE();
    private DropDownMenu dropDownMenu = null;
    private ListView lvSearchOrders = null;
    private TextView tvSearchCost,tvSearchMoney,tvSearchIncome;
    private TextView tvSearchConditionVersion,tvSearchConditionDate,tvSearchConditionOtherDescription;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        searchConditionEntity.setContext(OrderItemSearchActivity.this);
        setContentView(R.layout.activity_order_item_search);
        findViewsById();
        initDropDownMenu();
    }

    @Override
    protected void onStart() {
        searchOrders();
        super.onStart();
    }

    //找到各个控件并进行数据的初始化
    public void findViewsById(){
        //找到控件
        llDropMenu = findViewById(R.id.ll_down_menu);
        dropDownMenu = findViewById(R.id.drop_down_menu);
        llOrderList = findViewById(R.id.ll_order_list);
        LinearLayout ll_search_order_list = (LinearLayout) View.inflate(this,R.layout.item_activity_order_search_order_list_item,null);
        lvSearchOrders = ll_search_order_list.findViewById(R.id.lv_search_orders);
        llOrderList.addView(ll_search_order_list);
        tvSearchCost = ll_search_order_list.findViewById(R.id.tv_search_cost);
        tvSearchMoney = ll_search_order_list.findViewById(R.id.tv_search_money);
        tvSearchIncome = ll_search_order_list.findViewById(R.id.tv_search_income);
        tvSearchConditionVersion = findViewById(R.id.tv_search_condition_version);
        tvSearchConditionDate = findViewById(R.id.tv_search_condition_date);
        tvSearchConditionOtherDescription = findViewById(R.id.tv_search_condition_other_description);
    }

    private void initDropDownMenu(){
        //初始化下拉菜单
        ArrayList<String> tabs = new ArrayList<>();
        tabs.add("版本");
        tabs.add("日期");
        tabs.add("其他筛选");
        dropDownMenu.setupDropDownMenu(tabs, searchConditionEntity.getPopupViews());
    }

    public void closeMenu(){
        dropDownMenu.closeMenu();
        searchOrders();
    }

    private void searchOrders(){
        //输出搜索条件
        searchConditionEntity.printSearchCondition();
        StyledDialog.buildLoading().show();
        //向后端拿数据
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = IP+"/searchOrders";
                OkHttpClient client = new OkHttpClient();
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("mode",searchConditionEntity.getMode());
                    //家庭号
                    String familyID = (String) SpUtils.get(OrderItemSearchActivity.this,"familyId","");
                    jsonObject.put("familyId",searchConditionEntity.getMode().equals("家庭版")?familyID:"");
                    //用户号
                    String userId = (String) SpUtils.get(OrderItemSearchActivity.this,"phoneNum","");
                    jsonObject.put("userId",userId);
                    jsonObject.put("year",searchConditionEntity.getYear());
                    jsonObject.put("month",searchConditionEntity.getMonth());
                    jsonObject.put("day",searchConditionEntity.getDay());
                    jsonObject.put("searchOrderRemark",searchConditionEntity.getSearchOrderRemark());
                    jsonObject.put("searchCostType", Arrays.toString(searchConditionEntity.getSearchCostType()));
                    jsonObject.put("ifIgnoreYear",searchConditionEntity.isIfIgnoreYear());
                    jsonObject.put("ifIgnoreMonth",searchConditionEntity.isIfIgnoreMonth());
                    jsonObject.put("ifIgnoreDay",searchConditionEntity.isIfIgnoreDay());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json;charset=utf-8"));
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    if(response.code()==200){
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        if(jsonResponse.getBoolean("success")){
                            Log.d("OrderItemSearchActivity","成功拿到数据");
                            Log.d("OrderItemSearchActivity",jsonResponse.toString());
                            Map<String,Object> map = StringUtil.Json2Map(jsonResponse.getJSONObject("data").toString());
                            afterSearchOrders(map);
                        }
                        else{
                            Looper.prepare();
                            ProjectUtil.toastMsg(OrderItemSearchActivity.this,jsonResponse.getString("message"));
                            Looper.loop();
                        }
                    }
                    else{
                        Looper.prepare();
                        ProjectUtil.toastMsg(OrderItemSearchActivity.this,"服务器出错");
                        Looper.loop();
                    }
                    // str为json字符串
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();



    }

    private void afterSearchOrders(Map<String,Object> map){
        //重新解析数据
        Map<String,String> userInfo = new HashMap<>();
        List<OrderInfo> ordersInfo = new ArrayList<>();
        double cost = 0,money = 0,income = 0;
        try {
            //遍历map的key
            for(Map.Entry<String, Object> entry : map.entrySet()){
                if(entry.getKey().equals("userInfo")){
                    JSONArray userInfoArray = new JSONArray(entry.getValue().toString());
                    for(int i=0;i<userInfoArray.length();i++){
                        JSONObject userInfoObject = userInfoArray.getJSONObject(i);
                        userInfo.put(userInfoObject.getString("phoneNum"),userInfoObject.getString("portrait"));
                    }
                }
                else {
                    JSONArray jsonArray = new JSONArray(entry.getValue().toString());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        //解析订单信息
                        ordersInfo.add(new OrderInfo(
                                jsonArray.getJSONObject(i).getInt("id"),
                                jsonArray.getJSONObject(i).getInt("year"),
                                jsonArray.getJSONObject(i).getInt("month"),
                                jsonArray.getJSONObject(i).getInt("day"),
                                jsonArray.getJSONObject(i).getString("clock"),
                                jsonArray.getJSONObject(i).getDouble("money"),
                                jsonArray.getJSONObject(i).getString("bankName"),
                                jsonArray.getJSONObject(i).getString("orderRemark"),
                                jsonArray.getJSONObject(i).getString("costType"),
                                jsonArray.getJSONObject(i).getString("userId")
                        ));
                        //设置总支出、总收入、总金额
                        if(jsonArray.getJSONObject(i).getString("costType").equals("收入")){
                            income += jsonArray.getJSONObject(i).getDouble("money");
                        }
                        else{
                            cost += jsonArray.getJSONObject(i).getDouble("money");
                        }
                    }
                }
            }
        }
        catch (JSONException e){
            e.printStackTrace();
        }
        double finalCost = cost;
        double finalIncome = income;
        runOnUiThread(new Runnable() {
            @SuppressLint("DefaultLocale")
            @Override
            public void run() {
                //把数据给到Adapter
                lvSearchOrders.setAdapter(new SearchOrderListViewAdapter(ordersInfo,OrderItemSearchActivity.this,userInfo));
                //设置总支出、总收入、总金额,一位小数
                tvSearchCost.setText(String.format("%.1f", finalCost));
                tvSearchCost.setTextColor(Color.GREEN);
                tvSearchIncome.setText(String.format("%.1f", finalIncome));
                tvSearchIncome.setTextColor(Color.RED);
                tvSearchMoney.setText(String.format("%.1f", finalIncome+finalCost));
                tvSearchMoney.setTextColor(finalCost+finalIncome>0?Color.RED:Color.GREEN);
                //设置搜索条件
                tvSearchConditionVersion.setText(searchConditionEntity.getMode());
                tvSearchConditionDate.setText(searchConditionEntity.getDate());
                tvSearchConditionOtherDescription.setText(searchConditionEntity.getOtherDescription());

            }
        });
        StyledDialog.dismissLoading(this);
    }
}