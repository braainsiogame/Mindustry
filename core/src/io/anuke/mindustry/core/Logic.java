package io.anuke.mindustry.core;

import io.anuke.annotations.Annotations.*;
import io.anuke.arc.*;
import io.anuke.arc.util.*;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.content.*;
import io.anuke.mindustry.core.GameState.*;
import io.anuke.mindustry.ctype.*;
import io.anuke.mindustry.entities.*;
import io.anuke.mindustry.entities.type.*;
import io.anuke.mindustry.game.EventType.*;
import io.anuke.mindustry.game.*;
import io.anuke.mindustry.game.Teams.*;
import io.anuke.mindustry.gen.*;
import io.anuke.mindustry.type.*;
import io.anuke.mindustry.world.*;
import io.anuke.mindustry.world.blocks.*;
import io.anuke.mindustry.world.blocks.BuildBlock.*;
import io.anuke.mindustry.world.blocks.power.*;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

import static io.anuke.mindustry.Vars.*;

/**
 * Logic module.
 * Handles all logic for entities and waves.
 * Handles game state events.
 * Does not store any game state itself.
 * <p>
 * This class should <i>not</i> call any outside methods to change state of modules, but instead fire events.
 */
public class Logic implements ApplicationListener{

    public Logic(){
        Events.on(WaveEvent.class, event -> {
            for(Player p : playerGroup.all()){
                p.respawns = state.rules.respawns;
            }

            if(world.isZone()){
                world.getZone().updateWave(state.wave);
            }
        });

        Events.on(BlockDestroyEvent.class, event -> {
            //blocks that get broken are appended to the team's broken block queue
            Tile tile = event.tile;
            Block block = tile.block();
            //skip null entities or nukes, for obvious reasons
            if(tile.entity == null || tile.block() instanceof NuclearReactor) return;

            if(block instanceof BuildBlock){

                BuildEntity entity = tile.ent();

                //update block to reflect the fact that something was being constructed
                if(entity.cblock != null && entity.cblock.synthetic()){
                    block = entity.cblock;
                }else{
                    //otherwise this was a deconstruction that was interrupted, don't want to rebuild that
                    return;
                }
            }

            TeamData data = state.teams.get(tile.getTeam());

            //remove existing blocks that have been placed here.
            //painful O(n) iteration + copy
            for(int i = 0; i < data.brokenBlocks.size; i++){
                BrokenBlock b = data.brokenBlocks.get(i);
                if(b.x == tile.x && b.y == tile.y){
                    data.brokenBlocks.removeIndex(i);
                    break;
                }
            }

            data.brokenBlocks.addFirst(new BrokenBlock(tile.x, tile.y, tile.rotation(), block.id, tile.entity.config()));
        });

        Events.on(BlockBuildEndEvent.class, event -> {
            if(!event.breaking){
                TeamData data = state.teams.get(event.team);
                Iterator<BrokenBlock> it = data.brokenBlocks.iterator();
                while(it.hasNext()){
                    BrokenBlock b = it.next();
                    Block block = content.block(b.block);
                    if(event.tile.block().bounds(event.tile.x, event.tile.y, Tmp.r1).overlaps(block.bounds(b.x, b.y, Tmp.r2))){
                        it.remove();
                    }
                }
            }
        });
        try{
            FileOutputStream stream = new FileOutputStream(new File("C:\\Users\\ngkai\\Documents\\GitHub\\MinMod.ts\\testing\\mindustry.d.ts"));
            String typings = TypingsGenerator.generate(
                    BufferItem.class,
                    Call.class,
                    Icon.class,
                    MethodHash.class,
                    Musics.class,
                    PathTile.class,
                    PropCell.class,
                    RemoteReadClient.class,
                    RemoteReadServer.class,
                    Serialization.class,
                    Sounds.class,
                    Tex.class,
                    TileOp.class,

                    io.anuke.mindustry.ai.BlockIndexer.class,
                    io.anuke.mindustry.ai.Pathfinder.class,
                    io.anuke.mindustry.ai.WaveSpawner.class,
                    io.anuke.mindustry.ClientLauncher.class,
                    io.anuke.mindustry.content.Blocks.class,
                    io.anuke.mindustry.content.Bullets.class,
                    io.anuke.mindustry.content.Fx.class,
                    io.anuke.mindustry.content.Items.class,
                    io.anuke.mindustry.content.Liquids.class,
                    io.anuke.mindustry.content.Loadouts.class,
                    io.anuke.mindustry.content.Mechs.class,
                    io.anuke.mindustry.content.StatusEffects.class,
                    io.anuke.mindustry.content.TechTree.class,
                    io.anuke.mindustry.content.TypeIDs.class,
                    io.anuke.mindustry.content.UnitTypes.class,
                    io.anuke.mindustry.content.Zones.class,
                    io.anuke.mindustry.core.ContentLoader.class,
                    io.anuke.mindustry.core.Control.class,
                    io.anuke.mindustry.core.FileTree.class,
                    io.anuke.mindustry.core.GameState.class,
                    io.anuke.mindustry.core.Logic.class,
                    io.anuke.mindustry.core.NetClient.class,
                    io.anuke.mindustry.core.NetServer.class,
                    io.anuke.mindustry.core.Platform.class,
                    io.anuke.mindustry.core.Renderer.class,
                    io.anuke.mindustry.core.TypingsGenerator.class,
                    io.anuke.mindustry.core.UI.class,
                    io.anuke.mindustry.core.Version.class,
                    io.anuke.mindustry.core.World.class,
                    io.anuke.mindustry.ctype.Content.class,
                    io.anuke.mindustry.ctype.ContentList.class,
                    io.anuke.mindustry.ctype.MappableContent.class,
                    io.anuke.mindustry.ctype.UnlockableContent.class,
                    io.anuke.mindustry.editor.DrawOperation.class,
                    io.anuke.mindustry.editor.EditorTile.class,
                    io.anuke.mindustry.editor.EditorTool.class,
                    io.anuke.mindustry.editor.MapEditor.class,
                    io.anuke.mindustry.editor.MapEditorDialog.class,
                    io.anuke.mindustry.editor.MapGenerateDialog.class,
                    io.anuke.mindustry.editor.MapInfoDialog.class,
                    io.anuke.mindustry.editor.MapLoadDialog.class,
                    io.anuke.mindustry.editor.MapRenderer.class,
                    io.anuke.mindustry.editor.MapResizeDialog.class,
                    io.anuke.mindustry.editor.MapSaveDialog.class,
                    io.anuke.mindustry.editor.MapView.class,
                    io.anuke.mindustry.editor.OperationStack.class,
                    io.anuke.mindustry.editor.WaveInfoDialog.class,
                    io.anuke.mindustry.entities.bullet.ArtilleryBulletType.class,
                    io.anuke.mindustry.entities.bullet.BasicBulletType.class,
                    io.anuke.mindustry.entities.bullet.BombBulletType.class,
                    io.anuke.mindustry.entities.bullet.BulletType.class,
                    io.anuke.mindustry.entities.bullet.FlakBulletType.class,
                    io.anuke.mindustry.entities.bullet.HealBulletType.class,
                    io.anuke.mindustry.entities.bullet.LiquidBulletType.class,
                    io.anuke.mindustry.entities.bullet.MassDriverBolt.class,
                    io.anuke.mindustry.entities.bullet.MissileBulletType.class,
                    io.anuke.mindustry.entities.Damage.class,
                    io.anuke.mindustry.entities.effect.Decal.class,
                    io.anuke.mindustry.entities.effect.Fire.class,
                    io.anuke.mindustry.entities.effect.GroundEffectEntity.class,
                    io.anuke.mindustry.entities.effect.ItemTransfer.class,
                    io.anuke.mindustry.entities.effect.Lightning.class,
                    io.anuke.mindustry.entities.effect.Puddle.class,
                    io.anuke.mindustry.entities.effect.RubbleDecal.class,
                    io.anuke.mindustry.entities.effect.ScorchDecal.class,
                    io.anuke.mindustry.entities.Effects.class,
                    io.anuke.mindustry.entities.Entities.class,
                    io.anuke.mindustry.entities.EntityCollisions.class,
                    io.anuke.mindustry.entities.EntityGroup.class,
                    io.anuke.mindustry.entities.Predict.class,
                    io.anuke.mindustry.entities.TargetPriority.class,
                    io.anuke.mindustry.entities.traits.AbsorbTrait.class,
                    io.anuke.mindustry.entities.traits.BelowLiquidTrait.class,
                    io.anuke.mindustry.entities.traits.BuilderMinerTrait.class,
                    io.anuke.mindustry.entities.traits.BuilderTrait.class,
                    io.anuke.mindustry.entities.traits.DamageTrait.class,
                    io.anuke.mindustry.entities.traits.DrawTrait.class,
                    io.anuke.mindustry.entities.traits.Entity.class,
                    io.anuke.mindustry.entities.traits.HealthTrait.class,
                    io.anuke.mindustry.entities.traits.KillerTrait.class,
                    io.anuke.mindustry.entities.traits.MinerTrait.class,
                    io.anuke.mindustry.entities.traits.MoveTrait.class,
                    io.anuke.mindustry.entities.traits.Saveable.class,
                    io.anuke.mindustry.entities.traits.SaveTrait.class,
                    io.anuke.mindustry.entities.traits.ScaleTrait.class,
                    io.anuke.mindustry.entities.traits.ShooterTrait.class,
                    io.anuke.mindustry.entities.traits.SolidTrait.class,
                    io.anuke.mindustry.entities.traits.SpawnerTrait.class,
                    io.anuke.mindustry.entities.traits.SyncTrait.class,
                    io.anuke.mindustry.entities.traits.TargetTrait.class,
                    io.anuke.mindustry.entities.traits.TeamTrait.class,
                    io.anuke.mindustry.entities.traits.TimeTrait.class,
                    io.anuke.mindustry.entities.traits.TypeTrait.class,
                    io.anuke.mindustry.entities.traits.VelocityTrait.class,
                    io.anuke.mindustry.entities.type.base.BaseDrone.class,
                    io.anuke.mindustry.entities.type.base.BuilderDrone.class,
                    io.anuke.mindustry.entities.type.base.FlyingUnit.class,
                    io.anuke.mindustry.entities.type.base.GroundUnit.class,
                    io.anuke.mindustry.entities.type.base.HoverUnit.class,
                    io.anuke.mindustry.entities.type.base.MinerDrone.class,
                    io.anuke.mindustry.entities.type.base.RepairDrone.class,
                    io.anuke.mindustry.entities.type.BaseEntity.class,
                    io.anuke.mindustry.entities.type.BaseUnit.class,
                    io.anuke.mindustry.entities.type.Bullet.class,
                    io.anuke.mindustry.entities.type.DestructibleEntity.class,
                    io.anuke.mindustry.entities.type.EffectEntity.class,
                    io.anuke.mindustry.entities.type.Player.class,
                    io.anuke.mindustry.entities.type.SolidEntity.class,
                    io.anuke.mindustry.entities.type.TileEntity.class,
                    io.anuke.mindustry.entities.type.TimedEntity.class,
                    io.anuke.mindustry.entities.type.Unit.class,
                    io.anuke.mindustry.entities.units.StateMachine.class,
                    io.anuke.mindustry.entities.units.Statuses.class,
                    io.anuke.mindustry.entities.units.UnitCommand.class,
                    io.anuke.mindustry.entities.units.UnitDrops.class,
                    io.anuke.mindustry.entities.units.UnitState.class,
                    io.anuke.mindustry.entities.Units.class,
                    io.anuke.mindustry.game.DefaultWaves.class,
                    io.anuke.mindustry.game.Difficulty.class,
                    io.anuke.mindustry.game.EventType.class,
                    io.anuke.mindustry.game.Gamemode.class,
                    io.anuke.mindustry.game.GlobalData.class,
                    io.anuke.mindustry.game.LoopControl.class,
                    io.anuke.mindustry.game.MusicControl.class,
                    io.anuke.mindustry.game.Objective.class,
                    io.anuke.mindustry.game.Objectives.class,
                    io.anuke.mindustry.game.Rules.class,
                    io.anuke.mindustry.game.Saves.class,
                    io.anuke.mindustry.game.Schematic.class,
                    io.anuke.mindustry.game.Schematics.class,
                    io.anuke.mindustry.game.SoundLoop.class,
                    io.anuke.mindustry.game.SpawnGroup.class,
                    io.anuke.mindustry.game.Stats.class,
                    io.anuke.mindustry.game.Team.class,
                    io.anuke.mindustry.game.Teams.class,
                    io.anuke.mindustry.game.Tutorial.class,
                    io.anuke.mindustry.graphics.BlockRenderer.class,
                    io.anuke.mindustry.graphics.Bloom.class,
                    io.anuke.mindustry.graphics.CacheLayer.class,
                    io.anuke.mindustry.graphics.Drawf.class,
                    io.anuke.mindustry.graphics.FloorRenderer.class,
                    io.anuke.mindustry.graphics.IndexedRenderer.class,
                    io.anuke.mindustry.graphics.Layer.class,
                    io.anuke.mindustry.graphics.LightRenderer.class,
                    io.anuke.mindustry.graphics.MenuRenderer.class,
                    io.anuke.mindustry.graphics.MinimapRenderer.class,
                    io.anuke.mindustry.graphics.MultiPacker.class,
                    io.anuke.mindustry.graphics.OverlayRenderer.class,
                    io.anuke.mindustry.graphics.Pal.class,
                    io.anuke.mindustry.graphics.Pixelator.class,
                    io.anuke.mindustry.graphics.Shaders.class,
                    io.anuke.mindustry.input.Binding.class,
                    io.anuke.mindustry.input.DesktopInput.class,
                    io.anuke.mindustry.input.InputHandler.class,
                    io.anuke.mindustry.input.MobileInput.class,
                    io.anuke.mindustry.input.Placement.class,
                    io.anuke.mindustry.input.PlaceMode.class,
                    io.anuke.mindustry.io.JsonIO.class,
                    io.anuke.mindustry.io.LegacyMapIO.class,
                    io.anuke.mindustry.io.MapIO.class,
                    io.anuke.mindustry.io.SaveFileReader.class,
                    io.anuke.mindustry.io.SaveIO.class,
                    io.anuke.mindustry.io.SaveMeta.class,
                    io.anuke.mindustry.io.SavePreviewLoader.class,
                    io.anuke.mindustry.io.SaveVersion.class,
                    io.anuke.mindustry.io.TypeIO.class,
                    io.anuke.mindustry.io.versions.LegacyTypeTable.class,
                    io.anuke.mindustry.io.versions.Save1.class,
                    io.anuke.mindustry.io.versions.Save2.class,
                    io.anuke.mindustry.io.versions.Save3.class,
                    io.anuke.mindustry.maps.filters.BlendFilter.class,
                    io.anuke.mindustry.maps.filters.ClearFilter.class,
                    io.anuke.mindustry.maps.filters.DistortFilter.class,
                    io.anuke.mindustry.maps.filters.FilterOption.class,
                    io.anuke.mindustry.maps.filters.GenerateFilter.class,
                    io.anuke.mindustry.maps.filters.MedianFilter.class,
                    io.anuke.mindustry.maps.filters.MirrorFilter.class,
                    io.anuke.mindustry.maps.filters.NoiseFilter.class,
                    io.anuke.mindustry.maps.filters.OreFilter.class,
                    io.anuke.mindustry.maps.filters.OreMedianFilter.class,
                    io.anuke.mindustry.maps.filters.RiverNoiseFilter.class,
                    io.anuke.mindustry.maps.filters.ScatterFilter.class,
                    io.anuke.mindustry.maps.filters.TerrainFilter.class,
                    io.anuke.mindustry.maps.generators.BasicGenerator.class,
                    io.anuke.mindustry.maps.generators.Generator.class,
                    io.anuke.mindustry.maps.generators.MapGenerator.class,
                    io.anuke.mindustry.maps.generators.RandomGenerator.class,
                    io.anuke.mindustry.maps.Map.class,
                    io.anuke.mindustry.maps.MapException.class,
                    io.anuke.mindustry.maps.MapPreviewLoader.class,
                    io.anuke.mindustry.maps.Maps.class,
                    io.anuke.mindustry.maps.zonegen.DesertWastesGenerator.class,
                    io.anuke.mindustry.maps.zonegen.OvergrowthGenerator.class,
                    io.anuke.mindustry.mod.ClassAccess.class,
                    io.anuke.mindustry.mod.ContentParser.class,
                    io.anuke.mindustry.mod.Mod.class,
                    io.anuke.mindustry.mod.ModCrashHandler.class,
                    io.anuke.mindustry.mod.ModLoadingSound.class,
                    io.anuke.mindustry.mod.Mods.class,
                    io.anuke.mindustry.mod.Scripts.class,
                    io.anuke.mindustry.net.Administration.class,
                    io.anuke.mindustry.net.ArcNetProvider.class,
                    io.anuke.mindustry.net.CrashSender.class,
                    io.anuke.mindustry.net.Host.class,
                    io.anuke.mindustry.net.Interpolator.class,
                    io.anuke.mindustry.net.Net.class,
                    io.anuke.mindustry.net.NetConnection.class,
                    io.anuke.mindustry.net.NetworkIO.class,
                    io.anuke.mindustry.net.Packet.class,
                    io.anuke.mindustry.net.Packets.class,
                    io.anuke.mindustry.net.Registrator.class,
                    io.anuke.mindustry.net.Streamable.class,
                    io.anuke.mindustry.net.ValidateException.class,
                    io.anuke.mindustry.plugin.Plugin.class,
                    io.anuke.mindustry.type.Category.class,
                    io.anuke.mindustry.type.ContentType.class,
                    io.anuke.mindustry.type.Item.class,
                    io.anuke.mindustry.type.ItemStack.class,
                    io.anuke.mindustry.type.ItemType.class,
                    io.anuke.mindustry.type.Liquid.class,
                    io.anuke.mindustry.type.LiquidStack.class,
                    io.anuke.mindustry.type.Mech.class,
                    io.anuke.mindustry.type.Publishable.class,
                    io.anuke.mindustry.type.StatusEffect.class,
                    io.anuke.mindustry.type.TypeID.class,
                    io.anuke.mindustry.type.UnitType.class,
                    io.anuke.mindustry.type.Weapon.class,
                    io.anuke.mindustry.type.WeatherEvent.class,
                    io.anuke.mindustry.type.Zone.class,
                    io.anuke.mindustry.ui.Bar.class,
                    io.anuke.mindustry.ui.BorderImage.class,
                    io.anuke.mindustry.ui.Cicon.class,
                    io.anuke.mindustry.ui.ContentDisplay.class,
                    io.anuke.mindustry.ui.dialogs.AboutDialog.class,
                    io.anuke.mindustry.ui.dialogs.AdminsDialog.class,
                    io.anuke.mindustry.ui.dialogs.BansDialog.class,
                    io.anuke.mindustry.ui.dialogs.ColorPicker.class,
                    io.anuke.mindustry.ui.dialogs.ContentInfoDialog.class,
                    io.anuke.mindustry.ui.dialogs.ControlsDialog.class,
                    io.anuke.mindustry.ui.dialogs.CustomGameDialog.class,
                    io.anuke.mindustry.ui.dialogs.CustomRulesDialog.class,
                    io.anuke.mindustry.ui.dialogs.DatabaseDialog.class,
                    io.anuke.mindustry.ui.dialogs.DeployDialog.class,
                    io.anuke.mindustry.ui.dialogs.DiscordDialog.class,
                    io.anuke.mindustry.ui.dialogs.FileChooser.class,
                    io.anuke.mindustry.ui.dialogs.FloatingDialog.class,
                    io.anuke.mindustry.ui.dialogs.GameOverDialog.class,
                    io.anuke.mindustry.ui.dialogs.HostDialog.class,
                    io.anuke.mindustry.ui.dialogs.JoinDialog.class,
                    io.anuke.mindustry.ui.dialogs.LanguageDialog.class,
                    io.anuke.mindustry.ui.dialogs.LoadDialog.class,
                    io.anuke.mindustry.ui.dialogs.LoadoutDialog.class,
                    io.anuke.mindustry.ui.dialogs.MapPlayDialog.class,
                    io.anuke.mindustry.ui.dialogs.MapsDialog.class,
                    io.anuke.mindustry.ui.dialogs.MinimapDialog.class,
                    io.anuke.mindustry.ui.dialogs.ModsDialog.class,
                    io.anuke.mindustry.ui.dialogs.PaletteDialog.class,
                    io.anuke.mindustry.ui.dialogs.PausedDialog.class,
                    io.anuke.mindustry.ui.dialogs.SaveDialog.class,
                    io.anuke.mindustry.ui.dialogs.SchematicsDialog.class,
                    io.anuke.mindustry.ui.dialogs.SettingsMenuDialog.class,
                    io.anuke.mindustry.ui.dialogs.TechTreeDialog.class,
                    io.anuke.mindustry.ui.dialogs.TraceDialog.class,
                    io.anuke.mindustry.ui.dialogs.ZoneInfoDialog.class,
                    io.anuke.mindustry.ui.Fonts.class,
                    io.anuke.mindustry.ui.fragments.BlockConfigFragment.class,
                    io.anuke.mindustry.ui.fragments.BlockInventoryFragment.class,
                    io.anuke.mindustry.ui.fragments.ChatFragment.class,
                    io.anuke.mindustry.ui.fragments.FadeInFragment.class,
                    io.anuke.mindustry.ui.fragments.Fragment.class,
                    io.anuke.mindustry.ui.fragments.HudFragment.class,
                    io.anuke.mindustry.ui.fragments.LoadingFragment.class,
                    io.anuke.mindustry.ui.fragments.MenuFragment.class,
                    io.anuke.mindustry.ui.fragments.OverlayFragment.class,
                    io.anuke.mindustry.ui.fragments.PlacementFragment.class,
                    io.anuke.mindustry.ui.fragments.PlayerListFragment.class,
                    io.anuke.mindustry.ui.fragments.ScriptConsoleFragment.class,
                    io.anuke.mindustry.ui.GridImage.class,
                    io.anuke.mindustry.ui.IconSize.class,
                    io.anuke.mindustry.ui.IntFormat.class,
                    io.anuke.mindustry.ui.ItemDisplay.class,
                    io.anuke.mindustry.ui.ItemImage.class,
                    io.anuke.mindustry.ui.ItemsDisplay.class,
                    io.anuke.mindustry.ui.layout.BranchTreeLayout.class,
                    io.anuke.mindustry.ui.layout.RadialTreeLayout.class,
                    io.anuke.mindustry.ui.layout.TreeLayout.class,
                    io.anuke.mindustry.ui.Links.class,
                    io.anuke.mindustry.ui.LiquidDisplay.class,
                    io.anuke.mindustry.ui.Minimap.class,
                    io.anuke.mindustry.ui.MobileButton.class,
                    io.anuke.mindustry.ui.MultiReqImage.class,
                    io.anuke.mindustry.ui.ReqImage.class,
                    io.anuke.mindustry.ui.Styles.class,
                    io.anuke.mindustry.Vars.class,
                    io.anuke.mindustry.world.Block.class,
                    io.anuke.mindustry.world.blocks.Attributes.class,
                    io.anuke.mindustry.world.blocks.Autotiler.class,
                    io.anuke.mindustry.world.blocks.BlockPart.class,
                    io.anuke.mindustry.world.blocks.BuildBlock.class,
                    io.anuke.mindustry.world.blocks.defense.DeflectorWall.class,
                    io.anuke.mindustry.world.blocks.defense.Door.class,
                    io.anuke.mindustry.world.blocks.defense.ForceProjector.class,
                    io.anuke.mindustry.world.blocks.defense.MendProjector.class,
                    io.anuke.mindustry.world.blocks.defense.OverdriveProjector.class,
                    io.anuke.mindustry.world.blocks.defense.ShockMine.class,
                    io.anuke.mindustry.world.blocks.defense.SurgeWall.class,
                    io.anuke.mindustry.world.blocks.defense.turrets.ArtilleryTurret.class,
                    io.anuke.mindustry.world.blocks.defense.turrets.BurstTurret.class,
                    io.anuke.mindustry.world.blocks.defense.turrets.ChargeTurret.class,
                    io.anuke.mindustry.world.blocks.defense.turrets.CooledTurret.class,
                    io.anuke.mindustry.world.blocks.defense.turrets.DoubleTurret.class,
                    io.anuke.mindustry.world.blocks.defense.turrets.ItemTurret.class,
                    io.anuke.mindustry.world.blocks.defense.turrets.LaserTurret.class,
                    io.anuke.mindustry.world.blocks.defense.turrets.LiquidTurret.class,
                    io.anuke.mindustry.world.blocks.defense.turrets.PowerTurret.class,
                    io.anuke.mindustry.world.blocks.defense.turrets.Turret.class,
                    io.anuke.mindustry.world.blocks.defense.Wall.class,
                    io.anuke.mindustry.world.blocks.distribution.ArmoredConveyor.class,
                    io.anuke.mindustry.world.blocks.distribution.BufferedItemBridge.class,
                    io.anuke.mindustry.world.blocks.distribution.Conveyor.class,
                    io.anuke.mindustry.world.blocks.distribution.ExtendingItemBridge.class,
                    io.anuke.mindustry.world.blocks.distribution.ItemBridge.class,
                    io.anuke.mindustry.world.blocks.distribution.Junction.class,
                    io.anuke.mindustry.world.blocks.distribution.MassDriver.class,
                    io.anuke.mindustry.world.blocks.distribution.OverflowGate.class,
                    io.anuke.mindustry.world.blocks.distribution.Router.class,
                    io.anuke.mindustry.world.blocks.distribution.Sorter.class,
                    io.anuke.mindustry.world.blocks.DoubleOverlayFloor.class,
                    io.anuke.mindustry.world.blocks.Floor.class,
                    io.anuke.mindustry.world.blocks.ItemSelection.class,
                    io.anuke.mindustry.world.blocks.liquid.ArmoredConduit.class,
                    io.anuke.mindustry.world.blocks.liquid.Conduit.class,
                    io.anuke.mindustry.world.blocks.liquid.LiquidBridge.class,
                    io.anuke.mindustry.world.blocks.liquid.LiquidExtendingBridge.class,
                    io.anuke.mindustry.world.blocks.liquid.LiquidJunction.class,
                    io.anuke.mindustry.world.blocks.liquid.LiquidOverflowGate.class,
                    io.anuke.mindustry.world.blocks.liquid.LiquidRouter.class,
                    io.anuke.mindustry.world.blocks.liquid.LiquidTank.class,
                    io.anuke.mindustry.world.blocks.LiquidBlock.class,
                    io.anuke.mindustry.world.blocks.logic.LogicBlock.class,
                    io.anuke.mindustry.world.blocks.logic.MessageBlock.class,
                    io.anuke.mindustry.world.blocks.OreBlock.class,
                    io.anuke.mindustry.world.blocks.OverlayFloor.class,
                    io.anuke.mindustry.world.blocks.power.Battery.class,
                    io.anuke.mindustry.world.blocks.power.BurnerGenerator.class,
                    io.anuke.mindustry.world.blocks.power.ConditionalConsumePower.class,
                    io.anuke.mindustry.world.blocks.power.DecayGenerator.class,
                    io.anuke.mindustry.world.blocks.power.ImpactReactor.class,
                    io.anuke.mindustry.world.blocks.power.ItemLiquidGenerator.class,
                    io.anuke.mindustry.world.blocks.power.LightBlock.class,
                    io.anuke.mindustry.world.blocks.power.NuclearReactor.class,
                    io.anuke.mindustry.world.blocks.power.PowerDiode.class,
                    io.anuke.mindustry.world.blocks.power.PowerDistributor.class,
                    io.anuke.mindustry.world.blocks.power.PowerGenerator.class,
                    io.anuke.mindustry.world.blocks.power.PowerGraph.class,
                    io.anuke.mindustry.world.blocks.power.PowerNode.class,
                    io.anuke.mindustry.world.blocks.power.SingleTypeGenerator.class,
                    io.anuke.mindustry.world.blocks.power.SolarGenerator.class,
                    io.anuke.mindustry.world.blocks.power.ThermalGenerator.class,
                    io.anuke.mindustry.world.blocks.PowerBlock.class,
                    io.anuke.mindustry.world.blocks.production.Cultivator.class,
                    io.anuke.mindustry.world.blocks.production.Drill.class,
                    io.anuke.mindustry.world.blocks.production.Fracker.class,
                    io.anuke.mindustry.world.blocks.production.GenericCrafter.class,
                    io.anuke.mindustry.world.blocks.production.GenericSmelter.class,
                    io.anuke.mindustry.world.blocks.production.Incinerator.class,
                    io.anuke.mindustry.world.blocks.production.LiquidConverter.class,
                    io.anuke.mindustry.world.blocks.production.Pump.class,
                    io.anuke.mindustry.world.blocks.production.Separator.class,
                    io.anuke.mindustry.world.blocks.production.SolidPump.class,
                    io.anuke.mindustry.world.blocks.RespawnBlock.class,
                    io.anuke.mindustry.world.blocks.Rock.class,
                    io.anuke.mindustry.world.blocks.sandbox.ItemSource.class,
                    io.anuke.mindustry.world.blocks.sandbox.ItemVoid.class,
                    io.anuke.mindustry.world.blocks.sandbox.LiquidSource.class,
                    io.anuke.mindustry.world.blocks.sandbox.PowerSource.class,
                    io.anuke.mindustry.world.blocks.sandbox.PowerVoid.class,
                    io.anuke.mindustry.world.blocks.StaticWall.class,
                    io.anuke.mindustry.world.blocks.storage.CoreBlock.class,
                    io.anuke.mindustry.world.blocks.storage.LaunchPad.class,
                    io.anuke.mindustry.world.blocks.storage.StorageBlock.class,
                    io.anuke.mindustry.world.blocks.storage.Unloader.class,
                    io.anuke.mindustry.world.blocks.storage.Vault.class,
                    io.anuke.mindustry.world.blocks.TreeBlock.class,
                    io.anuke.mindustry.world.blocks.units.CommandCenter.class,
                    io.anuke.mindustry.world.blocks.units.MechPad.class,
                    io.anuke.mindustry.world.blocks.units.RallyPoint.class,
                    io.anuke.mindustry.world.blocks.units.RepairPoint.class,
                    io.anuke.mindustry.world.blocks.units.UnitFactory.class,
                    io.anuke.mindustry.world.BlockStorage.class,
                    io.anuke.mindustry.world.Build.class,
                    io.anuke.mindustry.world.CachedTile.class,
                    io.anuke.mindustry.world.consumers.Consume.class,
                    io.anuke.mindustry.world.consumers.ConsumeItemFilter.class,
                    io.anuke.mindustry.world.consumers.ConsumeItems.class,
                    io.anuke.mindustry.world.consumers.ConsumeLiquid.class,
                    io.anuke.mindustry.world.consumers.ConsumeLiquidBase.class,
                    io.anuke.mindustry.world.consumers.ConsumeLiquidFilter.class,
                    io.anuke.mindustry.world.consumers.ConsumePower.class,
                    io.anuke.mindustry.world.consumers.Consumers.class,
                    io.anuke.mindustry.world.consumers.ConsumeType.class,
                    io.anuke.mindustry.world.DirectionalItemBuffer.class,
                    io.anuke.mindustry.world.Edges.class,
                    io.anuke.mindustry.world.ItemBuffer.class,
                    io.anuke.mindustry.world.LegacyColorMapper.class,
                    io.anuke.mindustry.world.meta.Attribute.class,
                    io.anuke.mindustry.world.meta.BlockBars.class,
                    io.anuke.mindustry.world.meta.BlockFlag.class,
                    io.anuke.mindustry.world.meta.BlockGroup.class,
                    io.anuke.mindustry.world.meta.BlockStat.class,
                    io.anuke.mindustry.world.meta.BlockStats.class,
                    io.anuke.mindustry.world.meta.BuildVisibility.class,
                    io.anuke.mindustry.world.meta.PowerType.class,
                    io.anuke.mindustry.world.meta.Producers.class,
                    io.anuke.mindustry.world.meta.StatCategory.class,
                    io.anuke.mindustry.world.meta.StatUnit.class,
                    io.anuke.mindustry.world.meta.StatValue.class,
                    io.anuke.mindustry.world.meta.values.AmmoListValue.class,
                    io.anuke.mindustry.world.meta.values.BooleanValue.class,
                    io.anuke.mindustry.world.meta.values.BoosterListValue.class,
                    io.anuke.mindustry.world.meta.values.ItemFilterValue.class,
                    io.anuke.mindustry.world.meta.values.ItemListValue.class,
                    io.anuke.mindustry.world.meta.values.LiquidFilterValue.class,
                    io.anuke.mindustry.world.meta.values.LiquidValue.class,
                    io.anuke.mindustry.world.meta.values.NumberValue.class,
                    io.anuke.mindustry.world.meta.values.StringValue.class,
                    io.anuke.mindustry.world.modules.BlockModule.class,
                    io.anuke.mindustry.world.modules.ConsumeModule.class,
                    io.anuke.mindustry.world.modules.ItemModule.class,
                    io.anuke.mindustry.world.modules.LiquidModule.class,
                    io.anuke.mindustry.world.modules.PowerModule.class,
                    io.anuke.mindustry.world.Pos.class,
                    io.anuke.mindustry.world.producers.Produce.class,
                    io.anuke.mindustry.world.producers.ProduceItem.class,
                    io.anuke.mindustry.world.StaticTree.class,
                    io.anuke.mindustry.world.Tile.class,
                    io.anuke.mindustry.world.WorldContext.class
            );
            stream.write(typings.getBytes());
            stream.close();
        } catch(Exception e) {
            Log.err(e);
        }
    }

