package org.udoo.udoodroidcondemo.twitter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.Arrays;
import java.util.List;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.google.gson.Gson;

public class TwitterReceiver {
    private final static String TAG_LOG = "TwitterReceiver";
    private final static String CONSUMER_KEY = "csd4NUgQ01WXYH2nG63qIfcM3";
    private final static String CONSUMER_SECRET = "NHeFB8ZsUpidtuXySdKv2ErevkFeJ2vVo9anrHbhfZyH0H1j5s";
    private final static String BEARER_PARAMETERS = "grant_type=client_credentials";


    private final static String TOKEN_ENDPOINT = "https://api.twitter.com/oauth2/token";
    private final static String TIMELINE_API = "https://api.twitter.com/1.1/statuses/user_timeline.json?";
    private final static String SEARCH_API = "https://api.twitter.com/1.1/search/tweets.json?";

    private String bearerToken = null;
    
    public JSONArray getTwitterStream(String username, String lastId) {
        JSONArray twitterTimeline = null;

        // In a production environment remember to store it in SharedPreferences instead in memory
        // as stated here: http://developer.android.com/guide/topics/data/data-storage.html#pref-        
        if (bearerToken == null) {
            bearerToken = getBearerToken();
        }

        if (bearerToken != null) {
            twitterTimeline = getTwitterTimeline(username, lastId);
        }

        return twitterTimeline;
    }

    public Twitter searchMentions(String username, String lastId) throws IOException {
        JSONArray twitterMentions = null;

        // In a production environment remember to store it in SharedPreferences instead in memory
        // as stated here: http://developer.android.com/guide/topics/data/data-storage.html#pref
        if (bearerToken == null) {
            bearerToken = getBearerToken();
        }

        if (bearerToken != null) {
            twitterMentions = getTwitterMentions(username, lastId);
        }

        return jsonToTwitter(twitterMentions.toString());
    }

    private String getBearerToken() {
        String bearerToken = null;

        try {
            // Encodes consumer key and secret
            String basicAuthorization = UrlConnector.oAuth2TwitterEncoding(CONSUMER_KEY, CONSUMER_SECRET);

            // Requires Bearer token
            UrlConnector bearerConnector = new UrlConnector(TOKEN_ENDPOINT);
            bearerConnector.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            bearerConnector.addHeader("Authorization", "Basic " + basicAuthorization);

            // Do POST and grab access_token
            int statusCode = bearerConnector.post(BEARER_PARAMETERS);

            if (statusCode == HttpURLConnection.HTTP_OK) {
                bearerToken = new JSONObject(bearerConnector.getResponse()).getString("access_token");
            }

            bearerConnector.disconnect();

        } catch (UnsupportedEncodingException e) {
            Log.e(TAG_LOG, e.getMessage());
        } catch (IllegalStateException e) {
            Log.e(TAG_LOG, e.getMessage());
        } catch (MalformedURLException e) {
            Log.e(TAG_LOG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG_LOG, e.getMessage());
        } catch (JSONException e) {
            Log.e(TAG_LOG, e.getMessage());
        }

        return bearerToken;
    }

    private JSONArray getTwitterTimeline(String username, String lastId) {
        JSONArray results = null;


        try {
            // Requires Twitter timeline
            UrlConnector twitterConnector = new UrlConnector(timelineWithFilters(username, lastId));
            twitterConnector.addHeader("Content-Type", "application/json");
            twitterConnector.addHeader("Authorization", "Bearer " + bearerToken);

            // Do GET and grab tweets into a JSONArray
            int statusCode = twitterConnector.get();

            if (statusCode == HttpURLConnection.HTTP_OK) {
                results = new JSONArray(twitterConnector.getResponse());
            }

        } catch (UnsupportedEncodingException e) {
            Log.e(TAG_LOG, e.getMessage());
        } catch (IllegalStateException e) {
            Log.e(TAG_LOG, e.getMessage());
        } catch (MalformedURLException e) {
            Log.e(TAG_LOG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG_LOG, e.getMessage());
        } catch (JSONException e) {
            Log.e(TAG_LOG, e.getMessage());
        }

        return results;
    }

    private JSONArray getTwitterMentions(String username, String lastId)  {
        JSONArray results = null;

        try {
            // Requires Twitter timeline
            UrlConnector twitterConnector = new UrlConnector(searchWithFilters(username, lastId));
            twitterConnector.addHeader("Content-Type", "application/json");
            twitterConnector.addHeader("Authorization", "Bearer " + bearerToken);

            // Do GET and grab tweets into a JSONArray
            int statusCode = 0;
			try {
				statusCode = twitterConnector.get();
			} catch (ProtocolException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

            Log.d(TAG_LOG, String.valueOf(statusCode));

            if (statusCode == HttpURLConnection.HTTP_OK) {
                try {
					results = new JSONObject(twitterConnector.getResponse()).getJSONArray("statuses");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }

        } catch (UnsupportedEncodingException e) {
            Log.e(TAG_LOG, e.getMessage());
            Log.e(TAG_LOG, e.getMessage());
        } catch (MalformedURLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} 

        return results;
    }

    private String timelineWithFilters(String username, String lastId) {
        List<BasicNameValuePair> parameters = Arrays.asList(
                new BasicNameValuePair("screen_name", username),
                new BasicNameValuePair("since_id", lastId));

        return TIMELINE_API + URLEncodedUtils.format(parameters, "UTF-8");
    }

    private String searchWithFilters(String username, String lastId) {
        List<BasicNameValuePair> parameters = Arrays.asList(
                new BasicNameValuePair("q", "@" + username),
                new BasicNameValuePair("since_id", lastId));

        return SEARCH_API + URLEncodedUtils.format(parameters, "UTF-8");
    }
    
 	// converts a string of JSON data into a Twitter object
 	private Twitter jsonToTwitter(String result) {
 		Twitter twits = null;
 		if (result != null && result.length() > 0) {
 			try {
 				Gson gson = new Gson();
 				twits = gson.fromJson(result, Twitter.class);
 			} catch (IllegalStateException ex) {
 				// just eat the exception
 			}
 		}
 		return twits;
 	}	
}