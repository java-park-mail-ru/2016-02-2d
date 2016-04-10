package bomberman.gameMechanics.interfaces;

public interface ITile extends Describable, Physical, Updateable, Actable {
    int[] getCoordinates();
    boolean shouldBeDestroyed();
}
