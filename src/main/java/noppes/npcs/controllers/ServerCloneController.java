package noppes.npcs.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextComponentString;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.handler.ICloneHandler;
import noppes.npcs.util.NBTJsonUtil;

public class ServerCloneController implements ICloneHandler {
     public static ServerCloneController Instance;

     public ServerCloneController() {
          this.loadClones();
     }

     private void loadClones() {
          try {
               File dir = new File(this.getDir(), "..");
               File file = new File(dir, "clonednpcs.dat");
               if (file.exists()) {
                    Map clones = this.loadOldClones(file);
                    file.delete();
                    file = new File(dir, "clonednpcs.dat_old");
                    if (file.exists()) {
                         file.delete();
                    }

                    Iterator var4 = clones.keySet().iterator();

                    while(var4.hasNext()) {
                         int tab = (Integer)var4.next();
                         Map map = (Map)clones.get(tab);
                         Iterator var7 = map.keySet().iterator();

                         while(var7.hasNext()) {
                              String name = (String)var7.next();
                              this.saveClone(tab, name, (NBTTagCompound)map.get(name));
                         }
                    }
               }
          } catch (Exception var9) {
               LogWriter.except(var9);
          }

     }

     public File getDir() {
          File dir = new File(CustomNpcs.getWorldSaveDirectory(), "clones");
          if (!dir.exists()) {
               dir.mkdir();
          }

          return dir;
     }

     private Map loadOldClones(File file) throws Exception {
          Map clones = new HashMap();
          NBTTagCompound nbttagcompound1 = CompressedStreamTools.func_74796_a(new FileInputStream(file));
          NBTTagList list = nbttagcompound1.func_150295_c("Data", 10);
          if (list == null) {
               return clones;
          } else {
               for(int i = 0; i < list.func_74745_c(); ++i) {
                    NBTTagCompound compound = list.func_150305_b(i);
                    if (!compound.func_74764_b("ClonedTab")) {
                         compound.func_74768_a("ClonedTab", 1);
                    }

                    Map tab = (Map)clones.get(compound.func_74762_e("ClonedTab"));
                    if (tab == null) {
                         clones.put(compound.func_74762_e("ClonedTab"), tab = new HashMap());
                    }

                    String name = compound.func_74779_i("ClonedName");

                    for(int number = 1; ((Map)tab).containsKey(name); name = String.format("%s%s", compound.func_74779_i("ClonedName"), number)) {
                         ++number;
                    }

                    compound.func_82580_o("ClonedName");
                    compound.func_82580_o("ClonedTab");
                    compound.func_82580_o("ClonedDate");
                    this.cleanTags(compound);
                    ((Map)tab).put(name, compound);
               }

               return clones;
          }
     }

     public NBTTagCompound getCloneData(ICommandSender player, String name, int tab) {
          File file = new File(new File(this.getDir(), tab + ""), name + ".json");
          if (!file.exists()) {
               if (player != null) {
                    player.func_145747_a(new TextComponentString("Could not find clone file"));
               }

               return null;
          } else {
               try {
                    return NBTJsonUtil.LoadFile(file);
               } catch (Exception var6) {
                    LogWriter.error("Error loading: " + file.getAbsolutePath(), var6);
                    if (player != null) {
                         player.func_145747_a(new TextComponentString(var6.getMessage()));
                    }

                    return null;
               }
          }
     }

     public void saveClone(int tab, String name, NBTTagCompound compound) {
          try {
               File dir = new File(this.getDir(), tab + "");
               if (!dir.exists()) {
                    dir.mkdir();
               }

               String filename = name + ".json";
               File file = new File(dir, filename + "_new");
               File file2 = new File(dir, filename);
               NBTJsonUtil.SaveFile(file, compound);
               if (file2.exists()) {
                    file2.delete();
               }

               file.renameTo(file2);
          } catch (Exception var8) {
               LogWriter.except(var8);
          }

     }

     public List getClones(int tab) {
          List list = new ArrayList();
          File dir = new File(this.getDir(), tab + "");
          if (dir.exists() && dir.isDirectory()) {
               String[] var4 = dir.list();
               int var5 = var4.length;

               for(int var6 = 0; var6 < var5; ++var6) {
                    String file = var4[var6];
                    if (file.endsWith(".json")) {
                         list.add(file.substring(0, file.length() - 5));
                    }
               }

               return list;
          } else {
               return list;
          }
     }

