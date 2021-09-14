package noppes.npcs.blocks.tiles;

import com.google.common.base.MoreObjects;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.EventHooks;
import noppes.npcs.NBTTags;
import noppes.npcs.api.block.IBlock;
import noppes.npcs.api.wrapper.BlockScriptedDoorWrapper;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.IScriptBlockHandler;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.entity.data.DataTimers;

public class TileScriptedDoor extends TileDoor implements ITickable, IScriptBlockHandler {
     public List scripts = new ArrayList();
     public boolean shouldRefreshData = false;
     public String scriptLanguage = "ECMAScript";
     public boolean enabled = false;
     private IBlock blockDummy = null;
     public DataTimers timers = new DataTimers(this);
     public long lastInited = -1L;
     private short ticksExisted = 0;
     public int newPower = 0;
     public int prevPower = 0;
     public float blockHardness = 5.0F;
     public float blockResistance = 10.0F;

     public IBlock getBlock() {
          if (this.blockDummy == null) {
               this.blockDummy = new BlockScriptedDoorWrapper(this.getWorld(), this.func_145838_q(), this.getPos());
          }

          return this.blockDummy;
     }

     public void readFromNBT(NBTTagCompound compound) {
          super.readFromNBT(compound);
          this.setNBT(compound);
          this.timers.readFromNBT(compound);
     }

     public void setNBT(NBTTagCompound compound) {
          this.scripts = NBTTags.GetScript(compound.getTagList("Scripts", 10), this);
          this.scriptLanguage = compound.getString("ScriptLanguage");
          this.enabled = compound.getBoolean("ScriptEnabled");
          this.prevPower = compound.getInteger("BlockPrevPower");
          if (compound.hasKey("BlockHardness")) {
               this.blockHardness = compound.getFloat("BlockHardness");
               this.blockResistance = compound.getFloat("BlockResistance");
          }

     }

     public NBTTagCompound writeToNBT(NBTTagCompound compound) {
          this.getNBT(compound);
          this.timers.writeToNBT(compound);
          return super.writeToNBT(compound);
     }

     public NBTTagCompound getNBT(NBTTagCompound compound) {
          compound.setTag("Scripts", NBTTags.NBTScript(this.scripts));
          compound.setString("ScriptLanguage", this.scriptLanguage);
          compound.setBoolean("ScriptEnabled", this.enabled);
          compound.setInteger("BlockPrevPower", this.prevPower);
          compound.setFloat("BlockHardness", this.blockHardness);
          compound.setFloat("BlockResistance", this.blockResistance);
          return compound;
     }

     public void runScript(EnumScriptType type, Event event) {
          if (this.isEnabled()) {
               if (ScriptController.Instance.lastLoaded > this.lastInited) {
                    this.lastInited = ScriptController.Instance.lastLoaded;
                    if (type != EnumScriptType.INIT) {
                         EventHooks.onScriptBlockInit(this);
                    }
               }

               Iterator var3 = this.scripts.iterator();

               while(var3.hasNext()) {
                    ScriptContainer script = (ScriptContainer)var3.next();
                    script.run(type, event);
               }

          }
     }

     private boolean isEnabled() {
          return this.enabled && ScriptController.HasStart && !this.field_145850_b.isRemote;
     }

     public void func_73660_a() {
          super.func_73660_a();
          ++this.ticksExisted;
          if (this.prevPower != this.newPower) {
               EventHooks.onScriptBlockRedstonePower(this, this.prevPower, this.newPower);
               this.prevPower = this.newPower;
          }

          this.timers.update();
          if (this.ticksExisted >= 10) {
               EventHooks.onScriptBlockUpdate(this);
               this.ticksExisted = 0;
          }

     }

     public boolean isClient() {
          return this.getWorld().isRemote;
     }

     public boolean getEnabled() {
          return this.enabled;
     }

     public void setEnabled(boolean bo) {
          this.enabled = bo;
     }

     public String getLanguage() {
          return this.scriptLanguage;
     }

     public void setLanguage(String lang) {
          this.scriptLanguage = lang;
     }

     public List getScripts() {
          return this.scripts;
     }

     public String noticeString() {
          BlockPos pos = this.getPos();
          return MoreObjects.toStringHelper(this).add("x", pos.getX()).add("y", pos.getY()).add("z", pos.getZ()).toString();
     }

     public Map getConsoleText() {
          Map map = new TreeMap();
          int tab = 0;
          Iterator var3 = this.getScripts().iterator();

          while(var3.hasNext()) {
               ScriptContainer script = (ScriptContainer)var3.next();
               ++tab;
               Iterator var5 = script.console.entrySet().iterator();

               while(var5.hasNext()) {
                    Entry entry = (Entry)var5.next();
                    map.put(entry.getKey(), " tab " + tab + ":\n" + (String)entry.getValue());
               }
          }

          return map;
     }

     public void clearConsole() {
          Iterator var1 = this.getScripts().iterator();

          while(var1.hasNext()) {
               ScriptContainer script = (ScriptContainer)var1.next();
               script.console.clear();
          }

     }
}
