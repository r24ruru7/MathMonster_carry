package com.example.fura.mathmonster_carry

import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_result.*

class ResultActivity : AppCompatActivity() {

    private lateinit var soundPool: SoundPool       //音声流すためのやつ
    private var GreatSound = 0    //音声ID

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

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {        //アンドロイドのバージョンがlolipop以前か
            soundPool = SoundPool(2, AudioManager.STREAM_MUSIC, 0)
        } else {
            val attr = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            soundPool = SoundPool.Builder()
                .setAudioAttributes(attr)
                //パラメーターはリソースの数に合わせる
                .setMaxStreams(2)
                .build()
        }

        GreatSound = soundPool.load(this, R.raw.mix_clear, 1)

        //音楽のロードを確認するまでループ
        var streamID = 0
        do {
            //少し待ち時間を入れる
            try {
                Thread.sleep(10)
            } catch (e: InterruptedException) {
            }

            //ボリュームをゼロにして再生して戻り値をチェック
            streamID = soundPool.play(GreatSound, 0.0f, 0.0f, 1, 0, 1.0f)
        } while (streamID == 0)
        //確認


        /******本文******/
        soundPool.play(GreatSound, 1.0f, 1.0f, 0, 0, 1.0f)

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

    override fun onPause() {
        soundPool.release ()
        super.onPause()
    }
}
