package com.github.chainmailstudios.astromine.foundations.datagen;

import com.github.chainmailstudios.astromine.AstromineCommon;
import com.github.chainmailstudios.astromine.foundations.datagen.generators.Generator;
import com.github.chainmailstudios.astromine.foundations.datagen.generators.SetGenerator;
import me.shedaniel.cloth.api.datagen.v1.TagData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.github.chainmailstudios.astromine.foundations.datagen.MaterialItemType.*;

public class MaterialSet {
	private final String name;
	private final Map<MaterialItemType, MaterialEntry> items;
	private final Optional<MaterialSet> smithingBaseSet;
	private final boolean piglinLoved;

	private MaterialSet(String name, Map<MaterialItemType, MaterialEntry> items, Optional<MaterialSet> smithingBaseSet, boolean piglinLoved) {
		this.name = name;
		this.items = items;
		this.smithingBaseSet = smithingBaseSet;
		this.piglinLoved = piglinLoved;
	}

	public boolean hasType(MaterialItemType type) {
		return items.containsKey(type);
	}

	public Map<MaterialItemType, MaterialEntry> getItems() {
		return items;
	}

	public MaterialEntry getEntry(MaterialItemType type) {
		return items.get(type);
	}

	public Identifier getItemId(MaterialItemType type) {
		return getEntry(type).getItemId();
	}

	public String getItemIdPath(MaterialItemType type) {
		return getItemId(type).getPath();
	}

	public Identifier getItemTagId(MaterialItemType type) {
		return getEntry(type).getItemTagId();
	}

	public Ingredient getIngredient(MaterialItemType type) {
		return getEntry(type).asIngredient();
	}

	public Item getItem(MaterialItemType type) {
		return getEntry(type).asItem();
	}

	public boolean isFromVanilla(MaterialItemType type) {
		return hasType(type) && getEntry(type).isFromVanilla();
	}

	public String getName() {
		return name;
	}

	public boolean shouldGenerate(SetGenerator generator) {
		return generator.shouldGenerate(this);
	}

	public boolean usesSmithing() {
		return smithingBaseSet.isPresent();
	}

	public MaterialSet getSmithingBaseSet() {
		return smithingBaseSet.get();
	}

	public void generateTags(TagData tags) {
		items.forEach((type, entry) -> {
			if (entry.hasItemTag()) {
				tags.item(entry.getItemTagId()).append(entry);
				if (entry.isBlock()) {
					tags.block(entry.getItemTagId()).append(entry.asBlock());
				}
			}
		});
	}

	public boolean isPiglinLoved() {
		return piglinLoved;
	}

	public static class Builder {
		public Map<MaterialItemType, MaterialEntry> items = new HashMap<>();
		public String name;
		public MaterialSet smithingBaseSet = null;
		public boolean piglinLoved = false;

		public Builder(String name) {
			this.name = name;
		}

		public Builder setType(MaterialItemType type, MaterialEntry entry) {
			items.put(type, entry);
			return this;
		}

		public Builder setType(MaterialItemType type, Identifier itemId, Identifier tagId) {
			return setType(type, new MaterialEntry(itemId, tagId));
		}

		public Builder setType(MaterialItemType type, Identifier itemId) {
			return setType(type, new MaterialEntry(itemId));
		}

		public Builder setType(MaterialItemType type, ItemConvertible item) {
			return setType(type, Registry.ITEM.getId(item.asItem()));
		}

		public Builder setType(MaterialItemType type, ItemConvertible item, Identifier tagId) {
			return setType(type, Registry.ITEM.getId(item.asItem()), tagId);
		}

		public Builder addType(MaterialItemType type, Identifier tagId) {
			return setType(type, AstromineCommon.identifier(type.getItemId(name)), tagId);
		}

		public Builder addType(MaterialItemType type) {
			return setType(type, new Identifier("c", type.getItemId(name)));
		}

		public Builder basics() {
			block();
			return dusts();
		}

		public Builder metal() {
			ingot();
			nugget();
			block();
			plates();
			return gear();
		}

		public Builder ingot() {
			return addType(INGOT, new Identifier("c", name + "_ingots"));
		}

		public Builder nugget() {
			return addType(NUGGET, new Identifier("c", name + "_nuggets"));
		}

