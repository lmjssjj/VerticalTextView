package com.lmjssjj.verticaltextview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {
    var verticalTextView: VerticalTextView? = null;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.lmjssjj.verticaltextview.demo.R.layout.activity_main)
        verticalTextView = findViewById(com.lmjssjj.verticaltextview.demo.R.id.vtv)
    }

    fun click(view: View) {
//        verticalTextView?.setText("123456789")
//        verticalTextView?.setTextColor(getColor(com.lmjssjj.verticaltextview.demo.R.color.purple_200))
//        verticalTextView?.setRowSpacing(50)
//        verticalTextView?.setColumnSpacing(50)
        verticalTextView?.setTextSize(150)
    }
}