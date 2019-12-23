package io.anuke.mindustry.world.blocks.logic.commanderblock;

import io.anuke.mindustry.world.blocks.logic.DroneCommanderBlock;

public class Variable extends BoxedValue {
    public Variable(Object key) {
        super(key);
    }
    @Override
    public Object resolve(DroneCommanderBlock.DroneCommanderBlockEntity entity) {
        final BoxedValue value = entity.variables.get(getKey(entity));
        return value == null ? null : value.resolve(entity);
    }
    public Object getKey(DroneCommanderBlock.DroneCommanderBlockEntity entity){
        return super.resolve(entity);
    }
}
