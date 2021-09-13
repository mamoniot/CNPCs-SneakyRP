package noppes.npcs;

import io.netty.buffer.ByteBuf;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.rcon.RConConsoleSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.WeightedSpawnerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumPlayerData;
import noppes.npcs.containers.ContainerManageBanks;
import noppes.npcs.containers.ContainerManageRecipes;
import noppes.npcs.controllers.BankController;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.PlayerQuestController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.RecipeController;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.controllers.SyncController;
import noppes.npcs.controllers.TransportController;
import noppes.npcs.controllers.data.Bank;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogOption;
import noppes.npcs.controllers.data.Faction;
import noppes.npcs.controllers.data.PlayerBankData;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerDialogData;
import noppes.npcs.controllers.data.PlayerFactionData;
import noppes.npcs.controllers.data.PlayerQuestData;
import noppes.npcs.controllers.data.PlayerTransportData;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.RecipeCarpentry;
import noppes.npcs.controllers.data.TransportCategory;
import noppes.npcs.controllers.data.TransportLocation;
import noppes.npcs.entity.EntityDialogNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleTransporter;
import noppes.npcs.util.CustomNPCsScheduler;

public class NoppesUtilServer {
     private static HashMap editingQuests = new HashMap();
     private static HashMap editingQuestsClient = new HashMap();

     public static void setEditingNpc(EntityPlayer player, EntityNPCInterface npc) {
          PlayerData data = PlayerData.get(player);
          data.editingNpc = npc;
          if (npc != null) {
               Server.sendDataChecked((EntityPlayerMP)player, EnumPacketClient.EDIT_NPC, npc.func_145782_y());
          }

     }

     public static EntityNPCInterface getEditingNpc(EntityPlayer player) {
          PlayerData data = PlayerData.get(player);
          return data.editingNpc;
     }

     public static void setEditingQuest(EntityPlayer player, Quest quest) {
          if (player.field_70170_p.field_72995_K) {
               editingQuestsClient.put(player.func_110124_au(), quest);
          } else {
               editingQuests.put(player.func_110124_au(), quest);
          }

     }

     public static Quest getEditingQuest(EntityPlayer player) {
          return player.field_70170_p.field_72995_K ? (Quest)editingQuestsClient.get(player.func_110124_au()) : (Quest)editingQuests.get(player.func_110124_au());
     }

     public static void sendRoleData(EntityPlayer player, EntityNPCInterface npc) {
          if (npc.advanced.role != 0) {
               NBTTagCompound comp = new NBTTagCompound();
               npc.roleInterface.writeToNBT(comp);
               comp.func_74768_a("EntityId", npc.func_145782_y());
               comp.func_74768_a("Role", npc.advanced.role);
               Server.sendData((EntityPlayerMP)player, EnumPacketClient.ROLE, comp);
          }
     }

     public static void sendFactionDataAll(EntityPlayerMP player) {
          Map map = new HashMap();
          Iterator var2 = FactionController.instance.factions.values().iterator();

          while(var2.hasNext()) {
               Faction faction = (Faction)var2.next();
               map.put(faction.name, faction.id);
          }

          sendScrollData(player, map);
     }

     public static void sendBankDataAll(EntityPlayerMP player) {
          Map map = new HashMap();
          Iterator var2 = BankController.getInstance().banks.values().iterator();

          while(var2.hasNext()) {
               Bank bank = (Bank)var2.next();
               map.put(bank.name, bank.id);
          }

          sendScrollData(player, map);
     }

