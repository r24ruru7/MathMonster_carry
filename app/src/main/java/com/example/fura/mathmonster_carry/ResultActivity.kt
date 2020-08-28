package com.example.fura.mathmonster_carry

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_result.*

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        /*intent受け取り*/
        val titletext_from_Break = intent.getIntExtra("LevelCalc_Break_Result", 0)              //BreakActivityから(足し算or引き算、レベルミックス)
        val falseanswer_from_Break = intent.getIntExtra("FalseAns_Break_Result", 0)             //BreakActivityからまちがい数
        val cleartime_from_Break = intent.getLongExtra("ClearTime_Break_Result", 0)              //BreakActivityから解答時間

        /*intent渡し*/
        val intent_to_Main = Intent(this, MainActivity::class.java)                        //MainAtivityに遷移
        val intent_to_Level = Intent(this, LevelActivity::class.java)                        //LevelAtivityに(足し算or引き算)

        var timer_minute = 0L   //時間計測(〇分)
        var timer_second = 0L   //時間計測(〇秒)
        /******本文******/


        for(i in 1..cleartime_from_Break){      //かかった時間を〇分〇秒に分ける
            if(i % 60L  == 0L){                        //６０秒カウントしたらtimer_minuteに＋１
                timer_minute ++
            }
            timer_second = cleartime_from_Break - ( timer_minute * 60 )     //x分だけかかった時間引くことで残りの秒数をだす
        }

        count_false2.text = falseanswer_from_Break.toString()                                       //間違った数を表示
        count_time2.text = timer_minute.toString() + "ふん" + timer_second.toString() + "びょう"   //かかった時間を表示


        titleBtn.setOnClickListener {
            startActivity(intent_to_Main)
        }

        levBtn.setOnClickListener {
            intent_to_Level.putExtra("LevelCalc_Result_Level", titletext_from_Break)
            startActivity(intent_to_Level)
        }

    }
}
