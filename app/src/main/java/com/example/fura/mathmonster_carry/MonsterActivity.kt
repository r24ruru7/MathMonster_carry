package com.example.fura.mathmonster_carry

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import kotlinx.android.synthetic.main.activity_monster.*

class MonsterActivity : AppCompatActivity() {

    var fadeAnimation = 0   //「がめんにふれてね」 がフェードインしたか確認フラグ(モンスターのアニメーションが始まったら１になる)
    val Titles = arrayOf("レベル1", "レベル2", "レベル3", "レベル4", "レベル5", "レベル6",
        "レベル1", "レベル2", "レベル3", "レベル4", "レベル5", "レベル6")                                                                         //タイトルを格納する配列、レベル6は使用しないが数の調整のため
    val Picc = arrayOf(R.drawable.monster1, R.drawable.monster2, R.drawable.monster3, R.drawable.monster4, R.drawable.monster5, R.drawable.circle1,
        R.drawable.monster6, R.drawable.monster7, R.drawable.monster8, R.drawable.monster9, R.drawable.monster10, R.drawable.circle1)                   //モンスター画像の配列、ミックス(titletext_from_Level = 6, 12)には適当な画像を入れてる

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monster)

        /*intent受け取り*/
        val titletext_from_Level = intent.getIntExtra("LevelCalc_Level_Monster", 0)      //LevelActivityから(足し算or引き算、選択レベル)
        val titletext_from_Break = intent.getIntExtra("LevelCalc_Break_Monster",0)       //BreakActivityから(足し算or引き算、選択レベル)
        val nowlevel_from_Break = intent.getIntExtra("NowLevel_Break_Monster",0)       //BreakActivityから(レベルミックス時の現在のレベル)
        val falsecount_from_Break = intent.getIntExtra("FalseAns_Break_Monster", 0)     //BreakActivityからレベルミックス時の間違えた数
        val timerpast_from_Break = intent.getLongExtra("ClearTime_Break_Monster", 0)    //BreakActivityから(レベルミックス時の計測時間)

        /*intent渡し*/
        val intent_to_Math = Intent(this, MathActivity::class.java)                        //MathAtivityに(足し算or引き算、選択レベル)


        /******本文******/

        //複数のintentをまとめる関数を使用
        leveltext.text = Titles[ LevelMix_Judgment(titletext_from_Level, titletext_from_Break, nowlevel_from_Break) ]           //タイトルテキストを変更
        subimage.setImageResource(Picc[ LevelMix_Judgment(titletext_from_Level, titletext_from_Break, nowlevel_from_Break) ])   //モンスター画像を変更

        PictureAnimation()                                                                                  //モンスター画像のアニメーションを実行
        fadein()                                                                                            //テキストをフェードインさせて、フラグを立てる

        textView2.text = timerpast_from_Break.toString()

        //画面タッチ後、次の画面に遷移する
        ground0.setOnClickListener{
            if(titletext_from_Break == 6 || titletext_from_Break == 12){  //レベルミックスの時
                when(fadeAnimation){
                    0 -> {      }
                    1 -> {      //フラグが立っているとき
                        intent_to_Math.putExtra("LevelCalc_Monster_Math", titletext_from_Break)  //足し算or引き算、選択したレベルを渡す
                        intent_to_Math.putExtra("NowLevel_Monster_Math", nowlevel_from_Break)    //レベルミックス時の現在のレベルを渡す
                        intent_to_Math.putExtra("FalseAns_Monster_Math", falsecount_from_Break) //レベルミックス時の間違えた数を渡す
                        intent_to_Math.putExtra("ClearTime_Monster_Math", timerpast_from_Break)  //レベルミックス時の計測時間を渡す
                        startActivity(intent_to_Math)
                    }
                }
            }
            else{                                                       //レベル1～5の時
                when(fadeAnimation){
                    0 -> {      }
                    1 -> {      //フラグが立っているとき
                        intent_to_Math.putExtra("LevelCalc_Monster_Math", titletext_from_Level)                  //足し算or引き算、レベルを渡す
                        startActivity(intent_to_Math)
                    }
                }
            }
        }
    }

    //レベルミックス時に何レベルをやってるか判断する関数
    private fun LevelMix_Judgment(titletext_from_level : Int, titletext_from_break : Int, nowlevel_from_break : Int):Int{    //別ページから受け取った数字をひとまとめにする

        var answer_titletext = 0                                                                               //まとめた値を入れる
        when(titletext_from_level){
            0, 6, 12 -> {
                answer_titletext = titletext_from_level + titletext_from_break - 6 + nowlevel_from_break        //レベルミックスの時の現在のレベルの判断
            }
            else -> {
                answer_titletext = titletext_from_level - 1                                                     //選択したレベル
            }
        }
        return answer_titletext
    }

    //フェードインの処理関数
    private fun fadein(): Int {
        val alphaFadeout = AlphaAnimation(0.0f, 1.0f)                   // 透明度を1から0に変化
        alphaFadeout.duration = 2000                                    // animation時間 msec
        alphaFadeout.fillAfter = true                                  // animationが終わったそのまま表示にする
        touchtext.startAnimation(alphaFadeout)
        return 1                                                       //フェードインフラグ用に1を返す
    }

    //モンスター画像のアニメーション関数
    private fun PictureAnimation(){

        val Anime1 = RotateAnimation(                                   /*画像を右に15度傾けるアニメーション*/
            0f,                                            //初期位置 = 0度
            15f,                                             //停止位置 = 15度
            Animation.RELATIVE_TO_SELF, 0.5f,               //回転軸(x軸)
            Animation.RELATIVE_TO_SELF, 0.5f                //回転軸(y軸)
        )
        Anime1.duration = 500                                           // 0.5秒かけてアニメーションする

        val Anime2 = RotateAnimation(                                  /*画像を左に15度傾けるアニメーション*/
            15f,                                           //初期位置 = 15度
            -15f,                                            //停止位置 = -15度
            Animation.RELATIVE_TO_SELF, 0.5f,               //回転軸(x軸)
            Animation.RELATIVE_TO_SELF, 0.5f                //回転軸(y軸)
        )
        Anime2.duration = 750                                         //0.75秒かけてアニメーションする

        val Anime3 = RotateAnimation(                                  /*画像を傾き0度にするアニメーション*/
            -15f,                                          //初期位置 = -15度
            0f,                                              //停止位置　= 0度
            Animation.RELATIVE_TO_SELF, 0.5f,               //回転軸(x軸)
            Animation.RELATIVE_TO_SELF, 0.5f                //回転軸(y軸)
        )
        Anime3.duration = 500                                          //0.5秒かけてアニメーションする

        //アニメーションを実行
        Handler().postDelayed(Runnable {
            subimage.startAnimation(Anime1)             //画面表示から0.5秒後モンスターのアニメーション1を開始
            fadeAnimation = 1
            Handler().postDelayed(Runnable {
                subimage.startAnimation(Anime2)         //0.5秒の遅延後、モンスターのアニメーション2を開始
                Handler().postDelayed(Runnable {
                    subimage.startAnimation(Anime3)      //0.75秒の遅延後、モンスターのアニメーション3を開始
                }, 750)
            }, 500)
        },500)
    }


}


