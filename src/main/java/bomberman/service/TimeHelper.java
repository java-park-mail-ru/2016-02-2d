package bomberman.service;

import org.jetbrains.annotations.NotNull;

import java.time.Clock;

public class TimeHelper {

    public static void sleepFor(long ms) {
        if (ms <= 0) {
            return;
        }
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("StaticMethodNamingConvention")
    public static long now() {
        return clock.millis();
    }

    public static void executeAfter(int ms, Executor action) {
        sleepFor(ms);
        action.execute();
    }

    public interface Executor {
        void execute();
    }

    @NotNull
    private static Clock clock = Clock.systemUTC();
}
