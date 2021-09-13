package noppes.npcs.constants;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public enum EnumCompanionTalent {
     INVENTORY(Item.getItemFromBlock(Blocks.CRAFTING_TABLE)),
     ARMOR(Items.field_151030_Z),
     SWORD(Items.field_151048_u),
     RANGED(Items.field_151031_f),
     ACROBATS(Items.field_151021_T),
     INTEL(Items.field_151122_aG);

     public ItemStack item;

     private EnumCompanionTalent(Item item) {
          this.item = new ItemStack(item);
     }
}
