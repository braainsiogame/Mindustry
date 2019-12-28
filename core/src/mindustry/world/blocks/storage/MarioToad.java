package mindustry.world.blocks.storage;

import arc.*;
import arc.graphics.g2d.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.traits.*;
import mindustry.entities.traits.BuilderTrait.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;

public class MarioToad extends Block{
    private TextureRegion[] arrow = new TextureRegion[4];

    public MarioToad(String name){
        super(name);
        rotate = true;
        breakable = true;
        solid = true;
    }

    @Override
    public void load(){
        super.load();

        arrow[0] = Core.atlas.find(name + "-arrow-" + 0);
        arrow[1] = Core.atlas.find(name + "-arrow-" + 1);
        arrow[2] = Core.atlas.find(name + "-arrow-" + 2);
        arrow[3] = Core.atlas.find(name + "-arrow-" + 3);
    }

    @Override
    public void draw(Tile tile){
        Draw.rect(region, tile.drawx(), tile.drawy(), 0);
        Draw.rect(arrow[tile.rotation()], tile.drawx(), tile.drawy(), 0);
    }

    @Override
    public void drawRequestRegion(BuilderTrait.BuildRequest req, Eachable<BuildRequest> list) {
        TextureRegion reg = icon(Cicon.full);
        Draw.rect(icon(Cicon.full), req.drawx(), req.drawy(),
        reg.getWidth() * req.animScale * Draw.scl,
        reg.getHeight() * req.animScale * Draw.scl,
        0);
        Draw.rect(arrow[req.rotation], req.drawx(), req.drawy(),
        arrow[0].getWidth() * req.animScale * Draw.scl,
        arrow[0].getHeight() * req.animScale * Draw.scl,
        0);
    }

    @Override
    public boolean acceptItem(Item item, Tile tile, Tile source){
        if (tile == tile.link()) return false;
        return tile.link().block().acceptItem(item, tile.link(), source);
    }

    @Override
    public void handleItem(Item item, Tile tile, Tile source){
        tile.link().block().handleItem(item, tile.link(), source);
    }

    @Override
    public void handleLiquid(Tile tile, Tile source, Liquid liquid, float amount){
        Block block = tile.link().block();
        block.handleLiquid(tile.link(), source, liquid, amount);
    }

    @Override
    public boolean acceptLiquid(Tile tile, Tile source, Liquid liquid, float amount){
        if (tile == tile.link()) return false;
        Block block = tile.link().block();
        return block.hasLiquids && block.acceptLiquid(tile.link(), source, liquid, amount);
    }

    @Override
    public Tile linked(Tile tile){
        if(tile.front() == null) return tile;
        if(tile.front().block() == Blocks.air) return tile;
        return tile.front();
    }
}
