package kr.syeyoung.dungeonsguide.mod.whosonline.api.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Data
@AllArgsConstructor
public class ActiveUser {
    public CompletableFuture<Optional<String>> username;
    public boolean isOnline;
    public long updatedAt;
}
