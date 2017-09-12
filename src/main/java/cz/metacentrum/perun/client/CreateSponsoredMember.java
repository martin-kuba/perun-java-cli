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
public class CreateSponsoredMember {

	private final static Logger log = LoggerFactory.getLogger(CreateSponsoredMember.class);

	public static void main(String[] args) throws ParseException, IOException {
		//command line options
		Options options = new Options();
		options.addOption(Option.builder("u").required(true).hasArg().longOpt("httpuser").desc("HTTP Basic Auth user").build());
		options.addOption(Option.builder("p").required(true).hasArg().longOpt("httppassword").desc("HTTP Basic Auth password").build());
		options.addOption(Option.builder("i").required(false).hasArg().longOpt("perun-url").desc("Perun URL i.e https://perun-dev.meta.zcu.cz/krb/rpc-makub").build());

		options.addOption(Option.builder("vo").required(true).hasArg().desc("virtual organization id").build());
		options.addOption(Option.builder("n").required(false).hasArg().longOpt("namespace").desc("namespace").build());
		options.addOption(Option.builder("gn").required(true).hasArg().longOpt("guestName").desc("guest full name or description").build());
		options.addOption(Option.builder("gp").required(true).hasArg().longOpt("password").desc("guest password").build());
		options.addOption(Option.builder("uco").required(true).hasArg().longOpt("uco").desc("uco of sponsoring user").build());
		CommandLine commandLine;
		try {
			commandLine = new DefaultParser().parse(options, args);
		} catch (MissingOptionException ex) {
			new HelpFormatter().printHelp( CreateSponsoredMember.class.getSimpleName(), options );
			System.exit(1);
			return;
		}
		String username = commandLine.getOptionValue("u");
		String password = commandLine.getOptionValue("p");
		String perunUrl = System.getenv("PERUN_URL");
		if(commandLine.hasOption("i")) {
			perunUrl = commandLine.getOptionValue("i");
		}
		if(perunUrl==null) perunUrl = "https://perun-dev.meta.zcu.cz/krb/rpc";

		//prepare basic auth
		List<ClientHttpRequestInterceptor> interceptors = Collections.singletonList(new BasicAuthorizationInterceptor(username, password));
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(new InterceptingClientHttpRequestFactory(restTemplate.getRequestFactory(),interceptors));

		//make call
		Map<String, Object> map = new LinkedHashMap<>();

		map.put("vo", Integer.parseInt(commandLine.getOptionValue("vo")) );
		map.put("guestName", commandLine.getOptionValue("guestName"));
		map.put("password", commandLine.getOptionValue("password"));
		map.put("namespace", commandLine.hasOption("n")? commandLine.getOptionValue("n") : "dummy");
		map.put("extSourceName", "https://idp2.ics.muni.cz/idp/shibboleth");
		map.put("extLogin", commandLine.getOptionValue("uco")+"@muni.cz");

		String actionUrl = perunUrl + "/json/membersManager/createSponsoredMember";
		try {
			JsonNode jsonNode = restTemplate.postForObject(actionUrl, map, JsonNode.class);
			System.out.println("member.id : " + jsonNode.path("id"));
			System.out.println("member.sponsored : " + jsonNode.path("sponsored").asBoolean());
			System.out.println("user.id : " + jsonNode.path("userId"));
			System.out.println("user.titleBefore : " + jsonNode.path("user").path("titleBefore"));
			System.out.println("user.firstName : " + jsonNode.path("user").path("firstName"));
			System.out.println("user.lastName : " + jsonNode.path("user").path("lastName"));
			System.out.println("user.titleAfter : " + jsonNode.path("user").path("titleAfter"));
			int i=0;
			for(JsonNode ues :jsonNode.path("userExtSources")) {
				System.out.println("userExtSources["+i+"].login : "+ues.path("login"));
				System.out.println("userExtSources["+i+"].extSource.name : "+ues.path("extSource").path("name"));
				System.out.println("userExtSources["+i+"].extSource.type : "+ues.path("extSource").path("type"));
				i++;
			}
		} catch (HttpClientErrorException ex) {
			MediaType contentType = ex.getResponseHeaders().getContentType();
			String body = ex.getResponseBodyAsString();
			log.error("HTTP ERROR "+ex.getRawStatusCode()+" URL "+actionUrl+" Content-Type: "+ contentType);
			if("json".equals(contentType.getSubtype())) {
				log.error(new ObjectMapper().readValue(body, JsonNode.class).path("message").asText());
			} else {
				log.error(ex.getMessage());
			}
		}
	}
}
