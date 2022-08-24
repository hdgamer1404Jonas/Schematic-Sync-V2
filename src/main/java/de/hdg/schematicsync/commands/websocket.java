package de.hdg.schematicsync.commands;


import javax.websocket.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static javax.websocket.ContainerProvider.getWebSocketContainer;

public class websocket extends Endpoint {

    String url;
    Session session;
    TextArea textArea;

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;

        session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
                textArea.append("Recieved: " + message + "\n");
            }
        });
    }

    @Override
    public void onError(Session session, Throwable t) {
        super.onError(session, t);
    }

    public void connect() throws DeploymentException, IOException, URISyntaxException {
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();

        try {
            Thread.currentThread().setContextClassLoader(websocket.class.getClassLoader());

            WebSocketContainer container = getWebSocketContainer();

            ClientEndpointConfig config = ClientEndpointConfig.Builder.create()
                    .configurator(new ClientEndpointConfig.Configurator())
                    .build();

            container.connectToServer(this, config, new URI(url));


        }finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

    public websocket(String url, TextArea textArea) {
        super();
        this.url = url;
        this.textArea = textArea;
    }

    public void send_msg(String msg) {
        session.getAsyncRemote().sendText(msg);
    }
}
