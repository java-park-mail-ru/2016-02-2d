package bomberman.gameMechanics.interfaces;

public interface IEntity extends Updateable, Describable{
    float[] getCoordinates();
    void setCoordinates(float[] coords);
    void affectHealth(int amount);
}
