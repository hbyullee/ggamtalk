package com.soma.ggamtalk.api;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;

import com.soma.ggamtalk.utils.Config;

public class NoticePushAPI extends BaseAPI{
	
	public NoticePushAPI() { }
	
	@Override
	public void execute(Vertx vertx,HttpServerRequest request) {
		init(vertx, request);
		
		params.getString("msg_type");   //gcm, chat, all
		params.getString("to_what");  //누구에게 보낼지 , 특정 앱을 추가한 유저, 전체 유저, 채널 유저  all, app, channel
		params.getString("app_id");
		params.getString("channel_id");
		params.getString("msg");
		params.put("date", System.currentTimeMillis());
		if("gcm".equals(params.getString("msg_type"))){
			sendNoticeGCM(this, Config.sendGCM, params.toString());
		}
		else if("chat".equals(params.getString("msg_type"))){
			sendNoticeChat(this, Config.sendChat, params.toString());
		}
		else {
			sendNoticeGCM(this, Config.sendGCM, params.toString());
			sendNoticeChat(this, Config.sendChat, params.toString());
		}

	}

	@Override
	public void onExecute(int what, JsonObject resultJO) {

		if(resultJO.containsKey("result_code") && resultJO.getInteger("result_code")==-1){
			request.response().end(resultJO.toString());
			return;
		}
		JsonObject rs = new JsonObject();

		switch (what) {
	
		case Config.sendGCM:
			request.response().end(resultJO.toString());
			
			break;
		
		case Config.sendChat:
			request.response().end(resultJO.toString());
		
		break;
		}
	}
	
	@Override
	public JsonObject checkValidation(JsonObject Params) {
		return null;
	}
}
