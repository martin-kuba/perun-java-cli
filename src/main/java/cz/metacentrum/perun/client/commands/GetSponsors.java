package cz.metacentrum.perun.client.commands;

import com.fasterxml.jackson.databind.JsonNode;
import cz.metacentrum.perun.client.PerunApiClient;
import cz.metacentrum.perun.client.PerunCommand;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.util.Map;

/**
 * Adds or removes role SPONSOR to user or group.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class GetSponsors extends PerunCommand {

	@Override
	public void addOptions(Options options) {
		options.addOption(Option.builder("member").required(true).hasArg().desc("sponsored member id").build());
	}

	@Override
	public void addParameters(PerunApiClient.CommandContext ctx, Map<String, Object> params) {
		CommandLine commandLine = ctx.getCommandLine();
		params.put("member", commandLine.getOptionValue("member"));
	}

	@Override
	public String getCommandDescription() {
		return "Lists users who are sponsors of a member";
	}

	@Override
	public String getUrlPart(CommandLine commandLine) {
		return "/json/membersManager/getSponsors";
	}

	@Override
	public void processResponse(JsonNode resp) {
		for(JsonNode user : resp) {
			System.out.println("--- sponsor ----");
			System.out.println("id : " + user.path("id"));
			System.out.println("titleBefore : " + user.path("titleBefore"));
			System.out.println("firstName : " + user.path("firstName"));
			System.out.println("lastName : " + user.path("lastName"));
			System.out.println("titleAfter : " + user.path("titleAfter"));
		}
	}

}