    /** Handles the event of content being used by either the player or some block. */
    public void handleContent(UnlockableContent content){
        if(!headless){
            data.unlockContent(content);
        }
    }

    public void play(){
        state.set(State.playing);
        state.wavetime = state.rules.waveSpacing * 2; //grace period of 2x wave time before game starts
        Events.fire(new PlayEvent());

        //add starting items
        if(!world.isZone()){
            for(Team team : Team.all){
                if(!state.teams.get(team).cores.isEmpty()){
                    TileEntity entity = state.teams.get(team).cores.first().entity;
                    entity.items.clear();
                    for(ItemStack stack : state.rules.loadout){
                        entity.items.add(stack.item, stack.amount);
                    }
                }
            }
        }
    }

    public void reset(){
        state.wave = 1;
        state.wavetime = state.rules.waveSpacing;
        state.gameOver = state.launched = false;
        state.teams = new Teams();
        state.rules = new Rules();
        state.stats = new Stats();

        entities.clear();
        Time.clear();
        TileEntity.sleepingEntities = 0;

        Events.fire(new ResetEvent());
    }

    public void runWave(){
        spawner.spawnEnemies();
        state.wave++;
        state.wavetime = world.isZone() && world.getZone().isLaunchWave(state.wave) ? state.rules.waveSpacing * state.rules.launchWaveMultiplier : state.rules.waveSpacing;

        Events.fire(new WaveEvent());
    }

