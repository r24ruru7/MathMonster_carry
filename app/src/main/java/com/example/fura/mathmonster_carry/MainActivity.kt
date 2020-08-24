package com.example.fura.mathmonster_carry

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent_to_Level = Intent(this, LevelActivity::class.java)   //画面遷移変数(このソースから次のソースに)

        /*ここからボタン入力*/
        plusbtn.setOnClickListener{                            //足し算ボタンを押したとき
            intent_to_Level.putExtra("Cal", "たしざん")   //intentにputExtra(name:キー, value:渡す値)を入れる
            startActivity(intent_to_Level)                              //intentを実行
        }

        minusbtn.setOnClickListener{                           //引き算ボタンを押したとき
            intent_to_Level.putExtra("Cal", "ひきざん")   //intentにputExtra(name:キー, value:渡す値)を入れる
            startActivity(intent_to_Level)                               //intentを実行
        }

    }
}