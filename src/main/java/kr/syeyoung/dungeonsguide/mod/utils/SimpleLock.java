package kr.syeyoung.dungeonsguide.mod.utils;

public class SimpleLock {
    private boolean lockStatus = true;

    public SimpleLock(){
    }

    public SimpleLock(boolean lockStatus) {
        this.lockStatus = lockStatus;
    }

    public void lock(){
        lockStatus = true;
    }

    public void unLock(){
        lockStatus = false;
    }

    public boolean isLocked(){
        return lockStatus;
    }


}
