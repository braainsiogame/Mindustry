package mindustry.server.nydus;

import arc.*;
import mindustry.content.*;
import mindustry.game.EventType.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.distribution.ItemBridge.*;

import static mindustry.Vars.world;

public class BridgeBlocker implements ApplicationListener{
    @Override
    public void init(){

        Events.on(TapConfigEvent.class, event -> {
            if(event.tile.block == Blocks.itemBridge || event.tile.block == Blocks.bridgeConduit){
                ItemBridgeEntity entity = event.tile.ent();
                if(entity.incoming.size > 0 && ((ItemBridge)event.tile.block).linkValid(event.tile, world.tile(entity.link))){
                    Core.app.post(() -> event.tile.constructNet(Blocks.scrapWall, event.tile.getTeam(), (byte)0));
                }
            }
        });

    }
}