    private void checkGameOver(){
        if(!state.rules.attackMode && state.teams.get(defaultTeam).cores.size == 0 && !state.gameOver){
            state.gameOver = true;
            Events.fire(new GameOverEvent(waveTeam));
        }else if(state.rules.attackMode){
            Team alive = null;

            for(Team team : Team.all){
                if(state.teams.get(team).cores.size > 0){
                    if(alive != null){
                        return;
                    }
                    alive = team;
                }
            }

            if(alive != null && !state.gameOver){
                if(world.isZone() && alive == defaultTeam){
                    //in attack maps, a victorious game over is equivalent to a launch
                    Call.launchZone();
                }else{
                    Events.fire(new GameOverEvent(alive));
                }
                state.gameOver = true;
            }
        }
    }

    @Remote(called = Loc.both)
    public static void launchZone(){
        if(!headless){
            ui.hudfrag.showLaunch();
        }

        for(Tile tile : state.teams.get(defaultTeam).cores){
            Effects.effect(Fx.launch, tile);
        }

        if(world.getZone() != null){
            world.getZone().setLaunched();
        }

        Time.runTask(30f, () -> {
            for(Tile tile : state.teams.get(defaultTeam).cores){
                for(Item item : content.items()){
                    if(tile == null || tile.entity == null || tile.entity.items == null) continue;
                    data.addItem(item, tile.entity.items.get(item));
                    Events.fire(new LaunchItemEvent(item, tile.entity.items.get(item)));
                }
                world.removeBlock(tile);
            }
            state.launched = true;
            state.gameOver = true;
            Events.fire(new LaunchEvent());
            //manually fire game over event now
            Events.fire(new GameOverEvent(defaultTeam));
        });
    }

