package moze_intel.projecte.integration.crafttweaker.actions;

import com.blamejared.crafttweaker.api.action.base.IUndoableAction;
import java.util.Map;
import java.util.Map.Entry;
import moze_intel.projecte.api.nss.NSSTag;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.integration.crafttweaker.mappers.CrTConversionEMCMapper;
import moze_intel.projecte.integration.crafttweaker.mappers.CrTConversionEMCMapper.CrTConversion;

public class CustomConversionAction implements IUndoableAction {

	private final CrTConversion conversion;

	public CustomConversionAction(NormalizedSimpleStack output, int amount, boolean propagateTags, boolean set, Map<NormalizedSimpleStack, Integer> ingredients) {
		conversion = new CrTConversion(output, amount, propagateTags, set, ingredients);
	}

	@Override
	public void apply() {
		CrTConversionEMCMapper.addConversion(conversion);
	}

	@Override
	public String describe() {
		StringBuilder inputString = new StringBuilder();
		for (Entry<NormalizedSimpleStack, Integer> entry : conversion.ingredients().entrySet()) {
			if (inputString.length() > 0) {
				//If we already have elements, prepend a comma
				inputString.append(", ");
			}
			int amount = entry.getValue();
			if (amount > 1) {
				inputString.append(amount).append(" ");
			}
			inputString.append(entry.getKey());
		}
		String description = "Added custom conversion creating '" + conversion.amount() + "' of " + conversion.output() + ", from: " + inputString;
		if (conversion.propagateTags() && conversion.output() instanceof NSSTag) {
			description += "; propagating to elements of " + conversion.output();
		}
		return description;
	}

	@Override
	public void undo() {
		CrTConversionEMCMapper.removeConversion(conversion);
	}

	@Override
	public String describeUndo() {
		return "Undoing adding of custom conversion creating '" + conversion.amount() + "' of " + conversion.output();
	}
}