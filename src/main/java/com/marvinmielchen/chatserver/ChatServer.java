package com.marvinmielchen.chatserver;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.ArrayList;
import java.util.List;

public class ChatServer implements WebSocketServerListener
{
    private final WebSocketServer webSocketServer;
    private final List<Channel> channelList;
    
    public ChatServer(int port, String websocketPath)
    {
        webSocketServer = new WebSocketServer(port, websocketPath, this);
        channelList = new ArrayList<>();
    }
    
    @Override
    public void onOpen(Channel channel)
    {
        channelList.add(channel);
    }
    
    @Override
    public void onMessage(Channel channel, String text)
    {
        System.out.println(text.trim());
        for(Channel c : channelList){
            if(c != channel){
                c.writeAndFlush(new TextWebSocketFrame(text));
            }
        }
    }
    
    @Override
    public void onClose(Channel channel, String reasonText)
    {
        channelList.remove(channel);
    }
    
    public void start(){
        webSocketServer.start();
    }
    
    public static void main(String[] args)
    {
        ChatServer chatServer = new ChatServer(8080, "/ws");
        chatServer.start();
    }
}
