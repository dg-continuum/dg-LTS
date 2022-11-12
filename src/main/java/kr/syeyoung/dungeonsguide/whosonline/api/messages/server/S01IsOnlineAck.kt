package kr.syeyoung.dungeonsguide.whosonline.api.messages.server

import kr.syeyoung.dungeonsguide.whosonline.api.messages.AbstractMessage
import lombok.Data

@Data
class S01IsOnlineAck : AbstractMessage {
    @JvmField
    val is_online: Boolean
    @JvmField
    val uuid: String
    @JvmField
    val nonce: String

    constructor(is_online: Boolean, uuid: String, nonce: String) {
        this.is_online = is_online
        this.uuid = uuid
        this.nonce = nonce
    }
}