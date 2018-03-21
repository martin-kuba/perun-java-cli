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
public class MakeUserPerunAdmin extends PerunCommand {

	@Override
	public void addOptions(Options options) {
		options.addOption(Option.builder("uid").longOpt("userId").required(true).hasArg().desc("User id").build());
	}

	@Override
	public void addParameters(PerunApiClient.CommandContext ctx, Map<String, Object> params) {
		CommandLine commandLine = ctx.getCommandLine();
		params.put("user", commandLine.getOptionValue("uid"));
	}

	@Override
	public String getCommandDescription() {
		return "Marks a user as PERUN_ADMIN";
	}

	@Override
	public String getUrlPart(CommandLine commandLine) {
		return "/json/usersManager/makeUserPerunAdmin";
	}

	@Override
	public void processResponse(JsonNode resp) {
		System.out.println("OK, done");
	}

}