		public Builder gem() {
			return addType(GEM, new Identifier("c", name + "s"));
		}

		public Builder fragment() {
			return addType(FRAGMENT, new Identifier("c", name + "_fragments"));
		}

		public Builder block() {
			return addType(BLOCK, new Identifier("c", name + "_blocks"));
		}

		public Builder ore() {
			return addType(ORE, new Identifier("c", name + "_ores"));
		}

		public Builder asteroid() {
			asteroidOre();
			return asteroidCluster();
		}

		public Builder meteor() {
			meteorOre();
			return meteorCluster();
		}

		public Builder asteroidOre() {
			return addType(ASTEROID_ORE, new Identifier("c", "asteroid_" + name + "_ores"));
		}

		public Builder meteorOre() {
			return addType(METEOR_ORE, new Identifier("c", "meteor_" + name + "_ores"));
		}

		public Builder asteroidCluster() {
			return addType(ASTEROID_CLUSTER, new Identifier("c", "asteroid_" + name + "_clusters"));
		}

		public Builder meteorCluster() {
			return addType(METEOR_CLUSTER, new Identifier("c", "meteor_" + name + "_clusters"));
		}

		public Builder dusts() {
			dust();
			return tinyDust();
		}

		public Builder dust() {
			return addType(DUST, new Identifier("c", name + "_dusts"));
		}

		public Builder tinyDust() {
			return addType(TINY_DUST, new Identifier("c", name + "_tiny_dusts"));
		}

		public Builder gear() {
			return addType(GEAR, new Identifier("c", name + "_gears"));
		}

		public Builder plates() {
			return addType(PLATES, new Identifier("c", name + "_plates"));
		}

		public Builder wire() {
			return addType(WIRE, new Identifier("c", name + "_wires"));
		}

		public Builder allTools() {
			basicTools();
			multiTools();
			return magnaTools();
		}

		public Builder basicTools() {
			pickaxe();
			axe();
			shovel();
			sword();
			return hoe();
		}

		public Builder pickaxe() {
			return addType(PICKAXE, new Identifier("fabric", "pickaxes"));
		}

		public Builder axe() {
			return addType(AXE, new Identifier("fabric", "axes"));
		}

		public Builder shovel() {
			return addType(SHOVEL, new Identifier("fabric", "shovels"));
		}

		public Builder sword() {
			return addType(SWORD, new Identifier("fabric", "swords"));
		}

		public Builder hoe() {
			return addType(HOE, new Identifier("fabric", "hoes"));
		}

		public Builder multiTools() {
			mattock();
			return miningTool();
		}

		public Builder mattock() {
			return addType(MATTOCK, AstromineCommon.identifier("mattocks"));
		}

		public Builder mattock(Identifier id) {
			return setType(MATTOCK, id, AstromineCommon.identifier("mattocks"));
		}

		public Builder miningTool() {
			return addType(MINING_TOOL, AstromineCommon.identifier("mining_tools"));
		}

		public Builder miningTool(Identifier id) {
			return setType(MINING_TOOL, id, AstromineCommon.identifier("mining_tools"));
		}

		public Builder magnaTools() {
			hammer();
			return excavator();
		}

		public Builder hammer() {
			return addType(HAMMER, AstromineCommon.identifier("hammers"));
		}

		public Builder excavator() {
			return addType(EXCAVATOR, AstromineCommon.identifier("excavators"));
		}

		public Builder armor() {
			helmet();
			chestplate();
			leggings();
			return boots();
		}

		public Builder helmet() {
			return addType(HELMET, new Identifier("c", "helmets"));
		}

		public Builder chestplate() {
			return addType(CHESTPLATE, new Identifier("c", "chestplates"));
		}

		public Builder leggings() {
			return addType(LEGGINGS, new Identifier("c", "leggings"));
		}

		public Builder boots() {
			return addType(BOOTS, new Identifier("c", "boots"));
		}

		public Builder smithingBaseSet(MaterialSet set) {
			this.smithingBaseSet = set;
			return this;
		}

		public Builder piglinLoved() {
			this.piglinLoved = true;
			return this;
		}

		public MaterialSet build() {
			return new MaterialSet(name, items, Optional.ofNullable(smithingBaseSet), piglinLoved);
		}
	}
}
