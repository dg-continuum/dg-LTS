package kr.syeyoung.dungeonsguide.whosonline.api.messages.server

import kr.syeyoung.dungeonsguide.whosonline.api.messages.AbstractMessage
import lombok.Data

@Data
class S00ConnectAck(val sucess: Boolean) : AbstractMessage