package bomberman.gameMechanics;

import bomberman.gameMechanics.interfaces.EntityType;
import bomberman.gameMechanics.interfaces.EventStashable;
import bomberman.gameMechanics.interfaces.ITile;
import bomberman.gameMechanics.tiles.ActionTile;
import bomberman.gameMechanics.tiles.DestructibleWall;
import bomberman.gameMechanics.tiles.OwnedActionTile;
import bomberman.gameMechanics.tiles.UndestructibleWall;
import bomberman.gameMechanics.tiles.behaviors.BombBehavior;
import bomberman.gameMechanics.tiles.behaviors.BombRayBehavior;
import bomberman.gameMechanics.tiles.behaviors.NullBehavior;
import bomberman.gameMechanics.tiles.functors.*;

import javax.inject.Singleton;

@Singleton
public class TileFactory {
    public static TileFactory getInstance()
    {
        return SINGLETON;
    }

    public ITile getNewTile(EntityType type, int id, int x, int y) throws IllegalArgumentException {
        switch (type)
        {
            case UNDESTRUCTIBLE_WALL:
                return newUndestructibleWall(id, x, y);
            case DESTRUCTIBLE_WALL:
                return newDestructibleWall(id, x, y);
            default:
                throw new IllegalArgumentException();
        }

    }

    // ATTENTION! FALSE WARNING! It is just not fully implemented yet. We will use much more parameters soon.
    public ITile getNewTile(EntityType type, EventStashable list, int id, int x, int y) throws IllegalArgumentException {
        switch (type)
        {
            case BONUS_HEAL:
                return newBonusHealAllHP(id, x, y, list);
            case BONUS_INCMAXHP:
                return newBonusIncreaseMaxHP(id, x, y, list);
            case BONUS_INCMAXRANGE:
                return newBonusIncreaseBombRange(id, x, y, list);
            case BONUS_DECBOMBFUSE:
                return newBonusDecreaseSpawnDelay(id, x, y, list);
            case BONUS_DECBOMBSPAWN:
                return newBonusDecreaseExplosionDelay(id, x, y, list);
            default:
                throw new IllegalArgumentException();
        }
    }

    public ITile getNewTile(EntityType type, EventStashable list, Bomberman owner, int id, int x, int y) throws IllegalArgumentException {
        switch (type)
        {
            case BOMB:
                return newBomb(id, x, y, list, owner);
            case BOMB_RAY:
                return newBombRay(id, x, y, list, owner);
            default:
                throw new IllegalArgumentException();
        }
    }

    private ITile newUndestructibleWall(int id, int x, int y) {
        return new UndestructibleWall(id, x, y);
    }
    private ITile newDestructibleWall(int id, int x, int y) {
        return new DestructibleWall(id, x, y);
    }

    private ITile newBomb(int id, int x, int y, EventStashable list, Bomberman owner) {
        return new OwnedActionTile(id, x, y, new NullFunctor(list), new BombBehavior(list, owner.getBombExplosionDelay()), EntityType.BOMB, owner);
    }
    private ITile newBombRay(int id, int x, int y, EventStashable list, Bomberman owner) {
        return new OwnedActionTile(id, x, y, new NullFunctor(list), new BombRayBehavior(list), EntityType.BOMB_RAY, owner);
    }

    private ITile newBonusHealAllHP(int id, int x, int y, EventStashable list){
        return new ActionTile(id, x, y, new HealAllHPFunctor(list), new NullBehavior(list), EntityType.BONUS_HEAL);
    }
    private ITile newBonusIncreaseMaxHP(int id, int x, int y, EventStashable list){
        return new ActionTile(id, x, y, new IncreaseMaxHPFunctor(list), new NullBehavior(list), EntityType.BONUS_INCMAXHP);
    }
    private ITile newBonusIncreaseBombRange(int id, int x, int y, EventStashable list){
        return new ActionTile(id, x, y, new IncreaseBombRangeFunctor(list), new NullBehavior(list), EntityType.BONUS_INCMAXRANGE);
    }
    private ITile newBonusDecreaseSpawnDelay(int id, int x, int y, EventStashable list){
        return new ActionTile(id, x, y, new DecreaseBombSpawnDelayFunctor(list), new NullBehavior(list), EntityType.BONUS_DECBOMBSPAWN);
    }
    private ITile newBonusDecreaseExplosionDelay(int id, int x, int y, EventStashable list){
        return new ActionTile(id, x, y, new DecreaseBombExplosionDelayFunctor(list), new NullBehavior(list), EntityType.BONUS_DECBOMBFUSE);
    }


    private static final TileFactory SINGLETON = new TileFactory();
}
