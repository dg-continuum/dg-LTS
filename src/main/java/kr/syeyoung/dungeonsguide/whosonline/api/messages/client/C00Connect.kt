package kr.syeyoung.dungeonsguide.whosonline.api.messages.client

import lombok.Data

@Data
class C00Connect(val c: OBJ) {
    val t:String = "/connect"
    class OBJ(val server_id:String, val username:String)
}