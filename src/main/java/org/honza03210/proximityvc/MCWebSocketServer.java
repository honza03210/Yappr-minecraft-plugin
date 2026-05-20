package org.honza03210.proximityvc;
// https://tootallnate.github.io/Java-WebSocket/

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MCWebSocketServer extends WebSocketServer {
    // the minecraft side of the plugin stores the most recent player positions here
    public Map<String, float[]> positions_map = new HashMap<>();
    // last minecraft position stored - no need to send it
    public Map<String, float[]> last_positions_map = new HashMap<>();
    // the current websocket connection for each player
    public Map<String, WebSocket> active_feeds = new HashMap<>();

    public MCWebSocketServer(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onStart() {
        System.out.println("WebSocket server started");
        setConnectionLostTimeout(60);
    }

    public void stopServer() {
        try {
            System.out.println("Stopping WebSocket server...");
            for (WebSocket conn : active_feeds.values()) {
                if (conn != null && conn.isOpen()) {
                    conn.close(1000, "Server shutting down");
                }
            }
            this.stop(); // stops the server thread
            System.out.println("WebSocket server stopped.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("Client connected: " + conn.getRemoteSocketAddress());
    }


    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Received: " + message);
        try {
            if (!message.startsWith("{")){
                return;
            }
        } catch (Exception e) {
            System.err.println("error:" + e);
        }
        JsonObject obj = JsonParser.parseString(message).getAsJsonObject();
        try {
            if (!positions_map.containsKey(obj.get("token").getAsString())) {
                conn.send("#InvalidToken");
            }
            active_feeds.put(obj.get("token").getAsString(), conn);
        } catch (Exception e) {
            conn.send("#InvalidToken");
        }
    }

    public String getPositionString(float[] position) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < position.length; i++) {
            if (i > 0) sb.append(';');
            sb.append(position[i]);
        }
        return sb.toString();
    }

    public void SendPositions() {
        // sending position to each of the connected users
        active_feeds.forEach((token, conn) -> {
            try {
                float[] pos = positions_map.get(token);
                if (Arrays.equals(pos, last_positions_map.get(token))) {
                    return;
                }
                last_positions_map.put(token, positions_map.get(token).clone());
                conn.send("mc;" + getPositionString(pos));
            } catch (Exception e) {
                conn.send("Error when sending position: " + e);
            }
        });
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Client disconnected");
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }
}
