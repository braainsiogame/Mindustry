package mindustry.plugin;

import arc.*;
import arc.math.geom.*;
import arc.struct.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.type.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class CoreBarrage implements ApplicationListener{

    private Array<BulletType> bullets = new Array<>();

    public ObjectSet<Tile> pending = new ObjectSet<>();

    @Override
    public void init(){
        ((ItemTurret)Blocks.ripple).ammo.each((item, bullet) -> bullets.add(bullet));

        bullets.addAll(
        Bullets.artilleryDense,
//        Bullets.artilleryHoming,
        Bullets.artilleryIncendiary,
        Bullets.artilleryExplosive,
        Bullets.artilleryPlastic);
    }

    public void fire(Tile tile, Tile other){
        bullets.shuffle();
        BulletType type = bullets.first();

        pending.add(other);
        coreWithdraw(tile.getTeam(), other.block.upgrade.get(other));

        Vec2 predict = Predict.intercept(tile, other, type.speed);
        float dst = tile.dst(predict.x, predict.y);
        float maxTraveled = type.lifetime * type.speed;

        Call.createBullet(type, tile.getTeam(), tile.drawx(), tile.drawy(), tile.angleTo(other), 1f, (dst / maxTraveled));
        Bullet fired = bulletGroup.entitiesToAdd.get(bulletGroup.entitiesToAdd.size -1);

        fired.deathrattle = b -> Core.app.post(() -> { // todo, refund on failure
            other.block.upgrade(other);
            pending.remove(other);
        });
    }

    public Array<Tile> upgradable(Team team){
        return indexer.getAllied(team, BlockFlag.upgradable).select(t -> {
            if(pending.contains(t)) return false;
            if(t == null || t.block.upgrade == null || t.block.upgrade.get(t) == null) return false;
            if(!coreAllow(team, t.block.upgrade.get(t))) return false;
            if((t.block.upgrade.get(t) == Blocks.armoredConveyor || t.block.upgrade.get(t) == Blocks.platedConduit) && Units.closest(team, t.drawx(), t.drawy(), tilesize * 22, u -> u instanceof Player) != null) return false;
            return true;
        }).asArray();
    }

    public boolean coreAllow(Team team, Block block){
        return team.core().items.has(block.requirements, state.rules.buildCostMultiplier);
    }

    public void coreWithdraw(Team team, Block block){
        for(ItemStack is: block.requirements){
            team.core().items.remove(is.item, (int)(is.amount * state.rules.buildCostMultiplier));
        }
    }

    public void coreDeposit(Team team, Block block){
        for(ItemStack is: block.requirements){
            team.core().items.add(is.item, (int)(is.amount * state.rules.buildCostMultiplier));
        }
    }
}
