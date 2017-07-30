package com.github.apls777.photofeed;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;

class CustomHttpClient {

    private static final int HTTP_TIMEOUT = 30 * 1000; // milliseconds
	private static HttpClient mHttpClient;

    private static HttpClient getHttpClient() {
        if (mHttpClient == null) {
            mHttpClient = new DefaultHttpClient();

            final HttpParams params = mHttpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(params, HTTP_TIMEOUT);
            HttpConnectionParams.setSoTimeout(params, HTTP_TIMEOUT);
            ConnManagerParams.setTimeout(params, HTTP_TIMEOUT);
        }

        return mHttpClient;
    }

    /**
     * Performs an HTTP Post request to the specified url with the specified
     * parameters.
     *
     * @param url
     *            The web address to post the request to
     * @param postParameters
     *            The parameters to send via the request
     * @return The result of the request
     */
 	public static String executeHttpPost(String url,ArrayList<NameValuePair> postParameters) throws Exception {
        BufferedReader in = null;

        try {
            HttpClient client = getHttpClient();
            HttpPost request = new HttpPost(url);
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(postParameters);

            request.setEntity(formEntity);

            HttpResponse response = client.execute(request);
            in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            StringBuilder sb = new StringBuilder("");
            String line = "";
            String NL = System.getProperty("line.separator");

            while ((line = in.readLine()) != null) {
                sb.append(line).append(NL);
            }

            in.close();

            return sb.toString();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e("log_tag", "Error converting result " + e.toString());
                    e.printStackTrace();
                }
            }
        }
    }
 
    /**
     * Performs an HTTP GET request to the specified url.
     *
     * @param url
     *            The web address to post the request to
     * @return The result of the request
     */
    static String executeHttpGet(String url) throws Exception {
        BufferedReader in = null;
        try {
            HttpClient client = getHttpClient();
            HttpGet request = new HttpGet();
            request.setURI(new URI(url));

            HttpResponse response = client.execute(request);
            in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            StringBuilder sb = new StringBuilder("");
            String line = "";
            String NL = System.getProperty("line.separator");

            while ((line = in.readLine()) != null) {
                sb.append(line).append(NL);
            }

            in.close();

            return sb.toString();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e("log_tag", "Error converting result " + e.toString());
                    e.printStackTrace();
                }
            }
        }
    }
}
