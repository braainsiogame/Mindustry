package mindustry.world.blocks.storage;

import arc.struct.*;
import mindustry.content.*;
import mindustry.world.*;

import static mindustry.Vars.netServer;

public class Vault extends StorageBlock{

    private Array<Tile> batteries = new Array<>();

    public Vault(String name){
        super(name);
        solid = true;
        update = true;
        destructible = true;
    }

    @Override
    public void update(Tile tile){

        if(tile.block != Blocks.container) return;

        batteries.clear();
        batteries = tile.entity.proximity().select(t -> t.block == Blocks.battery && t.entity.power.status == 1f);
        if(batteries.isEmpty()) return;
        Tile battery = batteries.random();


        if(tile.entity.proximity().contains(t -> t.block == Blocks.siliconSmelter && (t.x == tile.x || t.y == tile.y))){
            if(tile.entity.items.has(Items.sand, itemCapacity) && tile.entity.items.has(Items.coal, itemCapacity / 2) && tile.entity.items.get(Items.silicon) <= itemCapacity / 2){
                tile.entity.items.remove(Items.sand, itemCapacity);
                tile.entity.items.remove(Items.coal, itemCapacity / 2);
                tile.entity.items.add(Items.silicon, itemCapacity / 2);
                battery.entity.power.status = 0f;
                netServer.titanic.add(tile, battery);
            }
        }
    }
}
