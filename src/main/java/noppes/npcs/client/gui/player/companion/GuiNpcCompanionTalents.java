package noppes.npcs.client.gui.player.companion;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.constants.EnumCompanionTalent;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleCompanion;
import org.lwjgl.input.Mouse;

public class GuiNpcCompanionTalents extends GuiNPCInterface {
     private RoleCompanion role;
     private Map talents = new HashMap();
     private GuiNpcButton selected;
     private long lastPressedTime = 0L;
     private long startPressedTime = 0L;

     public GuiNpcCompanionTalents(EntityNPCInterface npc) {
          super(npc);
          this.role = (RoleCompanion)npc.roleInterface;
          this.closeOnEsc = true;
          this.setBackground("companion_empty.png");
          this.xSize = 171;
          this.ySize = 166;
     }

     public void func_73866_w_() {
          super.func_73866_w_();
          this.talents = new HashMap();
          int y = this.guiTop + 12;
          this.addLabel(new GuiNpcLabel(0, NoppesStringUtils.translate("quest.exp", ": "), this.guiLeft + 4, this.guiTop + 10));
          GuiNpcCompanionStats.addTopMenu(this.role, this, 2);
          int i = 0;
          Iterator var3 = this.role.talents.keySet().iterator();

          while(var3.hasNext()) {
               EnumCompanionTalent e = (EnumCompanionTalent)var3.next();
               this.addTalent(i++, e);
          }

     }

     private void addTalent(int i, EnumCompanionTalent talent) {
          int y = this.guiTop + 28 + i / 2 * 26;
          int x = this.guiLeft + 4 + i % 2 * 84;
          GuiNpcCompanionTalents.GuiTalent gui = new GuiNpcCompanionTalents.GuiTalent(this.role, talent, x, y);
          gui.func_146280_a(this.field_146297_k, this.field_146294_l, this.field_146295_m);
          this.talents.put(i, gui);
          if (this.role.getTalentLevel(talent) < 5) {
               this.addButton(new GuiNpcButton(i + 10, x + 26, y, 14, 14, "+"));
               y += 8;
          }

          this.addLabel(new GuiNpcLabel(i, this.role.talents.get(talent) + "/" + this.role.getNextLevel(talent), x + 26, y + 8));
     }

     public void func_146284_a(GuiButton guibutton) {
          super.func_146284_a(guibutton);
          int id = guibutton.field_146127_k;
          if (id == 1) {
               CustomNpcs.proxy.openGui(this.npc, EnumGuiType.Companion);
          }

          if (id == 3) {
               NoppesUtilPlayer.sendData(EnumPlayerPacket.CompanionOpenInv);
          }

          if (id >= 10) {
               this.selected = (GuiNpcButton)guibutton;
               this.lastPressedTime = this.startPressedTime = this.field_146297_k.field_71441_e.func_72820_D();
               this.addExperience(1);
          }

     }

     private void addExperience(int exp) {
          EnumCompanionTalent talent = ((GuiNpcCompanionTalents.GuiTalent)this.talents.get(this.selected.field_146127_k - 10)).talent;
          if (this.role.canAddExp(-exp) || this.role.currentExp > 0) {
               if (exp > this.role.currentExp) {
                    exp = this.role.currentExp;
               }

               NoppesUtilPlayer.sendData(EnumPlayerPacket.CompanionTalentExp, talent.ordinal(), exp);
               this.role.talents.put(talent, (Integer)this.role.talents.get(talent) + exp);
               this.role.addExp(-exp);
               this.getLabel(this.selected.field_146127_k - 10).label = this.role.talents.get(talent) + "/" + this.role.getNextLevel(talent);
          }
     }

