package kr.syeyoung.dungeonsguide.whosonline.api.messages.server

import kr.syeyoung.dungeonsguide.whosonline.api.messages.AbstractMessage
import java.util.*

class S06IrcMessage(val message: String, val sender: UUID, val date:Double): AbstractMessage