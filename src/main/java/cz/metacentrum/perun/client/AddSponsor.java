package cz.metacentrum.perun.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

import java.io.IOException;

/**
 * Adds a sponsor.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class AddSponsor {

	public static void main(String[] args) throws ParseException, IOException {

		PerunApiClient pac = new PerunApiClient(
				"/json/membersManager/sponsorMember",
				options -> {
					options.addOption(Option.builder("m").longOpt("member").required(true).hasArg().desc("sponsored member id").build());
					options.addOption(Option.builder("s").longOpt("sponsor").required(true).hasArg().desc("sponsoring user id").build());
				},
				(params,commandLine) -> {
					params.put("member", commandLine.getOptionValue("m"));
					params.put("sponsor", commandLine.getOptionValue("s"));
				});

			JsonNode jsonNode = pac.call(args);

			System.out.println("member.id : " + jsonNode.path("id"));
			System.out.println("member.sponsored : " + jsonNode.path("sponsored").asBoolean());
			System.out.println("user.id : " + jsonNode.path("userId"));
			System.out.println("user.titleBefore : " + jsonNode.path("user").path("titleBefore"));
			System.out.println("user.firstName : " + jsonNode.path("user").path("firstName"));
			System.out.println("user.lastName : " + jsonNode.path("user").path("lastName"));
			System.out.println("user.titleAfter : " + jsonNode.path("user").path("titleAfter"));
			
	}
}
