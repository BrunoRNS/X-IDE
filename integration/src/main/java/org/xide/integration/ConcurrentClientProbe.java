package org.xide.integration;

import org.xide.client.ClientConnection;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

public final class ConcurrentClientProbe {
    private ConcurrentClientProbe() {}
    public static void main(String[] args) throws Exception {
        String url=CommunicationProbe.arg(args,"--url","wss://localhost:8443/ws");
        String user=CommunicationProbe.arg(args,"--user",null), password=CommunicationProbe.arg(args,"--password",null), cert=CommunicationProbe.arg(args,"--trust-cert",null);
        int count=Integer.parseInt(CommunicationProbe.arg(args,"--count","5"));
        if(user==null||password==null) throw new IllegalArgumentException("--user and --password are required");
        if(count<1||count>500) throw new IllegalArgumentException("Client count must be between 1 and 500");
        ExecutorService pool=Executors.newFixedThreadPool(Math.min(count,50)); List<Future<Boolean>> results=new ArrayList<>();
        try {
            for(int i=0;i<count;i++) { final int index=i; results.add(pool.submit(()->{
                String id="load-"+index+"-"+UUID.randomUUID();
                try(var client=new ClientConnection()) {
                    client.connect(URI.create(url),user,password.toCharArray(),cert==null?null:Path.of(cert));
                    var response=client.ping(id,"concurrent-client-"+index,15);
                    if(!"PONG".equals(response.type())||!id.equals(response.requestId())) throw new IllegalStateException("Unexpected response: "+response);
                    System.out.println("CLIENT_OK "+index+" "+id); return true;
                }
            })); }
            int passed=0; for(Future<Boolean> result:results) if(result.get(30,TimeUnit.SECONDS)) passed++;
            System.out.printf("CONCURRENT_TEST_RESULT passed=%d total=%d%n",passed,count);
            if(passed!=count) throw new IllegalStateException("One or more client sessions failed");
        } finally { pool.shutdownNow(); }
    }
}
