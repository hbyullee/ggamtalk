package com.soma.ggamtalk.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

import java.nio.ByteBuffer;

import com.soma.ggamtalk.verticle.ChatVerticle;
import com.soma.ggamtalk.verticle.RestVerticle;
import io.vertx.core.DeploymentOptions;

public class MainVerticle extends AbstractVerticle {

    public void createVertx() {
        vertx.deployVerticle(new RedisVerticle());
        vertx.deployVerticle(new DBVerticle());
        vertx.deployVerticle(new RestVerticle());
        vertx.deployVerticle(new GCMVerticle());
        vertx.deployVerticle(new ChatVerticle());
    }

	@Override
	public void start() throws Exception {
		super.start();
        createVertx();
    }
	
}

