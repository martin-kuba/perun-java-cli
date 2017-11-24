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
public class AddSponsor extends PerunCommand {

	@Override
	public void addOptions(Options options) {
		options.addOption(Option.builder("m").longOpt("member").required(true).hasArg().desc("sponsored member id").build());
		options.addOption(Option.builder("s").longOpt("sponsor").required(true).hasArg().desc("sponsoring user id").build());
	}

	@Override
	public void addParameters(PerunApiClient.RpcCallsContext ctx, Map<String, Object> params, CommandLine commandLine) {
		params.put("member", commandLine.getOptionValue("m"));
		params.put("sponsor", commandLine.getOptionValue("s"));
	}

	@Override
	public String getCommandDescription() {
		return "Adds a sponsor";
	}

	@Override
	public String getUrlPart(CommandLine commandLine) {
		return "/json/membersManager/sponsorMember";
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
	}

}
