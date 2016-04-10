package bomberman.gameMechanics.interfaces;

import javax.inject.Singleton;

@Singleton
public abstract class Blueprintable {
    public int getWidth() {return getBlueprint()[0].length();}
    public int getHeight() {return getBlueprint().length;}

    public abstract String[] getBlueprint();
}
