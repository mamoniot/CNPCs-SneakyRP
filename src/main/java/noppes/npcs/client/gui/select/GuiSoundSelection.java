package noppes.npcs.client.gui.select;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundRegistry;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.SubGuiInterface;

public class GuiSoundSelection extends SubGuiInterface implements ICustomScrollListener {
	private GuiCustomScroll scrollCategories;
	private GuiCustomScroll scrollQuests;
	private String selectedDomain;
	public ResourceLocation selectedResource;
	private HashMap domains = new HashMap();

	public GuiSoundSelection(String sound) {
		this.drawDefaultBackground = false;
		this.title = "";
		this.setBackground("menubg.png");
		this.xSize = 366;
		this.ySize = 226;
		SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();
		SoundRegistry registry = (SoundRegistry) ObfuscationReflectionHelper.getPrivateValue(SoundHandler.class,
				handler, 4);
		Set set = registry.getKeys();
		Iterator var5 = set.iterator();

		while (var5.hasNext()) {
			ResourceLocation location = (ResourceLocation) var5.next();
			List list = (List) this.domains.get(location.getResourceDomain());
			if (list == null) {
				this.domains.put(location.getResourceDomain(), list = new ArrayList());
			}

			((List) list).add(location.getResourcePath());
			this.domains.put(location.getResourceDomain(), list);
		}

		if (sound != null && !sound.isEmpty()) {
			this.selectedResource = new ResourceLocation(sound);
			this.selectedDomain = this.selectedResource.getResourceDomain();
			if (!this.domains.containsKey(this.selectedDomain)) {
				this.selectedDomain = null;
			}
		}

	}

	public void initGui() {
		super.initGui();
		this.addButton(new GuiNpcButton(2, this.guiLeft + this.xSize - 26, this.guiTop + 4, 20, 20, "X"));
		this.addButton(new GuiNpcButton(1, this.guiLeft + 160, this.guiTop + 212, 70, 20, "gui.play",
				this.selectedResource != null));
		if (this.scrollCategories == null) {
			this.scrollCategories = new GuiCustomScroll(this, 0);
			this.scrollCategories.setSize(90, 200);
		}

		this.scrollCategories.setList(Lists.newArrayList(this.domains.keySet()));
		if (this.selectedDomain != null) {
			this.scrollCategories.setSelected(this.selectedDomain);
		}

		this.scrollCategories.guiLeft = this.guiLeft + 4;
		this.scrollCategories.guiTop = this.guiTop + 14;
		this.addScroll(this.scrollCategories);
		if (this.scrollQuests == null) {
			this.scrollQuests = new GuiCustomScroll(this, 1);
			this.scrollQuests.setSize(250, 200);
		}

		if (this.selectedDomain != null) {
			this.scrollQuests.setList((List) this.domains.get(this.selectedDomain));
		}

		if (this.selectedResource != null) {
			this.scrollQuests.setSelected(this.selectedResource.getResourcePath());
		}

		this.scrollQuests.guiLeft = this.guiLeft + 95;
		this.scrollQuests.guiTop = this.guiTop + 14;
		this.addScroll(this.scrollQuests);
	}

	protected void actionPerformed(GuiButton guibutton) {
		super.actionPerformed(guibutton);
		if (guibutton.id == 1) {
			MusicController.Instance.stopMusic();
			BlockPos pos = this.player.getPosition();
			MusicController.Instance.playSound(SoundCategory.NEUTRAL, this.selectedResource.toString(), pos.getX(),
					pos.getY(), pos.getZ(), 1.0F, 1.0F);
		}

		if (guibutton.id == 2) {
			this.close();
		}

	}

	public void scrollClicked(int i, int j, int k, GuiCustomScroll scroll) {
		if (scroll.id == 0) {
			this.selectedDomain = scroll.getSelected();
			this.selectedResource = null;
			this.scrollQuests.selected = -1;
		}

		if (scroll.id == 1) {
			this.selectedResource = new ResourceLocation(this.selectedDomain, scroll.getSelected());
		}

		this.initGui();
	}

	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
		if (this.selectedResource != null) {
			this.close();
		}
	}
}
