package com.beta.autobookkeeping.activity.orderMap;

import static Util.ConstVariable.IP;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.LatLngBounds;
import com.amap.api.maps2d.model.MarkerOptions;
import com.beta.autobookkeeping.R;
import com.beta.autobookkeeping.activity.orderDetail.OrderDetailActivity;
import com.hss01248.dialog.StyledDialog;
import com.hss01248.dialog.interfaces.MyItemDialogListener;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Util.ProjectUtil;
import Util.SpUtils;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OrderMapActivity extends AppCompatActivity {

    private MapView mvOrderMap;
    private AMap aMap;
    private String currentVersion = "Personal";
    private Integer searchItems = 10;
    private Pair<Double,Double> location = null;
    private List<Pair<Double,Double>> orderLocations = new ArrayList<>();
    private QMUIRoundButton btnPersonalVersion,btnFamilyVersion,btnConfirmSearch,btnChooseItem;
    private View.OnClickListener btnPersonalVersionListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //修改按钮样式
            btnPersonalVersion.setBackgroundColor(getResources().getColor(R.color.blue));
            btnPersonalVersion.setTextColor(getResources().getColor(R.color.white));
            btnFamilyVersion.setBackgroundColor(getResources().getColor(R.color.item_background));
            btnFamilyVersion.setTextColor(getResources().getColor(R.color.primary_font));
            currentVersion = "Personal";
        }
    };
    private View.OnClickListener btnFamilyVersionListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //修改按钮样式
            btnFamilyVersion.setBackgroundColor(getResources().getColor(R.color.blue));
            btnFamilyVersion.setTextColor(getResources().getColor(R.color.white));
            btnPersonalVersion.setBackgroundColor(getResources().getColor(R.color.item_background));
            btnPersonalVersion.setTextColor(getResources().getColor(R.color.primary_font));
            currentVersion = "Family";
        }
    };
    private View.OnClickListener btnConfirmSearchListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //清除所有已有mark
            aMap.clear();
            initOrderMap();
        }
    };
    private View.OnClickListener btnChooseItemListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            List<String> items = new ArrayList<>();
            items.add("10");
            items.add("20");
            items.add("30");
            items.add("40");
            items.add("50");
            StyledDialog.buildIosSingleChoose(items, new MyItemDialogListener() {
                @Override
                public void onItemClick(CharSequence text, int position) {
                    searchItems = Integer.parseInt(text.toString());
                    btnChooseItem.setText(text);
                }
            }).show();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_map);
        getCurrentLocation();
        findViewByIdAndInit();
        mvOrderMap.onCreate(savedInstanceState);
        initOrderMap();
    }


    private void findViewByIdAndInit() {
        mvOrderMap = findViewById(R.id.mv_order_map);

        aMap = mvOrderMap.getMap();
        //移动到指定经纬度
        btnPersonalVersion = findViewById(R.id.btn_personal_version);
        //初始在个人版
        btnPersonalVersion.setBackgroundColor(getResources().getColor(R.color.blue));
        btnPersonalVersion.setTextColor(getResources().getColor(R.color.white));
        btnPersonalVersion.setOnClickListener(btnPersonalVersionListener);
        btnFamilyVersion = findViewById(R.id.btn_family_version);
        btnFamilyVersion.setOnClickListener(btnFamilyVersionListener);
        btnConfirmSearch = findViewById(R.id.btn_confirm_search);
        btnConfirmSearch.setOnClickListener(btnConfirmSearchListener);
        btnChooseItem = findViewById(R.id.btn_choose_items);
        btnChooseItem.setOnClickListener(btnChooseItemListener);
    }

    private void getCurrentLocation(){
        AMapLocationClient mLocationClient = null;
        try {
            mLocationClient = new AMapLocationClient(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mLocationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
                    //定位成功回调信息，设置相关消息
                    location = new Pair<>(aMapLocation.getLatitude(),aMapLocation.getLongitude());
                    aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.first,location.second), 15));
                }
            }
        });
        //初始化定位参数
        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //获取一次定位结果：
        //该方法默认为false。
        mLocationOption.setOnceLocation(true);
        //获取最近3s内精度最高的一次定位结果：
        mLocationOption.setOnceLocationLatest(true);
        //设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mvOrderMap.onDestroy();
    }

    //初始化账单地图
    private void initOrderMap() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String idInfo = (String) (currentVersion.equals("Personal") ? SpUtils.get(OrderMapActivity.this, "phoneNum", "") : SpUtils.get(OrderMapActivity.this, "familyId", ""));
                //上传后端
                String url = IP + "/findTopN"+currentVersion+"OrderMapPlace/"+ idInfo+ "/"+searchItems;
                OkHttpClient client = new OkHttpClient();
                Request requst = new Request.Builder().url(url).get().build();
                try {
                    Response response = client.newCall(requst).execute();
                    if (response.code() == 200) {
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonResponse.getJSONArray("data");
                        renderOrderMapMarks(jsonArray);
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void renderOrderMapMarks(JSONArray jsonArray) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                orderLocations.clear();
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        double latitude = jsonObject.getDouble("latitude");
                        double longitude = jsonObject.getDouble("longitude");
                        orderLocations.add(new Pair<>(latitude, longitude));
                        double money = jsonObject.getDouble("money");
                        String userId = jsonObject.getString("userId");
                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(new com.amap.api.maps2d.model.LatLng(latitude, longitude))
                                .title("金额：" + money)
                                .snippet("用户：" + userId)
                                .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.order_map_marker)));
                        aMap.addMarker(markerOptions);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                setMapCenterAndZoom();
            }
        });
    }

    private void setMapCenterAndZoom(){
        //计算得到zoom
        //获取距离最远的经度的点的经度差
        double maxLongitude = 0;
        for(int i = 0; i < orderLocations.size(); i++){
            double longitude = orderLocations.get(i).second;
            for(int j = i+1; j < orderLocations.size(); j++){
                double longitude2 = orderLocations.get(j).second;
                if(Math.abs(longitude - longitude2) > maxLongitude){
                    maxLongitude = Math.abs(longitude - longitude2);
                }
            }
        }
        float zoom = 0;
        if(maxLongitude > 180)
            zoom = 0;
        else{
            zoom = (float) (Math.log(360/maxLongitude)/Math.log(2));
        }
        if(location==null){
            //设置zoom
            aMap.moveCamera(CameraUpdateFactory.zoomTo(zoom));
        }
        else{
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new com.amap.api.maps2d.model.LatLng(orderLocations.get(0).first, orderLocations.get(0).second), zoom));
        }
    }
}