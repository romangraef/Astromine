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
import com.github.chainmailstudios.astromine.common.component.inventory.SimpleEnergyInventoryComponent;
import com.github.chainmailstudios.astromine.common.component.inventory.SimpleItemInventoryComponent;
import com.github.chainmailstudios.astromine.common.utilities.StackUtilities;
import com.github.chainmailstudios.astromine.common.volume.energy.EnergyVolume;
import com.github.chainmailstudios.astromine.common.volume.handler.ItemHandler;
import com.github.chainmailstudios.astromine.registry.AstromineConfig;
import com.github.chainmailstudios.astromine.technologies.common.block.entity.machine.EnergyConsumedProvider;
import com.github.chainmailstudios.astromine.technologies.common.block.entity.machine.EnergySizeProvider;
import com.github.chainmailstudios.astromine.technologies.common.block.entity.machine.SpeedProvider;
import com.github.chainmailstudios.astromine.technologies.registry.AstromineTechnologiesBlockEntityTypes;
import com.github.chainmailstudios.astromine.technologies.registry.AstromineTechnologiesBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class BlockBreakerBlockEntity extends ComponentEnergyInventoryBlockEntity implements EnergySizeProvider, SpeedProvider, EnergyConsumedProvider {
	private int cooldown = 0;

	public BlockBreakerBlockEntity() {
		super(AstromineTechnologiesBlocks.BLOCK_BREAKER.get(), AstromineTechnologiesBlockEntityTypes.BLOCK_BREAKER);
	}

	@Override
	protected IItemHandler createItemComponent() {
		return new SimpleItemInventoryComponent(1);
	}

	@Override
	protected EnergyInventoryComponent createEnergyComponent() {
		return new SimpleEnergyInventoryComponent(getEnergySize());
	}

	@Override
	public double getEnergySize() {
		return AstromineConfig.get().blockBreakerEnergy;
	}

	@Override
	public int getEnergyConsumed() {
		return AstromineConfig.get().blockBreakerEnergyConsumed;
	}

	@Override
	public double getMachineSpeed() {
		return AstromineConfig.get().blockBreakerSpeed;
	}

	@Override
	public void tick() {
		super.tick();

		if (level == null) return;
		if (level.isClientSide) return;

		ItemHandler.ofOptional(this).ifPresent(items -> {
			EnergyVolume energyVolume = getEnergyComponent().getVolume();
			if (energyVolume.getAmount() < getEnergyConsumed()) {
				cooldown = 0;

				tickInactive();
			} else {
				tickActive();

				cooldown++;

				if (cooldown > getMachineSpeed()) {
					cooldown = 0;

					ItemStack stored = items.getFirst();

					Direction direction = getBlockState().getValue(HorizontalBlock.FACING);

					BlockPos targetPos = getBlockPos().relative(direction);

					BlockState targetState = level.getBlockState(targetPos);

					if (targetState.isAir()) {
						tickInactive();
					} else {
						TileEntity targetEntity = level.getBlockEntity(targetPos);

						List<ItemStack> drops = Block.getDrops(targetState, (ServerWorld) level, targetPos, targetEntity);

						ItemStack storedCopy = stored.copy();

						Optional<ItemStack> matching = drops.stream().filter(stack -> storedCopy.isEmpty() || StackUtilities.equalItemAndTag(stack, storedCopy)).findFirst();

						matching.ifPresent(match -> {
							Tuple<ItemStack, ItemStack> pair = StackUtilities.merge(match, stored, match.getMaxStackSize(), stored.getMaxStackSize());
							items.setFirst(pair.getB());
							drops.remove(match);
							drops.add(pair.getA());
						});

						drops.forEach(stack -> {
							if (!stack.isEmpty()) {
								InventoryHelper.dropItemStack(level, targetPos.getX(), targetPos.getY(), targetPos.getZ(), stack);
							}
						});

						level.destroyBlock(targetPos, false);

						energyVolume.minus(getEnergyConsumed());
					}
				}
			}
		});
	}

	@Override
	public CompoundNBT save(CompoundNBT tag) {
		tag.putInt("cooldown", cooldown);
		return super.save(tag);
	}

	@Override
	public void load(BlockState state, @NotNull CompoundNBT tag) {
		cooldown = tag.getInt("cooldown");
		super.load(state, tag);
	}
}
