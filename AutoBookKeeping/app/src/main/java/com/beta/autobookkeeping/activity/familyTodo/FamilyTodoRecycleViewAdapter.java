package com.beta.autobookkeeping.activity.familyTodo;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.beta.autobookkeeping.R;

import java.util.List;

public class FamilyTodoRecycleViewAdapter extends RecyclerView.Adapter<FamilyTodoRecycleViewAdapter.InnerHolder> {
    private List<String> strings;

    @NonNull
    @Override
    public FamilyTodoRecycleViewAdapter.InnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.item_activity_family_todo, null);
        //view设置波纹效果
        return new InnerHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FamilyTodoRecycleViewAdapter.InnerHolder holder, int position) {
        holder.textView.setText(strings.get(position));
        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(holder.itemView.getContext(), "点击了" + position, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return strings.size();
    }

    public FamilyTodoRecycleViewAdapter(List<String> strings) {
        this.strings = strings;
    }

    public class InnerHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public InnerHolder(@NonNull View itemView) {
            super(itemView);
//            textView = itemView.findViewById(R.id.t1);
        }
    }
}