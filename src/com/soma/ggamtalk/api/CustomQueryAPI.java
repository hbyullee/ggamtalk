package com.soma.ggamtalk.api;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;

import com.soma.ggamtalk.utils.Config;

public class CustomQueryAPI extends BaseAPI{
	
	public CustomQueryAPI() { }
	
	@Override
	public JsonObject checkValidation(JsonObject Params) {
		return null;
	}

	@Override
	public void execute(Vertx vertx,HttpServerRequest request) {
		init(vertx, request);
		
		if(params.getString("query").indexOf("select")>-1){
			selectCustomQuery2(this, Config.customQuery, params.getString("query"));
		}else{
			insertCustomQuery(this, Config.customQuery, params.getString("query"));
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
	
		case Config.customQuery:
			rs.put("result_code", 0);
			rs.put("result_msg", "success to query");
			rs.put("result", resultJO.getJsonObject("results"));
			request.response().end(rs.toString());
		}
	}
}
