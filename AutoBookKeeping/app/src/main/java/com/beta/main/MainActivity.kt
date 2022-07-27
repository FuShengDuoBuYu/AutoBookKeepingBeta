package com.rainlife.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rainlife.autobookkeeping.R
import kotlinx.android.synthetic.main.activity_main.*

//import kotlinx.android.synthetic.main.activity_main
class MainActivity : AppCompatActivity() {
//    private val button by lazy {
//        findViewById(R.id.test_button)
//    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        test_button.setOnClickListener {
//            //跳转到设置界面
//            val intent = Intent(this@MainActivity, MainActivity::class.java)
//            startActivity(intent)
//        }
        main_title.setTitle("雨生活")
        main_title.setSubTitle("欢迎你")
}

}