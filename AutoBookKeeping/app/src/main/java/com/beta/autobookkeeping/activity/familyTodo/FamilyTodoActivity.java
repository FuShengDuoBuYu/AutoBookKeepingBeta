package com.beta.autobookkeeping.activity.familyTodo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.beta.autobookkeeping.R;

import java.util.ArrayList;
import java.util.List;

public class FamilyTodoActivity extends AppCompatActivity {

    RecyclerView rvFamilyTodo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_todo);
        findViewByIdAndInit();
    }

    private void findViewByIdAndInit() {
        rvFamilyTodo = findViewById(R.id.rv_family_todo_list);
        List<String> strings = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            strings.add("测试数据" + i);
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvFamilyTodo.setAdapter(new FamilyTodoRecycleViewAdapter(strings));
        rvFamilyTodo.setLayoutManager(layoutManager);
    }
}