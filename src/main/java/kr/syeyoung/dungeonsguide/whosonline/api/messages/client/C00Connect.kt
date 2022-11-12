package kr.syeyoung.dungeonsguide.whosonline.api.messages.client

import lombok.Data

@Data
class C00Connect(val c :String?) {
    val t = "/connect"

}