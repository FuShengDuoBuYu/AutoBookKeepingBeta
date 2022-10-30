package com.beta.autobookkeeping.activity.orderMap;

import static Util.ConstVariable.IP;

import androidx.appcompat.app.AppCompatActivity;

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
import com.amap.api.maps2d.model.LatLngBounds;
import com.beta.autobookkeeping.R;
import com.beta.autobookkeeping.activity.orderDetail.OrderDetailActivity;
import com.hss01248.dialog.StyledDialog;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

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

    private QMUIRoundButton btnPersonalVersion,btnFamilyVersion;
    private View.OnClickListener btnPersonalVersionListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //修改按钮样式
            btnPersonalVersion.setBackgroundColor(getResources().getColor(R.color.blue));
            btnPersonalVersion.setTextColor(getResources().getColor(R.color.white));
            btnFamilyVersion.setBackgroundColor(getResources().getColor(R.color.item_background));
            btnFamilyVersion.setTextColor(getResources().getColor(R.color.primary_font));
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
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_map);
        findViewByIdAndInit();
        mvOrderMap.onCreate(savedInstanceState);
        initOrderMap();
    }


    private void findViewByIdAndInit() {
        mvOrderMap = findViewById(R.id.mv_order_map);
        aMap = mvOrderMap.getMap();
        //移动到上海
        aMap.moveCamera(com.amap.api.maps2d.CameraUpdateFactory.newLatLngZoom(new com.amap.api.maps2d.model.LatLng(31.230416, 121.473701), 12));
        btnPersonalVersion = findViewById(R.id.btn_personal_version);
        btnPersonalVersion.setOnClickListener(btnPersonalVersionListener);
        btnFamilyVersion = findViewById(R.id.btn_family_version);
        btnFamilyVersion.setOnClickListener(btnFamilyVersionListener);
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
                //上传后端
                String url = IP + "/findTopNPersonalOrderMapPlace/"+ SpUtils.get(OrderMapActivity.this,"phoneNum","")+ "/10";
                OkHttpClient client = new OkHttpClient();
                Request requst = new Request.Builder()
                        .url(url)
                        .get()
                        .build();
                try {
                    Response response = client.newCall(requst).execute();
                    if (response.code() == 200) {
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        if (jsonResponse.getBoolean("success")) {
                            //转为JsonArray
                            JSONArray jsonArray = jsonResponse.getJSONArray("data");
                            renderOrderMapMarks(jsonArray);
                        }
                    } else {
                        Looper.prepare();
                        ProjectUtil.toastMsg(OrderMapActivity.this, "服务器出错");
                        Looper.loop();
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
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        double latitude = jsonObject.getDouble("latitude");
                        double longitude = jsonObject.getDouble("longitude");
                        double money = jsonObject.getDouble("money");
                        String userId = jsonObject.getString("userId");
                        Log.d("OrderMapActivity", "renderOrderMapMarks: " + latitude + " " + longitude + " " + money + " " + userId);
                        aMap.addMarker(new com.amap.api.maps2d.model.MarkerOptions()
                                .position(new com.amap.api.maps2d.model.LatLng(latitude, longitude))
                                .title("金额：" + money)
                                .snippet("用户：" + userId)
                                .draggable(true));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

}