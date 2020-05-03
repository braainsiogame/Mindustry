package mindustry.server.nydus;

import arc.*;
import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.core.GameState.*;
import mindustry.game.*;
import mindustry.game.EventType.*;
import mindustry.game.Teams.*;
import mindustry.plugin.*;
import mindustry.world.blocks.defense.Door.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class ConwayDoorway implements ApplicationListener{
//    private Interval timer = new Interval();

    private Array<DoorEntity> alive = new Array<>();
    private Array<DoorEntity> dead = new Array<>();

//    @Override
//    public void update(){
//        if(!state.is(State.playing)) return;
//
//        if(!timer.get(0, 60)) return;
//
//        for(TeamData team : state.teams.getActive()){
//            trigger(team.team);
//        }
//    }

    @Override
    public void init(){
        Events.on(WaveEvent.class, event -> {

            if(!Nydus.game_of_doors.active()) return;

            alive.clear();
            dead.clear();

            for(TeamData team : state.teams.getActive()){
                trigger(team.team);
            }

            alive.each(d -> d.open = false);
            dead.each(d  -> d.open = true);

            alive.each(d -> netServer.titanic.add(d.tile));
            dead.each(d  -> netServer.titanic.add(d.tile));
        });
    }

    private void trigger(Team team){
        indexer.getAllied(team, BlockFlag.door).each(tile -> {
            if(tile.block() != Blocks.door) return;

            DoorEntity door = tile.ent();

            int compass = tile.block.getAroundCount(tile, t -> t.block == Blocks.door && t.<DoorEntity>ent().alive());

            if(door.dead() && compass == 3) alive.add(door);
            if(door.alive() && compass < 2) dead.add(door);
            if(door.alive() && compass > 3) dead.add(door);
        });
    }
}
