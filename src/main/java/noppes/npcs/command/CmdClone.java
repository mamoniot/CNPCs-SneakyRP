package noppes.npcs.command;

import java.util.Iterator;
import java.util.List;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import noppes.npcs.api.CommandNoppesBase;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.entity.EntityNPCInterface;

public class CmdClone extends CommandNoppesBase {
	public String getName() {
		return "clone";
	}

	public String getDescription() {
		return "Clone operation (server side)";
	}

	@CommandNoppesBase.SubCommand(desc = "Add NPC(s) to clone storage", usage = "<npc> <tab> [clonedname]", permission = 4)
	public void add(MinecraftServer server, ICommandSender sender, String[] args) {
		int tab = 0;

		try {
			tab = Integer.parseInt(args[1]);
		} catch (NumberFormatException var10) {
		}

		List list = this.getEntities(EntityNPCInterface.class, sender.getEntityWorld(), sender.getPosition(), 80);
		Iterator var6 = list.iterator();

		while (var6.hasNext()) {
			EntityNPCInterface npc = (EntityNPCInterface) var6.next();
			if (npc.display.getName().equalsIgnoreCase(args[0])) {
				String name = npc.display.getName();
				if (args.length > 2) {
					name = args[2];
				}

				NBTTagCompound compound = new NBTTagCompound();
				if (!npc.writeToNBTAtomically(compound)) {
					return;
				}

				ServerCloneController.Instance.addClone(compound, name, tab);
			}
		}

	}

	@CommandNoppesBase.SubCommand(desc = "List NPC from clone storage", usage = "<tab>", permission = 2)
	public void list(MinecraftServer server, ICommandSender sender, String[] args) {
		this.sendMessage(sender, "--- Stored NPCs --- (server side)", new Object[0]);
		int tab = 0;

		try {
			tab = Integer.parseInt(args[0]);
		} catch (NumberFormatException var7) {
		}

		Iterator var5 = ServerCloneController.Instance.getClones(tab).iterator();

		while (var5.hasNext()) {
			String name = (String) var5.next();
			this.sendMessage(sender, name, new Object[0]);
		}

		this.sendMessage(sender, "------------------------------------", new Object[0]);
	}

	@CommandNoppesBase.SubCommand(desc = "Remove NPC from clone storage", usage = "<name> <tab>", permission = 4)
	public void del(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		String nametodel = args[0];
		int tab = 0;

		try {
			tab = Integer.parseInt(args[1]);
		} catch (NumberFormatException var9) {
		}

		boolean deleted = false;
		Iterator var7 = ServerCloneController.Instance.getClones(tab).iterator();

		while (var7.hasNext()) {
			String name = (String) var7.next();
			if (nametodel.equalsIgnoreCase(name)) {
				ServerCloneController.Instance.removeClone(name, tab);
				deleted = true;
				break;
			}
		}

		if (!ServerCloneController.Instance.removeClone(nametodel, tab)) {
			throw new CommandException("Npc '%s' wasn't found", new Object[] { nametodel });
		}
	}

	@CommandNoppesBase.SubCommand(desc = "Spawn cloned NPC", usage = "<name> <tab> [[world:]x,y,z]] [newname]", permission = 2)
	public void spawn(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		String name = args[0].replaceAll("%", " ");
		int tab = 0;

		try {
			tab = Integer.parseInt(args[1]);
		} catch (NumberFormatException var14) {
		}

		String newname = null;
		NBTTagCompound compound = ServerCloneController.Instance.getCloneData(sender, name, tab);
		if (compound == null) {
			throw new CommandException("Unknown npc", new Object[0]);
		} else {
			World world = sender.getEntityWorld();
			BlockPos pos = sender.getPosition();
			if (args.length > 2) {
				String location = args[2];
				String[] par;
				if (location.contains(":")) {
					par = location.split(":");
					location = par[1];
					world = this.getWorld(server, par[0]);
					if (world == null) {
						throw new CommandException("'%s' is an unknown world", new Object[] { par[0] });
					}
				}

				if (location.contains(",")) {
					par = location.split(",");
					if (par.length != 3) {
						throw new CommandException("Location need be x,y,z", new Object[0]);
					}

					try {
						pos = CommandBase.parseBlockPos(sender, par, 0, false);
					} catch (NumberInvalidException var13) {
						throw new CommandException("Location should be in numbers", new Object[0]);
					}

					if (args.length > 3) {
						newname = args[3];
					}
				} else {
					newname = location;
				}
			}

			if (pos.getX() == 0 && pos.getY() == 0 && pos.getZ() == 0) {
				throw new CommandException("Location needed", new Object[0]);
			} else {
				Entity entity = EntityList.createEntityFromNBT(compound, world);
				entity.setPosition((double) pos.getX() + 0.5D, (double) (pos.getY() + 1), (double) pos.getZ() + 0.5D);
				if (entity instanceof EntityNPCInterface) {
					EntityNPCInterface npc = (EntityNPCInterface) entity;
					npc.ais.setStartPos(pos);
					if (newname != null && !newname.isEmpty()) {
						npc.display.setName(newname.replaceAll("%", " "));
					}
				}

				world.spawnEntity(entity);
			}
		}
	}

