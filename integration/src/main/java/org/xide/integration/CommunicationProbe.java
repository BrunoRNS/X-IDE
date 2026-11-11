package org.xide.integration;
import org.xide.client.ClientConnection;
import java.net.URI;
import java.nio.file.Path;
import java.util.UUID;
public final class CommunicationProbe {
    private CommunicationProbe() {}
    public static void main(String[] args) throws Exception {
        String url=arg(args,"--url","wss://localhost:8443/ws"), user=arg(args,"--user",null), password=arg(args,"--password",null), cert=arg(args,"--trust-cert",null);
        if(user==null||password==null) throw new IllegalArgumentException("--user and --password are required");
        String id="probe-"+UUID.randomUUID();
        try(var client=new ClientConnection()) {
            client.connect(URI.create(url),user,password.toCharArray(),cert==null?null:Path.of(cert));
            var response=client.ping(id,"communication-test",10);
            if(!"PONG".equals(response.type())||!id.equals(response.requestId())) throw new IllegalStateException("Communication test failed: "+response);
            System.out.println("COMMUNICATION_OK "+response);
        }
    }
    static String arg(String[] args,String key,String fallback) { for(int i=0;i<args.length-1;i++) if(key.equals(args[i])) return args[i+1]; return fallback; }
}
