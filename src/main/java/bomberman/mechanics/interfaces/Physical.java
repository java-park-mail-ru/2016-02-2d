package bomberman.mechanics.interfaces;

public interface Physical {
    boolean isDestructible();
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isPassable();
}
