package kr.syeyoung.dungeonsguide.utils;

/**
 * Simple util class that "blows the fuse" after a specified amount of milliseconds
 */
public class SimpleTimeFuse {
    private volatile long triggerAt = -1;

    private final long delay;

    /**
     * @param delay How long in ms before the fuse blows
     */
    public SimpleTimeFuse(final long delay) {
        this.delay = delay;
    }

    volatile boolean isTriggered = false;

    public boolean isBlown(){
        if(isTriggered){
            return true;
        }

        if(triggerAt == -1){
            triggerAt = System.currentTimeMillis() + delay;
        }

        isTriggered = System.currentTimeMillis() > triggerAt;

        return isTriggered;
    }

}
