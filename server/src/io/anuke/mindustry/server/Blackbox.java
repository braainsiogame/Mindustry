package io.anuke.mindustry.server;

import io.anuke.arc.*;
import io.anuke.arc.collection.*;
import io.anuke.arc.collection.Array.*;
import io.anuke.arc.files.*;
import io.anuke.arc.util.*;
import io.anuke.mindustry.core.GameState.*;
import io.anuke.mindustry.entities.type.*;
import io.anuke.mindustry.gen.*;
import io.anuke.mindustry.io.*;

import java.io.*;

import static io.anuke.arc.util.Log.info;
import static io.anuke.mindustry.Vars.*;

public class Blackbox implements ApplicationListener{
    private long timestamp = 0;
    private String prefix = "timestamp-";

    @Override
    public void init(){

        Timer.schedule(() -> {
            if (!state.is(State.playing)) return;

            FileHandle file = saveDirectory.child(filenameFor(timestamp));

            Core.app.post(() -> {
                SaveIO.save(file);
                info("Autosaved {0}.", file);
            });
        }, 5f, 60f);

        //

        long mostRecent = mostRecent();
        Log.info("most recent: " + mostRecent);

        SaveIO.load(saveDirectory.child(filenameFor(mostRecent)));
        state.rules.zone = null;
        info("Save loaded.");
        try{
            net.host(Core.settings.getInt("port"));
        }catch(IOException e){
            e.printStackTrace();
        }
        state.set(State.playing);
    }

    @Override
    public void update(){
        timestamp = System.currentTimeMillis() / 1000;
    }

    protected String filenameFor(long timestamp){
        return prefix + timestamp + "." + saveExtension;
    }

    protected long mostRecent(){
        long mostRecent = 0;
        for(FileHandle file : saveDirectory.list()){
            if(file.extension().equals(saveExtension) && file.name().startsWith(prefix)){
                long ts = Long.parseLong(file.nameWithoutExtension().replace(prefix, ""));
                if (ts > mostRecent){
                    mostRecent = ts;
                }
            }
        }
        Log.info("most recent: " + mostRecent);
        return mostRecent;
    }

    protected void rollbackTo(long timestamp){
        Array<Player> players = new Array<>();
        for(Player p : playerGroup.all()){
            players.add(p);
            p.setDead(true);
        }

        logic.reset();
        SaveIO.load(saveDirectory.child(filenameFor(timestamp)));
        Call.onWorldDataBegin();
        logic.play();

        for(Player p : players){
            if(p.con == null) continue;

            p.reset();
            netServer.sendWorldData(p);
        }
    }
}
