package com.example.fura.mathmonster_carry

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_level.*

class LevelActivity : AppCompatActivity() {

    val TitleTexts = arrayOf("たしざん", "ひきざん")                                        //上に表示するタイトルの配列

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_level)

        /*ここからintent受け取り*/
        val calculation_from_Main = intent.getIntExtra("Cal_Main_Level", 0)                   //MainActivityから(足し算or引き算)
        val calculation_from_Math = intent.getIntExtra("LevelCalc_Math_Level", 0)            //MathActivityから(足し算or引き算)
        val calculation_from_Break = intent.getIntExtra("LevelCalc_Break_Level", 0)          //BreakActivityから(足し算or引き算)
        val calculation_from_Result = intent.getIntExtra("LevelCalc_Result_Level", 0)        //ResultActivityから(足し算or引き算)


        /*ここからintent渡し*/
        val intent_to_Monster = Intent(this, MonsterActivity::class.java)                               //選択したレベルをMonsterActivityに


        /*ここからintent結合*/
        var title_text = 0          //複数のintentを一つにまとめる
        title_text = Intent_Judgment(calculation_from_Main, calculation_from_Math, calculation_from_Break, calculation_from_Result)

        /******本文******/
        var select_calculation = 0                                                           //足し算(=0),引き算(=6)で判断
        var select_total = 0                                                                 //level + calculation　で足し引きとレベルを判断

        if(title_text <= 6 ) select_calculation = 0                                         //足し算を選んだ
        else select_calculation = 6                                                         //引き算を選んだ

        choice_math.text = TitleTexts[ select_calculation % 5 ]                            //画面上のテキストを(足し算or引き算)に変える

        /*ここからボタン入力*/
        backBtn.setOnClickListener{                                                         //戻るボタン
            val back_to_Main = Intent(this, MainActivity::class.java)      //行先はMainActivity
            startActivity(back_to_Main)
        }

        lev1.setOnClickListener {                                                           //レベル1のボタン
            select_total = Check_Levels(1,select_calculation)                   //足し算or引き算、レベルを判断する関数へ(Check_levels)
            intent_to_Monster.putExtra("LevelCalc_Level_Monster", select_total)            //足し算レベル1( = 1 ) 引き算レベル1( = 7 )
            startActivity(intent_to_Monster)
        }

        lev2.setOnClickListener {                                                           //レベル2のボタン
            select_total = Check_Levels(2,select_calculation)                   //足し算or引き算、レベルを判断する関数へ(Check_levels)
            intent_to_Monster.putExtra("LevelCalc_Level_Monster", select_total)            //足し算レベル2( = 2 ) 引き算レベル2( = 8 )
            startActivity(intent_to_Monster)
        }

        lev3.setOnClickListener {                                                           //レベル3のボタン
            select_total = Check_Levels(3,select_calculation)                   //足し算or引き算、レベルを判断する関数へ(Check_levels)
            intent_to_Monster.putExtra("LevelCalc_Level_Monster", select_total)            //足し算レベル3( = 3 ) 引き算レベル3( = 9 )
            startActivity(intent_to_Monster)
        }

        lev4.setOnClickListener {                                                           //レベル4のボタン
            select_total = Check_Levels(4,select_calculation)                   //足し算or引き算、レベルを判断する関数へ(Check_levels)
            intent_to_Monster.putExtra("LevelCalc_Level_Monster", select_total)            //足し算レベル4( = 4 ) 引き算レベル4( = 10 )
            startActivity(intent_to_Monster)
        }

        lev5.setOnClickListener {                                                           //レベル5のボタン
            select_total = Check_Levels(5,select_calculation)                   //足し算or引き算、レベルを判断する関数へ(Check_levels)
            intent_to_Monster.putExtra("LevelCalc_Level_Monster", select_total)            //足し算レベル5( = 5 ) 引き算レベル5( = 11 )
            startActivity(intent_to_Monster)
        }

        levmix.setOnClickListener {                                                         //レベルミックスのボタン
            select_total = Check_Levels(6,select_calculation)                   //足し算or引き算、レベルを判断する関数へ(Check_levels)
            intent_to_Monster.putExtra("LevelCalc_Level_Monster", select_total)            //足し算レベルミックス( = 6 ) 引き算レベルミックス( = 12 )
            startActivity(intent_to_Monster)
        }
        /*ここまでボタン入力*/
    }

    fun Check_Levels(select_level:Int, select_calculation:Int):Int{                         //足し算と引き算、選択したレベルを区別する関数
        var select_total = select_level + select_calculation                           //足し算は(1～6),引き算は(7～12)になる
        return select_total
    }

    fun Intent_Judgment(a : Int, b : Int, c : Int, d : Int):Int{                            //別ページから受け取った数字をひとまとめにする
        var choice_calc = a + b + c + d                                                //基本は0 + 0 + x + 0の形になる
        return choice_calc
    }
}



