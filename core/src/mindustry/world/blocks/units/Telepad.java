package mindustry.world.blocks.units;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.type.*;
import mindustry.world.*;
import mindustry.world.blocks.power.*;

public class Telepad extends Block{
    public Telepad(String name){
        super(name);
        solid = true;
        breakable = true;
        update = true;
        entityType = TelepadEntity::new;
    }

    @Override
    public void unitOver(Tile tile, Unit unit){
        TelepadEntity entity = tile.ent();
        entity.warmup = Mathf.clamp(entity.warmup += 0.015f * entity.efficiency(), 0, 1);
        unit.scale = 0f - (entity.warmup / 4);

        Tmp.v1.set(unit).sub(tile.drawx(), tile.drawy()).setLength(0.2f * entity.efficiency() * unit.dst(tile)).scl(0.45f * Time.delta());
        unit.applyImpulse(-Tmp.v1.x, -Tmp.v1.y);
    }

    @Override
    public void draw(Tile tile){
        super.draw(tile);

        ImpactReactor impact = ((ImpactReactor)Blocks.impactReactor);
        int plasmas = impact.plasmas;
        TelepadEntity entity = tile.ent();

        for(int i = 0; i < plasmas; i++){
            float r = 29f + Mathf.absin(Time.time(), 2f + i * 1f, 5f - i * 0.5f);

            r -= 30f * entity.warmup;

            Draw.color(Color.white, Mechs.trident.engineColor, (float)i / plasmas);
            Draw.alpha((0.3f + Mathf.absin(Time.time(), 2f + i * 2f, 0.3f + i * 0.05f)) * entity.warmup);
            Draw.blend(Blending.additive);
            Draw.rect(impact.reg(impact.plasmaRegions[i]), tile.drawx(), tile.drawy(), r, r, Time.time() * (12f + i * 2f) * entity.warmup);
            Draw.blend();
        }

        Draw.color();
    }

    @Override
    public void update(Tile tile){
        TelepadEntity entity = tile.ent();
        entity.warmup = Mathf.clamp(entity.warmup -= 0.01f, 0, 1);
    }

    class TelepadEntity extends TileEntity{
        public float warmup = 0;
    }
}
