package com.beta.autobookkeeping.activity.orderItemSearch.items.otherDescriptionView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

import com.beta.autobookkeeping.R;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

public class GridViewAdapter extends BaseAdapter {

    private String[] types;
    private Context context;

    public GridViewAdapter(Context context, String[] types) {
        this.context = context;
        this.types = types;
    }
    @Override
    public int getCount() {
        return types.length;
    }

    @Override
    public Object getItem(int position) {
        return types[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout ll_button = (LinearLayout) View.inflate(context,R.layout.item_round_button,null);
        QMUIRoundButton button = ll_button.findViewById(R.id.btn_round);
//        button.setBackgroundColor(context.getResources().getColor(R.color.item_background));
//        button.setTextColor(context.getResources().getColor(R.color.primary_font));
        button.setText(types[position]);
        return ll_button;
    }
}
