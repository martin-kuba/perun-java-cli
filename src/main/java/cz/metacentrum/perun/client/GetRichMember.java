package cz.metacentrum.perun.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

import java.io.IOException;

/**
 * Gets VO member with its attributes.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class GetRichMember {

	public static void main(String[] args) throws ParseException, IOException {

		PerunApiClient pac = new PerunApiClient(
				"/json/membersManager/getRichMember",
				options -> options.addOption(Option.builder("id").required(true).hasArg().longOpt("member").desc("member id").build()),
				(params, commandLine) -> params.put("id", commandLine.getOptionValue("id"))
		);


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
