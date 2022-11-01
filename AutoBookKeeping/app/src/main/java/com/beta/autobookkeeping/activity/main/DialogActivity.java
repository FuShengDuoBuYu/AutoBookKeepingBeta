package com.beta.autobookkeeping.activity.main;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.transition.Fade;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.beta.autobookkeeping.R;

import Util.ImageUtil;

public class DialogActivity extends AppCompatActivity {

    private LinearLayout ll_dialog_parent;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = View.inflate(this, R.layout.activity_main_dialog, null);
        //获取传来的bundle
        Bundle bundle = getIntent().getExtras();
        initView(view, bundle);
        setContentView(view);

    }

    private void initView(View v, Bundle bundle) {
        //找到内容
        ImageView portrait = v.findViewById(R.id.order_item_portrait_id);
        ImageView categoryImage = v.findViewById(R.id.order_item_category_image_id);
        TextView category = v.findViewById(R.id.order_item_category_id);
        TextView money = v.findViewById(R.id.order_item_price_id);
        TextView time = v.findViewById(R.id.order_item_time_id);
        TextView payway = v.findViewById(R.id.order_item_payway_id);
        TextView remark = v.findViewById(R.id.order_item_remark_id);
        ll_dialog_parent = v.findViewById(R.id.dialog_parent);
        ll_dialog_parent.setOnClickListener(v1->{
            finishAfterTransition();
        });
        byte[] bytes = (byte[])(bundle.get("background"));
        ll_dialog_parent.setBackground(new BitmapDrawable(BitmapFactory.decodeByteArray(bytes, 0, bytes.length)));
        //设置内容
        portrait.setImageDrawable(new BitmapDrawable((Bitmap) bundle.get("portrait")));
        categoryImage.setImageDrawable(new BitmapDrawable((Bitmap) bundle.get("categoryImage")));
        String categoryStr = (String) bundle.get("category");
        String costType = categoryStr.contains("-")?categoryStr.split("-")[0]:categoryStr;
        String remarkStr = categoryStr.contains("-")?categoryStr.split("-")[1]:"";
        remark.setText(remarkStr);
        category.setText(costType);
        category.setTextColor(costType.equals("收入")? Color.RED:Color.GREEN);
        money.setText(bundle.getString("money"));
        time.setText(bundle.getString("time"));
        payway.setText(bundle.getString("payway"));
    }
}