     public static void openDialog(EntityPlayer player, EntityNPCInterface npc, Dialog dia) {
          Dialog dialog = dia.copy(player);
          PlayerData playerdata = PlayerData.get(player);
          if (EventHooks.onNPCDialog(npc, player, dialog)) {
               playerdata.dialogId = -1;
          } else {
               playerdata.dialogId = dialog.id;
               if (!(npc instanceof EntityDialogNpc) && dia.id >= 0) {
                    Server.sendData((EntityPlayerMP)player, EnumPacketClient.DIALOG, npc.func_145782_y(), dialog.id);
               } else {
                    dialog.hideNPC = true;
                    Server.sendData((EntityPlayerMP)player, EnumPacketClient.DIALOG_DUMMY, npc.func_70005_c_(), dialog.writeToNBT(new NBTTagCompound()));
               }

               dia.factionOptions.addPoints(player);
               if (dialog.hasQuest()) {
                    PlayerQuestController.addActiveQuest(dialog.getQuest(), player);
               }

               if (!dialog.command.isEmpty()) {
                    runCommand(npc, npc.func_70005_c_(), dialog.command, player);
               }

               if (dialog.mail.isValid()) {
                    PlayerDataController.instance.addPlayerMessage(player.func_184102_h(), player.func_70005_c_(), dialog.mail);
               }

               PlayerDialogData data = playerdata.dialogData;
               if (!data.dialogsRead.contains(dialog.id) && dialog.id >= 0) {
                    data.dialogsRead.add(dialog.id);
                    playerdata.updateClient = true;
               }

               setEditingNpc(player, npc);
          }
     }

     public static String runCommand(ICommandSender executer, String name, String command, EntityPlayer player) {
          return runCommand(executer.func_130014_f_(), executer.func_180425_c(), name, command, player, executer);
     }

     public static String runCommand(final World world, final BlockPos pos, final String name, String command, EntityPlayer player, final ICommandSender executer) {
          if (!world.func_73046_m().func_82356_Z()) {
               LogWriter.warn("Cant run commands if CommandBlocks are disabled");
               return "Cant run commands if CommandBlocks are disabled";
          } else {
               if (player != null) {
                    command = command.replace("@dp", player.func_70005_c_());
               }

               command = command.replace("@npc", name);
               final TextComponentString output = new TextComponentString("");
               ICommandSender icommandsender = new RConConsoleSource(world.func_73046_m()) {
                    public String func_70005_c_() {
                         return "@CustomNPCs-" + name;
                    }

                    public ITextComponent func_145748_c_() {
                         return new TextComponentString(this.func_70005_c_());
                    }

                    public void func_145747_a(ITextComponent component) {
                         output.func_150257_a(component);
                    }

                    public boolean func_70003_b(int permLevel, String commandName) {
                         if (CustomNpcs.NpcUseOpCommands) {
                              return true;
                         } else {
                              return permLevel <= 2;
                         }
                    }

                    public BlockPos func_180425_c() {
                         return pos;
                    }

                    public Vec3d func_174791_d() {
                         return new Vec3d((double)pos.func_177958_n() + 0.5D, (double)pos.func_177956_o() + 0.5D, (double)pos.func_177952_p() + 0.5D);
                    }

                    public World func_130014_f_() {
                         return world;
                    }

                    public Entity func_174793_f() {
                         return executer == null ? null : executer.func_174793_f();
                    }

                    public boolean func_174792_t_() {
                         return this.func_184102_h().field_71305_c[0].func_82736_K().func_82766_b("commandBlockOutput");
                    }
               };
               ICommandManager icommandmanager = world.func_73046_m().func_71187_D();
               icommandmanager.func_71556_a(icommandsender, command);
               return output.func_150260_c().isEmpty() ? null : output.func_150260_c();
          }
     }

     public static void consumeItemStack(int i, EntityPlayer player) {
          ItemStack item = player.field_71071_by.func_70448_g();
          if (!player.field_71075_bZ.field_75098_d && item != null && !item.func_190926_b()) {
               item.func_190918_g(1);
               if (item.func_190916_E() <= 0) {
                    player.func_184611_a(EnumHand.MAIN_HAND, (ItemStack)null);
               }

          }
     }

     public static DataOutputStream getDataOutputStream(ByteArrayOutputStream stream) throws IOException {
          return new DataOutputStream(new GZIPOutputStream(stream));
     }

     public static void sendOpenGui(EntityPlayer player, EnumGuiType gui, EntityNPCInterface npc) {
          sendOpenGui(player, gui, npc, 0, 0, 0);
     }

