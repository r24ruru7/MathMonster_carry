package com.example.fura.mathmonster_carry

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_monster.*

class MonsterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monster)

        /*intent受け取り*/
        val titletext_from_Level = intent.getIntExtra("Level_and_Calc", 0)             //LevelActivityから(足し算or引き算、選択レベル)

        /*intent渡し*/


        /******本文******/
        when(titletext_from_Level){
            1, 7 -> leveltext.text = "レベル1"
            2, 8 -> leveltext.text = "レベル2"
            3, 9 -> leveltext.text = "レベル3"
            4, 10 -> leveltext.text = "レベル4"
            5, 11 -> leveltext.text = "レベル5"
            6, 12 -> leveltext.text = "ミックスだよ"
            //test
        }
    }
}
