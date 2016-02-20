package bomberman.Interfaces;

public interface ITile extends Describable, Destructible, Passable, Updateable, Actable {
    void destroy();
}
