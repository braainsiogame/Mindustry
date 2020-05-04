package mindustry.world.blocks.storage;

import arc.func.*;
import arc.struct.Array;
import arc.util.*;
import mindustry.content.*;
import mindustry.plugin.spidersilk.SpiderSilk.*;
import mindustry.world.*;

import java.lang.reflect.*;

import static mindustry.Vars.*;

public class Vault extends StorageBlock{

    public Vault(String name){
        super(name);
        solid = true;
        update = false;
        destructible = true;
    }

    @Override
    public void silk(Tile tile, Cons<Silk> cons){
        Array<Tile> container = tempTiles;
        container.clear();

        if(world.tile(tile.x + 0, tile.y + 0) != null && world.tile(tile.x + 0, tile.y + 0).block == Blocks.container) container.add(world.tile(tile.x + 0, tile.y + 0));
        if(world.tile(tile.x + 2, tile.y + 0) != null && world.tile(tile.x + 2, tile.y + 0).block == Blocks.container) container.add(world.tile(tile.x + 2, tile.y + 0));
        if(world.tile(tile.x + 2, tile.y + 2) != null && world.tile(tile.x + 2, tile.y + 2).block == Blocks.container) container.add(world.tile(tile.x + 2, tile.y + 2));
        if(world.tile(tile.x + 0, tile.y + 2) != null && world.tile(tile.x + 0, tile.y + 2).block == Blocks.container) container.add(world.tile(tile.x + 0, tile.y + 2));


        if(container.size == 4 && container.count(t -> t.<StorageBlockEntity>ent().linkedCore != null) > 0){
            cons.get(new Silk(world.tile(tile.x + 1, tile.y + 1)){{
                requirements = Blocks.launchPadLarge.requirements;
                trigger = () -> construct(Blocks.launchPadLarge);
                afford = (inventory) -> inventory.has(requirements, state.rules.buildCostMultiplier);
                size = 4;
                weightMultiplier = 0f;
            }});
        }
    }
}
