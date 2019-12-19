package io.anuke.mindustry.world.blocks.water;

import io.anuke.arc.func.*;
import io.anuke.arc.graphics.g2d.*;
import io.anuke.mindustry.ui.*;
import io.anuke.mindustry.world.*;

public class WaterBlock extends Block{
    protected Block facade;

    protected Cons<Boolean> selected = bool -> {};

    public WaterBlock(String name){
        super(name);

        destructible = true;
    }

    @Override
    public TextureRegion icon(Cicon icon){
        return facade.icon(icon);
    }

    @Override
    public void setStats(){
        //
    }

    public void selected(){
        this.selected.get(true);
    }
}
