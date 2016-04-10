package bomberman.mechanics.interfaces;

import bomberman.mechanics.Bomberman;

public interface Actable {
    //void applyAction();   // Should there be world-affecting bonuses? If yes, how can they affect it? It depends on World's interface...
    void applyAction(Bomberman bomberman);
}
