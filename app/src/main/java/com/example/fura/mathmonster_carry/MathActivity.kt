package com.example.fura.mathmonster_carry

import android.animation.ObjectAnimator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.TextView
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
            Auto_LevelHint(2)

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
        count_time2.text = number_question.toString()
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
        }

    }


    //タイルの位置を表示/リセット/色変更する関数
    private fun Tile_Position(select_mode :String, select_tile :String){
        val alltile = listOf(
            up_tile1, up_tile2, up_tile3 ,up_tile4 ,up_tile5,
            up_tile6, up_tile7, up_tile8, up_tile9, up_tile10, up_tile5_2,
            under_tile1, under_tile2, under_tile3, under_tile4, under_tile5,
            under_tile6, under_tile7, under_tile8, under_tile9, under_tile10, under_tile5_2,
            ten_tile1, under_tile5_3
        )       //使用しているタイルすべて(uptile = 0 ～ 10, undertile = 11 ～ 21, tentile = 22)

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
                for(i in 1..24){
                    alltile[i-1].setVisibility(View.GONE)       //画面から消す
                    alltile[i-1].setTranslationX(0.0f)          //x軸の初期位置に戻す
                    alltile[i-1].setTranslationY(0.0f)          //y軸の初期位置に戻す
                }
            }

            "reset" -> {    //タイルを元の位置に戻すモード
                for(i in 1..24){
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
                Tile_Position("delete", "all")   //全てのタイルを画面から消す
                Tile_Position("visible", "uptile")      //上側の数字分タイルを表示
                Tile_Position("visible", "undertile")    //下側の数字分タイルを表示
                Tile_ColorChange1("start")  //タイルの色変更
            }

            2 -> {      //5と5のタイルを合体
                if(user_click_hintbutton == 1){ //ヒントボタンを押したとき
                    var uptile52_moveX = ObjectAnimator.ofFloat(uptiles[5], "translationX", -390f)  //uptileのY軸を指定された分だけ下げる
                    Move_Animetion(uptile52_moveX)
                    var uptile52_moveY = ObjectAnimator.ofFloat(uptiles[5], "translationY", 148f)  //uptileのY軸を指定された分だけ下げる
                    Move_Animetion(uptile52_moveY)
                    var undertile52_moveX = ObjectAnimator.ofFloat(undertiles[5], "translationX", -390f)  //undertileのY軸を指定された分だけ下げる
                    Move_Animetion(undertile52_moveX)

                    if(user_click_hintbutton == 1 && number_up == 5 && number_down == 5){       //上下が5の時のみこれでヒント終わり
                        button11.setEnabled(false)      //ヒントを押せなくする
                    }

                    Handler().postDelayed({         //5と5のタイルが10に変化するアニメーション
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
                uptiles[5].setTranslationX(-390f)       //上側5のタイルをX座標を移動
                uptiles[5].setTranslationY(148f)        //上側5のタイルをY座標を移動
                undertiles[5].setTranslationX(-390f)    //下側5のタイルをX座標を移動
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
                    for(i in 6..number_up){     //上側のタイルを下のタイルに合体させるアニメーション
                        var uptile_moveY = ObjectAnimator.ofFloat(uptiles[i], "translationY", (418 - 27 * (number_down - 5)).toFloat())  //uptileのY軸を指定された分だけ下げる
                        Move_Animetion(uptile_moveY)
                    }
                    for(i in 6..number_down){   //下側のタイルを上のタイルに合体させるアニメーション
                        var undertile_moveY = ObjectAnimator.ofFloat(undertiles[i], "translationY", 135f)  //undertileのY軸を指定された分だけ下げる
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
                Tile_Position("delete", "all")   //全てのタイルを画面から消す
                Tile_Position("visible", "uptile")      //上側の数字分タイルを表示
                Tile_Position("visible", "undertile")    //下側の数字分タイルを表示
                Tile_ColorChange1("start")  //タイルの色変更
            }

            2 -> {      //9のタイルに空白のタイルを1つ追加
                if(user_click_hintbutton == 1){
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
                    if(number_up < number_down){    //下が9の時
                        var uptile_moveY = ObjectAnimator.ofFloat(uptiles[number_up - 1], "translationY", -27f)  //上のタイルの一番上にあるタイルを少しずらす
                        Move_Animetion(uptile_moveY)
                    }
                    else {  //上が9の時
                        var undertile_moveY = ObjectAnimator.ofFloat(undertiles[number_down - 1], "translationY", -27f)  //下のタイルの一番上にあるタイルを少しずらす
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
                    uptiles[number_up - 1].setTranslationY(-27f)    //上のタイルの一番上にあるタイルをずらした位置に固定
                }
                else {  //上が9の時
                    undertiles[number_down - 1].setTranslationY(-27f)    //下のタイルの一番上にあるタイルをずらした位置に固定
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
                    if(number_up < number_down){    //下が9の時
                        var uptile_moveY = ObjectAnimator.ofFloat(uptiles[number_up - 1], "translationY", ((27 * (number_up - 1)) + 30 + 10).toFloat())  //タイル配置の間隔27, タイル一つは重なりがないため30, 上と下の隙間10
                        Move_Animetion(uptile_moveY)
                        Tile_ColorChange1("after_movetile_Lev2")     //移動後のタイルの色を黄色にする
                        FadeTile(2, undertiles[10])     //空白のタイルを隠す
                    }
                    else {  //上が9の時
                        var undertile_moveY = ObjectAnimator.ofFloat(undertiles[number_down - 1], "translationY", ( ( -(10 - number_down) * 27) - 283  ).toFloat())  //タイル配置の間隔27, タイル一つは重なりがないため30, 上と下の隙間10
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
                    if(number_total == 10){     //答えが10の時はもうヒントがない
                        button13.setEnabled(false)
                    }
                    if(number_up < number_down){    //下が9の時
                        uptiles[number_up - 1].setTranslationY(((27 * (number_up - 1)) + 30 + 10).toFloat())        //一つだけ動かしたタイルの位置を固定
                        undertiles[10].setVisibility(View.GONE)     //空白のタイルを消す

                        Handler().postDelayed({     //タイル合体後、10の位に移動する
                            var uptile_moveX = ObjectAnimator.ofFloat(uptiles[number_up - 1], "translationX", -390f)    //10の位に移動
                            Move_Animetion(uptile_moveX)
                            for(i in 5..number_down){   //下のタイルは5のタイルと6より上のタイルを動かす
                                var undertile_moveX = ObjectAnimator.ofFloat(undertiles[i], "translationX", -390f)    //10の位に移動
                                Move_Animetion(undertile_moveX)
                            }
                            Handler().postDelayed({     //タイルが10の位に移動後、10のタイルに変化
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
                        undertiles[number_up - 1].setTranslationY(( ( -(10 - number_down) * 27) - 283  ).toFloat())     //一つだけ動かしたタイルの位置を固定
                        uptiles[10].setVisibility(View.GONE)        //空白のタイルを消す

                        Handler().postDelayed({     //タイル合体後、10の位に移動する
                            var undertile_moveX = ObjectAnimator.ofFloat(undertiles[number_down - 1], "translationX", -390f)    //10の位に移動する
                            Move_Animetion(undertile_moveX)
                            var undertile_moveY = ObjectAnimator.ofFloat(undertiles[number_down - 1], "translationY", (-(10 - number_down) * 27).toFloat())     //10の位に移動する
                            Move_Animetion(undertile_moveY)

                            for(i in 5..number_up){     //上のタイルは5のタイルと6より上のタイルが10の位に移動
                                var uptile_moveX = ObjectAnimator.ofFloat(uptiles[i], "translationX", -390f)         //10の位に移動する
                                Move_Animetion(uptile_moveX)
                                var uptile_moveY = ObjectAnimator.ofFloat(uptiles[i], "translationY", 283f)      //10の位に移動する
                                Move_Animetion(uptile_moveY)

                            }

                            Handler().postDelayed({     //10の位に移動後、10のタイルに変化
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
                    Tile_Position("visible", "uptile")      //上側の数字分タイルを表示
                    Tile_Position("visible", "undertile")    //下側の数字分タイルを表示
                    ten_tile1.setVisibility(View.GONE)  //戻ってきたとき10のタイルを表示
                    Level2_hint(2)
                }
            }

            7 -> {      //アニメーション後、タイルの位置を固定
                if(number_up < number_down){    //下が9の時
                    uptiles[number_up - 1].setTranslationX(-390f)       //10の位の位置に固定
                    uptiles[number_up - 1].setVisibility(View.GONE)     //移動したタイルは消す
                    for(i in 5..number_down){
                        undertiles[i].setTranslationX(-390f)       //10の位の位置に固定
                        undertiles[i].setVisibility(View.GONE)     //移動したタイルは消す
                    }
                    ten_tile1.setVisibility(View.VISIBLE)   //10のタイルが表示される
                }
                else {    //上が9の時
                    undertiles[number_down - 1].setTranslationX(-390f)       //10の位の位置に固定
                    undertiles[number_down - 1].setTranslationY((-(10 - number_down) * 27).toFloat())       //10の位の位置に固定
                    undertiles[number_down - 1].setVisibility(View.GONE)        //移動したタイルは消す
                    for(i in 5..number_up){
                        uptiles[i].setTranslationX(-390f)       //10の位の位置に固定
                        uptiles[i].setTranslationY(283f)        //10の位の位置に固定
                        uptiles[i].setVisibility(View.GONE)     //移動したタイルは消す
                    }
                    ten_tile1.setVisibility(View.VISIBLE)       //10のタイルが表示される
                }
            }

            8 -> {      //下が9の時のみ、上に残っているタイルを下に持ってくる
                if(user_click_hintbutton == 1){
                    if(number_up < number_down){
                        for(i in 0..number_up-1){       //残りのタイルを下に移動するアニメーション
                            var uptile_moveY = ObjectAnimator.ofFloat(uptiles[i], "translationY", 283f)
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
                        uptiles[i].setTranslationY(0f)  //戻ってくるとき残っていた上にあったタイルをもとの位置に戻す
                    }
                    button13.setEnabled(true)       //ヒントボタンを押せるようにする
                    Level2_hint(2)
                }
            }

            9 -> {      //アニメーション後、タイルの位置を固定
                if(number_up < number_down){
                    for(i in 0..number_up-1){
                        uptiles[i].setTranslationY(283f)
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
                Tile_Position("delete", "all")   //全てのタイルを画面から消す
                Tile_Position("visible", "uptile")      //上側の数字分タイルを表示
                Tile_Position("visible", "undertile")    //下側の数字分タイルを表示
                Tile_ColorChange1("start")  //タイルの色変更
            }

            2 -> {      //8のタイルに空白のタイルを1つ追加
                if(user_click_hintbutton == 1){
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
                    if(number_up < number_down){    //下が8の時
                        var uptile_moveY1 = ObjectAnimator.ofFloat(uptiles[number_up - 1], "translationY", -27f)  //上のタイルの一番上にあるタイルを少しずらす
                        var uptile_moveY2 = ObjectAnimator.ofFloat(uptiles[number_up - 2], "translationY", -27f)  //上のタイルの二番目に上にあるタイルを少しずらす
                        Move_Animetion(uptile_moveY1)
                        Move_Animetion(uptile_moveY2)
                    }
                    else {  //上が8の時
                        var undertile_moveY1 = ObjectAnimator.ofFloat(undertiles[number_down - 1], "translationY", -27f)  //下のタイルの一番上にあるタイルを少しずらす
                        var undertile_moveY2 = ObjectAnimator.ofFloat(undertiles[number_down - 2], "translationY", -27f)  //下のタイルの二番目に上にあるタイルを少しずらす
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
                    uptiles[number_up - 1].setTranslationY(-27f)    //上のタイルの一番上にあるタイルをずらした位置に固定
                    uptiles[number_up - 2].setTranslationY(-27f)    //上のタイルの二番目に上にあるタイルをずらした位置に固定
                }
                else {  //上が8の時
                    undertiles[number_down - 1].setTranslationY(-27f)    //下のタイルの一番上にあるタイルをずらした位置に固定
                    undertiles[number_down - 2].setTranslationY(-27f)    //下のタイルの二番目に上にあるタイルをずらした位置に固定
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
                    if(number_up < number_down){    //下が8の時
                        var uptile_moveY1 = ObjectAnimator.ofFloat(uptiles[number_up - 1], "translationY", ((27 * (number_up - 1)) + 30 + 10).toFloat())  //タイル配置の間隔27, タイル一つは重なりがないため30, 上と下の隙間10
                        var uptile_moveY2 = ObjectAnimator.ofFloat(uptiles[number_up - 2], "translationY", ((27 * (number_up - 1)) + 30 + 10).toFloat())  //タイル配置の間隔27, タイル一つは重なりがないため30, 上と下の隙間10
                        Move_Animetion(uptile_moveY1)
                        Move_Animetion(uptile_moveY2)
                        Tile_ColorChange1("after_movetile_Lev3")     //移動後のタイルの色を黄色にする
                        FadeTile(2, undertiles[9])     //空白のタイルを隠す
                        FadeTile(2, undertiles[10])     //空白のタイルを隠す
                    }
                    else {  //上が8の時
                        var undertile_moveY1 = ObjectAnimator.ofFloat(undertiles[number_down - 1], "translationY", ( ( -(10 - number_down) * 27) - 283  ).toFloat())  //タイル配置の間隔27, タイル一つは重なりがないため30, 上と下の隙間10
                        var undertile_moveY2 = ObjectAnimator.ofFloat(undertiles[number_down - 2], "translationY", ( ( -(10 - number_down) * 27) - 283  ).toFloat())  //タイル配置の間隔27, タイル一つは重なりがないため30, 上と下の隙間10
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
                        uptiles[number_up - 1].setTranslationY(((27 * (number_up - 1)) + 30 + 10).toFloat())        //一つだけ動かしたタイルの位置を固定
                        uptiles[number_up - 2].setTranslationY(((27 * (number_up - 1)) + 30 + 10).toFloat())        //一つだけ動かしたタイルの位置を固定
                        undertiles[9].setVisibility(View.GONE)
                        undertiles[10].setVisibility(View.GONE)     //空白のタイルを消す

                        Handler().postDelayed({     //タイル合体後、10の位に移動する
                            var uptile_moveX1 = ObjectAnimator.ofFloat(uptiles[number_up - 1], "translationX", -390f)    //10の位に移動
                            var uptile_moveX2 = ObjectAnimator.ofFloat(uptiles[number_up - 2], "translationX", -390f)    //10の位に移動
                            Move_Animetion(uptile_moveX1)
                            Move_Animetion(uptile_moveX2)
                            for(i in 5..number_down){   //下のタイルは5のタイルと6より上のタイルを動かす
                                var undertile_moveX = ObjectAnimator.ofFloat(undertiles[i], "translationX", -390f)    //10の位に移動
                                Move_Animetion(undertile_moveX)
                            }
                            Handler().postDelayed({     //タイルが10の位に移動後、10のタイルに変化
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
                        undertiles[number_up - 1].setTranslationY(( ( -(10 - number_down) * 27) - 283  ).toFloat())     //一つだけ動かしたタイルの位置を固定
                        undertiles[number_up - 2].setTranslationY(( ( -(10 - number_down) * 27) - 283  ).toFloat())     //一つだけ動かしたタイルの位置を固定
                        uptiles[9].setVisibility(View.GONE)        //空白のタイルを消す
                        uptiles[10].setVisibility(View.GONE)        //空白のタイルを消す

                        Handler().postDelayed({     //タイル合体後、10の位に移動する
                            var undertile_moveX1 = ObjectAnimator.ofFloat(undertiles[number_down - 1], "translationX", -390f)    //10の位に移動する
                            var undertile_moveX2 = ObjectAnimator.ofFloat(undertiles[number_down - 2], "translationX", -390f)    //10の位に移動する
                            Move_Animetion(undertile_moveX1)
                            Move_Animetion(undertile_moveX2)
                            var undertile_moveY1 = ObjectAnimator.ofFloat(undertiles[number_down - 1], "translationY", (-(10 - number_down) * 27).toFloat())     //10の位に移動する
                            var undertile_moveY2 = ObjectAnimator.ofFloat(undertiles[number_down - 2], "translationY", (-(10 - number_down) * 27).toFloat())     //10の位に移動する
                            Move_Animetion(undertile_moveY1)
                            Move_Animetion(undertile_moveY2)

                            for(i in 5..number_up){     //上のタイルは5のタイルと6より上のタイルが10の位に移動
                                var uptile_moveX = ObjectAnimator.ofFloat(uptiles[i], "translationX", -390f)         //10の位に移動する
                                Move_Animetion(uptile_moveX)
                                var uptile_moveY = ObjectAnimator.ofFloat(uptiles[i], "translationY", 283f)      //10の位に移動する
                                Move_Animetion(uptile_moveY)

                            }

                            Handler().postDelayed({     //10の位に移動後、10のタイルに変化
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
                    Tile_Position("visible", "uptile")      //上側の数字分タイルを表示
                    Tile_Position("visible", "undertile")    //下側の数字分タイルを表示
                    ten_tile1.setVisibility(View.GONE)  //戻ってきたとき10のタイルを表示
                    Level3_hint(2)
                }
            }

            7 -> {      //アニメーション後、タイルの位置を固定
                if(number_up < number_down){    //下が8の時
                    uptiles[number_up - 1].setTranslationX(-390f)       //10の位の位置に固定
                    uptiles[number_up - 2].setTranslationX(-390f)       //10の位の位置に固定
                    uptiles[number_up - 1].setVisibility(View.GONE)     //移動したタイルは消す
                    uptiles[number_up - 2].setVisibility(View.GONE)     //移動したタイルは消す
                    for(i in 5..number_down){
                        undertiles[i].setTranslationX(-390f)       //10の位の位置に固定
                        undertiles[i].setVisibility(View.GONE)     //移動したタイルは消す
                    }
                    ten_tile1.setVisibility(View.VISIBLE)   //10のタイルが表示される
                }
                else {    //上が8の時
                    undertiles[number_down - 1].setTranslationX(-390f)       //10の位の位置に固定
                    undertiles[number_down - 1].setTranslationY((-(10 - number_down) * 27).toFloat())       //10の位の位置に固定
                    undertiles[number_down - 2].setTranslationX(-390f)       //10の位の位置に固定
                    undertiles[number_down - 2].setTranslationY((-(10 - number_down) * 27).toFloat())       //10の位の位置に固定
                    undertiles[number_down - 1].setVisibility(View.GONE)        //移動したタイルは消す
                    undertiles[number_down - 2].setVisibility(View.GONE)        //移動したタイルは消す
                    for(i in 5..number_up){
                        uptiles[i].setTranslationX(-390f)       //10の位の位置に固定
                        uptiles[i].setTranslationY(283f)        //10の位の位置に固定
                        uptiles[i].setVisibility(View.GONE)     //移動したタイルは消す
                    }
                    ten_tile1.setVisibility(View.VISIBLE)       //10のタイルが表示される
                }
            }

            8 -> {      //下が8の時のみ、上に残っているタイルを下に持ってくる
                if(user_click_hintbutton == 1){
                    if(number_up < number_down){
                        for(i in 0..number_up-1){       //残りのタイルを下に移動するアニメーション
                            var uptile_moveY = ObjectAnimator.ofFloat(uptiles[i], "translationY", 283f)
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
                        uptiles[i].setTranslationY(0f)  //戻ってくるとき残っていた上にあったタイルをもとの位置に戻す
                    }
                    button13.setEnabled(true)       //ヒントボタンを押せるようにする
                    Level3_hint(2)
                }
            }

            9 -> {      //アニメーション後、タイルの位置を固定
                if(number_up < number_down){
                    for(i in 0..number_up-1){
                        uptiles[i].setTranslationY(283f)
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
                            var uptile52_moveX = ObjectAnimator.ofFloat(uptiles[5], "translationX", -390f)  //uptileのY軸を指定された分だけ下げる
                            Move_Animetion(uptile52_moveX)
                            var uptile52_moveY = ObjectAnimator.ofFloat(uptiles[5], "translationY", 148f)  //uptileのY軸を指定された分だけ下げる
                            Move_Animetion(uptile52_moveY)
                            var undertile52_moveX = ObjectAnimator.ofFloat(undertiles[5], "translationX", -390f)  //undertileのY軸を指定された分だけ下げる
                            Move_Animetion(undertile52_moveX)
                             Handler().postDelayed({         //5と5のタイルが10に変化するアニメーション
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
                    10 -> {
                        if(user_click_hintbutton == 1){
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
                        uptiles[5].setTranslationX(-390f)       //上側5のタイルをX座標を移動
                        uptiles[5].setTranslationY(148f)        //上側5のタイルをY座標を移動
                        undertiles[5].setTranslationX(-390f)    //下側5のタイルをX座標を移動
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
                    10 -> {
                        if(number_total < 15){      //どちらかの５のタイルを分解する
                            if(number_up < number_down){    //下が大きいとき
                                if(user_click_hintbutton == 1) FadeTile(2, uptiles[5])
                                else Tile_Position("reset", "all")   //全てのタイルを初期値に戻す
                                uptiles[5].setVisibility(View.GONE)
                                for(i in 0..4){
                                    uptiles[i].setVisibility(View.VISIBLE)
                                }
                            }
                            else {
                                if(user_click_hintbutton == 1) FadeTile(2, undertiles[5])
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
                            Tile_ColorChange1("after_movetile_Lev1")     //移動後のタイルの色を交互にさせる
                            for(i in 6..number_up){     //上側のタイルを下のタイルに合体させるアニメーション
                                var uptile_moveY = ObjectAnimator.ofFloat(uptiles[i], "translationY", (418 - 27 * (number_down - 5)).toFloat())  //uptileのY軸を指定された分だけ下げる
                                Move_Animetion(uptile_moveY)
                            }
                            for(i in 6..number_down){   //下側のタイルを上のタイルに合体させるアニメーション
                                var undertile_moveY = ObjectAnimator.ofFloat(undertiles[i], "translationY", 135f)  //undertileのY軸を指定された分だけ下げる
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
                    10 -> {
                        if(user_click_hintbutton == 1){
                            if(number_up < number_down){    //下が8or9の時
                                if(number_total < 15){  //合計が１５未満の時
                                    when(number_up){    //上の数字が５or6
                                        5 -> {
                                            for(i in (5 - (10 - number_down))..4){
                                                var uptile_moveY = ObjectAnimator.ofFloat(uptiles[i], "translationY", -27f)  //下のタイルの一番上にあるタイルを少しずらす
                                                Move_Animetion(uptile_moveY)
                                            }
                                        }
                                        6 -> {
                                            var uptile_moveY5 = ObjectAnimator.ofFloat(uptiles[4], "translationY", -27f)  //下のタイルの一番上にあるタイルを少しずらす
                                            var uptile_moveY6 = ObjectAnimator.ofFloat(uptiles[6], "translationY", -27f)  //下のタイルの一番上にあるタイルを少しずらす
                                            Move_Animetion(uptile_moveY5)
                                            Move_Animetion(uptile_moveY6)

                                        }
                                    }
                                }
                                else {  //合計が15以上の時
                                    var uptile_moveY1 = ObjectAnimator.ofFloat(uptiles[number_up], "translationY", -27f)  //上のタイルの一番上にあるタイルを少しずらす
                                    Move_Animetion(uptile_moveY1)
                                    if(number_down == 8){   //大きい数字が８の時
                                        var uptile_moveY2 = ObjectAnimator.ofFloat(uptiles[number_up - 1], "translationY", -27f)  //上のタイルの二番目に上にあるタイルを少しずらす
                                        Move_Animetion(uptile_moveY2)
                                    }
                                }
                            }
                            else {  //上が8or9の時
                                if(number_total < 15){  //合計が１５未満の時
                                    when(number_down){    //下の数字が５or6
                                        5 -> {
                                            for(i in (5 - (10 - number_up))..4){
                                                var undertile_moveY = ObjectAnimator.ofFloat(undertiles[i], "translationY", -27f)  //下のタイルの一番上にあるタイルを少しずらす
                                                Move_Animetion(undertile_moveY)
                                            }
                                        }
                                        6 -> {
                                            var undertile_moveY5 = ObjectAnimator.ofFloat(undertiles[4], "translationY", -27f)  //下のタイルの一番上にあるタイルを少しずらす
                                            var undertile_moveY6 = ObjectAnimator.ofFloat(undertiles[6], "translationY", -27f)  //下のタイルの一番上にあるタイルを少しずらす
                                            Move_Animetion(undertile_moveY5)
                                            Move_Animetion(undertile_moveY6)
                                        }
                                    }
                                }
                                else {  //合計が15以上の時
                                    var undertile_moveY1 = ObjectAnimator.ofFloat(undertiles[number_down], "translationY", -27f)  //下のタイルの一番上にあるタイルを少しずらす
                                    Move_Animetion(undertile_moveY1)
                                    if(number_up == 8){   //大きい数字が８の時
                                        var undertile_moveY2 = ObjectAnimator.ofFloat(undertiles[number_down - 1], "translationY", -27f)  //下のタイルの二番目に上にあるタイルを少しずらす
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
                            uptiles[i].setTranslationY((418 - 27 * (number_down - 5)).toFloat())
                        }
                        for(i in 6..number_down){
                            undertiles[i].setTranslationY(135f)
                        }
                        if(number_total < 15){
                            button11.setEnabled(false)
                        }
                    }
                    10 -> {

                    }
                }
            }

            6 -> {
                if(user_click_hintbutton == 1){
                    when(hint_button_select_flag){
                        5 -> {
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
                        10 -> {

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
                    10 -> {

                    }
                }

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
                button12.setEnabled(true)       //戻るボタンを押せるようにする
                Tile_Position("delete", "all")   //全てのタイルを画面から消す
                Tile_Position("visible", "uptile")      //上側の数字分タイルを表示
                Tile_Position("visible", "undertile")    //下側の数字分タイルを表示
                Tile_ColorChange1("start")  //タイルの色変更
            }

            2 -> {      //7のタイルに空白のタイルを1つ追加
                if(user_click_hintbutton == 1){
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
                    if(number_up < number_down){    //下が7の時
                        var uptile_moveY1 = ObjectAnimator.ofFloat(uptiles[number_up - 1], "translationY", -27f)  //上のタイルの一番上にあるタイルを少しずらす
                        var uptile_moveY2 = ObjectAnimator.ofFloat(uptiles[number_up - 2], "translationY", -27f)  //上のタイルの二番目に上にあるタイルを少しずらす
                        var uptile_moveY3 = ObjectAnimator.ofFloat(uptiles[number_up - 3], "translationY", -27f)  //上のタイルの三番目に上にあるタイルを少しずらす
                        Move_Animetion(uptile_moveY1)
                        Move_Animetion(uptile_moveY2)
                        Move_Animetion(uptile_moveY3)
                        if(number_down == 6){   //6の時は4つ目のタイルも
                            var uptile_moveY4 = ObjectAnimator.ofFloat(uptiles[number_up - 4], "translationY", -27f)  //上のタイルの四番目に上にあるタイルを少しずらす
                            Move_Animetion(uptile_moveY4)
                        }
                    }
                    else {  //上が7の時
                        var undertile_moveY1 = ObjectAnimator.ofFloat(undertiles[number_down - 1], "translationY", -27f)  //下のタイルの一番上にあるタイルを少しずらす
                        var undertile_moveY2 = ObjectAnimator.ofFloat(undertiles[number_down - 2], "translationY", -27f)  //下のタイルの二番目に上にあるタイルを少しずらす
                        var undertile_moveY3 = ObjectAnimator.ofFloat(undertiles[number_down - 3], "translationY", -27f)  //下のタイルの三番目に上にあるタイルを少しずらす
                        Move_Animetion(undertile_moveY1)
                        Move_Animetion(undertile_moveY2)
                        Move_Animetion(undertile_moveY3)
                        if(number_up == 6){ //6の時は4つ目のタイルも
                            var undertile_moveY4 = ObjectAnimator.ofFloat(undertiles[number_down - 4], "translationY", -27f)  //下のタイルの四番目に上にあるタイルを少しずらす
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
                    uptiles[number_up - 1].setTranslationY(-27f)    //上のタイルの一番上にあるタイルをずらした位置に固定
                    uptiles[number_up - 2].setTranslationY(-27f)    //上のタイルの二番目にあるタイルをずらした位置に固定
                    uptiles[number_up - 3].setTranslationY(-27f)    //上のタイルの三番目にあるタイルをずらした位置に固定
                    if(number_down == 6){   //6の時は4つ目のタイルも
                        uptiles[number_up - 4].setTranslationY(-27f)    //上のタイルの四番目にあるタイルをずらした位置に固定
                    }
                }
                else {  //上が7の時
                    undertiles[number_down - 1].setTranslationY(-27f)    //下のタイルの一番上にあるタイルをずらした位置に固定
                    undertiles[number_down - 2].setTranslationY(-27f)    //下のタイルの二番目に上にあるタイルをずらした位置に固定
                    undertiles[number_down - 3].setTranslationY(-27f)    //下のタイルの三番目に上にあるタイルをずらした位置に固定
                    if(number_up == 6){ //6の時は4つ目のタイルも
                        undertiles[number_down - 4].setTranslationY(-27f)    //下のタイルの四番目に上にあるタイルをずらした位置に固定
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
                    if(number_up < number_down){    //下が7の時
                        var uptile_moveY1 = ObjectAnimator.ofFloat(uptiles[number_up - 1], "translationY", ((27 * (number_up - 1)) + 30 + 10).toFloat())  //タイル配置の間隔27, タイル一つは重なりがないため30, 上と下の隙間10
                        var uptile_moveY2 = ObjectAnimator.ofFloat(uptiles[number_up - 2], "translationY", ((27 * (number_up - 1)) + 30 + 10).toFloat())  //タイル配置の間隔27, タイル一つは重なりがないため30, 上と下の隙間10
                        var uptile_moveY3 = ObjectAnimator.ofFloat(uptiles[number_up - 3], "translationY", ((27 * (number_up - 1)) + 30 + 10).toFloat())  //タイル配置の間隔27, タイル一つは重なりがないため30, 上と下の隙間10
                        Move_Animetion(uptile_moveY1)
                        Move_Animetion(uptile_moveY2)
                        Move_Animetion(uptile_moveY3)
                        if(number_down == 6){   //6の時は4つ目のタイルも
                            var uptile_moveY4 = ObjectAnimator.ofFloat(uptiles[number_up - 4], "translationY", ((27 * (number_up - 1)) + 30 + 10).toFloat())  //タイル配置の間隔27, タイル一つは重なりがないため30, 上と下の隙間10
                            Move_Animetion(uptile_moveY4)
                            FadeTile(2, undertiles[7])     //空白のタイルを隠す
                        }
                        Tile_ColorChange1("after_movetile_Lev5")     //移動後のタイルの色を黄色にする
                        FadeTile(2, undertiles[8])     //空白のタイルを隠す
                        FadeTile(2, undertiles[9])     //空白のタイルを隠す
                        FadeTile(2, undertiles[10])     //空白のタイルを隠す

                    }
                    else {  //上が7の時
                        var undertile_moveY1 = ObjectAnimator.ofFloat(undertiles[number_down - 1], "translationY", ( ( -(10 - number_down) * 27) - 283  ).toFloat())  //タイル配置の間隔27, タイル一つは重なりがないため30, 上と下の隙間10
                        var undertile_moveY2 = ObjectAnimator.ofFloat(undertiles[number_down - 2], "translationY", ( ( -(10 - number_down) * 27) - 283  ).toFloat())  //タイル配置の間隔27, タイル一つは重なりがないため30, 上と下の隙間10
                        var undertile_moveY3 = ObjectAnimator.ofFloat(undertiles[number_down - 3], "translationY", ( ( -(10 - number_down) * 27) - 283  ).toFloat())  //タイル配置の間隔27, タイル一つは重なりがないため30, 上と下の隙間10
                        // ( ( (10 - number_down) * -27) - 40) - (27 * 9)　下側のタイルの一番上まで移動(10-num-1)　→　上側のタイル１に移動(40)　→　上のタイルの一番上まで移動(27*9)
                        Move_Animetion(undertile_moveY1)
                        Move_Animetion(undertile_moveY2)
                        Move_Animetion(undertile_moveY3)
                        if(number_up == 6){ //6の時は4つ目のタイルも
                            var undertile_moveY4 = ObjectAnimator.ofFloat(undertiles[number_down - 4], "translationY", ( ( -(10 - number_down) * 27) - 283  ).toFloat())  //タイル配置の間隔27, タイル一つは重なりがないため30, 上と下の隙間10
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
                        uptiles[number_up - 1].setTranslationY(((27 * (number_up - 1)) + 30 + 10).toFloat())        //一つだけ動かしたタイルの位置を固定
                        uptiles[number_up - 2].setTranslationY(((27 * (number_up - 1)) + 30 + 10).toFloat())        //一つだけ動かしたタイルの位置を固定
                        uptiles[number_up - 3].setTranslationY(((27 * (number_up - 1)) + 30 + 10).toFloat())        //一つだけ動かしたタイルの位置を固定
                        undertiles[8].setVisibility(View.GONE)
                        undertiles[9].setVisibility(View.GONE)
                        undertiles[10].setVisibility(View.GONE)     //空白のタイルを消す
                        if(number_down == 6){   //6の時は4つ目のタイルも
                            uptiles[number_up - 4].setTranslationY(((27 * (number_up - 1)) + 30 + 10).toFloat())        //一つだけ動かしたタイルの位置を固定
                            undertiles[7].setVisibility(View.GONE)
                        }

                        Handler().postDelayed({     //タイル合体後、10の位に移動する
                            var uptile_moveX1 = ObjectAnimator.ofFloat(uptiles[number_up - 1], "translationX", -390f)    //10の位に移動
                            var uptile_moveX2 = ObjectAnimator.ofFloat(uptiles[number_up - 2], "translationX", -390f)    //10の位に移動
                            var uptile_moveX3 = ObjectAnimator.ofFloat(uptiles[number_up - 3], "translationX", -390f)    //10の位に移動
                            Move_Animetion(uptile_moveX1)
                            Move_Animetion(uptile_moveX2)
                            Move_Animetion(uptile_moveX3)
                            if(number_down == 6){   //6の時は4つ目のタイルも
                                var uptile_moveX4 = ObjectAnimator.ofFloat(uptiles[number_up - 4], "translationX", -390f)    //10の位に移動
                                Move_Animetion(uptile_moveX4)
                            }
                            for(i in 5..number_down){   //下のタイルは5のタイルと6より上のタイルを動かす
                                var undertile_moveX = ObjectAnimator.ofFloat(undertiles[i], "translationX", -390f)    //10の位に移動
                                Move_Animetion(undertile_moveX)
                            }
                            Handler().postDelayed({     //タイルが10の位に移動後、10のタイルに変化
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
                        undertiles[number_up - 1].setTranslationY(( ( -(10 - number_down) * 27) - 283  ).toFloat())     //一つだけ動かしたタイルの位置を固定
                        undertiles[number_up - 2].setTranslationY(( ( -(10 - number_down) * 27) - 283  ).toFloat())     //一つだけ動かしたタイルの位置を固定
                        undertiles[number_up - 3].setTranslationY(( ( -(10 - number_down) * 27) - 283  ).toFloat())     //一つだけ動かしたタイルの位置を固定
                        uptiles[8].setVisibility(View.GONE)        //空白のタイルを消す
                        uptiles[9].setVisibility(View.GONE)        //空白のタイルを消す
                        uptiles[10].setVisibility(View.GONE)        //空白のタイルを消す
                        if(number_up == 6){ //6の時は4つ目のタイルも
                            undertiles[number_up - 4].setTranslationY(( ( -(10 - number_down) * 27) - 283  ).toFloat())     //一つだけ動かしたタイルの位置を固定
                            uptiles[7].setVisibility(View.GONE)        //空白のタイルを消す
                        }

                        Handler().postDelayed({     //タイル合体後、10の位に移動する
                            var undertile_moveX1 = ObjectAnimator.ofFloat(undertiles[number_down - 1], "translationX", -390f)    //10の位に移動する
                            var undertile_moveX2 = ObjectAnimator.ofFloat(undertiles[number_down - 2], "translationX", -390f)    //10の位に移動する
                            var undertile_moveX3 = ObjectAnimator.ofFloat(undertiles[number_down - 3], "translationX", -390f)    //10の位に移動する
                            Move_Animetion(undertile_moveX1)
                            Move_Animetion(undertile_moveX2)
                            Move_Animetion(undertile_moveX3)
                            var undertile_moveY1 = ObjectAnimator.ofFloat(undertiles[number_down - 1], "translationY", (-(10 - number_down) * 27).toFloat())     //10の位に移動する
                            var undertile_moveY2 = ObjectAnimator.ofFloat(undertiles[number_down - 2], "translationY", (-(10 - number_down) * 27).toFloat())     //10の位に移動する
                            var undertile_moveY3 = ObjectAnimator.ofFloat(undertiles[number_down - 3], "translationY", (-(10 - number_down) * 27).toFloat())     //10の位に移動する
                            Move_Animetion(undertile_moveY1)
                            Move_Animetion(undertile_moveY2)
                            Move_Animetion(undertile_moveY3)
                            if(number_up == 6){ //6の時は4つ目のタイルも
                                var undertile_moveX4 = ObjectAnimator.ofFloat(undertiles[number_down - 4], "translationX", -390f)    //10の位に移動する
                                Move_Animetion(undertile_moveX4)
                                var undertile_moveY4 = ObjectAnimator.ofFloat(undertiles[number_down - 4], "translationY", (-(10 - number_down) * 27).toFloat())     //10の位に移動する
                                Move_Animetion(undertile_moveY4)
                            }

                            for(i in 5..number_up){     //上のタイルは5のタイルと6より上のタイルが10の位に移動
                                var uptile_moveX = ObjectAnimator.ofFloat(uptiles[i], "translationX", -390f)         //10の位に移動する
                                Move_Animetion(uptile_moveX)
                                var uptile_moveY = ObjectAnimator.ofFloat(uptiles[i], "translationY", 283f)      //10の位に移動する
                                Move_Animetion(uptile_moveY)

                            }

                            Handler().postDelayed({     //10の位に移動後、10のタイルに変化
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
                    Tile_Position("visible", "uptile")      //上側の数字分タイルを表示
                    Tile_Position("visible", "undertile")    //下側の数字分タイルを表示
                    ten_tile1.setVisibility(View.GONE)  //戻ってきたとき10のタイルを表示
                    Level5_hint(2)
                }
            }

            7 -> {      //アニメーション後、タイルの位置を固定
                if(number_up < number_down){    //下が8の時
                    uptiles[number_up - 1].setTranslationX(-390f)       //10の位の位置に固定
                    uptiles[number_up - 2].setTranslationX(-390f)       //10の位の位置に固定
                    uptiles[number_up - 3].setTranslationX(-390f)       //10の位の位置に固定
                    uptiles[number_up - 1].setVisibility(View.GONE)     //移動したタイルは消す
                    uptiles[number_up - 2].setVisibility(View.GONE)     //移動したタイルは消す
                    uptiles[number_up - 3].setVisibility(View.GONE)     //移動したタイルは消す
                    if(number_down == 6){   //6の時は4つ目のタイルも
                        uptiles[number_up - 4].setTranslationX(-390f)       //10の位の位置に固定
                        uptiles[number_up - 4].setVisibility(View.GONE)     //移動したタイルは消す
                    }
                    for(i in 5..number_down){
                        undertiles[i].setTranslationX(-390f)       //10の位の位置に固定
                        undertiles[i].setVisibility(View.GONE)     //移動したタイルは消す
                    }
                    ten_tile1.setVisibility(View.VISIBLE)   //10のタイルが表示される
                }
                else {    //上が8の時
                    undertiles[number_down - 1].setTranslationX(-390f)       //10の位の位置に固定
                    undertiles[number_down - 1].setTranslationY((-(10 - number_down) * 27).toFloat())       //10の位の位置に固定
                    undertiles[number_down - 2].setTranslationX(-390f)       //10の位の位置に固定
                    undertiles[number_down - 2].setTranslationY((-(10 - number_down) * 27).toFloat())       //10の位の位置に固定
                    undertiles[number_down - 3].setTranslationX(-390f)       //10の位の位置に固定
                    undertiles[number_down - 3].setTranslationY((-(10 - number_down) * 27).toFloat())       //10の位の位置に固定
                    undertiles[number_down - 1].setVisibility(View.GONE)        //移動したタイルは消す
                    undertiles[number_down - 2].setVisibility(View.GONE)        //移動したタイルは消す
                    undertiles[number_down - 3].setVisibility(View.GONE)        //移動したタイルは消す
                    if(number_up == 6){ //6の時は4つ目のタイルも
                        undertiles[number_down - 4].setTranslationX(-390f)       //10の位の位置に固定
                        undertiles[number_down - 4].setTranslationY((-(10 - number_down) * 27).toFloat())       //10の位の位置に固定
                        undertiles[number_down - 4].setVisibility(View.GONE)        //移動したタイルは消す
                    }
                    for(i in 5..number_up){
                        uptiles[i].setTranslationX(-390f)       //10の位の位置に固定
                        uptiles[i].setTranslationY(283f)        //10の位の位置に固定
                        uptiles[i].setVisibility(View.GONE)     //移動したタイルは消す
                    }
                    ten_tile1.setVisibility(View.VISIBLE)       //10のタイルが表示される
                }
            }

            8 -> {      //下が8の時のみ、上に残っているタイルを下に持ってくる
                if(user_click_hintbutton == 1){
                    if(number_up < number_down){
                        for(i in 0..number_up-1){       //残りのタイルを下に移動するアニメーション
                            var uptile_moveY = ObjectAnimator.ofFloat(uptiles[i], "translationY", 283f)
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
                        uptiles[i].setTranslationY(0f)  //戻ってくるとき残っていた上にあったタイルをもとの位置に戻す
                    }
                    button13.setEnabled(true)       //ヒントボタンを押せるようにする
                    Level5_hint(2)
                }
            }

            9 -> {      //アニメーション後、タイルの位置を固定
                if(number_up < number_down){
                    for(i in 0..number_up-1){
                        uptiles[i].setTranslationY(283f)
                    }
                }
                button13.setEnabled(false)
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


    //数字ボタンをクリックしたときの関数
    private fun Click_NumberButton(intent_to_Break: Intent, falsecount_to_Break :Int) {

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
                    Tile_Position("delete", "all")   //全てのタイルを画面から消す
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
        textView3.text = "Q_" + number_total

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
}

