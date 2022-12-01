package kr.syeyoung.dungeonsguide.whosonline.api.messages.client

import lombok.Data

@Data
class C02OnlineCheckBulk (val c: OBJ?) {
    val t = "/is_online/bulk"

    data class OBJ(val nonce: String, val uuids: Array<String>) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as OBJ

            if (nonce != other.nonce) return false
            if (!uuids.contentEquals(other.uuids)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = nonce.hashCode()
            result = 31 * result + uuids.contentHashCode()
            return result
        }
    }

}