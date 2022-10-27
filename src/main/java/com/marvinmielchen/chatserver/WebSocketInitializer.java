package com.marvinmielchen.chatserver;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolConfig;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;


public class WebSocketInitializer extends ChannelInitializer<SocketChannel>
{
    private final WebSocketServerListener listener;
    private final String webSocketPath;
    
    public WebSocketInitializer(WebSocketServerListener listener, String webSocketPath)
    {
        this.listener = listener;
        this.webSocketPath = webSocketPath;
    }
    
    @Override
    protected void initChannel(SocketChannel socketChannel)
    {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new WebSocketServerCompressionHandler());
        pipeline.addLast(new WebSocketServerProtocolHandler(WebSocketServerProtocolConfig.newBuilder().websocketPath(webSocketPath).dropPongFrames(false).allowExtensions(true).build()));
        pipeline.addLast(new WebSocketFrameHandler(listener));
    }
}
