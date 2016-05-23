package bomberman.service;

import org.jetbrains.annotations.NotNull;

import java.time.Clock;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        return CLOCK.millis();
    }

    public static void executeAfter(int ms, Executor action) {
        TASKS.execute( () -> {
            sleepFor(ms);
            action.execute();
        });
    }

    public interface Executor {
        void execute();
    }

    @NotNull
    private static final Clock CLOCK = Clock.systemUTC();
    private static final ExecutorService TASKS = Executors.newCachedThreadPool();
}
