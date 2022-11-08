package kr.syeyoung.dungeonsguide.mod.whosonline.api.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CosmeticData {
    public int id;
    public String display;
    public String description;
    public String name;
    public int required_flags;
}
