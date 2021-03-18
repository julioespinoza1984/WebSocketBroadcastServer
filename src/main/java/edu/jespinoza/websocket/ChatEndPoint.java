package edu.jespinoza.websocket;

import edu.jespinoza.model.Message;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint(value = "/chat/{username}", decoders = MessageDecoder.class, encoders = MessageEncoder.class)
public class ChatEndPoint {
    private Session session;
    private static final Set<ChatEndPoint> chatEndPoints = new CopyOnWriteArraySet<>();
    private static Map<String, String> users = new HashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) {
        this.session = session;
        chatEndPoints.add(this);
        users.put(session.getId(), username);

        Message message = new Message();
        message.setFrom(username);
        message.setContent("Connected!");
        broadcast(message);
    }

    @OnMessage
    public void onMessage(Session session, Message message) {
        message.setFrom(users.get(session.getId()));
        broadcast(message);
    }

    @OnClose
    public void onClose(Session session) {
        chatEndPoints.remove(this);
        Message message = new Message();
        message.setFrom(users.get(session.getId()));
        message.setContent("Disconnected!");
        broadcast(message);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        throwable.printStackTrace();
        // Do error handling here
    }

    private static void broadcast(Message message) {
        chatEndPoints.forEach(endpoint -> {
            synchronized (endpoint) {
                try {
                    endpoint.session.getBasicRemote().sendObject(message);
                } catch (IOException | EncodeException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
