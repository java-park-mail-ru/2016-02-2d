package bomberman;

import bomberman.interfaces.EntityType;
import bomberman.interfaces.EventType;

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

    private EventType eventType;
    private EntityType entityType;
    private int entityID;
}
