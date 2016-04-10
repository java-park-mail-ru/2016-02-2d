package bomberman.gameMechanics.interfaces;


import bomberman.gameMechanics.WorldEvent;

import java.util.Queue;

public interface EventObtainable {
    Queue<WorldEvent> getFreshEvents();
}
