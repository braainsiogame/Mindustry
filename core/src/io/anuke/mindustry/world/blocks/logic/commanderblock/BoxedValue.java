package io.anuke.mindustry.world.blocks.logic.commanderblock;

import io.anuke.mindustry.world.blocks.logic.DroneCommanderBlock;

public class BoxedValue {
    protected Object raw;
    public BoxedValue(Object value){
        raw = value;
    }
    public Object resolve(DroneCommanderBlock.DroneCommanderBlockEntity entity){
        return raw instanceof BoxedValue ? ((BoxedValue) raw).resolve(entity) : raw;
    }
}
