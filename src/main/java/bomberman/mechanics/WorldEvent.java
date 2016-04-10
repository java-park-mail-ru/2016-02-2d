package bomberman.mechanics;

import bomberman.mechanics.interfaces.EntityType;
import bomberman.mechanics.interfaces.EventType;

public class WorldEvent {
    public WorldEvent(EventType eventType, EntityType entityType, int entityID){
        this.eventType = eventType;
        this.entityType = entityType;
        this.entityID = entityID;
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

    private final EventType eventType;
    private final EntityType entityType;
    private final int entityID;
}
