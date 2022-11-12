package kr.syeyoung.dungeonsguide.utils;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple utility class that's meant to replace:
 * <pre>{@code
 *  int counter;
 *  public onSomeEvent(Event e){
 *      counter++;
 *      if(counter % 20 == 0){
 *          counter = 0
 *          // do work that should only be done every 20 events
 *      }
 *  }
 * }</pre>
 * With:
 * <pre>{@code
 *  SimpleTimer doSomethingtimer = new SimpleTimer(20);
 *  public onSomeEvent(Event e){
 *      doSomethingtimer.tick()
 *      if(doSomethingtimer.check()){
 *          // do work that should only be done every 20 events
 *      }
 *  }
 * }</pre>
 * as well as being thread safe
 */
public class SimpleTimer {

    private final AtomicInteger state = new AtomicInteger();
    private final AtomicBoolean shouldRun = new AtomicBoolean();
    final int freqency;

    /**
     * NOTE:
     * frequency is -1 bc we count from 0 and:
     * feq is 5, the tick is called 5 times, state is only 4 (0->1->2->3->4), and we would have to tick 6 times
     * @param frequency How many times should {@link #tick()} get called before shouldRun returns true
     *
     */
    public SimpleTimer(int frequency) {
        this.freqency = frequency - 1;
    }

    /**
     * @return true if the timer ticked the right amount of times
     */
    public boolean shouldRun(){
        return shouldRun.compareAndSet(true, false);
    }

    /**
     * tick the timer up
     */
    public void tick(){
        if(!shouldRun.get()){
            int copy = state.incrementAndGet();
            if(copy == freqency){
                state.set(0);
                shouldRun.set(true);
            }
        }
    }

}
