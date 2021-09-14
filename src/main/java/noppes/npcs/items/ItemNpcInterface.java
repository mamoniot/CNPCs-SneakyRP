package noppes.npcs.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import noppes.npcs.CustomItems;

public class ItemNpcInterface extends Item {
     private boolean damageAble;

     public ItemNpcInterface(int par1) {
          this();
     }

     public ItemNpcInterface() {
          this.damageAble = true;
          this.setCreativeTab(CustomItems.tab);
     }

     public void setUnDamageable() {
          this.damageAble = false;
     }

     public void playSound(EntityLivingBase entity, SoundEvent sound, float volume, float pitch) {
          entity.world.playSound((EntityPlayer)null, entity.field_70165_t, entity.field_70163_u, entity.field_70161_v, sound, SoundCategory.NEUTRAL, volume, pitch);
     }

     public void func_150895_a(CreativeTabs tab, NonNullList subItems) {
          subItems.add(new ItemStack(this, 1, 0));
     }

     public int func_77619_b() {
          return super.func_77619_b();
     }

     public Item setUnlocalizedName(String name) {
          super.setUnlocalizedName(name);
          this.setRegistryName(new ResourceLocation("customnpcs", name));
          return this;
     }

     public boolean func_77644_a(ItemStack par1ItemStack, EntityLivingBase par2EntityLiving, EntityLivingBase par3EntityLiving) {
          if (par2EntityLiving.getHealth() <= 0.0F) {
               return false;
          } else {
               if (this.damageAble) {
                    par1ItemStack.damageItem(1, par3EntityLiving);
               }

               return true;
          }
     }

     public boolean hasItem(EntityPlayer player, Item item) {
          return this.getItemStack(player, item) != null;
     }

     public boolean consumeItem(EntityPlayer player, Item item) {
          ItemStack itemstack = this.getItemStack(player, item);
          if (itemstack == null) {
               return false;
          } else {
               itemstack.shrink(1);
               if (itemstack.getCount() == 0) {
                    player.inventory.func_184437_d(itemstack);
               }

               return true;
          }
     }

     private ItemStack getItemStack(EntityPlayer player, Item item) {
          if (player.func_184586_b(EnumHand.OFF_HAND) != null && player.func_184586_b(EnumHand.OFF_HAND).getItem() == item) {
               return player.func_184586_b(EnumHand.OFF_HAND);
          } else if (player.func_184586_b(EnumHand.MAIN_HAND) != null && player.func_184586_b(EnumHand.MAIN_HAND).getItem() == item) {
               return player.func_184586_b(EnumHand.MAIN_HAND);
          } else {
               for(int i = 0; i < player.inventory.getSizeInventory(); ++i) {
                    ItemStack itemstack = player.inventory.getStackInSlot(i);
                    if (itemstack != null && itemstack.getItem() == item) {
                         return itemstack;
                    }
               }

               return null;
          }
     }
}
