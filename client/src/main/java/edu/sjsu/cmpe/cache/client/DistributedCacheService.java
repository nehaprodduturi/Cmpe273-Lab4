package edu.sjsu.cmpe.cache.client;

import com.mashape.unirest.http.Headers;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;

import java.io.InputStream;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;


/**
 * Distributed cache service
 * 
 */
public class DistributedCacheService implements CacheServiceInterface {
    private final String cacheServerUrl;
    public static int completedUrlsCount=0;
    String value = null;
    public static HashMap<String, Long> map = new HashMap<String, Long>();

    public static HashMap<String, String> urlKeyValueMap = new HashMap<String, String>();

    public DistributedCacheService(String serverUrl) {
        this.cacheServerUrl = serverUrl;
    }

    /**
     * @see edu.sjsu.cmpe.cache.client.CacheServiceInterface#get(long)
     */
    
    @Override
    public String get(long key) {
        System.out.println("In new get method");
        Future<HttpResponse<JsonNode>> future =null;
        final String url = this.cacheServerUrl;
        try {
            final long key1 = key;
            future = Unirest
                    .get(this.cacheServerUrl + "/cache/{key}")
                    .header("accept", "application/json")
                    .routeParam("key", Long.toString(key))
                    .asJsonAsync(new Callback<JsonNode>() {

                        public void failed(UnirestException e) {
                            System.out.println("The request has failed");
                        }

                        public void completed(HttpResponse<JsonNode> response) {
                        	value = null;
                        	try {
                            int code = response.getCode();
                            value = response.getBody().getObject().getString("value");
                        	}
                        	catch(Exception e) {
                        		System.out.println("Exception");
                        	}
                        }

                        public void cancelled() {
                            System.out.println("The request has been cancelled");
                        }

                    });
        } catch (Exception e) {
            System.err.println(e);
        }

        //sleep for 3 sec
        System.out.println("sleeping..........");
        try {
            Thread.sleep(3000);                 //1000 milliseconds is one second.
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        return value;
    }

    /**
     * @see edu.sjsu.cmpe.cache.client.CacheServiceInterface#put(long,
     *      java.lang.String)
     */

    public void put(long key, String value){
        System.out.println("In new put method");
        final String url = this.cacheServerUrl;
        Future<HttpResponse<JsonNode>> future = null;
        try {
        	final long key1 = key;
            future = Unirest
                    .put(this.cacheServerUrl + "/cache/{key}/{value}")
                    .header("accept", "application/json")
                    .routeParam("key", Long.toString(key))
                    .routeParam("value", value)
                    .asJsonAsync(new Callback<JsonNode>() {

                        public void failed(UnirestException e) {
                            System.out.println("The request has failed");
                        }

                        public void completed(HttpResponse<JsonNode> response) {
                        	map.put(url, key1);
                            completedUrlsCount++;
                            int code = response.getCode();
                            String val = response.getBody().getObject().getString("value");
                        }

                        public void cancelled() {
                            System.out.println("The request has been cancelled");
                        }
                    });
        }
        catch(Exception e) {
        }
    }

    public void delete(long key)
    {
        HttpResponse<JsonNode> response = null;
        try {
                response  = Unirest.delete(this.cacheServerUrl+"/cache/{key}")
                    .header("accept", "application/json")
                    .routeParam("key", Long.toString(key))
                    .asJson();
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        System.out.println("Response code after deletion "+ response.getCode());
    }
}
