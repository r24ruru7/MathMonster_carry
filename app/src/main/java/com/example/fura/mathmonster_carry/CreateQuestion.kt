package com.example.fura.mathmonster_carry

import kotlinx.android.synthetic.main.activity_math.*


class CreateQuestion {

    var up_number = 0
    var down_number = 0
    var question_number = 0
    var before_up_number = 0
    var before_down_number = 0


    //レベルごとの作成関数に飛んで問題(上と下の数字)を返す関数
    fun Level_Confirm(now_level :Int) :Triple<Int, Int, Int>{     //足し算or引き算、現在のレベルのすうじ１～１２をもらう

        when (now_level) {
            1 -> Plus_Question_01()
            2 -> Plus_Question_02()
            3 -> Plus_Question_03()
            4 -> Plus_Question_04()
            5 -> Plus_Question_05()
            6 -> Plus_Question_06()
            7 -> Minus_Question_01()
            8 -> Minus_Question_02()
            9 -> Minus_Question_03()
            10 -> Minus_Question_04()
            11 -> Minus_Question_05()
            12 -> Minus_Question_06()
        }

        return Triple(up_number, down_number, question_number)
    }


    //足し算問題作成後、グローバル関数に代入する関数
    fun Create_Plus_Finish(num_up :Int, num_down :Int){
        up_number = num_up          //今回の問題(上側)
        down_number = num_down      //今回の問題(下側)

        before_up_number = num_up       //次の問題用、前回の問題として使う
        before_down_number = num_down   //次の問題用、前回の問題として使う
    }


    //引き算問題作成後、グローバル関数に代入する関数
    fun Create_Minus_Finish(num_up :Int, num_down :Int){
        up_number = num_up          //今回の問題(上側)
        down_number = num_down      //今回の問題(下側)

        before_up_number = num_up       //次の問題用、前回の問題として使う
        before_down_number = num_down   //次の問題用、前回の問題として使う
    }


    //足し算レベル１の問題作成関数
    fun Plus_Question_01(){

        //["5+5", "5+6", "5+7", "6+5", "6+6", "6+7", "7+5", "7+6", "7+7"] 足し算レベル1
        var num_up = 0      //上の数字
        var num_down = 0    //下の数字

        question_number = 1 //作成した問題のレベル

        do{
            num_up = (5..7).random()        //５～７の範囲
            num_down = (5..7).random()      //５～７の範囲
        }
        while (num_up == before_up_number && num_down == before_down_number)    //前回と問題が同じになったらもう一回作り直す

        Create_Plus_Finish(num_up, num_down)
    }


    //足し算レベル２の問題作成関数
    fun Plus_Question_02(){

        //["9+1", "9+2", "9+3", "9+4", "1+9", "2+9", "3+9", "4+9"] 足し算レベル2
        var num_up = 0      //上の数字
        var num_down = 0    //下の数字
        var select_09 = (1..2).random()

        question_number = 2 //作成した問題のレベル

        do{
            when(select_09){    //１なら上に９が入る、２なら下に９がはいる
                1 -> {
                    num_up = 9
                    num_down = (1..4).random()
                }
                2 -> {
                    num_up = (1..4).random()
                    num_down = 9
                }
            }
        }
        while (num_up == before_up_number && num_down == before_down_number)    //前回と問題が同じになったらもう一回作り直す

        Create_Plus_Finish(num_up, num_down)
    }


    //足し算レベル３の問題作成関数
    fun Plus_Question_03(){

        //["8+2", "8+3", "8+4", "2+8", "3+8", "4+8"] 足し算レベル3
        var num_up = 0      //上の数字
        var num_down = 0    //下の数字
        var select_08 = (1..2).random()

        question_number = 3 //作成した問題のレベル

        do{
            when(select_08){    //１なら上に8が入る、２なら下に8がはいる
                1 -> {
                    num_up = 8
                    num_down = (2..4).random()
                }
                2 -> {
                    num_up = (2..4).random()
                    num_down = 8
                }
            }
        }
        while (num_up == before_up_number && num_down == before_down_number)    //前回と問題が同じになったらもう一回作り直す

        Create_Plus_Finish(num_up, num_down)

    }


    //足し算レベル４の問題作成関数
    fun Plus_Question_04(){

        //["9+5", "9+6", "9+7", "9+8", "9+9", "5+9", "6+9", "7+9", "8+9", "8+5", "8+6", "8+7", "8+8", "5+8", "6+8", "7+8"] 足し算レベル4
        var num_up = 0      //上の数字
        var num_down = 0    //下の数字
        var select_high = (1..2).random()

        question_number = 4 //作成した問題のレベル

        do{
            when(select_high){    //１なら上に高い数字が入る、２なら下に高い数字がはいる
                1 -> {
                    num_up = (8..9).random()
                    num_down = (5..9).random()
                }
                2 -> {
                    num_up = (5..9).random()
                    num_down = (8..9).random()
                }
            }
        }
        while (num_up == before_up_number && num_down == before_down_number)    //前回と問題が同じになったらもう一回作り直す

        Create_Plus_Finish(num_up, num_down)
    }


