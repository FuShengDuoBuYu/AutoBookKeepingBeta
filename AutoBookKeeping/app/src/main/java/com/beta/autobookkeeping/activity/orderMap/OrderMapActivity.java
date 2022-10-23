package com.beta.autobookkeeping.activity.orderMap;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.LatLngBounds;
import com.beta.autobookkeeping.R;

public class OrderMapActivity extends AppCompatActivity {

    private MapView mvOrderMap;
    private AMap aMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_map);
        mvOrderMap = findViewById(R.id.mv_order_map);
        mvOrderMap.onCreate(savedInstanceState);
        //设置基准位置在上海
        aMap = mvOrderMap.getMap();
        aMap.moveCamera(com.amap.api.maps2d.CameraUpdateFactory.newLatLngZoom(new com.amap.api.maps2d.model.LatLng(31.230416, 121.473701), 12));
        //在地图上标注当前位置
        AMapLocationClient mLocationClient = null;
        try {
            mLocationClient = new AMapLocationClient(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mLocationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation != null) {
                    if (aMapLocation.getErrorCode() == 0) {
                        //定位成功回调信息，设置相关消息
                        aMapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                        double latitude = aMapLocation.getLatitude();//获取纬度
                        double longitude = aMapLocation.getLongitude();//获取经度
                        aMapLocation.getAccuracy();//获取精度信息
                        com.amap.api.maps2d.model.LatLng latLng = new com.amap.api.maps2d.model.LatLng(latitude, longitude);
                        aMap.addMarker(new com.amap.api.maps2d.model.MarkerOptions().position(latLng).title("当前位置"));
                        aMap.moveCamera(com.amap.api.maps2d.CameraUpdateFactory.newLatLngZoom(latLng, 12));
                        Log.i("OrderMapActivity", "onLocationChanged: " + latitude + " " + longitude);
                    } else {
                        //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                        Log.e("AmapError", "location Error, ErrCode:"
                                + aMapLocation.getErrorCode() + ", errInfo:"
                                + aMapLocation.getErrorInfo());
                    }
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

        //在地图上标注10个订单位置,堆叠显示

        com.amap.api.maps2d.model.LatLng latLng1 = new com.amap.api.maps2d.model.LatLng(31.230416, 121.49);
        com.amap.api.maps2d.model.LatLng latLng2 = new com.amap.api.maps2d.model.LatLng(31.230416, 121.433701);
        com.amap.api.maps2d.model.LatLng latLng3 = new com.amap.api.maps2d.model.LatLng(31.230416, 121.473701);
        com.amap.api.maps2d.model.LatLng latLng4 = new com.amap.api.maps2d.model.LatLng(31.230416, 121.401);
        com.amap.api.maps2d.model.LatLng latLng5 = new com.amap.api.maps2d.model.LatLng(31.230416, 121.4301);
        com.amap.api.maps2d.model.LatLng latLng6 = new com.amap.api.maps2d.model.LatLng(31.20416, 121.473701);
        com.amap.api.maps2d.model.LatLng latLng7 = new com.amap.api.maps2d.model.LatLng(31.2416, 121.473701);
        com.amap.api.maps2d.model.LatLng latLng8 = new com.amap.api.maps2d.model.LatLng(31.216, 121.473701);
        com.amap.api.maps2d.model.LatLng latLng9 = new com.amap.api.maps2d.model.LatLng(31.2316, 121.473701);
        com.amap.api.maps2d.model.LatLng latLng10 = new com.amap.api.maps2d.model.LatLng(31.230, 121.473701);
        aMap.addMarker(new com.amap.api.maps2d.model.MarkerOptions().position(latLng1).title("订单1"));
        aMap.addMarker(new com.amap.api.maps2d.model.MarkerOptions().position(latLng2).title("订单2"));
        aMap.addMarker(new com.amap.api.maps2d.model.MarkerOptions().position(latLng3).title("订单3"));
        aMap.addMarker(new com.amap.api.maps2d.model.MarkerOptions().position(latLng4).title("订单4"));
        aMap.addMarker(new com.amap.api.maps2d.model.MarkerOptions().position(latLng5).title("订单5"));
        aMap.addMarker(new com.amap.api.maps2d.model.MarkerOptions().position(latLng6).title("订单6"));
        aMap.addMarker(new com.amap.api.maps2d.model.MarkerOptions().position(latLng7).title("订单7"));
        aMap.addMarker(new com.amap.api.maps2d.model.MarkerOptions().position(latLng8).title("订单8"));
        aMap.addMarker(new com.amap.api.maps2d.model.MarkerOptions().position(latLng9).title("订单9"));
        aMap.addMarker(new com.amap.api.maps2d.model.MarkerOptions().position(latLng10).title("订单10"));

        //显示地图上所有的marker
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(latLng1);
        builder.include(latLng2);
        builder.include(latLng3);
        builder.include(latLng4);
        builder.include(latLng5);
        builder.include(latLng6);
        builder.include(latLng7);
        builder.include(latLng8);
        builder.include(latLng9);
        builder.include(latLng10);
        LatLngBounds bounds = builder.build();
        aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 10));

        //设置地图的缩放级别
        aMap.moveCamera(CameraUpdateFactory.zoomTo(10));

        //设置地图的中心点
        aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng1));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mvOrderMap.onDestroy();
    }


}