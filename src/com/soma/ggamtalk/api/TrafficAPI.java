package com.soma.ggamtalk.api;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.soma.ggamtalk.utils.Config;

public class TrafficAPI extends BaseAPI{
	
	JsonArray keys;
	public TrafficAPI() { }
	
	@Override
	public JsonObject checkValidation(JsonObject Params) {
		return null;
	}

	@Override
	public void execute(Vertx vertx,HttpServerRequest request) {
		init(vertx, request);
		
		keysRedis(this, Config.keysRedis, "traffic:*");
				
	}

	@Override
	public void onExecute(int what, JsonObject resultJO) {

		if(resultJO.containsKey("result_code") && resultJO.getInteger("result_code")==-1){
			request.response().end(resultJO.toString());
			return;
		}
		JsonObject rs = new JsonObject();

		switch (what) {
	
		case Config.keysRedis:
		{
			keys = resultJO.getJsonArray("result");
			mgetManyRedis(this, Config.mgetManyRedis, resultJO.getJsonArray("result").toString());
			break;
		}
		case Config.mgetManyRedis:
		{	
			JsonArray list = new JsonArray();
			for( int i = 0 ; i < resultJO.getJsonArray("result").size() ; i++){
				JsonObject jo = new JsonObject();
				jo.put(keys.getString(i), new JsonArray(resultJO.getJsonArray("result").getString(i)));
				list.add(jo);
			}
			request.response().end(list.toString());
		}	
		}
	}
}
