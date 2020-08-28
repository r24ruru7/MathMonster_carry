package com.example.fura.mathmonster_carry

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_math.*

class MathActivity : AppCompatActivity() {

    val Titles = arrayOf("レベル1", "レベル2", "レベル3", "レベル4", "レベル5", "レベル6",
        "レベル1", "レベル2", "レベル3", "レベル4", "レベル5", "レベル6")                                                                         //タイトルを格納する配列、レベル6は使用しないが数の調整のため

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_math)


        /*intent受け取り*/
        val titletext_from_Monster = intent.getIntExtra("LevelCalc_Monster_Math", 0)      //MonsterActivityから(足し算or引き算、選択レベル)
        val nowlevel_from_Monster = intent.getIntExtra("NowLevel_Monster_Math", 0)        //MonsterActivityからレベルミックス時の現在のレベル
        val timerpast_from_Monster = intent.getLongExtra("ClearTime_Monster_Math", 0)   //MonsterActivityからレベルミックス時の計測時間

        /*intent渡し*/
        val intent_to_Break = Intent(this, BreakActivity::class.java)                        //BreakAtivityに(足し算or引き算、選択レベル)
        val intent_to_Level = Intent(this, LevelActivity::class.java)                        //LevelAtivityに(足し算or引き算、選択レベル)

        count_time2.text = timerpast_from_Monster.toString()

        val timer_start = System.currentTimeMillis()  //レベルミックスの時間計測用

        /******本文******/




        lev_Btn.text = Titles[ LevelMix_Judgment(titletext_from_Monster, nowlevel_from_Monster) ]
        lev_Btn.setOnClickListener {
            intent_to_Break.putExtra("LevelCalc_Math_Break", titletext_from_Monster)
            intent_to_Break.putExtra("NowLevel_Math_Break", nowlevel_from_Monster)
            intent_to_Break.putExtra("ClearTime_Math_Break", Time_fun(timer_start, timerpast_from_Monster))
            startActivity(intent_to_Break)
        }

        backBTN.setOnClickListener{
            intent_to_Level.putExtra("LevelCalc_Math_Level", titletext_from_Monster)                //戻るボタンを押すとレベル選択に戻る
            startActivity(intent_to_Level)
        }
    }

    //レベルミックス時に何レベルをやってるか判断する関数
    private fun LevelMix_Judgment(titletext_from_monster : Int, nowlevel_from_monster : Int):Int{            //別ページから受け取った数字をひとまとめにする

        var answer_titletext = 0                                   //まとめた値を入れる
        when(titletext_from_monster){
            0, 6, 12 -> {
                answer_titletext = nowlevel_from_monster            //レベルミックス時の現在のレベルを判断
            }
            else -> {
                answer_titletext = titletext_from_monster - 1      //選択したレベルになる
            }
        }
        return answer_titletext
    }

    /*****     レベルミックス用の時間処理     *****/
    private fun Time_fun(t_start: Long, t_past: Long):Long{
        val t_end = System.currentTimeMillis()          //終了時刻を計測
        var t_total = (t_end - t_start)/1000            //終了時刻 - 開始時刻でかかった時間がわかる
        t_total += t_past                                      //これまでのレベル分の時間を足す
        return t_total
    }
}
