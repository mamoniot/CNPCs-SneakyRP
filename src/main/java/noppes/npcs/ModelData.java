package noppes.npcs;

import java.lang.reflect.Method;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import noppes.npcs.controllers.PixelmonHelper;
import noppes.npcs.entity.EntityNPCInterface;

public class ModelData extends ModelDataShared {
     public EntityLivingBase getEntity(EntityNPCInterface npc) {
          if (this.entityClass == null) {
               return null;
          } else {
               if (this.entity == null) {
                    try {
                         this.entity = (EntityLivingBase)this.entityClass.getConstructor(World.class).newInstance(npc.field_70170_p);
                         if (PixelmonHelper.isPixelmon(this.entity) && npc.field_70170_p.field_72995_K && !this.extra.func_74764_b("Name")) {
                              this.extra.func_74778_a("Name", "Abra");
                         }

                         try {
                              this.entity.func_70037_a(this.extra);
                         } catch (Exception var6) {
                         }

                         this.entity.func_184224_h(true);
                         this.entity.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)npc.func_110138_aP());
                         EntityEquipmentSlot[] var2 = EntityEquipmentSlot.values();
                         int var3 = var2.length;

                         for(int var4 = 0; var4 < var3; ++var4) {
                              EntityEquipmentSlot slot = var2[var4];
                              this.entity.func_184201_a(slot, npc.func_184582_a(slot));
                         }
                    } catch (Exception var7) {
                    }
               }

               return this.entity;
          }
     }

     public ModelData copy() {
          ModelData data = new ModelData();
          data.readFromNBT(this.writeToNBT());
          return data;
     }

     public void setExtra(EntityLivingBase entity, String key, String value) {
          key = key.toLowerCase();
          if (key.equals("breed") && EntityList.func_75621_b(entity).equals("tgvstyle.Dog")) {
               try {
                    Method method = entity.getClass().getMethod("getBreedID");
                    Enum breed = (Enum)method.invoke(entity);
                    method = entity.getClass().getMethod("setBreedID", breed.getClass());
                    method.invoke(entity, ((Enum[])breed.getClass().getEnumConstants())[Integer.parseInt(value)]);
                    NBTTagCompound comp = new NBTTagCompound();
                    entity.func_70014_b(comp);
                    this.extra.func_74778_a("EntityData21", comp.func_74779_i("EntityData21"));
               } catch (Exception var7) {
                    var7.printStackTrace();
               }
          }

          if (key.equalsIgnoreCase("name") && PixelmonHelper.isPixelmon(entity)) {
               this.extra.func_74778_a("Name", value);
          }

          this.clearEntity();
     }
}
