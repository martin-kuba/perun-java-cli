package cz.metacentrum.perun.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of Perun RPC API client in Java.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
@SuppressWarnings("WeakerAccess")
public class PerunApiClient {

	private final static Logger log = LoggerFactory.getLogger(PerunApiClient.class);
	private OptionsModifier om;
	private ParametersModifier pm;
	private RpcPathModifier rpcPathModifier;
	private String rpcPath;

	@FunctionalInterface
	public interface OptionsModifier {
		void addOptions(Options options);
	}

	@FunctionalInterface
	public interface ParametersModifier {
		void addParameters(Map<String, Object> params, CommandLine commandLine);
	}

	@FunctionalInterface
	public interface RpcPathModifier {
		String build(String rpcPath, CommandLine commandLine);
	}

	public void modifyRpcPath(RpcPathModifier rpcPathModifier) {
		this.rpcPathModifier = rpcPathModifier;
	}

	/**
	 * Creates a client for Perun RPC API.
	 * Predefined command line options are:
	 * <li>
	 * <ul>-u --httpuser</ul>
	 * <ul>-p --httppassword</ul>
	 * <ul>-i --perun-url</ul>
	 * </li>
	 *
	 * @param rpcPath part of URL selecting called method
	 * @param om      lambda for modifying CLI options
	 * @param pm      lambda for setting request parameters
	 */
	public PerunApiClient(String rpcPath, OptionsModifier om, ParametersModifier pm) {
		this.om = om;
		this.pm = pm;
		this.rpcPath = rpcPath;
	}

	public JsonNode call(String[] cliArgs) throws IOException, ParseException {
		Options options = new Options();
		options.addOption(Option.builder("u").required(true).hasArg().longOpt("httpuser").desc("HTTP Basic Auth user").build());
		options.addOption(Option.builder("p").required(true).hasArg().longOpt("httppassword").desc("HTTP Basic Auth password").build());
		options.addOption(Option.builder("i").required(false).hasArg().longOpt("perun-url").desc("Perun URL i.e https://perun-dev.meta.zcu.cz/krb/rpc-makub").build());

		om.addOptions(options);

		CommandLine commandLine;
		try {
			commandLine = new DefaultParser().parse(options, cliArgs);
		} catch (MissingOptionException ex) {
			new HelpFormatter().printHelp(CreateSponsoredMember.class.getSimpleName(), options);
			System.exit(1);
			return null;
		}
		String username = commandLine.getOptionValue("u");
		String password = commandLine.getOptionValue("p");
		String perunUrl = System.getenv("PERUN_URL");
		if (commandLine.hasOption("i")) {
			perunUrl = commandLine.getOptionValue("i");
		}
		if (perunUrl == null) perunUrl = "https://perun-dev.meta.zcu.cz/krb/rpc";

		//prepare basic auth
		List<ClientHttpRequestInterceptor> interceptors = Collections.singletonList(new BasicAuthorizationInterceptor(username, password));
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(new InterceptingClientHttpRequestFactory(restTemplate.getRequestFactory(), interceptors));

		//make call
		Map<String, Object> map = new LinkedHashMap<>();
		pm.addParameters(map, commandLine);

		if (rpcPathModifier != null) rpcPath = rpcPathModifier.build(rpcPath, commandLine);
		String actionUrl = perunUrl + rpcPath;

		try {
			return restTemplate.postForObject(actionUrl, map, JsonNode.class);

		} catch (HttpClientErrorException ex) {
			MediaType contentType = ex.getResponseHeaders().getContentType();
			String body = ex.getResponseBodyAsString();
			log.error("HTTP ERROR " + ex.getRawStatusCode() + " URL " + actionUrl + " Content-Type: " + contentType);
			if ("json".equals(contentType.getSubtype())) {
				log.error(new ObjectMapper().readValue(body, JsonNode.class).path("message").asText());
			} else {
				log.error(ex.getMessage());
			}
			System.exit(1);
			return null;
		}
	}
}
