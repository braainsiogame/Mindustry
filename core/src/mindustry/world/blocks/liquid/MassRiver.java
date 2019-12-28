package mindustry.world.blocks.liquid;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import mindustry.entities.*;
import mindustry.entities.type.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.distribution.*;

import static mindustry.Vars.content;

public class MassRiver extends MassDriver{
    public int liquidRegion;

    public MassRiver(String name){
        super(name);
        hasItems = false;
        hasLiquids = true;
        group = DriverGroup.liquids;
        knockback = 0f;
        outputsLiquid = true;
        liquidRegion = reg("-liquid");

        fireCondition = (tile, link) -> {
            return
            tile.entity.liquids.total() >= minDistribute && //must shoot minimum amount of items
            link.block().liquidCapacity - link.entity.liquids.total() >= minDistribute; //must have minimum amount of space
        };

        acceptCondition = entity -> liquidCapacity - entity.liquids.total() >= minDistribute;
        idleCondition = entity -> liquidCapacity - entity.liquids.total() < minDistribute;
    }

    @Override
    public boolean acceptItem(Item item, Tile tile, Tile source){
        return false;
    }

    @Override
    public boolean acceptLiquid(Tile tile, Tile source, Liquid liquid, float amount){
        return tile.entity.liquids.get(liquid) + amount < liquidCapacity && (tile.entity.liquids.current() == liquid || tile.entity.liquids.get(tile.entity.liquids.current()) < 0.2f);
    }

    protected void handlePayload(MassDriverEntity entity, Bullet bullet, DriverBulletData data){
        float totalLiquids = Mathf.round(entity.liquids.total());

        //add all the items possible
        for(int i = 0; i < data.liquids.length; i++){
            float maxAdd = Math.min(data.liquids[i], liquidCapacity * 2 - totalLiquids);
            entity.liquids.add(content.liquid(i), maxAdd);
            data.liquids[i] -= maxAdd;
            totalLiquids += maxAdd;

            if(totalLiquids >= liquidCapacity * 2){
                break;
            }
        }

        Effects.shake(shake, shake, entity);
        Effects.effect(recieveEffect, bullet);

        entity.reload = 1f;
        bullet.remove();
    }

    @Override
    public void drawLayer(Tile tile){
        super.drawLayer(tile);

        MassDriverEntity entity = tile.ent();

        if(Core.atlas.isFound(reg(liquidRegion))){
            Draw.color(tile.entity.liquids.current().color);
            Draw.alpha(tile.entity.liquids.total() / liquidCapacity);
            Draw.rect(reg(liquidRegion), tile.drawx(), tile.drawy(), entity.rotation - 90);
            Draw.color();
        }
    }
}
