package cz.metacentrum.perun.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Empty command.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public abstract class PerunCommand {

	public abstract String getCommandDescription();


	public void addOptions(Options options) {
	}

	/**
	 * Default implementation calls just one Perun RPC method.
	 * @param ctx
	 */
	public void executeCommand(PerunApiClient.CommandContext ctx) {
		Map<String, Object> map = new LinkedHashMap<>();
		this.addParameters(ctx, map);
		JsonNode resp = PerunApiClient.callPerunRpc(ctx, map, this.getUrlPart(ctx.getCommandLine()));
		this.processResponse(resp);
	}

	public void addParameters(PerunApiClient.CommandContext ctx, Map<String, Object> params) {
	}

	public String getUrlPart(CommandLine commandLine) {
		return "";
	}


	public void processResponse(JsonNode resp) {
	}

}
