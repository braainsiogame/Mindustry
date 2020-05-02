package mindustry.world.blocks.storage;

import arc.func.*;
import mindustry.content.*;
import mindustry.plugin.spidersilk.SpiderSilk.*;
import mindustry.world.*;

public class Vault extends StorageBlock{

    public Vault(String name){
        super(name);
        solid = true;
        update = false;
        destructible = true;
    }

    @Override
    public void silk(Tile tile, Cons<Silk> cons){
        if(tile.block == Blocks.vault && getAroundCount(tile, t -> t.block == Blocks.solarPanel) >= size * size + 4) cons.get(new Silk(tile){{
            requirements = Blocks.vault.requirements;
            trigger = () -> construct(Blocks.coreShard);
            size = 5;
        }});
    }
}
