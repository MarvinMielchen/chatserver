package com.marvinmielchen.chatserver;

import io.netty.channel.Channel;

public interface WebSocketServerListener
{
    void onOpen(Channel channel);
    void onMessage(Channel channel, String text);
    void onClose(Channel channel, String reasonText);
}
