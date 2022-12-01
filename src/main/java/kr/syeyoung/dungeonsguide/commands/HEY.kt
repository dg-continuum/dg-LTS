package kr.syeyoung.dungeonsguide.commands

import kr.syeyoung.dungeonsguide.chat.ChatTransmitter

class HEY {
    fun doFunStuff(){
        ChatTransmitter.addToQueue("HEY!!!!!")
        print("HEYYYYYY")
    }
}