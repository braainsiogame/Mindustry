package mindustry.core;

import arc.*;
import arc.util.*;
import mindustry.annotations.Annotations.*;
import mindustry.content.*;
import mindustry.core.GameState.*;
import mindustry.core.typedefs.TypeConverter;
import mindustry.ctype.*;
import mindustry.entities.*;
import mindustry.entities.type.*;
import mindustry.game.EventType.*;
import mindustry.game.*;
import mindustry.game.Teams.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.BuildBlock.*;
import mindustry.world.blocks.power.*;
import org.mozilla.javascript.JavaAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

import static mindustry.Vars.*;

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
            //skip null entities or nukes, for obvious reasons; also skip client since they can't modify these requests
            if(tile.entity == null || tile.block() instanceof NuclearReactor || net.client()) return;

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
            Class[] classes = new Class[]{
                    arc.Application.class,
                    arc.ApplicationCore.class,
                    arc.ApplicationListener.class,
                    arc.Audio.class,
                    arc.Core.class,
                    arc.Events.class,
                    arc.Files.class,
                    arc.func.Boolc.class,
                    arc.func.Boolf.class,
                    arc.func.Boolf2.class,
                    arc.func.Boolp.class,
                    arc.func.Cons.class,
                    arc.func.Cons2.class,
                    arc.func.Floatc.class,
                    arc.func.Floatc2.class,
                    arc.func.Floatc4.class,
                    arc.func.Floatf.class,
                    arc.func.Floatp.class,
                    arc.func.Func.class,
                    arc.func.Func2.class,
                    arc.func.Func3.class,
                    arc.func.Intc.class,
                    arc.func.Intc2.class,
                    arc.func.Intc4.class,
                    arc.func.Intf.class,
                    arc.func.Intp.class,
                    arc.func.Prov.class,
                    arc.graphics.Blending.class,
                    arc.graphics.Camera.class,
                    arc.graphics.Color.class,
                    arc.graphics.Colors.class,
                    arc.graphics.Cubemap.class,
                    arc.graphics.CubemapData.class,
                    arc.graphics.g2d.Animation.class,
                    arc.graphics.g2d.BatchShader.class,
                    arc.graphics.g2d.BitmapFont.class,
                    arc.graphics.g2d.BitmapFontCache.class,
                    arc.graphics.g2d.CacheBatch.class,
                    arc.graphics.g2d.CapStyle.class,
                    arc.graphics.g2d.DistanceFieldFont.class,
                    arc.graphics.g2d.Draw.class,
                    arc.graphics.g2d.Fill.class,
                    arc.graphics.g2d.GlyphLayout.class,
                    arc.graphics.g2d.Lines.class,
                    arc.graphics.g2d.MultiCacheBatch.class,
                    arc.graphics.g2d.NinePatch.class,
                    arc.graphics.g2d.PixmapPacker.class,
                    arc.graphics.g2d.PixmapPackerIO.class,
                    arc.graphics.g2d.PixmapRegion.class,
                    arc.graphics.g2d.ScissorStack.class,
                    arc.graphics.g2d.SpriteBatch.class,
                    arc.graphics.g2d.SpriteCache.class,
                    arc.graphics.g2d.TextureAtlas.class,
                    arc.graphics.g2d.TextureRegion.class,
                    arc.graphics.Gl.class,
                    arc.graphics.GL20.class,
                    arc.graphics.GL30.class,
                    arc.graphics.GLTexture.class,
                    arc.graphics.Mesh.class,
                    arc.graphics.Pixmap.class,
                    arc.graphics.PixmapIO.class,
                    arc.graphics.Pixmaps.class,
                    arc.graphics.Texture.class,
                    arc.graphics.TextureArray.class,
                    arc.graphics.TextureArrayData.class,
                    arc.graphics.TextureData.class,
                    arc.graphics.VertexAttribute.class,
                    arc.graphics.VertexAttributes.class,
                    arc.Graphics.class,
                    arc.Input.class,
                    arc.KeyBinds.class,
                    arc.math.Affine2.class,
                    arc.math.Angles.class,
                    arc.math.CumulativeDistribution.class,
                    arc.math.DelaunayTriangulator.class,
                    arc.math.EarClippingTriangulator.class,
                    arc.math.Extrapolator.class,
                    arc.math.FloatCounter.class,
                    arc.math.geom.Bezier.class,
                    arc.math.geom.Bresenham2.class,
                    arc.math.geom.BSpline.class,
                    arc.math.geom.CatmullRomSpline.class,
                    arc.math.geom.Circle.class,
                    arc.math.geom.ConvexHull.class,
                    arc.math.geom.Ellipse.class,
                    arc.math.geom.FixedPosition.class,
                    arc.math.geom.Geometry.class,
                    arc.math.geom.Intersector.class,
                    arc.math.geom.Path.class,
                    arc.math.geom.Point2.class,
                    arc.math.geom.Point3.class,
                    arc.math.geom.Polygon.class,
                    arc.math.geom.Polyline.class,
                    arc.math.geom.Position.class,
                    arc.math.geom.QuadTree.class,
                    arc.math.geom.Rect.class,
                    arc.math.geom.Shape2D.class,
                    arc.math.geom.Spring1D.class,
                    arc.math.geom.Spring2D.class,
                    arc.math.geom.Vec2.class,
                    arc.math.geom.Vec3.class,
                    arc.math.geom.Vector.class,
                    arc.math.Interpolation.class,
                    arc.math.Mathf.class,
                    arc.math.Matrix3.class,
                    arc.math.RandomXS128.class,
                    arc.math.WindowedMean.class,
                    arc.Net.class,
                    arc.scene.Action.class,
                    arc.scene.actions.Actions.class,
                    arc.scene.actions.AddAction.class,
                    arc.scene.actions.AddListenerAction.class,
                    arc.scene.actions.AfterAction.class,
                    arc.scene.actions.AlphaAction.class,
                    arc.scene.actions.ColorAction.class,
                    arc.scene.actions.DelayAction.class,
                    arc.scene.actions.DelegateAction.class,
                    arc.scene.actions.FloatAction.class,
                    arc.scene.actions.IntAction.class,
                    arc.scene.actions.LayoutAction.class,
                    arc.scene.actions.MoveByAction.class,
                    arc.scene.actions.MoveToAction.class,
                    arc.scene.actions.OriginAction.class,
                    arc.scene.actions.ParallelAction.class,
                    arc.scene.actions.RelativeTemporalAction.class,
                    arc.scene.actions.RemoveAction.class,
                    arc.scene.actions.RemoveActorAction.class,
                    arc.scene.actions.RemoveListenerAction.class,
                    arc.scene.actions.RepeatAction.class,
                    arc.scene.actions.RotateByAction.class,
                    arc.scene.actions.RotateToAction.class,
                    arc.scene.actions.RunnableAction.class,
                    arc.scene.actions.ScaleByAction.class,
                    arc.scene.actions.ScaleToAction.class,
                    arc.scene.actions.SequenceAction.class,
                    arc.scene.actions.SizeByAction.class,
                    arc.scene.actions.SizeToAction.class,
                    arc.scene.actions.TemporalAction.class,
                    arc.scene.actions.TimeScaleAction.class,
                    arc.scene.actions.TouchableAction.class,
                    arc.scene.actions.TranslateByAction.class,
                    arc.scene.actions.VisibleAction.class,
                    arc.scene.Element.class,
                    arc.scene.event.ChangeListener.class,
                    arc.scene.event.ClickListener.class,
                    arc.scene.event.DragListener.class,
                    arc.scene.event.DragScrollListener.class,
                    arc.scene.event.ElementGestureListener.class,
                    arc.scene.event.EventListener.class,
                    arc.scene.event.FocusListener.class,
                    arc.scene.event.HandCursorListener.class,
                    arc.scene.event.IbeamCursorListener.class,
                    arc.scene.event.InputEvent.class,
                    arc.scene.event.InputListener.class,
                    arc.scene.event.SceneEvent.class,
                    arc.scene.event.Touchable.class,
                    arc.scene.event.VisibilityEvent.class,
                    arc.scene.event.VisibilityListener.class,
                    arc.scene.Group.class,
                    arc.scene.Scene.class,
                    arc.scene.style.BaseDrawable.class,
                    arc.scene.style.Drawable.class,
                    arc.scene.style.NinePatchDrawable.class,
                    arc.scene.style.ScaledNinePatchDrawable.class,
                    arc.scene.style.Style.class,
                    arc.scene.style.TextureRegionDrawable.class,
                    arc.scene.style.TiledDrawable.class,
                    arc.scene.style.TransformDrawable.class,
                    arc.scene.ui.Button.class,
                    arc.scene.ui.ButtonGroup.class,
                    arc.scene.ui.CheckBox.class,
                    arc.scene.ui.ColorImage.class,
                    arc.scene.ui.Dialog.class,
                    arc.scene.ui.Image.class,
                    arc.scene.ui.ImageButton.class,
                    arc.scene.ui.KeybindDialog.class,
                    arc.scene.ui.Label.class,
                    arc.scene.ui.layout.Cell.class,
                    arc.scene.ui.layout.Collapser.class,
                    arc.scene.ui.layout.HorizontalGroup.class,
                    arc.scene.ui.layout.Scl.class,
                    arc.scene.ui.layout.Stack.class,
                    arc.scene.ui.layout.Table.class,
                    arc.scene.ui.layout.VerticalGroup.class,
                    arc.scene.ui.layout.WidgetGroup.class,
                    arc.scene.ui.ProgressBar.class,
                    arc.scene.ui.ScrollPane.class,
                    arc.scene.ui.SettingsDialog.class,
                    arc.scene.ui.Slider.class,
                    arc.scene.ui.TextArea.class,
                    arc.scene.ui.TextButton.class,
                    arc.scene.ui.TextField.class,
                    arc.scene.ui.Tooltip.class,
                    arc.scene.ui.Touchpad.class,
                    arc.scene.ui.TreeElement.class,
                    arc.scene.utils.ArraySelection.class,
                    arc.scene.utils.Cullable.class,
                    arc.scene.utils.Disableable.class,
                    arc.scene.utils.DragAndDrop.class,
                    arc.scene.utils.Elements.class,
                    arc.scene.utils.Layout.class,
                    arc.scene.utils.Selection.class,
                    arc.Settings.class,
                    arc.struct.Array.class,
                    arc.struct.ArrayMap.class,
                    arc.struct.AtomicQueue.class,
                    arc.struct.BinaryHeap.class,
                    arc.struct.Bits.class,
                    arc.struct.BooleanArray.class,
                    arc.struct.ByteArray.class,
                    arc.struct.CharArray.class,
                    arc.struct.DelayedRemovalArray.class,
                    arc.struct.EnumSet.class,
                    arc.struct.FloatArray.class,
                    arc.struct.GridBits.class,
                    arc.struct.GridMap.class,
                    arc.struct.IdentityMap.class,
                    arc.struct.IntArray.class,
                    arc.struct.IntFloatMap.class,
                    arc.struct.IntIntMap.class,
                    arc.struct.IntMap.class,
                    arc.struct.IntQueue.class,
                    arc.struct.IntSet.class,
                    arc.struct.LongArray.class,
                    arc.struct.LongMap.class,
                    arc.struct.LongQueue.class,
                    arc.struct.ObjectFloatMap.class,
                    arc.struct.ObjectIntMap.class,
                    arc.struct.ObjectMap.class,
                    arc.struct.ObjectSet.class,
                    arc.struct.OrderedMap.class,
                    arc.struct.OrderedSet.class,
                    arc.struct.PooledLinkedList.class,
                    arc.struct.Queue.class,
                    arc.struct.ShortArray.class,
                    arc.struct.SnapshotArray.class,
                    arc.struct.Sort.class,
                    arc.struct.SortedIntList.class,
                    arc.struct.StringMap.class,
                    arc.util.Align.class,
                    arc.util.ArcAnnotate.class,
                    arc.util.ArcNativesLoader.class,
                    arc.util.ArcRuntimeException.class,
                    arc.util.Bench.class,
                    arc.util.BufferUtils.class,
                    arc.util.ColorCodes.class,
                    arc.util.CommandHandler.class,
                    arc.util.Disposable.class,
                    arc.util.Eachable.class,
                    arc.util.I18NBundle.class,
                    arc.util.Interval.class,
                    arc.util.Log.class,
                    arc.util.NetJavaImpl.class,
                    arc.util.OS.class,
                    arc.util.Pack.class,
                    arc.util.PauseableThread.class,
                    arc.util.PerformanceCounter.class,
                    arc.util.PerformanceCounters.class,
                    arc.util.QuickSelect.class,
                    arc.util.Reflect.class,
                    arc.util.Scaling.class,
                    arc.util.ScreenRecorder.class,
                    arc.util.ScreenUtils.class,
                    arc.util.Select.class,
                    arc.util.SharedLibraryLoader.class,
                    arc.util.StaticReset.class,
                    arc.util.Strings.class,
                    arc.util.Structs.class,
                    arc.util.TaskQueue.class,
                    arc.util.Time.class,
                    arc.util.Timekeeper.class,
                    arc.util.Timer.class,
                    arc.util.Tmp.class,
                    arc.util.UnsafeRunnable.class,

                    mindustry.ai.BlockIndexer.class,
                    mindustry.ai.Pathfinder.class,
                    mindustry.ai.WaveSpawner.class,
                    mindustry.ClientLauncher.class,
                    mindustry.content.Blocks.class,
                    mindustry.content.Bullets.class,
                    mindustry.content.Fx.class,
                    mindustry.content.Items.class,
                    mindustry.content.Liquids.class,
                    mindustry.content.Loadouts.class,
                    mindustry.content.Mechs.class,
                    mindustry.content.StatusEffects.class,
                    mindustry.content.TechTree.class,
                    mindustry.content.TypeIDs.class,
                    mindustry.content.UnitTypes.class,
                    mindustry.content.Zones.class,
                    mindustry.core.ContentLoader.class,
                    mindustry.core.Control.class,
                    mindustry.core.FileTree.class,
                    mindustry.core.GameState.class,
                    mindustry.core.Logic.class,
                    mindustry.core.NetClient.class,
                    mindustry.core.NetServer.class,
                    mindustry.core.Platform.class,
                    mindustry.core.Renderer.class,
                    mindustry.core.TypingsGenerator.class,
                    mindustry.core.UI.class,
                    mindustry.core.Version.class,
                    mindustry.core.World.class,
                    mindustry.ctype.Content.class,
                    mindustry.ctype.ContentList.class,
                    mindustry.ctype.ContentType.class,
                    mindustry.ctype.MappableContent.class,
                    mindustry.ctype.UnlockableContent.class,
                    mindustry.editor.DrawOperation.class,
                    mindustry.editor.EditorTile.class,
                    mindustry.editor.EditorTool.class,
                    mindustry.editor.MapEditor.class,
                    mindustry.editor.MapEditorDialog.class,
                    mindustry.editor.MapGenerateDialog.class,
                    mindustry.editor.MapInfoDialog.class,
                    mindustry.editor.MapLoadDialog.class,
                    mindustry.editor.MapRenderer.class,
                    mindustry.editor.MapResizeDialog.class,
                    mindustry.editor.MapSaveDialog.class,
                    mindustry.editor.MapView.class,
                    mindustry.editor.OperationStack.class,
                    mindustry.editor.WaveInfoDialog.class,
                    mindustry.entities.bullet.ArtilleryBulletType.class,
                    mindustry.entities.bullet.BasicBulletType.class,
                    mindustry.entities.bullet.BombBulletType.class,
                    mindustry.entities.bullet.BulletType.class,
                    mindustry.entities.bullet.FlakBulletType.class,
                    mindustry.entities.bullet.HealBulletType.class,
                    mindustry.entities.bullet.LiquidBulletType.class,
                    mindustry.entities.bullet.MassDriverBolt.class,
                    mindustry.entities.bullet.MissileBulletType.class,
                    mindustry.entities.Damage.class,
                    mindustry.entities.effect.Decal.class,
                    mindustry.entities.effect.Fire.class,
                    mindustry.entities.effect.GroundEffectEntity.class,
                    mindustry.entities.effect.ItemTransfer.class,
                    mindustry.entities.effect.Lightning.class,
                    mindustry.entities.effect.Puddle.class,
                    mindustry.entities.effect.RubbleDecal.class,
                    mindustry.entities.effect.ScorchDecal.class,
                    mindustry.entities.Effects.class,
                    mindustry.entities.Entities.class,
                    mindustry.entities.EntityCollisions.class,
                    mindustry.entities.EntityGroup.class,
                    mindustry.entities.Predict.class,
                    mindustry.entities.TargetPriority.class,
                    mindustry.entities.traits.AbsorbTrait.class,
                    mindustry.entities.traits.BelowLiquidTrait.class,
                    mindustry.entities.traits.BuilderMinerTrait.class,
                    mindustry.entities.traits.BuilderTrait.class,
                    mindustry.entities.traits.DamageTrait.class,
                    mindustry.entities.traits.DrawTrait.class,
                    mindustry.entities.traits.Entity.class,
                    mindustry.entities.traits.HealthTrait.class,
                    mindustry.entities.traits.KillerTrait.class,
                    mindustry.entities.traits.MinerTrait.class,
                    mindustry.entities.traits.MoveTrait.class,
                    mindustry.entities.traits.Saveable.class,
                    mindustry.entities.traits.SaveTrait.class,
                    mindustry.entities.traits.ScaleTrait.class,
                    mindustry.entities.traits.ShooterTrait.class,
                    mindustry.entities.traits.SolidTrait.class,
                    mindustry.entities.traits.SpawnerTrait.class,
                    mindustry.entities.traits.SyncTrait.class,
                    mindustry.entities.traits.TargetTrait.class,
                    mindustry.entities.traits.TeamTrait.class,
                    mindustry.entities.traits.TimeTrait.class,
                    mindustry.entities.traits.TypeTrait.class,
                    mindustry.entities.traits.VelocityTrait.class,
                    mindustry.entities.type.base.BaseDrone.class,
                    mindustry.entities.type.base.BuilderDrone.class,
                    mindustry.entities.type.base.FlyingUnit.class,
                    mindustry.entities.type.base.GroundUnit.class,
                    mindustry.entities.type.base.HoverUnit.class,
                    mindustry.entities.type.base.MinerDrone.class,
                    mindustry.entities.type.base.RepairDrone.class,
                    mindustry.entities.type.BaseEntity.class,
                    mindustry.entities.type.BaseUnit.class,
                    mindustry.entities.type.Bullet.class,
                    mindustry.entities.type.DestructibleEntity.class,
                    mindustry.entities.type.EffectEntity.class,
                    mindustry.entities.type.Player.class,
                    mindustry.entities.type.SolidEntity.class,
                    mindustry.entities.type.TileEntity.class,
                    mindustry.entities.type.TimedEntity.class,
                    mindustry.entities.type.Unit.class,
                    mindustry.entities.units.StateMachine.class,
                    mindustry.entities.units.Statuses.class,
                    mindustry.entities.units.UnitCommand.class,
                    mindustry.entities.units.UnitDrops.class,
                    mindustry.entities.units.UnitState.class,
                    mindustry.entities.Units.class,
                    mindustry.game.DefaultWaves.class,
                    mindustry.game.Difficulty.class,
                    mindustry.game.EventType.class,
                    mindustry.game.Gamemode.class,
                    mindustry.game.GlobalData.class,
                    mindustry.game.LoopControl.class,
                    mindustry.game.MusicControl.class,
                    mindustry.game.Objective.class,
                    mindustry.game.Objectives.class,
                    mindustry.game.Rules.class,
                    mindustry.game.Saves.class,
                    mindustry.game.Schematic.class,
                    mindustry.game.Schematics.class,
                    mindustry.game.SoundLoop.class,
                    mindustry.game.SpawnGroup.class,
                    mindustry.game.Stats.class,
                    mindustry.game.Team.class,
                    mindustry.game.Teams.class,
                    mindustry.game.Tutorial.class,
                    mindustry.graphics.BlockRenderer.class,
                    mindustry.graphics.Bloom.class,
                    mindustry.graphics.CacheLayer.class,
                    mindustry.graphics.Drawf.class,
                    mindustry.graphics.FloorRenderer.class,
                    mindustry.graphics.IndexedRenderer.class,
                    mindustry.graphics.Layer.class,
                    mindustry.graphics.LightRenderer.class,
                    mindustry.graphics.MenuRenderer.class,
                    mindustry.graphics.MinimapRenderer.class,
                    mindustry.graphics.MultiPacker.class,
                    mindustry.graphics.OverlayRenderer.class,
                    mindustry.graphics.Pal.class,
                    mindustry.graphics.Pixelator.class,
                    mindustry.graphics.Shaders.class,
                    mindustry.input.Binding.class,
                    mindustry.input.DesktopInput.class,
                    mindustry.input.InputHandler.class,
                    mindustry.input.MobileInput.class,
                    mindustry.input.Placement.class,
                    mindustry.input.PlaceMode.class,
                    mindustry.maps.filters.BlendFilter.class,
                    mindustry.maps.filters.ClearFilter.class,
                    mindustry.maps.filters.DistortFilter.class,
                    mindustry.maps.filters.FilterOption.class,
                    mindustry.maps.filters.GenerateFilter.class,
                    mindustry.maps.filters.MedianFilter.class,
                    mindustry.maps.filters.MirrorFilter.class,
                    mindustry.maps.filters.NoiseFilter.class,
                    mindustry.maps.filters.OreFilter.class,
                    mindustry.maps.filters.OreMedianFilter.class,
                    mindustry.maps.filters.RiverNoiseFilter.class,
                    mindustry.maps.filters.ScatterFilter.class,
                    mindustry.maps.filters.TerrainFilter.class,
                    mindustry.maps.generators.BasicGenerator.class,
                    mindustry.maps.generators.Generator.class,
                    mindustry.maps.generators.MapGenerator.class,
                    mindustry.maps.generators.RandomGenerator.class,
                    mindustry.maps.Map.class,
                    mindustry.maps.MapException.class,
                    mindustry.maps.MapPreviewLoader.class,
                    mindustry.maps.Maps.class,
                    mindustry.maps.zonegen.DesertWastesGenerator.class,
                    mindustry.maps.zonegen.OvergrowthGenerator.class,
                    mindustry.type.Category.class,
                    mindustry.type.ErrorContent.class,
                    mindustry.type.Item.class,
                    mindustry.type.ItemStack.class,
                    mindustry.type.ItemType.class,
                    mindustry.type.Liquid.class,
                    mindustry.type.LiquidStack.class,
                    mindustry.type.Mech.class,
                    mindustry.type.Publishable.class,
                    mindustry.type.StatusEffect.class,
                    mindustry.type.TypeID.class,
                    mindustry.type.UnitType.class,
                    mindustry.type.Weapon.class,
                    mindustry.type.WeatherEvent.class,
                    mindustry.type.Zone.class,
                    mindustry.ui.Bar.class,
                    mindustry.ui.BorderImage.class,
                    mindustry.ui.Cicon.class,
                    mindustry.ui.ContentDisplay.class,
                    mindustry.ui.dialogs.AboutDialog.class,
                    mindustry.ui.dialogs.AdminsDialog.class,
                    mindustry.ui.dialogs.BansDialog.class,
                    mindustry.ui.dialogs.ColorPicker.class,
                    mindustry.ui.dialogs.ContentInfoDialog.class,
                    mindustry.ui.dialogs.ControlsDialog.class,
                    mindustry.ui.dialogs.CustomGameDialog.class,
                    mindustry.ui.dialogs.CustomRulesDialog.class,
                    mindustry.ui.dialogs.DatabaseDialog.class,
                    mindustry.ui.dialogs.DeployDialog.class,
                    mindustry.ui.dialogs.DiscordDialog.class,
                    mindustry.ui.dialogs.FileChooser.class,
                    mindustry.ui.dialogs.FloatingDialog.class,
                    mindustry.ui.dialogs.GameOverDialog.class,
                    mindustry.ui.dialogs.HostDialog.class,
                    mindustry.ui.dialogs.JoinDialog.class,
                    mindustry.ui.dialogs.LanguageDialog.class,
                    mindustry.ui.dialogs.LoadDialog.class,
                    mindustry.ui.dialogs.LoadoutDialog.class,
                    mindustry.ui.dialogs.MapPlayDialog.class,
                    mindustry.ui.dialogs.MapsDialog.class,
                    mindustry.ui.dialogs.MinimapDialog.class,
                    mindustry.ui.dialogs.ModsDialog.class,
                    mindustry.ui.dialogs.PaletteDialog.class,
                    mindustry.ui.dialogs.PausedDialog.class,
                    mindustry.ui.dialogs.SaveDialog.class,
                    mindustry.ui.dialogs.SchematicsDialog.class,
                    mindustry.ui.dialogs.SettingsMenuDialog.class,
                    mindustry.ui.dialogs.TechTreeDialog.class,
                    mindustry.ui.dialogs.TraceDialog.class,
                    mindustry.ui.dialogs.ZoneInfoDialog.class,
                    mindustry.ui.Fonts.class,
                    mindustry.ui.fragments.BlockConfigFragment.class,
                    mindustry.ui.fragments.BlockInventoryFragment.class,
                    mindustry.ui.fragments.ChatFragment.class,
                    mindustry.ui.fragments.FadeInFragment.class,
                    mindustry.ui.fragments.Fragment.class,
                    mindustry.ui.fragments.HudFragment.class,
                    mindustry.ui.fragments.LoadingFragment.class,
                    mindustry.ui.fragments.MenuFragment.class,
                    mindustry.ui.fragments.MinimapFragment.class,
                    mindustry.ui.fragments.OverlayFragment.class,
                    mindustry.ui.fragments.PlacementFragment.class,
                    mindustry.ui.fragments.PlayerListFragment.class,
                    mindustry.ui.fragments.ScriptConsoleFragment.class,
                    mindustry.ui.GridImage.class,
                    mindustry.ui.IconSize.class,
                    mindustry.ui.IntFormat.class,
                    mindustry.ui.ItemDisplay.class,
                    mindustry.ui.ItemImage.class,
                    mindustry.ui.ItemsDisplay.class,
                    mindustry.ui.layout.BranchTreeLayout.class,
                    mindustry.ui.layout.RadialTreeLayout.class,
                    mindustry.ui.layout.TreeLayout.class,
                    mindustry.ui.Links.class,
                    mindustry.ui.LiquidDisplay.class,
                    mindustry.ui.Minimap.class,
                    mindustry.ui.MobileButton.class,
                    mindustry.ui.MultiReqImage.class,
                    mindustry.ui.ReqImage.class,
                    mindustry.ui.Styles.class,
                    mindustry.Vars.class,
                    mindustry.world.Block.class,
                    mindustry.world.blocks.Attributes.class,
                    mindustry.world.blocks.Autotiler.class,
                    mindustry.world.blocks.BlockPart.class,
                    mindustry.world.blocks.BuildBlock.class,
                    mindustry.world.blocks.defense.DeflectorWall.class,
                    mindustry.world.blocks.defense.Door.class,
                    mindustry.world.blocks.defense.ForceProjector.class,
                    mindustry.world.blocks.defense.MendProjector.class,
                    mindustry.world.blocks.defense.OverdriveProjector.class,
                    mindustry.world.blocks.defense.ShockMine.class,
                    mindustry.world.blocks.defense.SurgeWall.class,
                    mindustry.world.blocks.defense.turrets.ArtilleryTurret.class,
                    mindustry.world.blocks.defense.turrets.BurstTurret.class,
                    mindustry.world.blocks.defense.turrets.ChargeTurret.class,
                    mindustry.world.blocks.defense.turrets.CooledTurret.class,
                    mindustry.world.blocks.defense.turrets.DoubleTurret.class,
                    mindustry.world.blocks.defense.turrets.ItemTurret.class,
                    mindustry.world.blocks.defense.turrets.LaserTurret.class,
                    mindustry.world.blocks.defense.turrets.LiquidTurret.class,
                    mindustry.world.blocks.defense.turrets.PowerTurret.class,
                    mindustry.world.blocks.defense.turrets.Turret.class,
                    mindustry.world.blocks.defense.Wall.class,
                    mindustry.world.blocks.distribution.ArmoredConveyor.class,
                    mindustry.world.blocks.distribution.BufferedItemBridge.class,
                    mindustry.world.blocks.distribution.Conveyor.class,
                    mindustry.world.blocks.distribution.ExtendingItemBridge.class,
                    mindustry.world.blocks.distribution.ItemBridge.class,
                    mindustry.world.blocks.distribution.Junction.class,
                    mindustry.world.blocks.distribution.MassDriver.class,
                    mindustry.world.blocks.distribution.OverflowGate.class,
                    mindustry.world.blocks.distribution.Router.class,
                    mindustry.world.blocks.distribution.Sorter.class,
                    mindustry.world.blocks.DoubleOverlayFloor.class,
                    mindustry.world.blocks.Floor.class,
                    mindustry.world.blocks.ItemSelection.class,
                    mindustry.world.blocks.liquid.ArmoredConduit.class,
                    mindustry.world.blocks.liquid.Conduit.class,
                    mindustry.world.blocks.liquid.LiquidBridge.class,
                    mindustry.world.blocks.liquid.LiquidExtendingBridge.class,
                    mindustry.world.blocks.liquid.LiquidJunction.class,
                    mindustry.world.blocks.liquid.LiquidOverflowGate.class,
                    mindustry.world.blocks.liquid.LiquidRouter.class,
                    mindustry.world.blocks.liquid.LiquidTank.class,
                    mindustry.world.blocks.LiquidBlock.class,
                    mindustry.world.blocks.logic.LogicBlock.class,
                    mindustry.world.blocks.logic.MessageBlock.class,
                    mindustry.world.blocks.OreBlock.class,
                    mindustry.world.blocks.OverlayFloor.class,
                    mindustry.world.blocks.power.Battery.class,
                    mindustry.world.blocks.power.BurnerGenerator.class,
                    mindustry.world.blocks.power.ConditionalConsumePower.class,
                    mindustry.world.blocks.power.DecayGenerator.class,
                    mindustry.world.blocks.power.ImpactReactor.class,
                    mindustry.world.blocks.power.ItemLiquidGenerator.class,
                    mindustry.world.blocks.power.LightBlock.class,
                    mindustry.world.blocks.power.NuclearReactor.class,
                    mindustry.world.blocks.power.PowerDiode.class,
                    mindustry.world.blocks.power.PowerDistributor.class,
                    mindustry.world.blocks.power.PowerGenerator.class,
                    mindustry.world.blocks.power.PowerGraph.class,
                    mindustry.world.blocks.power.PowerNode.class,
                    mindustry.world.blocks.power.SingleTypeGenerator.class,
                    mindustry.world.blocks.power.SolarGenerator.class,
                    mindustry.world.blocks.power.ThermalGenerator.class,
                    mindustry.world.blocks.PowerBlock.class,
                    mindustry.world.blocks.production.Cultivator.class,
                    mindustry.world.blocks.production.Drill.class,
                    mindustry.world.blocks.production.Fracker.class,
                    mindustry.world.blocks.production.GenericCrafter.class,
                    mindustry.world.blocks.production.GenericSmelter.class,
                    mindustry.world.blocks.production.Incinerator.class,
                    mindustry.world.blocks.production.LiquidConverter.class,
                    mindustry.world.blocks.production.Pump.class,
                    mindustry.world.blocks.production.Separator.class,
                    mindustry.world.blocks.production.SolidPump.class,
                    mindustry.world.blocks.RespawnBlock.class,
                    mindustry.world.blocks.Rock.class,
                    mindustry.world.blocks.sandbox.ItemSource.class,
                    mindustry.world.blocks.sandbox.ItemVoid.class,
                    mindustry.world.blocks.sandbox.LiquidSource.class,
                    mindustry.world.blocks.sandbox.PowerSource.class,
                    mindustry.world.blocks.sandbox.PowerVoid.class,
                    mindustry.world.blocks.StaticWall.class,
                    mindustry.world.blocks.storage.CoreBlock.class,
                    mindustry.world.blocks.storage.LaunchPad.class,
                    mindustry.world.blocks.storage.StorageBlock.class,
                    mindustry.world.blocks.storage.Unloader.class,
                    mindustry.world.blocks.storage.Vault.class,
                    mindustry.world.blocks.TreeBlock.class,
                    mindustry.world.blocks.units.CommandCenter.class,
                    mindustry.world.blocks.units.MechPad.class,
                    mindustry.world.blocks.units.RallyPoint.class,
                    mindustry.world.blocks.units.RepairPoint.class,
                    mindustry.world.blocks.units.UnitFactory.class,
                    mindustry.world.BlockStorage.class,
                    mindustry.world.Build.class,
                    mindustry.world.CachedTile.class,
                    mindustry.world.consumers.Consume.class,
                    mindustry.world.consumers.ConsumeItemFilter.class,
                    mindustry.world.consumers.ConsumeItems.class,
                    mindustry.world.consumers.ConsumeLiquid.class,
                    mindustry.world.consumers.ConsumeLiquidBase.class,
                    mindustry.world.consumers.ConsumeLiquidFilter.class,
                    mindustry.world.consumers.ConsumePower.class,
                    mindustry.world.consumers.Consumers.class,
                    mindustry.world.consumers.ConsumeType.class,
                    mindustry.world.DirectionalItemBuffer.class,
                    mindustry.world.Edges.class,
                    mindustry.world.ItemBuffer.class,
                    mindustry.world.LegacyColorMapper.class,
                    mindustry.world.meta.Attribute.class,
                    mindustry.world.meta.BlockBars.class,
                    mindustry.world.meta.BlockFlag.class,
                    mindustry.world.meta.BlockGroup.class,
                    mindustry.world.meta.BlockStat.class,
                    mindustry.world.meta.BlockStats.class,
                    mindustry.world.meta.BuildVisibility.class,
                    mindustry.world.meta.PowerType.class,
                    mindustry.world.meta.Producers.class,
                    mindustry.world.meta.StatCategory.class,
                    mindustry.world.meta.StatUnit.class,
                    mindustry.world.meta.StatValue.class,
                    mindustry.world.meta.values.AmmoListValue.class,
                    mindustry.world.meta.values.BooleanValue.class,
                    mindustry.world.meta.values.BoosterListValue.class,
                    mindustry.world.meta.values.ItemFilterValue.class,
                    mindustry.world.meta.values.ItemListValue.class,
                    mindustry.world.meta.values.LiquidFilterValue.class,
                    mindustry.world.meta.values.LiquidValue.class,
                    mindustry.world.meta.values.NumberValue.class,
                    mindustry.world.meta.values.StringValue.class,
                    mindustry.world.modules.BlockModule.class,
                    mindustry.world.modules.ConsumeModule.class,
                    mindustry.world.modules.ItemModule.class,
                    mindustry.world.modules.LiquidModule.class,
                    mindustry.world.modules.PowerModule.class,
                    mindustry.world.Pos.class,
                    mindustry.world.producers.Produce.class,
                    mindustry.world.producers.ProduceItem.class,
                    mindustry.world.StaticTree.class,
                    mindustry.world.Tile.class,
                    mindustry.world.WorldContext.class,

                    mindustry.gen.BufferItem.class,
                    mindustry.gen.Call.class,
                    mindustry.gen.Icon.class,
                    mindustry.gen.MethodHash.class,
                    mindustry.gen.Musics.class,
                    mindustry.gen.PathTile.class,
                    mindustry.gen.PropCell.class,
                    mindustry.gen.RemoteReadClient.class,
                    mindustry.gen.RemoteReadServer.class,
                    mindustry.gen.Serialization.class,
                    mindustry.gen.Sounds.class,
                    mindustry.gen.Tex.class,
                    mindustry.gen.TileOp.class,

                    JavaAdapter.class
            };
            //File file = new File("C:\\Users\\ngkai\\Documents\\GitHub\\MinMod.ts\\testing\\mindustry.d.ts");
            //FileOutputStream stream = new FileOutputStream(file);
            TypeConverter tc = new TypeConverter();
            for(Class cl: classes) tc.resolveClass(cl);
            tc.finish();
            //stream.write(tc.toString().getBytes());
            //stream.close();
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
            for(TeamData team : state.teams.getActive()){
                if(team.hasCore()){
                    TileEntity entity = team.core();
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
        if(!state.rules.attackMode && state.teams.playerCores().size == 0 && !state.gameOver){
            state.gameOver = true;
            Events.fire(new GameOverEvent(state.rules.waveTeam));
        }else if(state.rules.attackMode){
            Team alive = null;

            for(TeamData team : state.teams.getActive()){
                if(team.hasCore()){
                    if(alive != null){
                        return;
                    }
                    alive = team.team;
                }
            }

            if(alive != null && !state.gameOver){
                if(world.isZone() && alive == state.rules.defaultTeam){
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

        for(TileEntity tile : state.teams.playerCores()){
            Effects.effect(Fx.launch, tile);
        }

        if(world.getZone() != null){
            world.getZone().setLaunched();
        }

        Time.runTask(30f, () -> {
            for(TileEntity entity : state.teams.playerCores()){
                for(Item item : content.items()){
                    data.addItem(item, entity.items.get(item));
                    Events.fire(new LaunchItemEvent(item, entity.items.get(item)));
                }
                entity.tile.remove();
            }
            state.launched = true;
            state.gameOver = true;
            Events.fire(new LaunchEvent());
            //manually fire game over event now
            Events.fire(new GameOverEvent(state.rules.defaultTeam));
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
        Events.fire(Trigger.update);

        if(!state.is(State.menu)){
            if(!net.client()){
                state.enemies = unitGroup.count(b -> b.getTeam() == state.rules.waveTeam && b.countsAsEnemy());
            }

            if(!state.isPaused()){
                Time.update();

                if(state.rules.waves && state.rules.waveTimer && !state.gameOver){
                    if(!state.rules.waitForWaveToEnd || state.enemies == 0){
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
                    unitGroup.update();
                    puddleGroup.update();
                    shieldGroup.update();
                    bulletGroup.update();
                    tileGroup.update();
                    fireGroup.update();
                }else{
                    unitGroup.updateEvents();
                    collisions.updatePhysics(unitGroup);
                }


                playerGroup.update();

                //effect group only contains item transfers in the headless version, update it!
                if(headless){
                    effectGroup.update();
                }

                if(!state.isEditor()){
                    //bulletGroup
                    collisions.collideGroups(bulletGroup, unitGroup);
                    collisions.collideGroups(bulletGroup, playerGroup);
                }
            }

            if(!net.client() && !world.isInvalidMap() && !state.isEditor() && state.rules.canGameOver){
                checkGameOver();
            }
        }
    }
}
