package com.example.fura.mathmonster_carry

import android.animation.ObjectAnimator
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_math.*

class MathActivity : AppCompatActivity() {

    var create_question = CreateQuestion()      //クラス宣言
    var dpi = 0 //画面解像度

    val timer_start = System.currentTimeMillis()  //レベルミックスの時間計測用
    var select_calculate = 0    //足し算なら１、引き算なら2が入る
    var success_count = 0       //正解した数

    var false_count = 0     //間違った回数
    var select_level = 0        //intentのtitletextを入れる
    var now_level = 0       //intentのnowlevelを入れる
    var time_past :Long = 0       //intentのtimerpastを入れる

    var on_numberbutton = true    //数字ボタンが押せるかのフラグ
    var on_one_button = false       //1の位のボタンが押されているかのフラグ
    var on_ten_button = false        //＋１０ボタンのフラグ
    var hint_button_hintlevel = 0   //ヒントの段階のフラグ
    var hint_button_select_flag = 0 //レベル4用、使用するヒント(5と5で10 → 5, あと〇で10 → 10)

    var Number_up_down_level = Triple(0, 0, 0)     //(上側の数字, 下側の数字, 問題のレベル)が入る
    var number_up = 0       //上側の数字が入る
    var number_down = 0    //下側の数字が入る
    var number_question = 0      //問題のレベルが入る
    var number_total = 0        //問題の答えが入る

    var answer_one = 0      //一の位の答えが入る
    var answer_ten = 0      //十の位の答えが入る
    var answer_total = 0    //入力した解答

    private lateinit var soundPool: SoundPool       //音声流すためのやつ
    private var soundCorrect = 0    //音声ID
    private var soundFalse = 0
    private var soundHint = 0
    private var soundHintMove = 0
    private var soundHintFive = 0
    private var soundHintFadeout = 0
    private var soundHintBack = 0

    var teat_btn_flag = false

