package cz.metacentrum.perun.client.commands;

import com.fasterxml.jackson.databind.JsonNode;
import cz.metacentrum.perun.client.PerunApiClient;
import cz.metacentrum.perun.client.PerunCommand;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.util.Map;

/**
 * Gets user with its attributes.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class GetRichUser extends PerunCommand {

	@Override
	public String getCommandDescription() {
		return "Gets user with its attributes";
	}

	@Override
	public String getUrlPart(CommandLine commandLine) {
		return "/json/usersManager/getRichUserWithAttributes";
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

	@Override
	public void addOptions(Options options) {
		options.addOption(Option.builder("id").required(true).hasArg().longOpt("member").desc("member id").build());
	}

	@Override
	public void addParameters(PerunApiClient.CommandContext ctx, Map<String, Object> params) {
		CommandLine commandLine = ctx.getCommandLine();
		params.put("id", commandLine.getOptionValue("id"));
	}

}
