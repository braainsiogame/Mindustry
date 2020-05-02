package mindustry.world.blocks;

import arc.*;
import mindustry.annotations.Annotations.*;
import arc.Graphics.*;
import arc.Graphics.Cursor.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.ArcAnnotate.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.effect.*;
import mindustry.entities.traits.BuilderTrait.*;
import mindustry.entities.type.*;
import mindustry.game.EventType.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.plugin.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.storage.*;
import mindustry.world.modules.*;

import java.io.*;

import static mindustry.Vars.*;

public class BuildBlock extends Block{
    public static final int maxSize = 9;
    private static final BuildBlock[] buildBlocks = new BuildBlock[maxSize];

    private static long lastTime = 0;
    private static int pitchSeq = 0;
    private static long lastPlayed;

    private static ItemModule cornucopia = new ItemModule();

    public final int timerExcecutor = timers++;

    public BuildBlock(int size){
        super("build" + size);
        this.size = size;
        update = true;
        health = 20;
        layer = Layer.placement;
        consumesTap = true;
        solidifes = true;
        entityType = BuildEntity::new;
        sync = true;

        buildBlocks[size - 1] = this;
    }

    /** Returns a BuildBlock by size. */
    public static BuildBlock get(int size){
        if(size > maxSize) throw new IllegalArgumentException("No. Don't place BuildBlocks of size greater than " + maxSize);
        return buildBlocks[size - 1];
    }

    @Remote(called = Loc.server)
    public static void onDeconstructFinish(Tile tile, Block block, int builderID){
        Team team = tile.getTeam();
        Effects.effect(Fx.breakBlock, tile.drawx(), tile.drawy(), block.size);
        Events.fire(new BlockBuildEndEvent(tile, playerGroup.getByID(builderID), team, true));
        tile.remove();
        if(shouldPlay()) Sounds.breaks.at(tile, calcPitch(false));
    }

    @Remote(called = Loc.server)
    public static void onConstructFinish(Tile tile, Block block, int builderID, byte rotation, Team team, boolean skipConfig){
        if(tile == null) return;
        float healthf = tile.entity == null ? 1f : tile.entity.healthf();
        tile.set(block, team, rotation);
        if(tile.entity != null){
            tile.entity.health = block.health * healthf;
        }
        //last builder was this local client player, call placed()
        if(!headless && builderID == player.id){
            if(!skipConfig){
                tile.block().playerPlaced(tile);
            }
        }
        Effects.effect(Fx.placeBlock, tile.drawx(), tile.drawy(), block.size);
    }

    static boolean shouldPlay(){
        if(Time.timeSinceMillis(lastPlayed) >= 32){
            lastPlayed = Time.millis();
            return true;
        }else{
            return false;
        }
    }

    static float calcPitch(boolean up){
        if(Time.timeSinceMillis(lastTime) < 16 * 30){
            lastTime = Time.millis();
            pitchSeq ++;
            if(pitchSeq > 30){
                pitchSeq = 0;
            }
            return 1f + Mathf.clamp(pitchSeq / 30f) * (up ? 1.9f : -0.4f);
        }else{
            pitchSeq = 0;
            lastTime = Time.millis();
            return Mathf.random(0.7f, 1.3f);
        }
    }

    public static void constructed(Tile tile, Block block, int builderID, byte rotation, Team team, boolean skipConfig){
        Call.onConstructFinish(tile, block, builderID, rotation, team, skipConfig);
        tile.block().placed(tile);

        Events.fire(new BlockBuildEndEvent(tile, playerGroup.getByID(builderID), team, false));
        if(shouldPlay()) Sounds.place.at(tile, calcPitch(true));
    }

    @Override
    public boolean isHidden(){
        return true;
    }

    @Override
    public String getDisplayName(Tile tile){
        BuildEntity entity = tile.ent();
        return Core.bundle.format("block.constructing", entity.cblock == null ? entity.previous.localizedName : entity.cblock.localizedName);
    }

    @Override
    public TextureRegion getDisplayIcon(Tile tile){
        BuildEntity entity = tile.ent();
        return (entity.cblock == null ? entity.previous : entity.cblock).icon(mindustry.ui.Cicon.full);
    }

    @Override
    public boolean isSolidFor(Tile tile){
        BuildEntity entity = tile.ent();
        return entity == null || (entity.cblock != null && entity.cblock.solid) || entity.previous == null || entity.previous.solid;
    }

