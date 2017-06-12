
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class ImplTest {

	String validTn;
	String validCustGuid;
	JSONObject obj = null;
	private String jsonFileName;
	private static final Logger LOGGER = LoggerFactory.getLogger(ImplTest.class);

	public void invokePayloadJson(String jsonFileName) {

		try {
			Resource resource = new ClassPathResource("payload/" + jsonFileName);
			String requestBody = IOUtils.toString(resource.getInputStream(), "UTF-8");
			obj = new JSONObject(requestBody);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void sort_voicemasBytimestamp() throws Exception {
		jsonFileName = "VoiceMailBox.json";
		invokePayloadJson(jsonFileName);
		JSONArray voicemails  = obj.getJSONArray("voicemails");
		
		
		JSONArray readvoicemails = new JSONArray();
		JSONArray unreadvoicemails = new JSONArray();
		JSONArray purgedvoicemails = new JSONArray();
		JSONArray sortedVoiceMails = new JSONArray();
		
		for (int i = 0 ; i < voicemails.length() ; i++ ){
			
			if(voicemails.getJSONObject(i).getJSONObject("metadata").get("status").toString().equals("read")){
				readvoicemails.put(voicemails.getJSONObject(i));
			} else if(voicemails.getJSONObject(i).getJSONObject("metadata").get("status").toString().equals("unread")) {
				unreadvoicemails.put(voicemails.getJSONObject(i));
			} else if(voicemails.getJSONObject(i).getJSONObject("metadata").get("status").toString().equals("purged")){
				purgedvoicemails.put(voicemails.getJSONObject(i));
			}
		}
		
	// voicemails sorted in ascending order
		sortedVoiceMails = concatArray(sortVoiceMailsAscOrDesc("asc" , purgedvoicemails), 
				sortVoiceMailsAscOrDesc("asc" , readvoicemails), 
				sortVoiceMailsAscOrDesc("asc" , unreadvoicemails));
		obj.put("voicemails", sortedVoiceMails);
		System.out.println(obj);
		
		
		// voicemails sorted in descending order
		sortedVoiceMails = concatArray(sortVoiceMailsAscOrDesc("desc" , purgedvoicemails), 
				sortVoiceMailsAscOrDesc("desc" , readvoicemails), 
				sortVoiceMailsAscOrDesc("desc" , unreadvoicemails));
		obj.put("voicemails", sortedVoiceMails);
		System.out.println(obj);
	}
	
	
	/**
	 * @param sortType ("asc" or "desc")
	 * @param voicemails ( of type read / unread / purged )
	 * @return sorted voicemails  ( of type read / unread / purged )
	 * @throws JSONException
	 */
	// Method that sorts an passed in JSONArray of voicemails and the condition of asc / desc order 
	JSONArray sortVoiceMailsAscOrDesc(String sortType, JSONArray voicemails) throws JSONException {
		Map<String,JSONObject> postedTimeStamps = null;
		JSONArray sortedVoicemails = new JSONArray();
		
		// form the sorting key based on sortType input
		if(sortType.equals("asc")) {
			postedTimeStamps = new TreeMap<String,JSONObject>();
			
		} else if(sortType.equals("desc")) {
			postedTimeStamps = new TreeMap<String,JSONObject>(Collections.reverseOrder());
		}
		
		
		// loop voicemails by the type passed and create a treemap with timePosted -> "voicemails"
		for (int i = 0 ; i < voicemails.length() ; i++ ){
			String key = voicemails.getJSONObject(i).getJSONObject("metadata").get("timePosted").toString();
			JSONObject value = voicemails.getJSONObject(i);
			postedTimeStamps.put(key, value);
		}

		// loop through the sorted treemap and  construct the sortedVoicemails array
		for (Map.Entry<String,JSONObject> entry : postedTimeStamps.entrySet()) {
		    sortedVoicemails.put(entry.getValue());
		}
		return sortedVoicemails;
	}
	
	/**
	 * @param (all the json arrays that needs to be concatenated )
	 * @return ( concatenated json array )
	 * @throws JSONException
	 */
	// method used to concatenate multiple json arrays into one single array
	private JSONArray concatArray(JSONArray... arrs)  throws JSONException {
	    JSONArray result = new JSONArray();
	    for (JSONArray arr : arrs) {
	        for (int i = 0; i < arr.length(); i++) {
	            result.put(arr.get(i));
	        }
	    }
	    return result;
	}

}
