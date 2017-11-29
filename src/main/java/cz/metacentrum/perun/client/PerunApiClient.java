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
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.security.kerberos.client.KerberosRestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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

	public static class RpcCallsContext {
		private final RestTemplate restTemplate;
		private final String perunUrl;

		public RpcCallsContext(RestTemplate restTemplate, String perunUrl) {
			this.restTemplate = restTemplate;
			this.perunUrl = perunUrl;
		}

		public RestTemplate getRestTemplate() {
			return restTemplate;
		}

		public String getPerunUrl() {
			return perunUrl;
		}
	}

	public static void call(PerunCommand command, String[] cliArgs) throws IOException, ParseException {
		Options options = new Options();
		options.addOption(Option.builder("i").required(false).hasArg().longOpt("perun-url").desc("Perun URL i.e https://perun-dev.meta.zcu.cz/krb/rpc-makub").build());
		options.addOption(Option.builder("u").required(false).hasArg().longOpt("httpuser").desc("HTTP Basic Auth user").build());
		options.addOption(Option.builder("p").required(false).hasArg().longOpt("httppassword").desc("HTTP Basic Auth password").build());

		command.addOptions(options);

		CommandLine commandLine;
		try {
			commandLine = new DefaultParser().parse(options, cliArgs);
		} catch (MissingOptionException ex) {
			new HelpFormatter().printHelp(command.getClass().getSimpleName(), options);
			System.exit(1);
			return;
		}
		String perunUrl = System.getenv("PERUN_URL");
		if (commandLine.hasOption("i")) {
			perunUrl = commandLine.getOptionValue("i");
		}
		if (perunUrl == null) perunUrl = "https://perun-dev.meta.zcu.cz/krb/rpc";

		//prepare basic auth
		RestTemplate restTemplate;
		if (commandLine.hasOption("u") && commandLine.hasOption("p")) {
			String username = commandLine.getOptionValue("u");
			String password = commandLine.getOptionValue("p");
			List<ClientHttpRequestInterceptor> interceptors = Collections.singletonList(new BasicAuthorizationInterceptor(username, password));
			restTemplate = new RestTemplate();
			restTemplate.setRequestFactory(new InterceptingClientHttpRequestFactory(restTemplate.getRequestFactory(), interceptors));
			log.info("using username {} for HTTP Basic Authorization", username);
		} else {
			restTemplate = new KerberosRestTemplate(null, "-");
			log.info("using Kerberos ticket for authorization");
		}

		//make call
		Map<String, Object> map = new LinkedHashMap<>();
		RpcCallsContext rpcCallsContext = new RpcCallsContext(restTemplate, perunUrl);
		command.addParameters(rpcCallsContext, map, commandLine);

		JsonNode resp = callPerunRpc(restTemplate, map, perunUrl + command.getUrlPart(commandLine));
		command.processResponse(resp);
	}

	public static JsonNode callPerunRpc(RestTemplate restTemplate, Map<String, Object> parametersMap, String actionUrl) {
		try {
			return restTemplate.postForObject(actionUrl, parametersMap, JsonNode.class);
		} catch (HttpClientErrorException ex) {
			MediaType contentType = ex.getResponseHeaders().getContentType();
			String body = ex.getResponseBodyAsString();
			log.error("HTTP ERROR " + ex.getRawStatusCode() + " URL " + actionUrl + " Content-Type: " + contentType);
			if ("json".equals(contentType.getSubtype())) {
				try {
					log.error(new ObjectMapper().readValue(body, JsonNode.class).path("message").asText());
				} catch (IOException e) {
					log.error("cannot parse response body: " + body);
				}
			} else {
				log.error(ex.getMessage());
			}
			throw new RuntimeException(ex);
		}
	}

	public static void main(String[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException, ParseException {
		//find all classes implementing commands
		Reflections reflections = new Reflections("cz.metacentrum.perun.client.commands");
		List<Class<? extends PerunCommand>> classes = new ArrayList<>(reflections.getSubTypesOf(PerunCommand.class));
		classes.sort(Comparator.comparing(Class::getSimpleName));
		List<PerunCommand> commands = new ArrayList<>(classes.size());
		for (Class<? extends PerunCommand> aClass : classes) {
			commands.add(aClass.newInstance());
		}
		//if no arguments specified, print list of available commands
		if (args.length == 0) {
			System.err.println("Usage: <command> <options>");
			System.err.println();
			System.err.println("run a command without options to see a list of its available options");
			System.err.println();
			System.err.println("available commands:");
			for (PerunCommand command : commands) {
				System.err.println("  " + command.getClass().getSimpleName() + " ... " + command.getCommandDescription());
			}
			System.exit(1);
		}
		//call the command from class specified as first argument
		String[] options = args.length == 1 ? new String[]{} : Arrays.copyOfRange(args, 1, args.length);
		for (PerunCommand command : commands) {
			if (command.getClass().getSimpleName().equals(args[0])) {
				call(command, options);
				return;
			}
		}
		System.err.println("Command not recognized: " + args[0]);
	}
}
