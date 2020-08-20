package com.github.chainmailstudios.astromine.foundations.datagen.generators.tag;

import net.minecraft.util.Identifier;

import com.github.chainmailstudios.astromine.foundations.datagen.MaterialSet;
import com.github.chainmailstudios.astromine.foundations.datagen.generators.SetGenerator;
import me.shedaniel.cloth.api.datagen.v1.TagData;

public class ItemInTagGenerator implements OneTimeTagGenerator {
	private final Identifier tagId;
	private final Identifier itemId;

	public ItemInTagGenerator(Identifier tagId, Identifier itemId) {
		this.tagId = tagId;
		this.itemId = itemId;
	}

	@Override
	public void generate(TagData data) {
		data.item(tagId).append(itemId);
	}

	@Override
	public String getGeneratorName() {
		return "item_in_tag";
	}
}
