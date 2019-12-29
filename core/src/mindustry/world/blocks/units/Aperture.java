package mindustry.world.blocks.units;

import arc.*;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.struct.*;
import mindustry.content.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.blocks.power.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class Aperture extends Block{

    TextureRegion top, bot;

    public Aperture(String name){
        super(name);
        destructible = true;
        layer = Layer.lights;
        insulated = true;
        hasPower = true;
        flags = EnumSet.of(BlockFlag.aperture);
    }

    @Override
    public void load(){
        super.load();

        top = Core.atlas.find(name + "-top");
        bot = Core.atlas.find(name + "-bot");
    }

//    public void drawLayer(Tile tile){
    @Override
    public void drawLight(Tile tile){
        boolean blue = (tile.x % 2) == 0;

        Tile a = world.tile(tile.x - size + 1, tile.y);
        Tile b = world.tile(tile.x + size - 1, tile.y);

        if(a == null || a.block() != Blocks.powerNode) return;
        if(b == null || b.block() != Blocks.powerNode) return;

        Draw.rect(blue ? top : bot, tile.drawx(), tile.drawy()); // < because i could not get scissors to work properly :(

        ((PowerNode)Blocks.powerNode).drawLaser(a, b, blue ? Color.blue : Pal.remove, tile.entity.power.graph.getSatisfaction());
    }
}
