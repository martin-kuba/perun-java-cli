package cz.metacentrum.perun.client.commands;

import com.fasterxml.jackson.databind.JsonNode;
import cz.metacentrum.perun.client.PerunApiClient;
import cz.metacentrum.perun.client.PerunCommand;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.util.Map;

/**
 * Creates a sponsored member in a VO.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class CreateSponsoredMember extends PerunCommand {

	@Override
	public void addOptions(Options options) {
		options.addOption(Option.builder("vo").required(true).hasArg().desc("virtual organization id").build());
		options.addOption(Option.builder("n").required(false).hasArg().longOpt("namespace").desc("namespace").build());
		options.addOption(Option.builder("gn").required(true).hasArg().longOpt("guestName").desc("guest full name or description").build());
		options.addOption(Option.builder("gp").required(true).hasArg().longOpt("password").desc("guest password").build());
		options.addOption(Option.builder("uco").required(true).hasArg().longOpt("uco").desc("uco of sponsoring user").build());
	}

	@Override
	public void addParameters(PerunApiClient.CommandContext ctx, Map<String, Object> params) {
		CommandLine commandLine = ctx.getCommandLine();
		params.put("vo", Integer.parseInt(commandLine.getOptionValue("vo")));
		params.put("guestName", commandLine.getOptionValue("guestName"));
		params.put("password", commandLine.getOptionValue("password"));
		params.put("namespace", commandLine.hasOption("n") ? commandLine.getOptionValue("n") : "dummy");
		params.put("extSourceName", "https://idp2.ics.muni.cz/idp/shibboleth");
		params.put("extLogin", commandLine.getOptionValue("uco") + "@muni.cz");
	}

	@Override
	public String getCommandDescription() {
		return "Creates a sponsored member in a VO";
	}

	@Override
	public String getUrlPart(CommandLine commandLine) {
		return "/json/membersManager/createSponsoredMember";
	}

	@Override
	public void processResponse(JsonNode resp) {
		System.out.println("member.id : " + resp.path("id"));
		System.out.println("member.sponsored : " + resp.path("sponsored").asBoolean());
		System.out.println("user.id : " + resp.path("userId"));
		System.out.println("user.titleBefore : " + resp.path("user").path("titleBefore"));
		System.out.println("user.firstName : " + resp.path("user").path("firstName"));
		System.out.println("user.lastName : " + resp.path("user").path("lastName"));
		System.out.println("user.titleAfter : " + resp.path("user").path("titleAfter"));
		int i = 0;
		for (JsonNode ues : resp.path("userExtSources")) {
			System.out.println("userExtSources[" + i + "].login : " + ues.path("login"));
			System.out.println("userExtSources[" + i + "].extSource.name : " + ues.path("extSource").path("name"));
			System.out.println("userExtSources[" + i + "].extSource.type : " + ues.path("extSource").path("type"));
			i++;
		}
	}
}
