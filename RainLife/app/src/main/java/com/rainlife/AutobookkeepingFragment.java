package com.rainlife;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rainlife.autobookkeeping.MainActivity;
import com.rainlife.autobookkeeping.OrderDetailActivity;
import com.rainlife.autobookkeeping.R;

import Util.Util;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AutobookkeepingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AutobookkeepingFragment extends Fragment {
    LinearLayout autoBookKeepingPanel = null;
    ImageView addForm = null;
    TextView dayCost = null;
    TextView monthCost = null;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AutobookkeepingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AutobookkeepingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AutobookkeepingFragment newInstance(String param1, String param2) {
        AutobookkeepingFragment fragment = new AutobookkeepingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public void onStart() {
        getComponentsAndSetEvent();
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_autobookkeeping, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getComponentsAndSetEvent();
    }

    //获取组件并设置点击事件
    void getComponentsAndSetEvent(){
        gerPanelAndSetEvent();
        getAddFormBtnAndSetEvent();
        getDayCostAndSetValue();
        getMonthCostAndSetValue();
    }

    //获取面板
    void gerPanelAndSetEvent(){
        autoBookKeepingPanel = getActivity().findViewById(R.id.auto_book_keeping_panel);
        autoBookKeepingPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
            }
        });
    }

    //获取添加账单按钮
    void getAddFormBtnAndSetEvent(){
        addForm = getActivity().findViewById(R.id.add_form);
        addForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), OrderDetailActivity.class);
                startActivity(intent);
            }
        });
    }

    //获取显示的日累计
    void getDayCostAndSetValue(){
        dayCost = getActivity().findViewById(R.id.day_cost);
        Double dayMoney = Util.getTodayMoney(getContext());
        dayCost.setTextColor(dayMoney>=0? Color.RED:Color.GREEN);
        dayCost.setText(String.format("¥%s", dayMoney));
    }

    //获取显示的月累计
    void getMonthCostAndSetValue(){
        monthCost = getActivity().findViewById(R.id.month_cost);
        Double monthMoney = Util.getMonthMoney(getContext());
        monthCost.setTextColor(monthMoney>=0? Color.RED:Color.GREEN);
        monthCost.setText(String.format("¥%s", monthMoney));
    }
}