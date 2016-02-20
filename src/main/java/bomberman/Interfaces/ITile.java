package bomberman.interfaces;

public interface ITile extends Describable, Destructible, Passable, Updateable, Actable {
    int[] getCoordinates();
    boolean shouldBeDestroyed();
}
