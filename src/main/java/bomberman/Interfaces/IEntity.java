package bomberman.Interfaces;

public interface IEntity extends Updateable, Describable{
    float[] getCoordinates();
    void affectHealth(int amount);
}
