package io.anuke.mindustry.world.blocks.storage;

import io.anuke.arc.util.*;
import io.anuke.mindustry.*;
import io.anuke.mindustry.content.*;
import io.anuke.mindustry.game.*;
import io.anuke.mindustry.type.*;
import io.anuke.mindustry.world.*;
import io.anuke.mindustry.world.meta.*;

public class CoreGhost extends Block {
    public CoreGhost(String name){
        super(name);
        update = true;
        hasShadow = false;
    }

    public Tile core(Tile tile){
        return Vars.indexer.getAllied(Team.sharded, BlockFlag.core).first();
    }

    @Override
    public boolean acceptItem(Item item, Tile tile, Tile source){
        return tile.link().block().acceptItem(item, tile.link(), source);
    }

    @Override
    public void handleItem(Item item, Tile tile, Tile source){
        tile.link().block().handleItem(item, tile.link(), source);
    }

    public boolean synthetic(){
        return true;
    }

    public boolean isHidden(){
        return true;
    }

    @Override
    public void update(Tile tile){
        super.update(tile);

        Timer.schedule(() -> {
            tile.setBlock(Blocks.air);
        }, 0.1f);
    }

    @Override
    public Tile linked(Tile tile){
        return Vars.indexer.getAllied(Team.sharded, BlockFlag.core).first();
    }

    @Override
    public void draw(Tile tile){

    }
}