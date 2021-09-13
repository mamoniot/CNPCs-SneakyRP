package noppes.npcs.api.wrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import noppes.npcs.Server;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.IDimension;
import noppes.npcs.api.INbt;
import noppes.npcs.api.IPos;
import noppes.npcs.api.IScoreboard;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.block.IBlock;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.entity.data.IData;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.controllers.PixelmonHelper;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.EntityProjectile;

public class WorldWrapper implements IWorld {
     public static Map tempData = new HashMap();
     public WorldServer world;
     public IDimension dimension;
     private IData tempdata = new IData() {
          public void put(String key, Object value) {
               WorldWrapper.tempData.put(key, value);
          }

          public Object get(String key) {
               return WorldWrapper.tempData.get(key);
          }

          public void remove(String key) {
               WorldWrapper.tempData.remove(key);
          }

          public boolean has(String key) {
               return WorldWrapper.tempData.containsKey(key);
          }

          public void clear() {
               WorldWrapper.tempData.clear();
          }

          public String[] getKeys() {
               return (String[])WorldWrapper.tempData.keySet().toArray(new String[WorldWrapper.tempData.size()]);
          }
     };
     private IData storeddata = new IData() {
          public void put(String key, Object value) {
               NBTTagCompound compound = ScriptController.Instance.compound;
               if (value instanceof Number) {
                    compound.func_74780_a(key, ((Number)value).doubleValue());
               } else if (value instanceof String) {
                    compound.setString(key, (String)value);
               }

               ScriptController.Instance.shouldSave = true;
          }

          public Object get(String key) {
               NBTTagCompound compound = ScriptController.Instance.compound;
               if (!compound.hasKey(key)) {
                    return null;
               } else {
                    NBTBase base = compound.getTag(key);
                    return base instanceof NBTPrimitive ? ((NBTPrimitive)base).func_150286_g() : ((NBTTagString)base).func_150285_a_();
               }
          }

          public void remove(String key) {
               ScriptController.Instance.compound.func_82580_o(key);
               ScriptController.Instance.shouldSave = true;
          }

          public boolean has(String key) {
               return ScriptController.Instance.compound.hasKey(key);
          }

          public void clear() {
               ScriptController.Instance.compound = new NBTTagCompound();
               ScriptController.Instance.shouldSave = true;
          }

          public String[] getKeys() {
               return (String[])ScriptController.Instance.compound.getKeySet().toArray(new String[ScriptController.Instance.compound.getKeySet().size()]);
          }
     };

     private WorldWrapper(World world) {
          this.world = (WorldServer)world;
          this.dimension = new DimensionWrapper(world.field_73011_w.getDimension(), world.field_73011_w.func_186058_p());
     }

     public WorldServer getMCWorld() {
          return this.world;
     }

     public IEntity[] getNearbyEntities(int x, int y, int z, int range, int type) {
          return this.getNearbyEntities(new BlockPosWrapper(new BlockPos(x, y, z)), range, type);
     }

     public IEntity[] getNearbyEntities(IPos pos, int range, int type) {
          AxisAlignedBB bb = (new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D)).func_186670_a(pos.getMCBlockPos()).expand((double)range, (double)range, (double)range);
          List entities = this.world.getEntitiesWithinAABB(this.getClassForType(type), bb);
          List list = new ArrayList();
          Iterator var7 = entities.iterator();

          while(var7.hasNext()) {
               Entity living = (Entity)var7.next();
               list.add(NpcAPI.Instance().getIEntity(living));
          }

