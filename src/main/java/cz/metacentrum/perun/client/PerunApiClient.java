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
import org.springframework.security.kerberos.client.KerberosRestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
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

	public static void call(PerunCommand command, String[] cliArgs) throws IOException, ParseException {
		Options options = new Options();
		options.addOption(Option.builder("i").required(false).hasArg().longOpt("perun-url").desc("Perun URL i.e https://perun-dev.meta.zcu.cz/krb/rpc-makub").build());

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
		KerberosRestTemplate restTemplate = new KerberosRestTemplate(null, "-");

		//make call
		Map<String, Object> map = new LinkedHashMap<>();
		command.addParameters(map, commandLine);

		String actionUrl = perunUrl + command.getUrlPart(commandLine);

		try {
			command.processResponse(restTemplate.postForObject(actionUrl, map, JsonNode.class));
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
