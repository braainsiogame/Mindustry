package mindustry.entities.traits;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.Time;
import mindustry.content.*;
import mindustry.entities.Effects;
import mindustry.entities.effect.*;
import mindustry.entities.type.*;
import mindustry.gen.Call;
import mindustry.graphics.*;
import mindustry.type.Item;
import mindustry.world.Tile;

import static mindustry.Vars.*;

public interface MinerTrait extends Entity{

    /** Returns the range at which this miner can mine blocks.*/
    default float getMiningRange(){
        return 70f;
    }

    default boolean isMining(){
        return getMineTile() != null;
    }

    /** Returns the tile this builder is currently mining. */
    Tile getMineTile();

    /** Sets the tile this builder is currently mining. */
    void setMineTile(Tile tile);

    /** Returns the mining speed of this miner. 1 = standard, 0.5 = half speed, 2 = double speed, etc. */
    float getMinePower();

    /** Returns whether or not this builder can mine a specific item type. */
    boolean canMine(Item item);

    /** @return whether to offload mined items immediately at the core. if false, items are collected and dropped in a burst. */
    default boolean offloadImmediately(){
        return false;
    }

    default void updateMining(){
        Unit unit = (Unit)this;

        if(unit instanceof MinerDrone){
            updateMiningDrone();
            return;
        }

        Tile tile = getMineTile();
        TileEntity core = unit.getClosestCore();

        if(core != null && tile != null && tile.drop() != null && !unit.acceptsItem(tile.drop()) && unit.dst(core) < mineTransferRange){
            int accepted = core.tile.block().acceptStack(unit.item().item, unit.item().amount, core.tile, unit);
            if(accepted > 0){
                Call.transferItemTo(unit.item().item, accepted,
                    tile.worldx() + Mathf.range(tilesize / 2f),
                    tile.worldy() + Mathf.range(tilesize / 2f), core.tile);
                unit.clearItem();
            }
        }

        if(tile == null || core == null || tile.block() != Blocks.air || dst(tile.worldx(), tile.worldy()) > getMiningRange()
        || tile.drop() == null || !unit.acceptsItem(tile.drop()) || !canMine(tile.drop())){
            setMineTile(null);
        }else{
            Item item = tile.drop();
            unit.rotation = Mathf.slerpDelta(unit.rotation, unit.angleTo(tile.worldx(), tile.worldy()), 0.4f);

            if(Mathf.chance(Time.delta() * (0.06 - item.hardness * 0.01) * getMinePower())){

                if(unit.dst(core) < mineTransferRange && core.tile.block().acceptStack(item, 1, core.tile, unit) == 1 && offloadImmediately()){
                    Call.transferItemTo(item, 1,
                    tile.worldx() + Mathf.range(tilesize / 2f),
                    tile.worldy() + Mathf.range(tilesize / 2f), core.tile);
                }else if(unit.acceptsItem(item)){
<<<<<<< HEAD:core/src/io/anuke/mindustry/entities/traits/MinerTrait.java
                    Call.transferItemToUnit(item,
                    tile.worldx() + Mathf.range(tilesize / 2f),
                    tile.worldy() + Mathf.range(tilesize / 2f),
                    unit);
                }
            }

            if(Mathf.chance(0.06 * Time.delta())){
                Effects.effect(Fx.pulverizeSmall,
                tile.worldx() + Mathf.range(tilesize / 2f),
                tile.worldy() + Mathf.range(tilesize / 2f), 0f, item.color);
            }
        }
    }

    default void updateMiningDrone(){
        Unit unit = (Unit)this;
        Tile tile = getMineTile();
        TileEntity core = unit.getClosestCore();

        if(unit instanceof MinerDrone){
            Tile melter = Array.with(indexer.getAllied(unit.getTeam(), BlockFlag.smelter)).random();
            if(melter != null){
                core = melter.entity;
            }else{
                core = null;
            }
        }

        Item drop = Items.scrap;

        if(tile == null || core == null || tile.block() != Blocks.air || dst(tile.worldx(), tile.worldy()) > getMiningRange()
        || tile.drop() == null || !unit.acceptsItem(drop) || !canMine(tile.drop())){
            setMineTile(null);
        }else{
            unit.rotation = Mathf.slerpDelta(unit.rotation, unit.angleTo(tile.worldx(), tile.worldy()), 0.4f);

            if(Mathf.chance(Time.delta() * (0.06 - drop.hardness * 0.01) * getMinePower())){

                if(unit.dst(core) < mineTransferRange && core.tile.block().acceptStack(drop, 1, core.tile, unit) == 1){
                    Call.transferItemTo(drop, 1,
                    tile.worldx() + Mathf.range(tilesize / 2f),
                    tile.worldy() + Mathf.range(tilesize / 2f), core.tile);
                }else if(unit.acceptsItem(drop)){
                    Call.transferItemToUnit(drop,
                    tile.worldx() + Mathf.range(tilesize / 2f),
                    tile.worldy() + Mathf.range(tilesize / 2f),
                    unit);
=======
                    //this is clientside, since items are synced anyway
                    ItemTransfer.transferItemToUnit(item,
                            tile.worldx() + Mathf.range(tilesize / 2f),
                            tile.worldy() + Mathf.range(tilesize / 2f),
                            unit);
>>>>>>> master:core/src/mindustry/entities/traits/MinerTrait.java
                }
            }

            if(Mathf.chance(0.06 * Time.delta())){
                Effects.effect(Fx.pulverizeSmall,
                tile.worldx() + Mathf.range(tilesize / 2f),
                tile.worldy() + Mathf.range(tilesize / 2f), 0f, drop.color);
            }
        }
    }

    default void drawMining(){
        Unit unit = (Unit)this;
        Tile tile = getMineTile();

        if(tile == null) return;

        float focusLen = 4f + Mathf.absin(Time.time(), 1.1f, 0.5f);
        float swingScl = 12f, swingMag = tilesize / 8f;
        float flashScl = 0.3f;

        float px = unit.x + Angles.trnsx(unit.rotation, focusLen);
        float py = unit.y + Angles.trnsy(unit.rotation, focusLen);

        float ex = tile.worldx() + Mathf.sin(Time.time() + 48, swingScl, swingMag);
        float ey = tile.worldy() + Mathf.sin(Time.time() + 48, swingScl + 2f, swingMag);

        Draw.color(Color.lightGray, Color.white, 1f - flashScl + Mathf.absin(Time.time(), 0.5f, flashScl));

        Drawf.laser(Core.atlas.find("minelaser"), Core.atlas.find("minelaser-end"), px, py, ex, ey, 0.75f);

        if(unit instanceof Player && ((Player)unit).isLocal){
            Lines.stroke(1f, Pal.accent);
            Lines.poly(tile.worldx(), tile.worldy(), 4, tilesize / 2f * Mathf.sqrt2, Time.time());
        }

        Draw.color();
    }
}
