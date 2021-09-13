package noppes.npcs.client.gui;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.IMerchant;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.util.ResourceLocation;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.ServerEventsHandler;
import noppes.npcs.client.Client;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.containers.ContainerMerchantAdd;

@SideOnly(Side.CLIENT)
public class GuiMerchantAdd extends GuiContainer {
     private static final ResourceLocation merchantGuiTextures = new ResourceLocation("textures/gui/container/villager.png");
     private IMerchant theIMerchant;
     private GuiMerchantAdd.MerchantButton nextRecipeButtonIndex;
     private GuiMerchantAdd.MerchantButton previousRecipeButtonIndex;
     private int currentRecipeIndex;
     private String field_94082_v;

     public GuiMerchantAdd() {
          super(new ContainerMerchantAdd(Minecraft.func_71410_x().field_71439_g, ServerEventsHandler.Merchant, Minecraft.func_71410_x().field_71441_e));
          this.theIMerchant = ServerEventsHandler.Merchant;
          this.field_94082_v = I18n.func_135052_a("entity.Villager.name", new Object[0]);
     }

     public void func_73866_w_() {
          super.func_73866_w_();
          int i = (this.field_146294_l - this.field_146999_f) / 2;
          int j = (this.field_146295_m - this.field_147000_g) / 2;
          this.field_146292_n.add(this.nextRecipeButtonIndex = new GuiMerchantAdd.MerchantButton(1, i + 120 + 27, j + 24 - 1, true));
          this.field_146292_n.add(this.previousRecipeButtonIndex = new GuiMerchantAdd.MerchantButton(2, i + 36 - 19, j + 24 - 1, false));
          this.field_146292_n.add(new GuiNpcButton(4, i + this.field_146999_f, j + 20, 60, 20, "gui.remove"));
          this.field_146292_n.add(new GuiNpcButton(5, i + this.field_146999_f, j + 50, 60, 20, "gui.add"));
          this.nextRecipeButtonIndex.field_146124_l = false;
          this.previousRecipeButtonIndex.field_146124_l = false;
     }

     protected void func_146979_b(int par1, int par2) {
          this.field_146289_q.func_78276_b(this.field_94082_v, this.field_146999_f / 2 - this.field_146289_q.func_78256_a(this.field_94082_v) / 2, 6, CustomNpcResourceListener.DefaultTextColor);
          this.field_146289_q.func_78276_b(I18n.func_135052_a("container.inventory", new Object[0]), 8, this.field_147000_g - 96 + 2, CustomNpcResourceListener.DefaultTextColor);
     }

     public void func_73876_c() {
          super.func_73876_c();
          Minecraft mc = Minecraft.func_71410_x();
          MerchantRecipeList merchantrecipelist = this.theIMerchant.func_70934_b(mc.field_71439_g);
          if (merchantrecipelist != null) {
               this.nextRecipeButtonIndex.field_146124_l = this.currentRecipeIndex < merchantrecipelist.size() - 1;
               this.previousRecipeButtonIndex.field_146124_l = this.currentRecipeIndex > 0;
          }

     }

     protected void func_146284_a(GuiButton par1GuiButton) {
          boolean flag = false;
          Minecraft mc = Minecraft.func_71410_x();
          if (par1GuiButton == this.nextRecipeButtonIndex) {
               ++this.currentRecipeIndex;
               flag = true;
          } else if (par1GuiButton == this.previousRecipeButtonIndex) {
               --this.currentRecipeIndex;
               flag = true;
          }

          if (par1GuiButton.field_146127_k == 4) {
               MerchantRecipeList merchantrecipelist = this.theIMerchant.func_70934_b(mc.field_71439_g);
               if (this.currentRecipeIndex < merchantrecipelist.size()) {
                    merchantrecipelist.remove(this.currentRecipeIndex);
                    if (this.currentRecipeIndex > 0) {
                         --this.currentRecipeIndex;
                    }

                    Client.sendData(EnumPacketServer.MerchantUpdate, ServerEventsHandler.Merchant.func_145782_y(), merchantrecipelist);
               }
          }

          if (par1GuiButton.field_146127_k == 5) {
               ItemStack item1 = this.field_147002_h.func_75139_a(0).func_75211_c();
               ItemStack item2 = this.field_147002_h.func_75139_a(1).func_75211_c();
               ItemStack sold = this.field_147002_h.func_75139_a(2).func_75211_c();
               if (item1 == null && item2 != null) {
                    item1 = item2;
                    item2 = null;
               }

               if (item1 != null && sold != null) {
                    item1 = item1.func_77946_l();
                    sold = sold.func_77946_l();
                    if (item2 != null) {
                         item2 = item2.func_77946_l();
                    }

                    MerchantRecipe recipe = new MerchantRecipe(item1, item2, sold);
                    recipe.func_82783_a(2147483639);
                    MerchantRecipeList merchantrecipelist = this.theIMerchant.func_70934_b(mc.field_71439_g);
                    merchantrecipelist.add(recipe);
                    Client.sendData(EnumPacketServer.MerchantUpdate, ServerEventsHandler.Merchant.func_145782_y(), merchantrecipelist);
               }
          }

          if (flag) {
               ((ContainerMerchantAdd)this.field_147002_h).setCurrentRecipeIndex(this.currentRecipeIndex);
               PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
               packetbuffer.writeInt(this.currentRecipeIndex);
               this.field_146297_k.func_147114_u().func_147297_a(new CPacketCustomPayload("MC|TrSel", packetbuffer));
          }

     }