    //足し算レベル５の問題作成関数
    fun Plus_Question_05(){

        //["6+4", "7+3", "7+4", "3+7", "4+6", "4+7"] 足し算レベル5
        var num_up = 0      //上の数字
        var num_down = 0    //下の数字
        var select_question = (1..6).random()

        question_number = 5 //作成した問題のレベル

        do{
            when(select_question){    //問題数が少ないから選択式に
                1 -> {
                    num_up = 6
                    num_down = 4
                }
                2 -> {
                    num_up = 7
                    num_down = 3
                }
                3 -> {
                    num_up = 7
                    num_down = 4
                }
                4 -> {
                    num_up = 3
                    num_down = 7
                }
                5 -> {
                    num_up = 4
                    num_down = 6
                }
                6 -> {
                    num_up = 4
                    num_down = 7
                }
            }
        }
        while (num_up == before_up_number && num_down == before_down_number)    //前回と問題が同じになったらもう一回作り直す

        Create_Plus_Finish(num_up, num_down)
    }


    //足し算レベルミックスの問題作成関数
    fun Plus_Question_06(){
        var random_question = (1..5).random()
        when(random_question){
            1 -> Plus_Question_01()
            2 -> Plus_Question_02()
            3 -> Plus_Question_03()
            4 -> Plus_Question_04()
            5 -> Plus_Question_05()
        }
    }


    //引き算レベル１の問題作成関数
    fun Minus_Question_01(){

        //["10-9", "11-9", "12-9", "13-9", "14-9", "15-9", "16-9", "17-9", "18-9"], //引き算レベル1
        var num_up = 0      //上の数字
        var num_down = 9    //下の数字

        question_number = 7 //作成した問題のレベル

        do{
            num_up = (10..18).random()
        }
        while (num_up == before_up_number && num_down == before_down_number)    //前回と問題が同じになったらもう一回作り直す

        Create_Minus_Finish(num_up, num_down)
    }


    //引き算レベル２の問題作成関数
    fun Minus_Question_02(){

        //["10-8", "11-8", "12-8", "13-8", "14-8", "15-8", "16-8", "17-8"], //引き算レベル2
        var num_up = 0      //上の数字
        var num_down = 8    //下の数字

        question_number = 8 //作成した問題のレベル

        do{
            num_up = (10..17).random()
        }
        while (num_up == before_up_number && num_down == before_down_number)    //前回と問題が同じになったらもう一回作り直す

        Create_Minus_Finish(num_up, num_down)

    }


    //引き算レベル３の問題作成関数
    fun Minus_Question_03(){

        //["10-7", "11-7", "12-7", "13-7", "14-7", "15-7", "16-7"], //引き算レベル3
        var num_up = 0      //上の数字
        var num_down = 7    //下の数字

        question_number = 9 //作成した問題のレベル

        do{
            num_up = (10..16).random()
        }
        while (num_up == before_up_number && num_down == before_down_number)    //前回と問題が同じになったらもう一回作り直す

        Create_Minus_Finish(num_up, num_down)
    }


    //引き算レベル４の問題作成関数
    fun Minus_Question_04(){

        // ["10-6", "11-6", "12-6", "13-6", "14-6", "15-6", "10-5", "11-5", "12-5", "13-5", "14-5" ] 引き算レベル4
        var num_up = 0      //上の数字
        var num_down = 0    //下の数字
        var select_question = (1..2).random()

        question_number = 10 //作成した問題のレベル

        do{
            when(select_question){
                1 -> {
                    num_up = (10..15).random()
                    num_down = 6
                }
                2 -> {
                    num_up = (10..14).random()
                    num_down = 5
                }
            }
        }
        while (num_up == before_up_number && num_down == before_down_number)    //前回と問題が同じになったらもう一回作り直す

        Create_Minus_Finish(num_up, num_down)
    }


    //引き算レベル５の問題作成関数
    fun Minus_Question_05(){

        //["10-4", "11-4", "12-4", "13-4", "10-3", "11-3", "12-3", "10-2", "11-2", "10-1"] 引き算レベル5
        var num_up = 0      //上の数字
        var num_down = 0    //下の数字

        question_number = 11 //作成した問題のレベル

        do{
            num_down = (1..4).random()
            when(num_down){
                1 -> num_up = 10
                2 -> num_up = (10..11).random()
                3 -> num_up = (10..12).random()
                4 -> num_up = (10..13).random()
            }
        }
        while (num_up == before_up_number && num_down == before_down_number)    //前回と問題が同じになったらもう一回作り直す

        Create_Minus_Finish(num_up, num_down)
    }


    //引き算レベルミックスの問題作成関数
    fun Minus_Question_06(){
        var random_question = (1..5).random()
        when(random_question){
            1 -> Minus_Question_01()
            2 -> Minus_Question_02()
            3 -> Minus_Question_03()
            4 -> Minus_Question_04()
            5 -> Minus_Question_05()
        }
    }
}