     public static void sendOpenGui(EntityPlayer player, EnumGuiType gui, EntityNPCInterface npc, int i, int j, int k) {
          if (player instanceof EntityPlayerMP) {
               setEditingNpc(player, npc);
               sendExtraData(player, npc, gui, i, j, k);
               CustomNPCsScheduler.runTack(() -> {
                    if (CustomNpcs.proxy.getServerGuiElement(gui.ordinal(), player, player.field_70170_p, i, j, k) != null) {
                         player.openGui(CustomNpcs.instance, gui.ordinal(), player.field_70170_p, i, j, k);
                    } else {
                         Server.sendDataChecked((EntityPlayerMP)player, EnumPacketClient.GUI, gui.ordinal(), i, j, k);
                         ArrayList list = getScrollData(player, gui, npc);
                         if (list != null && !list.isEmpty()) {
                              Server.sendData((EntityPlayerMP)player, EnumPacketClient.SCROLL_LIST, list);
                         }
                    }
               }, 200);
          }
     }

     private static void sendExtraData(EntityPlayer player, EntityNPCInterface npc, EnumGuiType gui, int i, int j, int k) {
          if (gui == EnumGuiType.PlayerFollower || gui == EnumGuiType.PlayerFollowerHire || gui == EnumGuiType.PlayerTrader || gui == EnumGuiType.PlayerTransporter) {
               sendRoleData(player, npc);
          }

     }

     private static ArrayList getScrollData(EntityPlayer player, EnumGuiType gui, EntityNPCInterface npc) {
          if (gui == EnumGuiType.PlayerTransporter) {
               RoleTransporter role = (RoleTransporter)npc.roleInterface;
               ArrayList list = new ArrayList();
               TransportLocation location = role.getLocation();
               String name = role.getLocation().name;
               Iterator var7 = location.category.getDefaultLocations().iterator();

               while(var7.hasNext()) {
                    TransportLocation loc = (TransportLocation)var7.next();
                    if (!list.contains(loc.name)) {
                         list.add(loc.name);
                    }
               }

               PlayerTransportData playerdata = PlayerData.get(player).transportData;
               Iterator var12 = playerdata.transports.iterator();

               while(var12.hasNext()) {
                    int i = (Integer)var12.next();
                    TransportLocation loc = TransportController.getInstance().getTransport(i);
                    if (loc != null && location.category.locations.containsKey(loc.id) && !list.contains(loc.name)) {
                         list.add(loc.name);
                    }
               }

               list.remove(name);
               return list;
          } else {
               return null;
          }
     }

     public static void spawnParticle(Entity entity, String particle, int dimension) {
          Server.sendAssociatedData(entity, EnumPacketClient.PARTICLE, entity.field_70165_t, entity.field_70163_u, entity.field_70161_v, entity.field_70131_O, entity.field_70130_N, particle);
     }

     public static void deleteNpc(EntityNPCInterface npc, EntityPlayer player) {
          Server.sendAssociatedData(npc, EnumPacketClient.DELETE_NPC, npc.func_145782_y());
     }

     public static void createMobSpawner(BlockPos pos, NBTTagCompound comp, EntityPlayer player) {
          ServerCloneController.Instance.cleanTags(comp);
          if (comp.func_74779_i("id").equalsIgnoreCase("entityhorse")) {
               player.func_145747_a(new TextComponentTranslation("Currently you cant create horse spawner, its a minecraft bug", new Object[0]));
          } else {
               player.field_70170_p.func_175656_a(pos, Blocks.field_150474_ac.func_176223_P());
               TileEntityMobSpawner tile = (TileEntityMobSpawner)player.field_70170_p.func_175625_s(pos);
               MobSpawnerBaseLogic logic = tile.func_145881_a();
               if (!comp.func_150297_b("id", 8)) {
                    comp.func_74778_a("id", "Pig");
               }

               comp.func_74783_a("StartPosNew", new int[]{pos.func_177958_n(), pos.func_177956_o(), pos.func_177952_p()});
               logic.func_184993_a(new WeightedSpawnerEntity(1, comp));
          }
     }

