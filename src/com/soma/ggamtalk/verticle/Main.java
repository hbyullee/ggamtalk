package com.soma.ggamtalk;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

import java.nio.ByteBuffer;

import com.soma.ggamtalk.verticle.MainVertilce;

public class Main {

	public static Vertx vertx;
	public static void main(String []args){
      createVertx();
	}

    public static void createVertx() {
    	  vertx = Vertx.vertx();
          
          VertxOptions options = new VertxOptions(); 
          options.setMaxEventLoopExecuteTime(Long.MAX_VALUE);
          vertx = Vertx.vertx(options);
        
          vertx.deployVerticle(new MainVerticle());
  		
    }

    public static void publishEvent(String type, String msg)
    {
        vertx.eventBus().publish(type, msg);
    }

    public static void publishEvent(String type, ByteBuffer buffer)
    {
        try
        {
            byte data[] = new byte[buffer.capacity()];
            buffer.get(data);
            vertx.eventBus().publish(type, data);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void publishEvent(String type, byte[] buffer)
    {
        try
        {
            vertx.eventBus().publish(type, buffer);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }


}

