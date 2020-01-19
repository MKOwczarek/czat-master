package czat.listener;

import czatl.message.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;
import czat.service.CzatService;

@Component
public class WebSocketEventListener {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @Autowired
    private CzatService gameService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        logger.info("Received a new web socket connection");
    }

    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String destination = headerAccessor.getDestination();
        if (destination.contains("public")){
            String username = headerAccessor.getUser().getName();
            String roomid = destination.split("/")[2];
            gameService.addUser(username, roomid);

        }

        if (destination.contains("table")){
            updateTable();
        }
    }

    @EventListener
    public void handleWebSocketUnsubscribeListener(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        if (headerAccessor.getSubscriptionId().contains("sub-4")){
            String username = (String) headerAccessor.getSessionAttributes().get("username");
            String roomId = (String) headerAccessor.getSessionAttributes().get("room_id");

            try{
            }catch (NullPointerException ignored){ }
            roomLeft(username, roomId);

            headerAccessor.getSessionAttributes().remove("room_id");

            updateTable();
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String username = (String) headerAccessor.getSessionAttributes().get("username");
        String roomId = (String) headerAccessor.getSessionAttributes().get("room_id");
        if (username != null) {
            logger.info("User Disconnected: " + username);
            roomLeft(username, roomId);

            if (roomId != null){
                try{
                    gameService.removeUser(username, roomId);
                }catch (NullPointerException e){
                    logger.info("get error " + e);
                }
            }

            updateTable();
        }
    }

    private void updateTable(){
        logger.info("update table");
        messagingTemplate.convertAndSend("/topic/table", new ChatMessage());
    }

    private void roomLeft(String username, String roomId){
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(ChatMessage.MessageType.LEAVE);
        chatMessage.setSender(username);
        messagingTemplate.convertAndSend(String.format("/topic/%s/public", roomId), chatMessage);
    }

}
