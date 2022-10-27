package com.marvinmielchen.chatserver;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame>{
    
    private final WebSocketServerListener listener;
    private long lastPongMillis;
    
    public WebSocketFrameHandler(WebSocketServerListener listener)
    {
        this.listener = listener;
        lastPongMillis = Long.MAX_VALUE;
    }
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame)
    {
        if(frame instanceof CloseWebSocketFrame){
            ChannelFuture future = ctx.channel().close();
            future.addListener((ChannelFutureListener) channelFuture -> listener.onClose(ctx.channel(), ((CloseWebSocketFrame) frame).reasonText()));
        } else if (frame instanceof TextWebSocketFrame)
        {
            listener.onMessage(ctx.channel(), ((TextWebSocketFrame) frame).text());
        } else if (frame instanceof PongWebSocketFrame)
        {
            lastPongMillis = System.currentTimeMillis();
        }
    }
    
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete)
        {
            listener.onOpen(ctx.channel());
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            Runnable task = () -> {
                ctx.channel().writeAndFlush(new PingWebSocketFrame());
                if(System.currentTimeMillis() - 1000 > lastPongMillis){
                    ChannelFuture future = ctx.channel().close();
                    future.addListener((ChannelFutureListener) channelFuture -> listener.onClose(ctx.channel(), "connection timeout"));
                    scheduler.shutdown();
                }
            };
            scheduler.scheduleAtFixedRate(task, 500, 500, TimeUnit.MILLISECONDS);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}