package org.xide.client;

import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;

public final class ClientMain {
    private ClientMain() {}
    public static void main(String[] args) throws Exception {
        String url=option(args,"--url","wss://localhost:8443/ws");
        String message=option(args,"--message","hello from X-IDE");
        String user=requiredOption(args,"--user"), password=requiredOption(args,"--password");
        String trust=option(args,"--trust-cert",null);
        char[] secret=password.toCharArray();
        try(var client=new ClientConnection()) {
            client.connect(URI.create(url),user,secret,trust==null?null:Path.of(trust));
            var response=client.ping(UUID.randomUUID().toString(),message,10);
            if(!"PONG".equals(response.type())) throw new IllegalStateException("Unexpected response: "+response);
            System.out.printf("Server replied: %s (%s): %s%n",response.type(),response.requestId(),response.payload());
        } finally { Arrays.fill(secret,'\0'); }
    }
    private static String requiredOption(String[] args,String key) {
        String value=option(args,key,null);
        if(value==null||value.isBlank()) throw new IllegalArgumentException("Required option: "+key);
        return value;
    }
    private static String option(String[] args,String key,String fallback) {
        for(int i=0;i<args.length-1;i++) if(key.equals(args[i])) return args[i+1];
        return fallback;
    }
}
