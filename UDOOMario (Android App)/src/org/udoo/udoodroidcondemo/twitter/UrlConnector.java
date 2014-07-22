package org.udoo.udoodroidcondemo.twitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import android.util.Base64;

public class UrlConnector {
    private HttpURLConnection mConnector;

    public UrlConnector(String encodedUrl) throws MalformedURLException, IOException {
        URL url = new URL(encodedUrl);
        mConnector = (HttpURLConnection) url.openConnection();
        mConnector.setReadTimeout(10000);
        mConnector.setConnectTimeout(15000);
        mConnector.setUseCaches(false);
    }

    public void addHeader(String header, String content) {
        mConnector.setRequestProperty(header, content);
    }

    public int get() throws ProtocolException, IOException {
        mConnector.setRequestMethod("GET");
        mConnector.setDoInput(true);

        return mConnector.getResponseCode();
    }

    public String getResponse() throws IOException {
        BufferedReader readerBuffer = new BufferedReader(new InputStreamReader(mConnector.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = readerBuffer.readLine()) != null) {
            response.append(line);
        }

        return response.toString();
    }

    public void disconnect() {
        mConnector.disconnect();
    }
    
    public int post(String parameters) throws ProtocolException, IOException {
         mConnector.setRequestMethod("POST");
         mConnector.setDoOutput(true);
         mConnector.setFixedLengthStreamingMode(parameters.getBytes().length);
 
         // Make the post
         PrintWriter output = new PrintWriter(mConnector.getOutputStream());
         output.print(parameters);
         output.close();
 
         return mConnector.getResponseCode();
    }
    
    public static String oAuth2TwitterEncoding(String consumerKey, String consumerSecret) {
	     String combinedKey = consumerKey + ":" + consumerSecret;
	 
	     return Base64.encodeToString(combinedKey.getBytes(), Base64.NO_WRAP);
    }
}