     public static void sendPlayerData(EnumPlayerData type, EntityPlayerMP player, String name) {
          Map map = new HashMap();
          if (type == EnumPlayerData.Players) {
               Iterator var4 = PlayerDataController.instance.nameUUIDs.keySet().iterator();

               while(var4.hasNext()) {
                    String username = (String)var4.next();
                    map.put(username, 0);
               }

               String[] var9 = player.func_184102_h().func_184103_al().func_72369_d();
               int var11 = var9.length;

               for(int var6 = 0; var6 < var11; ++var6) {
                    String username = var9[var6];
                    map.put(username, 0);
               }
          } else {
               PlayerData playerdata = PlayerDataController.instance.getDataFromUsername(player.func_184102_h(), name);
               Iterator var15;
               int factionId;
               if (type == EnumPlayerData.Dialog) {
                    PlayerDialogData data = playerdata.dialogData;
                    var15 = data.dialogsRead.iterator();

                    while(var15.hasNext()) {
                         factionId = (Integer)var15.next();
                         Dialog dialog = (Dialog)DialogController.instance.dialogs.get(factionId);
                         if (dialog != null) {
                              map.put(dialog.category.title + ": " + dialog.title, factionId);
                         }
                    }
               } else if (type == EnumPlayerData.Quest) {
                    PlayerQuestData data = playerdata.questData;
                    var15 = data.activeQuests.keySet().iterator();

                    Quest quest;
                    while(var15.hasNext()) {
                         factionId = (Integer)var15.next();
                         quest = (Quest)QuestController.instance.quests.get(factionId);
                         if (quest != null) {
                              map.put(quest.category.title + ": " + quest.title + "(Active quest)", factionId);
                         }
                    }

                    var15 = data.finishedQuests.keySet().iterator();

                    while(var15.hasNext()) {
                         factionId = (Integer)var15.next();
                         quest = (Quest)QuestController.instance.quests.get(factionId);
                         if (quest != null) {
                              map.put(quest.category.title + ": " + quest.title + "(Finished quest)", factionId);
                         }
                    }
               } else if (type == EnumPlayerData.Transport) {
                    PlayerTransportData data = playerdata.transportData;
                    var15 = data.transports.iterator();

                    while(var15.hasNext()) {
                         factionId = (Integer)var15.next();
                         TransportLocation location = TransportController.getInstance().getTransport(factionId);
                         if (location != null) {
                              map.put(location.category.title + ": " + location.name, factionId);
                         }
                    }
               } else if (type == EnumPlayerData.Bank) {
                    PlayerBankData data = playerdata.bankData;
                    var15 = data.banks.keySet().iterator();

                    while(var15.hasNext()) {
                         factionId = (Integer)var15.next();
                         Bank bank = (Bank)BankController.getInstance().banks.get(factionId);
                         if (bank != null) {
                              map.put(bank.name, factionId);
                         }
                    }
               } else if (type == EnumPlayerData.Factions) {
                    PlayerFactionData data = playerdata.factionData;
                    var15 = data.factionData.keySet().iterator();

                    while(var15.hasNext()) {
                         factionId = (Integer)var15.next();
                         Faction faction = (Faction)FactionController.instance.factions.get(factionId);
                         if (faction != null) {
                              map.put(faction.name + "(" + data.getFactionPoints(player, factionId) + ")", factionId);
                         }
                    }
               }
          }

          sendScrollData(player, map);
     }

     public static void removePlayerData(ByteBuf buffer, EntityPlayerMP player) throws IOException {
          int id = buffer.readInt();
          if (EnumPlayerData.values().length > id) {
               String name = Server.readString(buffer);
               if (name != null && !name.isEmpty()) {
                    EnumPlayerData type = EnumPlayerData.values()[id];
                    EntityPlayer pl = player.func_184102_h().func_184103_al().func_152612_a(name);
                    PlayerData playerdata = null;
                    if (pl == null) {
                         playerdata = PlayerDataController.instance.getDataFromUsername(player.func_184102_h(), name);
                    } else {
                         playerdata = PlayerData.get(pl);
                    }

                    if (type == EnumPlayerData.Players) {
                         File file = new File(CustomNpcs.getWorldSaveDirectory("playerdata"), playerdata.uuid + ".json");
                         if (file.exists()) {
                              file.delete();
                         }

                         if (pl != null) {
                              playerdata.setNBT(new NBTTagCompound());
                              sendPlayerData(type, player, name);
                              playerdata.save(true);
                              return;
                         }

                         PlayerDataController.instance.nameUUIDs.remove(name);
                    }

                    if (type == EnumPlayerData.Quest) {
                         PlayerQuestData data = playerdata.questData;
                         int questId = buffer.readInt();
                         data.activeQuests.remove(questId);
                         data.finishedQuests.remove(questId);
                         playerdata.save(true);
                    }

                    if (type == EnumPlayerData.Dialog) {
                         PlayerDialogData data = playerdata.dialogData;
                         data.dialogsRead.remove(buffer.readInt());
                         playerdata.save(true);
                    }

                    if (type == EnumPlayerData.Transport) {
                         PlayerTransportData data = playerdata.transportData;
                         data.transports.remove(buffer.readInt());
                         playerdata.save(true);
                    }

                    if (type == EnumPlayerData.Bank) {
                         PlayerBankData data = playerdata.bankData;
                         data.banks.remove(buffer.readInt());
                         playerdata.save(true);
                    }

                    if (type == EnumPlayerData.Factions) {
                         PlayerFactionData data = playerdata.factionData;
                         data.factionData.remove(buffer.readInt());
                         playerdata.save(true);
                    }

                    if (pl != null) {
                         SyncController.syncPlayer((EntityPlayerMP)pl);
                    }

                    sendPlayerData(type, player, name);
               }
          }
     }

