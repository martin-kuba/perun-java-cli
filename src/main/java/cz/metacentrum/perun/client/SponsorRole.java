package cz.metacentrum.perun.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.cli.*;
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
 * Created with IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class SponsorRole {

	private final static Logger log = LoggerFactory.getLogger(SponsorRole.class);

	public static void main(String[] args) throws ParseException, IOException {
		//command line options
		Options options = new Options();
		options.addOption(Option.builder("u").required(true).hasArg().desc("HTTP Basic Auth user").build());
		options.addOption(Option.builder("p").required(true).hasArg().desc("HTTP Basic Auth password").build());
		options.addOption(Option.builder("d").required(false).hasArg().longOpt("dev").desc("Perun development instance infix (makub,zlamal,etc.)").build());
		options.addOption(Option.builder("vo").required(true).hasArg().desc("virtual organization id").build());
		options.addOption(Option.builder("user").required(false).hasArg().desc("user id").build());
		options.addOption(Option.builder("group").required(false).hasArg().desc("group id").build());
		options.addOption(Option.builder("add").required(false).hasArg(false).desc("causes calling addSponsorRole (default)").build());
		options.addOption(Option.builder("remove").required(false).hasArg(false).desc("causes calling removeSponsorRole").build());
		CommandLine commandLine;
		try {
			commandLine = new DefaultParser().parse(options, args);
		} catch (MissingOptionException ex) {
			new HelpFormatter().printHelp( SponsorRole.class.getSimpleName(), options );
			System.exit(1);
			return;
		}
		String username = commandLine.getOptionValue("u");
		String password = commandLine.getOptionValue("p");


		//prepare basic auth
		List<ClientHttpRequestInterceptor> interceptors = Collections.singletonList(new BasicAuthorizationInterceptor(username, password));
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(new InterceptingClientHttpRequestFactory(restTemplate.getRequestFactory(),interceptors));

		//make call
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("vo", commandLine.getOptionValue("vo"));

		String dev = commandLine.hasOption("d") ? "-"+commandLine.getOptionValue("d") : "";
		String op = commandLine.hasOption("remove") ? "removeSponsorRole" : "addSponsorRole";

		if(commandLine.hasOption("user")) {
			map.put("user", Integer.parseInt(commandLine.getOptionValue("user")));
		} else if(commandLine.hasOption("group")) {
			map.put("authorizedGroup", Integer.parseInt(commandLine.getOptionValue("group")));
		} else {
			System.err.println("either -user or -group must be specified");
			System.exit(1);
		}
		try {
			JsonNode jsonNode = restTemplate.postForObject("https://perun-dev.meta.zcu.cz/krb/rpc" + dev + "/json/vosManager/"+op, map, JsonNode.class);
			System.out.println("OK");
		} catch (HttpClientErrorException ex) {
			MediaType contentType = ex.getResponseHeaders().getContentType();
			String body = ex.getResponseBodyAsString();
			log.error("HTTP ERROR "+ex.getRawStatusCode()+" Content-Type: "+ contentType);
			if("json".equals(contentType.getSubtype())) {
				log.error(new ObjectMapper().readValue(body, JsonNode.class).path("message").asText());
			} else {
				log.error(ex.getMessage());
			}
		}
	}
}