     protected void func_146976_a(float par1, int par2, int par3) {
          Minecraft mc = Minecraft.func_71410_x();
          GlStateManager.func_179131_c(1.0F, 1.0F, 1.0F, 1.0F);
          mc.func_110434_K().func_110577_a(merchantGuiTextures);
          int k = (this.field_146294_l - this.field_146999_f) / 2;
          int l = (this.field_146295_m - this.field_147000_g) / 2;
          this.func_73729_b(k, l, 0, 0, this.field_146999_f, this.field_147000_g);
          MerchantRecipeList merchantrecipelist = this.theIMerchant.func_70934_b(mc.field_71439_g);
          if (merchantrecipelist != null && !merchantrecipelist.isEmpty()) {
               int i1 = this.currentRecipeIndex;
               MerchantRecipe merchantrecipe = (MerchantRecipe)merchantrecipelist.get(i1);
               if (merchantrecipe.func_82784_g()) {
                    mc.func_110434_K().func_110577_a(merchantGuiTextures);
                    GlStateManager.func_179131_c(1.0F, 1.0F, 1.0F, 1.0F);
                    GlStateManager.func_179140_f();
                    this.func_73729_b(this.field_147003_i + 83, this.field_147009_r + 21, 212, 0, 28, 21);
                    this.func_73729_b(this.field_147003_i + 83, this.field_147009_r + 51, 212, 0, 28, 21);
               }
          }

     }

     public void func_73863_a(int par1, int par2, float par3) {
          super.func_73863_a(par1, par2, par3);
          Minecraft mc = Minecraft.func_71410_x();
          MerchantRecipeList merchantrecipelist = this.theIMerchant.func_70934_b(mc.field_71439_g);
          if (merchantrecipelist != null && !merchantrecipelist.isEmpty()) {
               int k = (this.field_146294_l - this.field_146999_f) / 2;
               int l = (this.field_146295_m - this.field_147000_g) / 2;
               int i1 = this.currentRecipeIndex;
               MerchantRecipe merchantrecipe = (MerchantRecipe)merchantrecipelist.get(i1);
               GlStateManager.func_179094_E();
               ItemStack itemstack = merchantrecipe.func_77394_a();
               ItemStack itemstack1 = merchantrecipe.func_77396_b();
               ItemStack itemstack2 = merchantrecipe.func_77397_d();
               RenderHelper.func_74520_c();
               GlStateManager.func_179091_B();
               GlStateManager.func_179142_g();
               GlStateManager.func_179145_e();
               this.field_146296_j.field_77023_b = 100.0F;
               this.field_146296_j.func_180450_b(itemstack, k + 36, l + 24);
               this.field_146296_j.func_175030_a(this.field_146289_q, itemstack, k + 36, l + 24);
               if (itemstack1 != null) {
                    this.field_146296_j.func_180450_b(itemstack1, k + 62, l + 24);
                    this.field_146296_j.func_175030_a(this.field_146289_q, itemstack1, k + 62, l + 24);
               }

               this.field_146296_j.func_180450_b(itemstack2, k + 120, l + 24);
               this.field_146296_j.func_175030_a(this.field_146289_q, itemstack2, k + 120, l + 24);
               this.field_146296_j.field_77023_b = 0.0F;
               GlStateManager.func_179140_f();
               if (this.func_146978_c(36, 24, 16, 16, par1, par2)) {
                    this.func_146285_a(itemstack, par1, par2);
               } else if (itemstack1 != null && this.func_146978_c(62, 24, 16, 16, par1, par2)) {
                    this.func_146285_a(itemstack1, par1, par2);
               } else if (this.func_146978_c(120, 24, 16, 16, par1, par2)) {
                    this.func_146285_a(itemstack2, par1, par2);
               }

               GlStateManager.func_179121_F();
               GlStateManager.func_179145_e();
               GlStateManager.func_179126_j();
               RenderHelper.func_74519_b();
          }

     }

     public IMerchant getIMerchant() {
          return this.theIMerchant;
     }

     static ResourceLocation func_110417_h() {
          return merchantGuiTextures;
     }

     @SideOnly(Side.CLIENT)
     static class MerchantButton extends GuiButton {
          private final boolean field_146157_o;
          private static final String __OBFID = "CL_00000763";

          public MerchantButton(int par1, int par2, int par3, boolean par4) {
               super(par1, par2, par3, 12, 19, "");
               this.field_146157_o = par4;
          }

          public void func_191745_a(Minecraft minecraft, int p_146112_2_, int p_146112_3_, float partialTicks) {
               if (this.field_146125_m) {
                    minecraft.func_110434_K().func_110577_a(GuiMerchantAdd.merchantGuiTextures);
                    GlStateManager.func_179131_c(1.0F, 1.0F, 1.0F, 1.0F);
                    boolean flag = p_146112_2_ >= this.field_146128_h && p_146112_3_ >= this.field_146129_i && p_146112_2_ < this.field_146128_h + this.field_146120_f && p_146112_3_ < this.field_146129_i + this.field_146121_g;
                    int k = 0;
                    int l = 176;
                    if (!this.field_146124_l) {
                         l += this.field_146120_f * 2;
                    } else if (flag) {
                         l += this.field_146120_f;
                    }

                    if (!this.field_146157_o) {
                         k += this.field_146121_g;
                    }

                    this.func_73729_b(this.field_146128_h, this.field_146129_i, l, k, this.field_146120_f, this.field_146121_g);
               }

          }
     }
}
