package io.anuke.mindustry.entities.type.base;

import io.anuke.arc.*;
import io.anuke.arc.graphics.*;
import io.anuke.arc.graphics.g2d.*;
import io.anuke.arc.math.*;
import io.anuke.arc.util.*;
import io.anuke.mindustry.entities.*;
import io.anuke.mindustry.entities.type.*;
import io.anuke.mindustry.graphics.*;
import io.anuke.mindustry.world.blocks.*;

public class TetherMech extends BaseUnit{
    private float strength;
    private float range = 20 * 8;
    private float height = 0;

    @Override
    public void draw(){

        Floor floor = getFloorOn();
        if(floor.isLiquid && !isFlying()){
            Draw.color(Color.white, floor.color, drownTime * 0.4f);
        }else{
            Draw.color(Color.white);
        }

        Draw.rect(type.baseRegion, x, y, 90);
        Draw.rect(type.region, x, y, rotation + 90);

        if(target == null) return;

        TextureRegion laser = Core.atlas.find("tether-laser");
        TextureRegion laserEnd = Core.atlas.find("tether-laser-end");

        float len = 5f;
        Draw.color(Color.valueOf("e8ffd7"));
        Drawf.laser(laser, laserEnd,
        x + Angles.trnsx(rotation, len), y + Angles.trnsy(rotation, len),
        target.getX(), target.getY(), strength);
    }

    @Override
    public void update(){
        target = Units.closest(team, x, y, range, b -> b instanceof Player);
        super.update();
    }

    @Override
    public void updateTargeting(){
        if(target == null || (target instanceof Unit && target.isDead()) || dst(target) > range
        || (target instanceof TileEntity && ((TileEntity)target).tile.entity == null)){
            target = null;
        }
    }

    @Override
    public void behavior(){
        rotation = Mathf.slerpDelta(rotation, angleTo(target), 1f);
        strength = Mathf.clamp(1 - dst(target) / range + 0.25f, 0f, 1f);

        if(timer.get(timerTarget, 5) && strength < 1f){
            Tmp.v1.set(this).sub(target.getX(), target.getY()).setLength(0.025f * dst(target)).scl(0.45f * Time.delta());
            applyImpulse(-Tmp.v1.x, -Tmp.v1.y);
        }
    }

    public void drawShadow(float offsetX, float offsetY){

        float modifier = velocity.len2() / 20;
        height = Mathf.lerpDelta(height, modifier, 0.1f);

        Draw.scl += height;
        Draw.rect(type.baseRegion, x, y, 90);
        Draw.scl -= height;
    }

    @Override
    public boolean isFlying(){
        return !velocity.isZero(0.175f);
    }
}
