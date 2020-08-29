package com.example.fura.mathmonster_carry

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import kotlinx.android.synthetic.main.activity_math.*

class MathActivity : AppCompatActivity() {

    var create_question = CreateQuestion()      //クラス宣言
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

    var Number_up_down_level = Triple(0, 0, 0)     //(上側の数字, 下側の数字, 問題のレベル)が入る
    var number_up = 0       //上側の数字が入る
    var number_down = 0    //下側の数字が入る
    var number_question = 0      //問題のレベルが入る
    var number_total = 0        //問題の答えが入る

    var answer_one = 0      //一の位の答えが入る
    var answer_ten = 0      //十の位の答えが入る
    var answer_total = 0    //入力した解答

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


        count_time2.text = timerpast_from_Monster.toString()


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


        button0.setOnClickListener {
            if (on_numberbutton == true) {        //ボタン入力フラグが立っている
                when (on_ten_button) {        //すでに+10のボタンが押されているか

                    //+10が押されている
                    true -> {
                        on_numberbutton = false       //数字ボタンを押せなくする
                        on_one_button = true    //1の位は入力されたフラグ
                        answer_one = 0          //一の位の数字は0
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
                        answer_one = 1          //一の位の数字は１
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
                        answer_one = 2          //一の位の数字は2
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
                        answer_one = 3          //一の位の数字は３
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
                        answer_one = 4          //一の位の数字は4
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
                        answer_one = 5          //一の位の数字は5
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
            intent_to_Break.putExtra("LevelCalc_Math_Break", titletext_from_Monster)
            intent_to_Break.putExtra("NowLevel_Math_Break", nowlevel_from_Monster)
            intent_to_Break.putExtra(
                "ClearTime_Math_Break",
                Time_fun(timer_start, timerpast_from_Monster)
            )
            startActivity(intent_to_Break)
        }

        backBTN.setOnClickListener {
            intent_to_Level.putExtra(
                "LevelCalc_Math_Level",
                titletext_from_Monster
            )                //戻るボタンを押すとレベル選択に戻る
            startActivity(intent_to_Level)
        }
    }


    //数字ボタンをクリックしたときの関数
    fun Click_NumberButton(intent_to_Break: Intent, falsecount_to_Break :Int) {

        answer_total = answer_ten + answer_one      //使用者が入力した答え

        if (number_total == answer_total) {       //解答が正しい
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
                }, 1100)
            }


            textView4.text = "As_" + answer_total


        } else {       //解答が間違い
            Handler().postDelayed({
                on_numberbutton = true      //数字ボタンの入力を許可
                false_count += 1            //まちがいカウントを＋１
            }, 100)
            textView4.text = "Af_" + answer_total
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
        textView3.text = "Q_" + number_total
    }


    //足し算か引き算かを判断する関数
    fun Calculate_Judgment(now_calculate: Int){
        when (now_calculate) {                  //titletext_from_Monsterが６以下なら足し算、７以上なら引き算
            1, 2, 3, 4, 5, 6 -> {
                select_calculate = 1
                Number_plus.text = "＋"
            }
            7, 8, 9, 10, 11, 12 -> {
                select_calculate = 2
                Number_plus.text = "－"
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
}

