package noppes.npcs.client.gui.model;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import noppes.npcs.ModelData;
import noppes.npcs.client.controllers.Preset;
import noppes.npcs.client.controllers.PresetController;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.SubGuiInterface;

public class GuiPresetSave extends SubGuiInterface {
     private ModelData data;
     private GuiScreen parent;

     public GuiPresetSave(GuiScreen parent, ModelData data) {
          this.data = data;
          this.parent = parent;
          this.xSize = 200;
          this.drawDefaultBackground = true;
     }

     public void func_73866_w_() {
          super.func_73866_w_();
          this.addTextField(new GuiNpcTextField(0, this, this.guiLeft, this.guiTop + 70, 200, 20, ""));
          this.addButton(new GuiNpcButton(0, this.guiLeft, this.guiTop + 100, 98, 20, "Save"));
          this.addButton(new GuiNpcButton(1, this.guiLeft + 100, this.guiTop + 100, 98, 20, "Cancel"));
     }

     protected void func_146284_a(GuiButton btn) {
          super.func_146284_a(btn);
          GuiNpcButton button = (GuiNpcButton)btn;
          if (button.field_146127_k == 0) {
               String name = this.getTextField(0).func_146179_b().trim();
               if (name.isEmpty()) {
                    return;
               }

               Preset preset = new Preset();
               preset.name = name;
               preset.data = this.data.copy();
               PresetController.instance.addPreset(preset);
          }

          this.close();
     }
}
