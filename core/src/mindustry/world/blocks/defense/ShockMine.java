package mindustry.world.blocks.defense;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import mindustry.content.*;
import mindustry.entities.effect.*;
import mindustry.entities.type.*;
import mindustry.game.*;
import mindustry.graphics.*;
import mindustry.world.*;

import static mindustry.Vars.*;

public class ShockMine extends Block{
    public final int timerDamage = timers++;

    public float cooldown = 80f;
    public float tileDamage = 5f;
    public float damage = 13;
    public int length = 10;
    public int tendrils = 6;

    public ShockMine(String name){
        super(name);
        update = false;
        destructible = true;
        solid = false;
        targetable = false;
        layer = Layer.overlay;
        rebuildable = false;
    }

    @Override
    public void drawLayer(Tile tile){
        super.draw(tile);
        Draw.color(tile.getTeam().color);
        Draw.alpha(0.22f);
        Fill.rect(tile.drawx(), tile.drawy(), 2f, 2f);
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

    @Override
    public void onDestroyed(Tile tile){

        tempTiles.clear();
        Geometry.circle(tile.x, tile.y, 5, (x, y) -> tempTiles.add(world.tile(x, y)));
        tempTiles.shuffle();

        Team team = tile.getTeam();

        for(int i = 0; i < 5; ++i){
            Tile target = tempTiles.pop();
            if(target == null) continue;
            Bullet bullet = coreBarrage.bullet(Bullets.artilleryHoming, tile, target);
            bullet.deathrattle = b -> {
                if(target.x == b.tileX() && target.y == b.tileY() && Build.validPlace(team, target.x, target.y, Blocks.shockMine, 0)) target.constructNet(Blocks.shockMine, team, (byte)0);
            };
        }

        super.onDestroyed(tile);
    }
}
