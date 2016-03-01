package bomberman.interfaces;


import bomberman.WorldEvent;

import java.util.Queue;

public interface EventObtainable {
    Queue<WorldEvent> getFreshEvents();
}
