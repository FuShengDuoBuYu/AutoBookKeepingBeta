package com.rainlife

import Util.Util
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rainlife.autobookkeeping.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    @SuppressLint("ResourceAsColor", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        date.text = Util.getCurrentMonth().toString()+" / "+Util.getCurrentDay().toString()
        time_and_week.text=Util.getCurrentHour().toString()+":"+Util.getCurrentMinute()+" / 周三"
    }


}

