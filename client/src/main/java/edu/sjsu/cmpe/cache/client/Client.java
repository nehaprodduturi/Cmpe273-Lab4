package edu.sjsu.cmpe.cache.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Client {
	public static void main(String[] args) throws Exception {
        System.out.println("Starting Cache Client...");
        
        CacheServiceInterface cache1 = new DistributedCacheService(
                "http://localhost:3000");
        CacheServiceInterface cache2 = new DistributedCacheService(
                "http://localhost:3001");
        CacheServiceInterface cache3 = new DistributedCacheService(
                "http://localhost:3002");
        
        //Implementing Write
        
        System.out.println("Http put 1=>a");
        cache1.put(1, "a");
        cache2.put(1, "a");
        cache3.put(1, "a");
        
        try {
            Thread.sleep(3000);                 
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        
        if(DistributedCacheService.completedUrlsCount <2) {
            System.out.println("rollback");
            rollBack(DistributedCacheService.map);
        }
        else {
            System.out.println("No rollback needed"); 
        
        //Implementing Read-Repair
        
        System.out.println("waiting for 30s for bringing down A");
        try {
            Thread.sleep(30000);                 
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("Http put update 1=>b");
        cache2.put(1, "b");
        cache3.put(1, "b");
        
        System.out.println("waiting for 30s for bringing up A");
        try {
            Thread.sleep(30000);                
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("Getting after server Down & Up");
        String value1 = cache1.get(1);
        System.out.println("get(1) after down=> " + value1);

        String value2 = cache2.get(1);
        System.out.println("get(2) after down=> " + value2);

        String value3 = cache3.get(1);
        System.out.println("get(3) after down=> " + value3);
        
      //sleep for 3 sec
        try {
            Thread.sleep(3000);                 //1000 milliseconds is one second.
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        
        //Checking for Inconsistencies and Performing Repair
        
        if(value1!= null && value1.equals(value2)){
            cache3.put(1,value1);
        }else if(value1!= null && value1.equals(value3)){
            cache2.put(1,value1);
        }else {
            cache1.put(1, value2);
        }
 
      //sleep for 3 sec
        try {
            Thread.sleep(3000);                 
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("Getting after Read-Repair");
        String val1 = cache1.get(1);
        System.out.println("get(1) after read repair=> " + val1);

        String val2 = cache2.get(1);
        System.out.println("get(2) after read repair=> " + val2);

        String val3 = cache3.get(1);
        System.out.println("get(3) after read repair=> " + val3);
 }
        System.out.println("Existing Cache Client...");
	}
	
	public static void rollBack(HashMap map)
    {
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            CacheServiceInterface cache = new DistributedCacheService(
                    pairs.getKey().toString());
            cache.delete((Long) pairs.getValue());
        }
    }
}

