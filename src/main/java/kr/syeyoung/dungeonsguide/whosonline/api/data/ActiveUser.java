package kr.syeyoung.dungeonsguide.whosonline.api.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Data
@AllArgsConstructor
public class ActiveUser {
    public CompletableFuture<Optional<String>> username;
    public boolean isOnline;
    public long updatedAt;
}
