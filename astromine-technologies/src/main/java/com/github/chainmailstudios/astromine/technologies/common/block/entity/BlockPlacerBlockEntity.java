/*
 * MIT License
 *
 * Copyright (c) 2020 Chainmail Studios
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.chainmailstudios.astromine.technologies.common.block.entity;

import com.github.chainmailstudios.astromine.common.block.entity.base.ComponentEnergyInventoryBlockEntity;
import com.github.chainmailstudios.astromine.common.component.inventory.EnergyInventoryComponent;
import com.github.chainmailstudios.astromine.common.component.inventory.ItemInventoryComponent;
import com.github.chainmailstudios.astromine.common.component.inventory.SimpleEnergyInventoryComponent;
import com.github.chainmailstudios.astromine.common.component.inventory.SimpleItemInventoryComponent;
import com.github.chainmailstudios.astromine.common.volume.energy.EnergyVolume;
import com.github.chainmailstudios.astromine.common.volume.fraction.Fraction;
import com.github.chainmailstudios.astromine.common.volume.handler.ItemHandler;
import com.github.chainmailstudios.astromine.registry.AstromineConfig;
import com.github.chainmailstudios.astromine.technologies.common.block.entity.machine.EnergyConsumedProvider;
import com.github.chainmailstudios.astromine.technologies.common.block.entity.machine.EnergySizeProvider;
import com.github.chainmailstudios.astromine.technologies.common.block.entity.machine.SpeedProvider;
import com.github.chainmailstudios.astromine.technologies.registry.AstromineTechnologiesBlockEntityTypes;
import com.github.chainmailstudios.astromine.technologies.registry.AstromineTechnologiesBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

public class BlockPlacerBlockEntity extends ComponentEnergyInventoryBlockEntity implements EnergySizeProvider, SpeedProvider, EnergyConsumedProvider {
	private Fraction cooldown = Fraction.empty();

	public BlockPlacerBlockEntity() {
		super(AstromineTechnologiesBlocks.BLOCK_PLACER, AstromineTechnologiesBlockEntityTypes.BLOCK_PLACER);
	}

	@Override
	protected ItemInventoryComponent createItemComponent() {
		return new SimpleItemInventoryComponent(1);
	}

	@Override
	protected EnergyInventoryComponent createEnergyComponent() {
		return new SimpleEnergyInventoryComponent(getEnergySize());
	}

	@Override
	public double getEnergySize() {
		return AstromineConfig.get().blockPlacerEnergy;
	}

	@Override
	public double getEnergyConsumed() {
		return AstromineConfig.get().blockPlacerEnergyConsumed;
	}

	@Override
	public double getMachineSpeed() {
		return AstromineConfig.get().blockPlacerSpeed;
	}

	@Override
	public void tick() {
		super.tick();

		if (world == null) return;
		if (world.isClient) return;

		ItemHandler.ofOptional(this).ifPresent(items -> {
			EnergyVolume energyVolume = getEnergyComponent().getVolume();
			if (energyVolume.getAmount() < getEnergyConsumed()) {
				cooldown = Fraction.empty();

				tickInactive();
			} else {
				tickActive();

				cooldown = cooldown.add(Fraction.ofDecimal(1.0D / getMachineSpeed()));

				cooldown.ifBiggerOrEqualThan(Fraction.of(1), () -> {
					cooldown = Fraction.empty();

					ItemStack stored = items.getFirst();

					Direction direction = getCachedState().get(HorizontalFacingBlock.FACING);

					BlockPos targetPos = pos.offset(direction);

					BlockState targetState = world.getBlockState(targetPos);

					if (stored.getItem() instanceof BlockItem && targetState.isAir()) {
						BlockState newState = ((BlockItem) stored.getItem()).getBlock().getDefaultState();

						world.setBlockState(targetPos, newState);

						stored.decrement(1);

						energyVolume.minus(getEnergyConsumed());
					}
				});
			}
		});
	}

	@Override
	public CompoundTag toTag(CompoundTag tag) {
		tag.put("cooldown", cooldown.toTag());
		return super.toTag(tag);
	}

	@Override
	public void fromTag(BlockState state, @NotNull CompoundTag tag) {
		cooldown = Fraction.fromTag(tag.getCompound("cooldown"));
		super.fromTag(state, tag);
	}
}
