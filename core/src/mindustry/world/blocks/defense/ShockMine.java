package mindustry.world.blocks.defense;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.entities.effect.Lightning;
import mindustry.entities.type.*;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.Block;
import mindustry.world.Tile;

import static mindustry.Vars.tilesize;

public class ShockMine extends Block{
    public final int timerDamage = timers++;

    public float cooldown = 80f;
    public float tileDamage = 5f;
    public float damage = 13;
    public int length = 10;
    public int tendrils = 6;

    private TextureRegion crater;

    public ShockMine(String name){
        super(name);
        update = false;
        destructible = true;
        solid = false;
        targetable = false;
        layer = Layer.overlay;
        rebuildable = false;
        entityType = ShockMineEntity::new;
    }

    @Override
    public void load(){
        super.load();

        crater = Core.atlas.find("crater");
    }

    @Override
    public void drawLayer(Tile tile){
        ShockMineEntity entity = tile.ent();

        Unit stare = Units.closestEnemy(tile.getTeam(), tile.drawx(), tile.drawy(), size * tilesize * tilesize, u -> !u.isDead());

        if(stare != null) entity.rotation = Angles.moveToward(entity.rotation, tile.angleTo(stare),  1f * entity.delta());

        super.draw(tile);
        Draw.rect(crater, tile.drawx(), tile.drawy(), tilesize * 0.85f, tilesize * 0.85f, entity.rotation - 90);
        Draw.color(tile.getTeam().color);
        Draw.alpha(0.22f);
        Fill.rect(tile.drawx(), tile.drawy(), 2f, 2f, entity.rotation - 90);
        Draw.color();
    }

    @Override
    public void drawTeam(Tile tile){
        //no
    }

    @Override
    public void draw(Tile tile){
        //nope
    }

    @Override
    public void unitOn(Tile tile, Unit unit){
        if(unit.getTeam() != tile.getTeam() && tile.entity.timer.get(timerDamage, cooldown)){
            for(int i = 0; i < tendrils; i++){
                Lightning.create(tile.getTeam(), Pal.lancerLaser, damage, tile.drawx(), tile.drawy(), Mathf.random(360f), length);
            }
            tile.entity.damage(tileDamage);
        }
    }

    class ShockMineEntity extends TileEntity{
        float rotation;
    }
}
