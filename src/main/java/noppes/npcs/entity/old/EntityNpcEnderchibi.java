package noppes.npcs.entity.old;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import noppes.npcs.ModelData;
import noppes.npcs.ModelPartData;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityNpcEnderchibi extends EntityNPCInterface {
     public EntityNpcEnderchibi(World world) {
          super(world);
          this.display.setSkinTexture("customnpcs:textures/entity/enderchibi/MrEnderchibi.png");
     }

     public void func_70071_h_() {
          this.field_70128_L = true;
          this.func_94061_f(true);
          if (!this.world.isRemote) {
               NBTTagCompound compound = new NBTTagCompound();
               this.func_189511_e(compound);
               EntityCustomNpc npc = new EntityCustomNpc(this.world);
               npc.func_70020_e(compound);
               ModelData data = npc.modelData;
               data.getPartConfig(EnumParts.LEG_LEFT).setScale(0.65F, 0.75F);
               data.getPartConfig(EnumParts.ARM_LEFT).setScale(0.5F, 1.45F);
               ModelPartData part = data.getOrCreatePart(EnumParts.PARTICLES);
               part.type = 1;
               part.color = 16711680;
               part.playerTexture = true;
               this.world.spawnEntity(npc);
          }

          super.func_70071_h_();
     }
}
