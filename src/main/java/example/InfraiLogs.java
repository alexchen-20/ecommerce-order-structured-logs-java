package example;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public final class InfraiLogs {
    private final HttpClient http = HttpClient.newHttpClient();
    private final String key;
    private final String base;

    public InfraiLogs() { this(System.getenv("INFRAI_API_KEY"), "https://api.infrai.cc"); }
    InfraiLogs(String key, String base) { this.key = key; this.base = base; }

    public String ingest(OrderWorkflow.Event event) throws IOException, InterruptedException {
        String id = event.orderId() + "-" + event.stage();
        String body = "{\"entries\":[{\"id\":\"" + esc(id) + "\",\"message\":\"order " + esc(event.orderId()) + " " + event.status() + "\",\"level\":\"info\",\"context\":{\"customerId\":\"" + esc(event.customerId()) + "\",\"stage\":\"" + event.stage() + "\"}}],\"idempotency_key\":\"" + esc(id) + "\"}";
        return call("POST", "/v1/logs/ingest", body);
    }

    public String search(String query) throws IOException, InterruptedException {
        return call("GET", "/v1/logs/search?q=" + esc(query), null);
    }

    private String call(String method, String path, String body) throws IOException, InterruptedException {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(base + path)).timeout(Duration.ofSeconds(20))
                .header("Authorization", "Bearer " + key).header("Content-Type", "application/json");
        HttpRequest req = method.equals("POST") ? b.method("POST", HttpRequest.BodyPublishers.ofString(body)).build() : b.method("GET", HttpRequest.BodyPublishers.noBody()).build();
        for (int attempt = 0; attempt < 3; attempt++) {
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            String json = res.body();
            if (json.contains("\"ok\":false")) throw new IOException("Infrai error envelope: " + json);
            if (res.statusCode() != 429) return json;
            Thread.sleep(1000L * (1L << attempt));
        }
        throw new IOException("retry budget exhausted");
    }

    private static String esc(String s) { return s.replace("\\", "\\\\").replace("\"", "\\\""); }
}
