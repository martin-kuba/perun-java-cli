package cz.metacentrum.perun.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Creates a sponsored member in a VO.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class CreateSponsoredMember {

	public static void main(String[] args) throws ParseException, IOException {

		PerunApiClient pac = new PerunApiClient(
				"/json/membersManager/createSponsoredMember",
				options -> {
					options.addOption(Option.builder("vo").required(true).hasArg().desc("virtual organization id").build());
					options.addOption(Option.builder("n").required(false).hasArg().longOpt("namespace").desc("namespace").build());
					options.addOption(Option.builder("gn").required(true).hasArg().longOpt("guestName").desc("guest full name or description").build());
					options.addOption(Option.builder("gp").required(true).hasArg().longOpt("password").desc("guest password").build());
					options.addOption(Option.builder("uco").required(true).hasArg().longOpt("uco").desc("uco of sponsoring user").build());
				},
				(params,commandLine) -> {
					params.put("vo", Integer.parseInt(commandLine.getOptionValue("vo")));
					params.put("guestName", commandLine.getOptionValue("guestName"));
					params.put("password", commandLine.getOptionValue("password"));
					params.put("namespace", commandLine.hasOption("n") ? commandLine.getOptionValue("n") : "dummy");
					params.put("extSourceName", "https://idp2.ics.muni.cz/idp/shibboleth");
					params.put("extLogin", commandLine.getOptionValue("uco") + "@muni.cz");
				});

			JsonNode jsonNode = pac.call(args);

			System.out.println("member.id : " + jsonNode.path("id"));
			System.out.println("member.sponsored : " + jsonNode.path("sponsored").asBoolean());
			System.out.println("user.id : " + jsonNode.path("userId"));
			System.out.println("user.titleBefore : " + jsonNode.path("user").path("titleBefore"));
			System.out.println("user.firstName : " + jsonNode.path("user").path("firstName"));
			System.out.println("user.lastName : " + jsonNode.path("user").path("lastName"));
			System.out.println("user.titleAfter : " + jsonNode.path("user").path("titleAfter"));
			int i = 0;
			for (JsonNode ues : jsonNode.path("userExtSources")) {
				System.out.println("userExtSources[" + i + "].login : " + ues.path("login"));
				System.out.println("userExtSources[" + i + "].extSource.name : " + ues.path("extSource").path("name"));
				System.out.println("userExtSources[" + i + "].extSource.type : " + ues.path("extSource").path("type"));
				i++;
			}
	}
}