     public static void sendRecipeData(EntityPlayerMP player, int size) {
          HashMap map = new HashMap();
          Iterator var3;
          RecipeCarpentry recipe;
          if (size == 3) {
               var3 = RecipeController.instance.globalRecipes.values().iterator();

               while(var3.hasNext()) {
                    recipe = (RecipeCarpentry)var3.next();
                    map.put(recipe.name, recipe.id);
               }
          } else {
               var3 = RecipeController.instance.anvilRecipes.values().iterator();

               while(var3.hasNext()) {
                    recipe = (RecipeCarpentry)var3.next();
                    map.put(recipe.name, recipe.id);
               }
          }

          sendScrollData(player, map);
     }

     public static void sendScrollData(EntityPlayerMP player, Map map) {
          Map send = new HashMap();
          Iterator var3 = map.keySet().iterator();

          while(var3.hasNext()) {
               String key = (String)var3.next();
               send.put(key, map.get(key));
               if (send.size() == 100) {
                    Server.sendData(player, EnumPacketClient.SCROLL_DATA_PART, send);
                    send = new HashMap();
               }
          }

          Server.sendData(player, EnumPacketClient.SCROLL_DATA, send);
     }

     public static void sendTransportCategoryData(EntityPlayerMP player) {
          HashMap map = new HashMap();
          Iterator var2 = TransportController.getInstance().categories.values().iterator();

          while(var2.hasNext()) {
               TransportCategory category = (TransportCategory)var2.next();
               map.put(category.title, category.id);
          }

          sendScrollData(player, map);
     }

     public static void sendTransportData(EntityPlayerMP player, int categoryid) {
          TransportCategory category = (TransportCategory)TransportController.getInstance().categories.get(categoryid);
          if (category != null) {
               HashMap map = new HashMap();
               Iterator var4 = category.locations.values().iterator();

               while(var4.hasNext()) {
                    TransportLocation transport = (TransportLocation)var4.next();
                    map.put(transport.name, transport.id);
               }

               sendScrollData(player, map);
          }
     }

     public static void sendNpcDialogs(EntityPlayer player) {
          EntityNPCInterface npc = getEditingNpc(player);
          if (npc != null) {
               Iterator var2 = npc.dialogs.keySet().iterator();

               while(var2.hasNext()) {
                    int pos = (Integer)var2.next();
                    DialogOption option = (DialogOption)npc.dialogs.get(pos);
                    if (option != null && option.hasDialog()) {
                         NBTTagCompound compound = option.writeNBT();
                         compound.func_74768_a("Position", pos);
                         Server.sendData((EntityPlayerMP)player, EnumPacketClient.GUI_DATA, compound);
                    }
               }

          }
     }

     public static DialogOption setNpcDialog(int slot, int dialogId, EntityPlayer player) {
          EntityNPCInterface npc = getEditingNpc(player);
          if (npc == null) {
               return null;
          } else {
               if (!npc.dialogs.containsKey(slot)) {
                    npc.dialogs.put(slot, new DialogOption());
               }

               DialogOption option = (DialogOption)npc.dialogs.get(slot);
               option.dialogId = dialogId;
               option.optionType = 1;
               if (option.hasDialog()) {
                    option.title = option.getDialog().title;
               }

               return option;
          }
     }

