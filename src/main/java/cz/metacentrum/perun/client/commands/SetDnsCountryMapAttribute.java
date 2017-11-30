package cz.metacentrum.perun.client.commands;

import com.fasterxml.jackson.databind.JsonNode;
import cz.metacentrum.perun.client.PerunApiClient;
import cz.metacentrum.perun.client.PerunCommand;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

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
	public String getCommandDescription() {
		return "Sets value for attribute urn:perun:entityless:attribute-def:def:dnsStateMapping";
	}


	@Override
	public void executeCommand(PerunApiClient.CommandContext ctx) {
		LinkedHashMap<String, Object> attrDef = getAttrDef(ctx);
		LinkedHashMap<String, String> tld2Country = loadCountries();
		for(Map.Entry<String,String> entry : tld2Country.entrySet()) {
			setAttrValue(ctx, attrDef, entry.getKey(),entry.getValue());
		}
	}

	private void getAttrValue(PerunApiClient.CommandContext ctx, LinkedHashMap<String, Object> attrDef, String tld) {
		Map<String, Object> params = new LinkedHashMap<>();
		params.put("attributeName", "urn:perun:entityless:attribute-def:def:dnsStateMapping");
		params.put("key", tld);
		JsonNode res = PerunApiClient.callPerunRpc(ctx, params, "/json/attributesManager/getAttribute");
		log.debug("got {}",res);
	}

	private void setAttrValue(PerunApiClient.CommandContext ctx, LinkedHashMap<String, Object> attrDef, String tld, String country) {
		log.debug("setting value {}={}",tld,country);
		Map<String, Object> params = new LinkedHashMap<>();
		attrDef.put("value",country);
		params.put("attribute", attrDef);
		params.put("key", tld);
		PerunApiClient.callPerunRpc(ctx, params, "/json/attributesManager/setAttribute");
	}


	private LinkedHashMap<String, String> loadCountries() {
		LinkedHashMap<String, String> dns2State = new LinkedHashMap<>();
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
				log.trace("adding {} => {}",tldName,countryName);
			}
		}
		//by hand
		dns2State.put(".cz","Czech Republic");
		dns2State.put("hostel.eduid.cz","");
		dns2State.put("github.extidp.cesnet.cz","");
		dns2State.put("google.extidp.cesnet.cz","");
		dns2State.put("facebook.extidp.cesnet.cz","");
		return dns2State;
	}

	private LinkedHashMap<String,Object> getAttrDef(PerunApiClient.CommandContext ctx) {
		//get attribute key
		log.debug("getting attribute id");
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("attributeName","urn:perun:entityless:attribute-def:def:dnsStateMapping");
		JsonNode attr = PerunApiClient.callPerunRpc(ctx, map, "/json/attributesManager/getAttributeDefinition");
		log.debug("attr: {}",attr);
		//attribute def
		LinkedHashMap<String,Object> atr = new LinkedHashMap<>();
		atr.put("id", attr.path("id").asInt());
		atr.put("namespace",attr.path("namespace").asText());
		atr.put("type",attr.path("type").asText());
		atr.put("friendlyName",attr.path("friendlyName").asText());
		return atr;
	}


}
