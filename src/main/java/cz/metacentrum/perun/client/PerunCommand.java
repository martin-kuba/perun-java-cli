package cz.metacentrum.perun.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.util.Map;

/**
 * Empty command.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public abstract class PerunCommand {

	public abstract String getCommandDescription();

	public abstract String getUrlPart(CommandLine commandLine);

	public void addOptions(Options options) {
	}

	public void addParameters(Map<String, Object> params, CommandLine commandLine) {
	}

	public void processResponse(JsonNode resp) {
	}

}
