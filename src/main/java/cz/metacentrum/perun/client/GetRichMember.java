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
public class GetRichMember {

	private final static Logger log = LoggerFactory.getLogger(GetRichMember.class);

	public static void main(String[] args) throws ParseException, IOException {
		//command line options
		Options options = new Options();
		options.addOption(Option.builder("u").required(true).hasArg().longOpt("httpuser").desc("HTTP Basic Auth user").build());
		options.addOption(Option.builder("p").required(true).hasArg().longOpt("httppassword").desc("HTTP Basic Auth password").build());
		options.addOption(Option.builder("d").required(false).hasArg().longOpt("dev").desc("Perun development instance infix (makub,zlamal,etc.)").build());

		options.addOption(Option.builder("id").required(true).hasArg().longOpt("member").desc("member id").build());
		CommandLine commandLine;
		try {
			commandLine = new DefaultParser().parse(options, args);
		} catch (MissingOptionException ex) {
			new HelpFormatter().printHelp( GetRichMember.class.getSimpleName(), options );
			System.exit(1);
			return;
		}
		String username = commandLine.getOptionValue("u");
		String password = commandLine.getOptionValue("p");
		String dev = commandLine.hasOption("d") ? "-"+commandLine.getOptionValue("d") : "";

		//prepare basic auth
		List<ClientHttpRequestInterceptor> interceptors = Collections.singletonList(new BasicAuthorizationInterceptor(username, password));
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(new InterceptingClientHttpRequestFactory(restTemplate.getRequestFactory(),interceptors));

		//make call
		Map<String, Object> map = new LinkedHashMap<>();

		map.put("id", commandLine.getOptionValue("id"));

		try {
			JsonNode jsonNode = restTemplate.postForObject("https://perun-dev.meta.zcu.cz/krb/rpc" + dev + "/json/membersManager/getRichMember", map, JsonNode.class);
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
			log.error("HTTP ERROR "+ex.getRawStatusCode()+" Content-Type: "+ contentType);
			if("json".equals(contentType.getSubtype())) {
				log.error(new ObjectMapper().readValue(body, JsonNode.class).path("message").asText());
			} else {
				log.error(ex.getMessage());
			}
		}
	}
}