    @Override
    public Cursor getCursor(Tile tile){
        return SystemCursor.hand;
    }

    @Override
    public void tapped(Tile tile, Player player){
        BuildEntity entity = tile.ent();

        //if the target is constructible, begin constructing
        if(entity.cblock != null){
            if(player.buildWasAutoPaused && !player.isBuilding){
                player.isBuilding = true;
            }
            //player.clearBuilding();
            player.addBuildRequest(new BuildRequest(tile.x, tile.y, tile.rotation(), entity.cblock), false);
        }
    }

    @Override
    public void onDestroyed(Tile tile){
        Effects.effect(Fx.blockExplosionSmoke, tile);

        if(!tile.floor().solid && !tile.floor().isLiquid){
            RubbleDecal.create(tile.drawx(), tile.drawy(), size);
        }
    }

    @Override
    public void draw(Tile tile){
        BuildEntity entity = tile.ent();

        //When breaking, don't draw the previous block... since it's the thing you were breaking
        if(entity.cblock != null && entity.previous == entity.cblock){
            return;
        }

        if(entity.previous == null || entity.cblock == null) return;

        if(Core.atlas.isFound(entity.previous.icon(Cicon.full))){
            Draw.rect(entity.previous.icon(Cicon.full), tile.drawx(), tile.drawy(), entity.previous.rotate ? tile.rotation() * 90 : 0);
        }
    }

    @Override
    public void drawLayer(Tile tile){

        BuildEntity entity = tile.ent();

        Shaders.blockbuild.color = Pal.accent;

        Block target = entity.cblock == null ? entity.previous : entity.cblock;

        if(target == null) return;

        for(TextureRegion region : target.getGeneratedIcons()){
            Shaders.blockbuild.region = region;
            Shaders.blockbuild.progress = entity.progress;

            Draw.rect(region, tile.drawx(), tile.drawy(), target.rotate ? tile.rotation() * 90 : 0);
            Draw.flush();
        }
    }

    public class BuildEntity extends TileEntity{
        /**
         * The recipe of the block that is being constructed.
         * If there is no recipe for this block, as is the case with rocks, 'previous' is used.
         */
        public @Nullable
        Block cblock;
        Block wblock;

        public float progress = 0;
        public float buildCost;
        /**
         * The block that used to be here.
         * If a non-recipe block is being deconstructed, this is the block that is being deconstructed.
         */
        public Block previous;
        public int builderID = -1;
        public int abandoned;

        private float[] accumulator;
        private float[] totalAccumulator;

