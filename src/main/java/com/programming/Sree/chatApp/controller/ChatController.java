package com.programming.Sree.chatApp.controller;

import com.programming.Sree.chatApp.dto.ChatMessage;
import com.programming.Sree.chatApp.dto.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final RedisTemplate redisTemplate;

    //Send message to clients
    @MessageMapping("/chat.send")
    public ChatMessage sendChatMessage(@Payload ChatMessage chatMessage){
        chatMessage.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        //Add logic to send message to DragonFly DB Queue
        redisTemplate.convertAndSend("chat", chatMessage);
        return chatMessage;
    }

    //add user to the  application
    @MessageMapping("/chat.adduser")
    public ChatMessage addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor)
    {
        //Get the username from the chatMessage object and add it to the Websocket session
        headerAccessor.getSessionAttributes().put("username", chatMessage.getUserName());
        chatMessage.setMessageType(MessageType.JOIN);
        chatMessage.setMessage(chatMessage.getUserName() + "joined the chat");
        chatMessage.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        log.info("User Joined: {} ", chatMessage.getUserName());

        //Send the chat message back to the clients with Message type as JOIN
        redisTemplate.convertAndSend("chat", chatMessage);
        return  chatMessage;
    }
}
