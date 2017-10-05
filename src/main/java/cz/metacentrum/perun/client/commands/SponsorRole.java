package cz.metacentrum.perun.client.commands;

import com.fasterxml.jackson.databind.JsonNode;
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
public class SponsorRole extends PerunCommand {

	@Override
	public void addOptions(Options options) {
		options.addOption(Option.builder("vo").required(true).hasArg().desc("virtual organization id").build());
		options.addOption(Option.builder("user").required(false).hasArg().desc("user id").build());
		options.addOption(Option.builder("group").required(false).hasArg().desc("group id").build());
		options.addOption(Option.builder("add").required(false).hasArg(false).desc("causes calling addSponsorRole (default)").build());
		options.addOption(Option.builder("remove").required(false).hasArg(false).desc("causes calling removeSponsorRole").build());
	}

	@Override
	public void addParameters(Map<String, Object> params, CommandLine commandLine) {
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

	@Override
	public String getCommandDescription() {
		return "Adds or removes role SPONSOR to user or group";
	}

	@Override
	public String getUrlPart(CommandLine commandLine) {
		return "/json/vosManager/" + (commandLine.hasOption("remove") ? "removeSponsorRole" : "addSponsorRole");
	}

	@Override
	public void processResponse(JsonNode resp) {
		System.out.println("OK");
	}

}
