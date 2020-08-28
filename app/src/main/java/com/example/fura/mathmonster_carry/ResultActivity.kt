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

        var timer_minute = 0L
        var timer_second = 0L
        /******本文******/


        for(i in 1..cleartime_from_Break){
            if(i % 60L  == 0L){
                timer_minute ++
            }
            timer_second = cleartime_from_Break - ( timer_minute * 60 )
        }

        count_false2.text = falseanswer_from_Break.toString()
        count_time2.text = timer_minute.toString() + "ふん" + timer_second.toString() + "びょう" + cleartime_from_Break.toString()


        titleBtn.setOnClickListener {
            startActivity(intent_to_Main)
        }

        levBtn.setOnClickListener {
            intent_to_Level.putExtra("LevelCalc_Result_Level", titletext_from_Break)
            startActivity(intent_to_Level)
        }

    }
}
