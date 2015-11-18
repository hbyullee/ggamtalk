package com.soma.ggamtalk.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


public class MyVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) {

        addCustomEvent();

        System.out.println("MyVerticle loaded");
    }

    private void addCustomEvent()
    {
        // 타 버티클에서 접속자에게 실시간 뉴스피드 전송
        vertx.eventBus().consumer("to.MonitorVerticle.test", new Handler<Message<String>>() {
            @Override
            public void handle(Message<String> objectMessage) {
                System.out.println("cluster:" + objectMessage.body());
                objectMessage.reply("complete cluster no33");
            }
        });

    }



    @Override
    public void stop(Future stopFuture) throws Exception {
        System.out.println("MyVerticle stopped!");
    }

}