package bomberman.mechanics;

import bomberman.mechanics.interfaces.EntityType;
import bomberman.mechanics.interfaces.EventType;

public class WorldEvent {
    public WorldEvent(EventType eventType, EntityType entityType, int entityID, float x, float y){
        this.eventType = eventType;
        this.entityType = entityType;
        this.entityID = entityID;
        this.x = x;
        this.y = y;

    }


    public EventType getEventType() {
        return eventType;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public int getEntityID() {
        return entityID;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    private final EventType eventType;
    private final EntityType entityType;
    private final int entityID;
    @SuppressWarnings("InstanceVariableNamingConvention")
    private final float x;
    @SuppressWarnings("InstanceVariableNamingConvention")
    private final float y;
}
