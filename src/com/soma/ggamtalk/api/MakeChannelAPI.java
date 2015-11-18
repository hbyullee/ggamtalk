package com.soma.ggamtalk.api;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;

import com.soma.ggamtalk.utils.Config;
import com.soma.ggamtalk.utils.Util;

public class MakeChannelAPI extends BaseAPI{
	String channel_id="";
	public MakeChannelAPI() { }
	

	@Override
	public void execute(Vertx vertx, HttpServerRequest request) {
		init(vertx, request);
		
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
			String user_info[]= resultJO.getString("result").split(",");
			String user_id=user_info[0];
			String gcm_id =user_info[1];
			params.put("user_id", user_id);
			params.put("gcm_id", gcm_id);
			channel_id = Util.getChannelId(params.getString("channel_name"));
			
			String query="";
			if("on".equals(params.getString("public_onoff")))
					query = String.format("INSERT INTO channel SET channel_id='%s', chief_id='%s', channel_name='%s', public_onoff='%s', "
										+ "channel_limit='%s', app_id='%s', datetime=%s ",
										channel_id, params.getString("user_id"), params.getString("channel_name"), 
										params.getString("public_onoff"), params.getString("channel_limit"), 
										params.getString("app_id"), "now()");
			else if("off".equals(params.getString("public_onoff")))
					query = String.format("INSERT INTO channel SET channel_id='%s', chief_id='%s', channel_name='%s', public_onoff='%s', "
						+ "channel_pw='%s', channel_limit='%s', app_id='%s', datetime=%s ",
						channel_id, params.getString("user_id"), params.getString("channel_name"), 
						params.getString("public_onoff"), params.getString("channel_pw"), params.getString("channel_limit"), 
						params.getString("app_id"), "now()");
			
			insertCustomQuery(this, Config.setChannel, query);
			
			JsonObject table = new JsonObject();
			table.put("key", "channelList:"+params.getString("app_id")+":"+channel_id);
			JsonObject value = params.copy();
			value.put("channel_id", channel_id); value.remove("token"); value.remove("user_nick"); value.put("chief_id",value.getString("user_id")); value.remove("user_id");
			table.put("value", value.toString());
			
			setRedis(this, Config.setRedis, table);
			
			break;

		case Config.setChannel:
		 	setPermission(this, Config.setPermission, channel_id);
			//TODO: 채널의 모든 정보 넣기로 변경
			
//			JsonObject table2 = new JsonObject();
//			table2.put("key", "channel:"+params.getString("user_id")+":"+params.getString("app_id"+":"+params.getString("channel_id"));
//			JsonObject value2 = new JsonObejct();
//			value2.put("user_id", params.getString("user_id"));
//			value2.put("app_id", params.getString("app_id"));
//			value2.put("user_nick", params.getString("user_nick"));
//			value2.put("channel_id", params.getString("channel_id"));
//			table2.put("value", value2.toString());
		 	
//		 	JsonObject user_info = new JsonObject();
//		 	user_info.put("user_id", params.getString("user_id"));
//		 	user_info.put("gcm_id", params.getString("gcm_id"));
//		 	user_info.put("user_nick", params.getString("user_nick"));
//		 	user_info.put("user_color", "#FFE400");
//
//		 	JsonObject table2 = new JsonObject();
//		 	table2.put("key","users:"+channel_id);
//		 	table2.put("value",params.getString("user_id")+","+params.getString("gcm_id")+","+params.getString("user_nick")+",#FFE400");
//			saddRedis(this, Config.saddRedis, table2);
//			
//			JsonObject table3 = new JsonObject();
//		 	table3.put("key","ch:"+user_id+":"+channel_id);
//		 	table3.put("value",user_info);
//		 	setRedis(this, Config.setRedis, table3);
			
		 	String query2 = String.format("INSERT INTO channel_user_list SET user_id='%s', app_id='%s', channel_id='%s', user_nick='%s'",
		 			params.getString("user_id"), params.getString("app_id"), channel_id, params.getString("user_nick"));
		 	
		 	insertCustomQuery(this, Config.setUserChannel, query2);
		 	break;
		 	
		 	//TODO: 채널 정보 캐시에 저장하는 로직 
		case Config.setUserChannel:
			rs.put("result_code", 0);
			rs.put("result_msg", "채널이 생성되었습니다.");
			rs.put("channel_id", channel_id);
			request.response().end(rs.toString());
			
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
		if (!params.containsKey("app_id") || params.getString("app_id").isEmpty() || params.getString("app_id").equals("")){
			res.put("result_code",-1);
			res.put("result_msg", "앱이 선택되지 않았습니다.");
			return res;
		}
		if (!params.containsKey("channel_name") || params.getString("channel_name").isEmpty() || params.getString("channel_name").equals("")){
			res.put("result_code",-1);
			res.put("result_msg", "채널 명을 정확히 입력해주세요.");
			return res;
		}
		if (!params.containsKey("public_onoff") || params.getString("public_onoff").isEmpty() || params.getString("public_onoff").equals("")){
			res.put("result_code",-1);
			res.put("result_msg", "비밀방 여부를 선택해주세요.");
			return res;
		}
		if (!params.containsKey("channel_limit") || params.getString("channel_limit").isEmpty() || params.getString("channel_limit").equals("")){
			res.put("result_code",-1);
			res.put("result_msg", "채널 입장 제한 수를 설정해주세요.");
			return res;
		}
		if (!params.containsKey("user_nick") || params.getString("user_nick").isEmpty() || params.getString("user_nick").equals("")){
			res.put("result_code",-1);
			res.put("result_msg", "해당하는 닉네임을 설정해주세요.");
			return res;
		}
		
		res.put("result_code", 0);
			return res;
	}
}
