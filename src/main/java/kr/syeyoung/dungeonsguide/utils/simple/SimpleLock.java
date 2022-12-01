package kr.syeyoung.dungeonsguide.utils.simple;

import java.util.concurrent.atomic.AtomicBoolean;

public class SimpleLock {
    private final AtomicBoolean status = new AtomicBoolean();

    public SimpleLock(){
        status.set(false);
    }

    public SimpleLock(boolean lockStatus) {
        status.set(lockStatus);
    }

    public void lock(){
        status.set(true);
    }

    public void unLock(){
        status.set(false);
    }

    public boolean isLocked(){
        return status.get();
    }

    public boolean isUnlocked(){
        return !status.get();
    }


}
