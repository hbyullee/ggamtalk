package com.soma.ggamtalk.verticle;

import io.vertx.core.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.*;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.*;

import java.lang.Object;
import java.lang.Override;

/**
 * Created by jwy on 2015. 8. 27..
 */
public class SockJSVerticle extends AbstractVerticle {


    int count;
    @Override
    public void start() throws Exception {

        Router router = Router.router(vertx);

        createHttpSvr(router);
    }

    private void createHttpSvr(Router router) {
        vertx.deployVerticle(new RestVerticle());
    	
//        StaticHandler sHandler = StaticHandler.create("./www");
//        sHandler.setCachingEnabled(false);
//        router.route().handler(sHandler);
//
//        vertx.createHttpServer().requestHandler(router::accept).listen(9010);
//        vertx.createHttpServer().requestHandler(new Handler<HttpServerRequest>() {
//            public void handle(HttpServerRequest req) {
//
//                // 특정 path에 대한 처리
//                if (req.uri().indexOf("/test") != -1) {
//
//                    vertx.eventBus().send(".MonitorVerticle.test", "test", new Handler<AsyncResult<Message<Object>>>() {
//                        @Override
//                        public void handle(AsyncResult<Message<Object>> messageAsyncResult) {
////                            System.out.println(messageAsyncResult.succeeded());
////                            System.out.println("cluster success : " + );
//                            req.response().end(messageAsyncResult.result().body().toString());
//                        }
//                    });
//
//                } else {
//                    req.response().headers().set("Access-Control-Allow-Origin", "*");
//
//                    // 테스트용 JSON
//                    JsonObject json = new JsonObject();
//                    json.put("test", "1");
//                    req.response().end(json.toString());
//                }
//            }
//        }).listen(8888);
    }



}
