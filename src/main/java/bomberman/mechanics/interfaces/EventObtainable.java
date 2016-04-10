package bomberman.mechanics.interfaces;


import bomberman.mechanics.WorldEvent;

import java.util.Queue;

public interface EventObtainable {
    Queue<WorldEvent> getFreshEvents();
}
