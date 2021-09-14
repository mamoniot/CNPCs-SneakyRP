package noppes.npcs.entity.old;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityNpcMonsterMale extends EntityNPCInterface {
     public EntityNpcMonsterMale(World world) {
          super(world);
          this.display.setSkinTexture("customnpcs:textures/entity/monstermale/ZombieSteve.png");
     }

     public void func_70071_h_() {
          this.field_70128_L = true;
          this.func_94061_f(true);
          if (!this.world.isRemote) {
               NBTTagCompound compound = new NBTTagCompound();
               this.writeToNBT(compound);
               EntityCustomNpc npc = new EntityCustomNpc(this.world);
               npc.readFromNBT(compound);
               npc.ais.animationType = 3;
               this.world.spawnEntity(npc);
          }

          super.func_70071_h_();
     }
}