	@CommandNoppesBase.SubCommand(desc = "Spawn multiple cloned NPC in a grid", usage = "<name> <tab> <lenght> <width> [[world:]x,y,z]] [newname]", permission = 2)
	public boolean grid(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		String name = args[0].replaceAll("%", " ");
		int tab = 0;

		try {
			tab = Integer.parseInt(args[1]);
		} catch (NumberFormatException var22) {
		}

		int width;
		int height;
		try {
			width = Integer.parseInt(args[2]);
			height = Integer.parseInt(args[3]);
		} catch (NumberFormatException var21) {
			throw new CommandException("lenght or width wasnt a number", new Object[0]);
		}

		String newname = null;
		NBTTagCompound compound = ServerCloneController.Instance.getCloneData(sender, name, tab);
		if (compound == null) {
			throw new CommandException("Unknown npc", new Object[0]);
		} else {
			World world = sender.getEntityWorld();
			BlockPos curpos = sender.getPosition();
			if (args.length > 4) {
				String location = args[4];
				String[] par;
				if (location.contains(":")) {
					par = location.split(":");
					location = par[1];
					world = this.getWorld(server, par[0]);
					if (world == null) {
						throw new CommandException("'%s' is an unknown world", new Object[] { par[0] });
					}
				}

				if (location.contains(",")) {
					par = location.split(",");
					if (par.length != 3) {
						throw new CommandException("Location need be x,y,z", new Object[0]);
					}

					try {
						curpos = CommandBase.parseBlockPos(sender, par, 0, false);
					} catch (NumberInvalidException var20) {
						throw new CommandException("Location should be in numbers", new Object[0]);
					}

					if (args.length > 5) {
						newname = args[5];
					}
				} else {
					newname = location;
				}
			}

			if (curpos.getX() == 0 && curpos.getY() == 0 && curpos.getZ() == 0) {
				throw new CommandException("Location needed", new Object[0]);
			} else {
				for (int x = 0; x < width; ++x) {
					for (int z = 0; z < height; ++z) {
						BlockPos npcpos = curpos.add(x, -2, z);

						for (int y = 0; y < 10; ++y) {
							BlockPos pos = npcpos.up(y);
							BlockPos pos2 = pos.up();
							IBlockState b = world.getBlockState(pos);
							IBlockState b2 = world.getBlockState(pos2);
							if (b.causesSuffocation() && !b2.causesSuffocation()) {
								npcpos = pos;
								break;
							}
						}

						Entity entity = EntityList.createEntityFromNBT(compound, world);
						entity.setPosition((double) npcpos.getX() + 0.5D, (double) (npcpos.getY() + 1),
								(double) npcpos.getZ() + 0.5D);
						if (entity instanceof EntityNPCInterface) {
							EntityNPCInterface npc = (EntityNPCInterface) entity;
							npc.ais.setStartPos(npcpos);
							if (newname != null && !newname.isEmpty()) {
								npc.display.setName(newname.replaceAll("%", " "));
							}
						}

						world.spawnEntity(entity);
					}
				}

				return true;
			}
		}
	}

	public World getWorld(MinecraftServer server, String t) {
		WorldServer[] ws = server.worlds;
		WorldServer[] var4 = ws;
		int var5 = ws.length;

		for (int var6 = 0; var6 < var5; ++var6) {
			WorldServer w = var4[var6];
			if (w != null && (w.provider.getDimension() + "").equalsIgnoreCase(t)) {
				return w;
			}
		}

		return null;
	}

	public List getEntities(Class cls, World world, BlockPos pos, int range) {
		return world.getEntitiesWithinAABB(cls,
				(new AxisAlignedBB(pos, pos.add(1, 1, 1))).grow((double) range, (double) range, (double) range));
	}
}
