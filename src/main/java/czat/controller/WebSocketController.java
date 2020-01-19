package czat.controller;

import czatl.message.ChatMessage;
import czatl.message.WordMessage;
import czatl.message.GuessMessage;
import czatl.message.DrawMessage;
import czat.model.Room;
import czat.room.RoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import czat.service.CzatService;

@Controller
public class WebSocketController {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @Autowired
    private CzatService gameService;

    @MessageMapping("/chat/{roomId}/chatMessage")
    public void getChatMessage(@DestinationVariable String roomId, @Payload ChatMessage chatMessage){
        String message = chatMessage.getContent();
        String sender = chatMessage.getSender();
        logger.info("received chat message '" + message + "' from '" + sender + "' in room '" + roomId + "'");
            sendMessage(roomId, chatMessage);
        
    }

    private void sendMessage(String roomId, ChatMessage chatMessage){
        String message = chatMessage.getContent();
        logger.info("send message '" + message + "' to the room '" + roomId + "'");
        messagingTemplate.convertAndSend(String.format("/topic/%s/public", roomId), chatMessage);
    }

    @MessageMapping("/chat/{roomId}/addUser")
    public void addUser(@DestinationVariable String roomId, @Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {

        if (headerAccessor.getSessionAttributes().get("room_id") == null){

            String currentRoomId = (String) headerAccessor.getSessionAttributes().put("room_id", roomId);

            if (currentRoomId != null) {
                ChatMessage leaveMessage = new ChatMessage();
                leaveMessage.setType(ChatMessage.MessageType.LEAVE);
                leaveMessage.setSender(chatMessage.getSender());
                messagingTemplate.convertAndSend(String.format("/topic/%s/public", currentRoomId), leaveMessage);
            }

            headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
            messagingTemplate.convertAndSend(String.format("/topic/%s/public", roomId), chatMessage);

            messagingTemplate.convertAndSend("/topic/table", new ChatMessage());
        }
    }

    @MessageMapping("/chat/{roomId}/changeGuess")
    public void changeGuess(@DestinationVariable String roomId, @Payload WordMessage wordMessage){

        String word = wordMessage.getWord();

        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < word.length(); i++) {
            list.add(i);
        }
        Collections.shuffle(list);

        String result = word + "#";
        StringBuilder sb = new StringBuilder(result);
        for (Integer integer : list) {
            sb.append(integer);
        }

        gameService.setGuess(roomId, word);

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(sb.toString());

        messagingTemplate.convertAndSend(String.format("/topic/%s/changeGuess", roomId), chatMessage);
    }

}
