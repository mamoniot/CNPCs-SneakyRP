package noppes.npcs.controllers.data;

import java.util.HashMap;
import java.util.Iterator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.NBTTags;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.Server;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.containers.ContainerNPCBankInterface;
import noppes.npcs.controllers.BankController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.CustomNPCsScheduler;

public class BankData {
     public HashMap itemSlots = new HashMap();
     public HashMap upgradedSlots = new HashMap();
     public int unlockedSlots = 0;
     public int bankId = -1;

     public BankData() {
          for(int i = 0; i < 6; ++i) {
               this.itemSlots.put(i, new NpcMiscInventory(54));
               this.upgradedSlots.put(i, false);
          }

     }

     public void readNBT(NBTTagCompound nbttagcompound) {
          this.bankId = nbttagcompound.func_74762_e("DataBankId");
          this.unlockedSlots = nbttagcompound.func_74762_e("UnlockedSlots");
          this.itemSlots = this.getItemSlots(nbttagcompound.func_150295_c("BankInv", 10));
          this.upgradedSlots = NBTTags.getBooleanList(nbttagcompound.func_150295_c("UpdatedSlots", 10));
     }

     private HashMap getItemSlots(NBTTagList tagList) {
          HashMap list = new HashMap();

          for(int i = 0; i < tagList.func_74745_c(); ++i) {
               NBTTagCompound nbttagcompound = tagList.func_150305_b(i);
               int slot = nbttagcompound.func_74762_e("Slot");
               NpcMiscInventory inv = new NpcMiscInventory(54);
               inv.setFromNBT(nbttagcompound.func_74775_l("BankItems"));
               list.put(slot, inv);
          }

          return list;
     }

     public void writeNBT(NBTTagCompound nbttagcompound) {
          nbttagcompound.func_74768_a("DataBankId", this.bankId);
          nbttagcompound.func_74768_a("UnlockedSlots", this.unlockedSlots);
          nbttagcompound.func_74782_a("UpdatedSlots", NBTTags.nbtBooleanList(this.upgradedSlots));
          nbttagcompound.func_74782_a("BankInv", this.nbtItemSlots(this.itemSlots));
     }

     private NBTTagList nbtItemSlots(HashMap items) {
          NBTTagList list = new NBTTagList();
          Iterator var3 = items.keySet().iterator();

          while(var3.hasNext()) {
               int slot = (Integer)var3.next();
               NBTTagCompound nbttagcompound = new NBTTagCompound();
               nbttagcompound.func_74768_a("Slot", slot);
               nbttagcompound.func_74782_a("BankItems", ((NpcMiscInventory)items.get(slot)).getToNBT());
               list.func_74742_a(nbttagcompound);
          }

          return list;
     }

     public boolean isUpgraded(Bank bank, int slot) {
          if (bank.isUpgraded(slot)) {
               return true;
          } else {
               return bank.canBeUpgraded(slot) && (Boolean)this.upgradedSlots.get(slot);
          }
     }

     public void openBankGui(EntityPlayer player, EntityNPCInterface npc, int bankId, int slot) {
          Bank bank = BankController.getInstance().getBank(bankId);
          if (bank.getMaxSlots() > slot) {
               if (bank.startSlots > this.unlockedSlots) {
                    this.unlockedSlots = bank.startSlots;
               }

               ItemStack currency = ItemStack.field_190927_a;
               if (this.unlockedSlots <= slot) {
                    currency = bank.currencyInventory.func_70301_a(slot);
                    NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerBankUnlock, npc, slot, bank.id, 0);
               } else if (this.isUpgraded(bank, slot)) {
                    NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerBankLarge, npc, slot, bank.id, 0);
               } else if (bank.canBeUpgraded(slot)) {
                    currency = bank.upgradeInventory.func_70301_a(slot);
                    NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerBankUprade, npc, slot, bank.id, 0);
               } else {
                    NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerBankSmall, npc, slot, bank.id, 0);
               }

               CustomNPCsScheduler.runTack(() -> {
                    NBTTagCompound compound = new NBTTagCompound();
                    compound.func_74768_a("MaxSlots", bank.getMaxSlots());
                    compound.func_74768_a("UnlockedSlots", this.unlockedSlots);
                    if (currency != null && !currency.func_190926_b()) {
                         compound.func_74782_a("Currency", currency.func_77955_b(new NBTTagCompound()));
                         ContainerNPCBankInterface container = this.getContainer(player);
                         if (container != null) {
                              container.setCurrency(currency);
                         }
                    }

                    Server.sendDataChecked((EntityPlayerMP)player, EnumPacketClient.GUI_DATA, compound);
               }, 300);
          }
     }

     private ContainerNPCBankInterface getContainer(EntityPlayer player) {
          Container con = player.field_71070_bA;
          return con != null && con instanceof ContainerNPCBankInterface ? (ContainerNPCBankInterface)con : null;
     }
}
