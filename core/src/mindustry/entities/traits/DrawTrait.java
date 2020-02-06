package mindustry.entities.traits;

import arc.math.*;
import mindustry.entities.type.*;

import static mindustry.Vars.*;

public interface DrawTrait extends Entity{

    default float drawSize(){
        return 20f;
    }

    void draw();

    default float drawf(){
        if(this instanceof BaseUnit) return Mathf.clamp(player.dst(this) / tilesize / tilesize - 0.5f, 0.01f, 1f);

        return 1f;
    }
}