          return (IEntity[])list.toArray(new IEntity[list.size()]);
     }

     public IEntity[] getAllEntities(int type) {
          List entities = this.world.func_175644_a(this.getClassForType(type), EntitySelectors.field_180132_d);
          List list = new ArrayList();
          Iterator var4 = entities.iterator();

          while(var4.hasNext()) {
               Entity living = (Entity)var4.next();
               list.add(NpcAPI.Instance().getIEntity(living));
          }

          return (IEntity[])list.toArray(new IEntity[list.size()]);
     }

     public IEntity getClosestEntity(int x, int y, int z, int range, int type) {
          return this.getClosestEntity(new BlockPosWrapper(new BlockPos(x, y, z)), range, type);
     }

     public IEntity getClosestEntity(IPos pos, int range, int type) {
          AxisAlignedBB bb = (new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D)).func_186670_a(pos.getMCBlockPos()).expand((double)range, (double)range, (double)range);
          List entities = this.world.getEntitiesWithinAABB(this.getClassForType(type), bb);
          double distance = (double)(range * range * range);
          Entity entity = null;
          Iterator var9 = entities.iterator();

          while(var9.hasNext()) {
               Entity e = (Entity)var9.next();
               double r = pos.getMCBlockPos().func_177951_i(e.func_180425_c());
               if (entity == null) {
                    distance = r;
                    entity = e;
               } else if (r < distance) {
                    distance = r;
                    entity = e;
               }
          }

          return NpcAPI.Instance().getIEntity(entity);
     }

     public IEntity getEntity(String uuid) {
          try {
               UUID id = UUID.fromString(uuid);
               Entity e = this.world.func_175733_a(id);
               if (e == null) {
                    e = this.world.func_152378_a(id);
               }

               return e == null ? null : NpcAPI.Instance().getIEntity((Entity)e);
          } catch (Exception var4) {
               throw new CustomNPCsException("Given uuid was invalid " + uuid, new Object[0]);
          }
     }

     public IEntity createEntityFromNBT(INbt nbt) {
          Entity entity = EntityList.func_75615_a(nbt.getMCNBT(), this.world);
          if (entity == null) {
               throw new CustomNPCsException("Failed to create an entity from given NBT", new Object[0]);
          } else {
               return NpcAPI.Instance().getIEntity(entity);
          }
     }

     public IEntity createEntity(String id) {
          ResourceLocation resource = new ResourceLocation(id);
          Entity entity = EntityList.func_188429_b(resource, this.world);
          if (entity == null) {
               throw new CustomNPCsException("Failed to create an entity from given id: " + id, new Object[0]);
          } else {
               return NpcAPI.Instance().getIEntity(entity);
          }
     }

     public IPlayer getPlayer(String name) {
          EntityPlayer player = this.world.func_72924_a(name);
          return player == null ? null : (IPlayer)NpcAPI.Instance().getIEntity(player);
     }

     private Class getClassForType(int type) {
          if (type == -1) {
               return Entity.class;
          } else if (type == 5) {
               return EntityLivingBase.class;
          } else if (type == 1) {
               return EntityPlayer.class;
          } else if (type == 4) {
               return EntityAnimal.class;
          } else if (type == 3) {
               return EntityMob.class;
          } else if (type == 2) {
               return EntityNPCInterface.class;
          } else if (type == 6) {
               return EntityItem.class;
          } else if (type == 7) {
               return EntityProjectile.class;
          } else if (type == 11) {
               return EntityThrowable.class;
          } else if (type == 10) {
               return EntityArrow.class;
          } else if (type == 3) {
               return EntityMob.class;
          } else if (type == 8) {
               return PixelmonHelper.getPixelmonClass();
          } else {
               return type == 9 ? EntityVillager.class : Entity.class;
          }
     }

     public long getTime() {
          return this.world.func_72820_D();
     }

     public void setTime(long time) {
          this.world.func_72877_b(time);
     }

     public long getTotalTime() {
          return this.world.func_82737_E();
     }

     public IBlock getBlock(int x, int y, int z) {
          return NpcAPI.Instance().getIBlock(this.world, new BlockPos(x, y, z));
     }

     public boolean isChunkLoaded(int x, int z) {
          return this.world.func_72863_F().func_73149_a(x >> 4, z >> 4);
     }

     public void setBlock(int x, int y, int z, String name, int meta) {
          Block block = Block.func_149684_b(name);
          if (block == null) {
               throw new CustomNPCsException("There is no such block: %s", new Object[0]);
          } else {
               this.world.func_175656_a(new BlockPos(x, y, z), block.func_176203_a(meta));
          }
     }

     public void removeBlock(int x, int y, int z) {
          this.world.func_175698_g(new BlockPos(x, y, z));
     }

     public float getLightValue(int x, int y, int z) {
          return (float)this.world.func_175699_k(new BlockPos(x, y, z)) / 16.0F;
     }

     public IBlock getSpawnPoint() {
          BlockPos pos = this.world.func_180504_m();
          if (pos == null) {
               pos = this.world.func_175694_M();
          }

          return NpcAPI.Instance().getIBlock(this.world, pos);
     }

     public void setSpawnPoint(IBlock block) {
          this.world.func_175652_B(new BlockPos(block.getX(), block.getY(), block.getZ()));
     }

     public boolean isDay() {
          return this.world.func_72820_D() % 24000L < 12000L;
     }

     public boolean isRaining() {
          return this.world.func_72912_H().func_76059_o();
     }

     public void setRaining(boolean bo) {
          this.world.func_72912_H().func_76084_b(bo);
     }

     public void thunderStrike(double x, double y, double z) {
          this.world.func_72942_c(new EntityLightningBolt(this.world, x, y, z, false));
     }

     public void spawnParticle(String particle, double x, double y, double z, double dx, double dy, double dz, double speed, int count) {
          EnumParticleTypes particleType = null;
          EnumParticleTypes[] var18 = EnumParticleTypes.values();
          int var19 = var18.length;

          for(int var20 = 0; var20 < var19; ++var20) {
               EnumParticleTypes enumParticle = var18[var20];
               if (enumParticle.func_179345_d() > 0) {
                    if (particle.startsWith(enumParticle.func_179346_b())) {
                         particleType = enumParticle;
                         break;
                    }
               } else if (particle.equals(enumParticle.func_179346_b())) {
                    particleType = enumParticle;
                    break;
               }
          }

          if (particleType != null) {
               this.world.func_175739_a(particleType, x, y, z, count, dx, dy, dz, speed, new int[0]);
          }

     }

     public IData getTempdata() {
          return this.tempdata;
     }

     public IData getStoreddata() {
          return this.storeddata;
     }

     public IItemStack createItem(String name, int damage, int size) {
          Item item = (Item)Item.field_150901_e.getObject(new ResourceLocation(name));
          if (item == null) {
               throw new CustomNPCsException("Unknown item id: " + name, new Object[0]);
          } else {
               return NpcAPI.Instance().getIItemStack(new ItemStack(item, size, damage));
          }
     }

     public IItemStack createItemFromNbt(INbt nbt) {
          ItemStack item = new ItemStack(nbt.getMCNBT());
          if (item.isEmpty()) {
               throw new CustomNPCsException("Failed to create an item from given NBT", new Object[0]);
          } else {
               return NpcAPI.Instance().getIItemStack(item);
          }
     }

     public void explode(double x, double y, double z, float range, boolean fire, boolean grief) {
          this.world.func_72885_a((Entity)null, x, y, z, range, fire, grief);
     }

     public IPlayer[] getAllPlayers() {
          List list = this.world.func_73046_m().getPlayerList().getPlayers();
          IPlayer[] arr = new IPlayer[list.size()];

          for(int i = 0; i < list.size(); ++i) {
               arr[i] = (IPlayer)NpcAPI.Instance().getIEntity((Entity)list.get(i));
          }

          return arr;
     }

     public String getBiomeName(int x, int z) {
          return this.world.getBiomeForCoordsBody(new BlockPos(x, 0, z)).field_76791_y;
     }

     public IEntity spawnClone(double x, double y, double z, int tab, String name) {
          return NpcAPI.Instance().getClones().spawn(x, y, z, tab, name, this);
     }

     public void spawnEntity(IEntity entity) {
          Entity e = entity.getMCEntity();
          if (this.world.func_175733_a(e.func_110124_au()) != null) {
               throw new CustomNPCsException("Entity with this UUID already exists", new Object[0]);
          } else {
               e.func_70107_b(e.field_70165_t, e.field_70163_u, e.field_70161_v);
               this.world.func_72838_d(e);
          }
     }

     public IEntity getClone(int tab, String name) {
          return NpcAPI.Instance().getClones().get(tab, name, this);
     }

     public IScoreboard getScoreboard() {
          return new ScoreboardWrapper(this.world.func_73046_m());
     }

     public void broadcast(String message) {
          this.world.func_73046_m().getPlayerList().func_148539_a(new TextComponentString(message));
     }

     public int getRedstonePower(int x, int y, int z) {
          return this.world.func_175676_y(new BlockPos(x, y, z));
     }

     /** @deprecated */
     @Deprecated
     public static WorldWrapper createNew(WorldServer world) {
          return new WorldWrapper(world);
     }

     public IDimension getDimension() {
          return this.dimension;
     }

     public String getName() {
          return this.world.func_72912_H().func_76065_j();
     }

     public BlockPos getMCBlockPos(int x, int y, int z) {
          return new BlockPos(x, y, z);
     }

     public void playSoundAt(IPos pos, String sound, float volume, float pitch) {
          Server.sendRangedData(this.world, pos.getMCBlockPos(), 16, EnumPacketClient.PLAY_SOUND, sound, pos.getX(), pos.getY(), pos.getZ(), volume, pitch);
     }
}