    val Titles = arrayOf(
        "レベル1", "レベル2", "レベル3", "レベル4", "レベル5", "レベル6",
        "レベル1", "レベル2", "レベル3", "レベル4", "レベル5", "レベル6"
    )                                                                         //タイトルを格納する配列、レベル6は使用しないが数の調整のため


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_math)

        /*intent受け取り*/
        val titletext_from_Monster =
            intent.getIntExtra("LevelCalc_Monster_Math", 0)      //MonsterActivityから(足し算or引き算、選択レベル)
        val nowlevel_from_Monster =
            intent.getIntExtra("NowLevel_Monster_Math", 0)        //MonsterActivityからレベルミックス時の現在のレベル
        val timerpast_from_Monster =
            intent.getLongExtra("ClearTime_Monster_Math", 0)   //MonsterActivityからレベルミックス時の計測時間
        val falsecount_from_Monster = intent.getIntExtra("FalseAns_Monster_Math", 0) //MonsterActivityからレベルミックス時の間違えた数

        /*intent渡し*/
        val intent_to_Break = Intent(
            this,
            BreakActivity::class.java
        )                        //BreakAtivityに(足し算or引き算、選択レベル)
        val intent_to_Level = Intent(
            this,
            LevelActivity::class.java
        )                        //LevelAtivityに(足し算or引き算、選択レベル)

        val density = resources.displayMetrics.density
        dpi = density.toInt()

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {        //アンドロイドのバージョンがlolipop以前か
            //1個目のパラメーターはリソースの数に合わせる
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

        soundCorrect = soundPool.load(this, R.raw.se_correct, 1)    //正解
        soundFalse = soundPool.load(this,R.raw.se_false, 1)     //はずれ
        soundHint = soundPool.load(this,R.raw.se_hint, 1)       //ヒント出す
        soundHintMove = soundPool.load(this,R.raw.se_movetile, 1)   //タイル移動
        soundHintFive = soundPool.load(this,R.raw.change_5, 1)       //タイルが5に
        soundHintFadeout = soundPool.load(this,R.raw.delete_tile, 1)  //タイル消える
        soundHintBack = soundPool.load(this,R.raw.se_hintback, 1)  //戻るボタン



        /******本文******/

        lev_Btn.text = Titles[LevelMix_Judgment(
            titletext_from_Monster,
            nowlevel_from_Monster
        )]           //レベルのテキスト変更
        Calculate_Judgment(titletext_from_Monster)      //＋か－を変更
        select_level = titletext_from_Monster         //１～１２の値が入る
        now_level = nowlevel_from_Monster           //intent(ミックス用現在のレベル)を変数に入れる
        time_past = timerpast_from_Monster      //intent(ミックス用時間計測)を変数に入れる
        Display_New_Question()                      //問題を表示
        Tile_Position("delete", "all")   //全てのタイルを画面から消す


        button0.setOnClickListener {
            if (on_numberbutton == true) {        //ボタン入力フラグが立っている
                when (on_ten_button) {        //すでに+10のボタンが押されているか

                    //+10が押されている
                    true -> {
                        on_numberbutton = false       //数字ボタンを押せなくする
                        on_one_button = true    //1の位は入力されたフラグ
                        answer_one = 0          //一の位の数字は6
                        Number_total_one.text = "0"
                        Click_NumberButton(intent_to_Break, falsecount_from_Monster)
                    }

                    //+10が押されていない
                    false -> {
                        on_one_button = true    //1の位は入力されたフラグ
                        answer_one = 0          //一の位の数字は0
                        Number_total_one.text = "0"
                        if(select_calculate == 2){  //引き算の時のみ一桁でも解答
                            on_numberbutton = false       //数字ボタンを押せなくする
                            Click_NumberButton(intent_to_Break, falsecount_from_Monster)
                        }
                    }

                }
            }
        }

        button1.setOnClickListener {
            if (on_numberbutton == true) {        //ボタン入力フラグが立っている
                when (on_ten_button) {        //すでに+10のボタンが押されているか

                    //+10が押されている
                    true -> {
                        on_numberbutton = false       //数字ボタンを押せなくする
                        on_one_button = true    //1の位は入力されたフラグ
                        answer_one = 1          //一の位の数字は6
                        Number_total_one.text = "1"
                        Click_NumberButton(intent_to_Break, falsecount_from_Monster)
                    }

                    //+10が押されていない
                    false -> {
                        on_one_button = true    //1の位は入力されたフラグ
                        answer_one = 1          //一の位の数字は1
                        Number_total_one.text = "1"
                        if(select_calculate == 2){  //引き算の時のみ一桁でも解答
                            on_numberbutton = false       //数字ボタンを押せなくする
                            Click_NumberButton(intent_to_Break, falsecount_from_Monster)
                        }
                    }

                }
            }
        }

        button2.setOnClickListener {
            if (on_numberbutton == true) {        //ボタン入力フラグが立っている
                when (on_ten_button) {        //すでに+10のボタンが押されているか

                    //+10が押されている
                    true -> {
                        on_numberbutton = false       //数字ボタンを押せなくする
                        on_one_button = true    //1の位は入力されたフラグ
                        answer_one = 2          //一の位の数字は6
                        Number_total_one.text = "2"
                        Click_NumberButton(intent_to_Break, falsecount_from_Monster)
                    }

                    //+10が押されていない
                    false -> {
                        on_one_button = true    //1の位は入力されたフラグ
                        answer_one = 2          //一の位の数字は2
                        Number_total_one.text = "2"
                        if(select_calculate == 2){  //引き算の時のみ一桁でも解答
                            on_numberbutton = false       //数字ボタンを押せなくする
                            Click_NumberButton(intent_to_Break, falsecount_from_Monster)
                        }
                    }

                }
            }
        }

        button3.setOnClickListener {
            if (on_numberbutton == true) {        //ボタン入力フラグが立っている
                when (on_ten_button) {        //すでに+10のボタンが押されているか

                    //+10が押されている
                    true -> {
                        on_numberbutton = false       //数字ボタンを押せなくする
                        on_one_button = true    //1の位は入力されたフラグ
                        answer_one = 3          //一の位の数字は6
                        Number_total_one.text = "3"
                        Click_NumberButton(intent_to_Break, falsecount_from_Monster)
                    }

                    //+10が押されていない
                    false -> {
                        on_one_button = true    //1の位は入力されたフラグ
                        answer_one = 3          //一の位の数字は３
                        Number_total_one.text = "3"
                        if(select_calculate == 2){  //引き算の時のみ一桁でも解答
                            on_numberbutton = false       //数字ボタンを押せなくする
                            Click_NumberButton(intent_to_Break, falsecount_from_Monster)
                        }
                    }

                }
            }
        }

        button4.setOnClickListener {
            if (on_numberbutton == true) {        //ボタン入力フラグが立っている
                when (on_ten_button) {        //すでに+10のボタンが押されているか

                    //+10が押されている
                    true -> {
                        on_numberbutton = false       //数字ボタンを押せなくする
                        on_one_button = true    //1の位は入力されたフラグ
                        answer_one = 4          //一の位の数字は6
                        Number_total_one.text = "4"
                        Click_NumberButton(intent_to_Break, falsecount_from_Monster)
                    }

                    //+10が押されていない
                    false -> {
                        on_one_button = true    //1の位は入力されたフラグ
                        answer_one = 4          //一の位の数字は4
                        Number_total_one.text = "4"
                        if(select_calculate == 2){  //引き算の時のみ一桁でも解答
                            on_numberbutton = false       //数字ボタンを押せなくする
                            Click_NumberButton(intent_to_Break, falsecount_from_Monster)
                        }
                    }

                }
            }
        }

        button5.setOnClickListener {
            if (on_numberbutton == true) {        //ボタン入力フラグが立っている
                when (on_ten_button) {        //すでに+10のボタンが押されているか

                    //+10が押されている
                    true -> {
                        on_numberbutton = false       //数字ボタンを押せなくする
                        on_one_button = true    //1の位は入力されたフラグ
                        answer_one = 5          //一の位の数字は6
                        Number_total_one.text = "5"
                        Click_NumberButton(intent_to_Break, falsecount_from_Monster)
                    }

                    //+10が押されていない
                    false -> {
                        on_one_button = true    //1の位は入力されたフラグ
                        answer_one = 5          //一の位の数字は5
                        Number_total_one.text = "5"
                        if(select_calculate == 2){  //引き算の時のみ一桁でも解答
                            on_numberbutton = false       //数字ボタンを押せなくする
                            Click_NumberButton(intent_to_Break, falsecount_from_Monster)
                        }
                    }

                }
            }
        }

        button6.setOnClickListener {
            if (on_numberbutton == true) {        //ボタン入力フラグが立っている
                when (on_ten_button) {        //すでに+10のボタンが押されているか

                    //+10が押されている
                    true -> {
                        on_numberbutton = false       //数字ボタンを押せなくする
                        on_one_button = true    //1の位は入力されたフラグ
                        answer_one = 6          //一の位の数字は6
                        Number_total_one.text = "6"
                        Click_NumberButton(intent_to_Break, falsecount_from_Monster)
                    }

                    //+10が押されていない
                    false -> {
                        on_one_button = true    //1の位は入力されたフラグ
                        answer_one = 6          //一の位の数字は6
                        Number_total_one.text = "6"
                        if(select_calculate == 2){  //引き算の時のみ一桁でも解答
                            on_numberbutton = false       //数字ボタンを押せなくする
                            Click_NumberButton(intent_to_Break, falsecount_from_Monster)
                        }
                    }

                }
            }
        }

        button7.setOnClickListener {
            if (on_numberbutton == true) {        //ボタン入力フラグが立っている
                when (on_ten_button) {        //すでに+10のボタンが押されているか

                    //+10が押されている
                    true -> {
                        on_numberbutton = false       //数字ボタンを押せなくする
                        on_one_button = true    //1の位は入力されたフラグ
                        answer_one = 7          //一の位の数字は7
                        Number_total_one.text = "7"
                        Click_NumberButton(intent_to_Break, falsecount_from_Monster)
                    }

                    //+10が押されていない
                    false -> {
                        on_one_button = true    //1の位は入力されたフラグ
                        answer_one = 7          //一の位の数字は7
                        Number_total_one.text = "7"
                        if(select_calculate == 2){  //引き算の時のみ一桁でも解答
                            on_numberbutton = false       //数字ボタンを押せなくする
                            Click_NumberButton(intent_to_Break, falsecount_from_Monster)
                        }
                    }

                }
            }
        }

        button8.setOnClickListener {
            if (on_numberbutton == true) {        //ボタン入力フラグが立っている
                when (on_ten_button) {        //すでに+10のボタンが押されているか

                    //+10が押されている
                    true -> {
                        on_numberbutton = false       //数字ボタンを押せなくする
                        on_one_button = true    //1の位は入力されたフラグ
                        answer_one = 8          //一の位の数字は8
                        Number_total_one.text = "8"
                        Click_NumberButton(intent_to_Break, falsecount_from_Monster)
                    }

                    //+10が押されていない
                    false -> {
                        on_one_button = true    //1の位は入力されたフラグ
                        answer_one = 8          //一の位の数字は8
                        Number_total_one.text = "8"
                        if(select_calculate == 2){  //引き算の時のみ一桁でも解答
                            on_numberbutton = false       //数字ボタンを押せなくする
                            Click_NumberButton(intent_to_Break, falsecount_from_Monster)
                        }
                    }

                }
            }
        }

        button9.setOnClickListener {
            if (on_numberbutton == true) {        //ボタン入力フラグが立っている
                when (on_ten_button) {        //すでに+10のボタンが押されているか

                    //+10が押されている
                    true -> {
                        on_numberbutton = false       //数字ボタンを押せなくする
                        on_one_button = true    //1の位は入力されたフラグ
                        answer_one = 9          //一の位の数字は9
                        Number_total_one.text = "9"
                        Click_NumberButton(intent_to_Break, falsecount_from_Monster)
                    }

                    //+10が押されていない
                    false -> {
                        on_one_button = true    //1の位は入力されたフラグ
                        answer_one = 9          //一の位の数字は9
                        Number_total_one.text = "9"
                        if(select_calculate == 2){  //引き算の時のみ一桁でも解答
                            on_numberbutton = false       //数字ボタンを押せなくする
                            Click_NumberButton(intent_to_Break, falsecount_from_Monster)
                        }
                    }

                }
            }
        }

        button10.setOnClickListener {
            if (on_numberbutton == true) {        //ボタン入力フラグが立っている
                when (on_ten_button) {        //すでに+10のボタンが押されているか

                    //+10が押されている
                    true -> {
                        Number_total_ten.text = null        //十の位の回答を消す
                        on_ten_button = false           //十の位解答されてない状態にする
                        answer_ten = 0      //十の位の数字は0
                        if(select_calculate == 2){  //引き算の時のみ一桁でも解答
                            on_numberbutton = false       //数字ボタンを押せなくする
                            Click_NumberButton(intent_to_Break, falsecount_from_Monster)
                        }
                    }

                    //+10が押されていない
                    false -> {
                        when (on_one_button) {    //すでに一の位が解答されている

                            //解答されている
                            true -> {
                                on_numberbutton = false       //数字ボタンを押せなくする
                                on_ten_button = true        //＋10が押されている
                                answer_ten = 10          //十の位の数字は１
                                Number_total_ten.text = "1"
                                Click_NumberButton(intent_to_Break, falsecount_from_Monster)
                            }

                            //解答されていない
                            false -> {
                                on_ten_button = true        //＋10が押されている
                                answer_ten = 10          //十の位の数字は１
                                Number_total_ten.text = "1"
                            }
                        }
                    }
                }
            }
        }

        lev_Btn.setOnClickListener {
            if(teat_btn_flag == true){
                intent_to_Break.putExtra("LevelCalc_Math_Break", titletext_from_Monster)
                intent_to_Break.putExtra("NowLevel_Math_Break", nowlevel_from_Monster)
                intent_to_Break.putExtra(
                    "ClearTime_Math_Break",
                    Time_fun(timer_start, timerpast_from_Monster)
                )
                startActivity(intent_to_Break)
            }
            else{
                teat_btn_flag = true
            }
        }

        backBTN.setOnClickListener {
            intent_to_Level.putExtra(
                "LevelCalc_Math_Level",
                titletext_from_Monster
            )                //戻るボタンを押すとレベル選択に戻る
            startActivity(intent_to_Level)
        }

        button13.setOnClickListener {       //あと〇で10のヒントボタン
            hint_button_select_flag = 10    //足し算レベル4用のフラグ(ヒントボタンはあと〇で10を押した)
            when(number_question){          //このボタンを使えるのは下記のレベルだけ
                2, 3, 4, 5 -> Auto_LevelHint(1)
            }
        }

        button11.setOnClickListener {       //5と5で10のヒントボタン
            hint_button_select_flag = 5     //足し算レベル4用のフラグ(ヒントボタンはあと〇で10を押した)
            when(number_question){      //このボタンを使えるのは下記のレベルだけ
                1, 4, 7, 8, 9, 10, 11, 12 -> Auto_LevelHint(1)
            }
        }

        button12.setOnClickListener {
            Sound(7)
            Auto_LevelHint(2)
        }
    }

    private fun Sound(Number:Int){
        when(Number){
            1 -> soundPool.play(soundCorrect, 1.0f, 1.0f, 0, 0, 1.0f)
            2 -> soundPool.play(soundFalse, 1.0f, 1.0f, 0, 0, 1.0f)
            3 -> soundPool.play(soundHint, 1.0f, 1.0f, 0, 0, 1.0f)
            4 -> soundPool.play(soundHintMove, 1.0f, 1.0f, 0, 0, 1.0f)
            5 -> soundPool.play(soundHintFive, 1.0f, 1.0f, 0, 0, 1.0f)
            6 -> soundPool.play(soundHintFadeout, 1.0f, 1.0f, 0, 0, 1.0f)
            7 -> soundPool.play(soundHintBack, 1.0f, 1.0f, 0, 0, 1.0f)
        }
    }

    //ヒントボタンを押したら出題されているレベルに応じてヒント関数に飛ぶ
    fun Auto_LevelHint(user_click_hintbutton :Int){
        when(number_question){
            1 -> Level1_hint(user_click_hintbutton)
            2 -> Level2_hint(user_click_hintbutton)
            3 -> Level3_hint(user_click_hintbutton)
            4 -> Level4_hint(user_click_hintbutton)
            5 -> Level5_hint(user_click_hintbutton)
            7 -> Level7_hint(user_click_hintbutton)
            8 -> Level8_hint(user_click_hintbutton)
            9 -> Level9_hint(user_click_hintbutton)
            10 -> Level10_hint(user_click_hintbutton)
            11 -> Level11_hint(user_click_hintbutton)
        }
    }

    //ヒントボタンが押されたらヒントのレベルを増減する
    fun Auto_HintLevel_plusminus(user_click_hintbutton: Int){
        when(user_click_hintbutton){
            1 -> hint_button_hintlevel ++   //ヒントレベルを＋１
            2 -> hint_button_hintlevel --   //ヒントレベルを－１
        }
    }

    //ヒントボタンが押せるかどうかの関数
    fun Put_Button_11_13_Flag(){
        when(number_question){
            1, 7, 8, 9, 10, 11, 12 -> {     //この数字のレベルは5と5で10のボタンを使う
                button11.setEnabled(true)
                button13.setEnabled(false)
            }
            2, 3, 5 ->{     //この数字のレベルはあと〇で10のボタンを使う
                button11.setEnabled(false)
                button13.setEnabled(true)
            }
            4 ->{       //この数字のレベルは両方のボタンが使える
                button11.setEnabled(true)
                button13.setEnabled(true)
            }
        }
    }


    //タイルの色を必ず交互に振り分ける関数
    private fun Tile_ColorChange1(select_mode: String) {

        when (select_mode) {

            "start" -> {    //一番最初のタイル表示のとき

                for (i in 0..number_down - 1) {  //下のタイルの数だけタイルの色変更
                    Tile_ColorChange2("under", i % 2, i)    //下側、偶数or奇数、タイルの番号を渡す
                }

                for (i in 0..number_up - 1) {  //下のタイルの数だけタイルの色変更
                    if(number_up < 5){
                        if(number_down % 2 == 0) Tile_ColorChange2("up", i % 2, i) //偶数番のタイルはオレンジ
                        else Tile_ColorChange2("up", (i + 1) % 2, i)
                    }
                    else Tile_ColorChange2("up", i % 2, i)    //上側、偶数or奇数、タイルの番号を渡す
                }
            }

            "after_movetile_Lev1" -> {   //残りのタイルを合体させるとき(5と5で10)
                for(i in 5..number_down){
                    Tile_ColorChange2("under", i % 2, i)    //最初のタイルをオレンジにする
                }
                for(i in 5..number_up){
                    if(number_down % 2 == 0) Tile_ColorChange2("up", (i + 1) % 2, i) //偶数番のタイルはオレンジ
                    else Tile_ColorChange2("up", i % 2, i)
                }
            }

            "after_movetile_Lev2" -> {      //タイルを合体させるとき(あと〇で10)
                if(number_up < number_down) Tile_ColorChange2("up", 1, number_up - 1)    //移動するタイルは黄色になる(10番目のタイルになるから)
                else Tile_ColorChange2("under", 1, number_down - 1)    //移動するタイルは黄色になる(10番目のタイルになるから)
            }

            "after_movetile_Lev3" -> {      //タイルを合体させるとき(あと〇で10)
                if(number_up < number_down) Tile_ColorChange2("up", 0, number_up - 2)    //移動するタイルはオレンジになる(9番目のタイルになるから)
                else Tile_ColorChange2("under", 0, number_down - 2)    //移動するタイルはオレンジになる(9番目のタイルになるから)
                if(number_up < number_down) Tile_ColorChange2("up", 1, number_up - 1)    //移動するタイルは黄色になる(10番目のタイルになるから)
                else Tile_ColorChange2("under", 1, number_down - 1)    //移動するタイルは黄色になる(10番目のタイルになるから)
            }

            "after_movetile_Lev4" -> {      //余ったタイルを合体させて６以上になったとき
                var i_count = 1
                for(i in (10 - number_down + 5)..(number_up - 1)){
                    Tile_ColorChange2("up", i_count, i)    //最初のタイルをオレンジにする
                    if(i_count == 1) i_count = 0
                    else i_count = 1
                }
            }

            "after_movetile_Lev4-2" -> {
                if(number_up < number_down){
                    when(number_down){
                        9 -> Tile_ColorChange2("up", 1, number_up - 1)
                        8 -> {
                            Tile_ColorChange2("up", 1, number_up - 1)
                            Tile_ColorChange2("up", 0, number_up - 2)
                        }
                    }
                }
                else{
                    when(number_up){
                        9 -> Tile_ColorChange2("under", 1, number_down - 1)
                        8 -> {
                            Tile_ColorChange2("under", 1, number_down - 1)
                            Tile_ColorChange2("under", 0, number_down - 2)
                        }
                    }
                }
            }

            "after_movetile_Lev5" -> {      //タイルを合体させるとき(あと〇で10)
                if(number_up < number_down) Tile_ColorChange2("up", 1, number_up - 3)    //移動するタイルは黄色になる(8番目のタイルになるから)
                else Tile_ColorChange2("under", 1, number_down - 3)    //移動するタイルは黄色になる(8番目のタイルになるから)
                if(number_up < number_down) Tile_ColorChange2("up", 0, number_up - 2)    //移動するタイルはオレンジになる(9番目のタイルになるから)
                else Tile_ColorChange2("under", 0, number_down - 2)    //移動するタイルはオレンジになる(9番目のタイルになるから)
                if(number_up < number_down) Tile_ColorChange2("up", 1, number_up - 1)    //移動するタイルは黄色になる(10番目のタイルになるから)
                else Tile_ColorChange2("under", 1, number_down - 1)    //移動するタイルは黄色になる(10番目のタイルになるから)
                if(number_up == 6 || number_down == 6){
                    if(number_up < number_down) Tile_ColorChange2("up", 0, number_up - 4)    //移動するタイルはオレンジになる(7番目のタイルになるから)
                    else Tile_ColorChange2("under", 0, number_down - 4)    //移動するタイルはオレンジになる(7番目のタイルになるから)
                }
            }
            "start_Minus" -> { //一番最初のタイル表示のとき
                for (i in 0..number_down) {  //下のタイルの数だけタイルの色変更
                    Tile_ColorChange2("minus", 0, i)    //下側、偶数or奇数、タイルの番号を渡す
                }
                Tile_ColorChange2("minus", 0, 10)   //下側５のタイルを透明

                for (i in 0..(number_up - 10)) {  //下のタイルの数だけタイルの色変更
                    if(i < 5) Tile_ColorChange2("up", ((i + 1) % 2), i)    //下側、偶数or奇数、タイルの番号を渡す
                    else Tile_ColorChange2("up", (i % 2), i)    //下側、偶数or奇数、タイルの番号を渡す
                }
            }

            "reset_Minus" -> {
                for(i in 0..5){
                    Tile_ColorChange2("minus_10", (i % 2), i)
                }
            }

            "after_movetile_Lev7" -> {
                if((10 - number_down) <= (number_up - 10)){
                    if((number_up - 10) < 5) Tile_ColorChange2("minus_10", (number_up - 10 + 1) % 2, 5)
                    else Tile_ColorChange2("minus_10", (number_up - 10) % 2, 5)
                }
                else{
                    //色変更なし
                }
            }

            "after_movetile_Lev8" -> {
                if((10 - number_down) <= (number_up - 10)){
                    if((number_up - 10) < 5) {
                        Tile_ColorChange2("minus_10", (number_up - 10) % 2, 5)
                        Tile_ColorChange2("minus_10", (number_up - 10 + 1) % 2, 4)
                    }
                    else {
                        Tile_ColorChange2("minus_10", (number_up - 10 + 1) % 2, 5)
                        Tile_ColorChange2("minus_10", (number_up - 10) % 2, 4)
                    }
                }
                else{
                    Tile_ColorChange2("up", 0, 0)
                }
            }

            "after_movetile_Lev9" -> {
                if((10 - number_down) <= (number_up - 10)){
                    if((number_up - 10) < 5) {
                        Tile_ColorChange2("minus_10", (number_up - 10 + 1) % 2, 5)
                        Tile_ColorChange2("minus_10", (number_up - 10) % 2, 4)
                        Tile_ColorChange2("minus_10", (number_up - 10 + 1) % 2, 3)
                    }
                    else {
                        Tile_ColorChange2("minus_10", (number_up - 10) % 2, 5)
                        Tile_ColorChange2("minus_10", (number_up - 10 + 1) % 2, 4)
                        Tile_ColorChange2("minus_10", (number_up - 10) % 2, 3)
                    }
                }
                else{
                    when(number_total){
                        5 -> {
                            Tile_ColorChange2("up", 1, 1)
                            Tile_ColorChange2("up", 0, 0)
                        }
                        4 -> {
                            Tile_ColorChange2("up", 0, 0)
                        }
                    }
                }
            }

            "after_movetile_Lev10" -> {
                when{
                    (number_up == 15) && (number_down == 6) -> {
                        for(i in 2..5){
                            Tile_ColorChange2("minus_10", (i + 1) % 2, i)
                        }
                    }
                    (number_down == 6) -> {
                        for(i in 0..(number_up - 10)){
                            Tile_ColorChange2("up", i % 2, i)
                        }
                    }
                    (number_down == 5) -> {
                        for(i in 0..(number_up - 10)){
                            Tile_ColorChange2("up", i % 2, i)
                        }
                    }
                }
            }

            "after_movetile_Lev10-2" -> {
                when{
                    (number_down == 6) -> {
                        for(i in 1..(number_up - 10)){
                            Tile_ColorChange2("up", i % 2, i)
                        }
                    }
                    (number_down == 5) -> {
                        for(i in 0..(number_up - 10)){
                            Tile_ColorChange2("up", (i + 1) % 2, i)
                        }
                    }
                }
            }

            "after_movetile_Lev11" -> {
                when(number_down){
                    1 -> {
                        //何もなし
                    }
                    2 -> {
                        Tile_ColorChange2("up", 0, 0)
                    }
                    3 -> {
                        Tile_ColorChange2("up", 0, 1)
                        Tile_ColorChange2("up", 1, 0)
                    }
                    4 -> {
                        Tile_ColorChange2("up", 0, 2)
                        Tile_ColorChange2("up", 1, 1)
                        Tile_ColorChange2("up", 0, 0)
                    }
                }
            }
        }
    }

    //タイルの色を変える関数
    private fun Tile_ColorChange2( up_or_under :String, change_color :Int, change_tile_number :Int){
        val uptiles = listOf(
            up_tile1, up_tile2, up_tile3 ,up_tile4 ,up_tile5,
            up_tile6, up_tile7, up_tile8, up_tile9, up_tile10, up_tile5_2
        )
        val undertiles = listOf(
            under_tile1, under_tile2, under_tile3, under_tile4, under_tile5,
            under_tile6, under_tile7, under_tile8, under_tile9, under_tile10, under_tile5_2
        )
        val uptiles_more = listOf(
            up_tile15_2, up_tile16, up_tile17, up_tile18, up_tile19, up_tile20
        )

        when(up_or_under){  //変更するタイルは上か下か

            "up" -> {   //上の場合
                when(change_color){
                    0 -> uptiles[change_tile_number].setBackgroundResource(R.drawable.wakusen)
                    1 -> uptiles[change_tile_number].setBackgroundResource(R.drawable.wakusen2)
                }
            }

            "under" -> {    //下の場合
                when(change_color){
                    0 -> undertiles[change_tile_number].setBackgroundResource(R.drawable.wakusen)
                    1 -> undertiles[change_tile_number].setBackgroundResource(R.drawable.wakusen2)
                }
            }

            "minus" -> {    //引き算の下側のタイルの場合
                when(change_color){
                    0 -> undertiles[change_tile_number].setBackgroundResource(R.drawable.wakusen3)
                }
            }

            "minus_10" -> {
                when(change_color){
                    0 -> uptiles_more[change_tile_number].setBackgroundResource(R.drawable.wakusen)
                    1 -> uptiles_more[change_tile_number].setBackgroundResource(R.drawable.wakusen2)
                }
            }
        }

    }


    //タイルの位置を表示/リセット/色変更する関数
    private fun Tile_Position(select_mode :String, select_tile :String){
        val alltile = listOf(
            up_tile1, up_tile2, up_tile3 ,up_tile4 ,up_tile5,
            up_tile6, up_tile7, up_tile8, up_tile9, up_tile10, up_tile5_2,
            under_tile1, under_tile2, under_tile3, under_tile4, under_tile5,
            under_tile6, under_tile7, under_tile8, under_tile9, under_tile10, under_tile5_2,
            ten_tile1, under_tile5_3,
            up_tile15_2, up_tile16, up_tile17, up_tile18, up_tile19, up_tile20, ten_tile2
        )       //使用しているタイルすべて(uptile = 0 ～ 10, undertile = 11 ～ 21, tentile = 22, up15 = 24~)

        val uptiles = listOf(
            up_tile1, up_tile2, up_tile3 ,up_tile4 ,up_tile5, up_tile5_2,
            up_tile6, up_tile7, up_tile8, up_tile9, up_tile10
        )
        val undertiles = listOf(
            under_tile1, under_tile2, under_tile3, under_tile4, under_tile5, under_tile5_2,
            under_tile6, under_tile7, under_tile8, under_tile9, under_tile10
        )

        when(select_mode){

            "visible" -> {  //タイルを表示するモード

                when(select_tile){      //タイルが5以上か5以下で場合分け
                    "uptile" -> {
                        if(number_up < 5) {
                            for(i in 0..number_up - 1){
                                uptiles[i].setVisibility(View.VISIBLE)       //5未満のタイルを表示
                            }
                        }
                        else if(number_up >= 10){
                            ten_tile2.setVisibility(View.VISIBLE)
                            if(number_up == 10){
                                //上の数字が１０の時は何もしない
                            }
                            else if(number_up < 15){
                                for(i in 0..(number_up - 10 - 1)){
                                    uptiles[i].setVisibility(View.VISIBLE)
                                }
                            }
                            else {
                                for(i in 5..(number_up - 10)){
                                    uptiles[i].setVisibility(View.VISIBLE)
                                }
                            }

                        }
                        else {
                            for(i in 5..number_up){
                                uptiles[i].setVisibility(View.VISIBLE)       //5以上のタイルを表示
                            }
                        }
                    }
                    "undertile" -> {
                        if(number_down < 5) {
                            for(i in 0..number_down - 1){
                                undertiles[i].setVisibility(View.VISIBLE)       //5未満のタイルを表示
                            }
                        }
                        else {
                            for(i in 5..number_down){
                                undertiles[i].setVisibility(View.VISIBLE)       //5以上のタイルを表示
                            }
                        }
                    }
                }

            }

            "delete" -> {  //タイルを消すモード
                for(i in 1..31){
                    alltile[i-1].setVisibility(View.GONE)       //画面から消す
                    alltile[i-1].setTranslationX(0.0f)          //x軸の初期位置に戻す
                    alltile[i-1].setTranslationY(0.0f)          //y軸の初期位置に戻す
                }
            }

            "reset" -> {    //タイルを元の位置に戻すモード
                for(i in 1..31){
                    alltile[i-1].setTranslationX(0.0f)          //x軸の初期位置に戻す
                    alltile[i-1].setTranslationY(0.0f)          //y軸の初期位置に戻す
                }
            }

        }
    }

    //足し算レベル1のヒント
    private fun Level1_hint(user_click_hintbutton: Int){

        val uptiles = listOf( up_tile1, up_tile2, up_tile3 ,up_tile4 ,up_tile5, up_tile5_2, up_tile6, up_tile7, up_tile8, up_tile9, up_tile10)  //上側のタイルリスト
        val undertiles = listOf( under_tile1, under_tile2, under_tile3, under_tile4, under_tile5, under_tile5_2, under_tile6, under_tile7, under_tile8, under_tile9, under_tile10)  //下側のタイルリスト

        Auto_HintLevel_plusminus(user_click_hintbutton)     //ヒントボタンが押されたらhint_button_hintlevelを増減させる

        when(hint_button_hintlevel){    //ヒントの段階に応じてヒントを与える

            0 -> {      //ヒント表示なし
                button12.setEnabled(false)      //戻るボタンを押せなくする
                Tile_Position("delete", "all")   //全てのタイルを画面から消す
            }

            1 -> {      //横にタイルを表示
                button12.setEnabled(true)       //戻るボタンを押せるようにする
                if(user_click_hintbutton == 1) Sound(3)
                Tile_Position("delete", "all")   //全てのタイルを画面から消す
                Tile_Position("visible", "uptile")      //上側の数字分タイルを表示
                Tile_Position("visible", "undertile")    //下側の数字分タイルを表示
                Tile_ColorChange1("start")  //タイルの色変更
            }

            2 -> {      //5と5のタイルを合体
                if(user_click_hintbutton == 1){ //ヒントボタンを押したとき
                    var uptile52_moveX = ObjectAnimator.ofFloat(uptiles[5], "translationX", ScreenType_Animeation("5_2",1))  //uptileのY軸を指定された分だけ下げる
                    Move_Animetion(uptile52_moveX)
                    var uptile52_moveY = ObjectAnimator.ofFloat(uptiles[5], "translationY", ScreenType_Animeation("5_2",2))  //uptileのY軸を指定された分だけ下げる
                    Move_Animetion(uptile52_moveY)
                    var undertile52_moveX = ObjectAnimator.ofFloat(undertiles[5], "translationX", ScreenType_Animeation("5_2",1))  //undertileのY軸を指定された分だけ下げる
                    Move_Animetion(undertile52_moveX)
                    Sound(4)

                    if(user_click_hintbutton == 1 && number_up == 5 && number_down == 5){       //上下が5の時のみこれでヒント終わり
                        button11.setEnabled(false)      //ヒントを押せなくする
                    }

                    Handler().postDelayed({         //5と5のタイルが10に変化するアニメーション
                        Sound(5)
                        FadeTile(2, uptiles[5])
                        FadeTile(2, undertiles[5])
                        FadeTile(1, ten_tile1)
                        Level1_hint(1)
                    }, 1500)
                }
                else{   //戻るボタンを押したとき
                    Level1_hint(2)
                }

            }

            3 -> {
                Tile_Position("reset", "all")   //全てのタイルを初期値に戻す
                uptiles[5].setTranslationX(ScreenType_Animeation("5_2",1))       //上側5のタイルをX座標を移動
                uptiles[5].setTranslationY(ScreenType_Animeation("5_2",2))        //上側5のタイルをY座標を移動
                undertiles[5].setTranslationX(ScreenType_Animeation("5_2",1))    //下側5のタイルをX座標を移動
                uptiles[5].setVisibility(View.GONE)     //上側5のタイルを見えなくする
                undertiles[5].setVisibility(View.GONE)  //下側5のタイルを見えなくする
                ten_tile1.setVisibility(View.VISIBLE)   //10のタイルを出現させる
                if(user_click_hintbutton == 1 && number_up == 5 && number_down == 5){   //上下が5の時のみ次へ飛ぶ
                    Level1_hint(1)
                }
                if(user_click_hintbutton == 2 && number_up == 5 && number_down == 5){   //上下が5の時のみ前に戻る
                    Level1_hint(2)
                }
            }

            4 -> {
                if(user_click_hintbutton == 1){
                    Tile_ColorChange1("after_movetile_Lev1")     //移動後のタイルの色を交互にさせる
                    Sound(4)
                    for(i in 6..number_up){     //上側のタイルを下のタイルに合体させるアニメーション
                        var uptile_moveY = ObjectAnimator.ofFloat(uptiles[i], "translationY", ScreenType_Animeation("5_2",3))  //uptileのY軸を指定された分だけ下げる
                        Move_Animetion(uptile_moveY)
                    }
                    for(i in 6..number_down){   //下側のタイルを上のタイルに合体させるアニメーション
                        var undertile_moveY = ObjectAnimator.ofFloat(undertiles[i], "translationY", ScreenType_Animeation("5_2",4))  //undertileのY軸を指定された分だけ下げる
                        Move_Animetion(undertile_moveY)
                    }

                    Level1_hint(1)
                }
                else {
                    Tile_ColorChange1("start")  //戻るボタンが押されたら変更していた色をもとに戻す
                    button11.setEnabled(true)       //戻るボタンを押せるようにする
                    Level1_hint(2)
                }
            }

            5 -> button11.setEnabled(false)
        }
    }

    private fun Level2_hint(user_click_hintbutton: Int){
        val uptiles = listOf( up_tile1, up_tile2, up_tile3 ,up_tile4 ,up_tile5, up_tile5_2, up_tile6, up_tile7, up_tile8, up_tile9, up_tile10)  //上側のタイルリスト
        val undertiles = listOf( under_tile1, under_tile2, under_tile3, under_tile4, under_tile5, under_tile5_2, under_tile6, under_tile7, under_tile8, under_tile9, under_tile10)  //下側のタイルリスト

        Auto_HintLevel_plusminus(user_click_hintbutton)     //ヒントボタンが押されたらhint_button_hintlevelを増減させる

        when(hint_button_hintlevel){    //ヒントの段階に応じてヒントを与える

            0 -> {      //ヒント表示なし
                button12.setEnabled(false)      //戻るボタンを押せなくする
                Tile_Position("delete", "all")   //全てのタイルを画面から消す
            }

            1 -> {      //横にタイルを表示
                button12.setEnabled(true)       //戻るボタンを押せるようにする
                if(user_click_hintbutton == 1) Sound(3)
                Tile_Position("delete", "all")   //全てのタイルを画面から消す
                Tile_Position("visible", "uptile")      //上側の数字分タイルを表示
                Tile_Position("visible", "undertile")    //下側の数字分タイルを表示
                Tile_ColorChange1("start")  //タイルの色変更
            }

            2 -> {      //9のタイルに空白のタイルを1つ追加
                if(user_click_hintbutton == 1){
                    Sound(3)
                    Tile_Position("reset", "all")   //全てのタイルを初期位置に戻す
                    if(number_up < number_down){    //下が9の時
                        undertiles[10].setVisibility(View.VISIBLE)
                        undertiles[10].setBackgroundResource(R.drawable.wakusen3)
                    }
                    else {  //上が9の時
                        uptiles[10].setVisibility(View.VISIBLE)
                        uptiles[10].setBackgroundResource(R.drawable.wakusen3)
                    }
                }
                else {
                    Level2_hint(2)
                }
            }

            3 -> {      //小さい数字のほうのタイル1つを上にずらすアニメーション
                if(user_click_hintbutton == 1){
                    Sound(4)
                    if(number_up < number_down){    //下が9の時
                        var uptile_moveY = ObjectAnimator.ofFloat(uptiles[number_up - 1], "translationY", ScreenType_Animeation("complement",1))  //上のタイルの一番上にあるタイルを少しずらす
                        Move_Animetion(uptile_moveY)
                    }
                    else {  //上が9の時
                        var undertile_moveY = ObjectAnimator.ofFloat(undertiles[number_down - 1], "translationY", ScreenType_Animeation("complement",1))  //下のタイルの一番上にあるタイルを少しずらす
                        Move_Animetion(undertile_moveY)
                    }

                    Level2_hint(1)
                }
                else {
                    Level2_hint(2)
                }

            }

            4 -> {      //ずらしたタイルの位置を固定
                Tile_Position("reset", "all")   //全てのタイルを初期位置に戻す
                if(number_up < number_down){    //下が9の時
                    uptiles[number_up - 1].setTranslationY(ScreenType_Animeation("complement",1))    //上のタイルの一番上にあるタイルをずらした位置に固定
                }
                else {  //上が9の時
                    undertiles[number_down - 1].setTranslationY(ScreenType_Animeation("complement",1))    //下のタイルの一番上にあるタイルをずらした位置に固定
                }
                if(user_click_hintbutton == 2){
                    if(number_up < number_down){    //下が9の時
                        undertiles[10].setVisibility(View.VISIBLE)  //戻ってきたとき空白のタイル10を表示する
                    }
                    else {  //上が9の時
                        uptiles[10].setVisibility(View.VISIBLE) //戻ってきたとき空白のタイル10を表示する
                    }
                }
            }

            5 -> {      //タイルを合体させて10のタイルにするアニメーション1
                if(user_click_hintbutton == 1){
                    if(number_down < number_up) button13.setEnabled(false)
                    Sound(4)
                    if(number_up < number_down){    //下が9の時
                        var uptile_moveY = ObjectAnimator.ofFloat(uptiles[number_up - 1], "translationY", ScreenType_Animeation("complement",2))  //タイル配置の間隔27, タイル一つは重なりがないため30, 上と下の隙間10
                        Move_Animetion(uptile_moveY)
                        Tile_ColorChange1("after_movetile_Lev2")     //移動後のタイルの色を黄色にする
                        FadeTile(2, undertiles[10])     //空白のタイルを隠す
                    }
                    else {  //上が9の時
                        var undertile_moveY = ObjectAnimator.ofFloat(undertiles[number_down - 1], "translationY", ScreenType_Animeation("complement",3))  //タイル配置の間隔27, タイル一つは重なりがないため30, 上と下の隙間10
                        // ( ( (10 - number_down) * -27) - 40) - (27 * 9)　下側のタイルの一番上まで移動(10-num-1)　→　上側のタイル１に移動(40)　→　上のタイルの一番上まで移動(27*9)
                        Move_Animetion(undertile_moveY)
                        Tile_ColorChange1("after_movetile_Lev2")     //移動後のタイルの色を黄色にする
                        FadeTile(2, uptiles[10])        //空白のタイルを隠す
                    }
                    Level2_hint(1)
                }
                else {
                    if(number_total == 10){     //答えが10の時は戻ってきたときここでヒントボタンが押せるようになる
                        button13.setEnabled(true)
                    }
                    Tile_ColorChange1("start")     //移動後のタイルの色を交互にさせる
                    Level2_hint(2)
                }
            }

            6 -> {      //タイルを合体させて10のタイルにするアニメーション2
                if(user_click_hintbutton == 1){
                    Sound(4)
                    if(number_total == 10){     //答えが10の時はもうヒントがない
                        button13.setEnabled(false)
                    }
                    if(number_up < number_down){    //下が9の時
                        uptiles[number_up - 1].setTranslationY(ScreenType_Animeation("complement",2))        //一つだけ動かしたタイルの位置を固定
                        undertiles[10].setVisibility(View.GONE)     //空白のタイルを消す

                        Handler().postDelayed({     //タイル合体後、10の位に移動する
                            Sound(4)
                            var uptile_moveX = ObjectAnimator.ofFloat(uptiles[number_up - 1], "translationX", ScreenType_Animeation("complement",4))    //10の位に移動
                            Move_Animetion(uptile_moveX)
                            for(i in 5..number_down){   //下のタイルは5のタイルと6より上のタイルを動かす
                                var undertile_moveX = ObjectAnimator.ofFloat(undertiles[i], "translationX", ScreenType_Animeation("complement",4))    //10の位に移動
                                Move_Animetion(undertile_moveX)
                            }
                            Handler().postDelayed({     //タイルが10の位に移動後、10のタイルに変化
                                Sound(5)
                                FadeTile(2, uptiles[number_up - 1])     //一つだけ動かしたタイルを消す
                                for(i in 5..number_down){
                                    FadeTile(2, undertiles[i])      //5と6より上のタイルを消す
                                }
                                FadeTile(1, ten_tile1)      //10のタイルをフェードイン
                                Level2_hint(1)
                            },1500)
                        },1500)
                    }
                    else {  //上が9の時
                        undertiles[number_up - 1].setTranslationY(ScreenType_Animeation("complement",3))     //一つだけ動かしたタイルの位置を固定
                        uptiles[10].setVisibility(View.GONE)        //空白のタイルを消す

                        Handler().postDelayed({     //タイル合体後、10の位に移動する
                            Sound(4)
                            var undertile_moveX = ObjectAnimator.ofFloat(undertiles[number_down - 1], "translationX", ScreenType_Animeation("complement",4))    //10の位に移動する
                            Move_Animetion(undertile_moveX)
                            var undertile_moveY = ObjectAnimator.ofFloat(undertiles[number_down - 1], "translationY", ScreenType_Animeation("complement",5))     //10の位に移動する
                            Move_Animetion(undertile_moveY)

                            for(i in 5..number_up){     //上のタイルは5のタイルと6より上のタイルが10の位に移動
                                var uptile_moveX = ObjectAnimator.ofFloat(uptiles[i], "translationX", ScreenType_Animeation("complement",4))         //10の位に移動する
                                Move_Animetion(uptile_moveX)
                                var uptile_moveY = ObjectAnimator.ofFloat(uptiles[i], "translationY", ScreenType_Animeation("complement",6))      //10の位に移動する
                                Move_Animetion(uptile_moveY)

                            }

                            Handler().postDelayed({     //10の位に移動後、10のタイルに変化
                                Sound(5)
                                FadeTile(2, undertiles[number_down - 1])        //一つだけ動かしたタイルを消す
                                for(i in 5..number_up){
                                    FadeTile(2, uptiles[i])     //5と6より上のタイルを消す
                                }
                                FadeTile(1, ten_tile1)      //10のタイルをフェードイン
                                Level2_hint(1)
                            },1500)
                        },1500)
                    }
                }
                else {
                    if(number_down < number_up) button13.setEnabled(true)
                    Tile_Position("visible", "uptile")      //上側の数字分タイルを表示
                    Tile_Position("visible", "undertile")    //下側の数字分タイルを表示
                    ten_tile1.setVisibility(View.GONE)  //戻ってきたとき10のタイルを表示
                    Level2_hint(2)
                }
            }

            7 -> {      //アニメーション後、タイルの位置を固定
                if(number_up < number_down){    //下が9の時
                    uptiles[number_up - 1].setTranslationX(ScreenType_Animeation("complement",4))       //10の位の位置に固定
                    uptiles[number_up - 1].setVisibility(View.GONE)     //移動したタイルは消す
                    for(i in 5..number_down){
                        undertiles[i].setTranslationX(ScreenType_Animeation("complement",4))       //10の位の位置に固定
                        undertiles[i].setVisibility(View.GONE)     //移動したタイルは消す
                    }
                    ten_tile1.setVisibility(View.VISIBLE)   //10のタイルが表示される
                }
                else {    //上が9の時
                    undertiles[number_down - 1].setTranslationX(ScreenType_Animeation("complement",4))       //10の位の位置に固定
                    undertiles[number_down - 1].setTranslationY(ScreenType_Animeation("complement",5))       //10の位の位置に固定
                    undertiles[number_down - 1].setVisibility(View.GONE)        //移動したタイルは消す
                    for(i in 5..number_up){
                        uptiles[i].setTranslationX(ScreenType_Animeation("complement",4))       //10の位の位置に固定
                        uptiles[i].setTranslationY(ScreenType_Animeation("complement",6))        //10の位の位置に固定
                        uptiles[i].setVisibility(View.GONE)     //移動したタイルは消す
                    }
                    ten_tile1.setVisibility(View.VISIBLE)       //10のタイルが表示される
                }
            }

            8 -> {      //下が9の時のみ、上に残っているタイルを下に持ってくる
                if(user_click_hintbutton == 1){
                    Sound(4)
                    if(number_up < number_down){
                        for(i in 0..number_up-1){       //残りのタイルを下に移動するアニメーション
                            var uptile_moveY = ObjectAnimator.ofFloat(uptiles[i], "translationY", ScreenType_Animeation("complement",6))
                            Move_Animetion(uptile_moveY)
                        }
                        Level2_hint(1)
                    }
                    else{
                        Level2_hint(1)
                    }
                }
                else {
                    for(i in 0..number_up-1){
                        uptiles[i].setTranslationY(ScreenType_Animeation("complement",7))  //戻ってくるとき残っていた上にあったタイルをもとの位置に戻す
                    }
                    button13.setEnabled(true)       //ヒントボタンを押せるようにする
                    Level2_hint(2)
                }
            }

            9 -> {      //アニメーション後、タイルの位置を固定
                if(number_up < number_down){
                    for(i in 0..number_up-1){
                        uptiles[i].setTranslationY(ScreenType_Animeation("complement",6))
                    }
                }
                button13.setEnabled(false)
            }
        }
    }

    private fun Level3_hint(user_click_hintbutton: Int){
        val uptiles = listOf( up_tile1, up_tile2, up_tile3 ,up_tile4 ,up_tile5, up_tile5_2, up_tile6, up_tile7, up_tile8, up_tile9, up_tile10)  //上側のタイルリスト
        val undertiles = listOf( under_tile1, under_tile2, under_tile3, under_tile4, under_tile5, under_tile5_2, under_tile6, under_tile7, under_tile8, under_tile9, under_tile10)  //下側のタイルリスト

        Auto_HintLevel_plusminus(user_click_hintbutton)     //ヒントボタンが押されたらhint_button_hintlevelを増減させる

        when(hint_button_hintlevel){    //ヒントの段階に応じてヒントを与える

            0 -> {      //ヒント表示なし
                button12.setEnabled(false)      //戻るボタンを押せなくする
                Tile_Position("delete", "all")   //全てのタイルを画面から消す
            }

            1 -> {      //横にタイルを表示
                button12.setEnabled(true)       //戻るボタンを押せるようにする
                if(user_click_hintbutton == 1) Sound(3)
                Tile_Position("delete", "all")   //全てのタイルを画面から消す
                Tile_Position("visible", "uptile")      //上側の数字分タイルを表示
                Tile_Position("visible", "undertile")    //下側の数字分タイルを表示
                Tile_ColorChange1("start")  //タイルの色変更
            }

            2 -> {      //8のタイルに空白のタイルを1つ追加
                if(user_click_hintbutton == 1){
                    Sound(3)
                    Tile_Position("reset", "all")   //全てのタイルを初期位置に戻す
                    if(number_up < number_down){    //下が8の時
                        undertiles[9].setVisibility(View.VISIBLE)
                        undertiles[10].setVisibility(View.VISIBLE)
                        undertiles[9].setBackgroundResource(R.drawable.wakusen3)
                        undertiles[10].setBackgroundResource(R.drawable.wakusen3)
                    }
                    else {  //上が8の時
                        uptiles[9].setVisibility(View.VISIBLE)
                        uptiles[10].setVisibility(View.VISIBLE)
                        uptiles[9].setBackgroundResource(R.drawable.wakusen3)
                        uptiles[10].setBackgroundResource(R.drawable.wakusen3)
                    }
                }
                else {
                    Level3_hint(2)
                }
            }

            3 -> {      //小さい数字のほうのタイル1つを上にずらすアニメーション
                if(user_click_hintbutton == 1){
                    Sound(4)
                    if(number_up < number_down){    //下が8の時
                        var uptile_moveY1 = ObjectAnimator.ofFloat(uptiles[number_up - 1], "translationY", ScreenType_Animeation("complement",1))  //上のタイルの一番上にあるタイルを少しずらす
                        var uptile_moveY2 = ObjectAnimator.ofFloat(uptiles[number_up - 2], "translationY", ScreenType_Animeation("complement",1))  //上のタイルの二番目に上にあるタイルを少しずらす
                        Move_Animetion(uptile_moveY1)
                        Move_Animetion(uptile_moveY2)
                    }
                    else {  //上が8の時
                        var undertile_moveY1 = ObjectAnimator.ofFloat(undertiles[number_down - 1], "translationY", ScreenType_Animeation("complement",1))  //下のタイルの一番上にあるタイルを少しずらす
                        var undertile_moveY2 = ObjectAnimator.ofFloat(undertiles[number_down - 2], "translationY", ScreenType_Animeation("complement",1))  //下のタイルの二番目に上にあるタイルを少しずらす
                        Move_Animetion(undertile_moveY1)
                        Move_Animetion(undertile_moveY2)
                    }

                    Level3_hint(1)
                }
                else {
                    Level3_hint(2)
                }

            }

            4 -> {      //ずらしたタイルの位置を固定
                Tile_Position("reset", "all")   //全てのタイルを初期位置に戻す
                if(number_up < number_down){    //下が8の時
                    uptiles[number_up - 1].setTranslationY(ScreenType_Animeation("complement",1))    //上のタイルの一番上にあるタイルをずらした位置に固定
                    uptiles[number_up - 2].setTranslationY(ScreenType_Animeation("complement",1))    //上のタイルの二番目に上にあるタイルをずらした位置に固定
                }
                else {  //上が8の時
                    undertiles[number_down - 1].setTranslationY(ScreenType_Animeation("complement",1))    //下のタイルの一番上にあるタイルをずらした位置に固定
                    undertiles[number_down - 2].setTranslationY(ScreenType_Animeation("complement",1))    //下のタイルの二番目に上にあるタイルをずらした位置に固定
                }
                if(user_click_hintbutton == 2){
                    if(number_up < number_down){    //下が8の時
                        undertiles[9].setVisibility(View.VISIBLE)  //戻ってきたとき空白のタイル9を表示する
                        undertiles[10].setVisibility(View.VISIBLE)  //戻ってきたとき空白のタイル10を表示する
                    }
                    else {  //上が8の時
                        uptiles[9].setVisibility(View.VISIBLE) //戻ってきたとき空白のタイル9を表示する
                        uptiles[10].setVisibility(View.VISIBLE) //戻ってきたとき空白のタイル10を表示する
                    }
                }
            }

            5 -> {      //タイルを合体させて10のタイルにするアニメーション1
                if(user_click_hintbutton == 1){
                    Sound(4)
                    if(number_down < number_up) button13.setEnabled(false)
                    if(number_up < number_down){    //下が8の時
                        var uptile_moveY1 = ObjectAnimator.ofFloat(uptiles[number_up - 1], "translationY", ScreenType_Animeation("complement",2))  //タイル配置の間隔27, タイル一つは重なりがないため30, 上と下の隙間10
                        var uptile_moveY2 = ObjectAnimator.ofFloat(uptiles[number_up - 2], "translationY", ScreenType_Animeation("complement",2))  //タイル配置の間隔27, タイル一つは重なりがないため30, 上と下の隙間10
                        Move_Animetion(uptile_moveY1)
                        Move_Animetion(uptile_moveY2)
                        Tile_ColorChange1("after_movetile_Lev3")     //移動後のタイルの色を黄色にする
                        FadeTile(2, undertiles[9])     //空白のタイルを隠す
                        FadeTile(2, undertiles[10])     //空白のタイルを隠す
                    }
                    else {  //上が8の時
                        var undertile_moveY1 = ObjectAnimator.ofFloat(undertiles[number_down - 1], "translationY", ScreenType_Animeation("complement",3))  //タイル配置の間隔27, タイル一つは重なりがないため30, 上と下の隙間10
                        var undertile_moveY2 = ObjectAnimator.ofFloat(undertiles[number_down - 2], "translationY", ScreenType_Animeation("complement",3))  //タイル配置の間隔27, タイル一つは重なりがないため30, 上と下の隙間10
                        // ( ( (10 - number_down) * -27) - 40) - (27 * 9)　下側のタイルの一番上まで移動(10-num-1)　→　上側のタイル１に移動(40)　→　上のタイルの一番上まで移動(27*9)
                        Move_Animetion(undertile_moveY1)
                        Move_Animetion(undertile_moveY2)
                        Tile_ColorChange1("after_movetile_Lev3")     //移動後のタイルの色を黄色にする
                        FadeTile(2, uptiles[9])        //空白のタイルを隠す
                        FadeTile(2, uptiles[10])        //空白のタイルを隠す
                    }
                    Level3_hint(1)
                }
                else {
                    if(number_total == 10){     //答えが10の時は戻ってきたときここでヒントボタンが押せるようになる
                        button13.setEnabled(true)
                    }
                    Tile_ColorChange1("start")     //移動後のタイルの色を交互にさせる
                    Level3_hint(2)
                }
            }

            6 -> {      //タイルを合体させて10のタイルにするアニメーション2
                if(user_click_hintbutton == 1){
                    if(number_total == 10){     //答えが10の時はもうヒントがない
                        button13.setEnabled(false)
                    }
                    if(number_up < number_down){    //下が8の時
                        uptiles[number_up - 1].setTranslationY(ScreenType_Animeation("complement",2))        //一つだけ動かしたタイルの位置を固定
                        uptiles[number_up - 2].setTranslationY(ScreenType_Animeation("complement",2))        //一つだけ動かしたタイルの位置を固定
                        undertiles[9].setVisibility(View.GONE)
                        undertiles[10].setVisibility(View.GONE)     //空白のタイルを消す

                        Handler().postDelayed({     //タイル合体後、10の位に移動する
                            Sound(4)
                            var uptile_moveX1 = ObjectAnimator.ofFloat(uptiles[number_up - 1], "translationX", ScreenType_Animeation("complement",4))    //10の位に移動
                            var uptile_moveX2 = ObjectAnimator.ofFloat(uptiles[number_up - 2], "translationX", ScreenType_Animeation("complement",4))    //10の位に移動
                            Move_Animetion(uptile_moveX1)
                            Move_Animetion(uptile_moveX2)
                            for(i in 5..number_down){   //下のタイルは5のタイルと6より上のタイルを動かす
                                var undertile_moveX = ObjectAnimator.ofFloat(undertiles[i], "translationX", ScreenType_Animeation("complement",4))    //10の位に移動
                                Move_Animetion(undertile_moveX)
                            }
                            Handler().postDelayed({     //タイルが10の位に移動後、10のタイルに変化
                                Sound(5)
                                FadeTile(2, uptiles[number_up - 1])     //一つだけ動かしたタイルを消す
                                FadeTile(2, uptiles[number_up - 2])     //一つだけ動かしたタイルを消す
                                for(i in 5..number_down){
                                    FadeTile(2, undertiles[i])      //5と6より上のタイルを消す
                                }
                                FadeTile(1, ten_tile1)      //10のタイルをフェードイン
                                Level3_hint(1)
                            },1500)
                        },1500)
                    }
                    else {  //上が8の時
                        undertiles[number_up - 1].setTranslationY(ScreenType_Animeation("complement",3))     //一つだけ動かしたタイルの位置を固定
                        undertiles[number_up - 2].setTranslationY(ScreenType_Animeation("complement",3))     //一つだけ動かしたタイルの位置を固定
                        uptiles[9].setVisibility(View.GONE)        //空白のタイルを消す
                        uptiles[10].setVisibility(View.GONE)        //空白のタイルを消す

                        Handler().postDelayed({     //タイル合体後、10の位に移動する
                            Sound(4)
                            var undertile_moveX1 = ObjectAnimator.ofFloat(undertiles[number_down - 1], "translationX", ScreenType_Animeation("complement",4))    //10の位に移動する
                            var undertile_moveX2 = ObjectAnimator.ofFloat(undertiles[number_down - 2], "translationX", ScreenType_Animeation("complement",4))    //10の位に移動する
                            Move_Animetion(undertile_moveX1)
                            Move_Animetion(undertile_moveX2)
                            var undertile_moveY1 = ObjectAnimator.ofFloat(undertiles[number_down - 1], "translationY", ScreenType_Animeation("complement",5))     //10の位に移動する
                            var undertile_moveY2 = ObjectAnimator.ofFloat(undertiles[number_down - 2], "translationY", ScreenType_Animeation("complement",5))     //10の位に移動する
                            Move_Animetion(undertile_moveY1)
                            Move_Animetion(undertile_moveY2)

                            for(i in 5..number_up){     //上のタイルは5のタイルと6より上のタイルが10の位に移動
                                var uptile_moveX = ObjectAnimator.ofFloat(uptiles[i], "translationX", ScreenType_Animeation("complement",4))         //10の位に移動する
                                Move_Animetion(uptile_moveX)
                                var uptile_moveY = ObjectAnimator.ofFloat(uptiles[i], "translationY", ScreenType_Animeation("complement",6))      //10の位に移動する
                                Move_Animetion(uptile_moveY)

                            }

                            Handler().postDelayed({     //10の位に移動後、10のタイルに変化
                                Sound(5)
                                FadeTile(2, undertiles[number_down - 1])        //一つだけ動かしたタイルを消す
                                FadeTile(2, undertiles[number_down - 2])        //一つだけ動かしたタイルを消す
                                for(i in 5..number_up){
                                    FadeTile(2, uptiles[i])     //5と6より上のタイルを消す
                                }
                                FadeTile(1, ten_tile1)      //10のタイルをフェードイン
                                Level3_hint(1)
                            },1500)
                        },1500)
                    }
                }
                else {
                    if(number_down < number_up) button13.setEnabled(true)
                    Tile_Position("visible", "uptile")      //上側の数字分タイルを表示
                    Tile_Position("visible", "undertile")    //下側の数字分タイルを表示
                    ten_tile1.setVisibility(View.GONE)  //戻ってきたとき10のタイルを表示
                    Level3_hint(2)
                }
            }

            7 -> {      //アニメーション後、タイルの位置を固定
                if(number_up < number_down){    //下が8の時
                    uptiles[number_up - 1].setTranslationX(ScreenType_Animeation("complement",4))       //10の位の位置に固定
                    uptiles[number_up - 2].setTranslationX(ScreenType_Animeation("complement",4))       //10の位の位置に固定
                    uptiles[number_up - 1].setVisibility(View.GONE)     //移動したタイルは消す
                    uptiles[number_up - 2].setVisibility(View.GONE)     //移動したタイルは消す
                    for(i in 5..number_down){
                        undertiles[i].setTranslationX(ScreenType_Animeation("complement",4))       //10の位の位置に固定
                        undertiles[i].setVisibility(View.GONE)     //移動したタイルは消す
                    }
                    ten_tile1.setVisibility(View.VISIBLE)   //10のタイルが表示される
                }
                else {    //上が8の時
                    undertiles[number_down - 1].setTranslationX(ScreenType_Animeation("complement",4))       //10の位の位置に固定
                    undertiles[number_down - 1].setTranslationY(ScreenType_Animeation("complement",5))       //10の位の位置に固定
                    undertiles[number_down - 2].setTranslationX(ScreenType_Animeation("complement",4))       //10の位の位置に固定
                    undertiles[number_down - 2].setTranslationY(ScreenType_Animeation("complement",5))       //10の位の位置に固定
                    undertiles[number_down - 1].setVisibility(View.GONE)        //移動したタイルは消す
                    undertiles[number_down - 2].setVisibility(View.GONE)        //移動したタイルは消す
                    for(i in 5..number_up){
                        uptiles[i].setTranslationX(ScreenType_Animeation("complement",4))       //10の位の位置に固定
                        uptiles[i].setTranslationY(ScreenType_Animeation("complement",6))        //10の位の位置に固定
                        uptiles[i].setVisibility(View.GONE)     //移動したタイルは消す
                    }
                    ten_tile1.setVisibility(View.VISIBLE)       //10のタイルが表示される
                }
            }

            8 -> {      //下が8の時のみ、上に残っているタイルを下に持ってくる
                if(user_click_hintbutton == 1){
                    Sound(4)
                    if(number_up < number_down){
                        for(i in 0..number_up-1){       //残りのタイルを下に移動するアニメーション
                            var uptile_moveY = ObjectAnimator.ofFloat(uptiles[i], "translationY", ScreenType_Animeation("complement",6))
                            Move_Animetion(uptile_moveY)
                        }
                        Level3_hint(1)
                    }
                    else{
                        Level3_hint(1)
                    }
                }
                else {
                    for(i in 0..number_up-1){
                        uptiles[i].setTranslationY(ScreenType_Animeation("complement",7))  //戻ってくるとき残っていた上にあったタイルをもとの位置に戻す
                    }
                    button13.setEnabled(true)       //ヒントボタンを押せるようにする
                    Level3_hint(2)
                }
            }

            9 -> {      //アニメーション後、タイルの位置を固定
                if(number_up < number_down){
                    for(i in 0..number_up-1){
                        uptiles[i].setTranslationY(ScreenType_Animeation("complement",6))
                    }
                }
                button13.setEnabled(false)
            }
        }
    }

    private fun Level4_hint(user_click_hintbutton: Int){
        val uptiles = listOf( up_tile1, up_tile2, up_tile3 ,up_tile4 ,up_tile5, up_tile5_2, up_tile6, up_tile7, up_tile8, up_tile9, up_tile10)  //上側のタイルリスト
        val undertiles = listOf( under_tile1, under_tile2, under_tile3, under_tile4, under_tile5, under_tile5_2, under_tile6, under_tile7, under_tile8, under_tile9, under_tile10)  //下側のタイルリスト

        Auto_HintLevel_plusminus(user_click_hintbutton)     //ヒントボタンが押されたらhint_button_hintlevelを増減させる

        when(hint_button_hintlevel){    //ヒントの段階に応じてヒントを与える

            0 -> {      //ヒント表示なし
                button11.setEnabled(true)
                button13.setEnabled(true)
                button12.setEnabled(false)      //戻るボタンを押せなくする
                Tile_Position("delete", "all")   //全てのタイルを画面から消す
                under_tile5_3.setVisibility(View.GONE)
            }

            1 -> {      //横にタイルを表示
                when(hint_button_select_flag){  //どっちのボタンが押されたか
                    5 -> {
                        button13.setEnabled(false)  //5と5で10を押したらあと〇で10は押せなくなる
                    }
                    10 -> {
                        button11.setEnabled(false)  //あと〇で10を押したら5と5で10は押せなくなる
                    }
                }
                if(user_click_hintbutton == 1)Sound(3)
                button12.setEnabled(true)       //戻るボタンを押せるようにする
                Tile_Position("delete", "all")   //全てのタイルを画面から消す
                Tile_Position("visible", "uptile")      //上側の数字分タイルを表示
                Tile_Position("visible", "undertile")    //下側の数字分タイルを表示
                Tile_ColorChange1("start")  //タイルの色変更
            }

            2 -> {      //5と5のタイルを合体
                when(hint_button_select_flag){
                    5 -> {
                        if(user_click_hintbutton == 1){
                            Sound(4)
                            var uptile52_moveX = ObjectAnimator.ofFloat(uptiles[5], "translationX", ScreenType_Animeation("5_2",1))  //uptileのY軸を指定された分だけ下げる
                            Move_Animetion(uptile52_moveX)
                            var uptile52_moveY = ObjectAnimator.ofFloat(uptiles[5], "translationY", ScreenType_Animeation("5_2",2))  //uptileのY軸を指定された分だけ下げる
                            Move_Animetion(uptile52_moveY)
                            var undertile52_moveX = ObjectAnimator.ofFloat(undertiles[5], "translationX", ScreenType_Animeation("5_2",1))  //undertileのY軸を指定された分だけ下げる
                            Move_Animetion(undertile52_moveX)
                             Handler().postDelayed({         //5と5のタイルが10に変化するアニメーション
                                 Sound(5)
                                 FadeTile(2, uptiles[5])
                                 FadeTile(2, undertiles[5])
                                 FadeTile(1, ten_tile1)
                                 Level4_hint(1)
                             }, 1500)
                        }
                        else {
                            Level4_hint(2)
                        }
                    }
                    10 -> {     //空白のタイルを入れる
                        if(user_click_hintbutton == 1){
                            Sound(3)
                            Tile_Position("reset", "all")   //全てのタイルを初期位置に戻す
                            if(number_up < number_down){    //下が8or9の時
                                undertiles[10].setVisibility(View.VISIBLE)
                                undertiles[10].setBackgroundResource(R.drawable.wakusen3)
                                if(number_down == 8){
                                    undertiles[9].setVisibility(View.VISIBLE)
                                    undertiles[9].setBackgroundResource(R.drawable.wakusen3)
                                }
                            }
                            else {  //上が8or9の時
                                uptiles[10].setVisibility(View.VISIBLE)
                                uptiles[10].setBackgroundResource(R.drawable.wakusen3)
                                if(number_up == 8){
                                    uptiles[9].setVisibility(View.VISIBLE)
                                    uptiles[9].setBackgroundResource(R.drawable.wakusen3)
                                }
                            }
                        }
                        else {
                            Tile_Position("reset", "all")   //全てのタイルを初期位置に戻す
                            if(number_up < number_down){    //下が大きいとき
                                uptiles[5].setVisibility(View.VISIBLE)
                                for(i in 0..4){
                                    uptiles[i].setVisibility(View.GONE)
                                }
                            }
                            else {
                                undertiles[5].setVisibility(View.VISIBLE)
                                for(i in 0..4){
                                    undertiles[i].setVisibility(View.GONE)
                                }
                            }
                        }
                    }
                }
            }

            3 -> {
                when(hint_button_select_flag){
                    5 -> {
                        Tile_Position("reset", "all")   //全てのタイルを初期値に戻す
                        uptiles[5].setTranslationX(ScreenType_Animeation("5_2",1))       //上側5のタイルをX座標を移動
                        uptiles[5].setTranslationY(ScreenType_Animeation("5_2",2))        //上側5のタイルをY座標を移動
                        undertiles[5].setTranslationX(ScreenType_Animeation("5_2",1))    //下側5のタイルをX座標を移動
                        uptiles[5].setVisibility(View.GONE)     //上側5のタイルを見えなくする
                        undertiles[5].setVisibility(View.GONE)  //下側5のタイルを見えなくする
                        ten_tile1.setVisibility(View.VISIBLE)   //10のタイルを出現させる
                        if(user_click_hintbutton == 1 && number_up == 5 && number_down == 5){   //上下が5の時のみ次へ飛ぶ
                            Level4_hint(1)
                        }
                        if(user_click_hintbutton == 2 && number_up == 5 && number_down == 5){   //上下が5の時のみ前に戻る
                            Level4_hint(2)
                        }
                    }
                    10 -> { //合計が15以下の時5のタイルを分解する、15以上の答えはこの動作飛ばす
                        if(number_total < 15){      //どちらかの５のタイルを分解する
                            if(number_up < number_down){    //下が大きいとき
                                if(user_click_hintbutton == 1) {
                                    Sound(5)
                                    FadeTile(2, uptiles[5])
                                }
                                else Tile_Position("reset", "all")   //全てのタイルを初期値に戻す
                                uptiles[5].setVisibility(View.GONE)
                                for(i in 0..4){
                                    uptiles[i].setVisibility(View.VISIBLE)
                                }
                            }
                            else {
                                if(user_click_hintbutton == 1) {
                                    Sound(5)
                                    FadeTile(2, undertiles[5])
                                }
                                else Tile_Position("reset", "all")   //全てのタイルを初期値に戻す
                                undertiles[5].setVisibility(View.GONE)
                                for(i in 0..4){
                                    undertiles[i].setVisibility(View.VISIBLE)
                                }
                            }
                        }
                        else {      //答えが１５以上の時は上の動作は飛ばす
                            if(user_click_hintbutton == 1) Level4_hint(1)
                            else Level4_hint(2)

                        }
                    }
                }

            }

            4 -> {
                when(hint_button_select_flag){
                    5 -> {
                        if(user_click_hintbutton == 1){
                            Sound(4)
                            Tile_ColorChange1("after_movetile_Lev1")     //移動後のタイルの色を交互にさせる
                            for(i in 6..number_up){     //上側のタイルを下のタイルに合体させるアニメーション
                                var uptile_moveY = ObjectAnimator.ofFloat(uptiles[i], "translationY", ScreenType_Animeation("5_2",3))  //uptileのY軸を指定された分だけ下げる
                                Move_Animetion(uptile_moveY)
                            }
                            for(i in 6..number_down){   //下側のタイルを上のタイルに合体させるアニメーション
                                var undertile_moveY = ObjectAnimator.ofFloat(undertiles[i], "translationY", ScreenType_Animeation("5_2",4))  //undertileのY軸を指定された分だけ下げる
                                Move_Animetion(undertile_moveY)
                            }
                            Level4_hint(1)
                        }
                        else {
                            Tile_ColorChange1("start")  //戻るボタンが押されたら変更していた色をもとに戻す
                            button11.setEnabled(true)
                            under_tile5_3.setVisibility(View.GONE)
                            Level4_hint(2)
                        }

                    }
                    10 -> { //上に少しずらすアニメーション
                        if(user_click_hintbutton == 1){
                            Sound(4)
                            if(number_up < number_down){    //下が8or9の時
                                if(number_total < 15){  //合計が１５未満の時
                                    when(number_up){    //上の数字が５or6
                                        5 -> {
                                            for(i in (5 - (10 - number_down))..4){
                                                var uptile_moveY = ObjectAnimator.ofFloat(uptiles[i], "translationY", ScreenType_Animeation("complement", 1))  //下のタイルの一番上にあるタイルを少しずらす
                                                Move_Animetion(uptile_moveY)
                                            }
                                        }
                                        6 -> {
                                            var uptile_moveY5 = ObjectAnimator.ofFloat(uptiles[4], "translationY", ScreenType_Animeation("complement", 1))  //下のタイルの一番上にあるタイルを少しずらす
                                            var uptile_moveY6 = ObjectAnimator.ofFloat(uptiles[6], "translationY", ScreenType_Animeation("complement", 1))  //下のタイルの一番上にあるタイルを少しずらす
                                            Move_Animetion(uptile_moveY5)
                                            Move_Animetion(uptile_moveY6)

                                        }
                                    }
                                }
                                else {  //合計が15以上の時
                                    var uptile_moveY1 = ObjectAnimator.ofFloat(uptiles[number_up], "translationY", ScreenType_Animeation("complement", 1))  //上のタイルの一番上にあるタイルを少しずらす
                                    Move_Animetion(uptile_moveY1)
                                    if(number_down == 8){   //大きい数字が８の時
                                        var uptile_moveY2 = ObjectAnimator.ofFloat(uptiles[number_up - 1], "translationY", ScreenType_Animeation("complement", 1))  //上のタイルの二番目に上にあるタイルを少しずらす
                                        Move_Animetion(uptile_moveY2)
                                    }
                                }
                            }
                            else {  //上が8or9の時
                                if(number_total < 15){  //合計が１５未満の時
                                    when(number_down){    //下の数字が５or6
                                        5 -> {
                                            for(i in (5 - (10 - number_up))..4){
                                                var undertile_moveY = ObjectAnimator.ofFloat(undertiles[i], "translationY", ScreenType_Animeation("complement", 1))  //下のタイルの一番上にあるタイルを少しずらす
                                                Move_Animetion(undertile_moveY)
                                            }
                                        }
                                        6 -> {
                                            var undertile_moveY5 = ObjectAnimator.ofFloat(undertiles[4], "translationY", ScreenType_Animeation("complement", 1))  //下のタイルの一番上にあるタイルを少しずらす
                                            var undertile_moveY6 = ObjectAnimator.ofFloat(undertiles[6], "translationY", ScreenType_Animeation("complement", 1))  //下のタイルの一番上にあるタイルを少しずらす
                                            Move_Animetion(undertile_moveY5)
                                            Move_Animetion(undertile_moveY6)
                                        }
                                    }
                                }
                                else {  //合計が15以上の時
                                    var undertile_moveY1 = ObjectAnimator.ofFloat(undertiles[number_down], "translationY", ScreenType_Animeation("complement", 1))  //下のタイルの一番上にあるタイルを少しずらす
                                    Move_Animetion(undertile_moveY1)
                                    if(number_up == 8){   //大きい数字が８の時
                                        var undertile_moveY2 = ObjectAnimator.ofFloat(undertiles[number_down - 1], "translationY", ScreenType_Animeation("complement", 1))  //下のタイルの二番目に上にあるタイルを少しずらす
                                        Move_Animetion(undertile_moveY2)
                                    }
                                }
                            }

                            Level4_hint(1)
                        }
                        else {
                            Level4_hint(2)
                        }
                    }
                }
            }

            5 -> {
                when(hint_button_select_flag){
                    5 -> {
                        for(i in 6..number_up){
                            uptiles[i].setTranslationY(ScreenType_Animeation("5_2",3))
                        }
                        for(i in 6..number_down){
                            undertiles[i].setTranslationY(ScreenType_Animeation("5_2",4))
                        }
                        if(number_total < 15){
                            button11.setEnabled(false)
                        }
                    }
                    10 -> { //ずらしたタイルを固定
                        Tile_Position("reset", "all")   //全てのタイルを初期位置に戻す
                        if(number_up < number_down){    //下が8or9の時
                            if(number_total < 15) {
                                when(number_up){    //下の数字が５or6
                                    5 -> {
                                        for(i in (5 - (10 - number_down))..4){
                                            uptiles[i].setTranslationY(ScreenType_Animeation("complement", 1))    //上のタイルの一番上にあるタイルをずらした位置に固定
                                        }
                                    }
                                    6 -> {
                                        uptiles[6].setTranslationY(ScreenType_Animeation("complement", 1))    //上のタイルの一番上にあるタイルをずらした位置に固定
                                        uptiles[4].setTranslationY(ScreenType_Animeation("complement", 1))    //上のタイルの二番目に上にあるタイルをずらした位置に固定
                                    }
                                }
                            }
                            else{
                                uptiles[number_up].setTranslationY(ScreenType_Animeation("complement", 1))    //上のタイルの一番上にあるタイルをずらした位置に固定
                                if (number_down == 8) uptiles[number_up - 1].setTranslationY(ScreenType_Animeation("complement", 1))    //上のタイルの二番目に上にあるタイルをずらした位置に固定
                            }
                        }
                        else {  //上が8の時
                            if(number_total < 15) {
                                when(number_down){    //下の数字が５or6
                                    5 -> {
                                        for(i in (5 - (10 - number_up))..4){
                                            undertiles[i].setTranslationY(ScreenType_Animeation("complement", 1))    //上のタイルの一番上にあるタイルをずらした位置に固定
                                        }
                                    }
                                    6 -> {
                                        undertiles[6].setTranslationY(ScreenType_Animeation("complement", 1))    //上のタイルの一番上にあるタイルをずらした位置に固定
                                        undertiles[4].setTranslationY(ScreenType_Animeation("complement", 1))    //上のタイルの二番目に上にあるタイルをずらした位置に固定
                                    }
                                }
                            }
                            else{
                                undertiles[number_down].setTranslationY(ScreenType_Animeation("complement", 1))    //下のタイルの一番上にあるタイルをずらした位置に固定
                                if(number_up == 8) undertiles[number_down - 1].setTranslationY(ScreenType_Animeation("complement", 1))    //下のタイルの二番目に上にあるタイルをずらした位置に固定
                            }
                        }
                        if(user_click_hintbutton == 2){
                            if(number_up < number_down){    //下が8の時
                                if(number_down == 8) undertiles[9].setVisibility(View.VISIBLE)  //戻ってきたとき空白のタイル9を表示する
                                undertiles[10].setVisibility(View.VISIBLE)  //戻ってきたとき空白のタイル10を表示する
                            }
                            else {  //上が8の時
                                if(number_up == 8) uptiles[9].setVisibility(View.VISIBLE) //戻ってきたとき空白のタイル9を表示する
                                uptiles[10].setVisibility(View.VISIBLE) //戻ってきたとき空白のタイル10を表示する
                            }
                        }
                    }
                }
            }

            6 -> {
                if(user_click_hintbutton == 1){
                    when(hint_button_select_flag){
                        5 -> {
                            Sound(5)
                            for(i in 1..(number_down - 5) ){
                                FadeTile(2, undertiles[5+i])    //下側タイルの５の塊になるやつを消す
                            }
                            for(i in 1..(10 - number_down) ){
                                FadeTile(2, uptiles[5+i])       //上側タイルの５の塊になるやつを消す
                            }
                            FadeTile(1, under_tile5_3)      //新しい５のタイルをフェードイン
                            if(number_total > 15){      //５の塊に１の塊がついてる場合色変更
                                Tile_ColorChange1("after_movetile_Lev4")     //移動後のタイルの色を交互にさせる
                            }
                            Level4_hint(1)
                            Handler().postDelayed({         //5と5のタイルが10に変化するアニメーション

                            }, 1500)

                        }
                        10 -> {     //10の塊にするアニメーション１
                            Sound(4)
                            if(number_down < number_up) button13.setEnabled(false)
                            if(number_up < number_down){    //下が8の時
                                if(number_total < 15){
                                    when(number_up){
                                        5 -> {
                                            for(i in (5 - (10 - number_down))..4){
                                                var uptile_moveY1 = ObjectAnimator.ofFloat(uptiles[i], "translationY", ScreenType_Animeation("complement", 2))  //タイル配置の間隔27, タイル一つは重なりがないため30, 上と下の隙間10
                                                Move_Animetion(uptile_moveY1)
                                            }
                                        }
                                        6 -> {
                                            var uptile_moveY1 = ObjectAnimator.ofFloat(uptiles[4], "translationY", ScreenType_Animeation("complement", 2))  //タイル配置の間隔27, タイル一つは重なりがないため30, 上と下の隙間10
                                            Move_Animetion(uptile_moveY1)
                                            var uptile_moveY2 = ObjectAnimator.ofFloat(uptiles[6], "translationY", ScreenType_Animeation("complement", 2))  //タイル配置の間隔27, タイル一つは重なりがないため30, 上と下の隙間10
                                            Move_Animetion(uptile_moveY2)
                                        }
                                    }
                                }
                                else{
                                    var uptile_moveY1 = ObjectAnimator.ofFloat(uptiles[number_up], "translationY", ScreenType_Animeation("complement", 2))  //タイル配置の間隔27, タイル一つは重なりがないため30, 上と下の隙間10
                                    Move_Animetion(uptile_moveY1)
                                    if(number_down == 8){
                                        var uptile_moveY2 = ObjectAnimator.ofFloat(uptiles[number_up - 1], "translationY", ScreenType_Animeation("complement", 2))  //タイル配置の間隔27, タイル一つは重なりがないため30, 上と下の隙間10
                                        Move_Animetion(uptile_moveY2)
                                    }
                                }
                                Tile_ColorChange1("after_movetile_Lev4-2")
                                if(number_down == 8) FadeTile(2, undertiles[9])     //空白のタイルを隠す
                                FadeTile(2, undertiles[10])     //空白のタイルを隠す

                            }
                            else {  //上が8の時
                                if(number_total < 15){
                                    when(number_down){
                                        5 -> {
                                            for(i in (5 - (10 - number_up))..4){
                                                var undertile_moveY1 = ObjectAnimator.ofFloat(undertiles[i], "translationY", ScreenType_Animeation("complement", 3))  //タイル配置の間隔27, タイル一つは重なりがないため30, 上と下の隙間10
                                                Move_Animetion(undertile_moveY1)
                                            }
                                        }
                                        6 -> {
                                            var undertile_moveY1 = ObjectAnimator.ofFloat(undertiles[4], "translationY", ScreenType_Animeation("complement", 3))  //タイル配置の間隔27, タイル一つは重なりがないため30, 上と下の隙間10
                                            Move_Animetion(undertile_moveY1)
                                            var undertile_moveY2 = ObjectAnimator.ofFloat(undertiles[6], "translationY", ScreenType_Animeation("complement", 3))  //タイル配置の間隔27, タイル一つは重なりがないため30, 上と下の隙間10
                                            Move_Animetion(undertile_moveY2)
                                        }
                                    }
                                }
                                else {
                                    var undertile_moveY1 = ObjectAnimator.ofFloat(undertiles[number_down], "translationY", ScreenType_Animeation("complement", 3))  //タイル配置の間隔27, タイル一つは重なりがないため30, 上と下の隙間10
                                    // ( ( (10 - number_down) * -27) - 40) - (27 * 9)　下側のタイルの一番上まで移動(10-num-1)　→　上側のタイル１に移動(40)　→　上のタイルの一番上まで移動(27*9)
                                    Move_Animetion(undertile_moveY1)
                                    if(number_up == 8){
                                        var undertile_moveY2 = ObjectAnimator.ofFloat(undertiles[number_down - 1], "translationY", ScreenType_Animeation("complement", 3))  //タイル配置の間隔27, タイル一つは重なりがないため30, 上と下の隙間10
                                        Move_Animetion(undertile_moveY2)
                                    }
                                }

                                Tile_ColorChange1("after_movetile_Lev4-2")
                                if(number_up == 8) FadeTile(2, uptiles[9])        //空白のタイルを隠す
                                FadeTile(2, uptiles[10])        //空白のタイルを隠す
                            }
                            Level4_hint(1)
                        }
                    }
                }
                else{
                    when(hint_button_select_flag){
                        5 -> {
                            under_tile5_3.setVisibility(View.GONE)
                            button11.setEnabled(true)
                            Tile_ColorChange1("after_movetile_Lev1")     //移動後のタイルの色を交互にさせる
                            Level4_hint(2)
                        }
                        10 -> {
                            Tile_ColorChange1("start")     //移動後のタイルの色を交互にさせる
                            Level4_hint(2)
                        }
                    }

                }

            }

            7 -> {
                when(hint_button_select_flag){
                    5 -> {
                        under_tile5_3.setVisibility(View.VISIBLE)
                        button11.setEnabled(false)
                        button13.setEnabled(false)
                    }
                    10 -> {     //10の塊にするアニメーション１
                        if(user_click_hintbutton == 1){
                            if(number_up < number_down){    //下が8の時
                                if(number_total < 15){
                                    when(number_up){
                                        5 -> {
                                            for(i in (5 - (10 - number_down))..4){
                                                uptiles[i].setTranslationY(ScreenType_Animeation("complement", 2))        //一つだけ動かしたタイルの位置を固定
                                                if(i == 3)undertiles[9].setVisibility(View.GONE)
                                                else undertiles[10].setVisibility(View.GONE)
                                            }
                                            Handler().postDelayed({     //タイル合体後、10の位に移動する
                                                Sound(4)
                                                for(i in (5 - (10 - number_down))..4){
                                                    var uptile_moveX1 = ObjectAnimator.ofFloat(uptiles[i], "translationX", ScreenType_Animeation("complement", 4))    //10の位に移動
                                                    Move_Animetion(uptile_moveX1)
                                                }
                                                for(i in 5..number_down){   //下のタイルは5のタイルと6より上のタイルを動かす
                                                    var undertile_moveX = ObjectAnimator.ofFloat(undertiles[i], "translationX", ScreenType_Animeation("complement", 4))    //10の位に移動
                                                    Move_Animetion(undertile_moveX)
                                                }
                                                Handler().postDelayed({     //タイルが10の位に移動後、10のタイルに変化
                                                    Sound(5)
                                                    for(i in (5 - (10 - number_down))..4){
                                                        FadeTile(2, uptiles[i])     //一つだけ動かしたタイルを消す
                                                    }
                                                    for(i in 5..number_down){
                                                        FadeTile(2, undertiles[i])      //5と6より上のタイルを消す
                                                    }
                                                    FadeTile(1, ten_tile1)      //10のタイルをフェードイン
                                                    Level4_hint(1)
                                                },1500)
                                            },1500)
                                        }

                                        6 -> {
                                            uptiles[6].setTranslationY(ScreenType_Animeation("complement", 2))        //一つだけ動かしたタイルの位置を固定
                                            undertiles[10].setVisibility(View.GONE)
                                            uptiles[4].setTranslationY(ScreenType_Animeation("complement", 2))        //一つだけ動かしたタイルの位置を固定
                                            undertiles[9].setVisibility(View.GONE)     //空白のタイルを消す

                                            Handler().postDelayed({     //タイル合体後、10の位に移動する
                                                Sound(4)
                                                var uptile_moveX1 = ObjectAnimator.ofFloat(uptiles[4], "translationX", ScreenType_Animeation("complement", 4))    //10の位に移動
                                                Move_Animetion(uptile_moveX1)
                                                var uptile_moveX2 = ObjectAnimator.ofFloat(uptiles[6], "translationX", ScreenType_Animeation("complement", 4))    //10の位に移動
                                                Move_Animetion(uptile_moveX2)

                                                for(i in 5..number_down){   //下のタイルは5のタイルと6より上のタイルを動かす
                                                    var undertile_moveX = ObjectAnimator.ofFloat(undertiles[i], "translationX", ScreenType_Animeation("complement", 4))    //10の位に移動
                                                    Move_Animetion(undertile_moveX)
                                                }
                                                Handler().postDelayed({     //タイルが10の位に移動後、10のタイルに変化
                                                    Sound(5)
                                                    FadeTile(2, uptiles[4])     //一つだけ動かしたタイルを消す
                                                    FadeTile(2, uptiles[6])     //一つだけ動かしたタイルを消す
                                                    for(i in 5..number_down){
                                                        FadeTile(2, undertiles[i])      //5と6より上のタイルを消す
                                                    }
                                                    FadeTile(1, ten_tile1)      //10のタイルをフェードイン
                                                    Level4_hint(1)
                                                },1500)
                                            },1500)
                                        }
                                    }

                                }
                                else{
                                    uptiles[number_up].setTranslationY(ScreenType_Animeation("complement", 2))        //一つだけ動かしたタイルの位置を固定
                                    undertiles[10].setVisibility(View.GONE)
                                    if(number_down == 8){
                                        uptiles[number_up - 1].setTranslationY(ScreenType_Animeation("complement", 2))        //一つだけ動かしたタイルの位置を固定
                                        undertiles[9].setVisibility(View.GONE)     //空白のタイルを消す
                                    }

                                    Handler().postDelayed({     //タイル合体後、10の位に移動する
                                        Sound(4)
                                        var uptile_moveX1 = ObjectAnimator.ofFloat(uptiles[number_up], "translationX", ScreenType_Animeation("complement", 4))    //10の位に移動
                                        Move_Animetion(uptile_moveX1)
                                        if(number_down == 8){
                                            var uptile_moveX2 = ObjectAnimator.ofFloat(uptiles[number_up - 1], "translationX", ScreenType_Animeation("complement", 4))    //10の位に移動
                                            Move_Animetion(uptile_moveX2)
                                        }
                                        for(i in 5..number_down){   //下のタイルは5のタイルと6より上のタイルを動かす
                                            var undertile_moveX = ObjectAnimator.ofFloat(undertiles[i], "translationX", ScreenType_Animeation("complement", 4))    //10の位に移動
                                            Move_Animetion(undertile_moveX)
                                        }
                                        Handler().postDelayed({     //タイルが10の位に移動後、10のタイルに変化
                                            Sound(5)
                                            FadeTile(2, uptiles[number_up])     //一つだけ動かしたタイルを消す
                                            if(number_down == 8) FadeTile(2, uptiles[number_up - 1])     //一つだけ動かしたタイルを消す
                                            for(i in 5..number_down){
                                                FadeTile(2, undertiles[i])      //5と6より上のタイルを消す
                                            }
                                            FadeTile(1, ten_tile1)      //10のタイルをフェードイン
                                            Level4_hint(1)
                                        },1500)
                                    },1500)
                                }
                            }

                            else {  //上が8の時
                                if(number_total < 15){
                                    when(number_down){
                                        5 -> {
                                            for(i in (5 - (10 - number_up))..4){
                                                undertiles[i].setTranslationY(ScreenType_Animeation("complement", 3))     //一つだけ動かしたタイルの位置を固定
                                                if(i == 3) uptiles[9].setVisibility(View.GONE)        //空白のタイルを消す
                                                uptiles[10].setVisibility(View.GONE)        //空白のタイルを消す
                                            }

                                            Handler().postDelayed({     //タイル合体後、10の位に移動する
                                                Sound(4)
                                                for(i in (5 - (10 - number_up))..4){
                                                    var undertile_moveX1 = ObjectAnimator.ofFloat(undertiles[i], "translationX", ScreenType_Animeation("complement", 4))    //10の位に移動する
                                                    Move_Animetion(undertile_moveX1)
                                                    var undertile_moveY1 = ObjectAnimator.ofFloat(undertiles[i], "translationY", ScreenType_Animeation("complement", 5))     //10の位に移動する
                                                    Move_Animetion(undertile_moveY1)
                                                }

                                                for(i in 5..number_up){     //上のタイルは5のタイルと6より上のタイルが10の位に移動
                                                    var uptile_moveX = ObjectAnimator.ofFloat(uptiles[i], "translationX", ScreenType_Animeation("complement", 4))         //10の位に移動する
                                                    Move_Animetion(uptile_moveX)
                                                    var uptile_moveY = ObjectAnimator.ofFloat(uptiles[i], "translationY", ScreenType_Animeation("complement", 6))      //10の位に移動する
                                                    Move_Animetion(uptile_moveY)

                                                }

                                                Handler().postDelayed({     //10の位に移動後、10のタイルに変化
                                                    Sound(5)
                                                    for(i in (5 - (10 - number_up))..4){
                                                        FadeTile(2, undertiles[i])        //一つだけ動かしたタイルを消す
                                                    }
                                                    for(i in 5..number_up){
                                                        FadeTile(2, uptiles[i])     //5と6より上のタイルを消す
                                                    }
                                                    FadeTile(1, ten_tile1)      //10のタイルをフェードイン
                                                    Level4_hint(1)
                                                },1500)
                                            },1500)
                                        }

                                        6 -> {
                                            undertiles[4].setTranslationY(ScreenType_Animeation("complement", 3))     //一つだけ動かしたタイルの位置を固定
                                            uptiles[9].setVisibility(View.GONE)        //空白のタイルを消す
                                            undertiles[6].setTranslationY(ScreenType_Animeation("complement", 3))     //一つだけ動かしたタイルの位置を固定
                                            uptiles[10].setVisibility(View.GONE)        //空白のタイルを消す

                                            Handler().postDelayed({     //タイル合体後、10の位に移動する
                                                Sound(4)
                                                var undertile_moveX1 = ObjectAnimator.ofFloat(undertiles[4], "translationX", ScreenType_Animeation("complement", 4))    //10の位に移動する
                                                Move_Animetion(undertile_moveX1)
                                                var undertile_moveY1 = ObjectAnimator.ofFloat(undertiles[4], "translationY", ScreenType_Animeation("complement", 5))     //10の位に移動する
                                                Move_Animetion(undertile_moveY1)
                                                var undertile_moveX2 = ObjectAnimator.ofFloat(undertiles[6], "translationX", ScreenType_Animeation("complement", 4))    //10の位に移動する
                                                Move_Animetion(undertile_moveX2)
                                                var undertile_moveY2 = ObjectAnimator.ofFloat(undertiles[6], "translationY", ScreenType_Animeation("complement", 5))     //10の位に移動する
                                                Move_Animetion(undertile_moveY2)

                                                for(i in 5..number_up){     //上のタイルは5のタイルと6より上のタイルが10の位に移動
                                                    var uptile_moveX = ObjectAnimator.ofFloat(uptiles[i], "translationX", ScreenType_Animeation("complement", 4))         //10の位に移動する
                                                    Move_Animetion(uptile_moveX)
                                                    var uptile_moveY = ObjectAnimator.ofFloat(uptiles[i], "translationY", ScreenType_Animeation("complement", 6))      //10の位に移動する
                                                    Move_Animetion(uptile_moveY)

                                                }

                                                Handler().postDelayed({     //10の位に移動後、10のタイルに変化
                                                    Sound(5)
                                                    FadeTile(2, undertiles[4])        //一つだけ動かしたタイルを消す
                                                    FadeTile(2, undertiles[6])        //一つだけ動かしたタイルを消す
                                                    for(i in 5..number_up){
                                                        FadeTile(2, uptiles[i])     //5と6より上のタイルを消す
                                                    }
                                                    FadeTile(1, ten_tile1)      //10のタイルをフェードイン
                                                    Level4_hint(1)
                                                },1500)
                                            },1500)
                                        }
                                    }
                                }

                                else {
                                    undertiles[number_up].setTranslationY(ScreenType_Animeation("complement", 3))     //一つだけ動かしたタイルの位置を固定
                                    uptiles[10].setVisibility(View.GONE)        //空白のタイルを消す
                                    if(number_up == 8){
                                        undertiles[number_up - 1].setTranslationY(ScreenType_Animeation("complement", 3))     //一つだけ動かしたタイルの位置を固定
                                        uptiles[9].setVisibility(View.GONE)        //空白のタイルを消す
                                    }

                                    Handler().postDelayed({     //タイル合体後、10の位に移動する
                                        Sound(4)
                                        var undertile_moveX1 = ObjectAnimator.ofFloat(undertiles[number_down], "translationX", ScreenType_Animeation("complement", 4))    //10の位に移動する
                                        Move_Animetion(undertile_moveX1)
                                        var undertile_moveY1 = ObjectAnimator.ofFloat(undertiles[number_down], "translationY", ScreenType_Animeation("complement", 5))     //10の位に移動する
                                        Move_Animetion(undertile_moveY1)
                                        if(number_up == 8){
                                            var undertile_moveX2 = ObjectAnimator.ofFloat(undertiles[number_down - 1], "translationX", ScreenType_Animeation("complement", 4))    //10の位に移動する
                                            Move_Animetion(undertile_moveX2)
                                            var undertile_moveY2 = ObjectAnimator.ofFloat(undertiles[number_down - 1], "translationY", ScreenType_Animeation("complement", 5))     //10の位に移動する
                                            Move_Animetion(undertile_moveY2)
                                        }

                                        for(i in 5..number_up){     //上のタイルは5のタイルと6より上のタイルが10の位に移動
                                            var uptile_moveX = ObjectAnimator.ofFloat(uptiles[i], "translationX", ScreenType_Animeation("complement", 4))         //10の位に移動する
                                            Move_Animetion(uptile_moveX)
                                            var uptile_moveY = ObjectAnimator.ofFloat(uptiles[i], "translationY", ScreenType_Animeation("complement", 6))      //10の位に移動する
                                            Move_Animetion(uptile_moveY)

                                        }

                                        Handler().postDelayed({     //10の位に移動後、10のタイルに変化
                                            Sound(5)
                                            FadeTile(2, undertiles[number_down])        //一つだけ動かしたタイルを消す
                                            if(number_up == 8) FadeTile(2, undertiles[number_down - 1])        //一つだけ動かしたタイルを消す
                                            for(i in 5..number_up){
                                                FadeTile(2, uptiles[i])     //5と6より上のタイルを消す
                                            }
                                            FadeTile(1, ten_tile1)      //10のタイルをフェードイン
                                            Level4_hint(1)
                                        },1500)
                                    },1500)
                                }

                            }
                        }
                        else {
                            if(number_down < number_up) button13.setEnabled(true)
                            Tile_Position("visible", "uptile")      //上側の数字分タイルを表示
                            Tile_Position("visible", "undertile")    //下側の数字分タイルを表示
                            ten_tile1.setVisibility(View.GONE)  //戻ってきたとき10のタイルを表示
                            if(number_up == 5) {    //タイル直すのに5の時だけうまくいかないから
                                uptiles[3].setVisibility(View.VISIBLE)
                                uptiles[4].setVisibility(View.VISIBLE)
                                uptiles[5].setVisibility(View.GONE)
                            }
                            else if(number_down == 5) {
                                undertiles[3].setVisibility(View.VISIBLE)
                                undertiles[4].setVisibility(View.VISIBLE)
                                undertiles[5].setVisibility(View.GONE)
                            }
                            else if(number_up == 6 && number_down == 8){
                                uptiles[6].setVisibility(View.VISIBLE)
                                uptiles[4].setVisibility(View.VISIBLE)
                                uptiles[5].setVisibility(View.GONE)
                            }
                            else if(number_up == 8 && number_down == 6){
                                undertiles[6].setVisibility(View.VISIBLE)
                                undertiles[4].setVisibility(View.VISIBLE)
                                undertiles[5].setVisibility(View.GONE)
                            }
                            Level4_hint(2)
                        }
                    }
                }
            }
            8 -> {  //移動したタイルを固定
                if(number_up < number_down){    //下が8の時
                    if(number_total < 15){
                        when(number_up){
                            5 -> {
                                uptiles[number_up - 1].setTranslationX(ScreenType_Animeation("complement", 4))       //10の位の位置に固定
                                uptiles[number_up - 1].setVisibility(View.GONE)     //移動したタイルは消す
                                if(number_down == 8){
                                    uptiles[number_up - 2].setTranslationX(ScreenType_Animeation("complement", 4))       //10の位の位置に固定
                                    uptiles[number_up - 2].setVisibility(View.GONE)     //移動したタイルは消す
                                }
                            }
                            6 -> {
                                uptiles[4].setTranslationX(ScreenType_Animeation("complement", 4))       //10の位の位置に固定
                                uptiles[4].setVisibility(View.GONE)     //移動したタイルは消す
                                uptiles[6].setTranslationX(ScreenType_Animeation("complement", 4))       //10の位の位置に固定
                                uptiles[6].setVisibility(View.GONE)     //移動したタイルは消す
                            }
                        }
                    }
                    else {
                        uptiles[number_up].setTranslationX(ScreenType_Animeation("complement", 4))       //10の位の位置に固定
                        uptiles[number_up].setVisibility(View.GONE)     //移動したタイルは消す
                        if(number_down == 8){
                            uptiles[number_up - 1].setTranslationX(ScreenType_Animeation("complement", 4))       //10の位の位置に固定
                            uptiles[number_up - 1].setVisibility(View.GONE)     //移動したタイルは消す
                        }
                    }
                    for(i in 5..number_down){
                        undertiles[i].setTranslationX(ScreenType_Animeation("complement", 4))       //10の位の位置に固定
                        undertiles[i].setVisibility(View.GONE)     //移動したタイルは消す
                    }
                    ten_tile1.setVisibility(View.VISIBLE)   //10のタイルが表示される
                }

                else {    //上が8の時
                    if(number_total < 15){
                        when(number_down){
                            5 -> {
                                undertiles[number_down - 1].setTranslationX(ScreenType_Animeation("complement", 4))       //10の位の位置に固定
                                undertiles[number_down - 1].setTranslationY(ScreenType_Animeation("complement", 5))       //10の位の位置に固定
                                undertiles[number_down - 1].setVisibility(View.GONE)        //移動したタイルは消す
                                if(number_up == 8){
                                    undertiles[number_down - 2].setTranslationX(ScreenType_Animeation("complement", 4))       //10の位の位置に固定
                                    undertiles[number_down - 2].setTranslationY(ScreenType_Animeation("complement", 5))       //10の位の位置に固定
                                    undertiles[number_down - 2].setVisibility(View.GONE)        //移動したタイルは消す
                                }
                            }
                            6 -> {
                                undertiles[4].setTranslationX(ScreenType_Animeation("complement", 4))       //10の位の位置に固定
                                undertiles[4].setVisibility(View.GONE)     //移動したタイルは消す
                                undertiles[6].setTranslationX(ScreenType_Animeation("complement", 4))       //10の位の位置に固定
                                undertiles[6].setVisibility(View.GONE)     //移動したタイルは消す
                            }
                        }
                    }
                    else {
                        undertiles[number_down].setTranslationX(ScreenType_Animeation("complement", 4))       //10の位の位置に固定
                        undertiles[number_down].setTranslationY(ScreenType_Animeation("complement", 5))       //10の位の位置に固定
                        undertiles[number_down].setVisibility(View.GONE)        //移動したタイルは消す
                        if(number_up == 8){
                            undertiles[number_down - 1].setTranslationX(ScreenType_Animeation("complement", 4))       //10の位の位置に固定
                            undertiles[number_down - 1].setTranslationY(ScreenType_Animeation("complement", 5))       //10の位の位置に固定
                            undertiles[number_down - 1].setVisibility(View.GONE)        //移動したタイルは消す
                        }
                    }

                    for(i in 5..number_up){
                        uptiles[i].setTranslationX(ScreenType_Animeation("complement", 4))       //10の位の位置に固定
                        uptiles[i].setTranslationY(ScreenType_Animeation("complement", 6))        //10の位の位置に固定
                        uptiles[i].setVisibility(View.GONE)     //移動したタイルは消す
                    }
                    ten_tile1.setVisibility(View.VISIBLE)       //10のタイルが表示される
                }
            }

            9 -> {      //下が8の時のみ、上に残っているタイルを下に持ってくる
                if(user_click_hintbutton == 1){
                    Sound(4)
                    if(number_up < number_down){
                        for(i in 0..number_up-1){       //残りのタイルを下に移動するアニメーション
                            var uptile_moveY = ObjectAnimator.ofFloat(uptiles[i], "translationY", ScreenType_Animeation("complement", 6))
                            Move_Animetion(uptile_moveY)
                        }
                        Level4_hint(1)
                    }
                    else{
                        Level4_hint(1)
                    }
                }
                else {
                    for(i in 0..number_up-1){
                        uptiles[i].setTranslationY(ScreenType_Animeation("complement", 7))  //戻ってくるとき残っていた上にあったタイルをもとの位置に戻す
                    }
                    button13.setEnabled(true)       //ヒントボタンを押せるようにする
                    Level4_hint(2)
                }
            }

            10 -> {      //アニメーション後、タイルの位置を固定
                if(number_up < number_down){
                    for(i in 0..number_up-1){
                        uptiles[i].setTranslationY(ScreenType_Animeation("complement", 6))
                    }
                }
                button13.setEnabled(false)
            }
        }
    }

    //足し算レベル5のヒント
    private fun Level5_hint(user_click_hintbutton: Int){

        val uptiles = listOf( up_tile1, up_tile2, up_tile3 ,up_tile4 ,up_tile5, up_tile5_2, up_tile6, up_tile7, up_tile8, up_tile9, up_tile10)  //上側のタイルリスト
        val undertiles = listOf( under_tile1, under_tile2, under_tile3, under_tile4, under_tile5, under_tile5_2, under_tile6, under_tile7, under_tile8, under_tile9, under_tile10)  //下側のタイルリスト

        Auto_HintLevel_plusminus(user_click_hintbutton)     //ヒントボタンが押されたらhint_button_hintlevelを増減させる

        when(hint_button_hintlevel){    //ヒントの段階に応じてヒントを与える

            0 -> {      //ヒント表示なし
                button12.setEnabled(false)      //戻るボタンを押せなくする
                Tile_Position("delete", "all")   //全てのタイルを画面から消す
            }

            1 -> {      //横にタイルを表示
                if(user_click_hintbutton == 1) Sound(3)
                button12.setEnabled(true)       //戻るボタンを押せるようにする
                Tile_Position("delete", "all")   //全てのタイルを画面から消す
                Tile_Position("visible", "uptile")      //上側の数字分タイルを表示
                Tile_Position("visible", "undertile")    //下側の数字分タイルを表示
                Tile_ColorChange1("start")  //タイルの色変更
            }

            2 -> {      //7のタイルに空白のタイルを1つ追加
                if(user_click_hintbutton == 1){
                    Sound(3)
                    Tile_Position("reset", "all")   //全てのタイルを初期位置に戻す
                    if(number_up < number_down){    //下が7の時
                        undertiles[8].setVisibility(View.VISIBLE)
                        undertiles[9].setVisibility(View.VISIBLE)
                        undertiles[10].setVisibility(View.VISIBLE)
                        undertiles[8].setBackgroundResource(R.drawable.wakusen3)
                        undertiles[9].setBackgroundResource(R.drawable.wakusen3)
                        undertiles[10].setBackgroundResource(R.drawable.wakusen3)
                        if(number_down == 6){   //6の時は4つ目のタイルも
                            undertiles[7].setVisibility(View.VISIBLE)
                            undertiles[7].setBackgroundResource(R.drawable.wakusen3)
                        }
                    }
                    else {  //上が7の時
                        uptiles[8].setVisibility(View.VISIBLE)
                        uptiles[9].setVisibility(View.VISIBLE)
                        uptiles[10].setVisibility(View.VISIBLE)
                        uptiles[8].setBackgroundResource(R.drawable.wakusen3)
                        uptiles[9].setBackgroundResource(R.drawable.wakusen3)
                        uptiles[10].setBackgroundResource(R.drawable.wakusen3)
                        if(number_up == 6){   //6の時は4つ目のタイルも
                            uptiles[7].setVisibility(View.VISIBLE)
                            uptiles[7].setBackgroundResource(R.drawable.wakusen3)
                        }
                    }
                }
                else {
                    Level5_hint(2)
                }
            }

            3 -> {      //小さい数字のほうのタイル1つを上にずらすアニメーション
                if(user_click_hintbutton == 1){
                    Sound(4)
                    if(number_up < number_down){    //下が7の時
                        var uptile_moveY1 = ObjectAnimator.ofFloat(uptiles[number_up - 1], "translationY", ScreenType_Animeation("complement", 1))  //上のタイルの一番上にあるタイルを少しずらす
                        var uptile_moveY2 = ObjectAnimator.ofFloat(uptiles[number_up - 2], "translationY", ScreenType_Animeation("complement", 1))  //上のタイルの二番目に上にあるタイルを少しずらす
                        var uptile_moveY3 = ObjectAnimator.ofFloat(uptiles[number_up - 3], "translationY", ScreenType_Animeation("complement", 1))  //上のタイルの三番目に上にあるタイルを少しずらす
                        Move_Animetion(uptile_moveY1)
                        Move_Animetion(uptile_moveY2)
                        Move_Animetion(uptile_moveY3)
                        if(number_down == 6){   //6の時は4つ目のタイルも
                            var uptile_moveY4 = ObjectAnimator.ofFloat(uptiles[number_up - 4], "translationY", ScreenType_Animeation("complement", 1))  //上のタイルの四番目に上にあるタイルを少しずらす
                            Move_Animetion(uptile_moveY4)
                        }
                    }
                    else {  //上が7の時
                        var undertile_moveY1 = ObjectAnimator.ofFloat(undertiles[number_down - 1], "translationY", ScreenType_Animeation("complement", 1))  //下のタイルの一番上にあるタイルを少しずらす
                        var undertile_moveY2 = ObjectAnimator.ofFloat(undertiles[number_down - 2], "translationY", ScreenType_Animeation("complement", 1))  //下のタイルの二番目に上にあるタイルを少しずらす
                        var undertile_moveY3 = ObjectAnimator.ofFloat(undertiles[number_down - 3], "translationY", ScreenType_Animeation("complement", 1))  //下のタイルの三番目に上にあるタイルを少しずらす
                        Move_Animetion(undertile_moveY1)
                        Move_Animetion(undertile_moveY2)
                        Move_Animetion(undertile_moveY3)
                        if(number_up == 6){ //6の時は4つ目のタイルも
                            var undertile_moveY4 = ObjectAnimator.ofFloat(undertiles[number_down - 4], "translationY", ScreenType_Animeation("complement", 1))  //下のタイルの四番目に上にあるタイルを少しずらす
                            Move_Animetion(undertile_moveY4)
                        }
                    }

                    Level5_hint(1)
                }
                else {
                    Level5_hint(2)
                }

            }

            4 -> {      //ずらしたタイルの位置を固定
                Tile_Position("reset", "all")   //全てのタイルを初期位置に戻す
                if(number_up < number_down){    //下が7の時
                    uptiles[number_up - 1].setTranslationY(ScreenType_Animeation("complement", 1))    //上のタイルの一番上にあるタイルをずらした位置に固定
                    uptiles[number_up - 2].setTranslationY(ScreenType_Animeation("complement", 1))    //上のタイルの二番目にあるタイルをずらした位置に固定
                    uptiles[number_up - 3].setTranslationY(ScreenType_Animeation("complement", 1))    //上のタイルの三番目にあるタイルをずらした位置に固定
                    if(number_down == 6){   //6の時は4つ目のタイルも
                        uptiles[number_up - 4].setTranslationY(ScreenType_Animeation("complement", 1))    //上のタイルの四番目にあるタイルをずらした位置に固定
                    }
                }
                else {  //上が7の時
                    undertiles[number_down - 1].setTranslationY(ScreenType_Animeation("complement", 1))    //下のタイルの一番上にあるタイルをずらした位置に固定
                    undertiles[number_down - 2].setTranslationY(ScreenType_Animeation("complement", 1))    //下のタイルの二番目に上にあるタイルをずらした位置に固定
                    undertiles[number_down - 3].setTranslationY(ScreenType_Animeation("complement", 1))    //下のタイルの三番目に上にあるタイルをずらした位置に固定
                    if(number_up == 6){ //6の時は4つ目のタイルも
                        undertiles[number_down - 4].setTranslationY(ScreenType_Animeation("complement", 1))    //下のタイルの四番目に上にあるタイルをずらした位置に固定
                    }
                }
                if(user_click_hintbutton == 2){
                    if(number_up < number_down){    //下が7の時
                        undertiles[8].setVisibility(View.VISIBLE)  //戻ってきたとき空白のタイル8を表示する
                        undertiles[9].setVisibility(View.VISIBLE)  //戻ってきたとき空白のタイル9を表示する
                        undertiles[10].setVisibility(View.VISIBLE)  //戻ってきたとき空白のタイル10を表示する
                        if(number_down == 6){   //6の時は4つ目のタイルも
                            undertiles[7].setVisibility(View.VISIBLE)  //戻ってきたとき空白のタイル7を表示する
                        }
                    }
                    else {  //上が7の時
                        uptiles[8].setVisibility(View.VISIBLE) //戻ってきたとき空白のタイル8を表示する
                        uptiles[9].setVisibility(View.VISIBLE) //戻ってきたとき空白のタイル9を表示する
                        uptiles[10].setVisibility(View.VISIBLE) //戻ってきたとき空白のタイル10を表示する
                        if(number_up == 6){ //6の時は4つ目のタイルも
                            uptiles[7].setVisibility(View.VISIBLE) //戻ってきたとき空白のタイル7を表示する
                        }
                    }
                }
            }

            5 -> {      //タイルを合体させて10のタイルにするアニメーション1
                if(user_click_hintbutton == 1){
                    Sound(4)
                    if(number_down < number_up) button13.setEnabled(false)
                    if(number_up < number_down){    //下が7の時
                        var uptile_moveY1 = ObjectAnimator.ofFloat(uptiles[number_up - 1], "translationY", ScreenType_Animeation("complement", 2))  //タイル配置の間隔27, タイル一つは重なりがないため30, 上と下の隙間10
                        var uptile_moveY2 = ObjectAnimator.ofFloat(uptiles[number_up - 2], "translationY", ScreenType_Animeation("complement", 2))  //タイル配置の間隔27, タイル一つは重なりがないため30, 上と下の隙間10
                        var uptile_moveY3 = ObjectAnimator.ofFloat(uptiles[number_up - 3], "translationY", ScreenType_Animeation("complement", 2))  //タイル配置の間隔27, タイル一つは重なりがないため30, 上と下の隙間10
                        Move_Animetion(uptile_moveY1)
                        Move_Animetion(uptile_moveY2)
                        Move_Animetion(uptile_moveY3)
                        if(number_down == 6){   //6の時は4つ目のタイルも
                            var uptile_moveY4 = ObjectAnimator.ofFloat(uptiles[number_up - 4], "translationY", ScreenType_Animeation("complement", 2))  //タイル配置の間隔27, タイル一つは重なりがないため30, 上と下の隙間10
                            Move_Animetion(uptile_moveY4)
                            FadeTile(2, undertiles[7])     //空白のタイルを隠す
                        }
                        Tile_ColorChange1("after_movetile_Lev5")     //移動後のタイルの色を黄色にする
                        FadeTile(2, undertiles[8])     //空白のタイルを隠す
                        FadeTile(2, undertiles[9])     //空白のタイルを隠す
                        FadeTile(2, undertiles[10])     //空白のタイルを隠す

                    }
                    else {  //上が7の時
                        var undertile_moveY1 = ObjectAnimator.ofFloat(undertiles[number_down - 1], "translationY", ScreenType_Animeation("complement", 3))  //タイル配置の間隔27, タイル一つは重なりがないため30, 上と下の隙間10
                        var undertile_moveY2 = ObjectAnimator.ofFloat(undertiles[number_down - 2], "translationY", ScreenType_Animeation("complement", 3))  //タイル配置の間隔27, タイル一つは重なりがないため30, 上と下の隙間10
                        var undertile_moveY3 = ObjectAnimator.ofFloat(undertiles[number_down - 3], "translationY", ScreenType_Animeation("complement", 3))  //タイル配置の間隔27, タイル一つは重なりがないため30, 上と下の隙間10
                        // ( ( (10 - number_down) * -27) - 40) - (27 * 9)　下側のタイルの一番上まで移動(10-num-1)　→　上側のタイル１に移動(40)　→　上のタイルの一番上まで移動(27*9)
                        Move_Animetion(undertile_moveY1)
                        Move_Animetion(undertile_moveY2)
                        Move_Animetion(undertile_moveY3)
                        if(number_up == 6){ //6の時は4つ目のタイルも
                            var undertile_moveY4 = ObjectAnimator.ofFloat(undertiles[number_down - 4], "translationY", ScreenType_Animeation("complement", 3))  //タイル配置の間隔27, タイル一つは重なりがないため30, 上と下の隙間10
                            Move_Animetion(undertile_moveY4)
                            FadeTile(2, uptiles[7])        //空白のタイルを隠す
                        }
                        Tile_ColorChange1("after_movetile_Lev5")     //移動後のタイルの色を黄色にする
                        FadeTile(2, uptiles[8])        //空白のタイルを隠す
                        FadeTile(2, uptiles[9])        //空白のタイルを隠す
                        FadeTile(2, uptiles[10])        //空白のタイルを隠す
                    }
                    Level5_hint(1)
                }
                else {
                    if(number_total == 10){     //答えが10の時は戻ってきたときここでヒントボタンが押せるようになる
                        button13.setEnabled(true)
                    }
                    Tile_ColorChange1("start")     //移動後のタイルの色を交互にさせる
                    Level5_hint(2)
                }
            }

            6 -> {      //タイルを合体させて10のタイルにするアニメーション2
                if(user_click_hintbutton == 1){
                    if(number_total == 10){     //答えが10の時はもうヒントがない
                        button13.setEnabled(false)
                    }
                    if(number_up < number_down){    //下が7の時
                        uptiles[number_up - 1].setTranslationY(ScreenType_Animeation("complement", 2))        //一つだけ動かしたタイルの位置を固定
                        uptiles[number_up - 2].setTranslationY(ScreenType_Animeation("complement", 2))        //一つだけ動かしたタイルの位置を固定
                        uptiles[number_up - 3].setTranslationY(ScreenType_Animeation("complement", 2))        //一つだけ動かしたタイルの位置を固定
                        undertiles[8].setVisibility(View.GONE)
                        undertiles[9].setVisibility(View.GONE)
                        undertiles[10].setVisibility(View.GONE)     //空白のタイルを消す
                        if(number_down == 6){   //6の時は4つ目のタイルも
                            uptiles[number_up - 4].setTranslationY(ScreenType_Animeation("complement", 2))        //一つだけ動かしたタイルの位置を固定
                            undertiles[7].setVisibility(View.GONE)
                        }

                        Handler().postDelayed({     //タイル合体後、10の位に移動する
                            Sound(4)
                            var uptile_moveX1 = ObjectAnimator.ofFloat(uptiles[number_up - 1], "translationX", ScreenType_Animeation("complement", 4))    //10の位に移動
                            var uptile_moveX2 = ObjectAnimator.ofFloat(uptiles[number_up - 2], "translationX", ScreenType_Animeation("complement", 4))    //10の位に移動
                            var uptile_moveX3 = ObjectAnimator.ofFloat(uptiles[number_up - 3], "translationX", ScreenType_Animeation("complement", 4))    //10の位に移動
                            Move_Animetion(uptile_moveX1)
                            Move_Animetion(uptile_moveX2)
                            Move_Animetion(uptile_moveX3)
                            if(number_down == 6){   //6の時は4つ目のタイルも
                                var uptile_moveX4 = ObjectAnimator.ofFloat(uptiles[number_up - 4], "translationX", ScreenType_Animeation("complement", 4))    //10の位に移動
                                Move_Animetion(uptile_moveX4)
                            }
                            for(i in 5..number_down){   //下のタイルは5のタイルと6より上のタイルを動かす
                                var undertile_moveX = ObjectAnimator.ofFloat(undertiles[i], "translationX", ScreenType_Animeation("complement", 4))    //10の位に移動
                                Move_Animetion(undertile_moveX)
                            }
                            Handler().postDelayed({     //タイルが10の位に移動後、10のタイルに変化
                                Sound(5)
                                FadeTile(2, uptiles[number_up - 1])     //一つだけ動かしたタイルを消す
                                FadeTile(2, uptiles[number_up - 2])     //一つだけ動かしたタイルを消す
                                FadeTile(2, uptiles[number_up - 3])     //一つだけ動かしたタイルを消す
                                if(number_down == 6){   //6の時は4つ目のタイルも
                                    FadeTile(2, uptiles[number_up - 4])     //一つだけ動かしたタイルを消す
                                }
                                for(i in 5..number_down){
                                    FadeTile(2, undertiles[i])      //5と6より上のタイルを消す
                                }
                                FadeTile(1, ten_tile1)      //10のタイルをフェードイン
                                Level5_hint(1)
                            },1500)
                        },1500)
                    }
                    else {  //上が8の時
                        undertiles[number_up - 1].setTranslationY(ScreenType_Animeation("complement", 3))     //一つだけ動かしたタイルの位置を固定
                        undertiles[number_up - 2].setTranslationY(ScreenType_Animeation("complement", 3))     //一つだけ動かしたタイルの位置を固定
                        undertiles[number_up - 3].setTranslationY(ScreenType_Animeation("complement", 3))     //一つだけ動かしたタイルの位置を固定
                        uptiles[8].setVisibility(View.GONE)        //空白のタイルを消す
                        uptiles[9].setVisibility(View.GONE)        //空白のタイルを消す
                        uptiles[10].setVisibility(View.GONE)        //空白のタイルを消す
                        if(number_up == 6){ //6の時は4つ目のタイルも
                            undertiles[number_up - 4].setTranslationY(ScreenType_Animeation("complement", 3))     //一つだけ動かしたタイルの位置を固定
                            uptiles[7].setVisibility(View.GONE)        //空白のタイルを消す
                        }

                        Handler().postDelayed({     //タイル合体後、10の位に移動する
                            Sound(4)
                            var undertile_moveX1 = ObjectAnimator.ofFloat(undertiles[number_down - 1], "translationX", ScreenType_Animeation("complement", 4))    //10の位に移動する
                            var undertile_moveX2 = ObjectAnimator.ofFloat(undertiles[number_down - 2], "translationX", ScreenType_Animeation("complement", 4))    //10の位に移動する
                            var undertile_moveX3 = ObjectAnimator.ofFloat(undertiles[number_down - 3], "translationX", ScreenType_Animeation("complement", 4))    //10の位に移動する
                            Move_Animetion(undertile_moveX1)
                            Move_Animetion(undertile_moveX2)
                            Move_Animetion(undertile_moveX3)
                            var undertile_moveY1 = ObjectAnimator.ofFloat(undertiles[number_down - 1], "translationY", ScreenType_Animeation("complement", 5))     //10の位に移動する
                            var undertile_moveY2 = ObjectAnimator.ofFloat(undertiles[number_down - 2], "translationY", ScreenType_Animeation("complement", 5))     //10の位に移動する
                            var undertile_moveY3 = ObjectAnimator.ofFloat(undertiles[number_down - 3], "translationY", ScreenType_Animeation("complement", 5))     //10の位に移動する
                            Move_Animetion(undertile_moveY1)
                            Move_Animetion(undertile_moveY2)
                            Move_Animetion(undertile_moveY3)
                            if(number_up == 6){ //6の時は4つ目のタイルも
                                var undertile_moveX4 = ObjectAnimator.ofFloat(undertiles[number_down - 4], "translationX", ScreenType_Animeation("complement", 4))    //10の位に移動する
                                Move_Animetion(undertile_moveX4)
                                var undertile_moveY4 = ObjectAnimator.ofFloat(undertiles[number_down - 4], "translationY", ScreenType_Animeation("complement", 5))     //10の位に移動する
                                Move_Animetion(undertile_moveY4)
                            }

                            for(i in 5..number_up){     //上のタイルは5のタイルと6より上のタイルが10の位に移動
                                var uptile_moveX = ObjectAnimator.ofFloat(uptiles[i], "translationX", ScreenType_Animeation("complement", 4))         //10の位に移動する
                                Move_Animetion(uptile_moveX)
                                var uptile_moveY = ObjectAnimator.ofFloat(uptiles[i], "translationY", ScreenType_Animeation("complement", 6))      //10の位に移動する
                                Move_Animetion(uptile_moveY)

                            }

                            Handler().postDelayed({     //10の位に移動後、10のタイルに変化
                                Sound(5)
                                FadeTile(2, undertiles[number_down - 1])        //一つだけ動かしたタイルを消す
                                FadeTile(2, undertiles[number_down - 2])        //一つだけ動かしたタイルを消す
                                FadeTile(2, undertiles[number_down - 3])        //一つだけ動かしたタイルを消す
                                if(number_up == 6){ //6の時は4つ目のタイルも
                                    FadeTile(2, undertiles[number_down - 4])        //一つだけ動かしたタイルを消す
                                }
                                for(i in 5..number_up){
                                    FadeTile(2, uptiles[i])     //5と6より上のタイルを消す
                                }
                                FadeTile(1, ten_tile1)      //10のタイルをフェードイン
                                Level5_hint(1)
                            },1500)
                        },1500)
                    }
                }
                else {
                    if(number_down < number_up) button13.setEnabled(true)
                    Tile_Position("visible", "uptile")      //上側の数字分タイルを表示
                    Tile_Position("visible", "undertile")    //下側の数字分タイルを表示
                    ten_tile1.setVisibility(View.GONE)  //戻ってきたとき10のタイルを表示
                    Level5_hint(2)
                }
            }

            7 -> {      //アニメーション後、タイルの位置を固定
                if(number_up < number_down){    //下が8の時
                    uptiles[number_up - 1].setTranslationX(ScreenType_Animeation("complement", 4))       //10の位の位置に固定
                    uptiles[number_up - 2].setTranslationX(ScreenType_Animeation("complement", 4))       //10の位の位置に固定
                    uptiles[number_up - 3].setTranslationX(ScreenType_Animeation("complement", 4))       //10の位の位置に固定
                    uptiles[number_up - 1].setVisibility(View.GONE)     //移動したタイルは消す
                    uptiles[number_up - 2].setVisibility(View.GONE)     //移動したタイルは消す
                    uptiles[number_up - 3].setVisibility(View.GONE)     //移動したタイルは消す
                    if(number_down == 6){   //6の時は4つ目のタイルも
                        uptiles[number_up - 4].setTranslationX(ScreenType_Animeation("complement", 4))       //10の位の位置に固定
                        uptiles[number_up - 4].setVisibility(View.GONE)     //移動したタイルは消す
                    }
                    for(i in 5..number_down){
                        undertiles[i].setTranslationX(ScreenType_Animeation("complement", 4))       //10の位の位置に固定
                        undertiles[i].setVisibility(View.GONE)     //移動したタイルは消す
                    }
                    ten_tile1.setVisibility(View.VISIBLE)   //10のタイルが表示される
                }
                else {    //上が8の時
                    undertiles[number_down - 1].setTranslationX(ScreenType_Animeation("complement", 4))       //10の位の位置に固定
                    undertiles[number_down - 1].setTranslationY(ScreenType_Animeation("complement", 5))       //10の位の位置に固定
                    undertiles[number_down - 2].setTranslationX(ScreenType_Animeation("complement", 4))       //10の位の位置に固定
                    undertiles[number_down - 2].setTranslationY(ScreenType_Animeation("complement", 5))       //10の位の位置に固定
                    undertiles[number_down - 3].setTranslationX(ScreenType_Animeation("complement", 4))       //10の位の位置に固定
                    undertiles[number_down - 3].setTranslationY(ScreenType_Animeation("complement", 5))       //10の位の位置に固定
                    undertiles[number_down - 1].setVisibility(View.GONE)        //移動したタイルは消す
                    undertiles[number_down - 2].setVisibility(View.GONE)        //移動したタイルは消す
                    undertiles[number_down - 3].setVisibility(View.GONE)        //移動したタイルは消す
                    if(number_up == 6){ //6の時は4つ目のタイルも
                        undertiles[number_down - 4].setTranslationX(ScreenType_Animeation("complement", 4))       //10の位の位置に固定
                        undertiles[number_down - 4].setTranslationY(ScreenType_Animeation("complement", 5))       //10の位の位置に固定
                        undertiles[number_down - 4].setVisibility(View.GONE)        //移動したタイルは消す
                    }
                    for(i in 5..number_up){
                        uptiles[i].setTranslationX(ScreenType_Animeation("complement", 4))       //10の位の位置に固定
                        uptiles[i].setTranslationY(ScreenType_Animeation("complement", 6))        //10の位の位置に固定
                        uptiles[i].setVisibility(View.GONE)     //移動したタイルは消す
                    }
                    ten_tile1.setVisibility(View.VISIBLE)       //10のタイルが表示される
                }
            }

            8 -> {      //下が8の時のみ、上に残っているタイルを下に持ってくる
                if(user_click_hintbutton == 1){
                    Sound(4)
                    if(number_up < number_down){
                        for(i in 0..number_up-1){       //残りのタイルを下に移動するアニメーション
                            var uptile_moveY = ObjectAnimator.ofFloat(uptiles[i], "translationY", ScreenType_Animeation("complement", 6))
                            Move_Animetion(uptile_moveY)
                        }
                        Level5_hint(1)
                    }
                    else{
                        Level5_hint(1)
                    }
                }
                else {
                    for(i in 0..number_up-1){
                        uptiles[i].setTranslationY(ScreenType_Animeation("complement", 7))  //戻ってくるとき残っていた上にあったタイルをもとの位置に戻す
                    }
                    button13.setEnabled(true)       //ヒントボタンを押せるようにする
                    Level5_hint(2)
                }
            }

            9 -> {      //アニメーション後、タイルの位置を固定
                if(number_up < number_down){
                    for(i in 0..number_up-1){
                        uptiles[i].setTranslationY(ScreenType_Animeation("complement", 6))
                    }
                }
                button13.setEnabled(false)
            }
        }
    }

    private fun  Level7_hint(user_click_hintbutton: Int){   //引き算レベル1のヒント
        val uptiles = listOf( up_tile1, up_tile2, up_tile3 ,up_tile4 ,up_tile5, up_tile6, up_tile7, up_tile8, up_tile9, up_tile10, up_tile5_2)  //上側のタイルリスト
        val undertiles = listOf( under_tile1, under_tile2, under_tile3, under_tile4, under_tile5, under_tile6, under_tile7, under_tile8, under_tile9, under_tile10, under_tile5_2)  //下側のタイルリスト
        val uptiles_more = listOf( up_tile15_2, up_tile16, up_tile17, up_tile18, up_tile19, up_tile20)

        Auto_HintLevel_plusminus(user_click_hintbutton)     //ヒントボタンが押されたらhint_button_hintlevelを増減させる

        when(hint_button_hintlevel) {    //ヒントの段階に応じてヒントを与える
            0 -> {      //ヒント表示なし
                button12.setEnabled(false)      //戻るボタンを押せなくする
                Tile_Position("delete", "all")   //全てのタイルを画面から消す
            }

            1 -> {      //横にタイルを表示
                if(user_click_hintbutton == 1) Sound(3)
                button12.setEnabled(true)       //戻るボタンを押せるようにする
                Tile_Position("delete", "all")   //全てのタイルを画面から消す
                Tile_Position("visible", "uptile")      //上側の数字分タイルを表示
                Tile_Position("visible", "undertile")    //下側の数字分タイルを表示
                Tile_ColorChange1("start_Minus")  //タイルの色変更
            }

            2 -> {  //10のタイルを右に移動するアニメーション
                if(user_click_hintbutton == 1){
                    Sound(4)
                    var uptile_moveX = ObjectAnimator.ofFloat(ten_tile2, "translationX", ScreenType_Animeation("minus_hint", 1))
                    Move_Animetion(uptile_moveX)
                    Handler().postDelayed({
                        Sound(5)
                        FadeTile(2, ten_tile2)        //10のタイルを消す
                        for(i in 0..5){
                            FadeTile(1, uptiles_more[i])
                        }
                        Level7_hint(1)
                    },1500)

                }
                else {
                    Level7_hint(2)
                }

            }

            3 -> {  //移動したタイルを固定
                ten_tile2.setTranslationX(ScreenType_Animeation("minus_hint", 1))
                ten_tile2.setVisibility(View.GONE)
                for(i in 0..5){
                    uptiles_more[i].setVisibility(View.VISIBLE)
                }
            }

            4 -> {  //１０の塊から９個のタイルを移動させるアニメーション
                if(user_click_hintbutton == 1){
                    Sound(4)
                    for(i in 0..4){
                        var uptile_moveX = ObjectAnimator.ofFloat(uptiles_more[i], "translationX", ScreenType_Animeation("minus_hint", 2))
                        Move_Animetion(uptile_moveX)
                        var uptile_moveY = ObjectAnimator.ofFloat(uptiles_more[i], "translationY", ScreenType_Animeation("minus_hint", 3))
                        Move_Animetion(uptile_moveY)
                    }
                    Handler().postDelayed({
                        Sound(6)
                        for(i in 0..4){
                            FadeTile(2, uptiles_more[i])
                        }
                        for(i in 0..10){
                            FadeTile(2, undertiles[i])
                        }
                        Level7_hint(1)
                    },1500)
                }
                else{
                    Tile_Position("reset", "all")   //全てのタイルを画面から消す
                    Tile_Position("visible", "undertile")    //下側の数字分タイルを表示
                    Level7_hint(2)
                }
            }

            5 -> {
                for(i in 0..4){
                    uptiles_more[i].setTranslationX(ScreenType_Animeation("minus_hint", 2))
                    uptiles_more[i].setTranslationY(ScreenType_Animeation("minus_hint", 3))
                    uptiles_more[i].setVisibility(View.GONE)
                }
                for(i in 0..10){
                    undertiles[i].setVisibility(View.GONE)
                }
            }

            6 -> {
                if(user_click_hintbutton == 1){
                    if(number_total != 5){  //答えが５以外はここでヒントが終わり
                        button11.setEnabled(false)
                    }
                    Sound(4)
                    if((10 - number_down) <= (number_up - 10)){  //10のタイルから９を引いたときに上の１の位のタイルと大きさを比較して１の位が大きいとき
                        Tile_ColorChange1("after_movetile_Lev7")
                        //右側のタイルが下側になる
                        for(i in 0..(number_up - 10 - 1)){
                            var uptile_moveY = ObjectAnimator.ofFloat(uptiles[i], "translationY", ScreenType_Animeation("minus_hint", 4))
                            Move_Animetion(uptile_moveY)
                        }
                        if(5 <= (number_up - 10)){
                            var uptile_moveY = ObjectAnimator.ofFloat(uptiles[10], "translationY", ScreenType_Animeation("minus_hint", 4))
                            Move_Animetion(uptile_moveY)
                        }

                        //10の塊だったタイルは上になる
                        for(i in (6 - (10 - number_down))..5){
                            var uptile_moveY1 = ObjectAnimator.ofFloat(uptiles_more[i], "translationY", ScreenType_Animeation("minus_hint", 5))
                            Move_Animetion(uptile_moveY1)
                            // 793→10のタイルが一番下まで行くピクセル数、　27→タイル１枚の高さ
                        }
                        var uptile_moveX1 = ObjectAnimator.ofFloat(uptiles_more[5], "translationX", ScreenType_Animeation("minus_hint", 2))
                        Move_Animetion(uptile_moveX1)

                        Handler().postDelayed({
                            Level7_hint(1)
                        },1500)
                    }
                    else {
                        Tile_ColorChange1("after_movetile_Lev7")
                        //１０の塊だったタイルは下になる
                        for(i in (6 - (10 - number_down))..5){
                            var uptile_moveY1 = ObjectAnimator.ofFloat(uptiles_more[i], "translationY", ScreenType_Animeation("minus_hint", 6))
                            Move_Animetion(uptile_moveY1)
                            // 793→10のタイルが一番下まで行くピクセル数、　27→タイル１枚の高さ
                        }
                        var uptile_moveX1 = ObjectAnimator.ofFloat(uptiles_more[5], "translationX", ScreenType_Animeation("minus_hint", 2))
                        Move_Animetion(uptile_moveX1)

                        //右側のタイルは上になる
                        for(i in 0..(number_up - 10 - 1)){
                            var uptile_moveY = ObjectAnimator.ofFloat(uptiles[i], "translationY", ScreenType_Animeation("minus_hint", 5))
                            Move_Animetion(uptile_moveY)
                        }
                        if(5 <= (number_up - 10)){
                            var uptile_moveY = ObjectAnimator.ofFloat(uptiles[10], "translationY", ScreenType_Animeation("minus_hint", 4))
                            Move_Animetion(uptile_moveY)
                        }
                        Handler().postDelayed({
                            Level7_hint(1)
                        },1500)
                    }
                }
                else {
                    Tile_Position("reset", "all")   //全てのタイルを画面から消す
                    Tile_ColorChange1("reset_Minus")  //タイルの色変更
                    if(number_total != 5) button11.setEnabled(true)
                    Level7_hint(2)
                }

            }

            7 -> {
                if((10 - number_down) <= (number_up - 10)){  //10のタイルから９を引いたときに上の１の位のタイルと大きさを比較して１の位が大きいとき
                    //右側のタイルが下側になる
                    for(i in 0..(number_up - 10 - 1)){
                        uptiles[i].setTranslationY(ScreenType_Animeation("minus_hint", 4))
                    }
                    if(5 <= (number_up - 10)){
                        uptiles[10].setTranslationY(ScreenType_Animeation("minus_hint", 4))
                    }
                    //10の塊だったタイルは上になる
                    for(i in (6 - (10 - number_down))..5){
                        uptiles_more[i].setTranslationY(ScreenType_Animeation("minus_hint", 5))
                        // 793→10のタイルが一番下まで行くピクセル数、　27→タイル１枚の高さ
                    }
                    uptiles_more[5].setTranslationX(ScreenType_Animeation("minus_hint", 2))
                }
                else {
                    //１０の塊だったタイルは下になる
                    for(i in (6 - (10 - number_down))..5){
                        uptiles_more[i].setTranslationY(ScreenType_Animeation("minus_hint", 6))
                        // 793→10のタイルが一番下まで行くピクセル数、　27→タイル１枚の高さ
                    }
                    uptiles_more[5].setTranslationX(ScreenType_Animeation("minus_hint", 2))

                    //右側のタイルは上になる
                    for(i in 0..(number_up - 10 - 1)){
                        uptiles[i].setTranslationY(ScreenType_Animeation("minus_hint", 5))
                    }
                    if(5 <= (number_up - 10)){
                        uptiles[10].setTranslationY(ScreenType_Animeation("minus_hint", 4))
                    }
                }
            }

            8 -> {
                if(user_click_hintbutton == 1){
                    Sound(5)
                    button11.setEnabled(false)
                    FadeTile(1, under_tile5_3)
                    FadeTile(2, uptiles_more[5])
                    for(i in 0..3){
                        FadeTile(2, uptiles[i])
                    }
                    Level7_hint(1)
                }
                else {
                    button11.setEnabled(true)
                    under_tile5_3.setVisibility(View.GONE)
                    uptiles_more[5].setVisibility(View.VISIBLE)
                    for(i in 0..3) {
                        uptiles[i].setVisibility(View.VISIBLE)
                    }
                    Level7_hint(2)
                }
            }

            9 -> {
                under_tile5_3.setVisibility(View.VISIBLE)
                under_tile5_3.setTranslationY(ScreenType_Animeation("minus_hint", 7))
                uptiles_more[5].setVisibility(View.GONE)
                for(i in 0..3) {
                    uptiles[i].setVisibility(View.GONE)
                }
            }
        }
    }

    private fun  Level8_hint(user_click_hintbutton: Int){   //引き算レベル2のヒント
        val uptiles = listOf( up_tile1, up_tile2, up_tile3 ,up_tile4 ,up_tile5, up_tile6, up_tile7, up_tile8, up_tile9, up_tile10, up_tile5_2)  //上側のタイルリスト
        val undertiles = listOf( under_tile1, under_tile2, under_tile3, under_tile4, under_tile5, under_tile6, under_tile7, under_tile8, under_tile9, under_tile10, under_tile5_2)  //下側のタイルリスト
        val uptiles_more = listOf( up_tile15_2, up_tile16, up_tile17, up_tile18, up_tile19, up_tile20)

        Auto_HintLevel_plusminus(user_click_hintbutton)     //ヒントボタンが押されたらhint_button_hintlevelを増減させる

        when(hint_button_hintlevel) {    //ヒントの段階に応じてヒントを与える
            0 -> {      //ヒント表示なし
                button12.setEnabled(false)      //戻るボタンを押せなくする
                Tile_ColorChange1("reset_Minus")  //タイルの色変更
                Tile_Position("delete", "all")   //全てのタイルを画面から消す
            }

            1 -> {      //横にタイルを表示
                if(user_click_hintbutton == 1) Sound(3)
                button12.setEnabled(true)       //戻るボタンを押せるようにする
                Tile_Position("delete", "all")   //全てのタイルを画面から消す
                Tile_Position("visible", "uptile")      //上側の数字分タイルを表示
                Tile_Position("visible", "undertile")    //下側の数字分タイルを表示
                Tile_ColorChange1("start_Minus")  //タイルの色変更
            }

            2 -> {  //10のタイルを右に移動するアニメーション
                if(user_click_hintbutton == 1){
                    Sound(4)
                    var uptile_moveX = ObjectAnimator.ofFloat(ten_tile2, "translationX", ScreenType_Animeation("minus_hint", 1))
                    Move_Animetion(uptile_moveX)
                    Handler().postDelayed({
                        Sound(5)
                        FadeTile(2, ten_tile2)        //10のタイルを消す
                        for(i in 0..5){
                            FadeTile(1, uptiles_more[i])
                        }
                        Level8_hint(1)
                    },1500)

                }
                else {
                    Level8_hint(2)
                }

            }

            3 -> {  //移動したタイルを固定
                ten_tile2.setTranslationX(ScreenType_Animeation("minus_hint", 1))
                ten_tile2.setVisibility(View.GONE)
                for(i in 0..5){
                    uptiles_more[i].setVisibility(View.VISIBLE)
                }
            }

            4 -> {  //１０の塊から8個のタイルを移動させるアニメーション
                if(user_click_hintbutton == 1){
                    Sound(4)
                    for(i in 0..3){ //８のタイルを右下に移動
                        var uptile_moveX = ObjectAnimator.ofFloat(uptiles_more[i], "translationX", ScreenType_Animeation("minus_hint", 2))
                        Move_Animetion(uptile_moveX)
                        var uptile_moveY = ObjectAnimator.ofFloat(uptiles_more[i], "translationY", ScreenType_Animeation("minus_hint", 3))
                        Move_Animetion(uptile_moveY)
                    }
                    Handler().postDelayed({
                        Sound(6)
                        for(i in 0..3){ //８のタイルを消す
                            FadeTile(2, uptiles_more[i])
                        }
                        for(i in 0..10){    //空白のタイルも消す
                            FadeTile(2, undertiles[i])
                        }
                        Level8_hint(1)
                    },1500)
                }
                else{
                    Tile_Position("reset", "all")   //全てのタイルを画面から消す
                    Tile_Position("visible", "undertile")    //下側の数字分タイルを表示
                    Level8_hint(2)
                }
            }

            5 -> {      //移動したタイルを固定するアニメーション
                for(i in 0..3){
                    uptiles_more[i].setTranslationX(ScreenType_Animeation("minus_hint", 2))
                    uptiles_more[i].setTranslationY(ScreenType_Animeation("minus_hint", 3))
                    uptiles_more[i].setVisibility(View.GONE)
                }
                for(i in 0..10){
                    undertiles[i].setVisibility(View.GONE)
                }
            }

            6 -> {
                if(user_click_hintbutton == 1){
                    if(number_total == 5 || number_total == 6){  //答えが５か６以外はここでヒントが終わり
                        //こっちは何もしない
                    }
                    else {
                        button11.setEnabled(false)
                    }

                    Sound(4)
                    if((10 - number_down) <= (number_up - 10)){  //10のタイルから９を引いたときに上の１の位のタイルと大きさを比較して１の位が大きいとき
                        Tile_ColorChange1("after_movetile_Lev8")
                        //右側のタイルが下側になる
                        for(i in 0..(number_up - 10 - 1)){      //右側のタイルのアニメーション
                            var uptile_moveY = ObjectAnimator.ofFloat(uptiles[i], "translationY", ScreenType_Animeation("minus_hint", 4))
                            Move_Animetion(uptile_moveY)
                        }
                        if(5 <= (number_up - 10)){
                            var uptile_moveY = ObjectAnimator.ofFloat(uptiles[10], "translationY", ScreenType_Animeation("minus_hint", 4))
                            Move_Animetion(uptile_moveY)
                        }

                        //10の塊だったタイルは上になる
                        for(i in (6 - (10 - number_down))..5){
                            var uptile_moveY1 = ObjectAnimator.ofFloat(uptiles_more[i], "translationY", ScreenType_Animeation("minus_hint", 8))
                            Move_Animetion(uptile_moveY1)
                            // 793→10のタイルが一番下まで行くピクセル数、　27→タイル１枚の高さ
                            var uptile_moveX1 = ObjectAnimator.ofFloat(uptiles_more[i], "translationX", ScreenType_Animeation("minus_hint", 2))
                            Move_Animetion(uptile_moveX1)
                        }


                        Handler().postDelayed({
                            Level8_hint(1)
                        },1500)
                    }
                    else {
                        Tile_ColorChange1("after_movetile_Lev8")
                        //１０の塊だったタイルは下になる
                        for(i in (6 - (10 - number_down))..5){
                            var uptile_moveY1 = ObjectAnimator.ofFloat(uptiles_more[i], "translationY", ScreenType_Animeation("minus_hint", 6))
                            Move_Animetion(uptile_moveY1)
                            // 793→10のタイルが一番下まで行くピクセル数、　27→タイル１枚の高さ
                            var uptile_moveX1 = ObjectAnimator.ofFloat(uptiles_more[i], "translationX", ScreenType_Animeation("minus_hint", 2))
                            Move_Animetion(uptile_moveX1)
                        }


                        //右側のタイルは上になる
                        for(i in 0..(number_up - 10 - 1)){
                            var uptile_moveY = ObjectAnimator.ofFloat(uptiles[i], "translationY", ScreenType_Animeation("minus_hint", 9))
                            Move_Animetion(uptile_moveY)
                        }
                        if(5 <= (number_up - 10)){
                            var uptile_moveY = ObjectAnimator.ofFloat(uptiles[10], "translationY", ScreenType_Animeation("minus_hint", 4))
                            Move_Animetion(uptile_moveY)
                        }
                        Handler().postDelayed({
                            Level8_hint(1)
                        },1500)
                    }
                }
                else {
                    Tile_Position("reset", "all")   //全てのタイルを画面から消す
                    Tile_ColorChange1("start_Minus")  //タイルの色変更
                    Tile_ColorChange1("reset_Minus")  //タイルの色変更
                    if(number_total == 5 || number_total == 6) {}
                    else button11.setEnabled(true)
                    Level8_hint(2)
                }

            }

            7 -> {
                if((10 - number_down) <= (number_up - 10)){  //10のタイルから９を引いたときに上の１の位のタイルと大きさを比較して１の位が大きいとき
                    //右側のタイルが下側になる
                    for(i in 0..(number_up - 10 - 1)){
                        uptiles[i].setTranslationY(ScreenType_Animeation("minus_hint", 4))
                    }
                    if(5 <= (number_up - 10)){
                        uptiles[10].setTranslationY(ScreenType_Animeation("minus_hint", 4))
                    }
                    //10の塊だったタイルは上になる
                    for(i in (6 - (10 - number_down))..5){
                        uptiles_more[i].setTranslationY(ScreenType_Animeation("minus_hint", 8))
                        // 793→10のタイルが一番下まで行くピクセル数、　27→タイル１枚の高さ
                    }
                    uptiles_more[5].setTranslationX(ScreenType_Animeation("minus_hint", 2))
                }
                else {
                    //１０の塊だったタイルは下になる
                    for(i in (6 - (10 - number_down))..5){
                        uptiles_more[i].setTranslationY(ScreenType_Animeation("minus_hint", 6))
                        // 793→10のタイルが一番下まで行くピクセル数、　27→タイル１枚の高さ
                    }
                    uptiles_more[5].setTranslationX(ScreenType_Animeation("minus_hint", 2))

                    //右側のタイルは上になる
                    for(i in 0..(number_up - 10 - 1)){
                        uptiles[i].setTranslationY(ScreenType_Animeation("minus_hint", 9))
                    }
                    if(5 <= (number_up - 10)){
                        uptiles[10].setTranslationY(ScreenType_Animeation("minus_hint", 4))
                    }
                }
            }

            8 -> {
                if(user_click_hintbutton == 1){
                    button11.setEnabled(false)
                    Sound(5)
                    FadeTile(1, under_tile5_3)
                    if(number_total == 5) FadeTile(2, uptiles_more[5])  //答えが５なら消す、６なら消さない
                    if(number_total == 6) uptiles_more[5].setBackgroundResource(R.drawable.wakusen2)
                    FadeTile(2, uptiles_more[4])
                    for(i in 0..2){
                        FadeTile(2, uptiles[i])
                    }
                    Level8_hint(1)
                }
                else {
                    button11.setEnabled(true)
                    under_tile5_3.setVisibility(View.GONE)
                    if(number_total == 5) uptiles_more[5].setVisibility(View.VISIBLE)  //答えが５なら消す、６なら消さない
                    if(number_total == 6) uptiles_more[5].setBackgroundResource(R.drawable.wakusen)
                    uptiles_more[4].setVisibility(View.VISIBLE)
                    for(i in 0..2) {
                        uptiles[i].setVisibility(View.VISIBLE)
                    }
                    Level8_hint(2)
                }
            }

            9 -> {
                under_tile5_3.setVisibility(View.VISIBLE)
                under_tile5_3.setTranslationY(ScreenType_Animeation("minus_hint", 7))
                if(number_total == 5) uptiles_more[5].setVisibility(View.GONE)  //答えが５なら消す、６なら消さない
                uptiles_more[4].setVisibility(View.GONE)
                for(i in 0..2) {
                    uptiles[i].setVisibility(View.GONE)
                }
            }
        }
    }

    private fun  Level9_hint(user_click_hintbutton: Int){   //引き算レベル3のヒント
        val uptiles = listOf( up_tile1, up_tile2, up_tile3 ,up_tile4 ,up_tile5, up_tile6, up_tile7, up_tile8, up_tile9, up_tile10, up_tile5_2)  //上側のタイルリスト
        val undertiles = listOf( under_tile1, under_tile2, under_tile3, under_tile4, under_tile5, under_tile6, under_tile7, under_tile8, under_tile9, under_tile10, under_tile5_2)  //下側のタイルリスト
        val uptiles_more = listOf( up_tile15_2, up_tile16, up_tile17, up_tile18, up_tile19, up_tile20)

        Auto_HintLevel_plusminus(user_click_hintbutton)     //ヒントボタンが押されたらhint_button_hintlevelを増減させる

        when(hint_button_hintlevel) {    //ヒントの段階に応じてヒントを与える
            0 -> {      //ヒント表示なし
                button12.setEnabled(false)      //戻るボタンを押せなくする
                Tile_ColorChange1("reset_Minus")  //タイルの色変更
                Tile_Position("delete", "all")   //全てのタイルを画面から消す
            }

            1 -> {      //横にタイルを表示
                if(user_click_hintbutton == 1) Sound(3)
                button12.setEnabled(true)       //戻るボタンを押せるようにする
                Tile_Position("delete", "all")   //全てのタイルを画面から消す
                Tile_Position("visible", "uptile")      //上側の数字分タイルを表示
                Tile_Position("visible", "undertile")    //下側の数字分タイルを表示
                Tile_ColorChange1("start_Minus")  //タイルの色変更
            }

            2 -> {  //10のタイルを右に移動するアニメーション
                if(user_click_hintbutton == 1){
                    Sound(4)
                    var uptile_moveX = ObjectAnimator.ofFloat(ten_tile2, "translationX", ScreenType_Animeation("minus_hint", 1))
                    Move_Animetion(uptile_moveX)
                    Handler().postDelayed({
                        Sound(5)
                        FadeTile(2, ten_tile2)        //10のタイルを消す
                        for(i in 0..5){
                            FadeTile(1, uptiles_more[i])
                        }
                        Level9_hint(1)
                    },1500)

                }
                else {
                    Level9_hint(2)
                }

            }

            3 -> {  //移動したタイルを固定
                ten_tile2.setTranslationX(ScreenType_Animeation("minus_hint", 1))
                ten_tile2.setVisibility(View.GONE)
                for(i in 0..5){
                    uptiles_more[i].setVisibility(View.VISIBLE)
                }
            }

            4 -> {  //１０の塊から7個のタイルを移動させるアニメーション
                if(user_click_hintbutton == 1){
                    Sound(4)
                    for(i in 0..2){ //7のタイルを右下に移動
                        var uptile_moveX = ObjectAnimator.ofFloat(uptiles_more[i], "translationX", ScreenType_Animeation("minus_hint", 2))
                        Move_Animetion(uptile_moveX)
                        var uptile_moveY = ObjectAnimator.ofFloat(uptiles_more[i], "translationY", ScreenType_Animeation("minus_hint", 3))
                        Move_Animetion(uptile_moveY)
                    }
                    Handler().postDelayed({
                        Sound(6)
                        for(i in 0..2){ //８のタイルを消す
                            FadeTile(2, uptiles_more[i])
                        }
                        for(i in 0..10){    //空白のタイルも消す
                            FadeTile(2, undertiles[i])
                        }
                        Level9_hint(1)
                    },1500)
                }
                else{
                    Tile_Position("reset", "all")   //全てのタイルを画面から消す
                    Tile_Position("visible", "undertile")    //下側の数字分タイルを表示
                    Level9_hint(2)
                }
            }

            5 -> {      //移動したタイルを固定するアニメーション
                for(i in 0..2){
                    uptiles_more[i].setTranslationX(ScreenType_Animeation("minus_hint", 2))
                    uptiles_more[i].setTranslationY(ScreenType_Animeation("minus_hint", 3))
                    uptiles_more[i].setVisibility(View.GONE)
                }
                for(i in 0..10){
                    undertiles[i].setVisibility(View.GONE)
                }
            }

            6 -> {
                if(user_click_hintbutton == 1){
                    if(number_total == 5 || number_total == 6 || number_total == 7){  //答えが５か６以外はここでヒントが終わり
                        //こっちは何もしない
                    }
                    else {
                        button11.setEnabled(false)
                    }

                    Sound(4)
                    if((10 - number_down) <= (number_up - 10)){  //10のタイルから９を引いたときに上の１の位のタイルと大きさを比較して１の位が大きいとき
                        Tile_ColorChange1("after_movetile_Lev9")
                        //右側のタイルが下側になる
                        for(i in 0..(number_up - 10 - 1)){      //右側のタイルのアニメーション
                            var uptile_moveY = ObjectAnimator.ofFloat(uptiles[i], "translationY", ScreenType_Animeation("minus_hint", 4))
                            Move_Animetion(uptile_moveY)
                        }
                        if(5 <= (number_up - 10)){
                            var uptile_moveY = ObjectAnimator.ofFloat(uptiles[10], "translationY", ScreenType_Animeation("minus_hint", 4))
                            Move_Animetion(uptile_moveY)
                        }

                        //10の塊だったタイルは上になる
                        for(i in (6 - (10 - number_down))..5){
                            var uptile_moveY1 = ObjectAnimator.ofFloat(uptiles_more[i], "translationY", ScreenType_Animeation("minus_hint", 10))
                            Move_Animetion(uptile_moveY1)
                            // 793→10のタイルが一番下まで行くピクセル数、　27→タイル１枚の高さ
                            var uptile_moveX1 = ObjectAnimator.ofFloat(uptiles_more[i], "translationX", ScreenType_Animeation("minus_hint", 2))
                            Move_Animetion(uptile_moveX1)
                        }


                        Handler().postDelayed({
                            Level9_hint(1)
                        },1500)
                    }
                    else {
                        Tile_ColorChange1("after_movetile_Lev9")
                        //１０の塊だったタイルは下になる
                        for(i in (6 - (10 - number_down))..5){
                            var uptile_moveY1 = ObjectAnimator.ofFloat(uptiles_more[i], "translationY", ScreenType_Animeation("minus_hint", 6))
                            Move_Animetion(uptile_moveY1)
                            // 793→10のタイルが一番下まで行くピクセル数、　27→タイル１枚の高さ
                            var uptile_moveX1 = ObjectAnimator.ofFloat(uptiles_more[i], "translationX", ScreenType_Animeation("minus_hint", 2))
                            Move_Animetion(uptile_moveX1)
                        }


                        //右側のタイルは上になる
                        for(i in 0..(number_up - 10 - 1)){
                            var uptile_moveY = ObjectAnimator.ofFloat(uptiles[i], "translationY", ScreenType_Animeation("minus_hint", 11))
                            Move_Animetion(uptile_moveY)
                        }
                        if(5 <= (number_up - 10)){
                            var uptile_moveY = ObjectAnimator.ofFloat(uptiles[10], "translationY", ScreenType_Animeation("minus_hint", 4))
                            Move_Animetion(uptile_moveY)
                        }
                        Handler().postDelayed({
                            Level9_hint(1)
                        },1500)
                    }
                }
                else {
                    Tile_Position("reset", "all")   //全てのタイルを画面から消す
                    Tile_ColorChange1("start_Minus")  //タイルの色変更
                    Tile_ColorChange1("reset_Minus")  //タイルの色変更
                    if(number_total == 5 || number_total == 6 || number_total == 7) {}
                    else button11.setEnabled(true)
                    Level9_hint(2)
                }

            }

            7 -> {
                if((10 - number_down) <= (number_up - 10)){  //10のタイルから９を引いたときに上の１の位のタイルと大きさを比較して１の位が大きいとき
                    //右側のタイルが下側になる
                    for(i in 0..(number_up - 10 - 1)){
                        uptiles[i].setTranslationY(ScreenType_Animeation("minus_hint", 4))
                    }
                    if(5 <= (number_up - 10)){
                        uptiles[10].setTranslationY(ScreenType_Animeation("minus_hint", 4))
                    }
                    //10の塊だったタイルは上になる
                    for(i in (6 - (10 - number_down))..5){
                        uptiles_more[i].setTranslationY(ScreenType_Animeation("minus_hint", 10))
                        // 793→10のタイルが一番下まで行くピクセル数、　27→タイル１枚の高さ
                    }
                    uptiles_more[5].setTranslationX(ScreenType_Animeation("minus_hint", 2))
                }
                else {
                    //１０の塊だったタイルは下になる
                    for(i in (6 - (10 - number_down))..5){
                        uptiles_more[i].setTranslationY(ScreenType_Animeation("minus_hint", 6))
                        // 793→10のタイルが一番下まで行くピクセル数、　27→タイル１枚の高さ
                    }
                    uptiles_more[5].setTranslationX(ScreenType_Animeation("minus_hint", 2))

                    //右側のタイルは上になる
                    for(i in 0..(number_up - 10 - 1)){
                        uptiles[i].setTranslationY(ScreenType_Animeation("minus_hint", 11))
                    }
                    if(5 <= (number_up - 10)){
                        uptiles[10].setTranslationY(ScreenType_Animeation("minus_hint", 4))
                    }
                }
            }

            8 -> {
                if(user_click_hintbutton == 1){
                    Sound(5)
                    button11.setEnabled(false)
                    FadeTile(1, under_tile5_3)
                    when(number_total) {
                        5 -> {  //答えが５なら残ったタイルはすべて消す
                            FadeTile(2, uptiles_more[5])
                            FadeTile(2, uptiles_more[4])
                            FadeTile(2, uptiles_more[3])
                        }
                        6 -> { //答えが6なら上の一つのタイル以外消す
                            uptiles_more[5].setBackgroundResource(R.drawable.wakusen2)
                            FadeTile(2, uptiles_more[4])
                            FadeTile(2, uptiles_more[3])

                        }
                        7 -> {//答えが7なら下の一つのタイルを消す
                            uptiles_more[5].setBackgroundResource(R.drawable.wakusen)
                            uptiles_more[4].setBackgroundResource(R.drawable.wakusen2)
                            FadeTile(2, uptiles_more[3])
                        }
                    }
                    for(i in 0..1){
                        FadeTile(2, uptiles[i])
                    }
                    Level9_hint(1)
                }
                else {
                    button11.setEnabled(true)
                    under_tile5_3.setVisibility(View.GONE)
                    when(number_total) {
                        5 -> {  //答えが５なら残ったタイルはすべて消す
                            uptiles_more[5].setVisibility(View.VISIBLE)
                            uptiles_more[4].setVisibility(View.VISIBLE)
                            uptiles_more[3].setVisibility(View.VISIBLE)
                        }
                        6 -> { //答えが6なら上の一つのタイル以外消す
                            uptiles_more[5].setBackgroundResource(R.drawable.wakusen)
                            uptiles_more[4].setVisibility(View.VISIBLE)
                            uptiles_more[3].setVisibility(View.VISIBLE)

                        }
                        7 -> {//答えが7なら下の一つのタイルを消す
                            uptiles_more[5].setBackgroundResource(R.drawable.wakusen2)
                            uptiles_more[4].setBackgroundResource(R.drawable.wakusen)
                            uptiles_more[3].setVisibility(View.VISIBLE)
                        }
                    }
                    for(i in 0..1) {
                        uptiles[i].setVisibility(View.VISIBLE)
                    }
                    Level9_hint(2)
                }
            }

            9 -> {
                under_tile5_3.setVisibility(View.VISIBLE)
                under_tile5_3.setTranslationY(ScreenType_Animeation("minus_hint", 7))
                when(number_total) {
                    5 -> {  //答えが５なら残ったタイルはすべて消す
                        uptiles_more[5].setVisibility(View.GONE)
                        uptiles_more[4].setVisibility(View.GONE)
                        uptiles_more[3].setVisibility(View.GONE)
                    }
                    6 -> { //答えが6なら上の一つのタイル以外消す
                        uptiles_more[4].setVisibility(View.GONE)
                        uptiles_more[3].setVisibility(View.GONE)
                    }
                    7 -> {//答えが7なら下の一つのタイルを消す
                        uptiles_more[3].setVisibility(View.GONE)
                    }
                }
                for(i in 0..1) {
                    uptiles[i].setVisibility(View.GONE)
                }
            }
        }
    }

    private fun  Level10_hint(user_click_hintbutton: Int){   //引き算レベル4のヒント
        val uptiles = listOf( up_tile1, up_tile2, up_tile3 ,up_tile4 ,up_tile5, up_tile6, up_tile7, up_tile8, up_tile9, up_tile10, up_tile5_2)  //上側のタイルリスト
        val undertiles = listOf( under_tile1, under_tile2, under_tile3, under_tile4, under_tile5, under_tile6, under_tile7, under_tile8, under_tile9, under_tile10, under_tile5_2)  //下側のタイルリスト
        val uptiles_more = listOf( up_tile15_2, up_tile16, up_tile17, up_tile18, up_tile19, up_tile20)

        Auto_HintLevel_plusminus(user_click_hintbutton)     //ヒントボタンが押されたらhint_button_hintlevelを増減させる

        when(hint_button_hintlevel) {    //ヒントの段階に応じてヒントを与える
            0 -> {      //ヒント表示なし
                button12.setEnabled(false)      //戻るボタンを押せなくする
                Tile_ColorChange1("reset_Minus")  //タイルの色変更
                Tile_Position("delete", "all")   //全てのタイルを画面から消す
            }

            1 -> {      //横にタイルを表示
                if(user_click_hintbutton == 1) Sound(3)
                button12.setEnabled(true)       //戻るボタンを押せるようにする
                Tile_Position("delete", "all")   //全てのタイルを画面から消す
                Tile_Position("visible", "uptile")      //上側の数字分タイルを表示
                Tile_Position("visible", "undertile")    //下側の数字分タイルを表示
                Tile_ColorChange1("start_Minus")  //タイルの色変更
            }

            2 -> {  //10のタイルを右に移動するアニメーション
                if(user_click_hintbutton == 1){
                    Sound(4)
                    var uptile_moveX = ObjectAnimator.ofFloat(ten_tile2, "translationX", ScreenType_Animeation("minus_hint", 1))
                    Move_Animetion(uptile_moveX)
                    Handler().postDelayed({
                        Sound(5)
                        FadeTile(2, ten_tile2)        //10のタイルを消す
                        for(i in 0..5){
                            FadeTile(1, uptiles_more[i])
                        }
                        Level10_hint(1)
                    },1500)

                }
                else {
                    Level10_hint(2)
                }

            }

            3 -> {  //移動したタイルを固定
                ten_tile2.setTranslationX(ScreenType_Animeation("minus_hint", 1))
                ten_tile2.setVisibility(View.GONE)
                for(i in 0..5){
                    uptiles_more[i].setVisibility(View.VISIBLE)
                }
            }

            4 -> {  //１０の塊から5or6個のタイルを移動させるアニメーション
                if(user_click_hintbutton == 1){
                    Sound(4)
                    for(i in 0..(number_down - 5)){ //7のタイルを右下に移動
                        var uptile_moveX = ObjectAnimator.ofFloat(uptiles_more[i], "translationX", ScreenType_Animeation("minus_hint", 2))
                        Move_Animetion(uptile_moveX)
                        var uptile_moveY = ObjectAnimator.ofFloat(uptiles_more[i], "translationY", ScreenType_Animeation("minus_hint", 3))
                        Move_Animetion(uptile_moveY)
                    }
                    Handler().postDelayed({
                        Sound(6)
                        for(i in 0..(number_down - 5)){ //８のタイルを消す
                            FadeTile(2, uptiles_more[i])
                        }
                        for(i in 0..(number_down - 1)){    //空白のタイルも消す
                            FadeTile(2, undertiles[i])
                        }
                        FadeTile(2, undertiles[10])
                        Level10_hint(1)
                    },1500)
                }
                else{
                    Tile_Position("reset", "all")   //全てのタイルを画面から消す
                    Tile_Position("visible", "undertile")    //下側の数字分タイルを表示
                    Level10_hint(2)
                }
            }

            5 -> {      //移動したタイルを固定するアニメーション
                for(i in 0..(number_down - 5)){
                    uptiles_more[i].setTranslationX(ScreenType_Animeation("minus_hint", 2))
                    uptiles_more[i].setTranslationY(ScreenType_Animeation("minus_hint", 3))
                    uptiles_more[i].setVisibility(View.GONE)
                }
                for(i in 0..(number_down - 1)){
                    undertiles[i].setVisibility(View.GONE)
                }
                undertiles[10].setVisibility(View.GONE)
            }

            6 -> {
                if(user_click_hintbutton == 1){
                    when{
                        (number_up == 10) && (number_down == 6) -> button11.setEnabled(false)
                        (number_up == 15) && (number_down == 6) -> button11.setEnabled(false)
                        else -> button11.setEnabled(true)
                    }

                    Sound(4)
                    if(number_up == 15 && number_down == 6){
                        Tile_ColorChange1("after_movetile_Lev10")
                        //右側のタイルが下側になる
                        var uptile_moveY = ObjectAnimator.ofFloat(uptiles[10], "translationY", ScreenType_Animeation("minus_hint", 4))
                        Move_Animetion(uptile_moveY)

                        //10の塊だったタイルは上になる
                        for(i in 2..5){
                            var uptile_moveY1 = ObjectAnimator.ofFloat(uptiles_more[i], "translationY", ScreenType_Animeation("minus_hint", 12))
                            Move_Animetion(uptile_moveY1)
                            // 793→10のタイルが一番下まで行くピクセル数、　27→タイル１枚の高さ
                            var uptile_moveX1 = ObjectAnimator.ofFloat(uptiles_more[i], "translationX", ScreenType_Animeation("minus_hint", 2))
                            Move_Animetion(uptile_moveX1)
                        }

                        Handler().postDelayed({
                            Level10_hint(1)
                        },1500)
                    }
                    else {
                        Tile_ColorChange1("after_movetile_Lev10")
                        //１０の塊だったタイルは下になる
                        for(i in (6 - (10 - number_down))..5){
                            var uptile_moveY1 = ObjectAnimator.ofFloat(uptiles_more[i], "translationY", ScreenType_Animeation("minus_hint", 6))
                            Move_Animetion(uptile_moveY1)
                            // 793→10のタイルが一番下まで行くピクセル数、　27→タイル１枚の高さ
                            var uptile_moveX1 = ObjectAnimator.ofFloat(uptiles_more[i], "translationX", ScreenType_Animeation("minus_hint", 2))
                            Move_Animetion(uptile_moveX1)
                        }
                        //右側のタイルは上になる
                        for(i in 0..(number_up - 10 - 1)){
                            var uptile_moveY = ObjectAnimator.ofFloat(uptiles[i], "translationY", ScreenType_Animeation("minus_hint", 13))
                            Move_Animetion(uptile_moveY)
                        }
                        Handler().postDelayed({
                            Level10_hint(1)
                        },1500)
                    }

                }
                else {
                    Tile_Position("reset", "all")   //全てのタイルを画面から消す
                    Tile_ColorChange1("start_Minus")  //タイルの色変更
                    Tile_ColorChange1("reset_Minus")  //タイルの色変更
                    button11.setEnabled(true)
                    Level10_hint(2)
                }

            }

            7 -> {
                if(number_up == 15 && number_down == 6){
                    uptiles[10].setTranslationY(ScreenType_Animeation("minus_hint", 4))

                    //10の塊だったタイルは上になる
                    for(i in 2..5){
                        uptiles_more[i].setTranslationY(ScreenType_Animeation("minus_hint", 12))
                        // 793→10のタイルが一番下まで行くピクセル数、　27→タイル１枚の高さ
                        uptiles_more[i].setTranslationX(ScreenType_Animeation("minus_hint", 2))
                    }

                }
                else {
                    for(i in (6 - (10 - number_down))..5){
                        uptiles_more[i].setTranslationY(ScreenType_Animeation("minus_hint", 6))
                        uptiles_more[i].setTranslationX(ScreenType_Animeation("minus_hint", 2))
                    }
                    for(i in 0..(number_up - 10 - 1)){
                        uptiles[i].setTranslationY(ScreenType_Animeation("minus_hint", 13))
                    }

                }
            }

            8 -> {
                if(user_click_hintbutton == 1){
                    Sound(5)
                    button11.setEnabled(false)
                    FadeTile(1, under_tile5_3)
                    Tile_ColorChange1("after_movetile_Lev10-2")
                    when(number_down) {
                        6 -> {
                            for(i in 2..5){
                                FadeTile(2, uptiles_more[i])    //左のタイルを４つ消す
                            }
                            FadeTile(2, uptiles[0])    //右のタイルを１つ消す
                        }
                        5 -> {
                            for(i in 1..5){
                                FadeTile(2, uptiles_more[i])    //左のタイルを5つ消す
                            }
                        }
                    }
                    Level10_hint(1)
                }
                else {
                    button11.setEnabled(true)
                    under_tile5_3.setVisibility(View.GONE)
                    Tile_ColorChange1("start_Minus")  //タイルの色変更
                    Tile_ColorChange1("reset_Minus")  //タイルの色変更
                    Tile_ColorChange1("after_movetile_Lev10")
                    when(number_down) {
                        6 -> {
                            for(i in 2..5){
                                uptiles_more[i].setVisibility(View.VISIBLE)
                            }
                            uptiles[0].setVisibility(View.VISIBLE)
                        }
                        5 -> {
                            for(i in 1..5){
                                uptiles_more[i].setVisibility(View.VISIBLE)
                            }
                        }
                    }
                    Level10_hint(2)
                }
            }

            9 -> {
                under_tile5_3.setVisibility(View.VISIBLE)
                under_tile5_3.setTranslationY(ScreenType_Animeation("minus_hint", 7))
                when(number_down) {
                    6 -> {
                        for(i in 2..5){
                            uptiles_more[i].setVisibility(View.GONE)
                        }
                        uptiles[0].setVisibility(View.GONE)
                    }
                    5 -> {
                        for(i in 1..5){
                            uptiles_more[i].setVisibility(View.GONE)
                        }
                    }
                }
            }
        }
    }

    private fun  Level11_hint(user_click_hintbutton: Int){   //引き算レベル5のヒント
        val uptiles = listOf( up_tile1, up_tile2, up_tile3 ,up_tile4 ,up_tile5, up_tile6, up_tile7, up_tile8, up_tile9, up_tile10, up_tile5_2)  //上側のタイルリスト
        val undertiles = listOf( under_tile1, under_tile2, under_tile3, under_tile4, under_tile5, under_tile6, under_tile7, under_tile8, under_tile9, under_tile10, under_tile5_2)  //下側のタイルリスト
        val uptiles_more = listOf( up_tile15_2, up_tile16, up_tile17, up_tile18, up_tile19, up_tile20)

        Auto_HintLevel_plusminus(user_click_hintbutton)     //ヒントボタンが押されたらhint_button_hintlevelを増減させる

        when(hint_button_hintlevel) {    //ヒントの段階に応じてヒントを与える
            0 -> {      //ヒント表示なし
                button12.setEnabled(false)      //戻るボタンを押せなくする
                Tile_ColorChange1("reset_Minus")  //タイルの色変更
                Tile_Position("delete", "all")   //全てのタイルを画面から消す
            }

            1 -> {      //横にタイルを表示
                if(user_click_hintbutton == 1) Sound(3)
                button12.setEnabled(true)       //戻るボタンを押せるようにする
                Tile_Position("delete", "all")   //全てのタイルを画面から消す
                Tile_Position("visible", "uptile")      //上側の数字分タイルを表示
                Tile_Position("visible", "undertile")    //下側の数字分タイルを表示
                Tile_ColorChange1("start_Minus")  //タイルの色変更
            }

            2 -> {  //10のタイルを右に移動するアニメーション
                if(user_click_hintbutton == 1){
                    Sound(4)
                    var uptile_moveX = ObjectAnimator.ofFloat(ten_tile2, "translationX", ScreenType_Animeation("minus_hint", 1))
                    Move_Animetion(uptile_moveX)
                    Handler().postDelayed({
                        Sound(5)
                        FadeTile(2, ten_tile2)        //10のタイルを消す
                        for(i in 0..5){
                            FadeTile(1, uptiles_more[i])
                        }
                        Level11_hint(1)
                    },1500)

                }
                else {
                    Level11_hint(2)
                }

            }

            3 -> {  //移動したタイルを固定
                ten_tile2.setTranslationX(ScreenType_Animeation("minus_hint", 1))
                ten_tile2.setVisibility(View.GONE)
                for(i in 0..5){
                    uptiles_more[i].setVisibility(View.VISIBLE)
                }
            }

            4 -> {  //１０の塊から5or6個のタイルを移動させるアニメーション
                if(user_click_hintbutton == 1){
                    Sound(4)
                    var roop_time = 0
                    while(roop_time < number_down){
                        var uptile_moveX = ObjectAnimator.ofFloat(uptiles_more[5 - roop_time], "translationX", ScreenType_Animeation("minus_hint", 2))
                        Move_Animetion(uptile_moveX)
                        var uptile_moveY = ObjectAnimator.ofFloat(uptiles_more[5 - roop_time], "translationY", ScreenType_Animeation("minus_hint", 14))
                        Move_Animetion(uptile_moveY)
                        //553→左の上から右の下のタイルに移動する量
                        roop_time++
                    }
                    Handler().postDelayed({
                        Sound(6)
                        roop_time = 0
                        while(roop_time < number_down){
                            FadeTile(2, uptiles_more[5 - roop_time])
                            roop_time++
                        }
                        for(i in 0..(number_down - 1)){    //空白のタイルも消す
                            FadeTile(2, undertiles[i])
                        }
                        Level11_hint(1)
                    },1500)
                }
                else{
                    Tile_Position("reset", "all")   //全てのタイルを画面から消す
                    Tile_Position("visible", "undertile")    //下側の数字分タイルを表示
                    Level11_hint(2)
                }
            }

            5 -> {      //移動したタイルを固定するアニメーション
                var roop_time = 0
                while(roop_time < number_down){
                    uptiles_more[5 - roop_time].setTranslationX(ScreenType_Animeation("minus_hint", 2))
                    uptiles_more[5 - roop_time].setTranslationY(ScreenType_Animeation("minus_hint", 14))
                    uptiles_more[5 - roop_time].setVisibility(View.GONE)
                    roop_time++
                }
                for(i in 0..(number_down - 1)){
                    undertiles[i].setVisibility(View.GONE)
                }
            }

            6 -> {
                if(user_click_hintbutton == 1){
                    button11.setEnabled(false)
                    Tile_ColorChange1("after_movetile_Lev11")

                    Sound(4)
                    for(i in 0..(10 - number_down - 5)){
                        var uptile_moveY1 = ObjectAnimator.ofFloat(uptiles_more[i], "translationY", ScreenType_Animeation("minus_hint", 4))
                        Move_Animetion(uptile_moveY1)
                        // 793→10のタイルが一番下まで行くピクセル数、　27→タイル１枚の高さ
                        var uptile_moveX1 = ObjectAnimator.ofFloat(uptiles_more[i], "translationX", ScreenType_Animeation("minus_hint", 2))
                        Move_Animetion(uptile_moveX1)
                    }

                    //右側のタイルは上になる
                    for(i in 0..(number_up - 10 - 1)){
                        var uptile_moveY = ObjectAnimator.ofFloat(uptiles[i], "translationY", ScreenType_Animeation("minus_hint", 13))
                        Move_Animetion(uptile_moveY)
                    }
                    Handler().postDelayed({
                        Level11_hint(1)
                    },1500)

                }
                else {
                    Tile_Position("reset", "all")   //全てのタイルを画面から消す
                    Tile_ColorChange1("start_Minus")  //タイルの色変更
                    Tile_ColorChange1("reset_Minus")  //タイルの色変更
                    button11.setEnabled(true)
                    Level11_hint(2)
                }

            }

            7 -> {
                for(i in 0..(10 - number_down - 5)){
                    uptiles_more[i].setTranslationY(ScreenType_Animeation("minus_hint", 4))
                    uptiles_more[i].setTranslationX(ScreenType_Animeation("minus_hint", 2))
                }
                //右側のタイルは上になる
                for(i in 0..(number_up - 10 - 1)){
                    uptiles[i].setTranslationY(ScreenType_Animeation("minus_hint", 13))
                }
            }

        }
    }

    private fun FadeTile(In_or_Out:Int, Tiles :TextView){     //移動後のタイルを表示/非表示にする、非表示にするときにフェードアウトさせる
        val uptile = listOf(up_tile1, up_tile2, up_tile3, up_tile4, up_tile5, up_tile6, up_tile7, up_tile8, up_tile9, up_tile5_2)   //ヒントタイルの配列(上側)
        val undertile = listOf(under_tile1, under_tile2, under_tile3, under_tile4, under_tile5, under_tile6, under_tile7, under_tile8, under_tile9, under_tile5_2) //ヒントタイルの配列(下側)

        val alphaFadeout = AlphaAnimation(1.0f, 0.0f)   //フェードアウトの設定
        alphaFadeout.duration = 500     //0.5秒後
        //alphaFadeout.fillAfter = true
        val alphaFadein = AlphaAnimation(0.0f, 1.0f)
        alphaFadein.duration = 500
        //alphaFadein.fillAfter = true

        when(In_or_Out){
            1 -> Tiles.startAnimation(alphaFadein)
            2 -> Tiles.startAnimation(alphaFadeout)
        }


    }

    fun Move_Animetion(anime :ObjectAnimator){
        anime.duration = 1250
        anime.repeatCount = 0
        anime.start()
    }

    private fun ScreenType_Animeation(play_mode:String, move_level:Int):Float{
        var moveTileAnime = 0f
        var ScreenSize :String = dpiText.text.toString()

        if(ScreenSize == "720"){    //mdpiアニメーション
            when(play_mode){
                "5_2" -> {
                    when(move_level){
                        1 -> moveTileAnime = -390.0f //1の位から10の位にタイルを動かすX軸のアニメーション
                        2 -> moveTileAnime = 148.0f //1の位から10の位にタイルを動かすuptileのY軸のアニメーション
                        3 -> moveTileAnime = (418 - 27 * (number_down - 5)).toFloat()   //uptileをundertileに合体させるY軸のアニメーション
                        4 -> moveTileAnime = 135.0f //undertileをuptileに合体させるY軸のアニメーション
                    }
                }
                "complement" -> {
                    when(move_level){
                        1 -> moveTileAnime = -27.0f //タイルを一つ分上に動かすY軸のアニメーション
                        2 -> moveTileAnime = ((27 * (number_up - 1)) + 30 + 10).toFloat()   //uptileをundertileに合体させて10を作るY軸のアニメーション
                        3 -> moveTileAnime = ( ( -(10 - number_down) * 27) - 283  ).toFloat()   //undertileをuptileに合体させて10を作るY軸のアニメーション
                        4 -> moveTileAnime = -390.0f //1の位から10の位にタイルを動かすX軸のアニメーション
                        5 -> moveTileAnime = (-(10 - number_down) * 27).toFloat()   //uptileに移動したundertileを10の位の位置に動かすY軸のアニメーション
                        6 -> moveTileAnime = 283.0f //10になったuptileを10の位に動かすX軸のアニメーション
                        7 -> moveTileAnime = 0.0f   //もとの位置に戻す
                    }
                }
                "minus_hint" -> {
                    when(move_level){
                        1 -> moveTileAnime = 140.0f //10の位から1の位に10のタイルを移すX軸のアニメーション
                        2 -> moveTileAnime = 260.0f //引く分のタイルを移動させるX軸のアニメーション
                        3 -> moveTileAnime = 283.0f //引く分のタイルを移動させるY軸のアニメーション
                        4 -> moveTileAnime = 550.0f //上の数字右側が下に来るときのY軸のアニメーション
                        5 -> moveTileAnime = (793 - ((number_up - 10) * 27)).toFloat()  //タイルが上側として合体するY軸のアニメーション(レベル1)
                        6 -> moveTileAnime = (793 - ((10 - number_down - 1) * 27)).toFloat()    //10の塊だったタイルが下側として合体するY軸のアニメーション
                        7 -> moveTileAnime = 267.0f //答えのタイルが5に変化するときに隠してた5のタイルを移動させるY軸のアニメーション

                        8 -> moveTileAnime = (793 - ((number_up - 10 + 1) * 27)).toFloat()  //タイルが上側として合体するY軸のアニメーション(レベル2)
                        9 -> moveTileAnime = (550 - ((number_up - 10 + 1) * 27)).toFloat() //上の数字右側が上に来るときのY軸のアニメーション

                        10 -> moveTileAnime = (793 - ((number_up - 10 + 2) * 27)).toFloat() //タイルが上側として合体するY軸のアニメーション(レベル3)
                        11 -> moveTileAnime = (550 - (3 * 27)).toFloat()    //上の数字右側が上に来るときのY軸のアニメーション

                        12 -> moveTileAnime = (793 - (8 * 27)).toFloat()    //15-6の時に10の塊が下に来るときのY軸のアニメーション
                        13 -> moveTileAnime = (550 - ((10 - number_down) * 27)).toFloat()   //右側のタイルが上に来るY軸のアニメーション

                        14 -> moveTileAnime = (553 - (number_down * 27)).toFloat()  //左上から右下のタイルに移動するY軸のアニメーション
                        15 -> moveTileAnime
                        16 -> moveTileAnime
                        17 -> moveTileAnime
                    }
                }
            }
        }

        return moveTileAnime
    }


    //数字ボタンをクリックしたときの関数
    private fun Click_NumberButton(intent_to_Break: Intent, falsecount_to_Break :Int) {

        answer_total = answer_ten + answer_one      //使用者が入力した答え

        if (number_total == answer_total) {       //解答が正しい
            Sound(1)
            success_count += 1      //正解数を＋１
            when(success_count){        //画面上の丸の〇を●に変える
                1 -> suc_count1.setImageResource(R.drawable.circle2)
                2 -> suc_count2.setImageResource(R.drawable.circle2)
                3 -> suc_count3.setImageResource(R.drawable.circle2)
                4 -> suc_count4.setImageResource(R.drawable.circle2)
                5 -> suc_count5.setImageResource(R.drawable.circle2)
                6 -> {
                    suc_count6.setImageResource(R.drawable.circle2)
                    Handler().postDelayed({
                        intent_to_Break.putExtra("LevelCalc_Math_Break", select_level)
                        intent_to_Break.putExtra("NowLevel_Math_Break", now_level)
                        intent_to_Break.putExtra("ClearTime_Math_Break", Time_fun(timer_start, time_past))
                        intent_to_Break.putExtra("FalseAns_Math_Break", falsecount_to_Break + false_count)
                        startActivity(intent_to_Break)
                    }, 1000)
                }
            }

            if(success_count <= 5){     //正解数5問以下は新しい問題に進む
                Handler().postDelayed({
                    Display_New_Question()          //新しい問題にする
                    on_numberbutton = true      //数字ボタンの入力を許可
                    Answer_delete()                 //解答欄をクリアにする
                    Tile_Position("delete", "all")   //全てのタイルを画面から消す
                }, 1100)
            }


        } else {       //解答が間違い
            Sound(2)
            Handler().postDelayed({
                on_numberbutton = true      //数字ボタンの入力を許可
                false_count += 1            //まちがいカウントを＋１
            }, 100)
        }
    }

    //解答をリセットする関数
    fun Answer_delete(){
        answer_one = 0      //一の位の答えを初期値に戻す
        answer_ten = 0      //十の位の答えを初期値に戻す
        answer_total = 0    //解答を初期値にする
        Number_total_one.text = null    //解答欄をきれいに
        Number_total_ten.text = null    //解答欄をきれいに
        on_one_button = false
        on_ten_button = false

        hint_button_hintlevel = 0
        Tile_Position("delete", "all")   //全てのタイルを画面から消す
        button12.setEnabled(false)
    }


    //問題を表示する関数
    fun Display_New_Question (){

        Number_up_down_level = create_question.Level_Confirm(select_level)      //問題を作る

        number_up = Number_up_down_level.first          //上の数字
        number_down = Number_up_down_level.second      //下の数字
        number_question = Number_up_down_level.third   //作った問題のレベル
        when(select_calculate){     //足し算引き算で答え
            1 -> number_total = number_up + number_down          //問題の答え
            2 -> number_total = number_up - number_down          //問題の答え
        }

        when(select_calculate){                         //上の十の位の数字を表示
            1 -> {
                Number_up_ten.text = null
                Number_up_one.text = number_up.toString()           //上の一の位の数字を表示
                Number_down_one.text = number_down.toString()       //下の一の位の数字を表示
            }
            2 -> {
                Number_up_ten.text = "1"
                Number_up_one.text = (number_up - 10).toString()           //上の一の位の数字を表示
                Number_down_one.text = number_down.toString()       //下の一の位の数字を表示
            }
        }

        Put_Button_11_13_Flag()
    }


    //足し算か引き算かを判断する関数
    fun Calculate_Judgment(now_calculate: Int){
        when (now_calculate) {                  //titletext_from_Monsterが６以下なら足し算、７以上なら引き算
            1, 2, 3, 4, 5, 6 -> {
                select_calculate = 1
                Number_plus.text = "＋"
                button11.setText(R.string.hint_text2)       //足し算の時はヒントの表示が変わる
                button13.setVisibility(View.VISIBLE)        //ヒントボタン2つ目を表示
            }
            7, 8, 9, 10, 11, 12 -> {
                select_calculate = 2
                Number_plus.text = "－"
                button11.setText(R.string.hint_text)        //引き算の時はヒントの表示が変わる
                button13.setVisibility(View.GONE)       //ヒントボタン2つ目を消す
            }
        }
    }

    //レベルミックス時に何レベルをやってるか判断する関数
    private fun LevelMix_Judgment(
        titletext_from_monster: Int,
        nowlevel_from_monster: Int
    ): Int {            //別ページから受け取った数字をひとまとめにする

        var answer_titletext = 0                                   //まとめた値を入れる
        when (titletext_from_monster) {
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
    private fun Time_fun(t_start: Long, t_past: Long): Long {
        val t_end = System.currentTimeMillis()          //終了時刻を計測
        var t_total = (t_end - t_start) / 1000            //終了時刻 - 開始時刻でかかった時間がわかる
        t_total += t_past                                      //これまでのレベル分の時間を足す
        return t_total
    }

    override fun onPause() {
        soundPool.release ()
        super.onPause()
    }
}