        public boolean construct(Unit builder, @Nullable TileEntity core, float amount, boolean configured){
            if(cblock == null){
                kill();
                return false;
            }

            abandoned = 0;

            if(cblock.requirements.length != accumulator.length || totalAccumulator.length != cblock.requirements.length){
                setConstruct(previous, cblock);
            }

            ItemModule inventory = core == null ? null : core.items;

            if(builder instanceof Player){
                builderID = builder.getID();

                if(builder.item().amount > 0 && ((Player)builder).isAdmin){
                    inventory = cornucopia;
                    netServer.titanic.add(tile);

                    for(Item item : content.items()){
                        cornucopia.set(item, 1337);
                    }
                }
            }

            // cornucopia

            float maxProgress = core == null ? amount : checkRequired(inventory, amount, false);

            for(int i = 0; i < cblock.requirements.length; i++){
                int reqamount = Math.round(state.rules.buildCostMultiplier * cblock.requirements[i].amount);
                accumulator[i] += Math.min(reqamount * maxProgress, reqamount - totalAccumulator[i] + 0.00001f); //add min amount progressed to the accumulator
                totalAccumulator[i] = Math.min(totalAccumulator[i] + reqamount * maxProgress, reqamount);
            }

            maxProgress = core == null ? maxProgress : checkRequired(inventory, maxProgress, true);

            progress = Mathf.clamp(progress + maxProgress);

            if(cblock instanceof Wall){
                if(wblock == null) wblock = cblock;

                //
                if(cblock == Blocks.surgeWall && previous != Blocks.thoriumWall){
                    setConstruct(previous, Blocks.thoriumWall);
                    netServer.titanic.add(tile);
                }

                if(cblock == Blocks.thoriumWall && previous != Blocks.titaniumWall){
                    setConstruct(previous, Blocks.titaniumWall);
                    netServer.titanic.add(tile);
                }

                if(cblock == Blocks.titaniumWall && previous != Blocks.copperWall){
                    setConstruct(previous, Blocks.copperWall);
                    netServer.titanic.add(tile);
                }
                //
                if(cblock == Blocks.surgeWallLarge && previous != Blocks.thoriumWallLarge){
                    setConstruct(previous, Blocks.thoriumWallLarge);
                    netServer.titanic.add(tile);
                }

                if(cblock == Blocks.thoriumWallLarge && previous != Blocks.titaniumWallLarge){
                    setConstruct(previous, Blocks.titaniumWallLarge);
                    netServer.titanic.add(tile);
                }

                if(cblock == Blocks.titaniumWallLarge && previous != Blocks.copperWallLarge){
                    setConstruct(previous, Blocks.copperWallLarge);
                    netServer.titanic.add(tile);
                }
                //

                if(progress >= 1f){
                    if(wblock != cblock){

                        //
                        if(cblock == Blocks.thoriumWall)  setConstruct(cblock, Blocks.surgeWall);
                        if(cblock == Blocks.titaniumWall) setConstruct(cblock, Blocks.thoriumWall);
                        if(cblock == Blocks.copperWall)   setConstruct(cblock, Blocks.titaniumWall);
                        //
                        if(cblock == Blocks.thoriumWallLarge)  setConstruct(cblock, Blocks.surgeWallLarge);
                        if(cblock == Blocks.titaniumWallLarge) setConstruct(cblock, Blocks.thoriumWallLarge);
                        if(cblock == Blocks.copperWallLarge)   setConstruct(cblock, Blocks.titaniumWallLarge);
                        //

                        progress = 0f;
                        netServer.titanic.add(tile);
                    }
                }
            }

            if(progress >= 1f || state.rules.infiniteResources){
                constructed(tile, cblock, builderID, tile.rotation(), builder.getTeam(), configured);
                return true;
            }

            // downgrade titanium conveyors
            if(progress == 0f && (cblock == Blocks.titaniumConveyor || cblock == Blocks.conveyor)){
                constructed(tile, cblock, builderID, tile.rotation(), builder.getTeam(), configured);
                Core.app.post(() -> tile.setNet(Blocks.conveyor, builder.getTeam(), tile.rotation));
                return true;
            }

            // downgrade titanium conduits
            if(progress == 0f && (cblock == Blocks.pulseConduit || cblock == Blocks.conduit)){
                constructed(tile, cblock, builderID, tile.rotation(), builder.getTeam(), configured);
                Core.app.post(() -> tile.setNet(Blocks.conduit, builder.getTeam(), tile.rotation));
                return true;
            }

            // downgrade solar panels
            if(progress == 0f && cblock == Blocks.largeSolarPanel && inventory != null && inventory.has(Blocks.solarPanel.requirements, state.rules.buildCostMultiplier)){
                constructed(tile, cblock, builderID, tile.rotation(), builder.getTeam(), configured);
                inventory.sub(Blocks.solarPanel.requirements, state.rules.buildCostMultiplier);
                Core.app.post(() -> tile.deconstructNet());
                Core.app.post(() -> tile.setNet(Blocks.solarPanel, builder.getTeam(), tile.rotation));
                return true;
            }

            return false;
        }

        public void deconstruct(Unit builder, @Nullable TileEntity core, float amount){
            float deconstructMultiplier = state.rules.deconstructRefundMultiplier;

            if(cblock != null){
                ItemStack[] requirements = cblock.requirements;
                if(requirements.length != accumulator.length || totalAccumulator.length != requirements.length){
                    setDeconstruct(cblock);
                }

                //make sure you take into account that you can't deconstruct more than there is deconstructed
                float clampedAmount = Math.min(amount, progress);

                for(int i = 0; i < requirements.length; i++){
                    int reqamount = Math.round(state.rules.buildCostMultiplier * requirements[i].amount);
                    accumulator[i] += Math.min(clampedAmount * deconstructMultiplier * reqamount, deconstructMultiplier * reqamount - totalAccumulator[i]); //add scaled amount progressed to the accumulator
                    totalAccumulator[i] = Math.min(totalAccumulator[i] + reqamount * clampedAmount * deconstructMultiplier, reqamount);

                    int accumulated = (int)(accumulator[i]); //get amount

                    if(clampedAmount > 0 && accumulated > 0){ //if it's positive, add it to the core
                        if(core != null){
                            int accepting = core.tile.block().acceptStack(requirements[i].item, accumulated, core.tile, builder);
                            core.tile.block().handleStack(requirements[i].item, accepting, core.tile, builder);
                            accumulator[i] -= accepting;
                        }else{
                            accumulator[i] -= accumulated;
                        }
                    }
                }
            }

            progress = Mathf.clamp(progress - amount);

            if(builder instanceof Player){
                builderID = builder.getID();
            }

            if(progress <= 0 || state.rules.infiniteResources){

                Block block = this.cblock;
                if(block == null) block = previous;
                if(block == null) block = Blocks.air;

                Call.onDeconstructFinish(tile, block, builderID);
            }
        }

