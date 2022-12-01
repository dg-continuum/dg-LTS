package kr.syeyoung.dungeonsguide.utils.simple;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility class meant to replace:
 * <pre>{@code
 *  int counter;
 *  public onSomeEvent(Event e){
 *      if (counter < 5) {
 *          counter++;
 *          return;
 *      }
 *      // do work that happens after 5 events
 *  }
 * }</pre>
 * With:
 * <pre>{@code
 *  SimpleCounterFuse counterFuse = new SimpleCounterFuse(5);
 *  public onSomeEvent(Event e){
 *      if (counterFuse.countDown()) {
 *          // do work that happens after 5 events
 *      }
 *  }
 * }</pre>
 */
public class SimpleCounterFuse {

    private final int triggerPoint;
    private final AtomicInteger state = new AtomicInteger();
    private volatile boolean triggered = false;

    public SimpleCounterFuse(final int triggerPoint) {
        // decreased by 1 bc we count from 0
        this.triggerPoint = triggerPoint - 1;
    }

    /**
     * @return True if it's been called "tigger point" times
     */
    public boolean countDown() {
        if (!triggered) {
            int copy = state.incrementAndGet();
            if (copy == triggerPoint) {
                triggered = true;
            }
        }
        return triggered;
    }

}
