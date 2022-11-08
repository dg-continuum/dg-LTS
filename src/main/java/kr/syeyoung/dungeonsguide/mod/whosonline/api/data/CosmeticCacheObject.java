package kr.syeyoung.dungeonsguide.mod.whosonline.api.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CosmeticCacheObject {
    public final CosmeticData[] cosmetics;
    public final CosmeticsUserPerms[] users;
    public long expireAt;
}
