import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public class ApiServer {

    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/health", exchange -> {
            if(!"GET".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                return;
            }
            send(exchange, 200, "{\"status\": \"ok\"}", "application/json");
        });
          server.createContext("/echo", exchange -> {
            if (!"POST".equals(exchange.getRequestMethod())) {
                send(exchange, 405, "Method Not Allowed", "text/plain");
                return;
            }
                String body = readBody(exchange);
            // We’re not parsing JSON here (keeping it dependency-free),
            // just returning what the client sent.
            String response = "{\"you_sent\":" + safeJsonString(body) + "}";
            send(exchange, 200, response, "application/json");
        });

        server.start();
        System.out.println("API running on http://localhost:" + port);
        System.out.println("Try GET  /health");
        System.out.println("Try POST /echo");
    }

      private static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

     private static void send(HttpExchange exchange, int status, String body, String contentType) throws IOException {
        Headers h = exchange.getResponseHeaders();
        h.set("Content-Type", contentType + "; charset=utf-8");
        h.set("Access-Control-Allow-Origin", "*"); // dev-friendly; tighten later
        h.set("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
        h.set("Access-Control-Allow-Headers", "Content-Type");

        // Handle CORS preflight quickly
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

      private static String safeJsonString(String raw) {
        String escaped = raw
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
        return "\"" + escaped + "\"";
    }
}