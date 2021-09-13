package noppes.npcs.client.gui.custom.components;

import net.minecraft.client.Minecraft;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import noppes.npcs.client.gui.custom.GuiCustom;

public class CustomGuiSlot extends Slot {
     boolean clientSide;

     public CustomGuiSlot(IInventory inventoryIn, int index, int xPosition, int yPosition, boolean clientSide) {
          super(inventoryIn, index, xPosition, yPosition);
          this.clientSide = clientSide;
     }

     public void func_75218_e() {
          if (this.clientSide) {
               ((GuiCustom)Minecraft.func_71410_x().field_71462_r).slotChange(this);
          }

          super.func_75218_e();
     }
}
