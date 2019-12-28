package mindustry.entities.bullet;

import arc.graphics.*;
import arc.graphics.g2d.*;
import mindustry.graphics.*;
import mindustry.world.blocks.distribution.MassDriver.*;

import static mindustry.Vars.content;

public class MassRiverBolt extends MassDriverBolt{
    public MassRiverBolt(){
        super();
        speed = 2f;
    }

    @Override
    public void draw(mindustry.entities.type.Bullet b){
        float w = 11f, h = 13f;

        DriverBulletData data = (DriverBulletData)b.getData();
        Color c = Pal.bulletYellow;
        for(int i = 0; i < data.liquids.length; i++){
            if(data.liquids[i] > 0f) c = content.liquid(i).color;
        }

        Draw.color(Pal.bulletYellowBack);
        Draw.rect("shell-back", b.x, b.y, w, h, b.rot() + 90);

        Draw.color(c);
        Draw.rect("shell", b.x, b.y, w, h, b.rot() + 90);

        Draw.reset();
    }
}
