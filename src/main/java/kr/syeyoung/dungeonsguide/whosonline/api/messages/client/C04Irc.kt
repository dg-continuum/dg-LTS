package kr.syeyoung.dungeonsguide.whosonline.api.messages.client

class C04Irc (val t: String = "/irc/create", val c: IRCCLINETMESSAGE) {
    class IRCCLINETMESSAGE (val message: String)
}