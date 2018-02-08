package cz.metacentrum.perun.client.commands;

import com.fasterxml.jackson.databind.JsonNode;
import cz.metacentrum.perun.client.PerunApiClient;
import cz.metacentrum.perun.client.PerunCommand;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.util.Map;

/**
 * Adds a sponsor.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class ConvertAttributeToUnique extends PerunCommand {

	@Override
	public void addOptions(Options options) {
		options.addOption(Option.builder("a").longOpt("attrDefId").required(true).hasArg().desc("AttributeDefinition id").build());
	}

	@Override
	public void addParameters(PerunApiClient.CommandContext ctx, Map<String, Object> params) {
		CommandLine commandLine = ctx.getCommandLine();
		params.put("attrDefId", commandLine.getOptionValue("a"));
	}

	@Override
	public String getCommandDescription() {
		return "Converts attribute (definition and all values) to unique - no value may be duplicate";
	}

	@Override
	public String getUrlPart(CommandLine commandLine) {
		return "/json/attributesManager/convertAttributeToUnique";
	}

	@Override
	public void processResponse(JsonNode resp) {
		System.out.println("OK, done");
	}

}
