package mindustry.entities.type.base;

<<<<<<< HEAD:core/src/io/anuke/mindustry/entities/type/base/RepairDrone.java
import io.anuke.mindustry.entities.Units;
import io.anuke.mindustry.entities.type.TileEntity;
import io.anuke.mindustry.entities.units.UnitState;
import io.anuke.mindustry.world.Pos;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.blocks.*;
import io.anuke.mindustry.world.meta.Attribute;
=======
import mindustry.entities.Units;
import mindustry.entities.type.TileEntity;
import mindustry.entities.units.UnitState;
import mindustry.world.Pos;
import mindustry.world.Tile;
import mindustry.world.blocks.*;
>>>>>>> master:core/src/mindustry/entities/type/base/RepairDrone.java

import java.io.*;

import static mindustry.Vars.world;

public class RepairDrone extends BaseDrone{
    public final UnitState repair = new UnitState(){

        public void entered(){
            target = null;
        }

        public void update(){

            if(retarget()){
                target = Units.findDamagedTile(team, x, y);
            }

            if(target instanceof TileEntity && ((TileEntity)target).block instanceof BuildBlock){
                target = null;
            }

            if (target instanceof TileEntity && ((TileEntity) target).block.sumAttribute(Attribute.heat, ((TileEntity) target).tileX(), ((TileEntity) target).tileY()) > 0f){
                target = null;
            }

            if(target != null){
                if(target.dst(RepairDrone.this) > type.range){
                    circle(type.range * 0.9f);
                }else{
                    getWeapon().update(RepairDrone.this, target.getX(), target.getY());
                }
            }else{
                //circle spawner if there's nothing to repair
                if(getSpawner() != null){
                    target = getSpawner();
                    circle(type.range * 1.5f, type.speed/2f);
                    target = null;
                }
            }
        }
    };

    @Override
    public boolean shouldRotate(){
        return target != null;
    }

    @Override
    public UnitState getStartState(){
        return repair;
    }

    @Override
    public void write(DataOutput data) throws IOException{
        super.write(data);
        data.writeInt(state.is(repair) && target instanceof TileEntity ? ((TileEntity)target).tile.pos() : Pos.invalid);
    }

    @Override
    public void read(DataInput data) throws IOException{
        super.read(data);
        Tile repairing = world.tile(data.readInt());

        if(repairing != null){
            target = repairing.entity;
        }
    }
}
