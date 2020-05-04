package mindustry.world.blocks.storage;

import arc.math.*;
import mindustry.content.*;
import mindustry.world.*;
import mindustry.world.blocks.production.GenericCrafter.*;

import static mindustry.Vars.netServer;

public class Vault extends StorageBlock{

    public Vault(String name){
        super(name);
        solid = true;
        update = true;
        destructible = true;
    }

    @Override
    public void update(Tile tile){
        if(tile.block == Blocks.container){
            if(tile.entity.proximity().contains(t -> t.block == Blocks.siliconSmelter)){
                if(tile.entity.items.has(Items.sand, itemCapacity) && tile.entity.items.has(Items.coal, itemCapacity / 2)){
                    Tile other = tile.entity.proximity().select(t -> t.block == Blocks.siliconSmelter).random();
                    if(Mathf.equal(other.<GenericCrafterEntity>ent().warmup, 1f, 0.001f)){
                        tile.entity.items.remove(Items.sand, itemCapacity);
                        tile.entity.items.remove(Items.coal, itemCapacity / 2);
                        other.entity.items.add(Items.silicon, itemCapacity / 2);
                        netServer.titanic.add(tile, other);
                    }
                }
            }
        }
    }
}
