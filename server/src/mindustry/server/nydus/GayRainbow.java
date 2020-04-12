package mindustry.server.nydus;

import arc.*;
import arc.graphics.Color;
import arc.util.*;
import mindustry.content.*;
import mindustry.core.GameState.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.meta.*;

import java.awt.*;

import static mindustry.Vars.*;

public class GayRainbow implements ApplicationListener{

    private int hue = 0;

    @Override
    public void update(){
        if(!state.is(State.playing)) return;

        state.rules.ambientLight = new Color(java.awt.Color.getHSBColor((hue++ % 360) / 360f, 1f, 1f).getRGB());
        state.rules.ambientLight.a = 0.25f;
        state.rules.lighting = true;
        Call.onSetRules(state.rules);
    }
}
