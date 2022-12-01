package kr.syeyoung.dungeonsguide.whosonline.api.messages.server

import kr.syeyoung.dungeonsguide.whosonline.api.messages.AbstractMessage

class S02areOnlineAck(val users: Map<String, Boolean>,val nonce: String) : AbstractMessage {}