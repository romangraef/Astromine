package com.github.chainmailstudios.astromine.client.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import com.github.chainmailstudios.astromine.client.screen.base.DefaultedContainerScreen;
import com.github.chainmailstudios.astromine.common.container.NuclearWarheadContainer;
import com.github.chainmailstudios.astromine.common.container.base.DefaultedBlockStateContainer;

public class NuclearWarheadContainerScreen extends DefaultedContainerScreen<NuclearWarheadContainer> {
	public NuclearWarheadContainerScreen(Text name, DefaultedBlockStateContainer linkedContainer, PlayerEntity player) {
		super(name, linkedContainer, player);
	}
}
