package mindustry.world.blocks.power;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.Effects.*;
import mindustry.game.*;
import mindustry.plugin.spidersilk.SpiderSilk.*;
import mindustry.world.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class ThermalGenerator extends PowerGenerator{
    public Effect generateEffect = Fx.none;
    public Attribute attribute = Attribute.heat;

    public ThermalGenerator(String name){
        super(name);
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(BlockStat.tiles, attribute);
    }

    @Override
    public void update(Tile tile){
        GeneratorEntity entity = tile.ent();

        if(entity.productionEfficiency > 0.1f && Mathf.chance(0.05 * entity.delta())){
            Effects.effect(generateEffect, tile.drawx() + Mathf.range(3f), tile.drawy() + Mathf.range(3f));
        }
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        drawPlaceText(Core.bundle.formatFloat("bar.efficiency", sumAttribute(attribute, x, y) * 100, 1), x, y, valid);
    }

    @Override
    public void drawLight(Tile tile){
        GeneratorEntity entity = tile.ent();
        renderer.lights.add(tile.drawx(), tile.drawy(), (40f + Mathf.absin(10f, 5f)) * entity.productionEfficiency * size, Color.scarlet, 0.4f);
    }

    @Override
    public void onProximityAdded(Tile tile){
        super.onProximityAdded(tile);

        GeneratorEntity entity = tile.ent();
        entity.productionEfficiency = sumAttribute(attribute, tile.x, tile.y);
    }

    @Override
    public float getPowerProduction(Tile tile){
        //in this case, productionEfficiency means 'total heat'
        //thus, it may be greater than 1.0
        return powerProduction * tile.<GeneratorEntity>ent().productionEfficiency;
    }

    @Override
    public boolean canPlaceOn(Tile tile){
        //make sure there's heat at this location
        return tile.getLinkedTilesAs(this, tempTiles).sumf(other -> other.floor().attributes.get(attribute)) > 0.01f;
    }

    @Override
    public void silk(Tile tile, Cons<Silk> cons){
        Team tea = tile.getTeam();
        for(Point2 i : Geometry.d4){
            Tile other = world.tile(tile.x + (i.x * size), tile.y + (i.y * size));
            if(Build.validPlace(tile.getTeam(), other.x, other.y, Blocks.thermalGenerator, 0)){
                cons.get(new Silk(other){{
                    requirements = Blocks.thermalGenerator.requirements;
                    afford = (inventory) -> inventory.has(requirements, state.rules.buildCostMultiplier);
                    trigger = () -> construct(Blocks.thermalGenerator);
                    team = tea;
                    size = 2;
                    weightMultiplier = 0.5f;
                    abort = () -> abort.get() || !Build.validPlace(tile.getTeam(), other.x, other.y, Blocks.thermalGenerator, 0);
                }});
            }
        }
    }
}
