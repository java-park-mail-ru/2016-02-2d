package bomberman.mechanics.interfaces;

public interface ITile extends Describable, Physical, Updateable, Actable {
    boolean shouldSpawnBonusOnDestruction();
}
