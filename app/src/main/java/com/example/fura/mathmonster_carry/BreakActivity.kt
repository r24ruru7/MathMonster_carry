package com.example.fura.mathmonster_carry

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.*
import kotlinx.android.synthetic.main.activity_break.*
import kotlinx.android.synthetic.main.activity_monster.*
import kotlinx.android.synthetic.main.activity_monster.ground0

class BreakActivity : AppCompatActivity() {

    var fadeAnimation = 0   //「がめんにふれてね」 がフェードインしたか確認フラグ(モンスターのアニメーションが始まったら1になる)
    val Picc = arrayOf(R.drawable.monster1, R.drawable.monster2, R.drawable.monster3, R.drawable.monster4, R.drawable.monster5, R.drawable.circle1,
        R.drawable.monster6, R.drawable.monster7, R.drawable.monster8, R.drawable.monster9, R.drawable.monster10, R.drawable.circle1)                   //モンスター画像の配列、ミックス(titletext_from_Level = 6, 12)には適当な画像を入れてる

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_break)


        /*intent受け取り*/
        val titletext_from_Math = intent.getIntExtra("LevelCalc_Math_Break", 0)         //MathActivityから(足し算or引き算、選択レベル)
        val nowlevel_from_Math = intent.getIntExtra("NowLevel_Math_Break", 0)           //MathActivityからレベルミックス時の現在のレベル
        val timerpast_from_Math = intent.getLongExtra("ClearTime_Math_Break", 0)           //MathActivityからレベルミックス時の時間計測
        val falsecount_from_Math = intent.getIntExtra("FalseAns_Math_Break", 0)         //MathActivityからレベルミックス時の間違えた数

        /*intent渡し*/
        val intent_to_Level = Intent(this, LevelActivity::class.java)                        //BreakAtivityに(足し算or引き算、選択レベル)
        val intent_to_Monster = Intent(this, MonsterActivity::class.java)                    //MonsterActivityに(足し算or引き算、選択レベル)
        val intent_to_Result = Intent(this, ResultActivity::class.java)                      //ResultActivityに(まちがい数、クリア時間)


        /******本文******/
        breakimage.setImageResource(Picc[ LevelMix_Judgment(titletext_from_Math, nowlevel_from_Math) ])                                             //モンスター画像を変更

        TextAnimetion()                                                                             //テキストのアニメーションを実行
        MonsterAnimetion()                                                                          //モンスター画像のアニメーションを実行
        fadein()                                                                                    //がめんにふれてねアニメーションを実行


        textView4.text = timerpast_from_Math.toString()

        //画面タッチ後、次の画面に遷移する
        ground0.setOnClickListener{

            if(titletext_from_Math == 6 || titletext_from_Math == 12){  //レベルミックスの時
                if(nowlevel_from_Math == 4){    //レベルミックスでレベル５クリア時
                    intent_to_Result.putExtra("FalseAns_Break_Result", falsecount_from_Math)
                    intent_to_Result.putExtra("ClearTime_Break_Result", timerpast_from_Math)
                    intent_to_Result.putExtra("LevelCalc_Break_Result", titletext_from_Math)
                    startActivity(intent_to_Result)
                }
                else{   //レベルミックスでレベル５未満の時
                    when(fadeAnimation){
                        0 -> {      }
                        1 -> {      //フラグが立っているとき
                            intent_to_Monster.putExtra("LevelCalc_Break_Monster", titletext_from_Math)              //足し算or引き算、選択したレベルを渡す
                            intent_to_Monster.putExtra("NowLevel_Break_Monster", nowlevel_from_Math + 1)     //レベルミックス時の現在のレベルを渡す
                            intent_to_Monster.putExtra("FalseAns_Break_Monster", falsecount_from_Math)              //レベルミックス時の間違えた数を渡す
                            intent_to_Monster.putExtra("ClearTime_Break_Monster", timerpast_from_Math)              //レベルミックス時の計測時間を渡す
                            startActivity(intent_to_Monster)
                        }
                    }
                }
            }
            else{                                                       //レベル1～5の時
                when(fadeAnimation){
                    0 -> {      }
                    1 -> {      //フラグが立っているとき
                        intent_to_Level.putExtra("LevelCalc_Break_Level", titletext_from_Math)  //足し算or引き算、選択したレベルを渡す
                        startActivity(intent_to_Level)
                    }
                }
            }
        }
    }

    //レベルミックス時に何レベルをやってるか判断する関数
    private fun LevelMix_Judgment(titletext_from_math : Int, nowlevel_from_math : Int):Int{                            //別ページから受け取った数字をひとまとめにする

        var answer_titletext = 0                                                   //まとめた値を入れる
        when(titletext_from_math){
            0, 6, 12 -> {
                answer_titletext = titletext_from_math - 6 + nowlevel_from_math     //レベルミックス時の現在のレベルを判断
            }
            else -> {
                answer_titletext = titletext_from_math - 1                          //選択したレベルになる
            }
        }
        return answer_titletext
    }

    private fun fadein(): Int {                                        //フェードインの処理
        val alphaFadeout = AlphaAnimation(0.0f, 1.0f)                   // 透明度を1から0に変化
        alphaFadeout.duration = 2000                                    // animation時間 msec
        alphaFadeout.fillAfter = true                                  // animationが終わったそのまま表示にする
        touchTEXT.startAnimation(alphaFadeout)
        return 1                                                       //フェードインフラグ用に1を返す
    }

    //「やられた～」テキストのアニメーション関数
    private fun TextAnimetion(){

        val Anime1 = RotateAnimation(                       /*画像を右に15度傾けるアニメーション*/
            0f,                                //初期位置 = 0度
            15f,                                 //停止位置 = 15度
            Animation.RELATIVE_TO_SELF, 0.5f,   //回転軸(x軸)
            Animation.RELATIVE_TO_SELF, 0.5f    //回転軸(y軸)
        )
        Anime1.duration = 500                               //0.5秒かけてアニメーションする

        val Anime2 = RotateAnimation(                       /*画像を左に15度傾けるアニメーション*/
            15f,                               //初期位置 = 15度
            -15f,                                //停止位置 = -15度
            Animation.RELATIVE_TO_SELF, 0.5f,   //回転軸(x軸)
            Animation.RELATIVE_TO_SELF, 0.5f    //回転軸(y軸)
        )
        Anime2.duration = 1000                              //1秒かけてアニメーションする

        val Anime3 = RotateAnimation(                       /*画像の傾きを0にするアニメーション*/
            -15f,                              //初期位置 = -15度
            0f,                                  //停止位置 = 0度
            Animation.RELATIVE_TO_SELF, 0.5f,   //回転軸(x軸)
            Animation.RELATIVE_TO_SELF, 0.5f    //回転軸(y軸)
        )
        Anime3.duration = 500                               //0.5秒かけてアニメーションする

        //アニメーションを実行
        Handler().postDelayed(Runnable {
            yararetaText.startAnimation(Anime1)                 //画面表示から0.5秒後モンスターのアニメーション1を開始
            Handler().postDelayed(Runnable {
                yararetaText.startAnimation(Anime2)             //0.5秒の遅延後、モンスターのアニメーション2を開始
                Handler().postDelayed(Runnable {
                    yararetaText.startAnimation(Anime3)         //0.75秒の遅延後、モンスターのアニメーション3を開始
                }, 1000)
            }, 500)
        },500)
    }

    //回転しながら下に消えるモンスターのアニメーション関数
    private fun MonsterAnimetion(){

        val set = AnimationSet(true)        //アニメーションを同時に実行する変数

        val monster_rotate = RotateAnimation(                /*画像を右に180度傾けるアニメーション*/
            0f,                                 //初期位置 = 0度
            180f,                                 //停止位置 = 180度
            Animation.RELATIVE_TO_SELF, 0.5f,    //回転軸(x軸)
            Animation.RELATIVE_TO_SELF, 0.5f     //回転軸(y軸)
        )
        monster_rotate.duration = 4000                       // 4秒かけてアニメーションする
        set.addAnimation(monster_rotate)                     //同時実行変数にアニメーションを追加

        val monster_translate = TranslateAnimation(         /*画像を下に移動させるアニメーション*/
            Animation.RELATIVE_TO_SELF, 0f,     //初期位置(x軸)
            Animation.RELATIVE_TO_SELF, 0f,       //移動量(x軸)
            Animation.RELATIVE_TO_SELF, 0f,     //初期位置(y軸)
            Animation.RELATIVE_TO_SELF, 2f        //移動量(y軸)
        )
        monster_translate.duration = 4000                   //4秒かけてアニメーションする
        set.addAnimation(monster_translate)                 //同時実行変数にアニメーションを追加

        //アニメーションを実行
        Handler().postDelayed({
            breakimage.startAnimation(set)                  //同時アニメーションを実行
            fadeAnimation = 1                             //フラグを立てる
            Handler().postDelayed(Runnable {
                breakimage.setImageDrawable(null)          //4秒後モンスター画像を消す
            }, 4000)
        },500)

    }
}
