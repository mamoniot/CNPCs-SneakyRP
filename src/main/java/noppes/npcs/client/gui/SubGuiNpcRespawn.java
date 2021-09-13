package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.GuiButtonBiDirectional;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.entity.data.DataStats;

public class SubGuiNpcRespawn extends SubGuiInterface implements ITextfieldListener {
     private DataStats stats;

     public SubGuiNpcRespawn(DataStats stats) {
          this.stats = stats;
          this.setBackground("menubg.png");
          this.xSize = 256;
          this.ySize = 216;
          this.closeOnEsc = true;
     }

     public void func_73866_w_() {
          super.func_73866_w_();
          this.addLabel(new GuiNpcLabel(0, "stats.respawn", this.guiLeft + 5, this.guiTop + 35));
          this.addButton(new GuiButtonBiDirectional(0, this.guiLeft + 122, this.guiTop + 30, 80, 20, new String[]{"gui.yes", "gui.day", "gui.night", "gui.no", "stats.naturally"}, this.stats.spawnCycle));
          if (this.stats.respawnTime > 0) {
               this.addLabel(new GuiNpcLabel(3, "gui.time", this.guiLeft + 5, this.guiTop + 57));
               this.addTextField(new GuiNpcTextField(2, this, this.field_146289_q, this.guiLeft + 122, this.guiTop + 53, 50, 18, this.stats.respawnTime + ""));
               this.getTextField(2).numbersOnly = true;
               this.getTextField(2).setMinMaxDefault(1, Integer.MAX_VALUE, 20);
               this.addLabel(new GuiNpcLabel(4, "stats.deadbody", this.guiLeft + 4, this.guiTop + 79));
               this.addButton(new GuiNpcButton(4, this.guiLeft + 122, this.guiTop + 74, 60, 20, new String[]{"gui.no", "gui.yes"}, this.stats.hideKilledBody ? 1 : 0));
          }

          this.addButton(new GuiNpcButton(66, this.guiLeft + 82, this.guiTop + 190, 98, 20, "gui.done"));
     }

     protected void func_146284_a(GuiButton guibutton) {
          int id = guibutton.field_146127_k;
          GuiNpcButton button = (GuiNpcButton)guibutton;
          if (button.field_146127_k == 0) {
               this.stats.spawnCycle = button.getValue();
               if (this.stats.spawnCycle != 3 && this.stats.spawnCycle != 4) {
                    this.stats.respawnTime = 20;
               } else {
                    this.stats.respawnTime = 0;
               }

               this.func_73866_w_();
          } else if (button.field_146127_k == 4) {
               this.stats.hideKilledBody = button.getValue() == 1;
          }

          if (id == 66) {
               this.close();
          }

     }

     public void unFocused(GuiNpcTextField textfield) {
          if (textfield.field_175208_g == 2) {
               this.stats.respawnTime = textfield.getInteger();
          }

     }
}
