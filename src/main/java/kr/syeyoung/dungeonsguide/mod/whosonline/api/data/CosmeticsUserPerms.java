package kr.syeyoung.dungeonsguide.mod.whosonline.api.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class CosmeticsUserPerms {
    public UUID uuid;
    public int perms;
}