     public static TileEntity saveTileEntity(EntityPlayerMP player, NBTTagCompound compound) {
          int x = compound.func_74762_e("x");
          int y = compound.func_74762_e("y");
          int z = compound.func_74762_e("z");
          TileEntity tile = player.field_70170_p.func_175625_s(new BlockPos(x, y, z));
          if (tile != null) {
               tile.func_145839_a(compound);
          }

          return tile;
     }

     public static void setRecipeGui(EntityPlayerMP player, RecipeCarpentry recipe) {
          if (recipe != null) {
               if (player.field_71070_bA instanceof ContainerManageRecipes) {
                    ContainerManageRecipes container = (ContainerManageRecipes)player.field_71070_bA;
                    container.setRecipe(recipe);
                    Server.sendData(player, EnumPacketClient.GUI_DATA, recipe.writeNBT());
               }
          }
     }

     public static void sendBank(EntityPlayerMP player, Bank bank) {
          NBTTagCompound compound = new NBTTagCompound();
          bank.writeEntityToNBT(compound);
          Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
          if (player.field_71070_bA instanceof ContainerManageBanks) {
               ((ContainerManageBanks)player.field_71070_bA).setBank(bank);
          }

          player.func_71110_a(player.field_71070_bA, player.field_71070_bA.func_75138_a());
     }

     public static void sendNearbyNpcs(EntityPlayerMP player) {
          HashMap map = new HashMap();
          Iterator var2 = player.field_70170_p.field_72996_f.iterator();

          while(var2.hasNext()) {
               Entity entity = (Entity)var2.next();
               if (entity instanceof EntityNPCInterface) {
                    EntityNPCInterface npc = (EntityNPCInterface)entity;
                    if (!npc.field_70128_L) {
                         float distance = player.func_70032_d(npc);
                         DecimalFormat df = new DecimalFormat("#.#");
                         String s = df.format((double)distance);
                         if (distance < 10.0F) {
                              s = "0" + s;
                         }

                         map.put(s + " : " + npc.display.getName(), npc.func_145782_y());
                    }
               }
          }

          sendScrollData(player, map);
     }

     public static void sendGuiError(EntityPlayer player, int i) {
          Server.sendData((EntityPlayerMP)player, EnumPacketClient.GUI_ERROR, i, new NBTTagCompound());
     }

     public static void sendGuiClose(EntityPlayerMP player, int i, NBTTagCompound comp) {
          Server.sendData(player, EnumPacketClient.GUI_CLOSE, i, comp);
     }

     public static Entity spawnClone(NBTTagCompound compound, double x, double y, double z, World world) {
          ServerCloneController.Instance.cleanTags(compound);
          compound.func_74782_a("Pos", NBTTags.nbtDoubleList(x, y, z));
          Entity entity = EntityList.func_75615_a(compound, world);
          if (entity == null) {
               return null;
          } else {
               if (entity instanceof EntityNPCInterface) {
                    EntityNPCInterface npc = (EntityNPCInterface)entity;
                    npc.ais.setStartPos(new BlockPos(npc));
               }

               world.func_72838_d(entity);
               return entity;
          }
     }

     public static boolean isOp(EntityPlayer player) {
          return player.func_184102_h().func_184103_al().func_152596_g(player.func_146103_bH());
     }

     public static void GivePlayerItem(Entity entity, EntityPlayer player, ItemStack item) {
          if (!entity.field_70170_p.field_72995_K && item != null && !item.func_190926_b()) {
               item = item.func_77946_l();
               float f = 0.7F;
               double d = (double)(entity.field_70170_p.field_73012_v.nextFloat() * f) + (double)(1.0F - f);
               double d1 = (double)(entity.field_70170_p.field_73012_v.nextFloat() * f) + (double)(1.0F - f);
               double d2 = (double)(entity.field_70170_p.field_73012_v.nextFloat() * f) + (double)(1.0F - f);
               EntityItem entityitem = new EntityItem(entity.field_70170_p, entity.field_70165_t + d, entity.field_70163_u + d1, entity.field_70161_v + d2, item);
               entityitem.func_174867_a(2);
               entity.field_70170_p.func_72838_d(entityitem);
               int i = item.func_190916_E();
               if (player.field_71071_by.func_70441_a(item)) {
                    entity.field_70170_p.func_184148_a((EntityPlayer)null, player.field_70165_t, player.field_70163_u, player.field_70161_v, SoundEvents.field_187638_cR, SoundCategory.PLAYERS, 0.2F, ((player.func_70681_au().nextFloat() - player.func_70681_au().nextFloat()) * 0.7F + 1.0F) * 2.0F);
                    player.func_71001_a(entityitem, i);
                    PlayerQuestData playerdata = PlayerData.get(player).questData;
                    playerdata.checkQuestCompletion(player, 0);
                    if (item.func_190916_E() <= 0) {
                         entityitem.func_70106_y();
                    }
               }

          }
     }

