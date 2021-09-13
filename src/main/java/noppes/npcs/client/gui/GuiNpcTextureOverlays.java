package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiScreen;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNpcTextureOverlays extends GuiNpcSelectionInterface {
     public GuiNpcTextureOverlays(EntityNPCInterface npc, GuiScreen parent) {
          super(npc, parent, npc.display.getOverlayTexture().isEmpty() ? "customnpcs:textures/overlays/" : npc.display.getOverlayTexture());
          this.title = "Select Overlay";
          this.parent = parent;
     }

     public void func_73866_w_() {
          super.func_73866_w_();
          int index = this.npc.display.getOverlayTexture().lastIndexOf("/");
          if (index > 0) {
               String asset = this.npc.display.getOverlayTexture().substring(index + 1);
               if (this.npc.display.getOverlayTexture().equals(this.assets.getAsset(asset))) {
                    this.slot.selected = asset;
               }
          }

     }

     public void func_73863_a(int i, int j, float f) {
          int l = -50;
          int i1 = this.field_146295_m / 2 + 30;
          this.drawNpc(this.npc, l, i1, 2.0F, 0);
          super.func_73863_a(i, j, f);
     }

     public void elementClicked() {
          if (this.dataTextures.contains(this.slot.selected) && this.slot.selected != null) {
               this.npc.display.setOverlayTexture(this.assets.getAsset(this.slot.selected));
          }

     }

     public void save() {
     }

     public String[] getExtension() {
          return new String[]{"png"};
     }
}
