package kr.syeyoung.dungeonsguide.whosonline.api.messages.client

import lombok.AllArgsConstructor
import lombok.Data

@Data
@AllArgsConstructor
class C01IsOnline(val c: OBJ) {
    val t = "/is_online"


    data class OBJ(val uuid: String?, val nonce: String?)

}