     public void func_73863_a(int i, int j, float f) {
          super.func_73863_a(i, j, f);
          if (this.selected != null && this.field_146297_k.field_71441_e.func_72820_D() - this.startPressedTime > 4L && this.lastPressedTime < this.field_146297_k.field_71441_e.func_72820_D() && this.field_146297_k.field_71441_e.func_72820_D() % 4L == 0L) {
               if (this.selected.func_146116_c(this.field_146297_k, i, j) && Mouse.isButtonDown(0)) {
                    this.lastPressedTime = this.field_146297_k.field_71441_e.func_72820_D();
                    if (this.lastPressedTime - this.startPressedTime < 20L) {
                         this.addExperience(1);
                    } else if (this.lastPressedTime - this.startPressedTime < 40L) {
                         this.addExperience(2);
                    } else if (this.lastPressedTime - this.startPressedTime < 60L) {
                         this.addExperience(4);
                    } else if (this.lastPressedTime - this.startPressedTime < 90L) {
                         this.addExperience(8);
                    } else if (this.lastPressedTime - this.startPressedTime < 140L) {
                         this.addExperience(14);
                    } else {
                         this.addExperience(28);
                    }
               } else {
                    this.lastPressedTime = 0L;
                    this.selected = null;
               }
          }

          this.field_146297_k.func_110434_K().func_110577_a(Gui.field_110324_m);
          this.func_73729_b(this.guiLeft + 4, this.guiTop + 20, 10, 64, 162, 5);
          if (this.role.currentExp > 0) {
               float v = 1.0F * (float)this.role.currentExp / (float)this.role.getMaxExp();
               if (v > 1.0F) {
                    v = 1.0F;
               }

               this.func_73729_b(this.guiLeft + 4, this.guiTop + 20, 10, 69, (int)(v * 162.0F), 5);
          }

          String s = this.role.currentExp + "\\" + this.role.getMaxExp();
          this.field_146297_k.field_71466_p.func_78276_b(s, this.guiLeft + this.xSize / 2 - this.field_146297_k.field_71466_p.func_78256_a(s) / 2, this.guiTop + 10, CustomNpcResourceListener.DefaultTextColor);
          Iterator var5 = this.talents.values().iterator();

          while(var5.hasNext()) {
               GuiNpcCompanionTalents.GuiTalent talent = (GuiNpcCompanionTalents.GuiTalent)var5.next();
               talent.func_73863_a(i, j, f);
          }

     }

     public void save() {
     }

     public static class GuiTalent extends GuiScreen {
          private EnumCompanionTalent talent;
          private int x;
          private int y;
          private RoleCompanion role;
          private static final ResourceLocation resource = new ResourceLocation("customnpcs:textures/gui/talent.png");

          public GuiTalent(RoleCompanion role, EnumCompanionTalent talent, int x, int y) {
               this.talent = talent;
               this.x = x;
               this.y = y;
               this.role = role;
          }

          public void func_73863_a(int i, int j, float f) {
               Minecraft mc = Minecraft.func_71410_x();
               mc.func_110434_K().func_110577_a(resource);
               ItemStack item = this.talent.item;
               if (item.func_77973_b() == null) {
                    item = new ItemStack(Blocks.field_150346_d);
               }

               GlStateManager.func_179094_E();
               GlStateManager.func_179124_c(1.0F, 1.0F, 1.0F);
               GlStateManager.func_179147_l();
               boolean hover = this.x < i && this.x + 24 > i && this.y < j && this.y + 24 > j;
               this.func_73729_b(this.x, this.y, 0, hover ? 24 : 0, 24, 24);
               this.field_73735_i = 100.0F;
               this.field_146296_j.field_77023_b = 100.0F;
               GlStateManager.func_179145_e();
               GlStateManager.func_179091_B();
               RenderHelper.func_74520_c();
               this.field_146296_j.func_180450_b(item, this.x + 4, this.y + 4);
               this.field_146296_j.func_175030_a(mc.field_71466_p, item, this.x + 4, this.y + 4);
               RenderHelper.func_74518_a();
               GlStateManager.func_179140_f();
               GlStateManager.func_179109_b(0.0F, 0.0F, 200.0F);
               this.func_73732_a(mc.field_71466_p, this.role.getTalentLevel(this.talent) + "", this.x + 20, this.y + 16, 16777215);
               this.field_146296_j.field_77023_b = 0.0F;
               this.field_73735_i = 0.0F;
               GlStateManager.func_179121_F();
          }
     }
}
