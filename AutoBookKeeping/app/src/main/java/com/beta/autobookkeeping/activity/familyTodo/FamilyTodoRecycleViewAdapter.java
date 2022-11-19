package com.beta.autobookkeeping.activity.familyTodo;

import static androidx.appcompat.content.res.AppCompatResources.getDrawable;
import static Util.ConstVariable.IP;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Vibrator;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.beta.autobookkeeping.R;
import com.beta.autobookkeeping.activity.familyTodo.Entity.TodoItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FamilyTodoRecycleViewAdapter extends RecyclerView.Adapter<FamilyTodoRecycleViewAdapter.InnerHolder> {
    private List<TodoItem> todoItems;
    private Context context;
    private Activity activity;
    private Drawable statusIcon;
    @NonNull
    @Override
    public FamilyTodoRecycleViewAdapter.InnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity_family_todo,parent,false);
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(
                android.R.attr.selectableItemBackground, outValue, true);
        view.setForeground(getDrawable(context, outValue.resourceId));
        return new InnerHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FamilyTodoRecycleViewAdapter.InnerHolder holder, int position) {
        holder.tvItemTitle.setText(todoItems.get(position).getTitle());
        holder.ivConfirmBtn.setImageDrawable(statusIcon);
        holder.tvPostItemTime.setText("↑ "+todoItems.get(position).getPostTime());
        holder.tvDoneItemTime.setText(todoItems.get(position).getHandleTime().equals("")?"":"√ "+todoItems.get(position).getHandleTime());
        holder.ivPosterPortrait.setImageDrawable(todoItems.get(position).getPosterPortrait());
        holder.ivHandlerPortrait.setImageDrawable(todoItems.get(position).getHandlerPortrait());
        holder.todoItemId = todoItems.get(position).getId();
        holder.ivConfirmBtn.setOnClickListener(v -> {
            if(holder.tvDoneItemTime.getText().toString().equals("")){
                finishTodoItem(holder.todoItemId);
            }
            else {
                Toast.makeText(context,"该事项已完成",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return todoItems.size();
    }

    public FamilyTodoRecycleViewAdapter(List<TodoItem> todoItems,Context context,Drawable statusIcon) {
        this.todoItems = todoItems;
        this.context = context;
        this.activity = (Activity) context;
        this.statusIcon = statusIcon;
    }

    public class InnerHolder extends RecyclerView.ViewHolder {
        TextView tvItemTitle,tvPostItemTime,tvDoneItemTime;
        ImageView ivPosterPortrait,ivHandlerPortrait,ivConfirmBtn;
        Integer todoItemId;
        public InnerHolder(@NonNull View itemView) {
            super(itemView);
            tvItemTitle = itemView.findViewById(R.id.tv_item_title);
            tvPostItemTime = itemView.findViewById(R.id.tv_post_item_time);
            ivConfirmBtn = itemView.findViewById(R.id.iv_confirm_btn);
            tvDoneItemTime = itemView.findViewById(R.id.tv_done_item_time);
            ivPosterPortrait = itemView.findViewById(R.id.iv_poster_portrait);
            ivHandlerPortrait = itemView.findViewById(R.id.iv_handler_portrait);
        }
    }

    private void finishTodoItem(Integer itemId){
        new Thread(() -> {
            String url = IP+"/todoItem/finishTodoItem/"+itemId;
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).get().build();
            try{
                Response response = client.newCall(request).execute();
                JSONObject jsonResponse = new JSONObject(response.body().string());
                if(jsonResponse.getBoolean("success")){
                    activity.runOnUiThread(()->{
                        Toast.makeText(context,"已完成",Toast.LENGTH_SHORT).show();
                        FamilyTodoActivity familyTodoActivity = (FamilyTodoActivity) context;
                        familyTodoActivity.findFamilyTodoItem();
                        //振动
                        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                        if (vibrator.hasVibrator()) {
                            long[] pattern = { 10L, 60L }; // An array of longs of times for which to turn the vibrator on or off.
                            vibrator.vibrate(pattern, -1); // The index into pattern at which to repeat, or -1 if you don't want to repeat.
                        }
                    });
               }
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}