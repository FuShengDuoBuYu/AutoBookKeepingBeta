package com.beta.autobookkeeping.activity.familyTodo;

import static android.view.animation.Animation.RELATIVE_TO_SELF;
import static Util.ConstVariable.IP;
import static Util.ImageUtil.base642bitmap;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.beta.autobookkeeping.R;
import com.beta.autobookkeeping.activity.familyTodo.Entity.TodoItem;
import com.hss01248.dialog.StyledDialog;
import com.hss01248.dialog.adapter.SuperLvHolder;
import com.hss01248.dialog.interfaces.MyDialogListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Util.SpUtils;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FamilyTodoActivity extends AppCompatActivity {

    private TextView tvUndoItem,tvDoneItem;
    private RecyclerView rvFamilyUndo,rvFamilyDone;
    private ImageView ivAddTodo,ivUndoItem,ivDoneItem;
    private JSONArray familyMembers;
    private Integer chooseFamilyMemberIndex = -1;
    private LinearLayout llUndoItem,llDoneItem;
    private List<TodoItem> undoItems = new ArrayList<>();
    private List<TodoItem> doneItems = new ArrayList<>();

    //顺时针旋转90度的动画
    //逆时针旋转90度的动画
    private RotateAnimation animatorAntiClockwise90 = new RotateAnimation(0,-90, RELATIVE_TO_SELF,0.5f,RELATIVE_TO_SELF,0.5f);

    private final View.OnClickListener addTodoListener = v -> {
        final EditText[] etItemTitle = new EditText[1];
        final LinearLayout[] llFamilyMemberPortraits = new LinearLayout[1];
        Integer familyMemberIndex = null;
        StyledDialog.buildIosAlert("添加待办事项", "添加待办事项", new MyDialogListener() {
            @Override
            public void onFirst() {
                addTodoItem(etItemTitle[0].getText().toString());
            }
            @Override
            public void onSecond() {}
        }).setCustomContentHolder(new SuperLvHolder(this) {
            @Override
            protected void findViews() {
                etItemTitle[0] = rootView.findViewById(R.id.et_item_title);
                llFamilyMemberPortraits[0] = rootView.findViewById(R.id.ll_family_member_portraits);
                llFamilyMemberPortraits[0].addView(getFamilyMemberInfo(familyMemberIndex));
            }
            @Override
            protected int setLayoutRes() {
                return R.layout.item_activity_family_todo_add_todo_dialog;
            }
            @Override
            public void assingDatasAndEvents(Context context, @Nullable Object bean) {}
        }).show();
    };
    private final View.OnClickListener llUndoItemListener = v -> {
        if(rvFamilyUndo.getVisibility()==View.VISIBLE){
            rvFamilyUndo.setVisibility(View.GONE);
            RotateAnimation animator = new RotateAnimation(0,0, RELATIVE_TO_SELF,0.5f,RELATIVE_TO_SELF,0.5f);
            animator.setDuration(500);
            animator.setFillAfter(true);
            ivUndoItem.startAnimation(animatorAntiClockwise90);
        }
        else{
            rvFamilyUndo.setVisibility(View.VISIBLE);
            RotateAnimation animator = new RotateAnimation(0,-90, RELATIVE_TO_SELF,0.5f,RELATIVE_TO_SELF,0.5f);
            animator.setDuration(500);
            animator.setFillAfter(true);
            ivUndoItem.startAnimation(animator);
        }
    };
    private final View.OnClickListener llDoneItemListener = v -> {
        if(rvFamilyDone.getVisibility()==View.VISIBLE){
            rvFamilyDone.setVisibility(View.GONE);
            RotateAnimation animator = new RotateAnimation(0,0, RELATIVE_TO_SELF,0.5f,RELATIVE_TO_SELF,0.5f);
            animator.setDuration(500);
            animator.setFillAfter(true);
            ivDoneItem.startAnimation(animatorAntiClockwise90);
        }
        else{
            rvFamilyDone.setVisibility(View.VISIBLE);
            RotateAnimation animator = new RotateAnimation(0,90, RELATIVE_TO_SELF,0.5f,RELATIVE_TO_SELF,0.5f);
            animator.setDuration(500);
            animator.setFillAfter(true);
            ivDoneItem.startAnimation(animator);
        }
    };
    Thread findFamilyMemberThread = new Thread(() -> {
            String familyId = (String) SpUtils.get(FamilyTodoActivity.this,"familyId","");
            String url = IP+"/user/getFamilyMembers/"+familyId;
            OkHttpClient client = new OkHttpClient();
            JSONObject jsonObject = new JSONObject();
            Request request = new Request.Builder().url(url).get().build();
            try{
                Response response = client.newCall(request).execute();
                JSONObject jsonResponse = new JSONObject(response.body().string());
                if(jsonResponse.getBoolean("success")){
                    familyMembers = jsonResponse.getJSONArray("data");
                }
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_todo);
        findViewByIdAndInit();
        findFamilyMemberThread.start();
        findFamilyTodoItem();
    }

    private void findViewByIdAndInit() {
        rvFamilyUndo = findViewById(R.id.rv_family_undo_list);
        rvFamilyDone = findViewById(R.id.rv_family_done_list);
        ivAddTodo = findViewById(R.id.iv_add_order);
        llUndoItem = findViewById(R.id.ll_undo_item);
        llDoneItem = findViewById(R.id.ll_done_item);
        tvUndoItem = findViewById(R.id.tv_undo_item);
        tvDoneItem = findViewById(R.id.tv_done_item);
        ivAddTodo.setOnClickListener(addTodoListener);
        ivDoneItem = findViewById(R.id.iv_done_item);
        ivUndoItem = findViewById(R.id.iv_undo_item);
    }

    private void addTodoItem(String itemTitle){
        StyledDialog.buildLoading().show();
        new Thread(()->{
            try{
                String url = IP+"/todoItem/addTodoItem";
                OkHttpClient client = new OkHttpClient();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("itemTitle",itemTitle);
                jsonObject.put("familyId",SpUtils.get(this,"familyId",""));
                jsonObject.put("posterId",SpUtils.get(this,"phoneNum",""));
                jsonObject.put("posterPortrait",SpUtils.get(this,"portrait",""));
                jsonObject.put("handlerId",familyMembers.getJSONObject(chooseFamilyMemberIndex).getString("phoneNum"));
                jsonObject.put("handlerPortrait",familyMembers.getJSONObject(chooseFamilyMemberIndex).getString("portrait"));
                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"),jsonObject.toString());
                Request request = new Request.Builder().url(url).post(requestBody).build();
                Response response = client.newCall(request).execute();
                JSONObject jsonResponse = new JSONObject(response.body().string());
                if(jsonResponse.getBoolean("success")){
                    runOnUiThread(()->{
                        StyledDialog.dismissLoading(FamilyTodoActivity.this);
                        Toast.makeText(this,"添加成功",Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private LinearLayout getFamilyMemberInfo(Integer familyMemberIndex){
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        for (int i = 0; i < familyMembers.length(); i++) {
            LinearLayout linearLayoutItem = (LinearLayout) LinearLayout.inflate(this,R.layout.item_activity_personal_info_member_family_item,null);
            ImageView ivFamilyMemberPortrait = linearLayoutItem.findViewById(R.id.iv_family_member_portrait);
            TextView tvFamilyMemberIdentityAndNickname = linearLayoutItem.findViewById(R.id.tv_family_member_identity_and_nickname);
            TextView tvFamilyMemberPhoneNum = linearLayoutItem.findViewById(R.id.tv_family_member_phoneNum);
            //设置原始头像/自定义头像
            try {
                if(familyMembers.getJSONObject(i).getString("portrait")==null||"".equals(familyMembers.getJSONObject(i).getString("portrait"))){
                    ivFamilyMemberPortrait.setBackground(this.getDrawable(R.drawable.ic_portrait));
                }
                else{
                    ivFamilyMemberPortrait.setBackground(new BitmapDrawable(base642bitmap(familyMembers.getJSONObject(i).getString("portrait"))));
                }
                tvFamilyMemberIdentityAndNickname.setText(familyMembers.getJSONObject(i).getString("familyIdentity")+"·"+familyMembers.getJSONObject(i).getString("nickname"));
                tvFamilyMemberPhoneNum.setText(familyMembers.getJSONObject(i).getString("phoneNum"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            int finalI = i;
            linearLayoutItem.setOnClickListener(v->{
                chooseFamilyMemberIndex = finalI;
                //将背景设置为选中状态
                for (int j = 0; j < linearLayout.getChildCount(); j++) {
                    linearLayout.getChildAt(finalI).setBackgroundColor(FamilyTodoActivity.this.getColor(R.color.item_background));
                    //其他的白色
                    if(j!=finalI){
                        linearLayout.getChildAt(j).setBackgroundColor(FamilyTodoActivity.this.getColor(R.color.white));
                    }
                }
            });
            linearLayout.addView(linearLayoutItem);
        }
        return linearLayout;
    }


    public void findFamilyTodoItem(){
        new Thread(()->{
            StyledDialog.buildLoading().show();
            //清空item的数据
            undoItems.clear();
            doneItems.clear();
            String familyId = (String) SpUtils.get(FamilyTodoActivity.this,"familyId","");
            String url = IP+"/todoItem/getFamilyTodoItem/"+familyId;
            OkHttpClient client = new OkHttpClient();
            JSONObject jsonObject = new JSONObject();
            Request request = new Request.Builder().url(url).get().build();
            try{
                Response response = client.newCall(request).execute();
                JSONObject jsonResponse = new JSONObject(response.body().string());
                if(jsonResponse.getBoolean("success")){
                    JSONArray familyTodoList = jsonResponse.getJSONArray("data");
                    for(int i=0;i<familyTodoList.length();i++){
                        JSONObject familyTodoItem = familyTodoList.getJSONObject(i);
                        String itemTitle = familyTodoItem.getString("itemTitle");
                        String postTime = familyTodoItem.getString("postTime");
                        String handleTime = familyTodoItem.getString("handleTime");
                        Drawable posterPortrait = new BitmapDrawable(base642bitmap(familyTodoItem.getString("posterPortrait")));
                        Drawable handlerPortrait = new BitmapDrawable(base642bitmap(familyTodoItem.getString("handlerPortrait")));
                        TodoItem todoItem = new TodoItem(familyTodoItem.getInt("id"),itemTitle,postTime,handleTime,posterPortrait,handlerPortrait);
                        //设置待办事项状态
                        if (familyTodoItem.getBoolean("isFinished")) {
                            doneItems.add(todoItem);
                        } else {
                            undoItems.add(todoItem);
                        }
                    }
                    runOnUiThread(()->{
                        LinearLayoutManager undoLayoutManager = new LinearLayoutManager(this);
                        LinearLayoutManager doneLayoutManager = new LinearLayoutManager(this);
                        rvFamilyUndo.setAdapter(new FamilyTodoRecycleViewAdapter(undoItems,FamilyTodoActivity.this,getDrawable(R.drawable.ic_block)));
                        llUndoItem.setOnClickListener(llUndoItemListener);
                        llDoneItem.setOnClickListener(llDoneItemListener);
                        rvFamilyDone.setAdapter(new FamilyTodoRecycleViewAdapter(doneItems,FamilyTodoActivity.this,getDrawable(R.drawable.ic_fill_block)));
                        rvFamilyDone.setLayoutManager(undoLayoutManager);
                        rvFamilyUndo.setLayoutManager(doneLayoutManager);
                        rvFamilyDone.setVisibility(View.GONE);
                        tvUndoItem.setText("未完成 "+undoItems.size());
                        tvDoneItem.setText("已完成 "+doneItems.size());
                        StyledDialog.dismissLoading(this);
                    });
                }
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}