        private float checkRequired(ItemModule inventory, float amount, boolean remove){
            float maxProgress = amount;

            for(int i = 0; i < cblock.requirements.length; i++){
                int sclamount = Math.round(state.rules.buildCostMultiplier * cblock.requirements[i].amount);
                int required = (int)(accumulator[i]); //calculate items that are required now

                if(inventory.get(cblock.requirements[i].item) == 0 && sclamount != 0){
                    maxProgress = 0f;
                }else if(required > 0){ //if this amount is positive...
                    //calculate how many items it can actually use
                    int maxUse = Math.min(required, inventory.get(cblock.requirements[i].item));
                    //get this as a fraction
                    float fraction = maxUse / (float)required;

                    //move max progress down if this fraction is less than 1
                    maxProgress = Math.min(maxProgress, maxProgress * fraction);

                    accumulator[i] -= maxUse;

                    //remove stuff that is actually used
                    if(remove){
                        inventory.remove(cblock.requirements[i].item, maxUse);
                    }
                }
                //else, no items are required yet, so just keep going
            }

            return maxProgress;
        }

        public float progress(){
            return progress;
        }

        public void setConstruct(Block previous, Block block){
            this.cblock = block;
            this.previous = previous;
            this.accumulator = new float[block.requirements.length];
            this.totalAccumulator = new float[block.requirements.length];
            this.buildCost = block.buildCost * state.rules.buildCostMultiplier;

            if(Nydus.launchpad_upgrading.active()){
                if(this.cblock == Blocks.vault){
                    if(proximity().contains(t -> t.block instanceof CoreBlock)){
                        setConstruct(previous, Blocks.launchPad);
                        Core.app.post(() -> netServer.titanic.add(tile));
                    }
                }
            }
        }

        public void setDeconstruct(Block previous){
            if(previous == null) return;
            this.previous = previous;
            this.progress = 1f;
            if(previous.buildCost >= 0.01f){
                this.cblock = previous;
                this.buildCost = previous.buildCost * state.rules.buildCostMultiplier;
            }else{
                this.buildCost = 20f; //default no-requirement build cost is 20
            }
            this.accumulator = new float[previous.requirements.length];
            this.totalAccumulator = new float[previous.requirements.length];
        }

        @Override
        public void update(){
            super.update();

            abandoned++;

            if(abandoned > 60 * 10 && !getTeam().cores().isEmpty()){
                deconstruct(null, getTeam().core(), 1f / buildCost * Time.delta() * 2.5f * state.rules.buildSpeedMultiplier);
                netServer.titanic.add(tile);
            }
        }

        @Override
        public void write(DataOutput stream) throws IOException{
            super.write(stream);
            stream.writeFloat(progress);
            stream.writeShort(previous == null ? -1 : previous.id);
            stream.writeShort(cblock == null ? -1 : cblock.id);

            if(accumulator == null){
                stream.writeByte(-1);
            }else{
                stream.writeByte(accumulator.length);
                for(int i = 0; i < accumulator.length; i++){
                    stream.writeFloat(accumulator[i]);
                    stream.writeFloat(totalAccumulator[i]);
                }
            }
        }

        @Override
        public void read(DataInput stream, byte revision) throws IOException{
            super.read(stream, revision);
            progress = stream.readFloat();
            short pid = stream.readShort();
            short rid = stream.readShort();
            byte acsize = stream.readByte();

            if(acsize != -1){
                accumulator = new float[acsize];
                totalAccumulator = new float[acsize];
                for(int i = 0; i < acsize; i++){
                    accumulator[i] = stream.readFloat();
                    totalAccumulator[i] = stream.readFloat();
                }
            }

            if(pid != -1) previous = content.block(pid);
            if(rid != -1) cblock = content.block(rid);

            if(cblock != null){
                buildCost = cblock.buildCost * state.rules.buildCostMultiplier;
            }else{
                buildCost = 20f;
            }
        }
    }
}
