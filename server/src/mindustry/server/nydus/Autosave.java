package mindustry.server.nydus;

import arc.*;
import arc.files.*;
import arc.util.*;
import mindustry.*;
import mindustry.core.GameState.*;
import mindustry.io.*;

import static mindustry.Vars.*;

public class Autosave implements ApplicationListener{

    private Interval timer = new Interval();

    @Override
    public void update(){
        if(!Vars.state.is(State.playing)) return;

        int minutes = 1;
        if(timer.get(60 * 60 * minutes)){
            Log.info("&lm[autosave]");
            Fi file = saveDirectory.child("autosave" + "." + saveExtension);

            Core.app.post(() -> SaveIO.save(file));
        }
    }
}
