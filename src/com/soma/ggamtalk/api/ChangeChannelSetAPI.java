package com.soma.ggamtalk.api;

import java.util.Set;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;

import com.soma.ggamtalk.utils.Config;
import com.soma.ggamtalk.utils.Util;

public class ChangeChannelSetAPI extends BaseAPI{
	
	public ChangeChannelSetAPI() { }
	
	@Override
	public void execute(Vertx vertx, HttpServerRequest request) {
		init(vertx,request);
		
		
		if(params.isEmpty() || checkValidation(params).getInteger("result_code")==-1){
			request.response().end(checkValidation(params).toString());
			return;
		}
		
		getRedis(this, Config.getSession, params.getString("token"));
		
	}

	@Override
	public void onExecute(int what, JsonObject resultJO) {
		
		if(resultJO.containsKey("result_code") && resultJO.getInteger("result_code")==-1){
			request.response().end(resultJO.toString());
			return;
		}
		JsonObject rs = new JsonObject();

		switch (what) {
		case Config.getSession:
			if(!resultJO.containsKey("result")||resultJO.getString("result")==null){
				rs.put("result_code", -1);
				rs.put("result_msg", "로그인이 필요합니다.");
				request.response().end(rs.toString());
				break;
			}
			
			 Set<String> names = params.fieldNames();
			 StringBuilder setQuery=new StringBuilder();
	         for(String field : names) {
	             if(field.equals("channel_id")||field.equals("token"))
	                 continue;
	             setQuery.append(field);
	             setQuery.append("='");
	             setQuery.append(params.getString(field));
	             setQuery.append("',");
	         }
	         setQuery.deleteCharAt(setQuery.length()-1);
	         
			String query = String.format("UPDATE channel SET %s WHERE channel_id='%s'", 
									setQuery.toString(), params.getString("channel_id"));
			
			insertCustomQuery(this, Config.changeChannelSet, query );
			break;
			
		case Config.changeChannelSet:
			keysRedis(this, Config.keysRedis, "channelList:*:"+params.getString("channel_id"));
			rs.put("result_code", 0);
			rs.put("result_msg", "채널 정보를 수정하였습니다.");
			request.response().end(rs.toString());
			break;

		case Config.keysRedis:
			getRedis(this, Config.getRedis, resultJO.getJsonArray("result").getString(0));
			break;
			
		case Config.getRedis:
			JsonObject jo = new JsonObject(resultJO.getString("result"));

			Set<String> names2 = params.fieldNames();
	        for(String field : names2) {
	        	jo.put(field, params.getString(field));
	        }
			
	        JsonObject table = new JsonObject();
	        table.put("key","channelList:"+jo.getString("app_id")+":"+params.getString("channel_id"));
	        table.put("value", jo.toString());
	        setRedis(this, Config.setRedis,table);
			
			break;
			
		}
		
	}
	
	@Override
	public JsonObject checkValidation(JsonObject params) {
		JsonObject res = new JsonObject(); 
		if (!params.containsKey("token") || params.getString("token").isEmpty() || params.getString("token").equals("")){
			res.put("result_code",-1);
			res.put("result_msg", "로그인 후 이용해주세요.");
			return res;
		}
		if (!params.containsKey("channel_id") || params.getString("channel_id").isEmpty() || params.getString("channel_id").equals("")){
			res.put("result_code",-1);
			res.put("result_msg", "로그인 후 이용해주세요.");
			return res;
		}
		res.put("result_code", 0);
			return res;
	}

}