     public static BlockPos GetClosePos(BlockPos origin, World world) {
          for(int x = -1; x < 2; ++x) {
               for(int z = -1; z < 2; ++z) {
                    for(int y = 2; y >= -2; --y) {
                         BlockPos pos = origin.func_177982_a(x, y, z);
                         if (world.isSideSolid(pos, EnumFacing.UP) && world.func_175623_d(pos.func_177984_a()) && world.func_175623_d(pos.func_177981_b(2))) {
                              return pos.func_177984_a();
                         }
                    }
               }
          }

          return world.func_175672_r(origin);
     }

     public static void NotifyOPs(String message, Object... obs) {
          TextComponentTranslation chatcomponenttranslation = new TextComponentTranslation(message, obs);
          chatcomponenttranslation.func_150256_b().func_150238_a(TextFormatting.GRAY);
          chatcomponenttranslation.func_150256_b().func_150217_b(true);
          Iterator iterator = CustomNpcs.Server.func_184103_al().func_181057_v().iterator();

          while(iterator.hasNext()) {
               EntityPlayer entityplayer = (EntityPlayer)iterator.next();
               if (entityplayer.func_174792_t_() && isOp(entityplayer)) {
                    entityplayer.func_145747_a(chatcomponenttranslation);
               }
          }

          if (CustomNpcs.Server.field_71305_c[0].func_82736_K().func_82766_b("logAdminCommands")) {
               LogWriter.info(chatcomponenttranslation.func_150260_c());
          }

     }

     public static void playSound(EntityLivingBase entity, SoundEvent sound, float volume, float pitch) {
          entity.field_70170_p.func_184148_a((EntityPlayer)null, entity.field_70165_t, entity.field_70163_u, entity.field_70161_v, sound, SoundCategory.NEUTRAL, volume, pitch);
     }

     public static void playSound(World world, BlockPos pos, SoundEvent sound, SoundCategory cat, float volume, float pitch) {
          world.func_184133_a((EntityPlayer)null, pos, sound, cat, volume, pitch);
     }

     public static EntityPlayer getPlayer(MinecraftServer minecraftserver, UUID id) {
          List list = minecraftserver.func_184103_al().func_181057_v();
          Iterator var3 = list.iterator();

          EntityPlayer player;
          do {
               if (!var3.hasNext()) {
                    return null;
               }

               player = (EntityPlayer)var3.next();
          } while(!id.equals(player.func_110124_au()));

          return player;
     }

     public static Entity GetDamageSourcee(DamageSource damagesource) {
          Entity entity = damagesource.func_76346_g();
          if (entity == null) {
               entity = damagesource.func_76364_f();
          }

          if (entity instanceof EntityArrow && ((EntityArrow)entity).field_70250_c instanceof EntityLivingBase) {
               entity = ((EntityArrow)entity).field_70250_c;
          } else if (entity instanceof EntityThrowable) {
               entity = ((EntityThrowable)entity).func_85052_h();
          }

          return (Entity)entity;
     }

     public static boolean IsItemStackNull(ItemStack is) {
          return is == null || is.func_190926_b() || is == ItemStack.field_190927_a || is.func_77973_b() == null;
     }

     public static ItemStack ChangeItemStack(ItemStack is, Item item) {
          NBTTagCompound comp = is.func_77955_b(new NBTTagCompound());
          ResourceLocation resourcelocation = (ResourceLocation)Item.field_150901_e.func_177774_c(item);
          comp.func_74778_a("id", resourcelocation == null ? "minecraft:air" : resourcelocation.toString());
          return new ItemStack(comp);
     }
}
