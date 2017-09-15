package cz.metacentrum.perun.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Adds or removes role SPONSOR to user or group.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class SponsorRole {

	private final static Logger log = LoggerFactory.getLogger(SponsorRole.class);

	public static void main(String[] args) throws ParseException, IOException {

		PerunApiClient pac = new PerunApiClient(
				"/json/vosManager/",
				options -> {
					options.addOption(Option.builder("vo").required(true).hasArg().desc("virtual organization id").build());
					options.addOption(Option.builder("user").required(false).hasArg().desc("user id").build());
					options.addOption(Option.builder("group").required(false).hasArg().desc("group id").build());
					options.addOption(Option.builder("add").required(false).hasArg(false).desc("causes calling addSponsorRole (default)").build());
					options.addOption(Option.builder("remove").required(false).hasArg(false).desc("causes calling removeSponsorRole").build());
				},
				(params, commandLine) -> {
					params.put("vo", commandLine.getOptionValue("vo"));
					if (commandLine.hasOption("user")) {
						params.put("user", Integer.parseInt(commandLine.getOptionValue("user")));
					} else if (commandLine.hasOption("group")) {
						params.put("authorizedGroup", Integer.parseInt(commandLine.getOptionValue("group")));
					} else {
						System.err.println("either -user or -group must be specified");
						System.exit(1);
					}
				}
		);
		pac.modifyRpcPath((rpcPath, commandLine) -> rpcPath + (commandLine.hasOption("remove") ? "removeSponsorRole" : "addSponsorRole"));

		JsonNode result = pac.call(args);
		System.out.println("OK");
	}
}