     public boolean removeClone(String name, int tab) {
          File file = new File(new File(this.getDir(), tab + ""), name + ".json");
          if (!file.exists()) {
               return false;
          } else {
               file.delete();
               return true;
          }
     }

     public String addClone(NBTTagCompound nbttagcompound, String name, int tab) {
          this.cleanTags(nbttagcompound);
          this.saveClone(tab, name, nbttagcompound);
          return name;
     }

     public void cleanTags(NBTTagCompound nbttagcompound) {
          if (nbttagcompound.func_74764_b("ItemGiverId")) {
               nbttagcompound.func_74768_a("ItemGiverId", 0);
          }

          if (nbttagcompound.func_74764_b("TransporterId")) {
               nbttagcompound.func_74768_a("TransporterId", -1);
          }

          nbttagcompound.func_82580_o("StartPosNew");
          nbttagcompound.func_82580_o("StartPos");
          nbttagcompound.func_82580_o("MovingPathNew");
          nbttagcompound.func_82580_o("Pos");
          nbttagcompound.func_82580_o("Riding");
          nbttagcompound.func_82580_o("UUID");
          nbttagcompound.func_82580_o("UUIDMost");
          nbttagcompound.func_82580_o("UUIDLeast");
          if (!nbttagcompound.func_74764_b("ModRev")) {
               nbttagcompound.func_74768_a("ModRev", 1);
          }

          NBTTagCompound adv;
          if (nbttagcompound.func_74764_b("TransformRole")) {
               adv = nbttagcompound.func_74775_l("TransformRole");
               adv.func_74768_a("TransporterId", -1);
               nbttagcompound.func_74782_a("TransformRole", adv);
          }

          if (nbttagcompound.func_74764_b("TransformJob")) {
               adv = nbttagcompound.func_74775_l("TransformJob");
               adv.func_74768_a("ItemGiverId", 0);
               nbttagcompound.func_74782_a("TransformJob", adv);
          }

          if (nbttagcompound.func_74764_b("TransformAI")) {
               adv = nbttagcompound.func_74775_l("TransformAI");
               adv.func_82580_o("StartPosNew");
               adv.func_82580_o("StartPos");
               adv.func_82580_o("MovingPathNew");
               nbttagcompound.func_74782_a("TransformAI", adv);
          }

          if (nbttagcompound.func_74764_b("id")) {
               String id = nbttagcompound.func_74779_i("id");
               id = id.replace("customnpcs.", "customnpcs:");
               nbttagcompound.func_74778_a("id", id);
          }

     }

     public IEntity spawn(double x, double y, double z, int tab, String name, IWorld world) {
          NBTTagCompound compound = this.getCloneData((ICommandSender)null, name, tab);
          if (compound == null) {
               throw new CustomNPCsException("Unknown clone tab:" + tab + " name:" + name, new Object[0]);
          } else {
               Entity entity = NoppesUtilServer.spawnClone(compound, x, y, z, world.getMCWorld());
               return entity == null ? null : NpcAPI.Instance().getIEntity(entity);
          }
     }

     public IEntity get(int tab, String name, IWorld world) {
          NBTTagCompound compound = this.getCloneData((ICommandSender)null, name, tab);
          if (compound == null) {
               throw new CustomNPCsException("Unknown clone tab:" + tab + " name:" + name, new Object[0]);
          } else {
               Instance.cleanTags(compound);
               Entity entity = EntityList.func_75615_a(compound, world.getMCWorld());
               return entity == null ? null : NpcAPI.Instance().getIEntity(entity);
          }
     }

     public void set(int tab, String name, IEntity entity) {
          NBTTagCompound compound = new NBTTagCompound();
          if (!entity.getMCEntity().func_184198_c(compound)) {
               throw new CustomNPCsException("Cannot save dead entities", new Object[0]);
          } else {
               this.cleanTags(compound);
               this.saveClone(tab, name, compound);
          }
     }

     public void remove(int tab, String name) {
          this.removeClone(name, tab);
     }
}
