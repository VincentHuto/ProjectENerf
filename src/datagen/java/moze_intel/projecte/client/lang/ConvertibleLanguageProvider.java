package moze_intel.projecte.client.lang;

import java.util.List;
import moze_intel.projecte.client.lang.FormatSplitter.Component;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

/**
 * @apiNote From Mekanism
 */
public abstract class ConvertibleLanguageProvider extends LanguageProvider {

	public ConvertibleLanguageProvider(DataGenerator gen, String modid, String locale) {
		super(gen, modid, locale);
	}

	public abstract void convert(String key, List<Component> splitEnglish);

	@Override
	protected void addTranslations() {
	}
}