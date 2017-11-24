package cz.metacentrum.perun.client.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cz.metacentrum.perun.client.PerunApiClient;
import cz.metacentrum.perun.client.PerunCommand;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Sets value for attribute urn:perun:entityless:attribute-def:def:dnsStateMapping by parsing public list of countries.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class SetDnsCountryMapAttribute extends PerunCommand {

	private final static Logger log = LoggerFactory.getLogger(SetDnsCountryMapAttribute.class);

	@Override
	public void addOptions(Options options) {
	}

	@Override
	public void addParameters(PerunApiClient.RpcCallsContext ctx, Map<String, Object> params, CommandLine commandLine) {
		//get attribute key
		log.debug("getting attribute id");
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("attributeName","urn:perun:entityless:attribute-def:def:dnsStateMapping");
		JsonNode attr = PerunApiClient.callPerunRpc(ctx.getRestTemplate(), map, ctx.getPerunUrl() + "/json/attributesManager/getAttributeDefinition");
		log.debug("attr: {}",attr);
		LinkedHashMap<String,Object> atr = new LinkedHashMap<>();

		//parameter attribute
		atr.put("id", attr.path("id").asInt());
		atr.put("namespace",attr.path("namespace").asText());
		atr.put("type",attr.path("type").asText());
		atr.put("friendlyName",attr.path("friendlyName").asText());
		params.put("attribute", atr);
		//atribute value
		LinkedHashMap<String,Object> dns2State = new LinkedHashMap<>();
		loadCountries(ctx,dns2State);
		atr.put("value",dns2State);
		//parameter key - constant
		params.put("key", "config");
	}

	private void loadCountries(PerunApiClient.RpcCallsContext ctx, LinkedHashMap<String, Object> dns2State) {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getInterceptors().add((request, body, execution) -> {
			ClientHttpResponse response = execution.execute(request,body);
			response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
			return response;
		});
		JsonNode countries = restTemplate.getForObject("https://raw.githubusercontent.com/mledoze/countries/master/countries.json", JsonNode.class);
		for(JsonNode country : countries) {
			String countryName = country.path("name").path("common").asText();
			for(JsonNode tld : country.path("tld")) {
				String tldName = tld.asText();
				dns2State.put(tldName,countryName);
				log.info("adding {} => {}",tldName,countryName);
			}
		}
		//by hand
		dns2State.put(".cz","Czech Republic");
		dns2State.put("hostel.eduid.cz","");
		dns2State.put("github.extidp.cesnet.cz","");
		dns2State.put("google.extidp.cesnet.cz","");
		dns2State.put("facebook.extidp.cesnet.cz","");
	}

	@Override
	public String getCommandDescription() {
		return "Sets value for attribute urn:perun:entityless:attribute-def:def:dnsStateMapping";
	}

	@Override
	public String getUrlPart(CommandLine commandLine) {
		return "/json/attributesManager/setAttribute";
	}

	@Override
	public void processResponse(JsonNode resp) {
		System.out.println("Done ");
	}
}
