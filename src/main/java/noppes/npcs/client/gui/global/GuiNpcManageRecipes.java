package noppes.npcs.client.gui.global;

import java.util.HashMap;
import java.util.Vector;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface2;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcButtonYesNo;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.containers.ContainerManageRecipes;
import noppes.npcs.controllers.data.RecipeCarpentry;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNpcManageRecipes extends GuiContainerNPCInterface2 implements IScrollData, IGuiData, ICustomScrollListener, ITextfieldListener {
     private GuiCustomScroll scroll;
     private HashMap data = new HashMap();
     private ContainerManageRecipes container;
     private String selected = null;
     private ResourceLocation slot;

     public GuiNpcManageRecipes(EntityNPCInterface npc, ContainerManageRecipes container) {
          super(npc, container);
          this.container = container;
          this.drawDefaultBackground = false;
          Client.sendData(EnumPacketServer.RecipesGet, container.width, 100);
          this.setBackground("inventorymenu.png");
          this.slot = this.getResource("slot.png");
          this.field_147000_g = 200;
     }

     public void func_73866_w_() {
          super.func_73866_w_();
          if (this.scroll == null) {
               this.scroll = new GuiCustomScroll(this, 0);
          }

          this.scroll.setSize(130, 180);
          this.scroll.guiLeft = this.field_147003_i + 172;
          this.scroll.guiTop = this.field_147009_r + 8;
          this.addScroll(this.scroll);
          this.addButton(new GuiNpcButton(0, this.field_147003_i + 306, this.field_147009_r + 10, 84, 20, "menu.global"));
          this.addButton(new GuiNpcButton(1, this.field_147003_i + 306, this.field_147009_r + 32, 84, 20, "tile.npccarpentybench.name"));
          this.getButton(0).setEnabled(this.container.width == 4);
          this.getButton(1).setEnabled(this.container.width == 3);
          this.addButton(new GuiNpcButton(3, this.field_147003_i + 306, this.field_147009_r + 60, 84, 20, "gui.add"));
          this.addButton(new GuiNpcButton(4, this.field_147003_i + 306, this.field_147009_r + 82, 84, 20, "gui.remove"));
          this.addLabel(new GuiNpcLabel(0, "gui.ignoreDamage", this.field_147003_i + 86, this.field_147009_r + 32));
          this.addButton(new GuiNpcButtonYesNo(5, this.field_147003_i + 114, this.field_147009_r + 40, 50, 20, this.container.recipe.ignoreDamage));
          this.addLabel(new GuiNpcLabel(1, "gui.ignoreNBT", this.field_147003_i + 86, this.field_147009_r + 82));
          this.addButton(new GuiNpcButtonYesNo(6, this.field_147003_i + 114, this.field_147009_r + 90, 50, 20, this.container.recipe.ignoreNBT));
          this.addTextField(new GuiNpcTextField(0, this, this.field_146289_q, this.field_147003_i + 8, this.field_147009_r + 8, 160, 20, this.container.recipe.name));
          this.getTextField(0).enabled = false;
          this.getButton(5).setEnabled(false);
          this.getButton(6).setEnabled(false);
     }

     protected void func_146284_a(GuiButton guibutton) {
          GuiNpcButton button = (GuiNpcButton)guibutton;
          if (button.field_146127_k == 0) {
               this.save();
               NoppesUtil.requestOpenGUI(EnumGuiType.ManageRecipes, 3, 0, 0);
          }

          if (button.field_146127_k == 1) {
               this.save();
               NoppesUtil.requestOpenGUI(EnumGuiType.ManageRecipes, 4, 0, 0);
          }

          if (button.field_146127_k == 3) {
               this.save();
               this.scroll.clear();

               String name;
               for(name = I18n.func_74838_a("gui.new"); this.data.containsKey(name); name = name + "_") {
               }

               RecipeCarpentry recipe = new RecipeCarpentry(name);
               recipe.isGlobal = this.container.width == 3;
               Client.sendData(EnumPacketServer.RecipeSave, recipe.writeNBT());
          }

          if (button.field_146127_k == 4 && this.data.containsKey(this.scroll.getSelected())) {
               Client.sendData(EnumPacketServer.RecipeRemove, this.data.get(this.scroll.getSelected()));
               this.scroll.clear();
          }

          if (button.field_146127_k == 5) {
               this.container.recipe.ignoreDamage = button.getValue() == 1;
          }

          if (button.field_146127_k == 6) {
               this.container.recipe.ignoreNBT = button.getValue() == 1;
          }

     }

     public void setGuiData(NBTTagCompound compound) {
          RecipeCarpentry recipe = RecipeCarpentry.read(compound);
          this.getTextField(0).func_146180_a(recipe.name);
          this.container.setRecipe(recipe);
          this.getTextField(0).enabled = true;
          this.getButton(5).setEnabled(true);
          this.getButton(5).setDisplay(recipe.ignoreDamage ? 1 : 0);
          this.getButton(6).setEnabled(true);
          this.getButton(6).setDisplay(recipe.ignoreNBT ? 1 : 0);
          this.setSelected(recipe.name);
     }

     protected void func_146976_a(float f, int x, int y) {
          super.func_146976_a(f, x, y);
          GlStateManager.func_179131_c(1.0F, 1.0F, 1.0F, 1.0F);
          this.field_146297_k.field_71446_o.func_110577_a(this.slot);

          for(int i = 0; i < this.container.width; ++i) {
               for(int j = 0; j < this.container.width; ++j) {
                    this.func_73729_b(this.field_147003_i + i * 18 + 7, this.field_147009_r + j * 18 + 34, 0, 0, 18, 18);
               }
          }

          this.func_73729_b(this.field_147003_i + 86, this.field_147009_r + 60, 0, 0, 18, 18);
     }

     public void setData(Vector list, HashMap data) {
          String name = this.scroll.getSelected();
          this.data = data;
          this.scroll.setList(list);
          this.getTextField(0).enabled = name != null;
          this.getButton(5).setEnabled(name != null);
          if (name != null) {
               this.scroll.setSelected(name);
          }

     }

     public void setSelected(String selected) {
          this.selected = selected;
          this.scroll.setSelected(selected);
     }

     public void scrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
          this.save();
          this.selected = this.scroll.getSelected();
          Client.sendData(EnumPacketServer.RecipeGet, this.data.get(this.selected));
     }

     public void save() {
          GuiNpcTextField.unfocus();
          if (this.selected != null && this.data.containsKey(this.selected)) {
               this.container.saveRecipe();
               Client.sendData(EnumPacketServer.RecipeSave, this.container.recipe.writeNBT());
          }

     }

     public void unFocused(GuiNpcTextField guiNpcTextField) {
          String name = guiNpcTextField.func_146179_b();
          if (!name.isEmpty() && !this.data.containsKey(name)) {
               String old = this.container.recipe.name;
               this.data.remove(this.container.recipe.name);
               this.container.recipe.name = name;
               this.data.put(this.container.recipe.name, this.container.recipe.id);
               this.selected = name;
               this.scroll.replace(old, this.container.recipe.name);
          }

     }

     public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
     }
}