    @Remote(called = Loc.both)
    public static void onGameOver(Team winner){
        state.stats.wavesLasted = state.wave;
        ui.restart.show(winner);
        netClient.setQuiet();
    }

    @Override
    public void update(){

        if(!state.is(State.menu)){

            if(!state.isPaused()){
                Time.update();

                if(state.rules.waves && state.rules.waveTimer && !state.gameOver){
                    if(!state.rules.waitForWaveToEnd || unitGroups[waveTeam.ordinal()].size() == 0){
                        state.wavetime = Math.max(state.wavetime - Time.delta(), 0);
                    }
                }

                if(!net.client() && state.wavetime <= 0 && state.rules.waves){
                    runWave();
                }

                if(!headless){
                    effectGroup.update();
                    groundEffectGroup.update();
                }

                if(!state.isEditor()){
                    for(EntityGroup group : unitGroups){
                        group.update();
                    }

                    puddleGroup.update();
                    shieldGroup.update();
                    bulletGroup.update();
                    tileGroup.update();
                    fireGroup.update();
                }else{
                    for(EntityGroup<?> group : unitGroups){
                        group.updateEvents();
                        collisions.updatePhysics(group);
                    }
                }


                playerGroup.update();

                //effect group only contains item transfers in the headless version, update it!
                if(headless){
                    effectGroup.update();
                }

                if(!state.isEditor()){

                    for(EntityGroup group : unitGroups){
                        if(group.isEmpty()) continue;
                        collisions.collideGroups(bulletGroup, group);
                    }

                    collisions.collideGroups(bulletGroup, playerGroup);
                }
            }

            if(!net.client() && !world.isInvalidMap() && !state.isEditor()){
                checkGameOver();
            }
        }
    }
}
