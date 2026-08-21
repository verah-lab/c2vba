package de.heuboe.tls.iface.syscon.rmq.json;

import com.google.gson.Gson;

import de.heuboe.tls.iface.syscon.rmq.data.TlsTelegram;

public class JsonConverter {

	private Gson gson;

	public JsonConverter() {
		gson = new Gson();
	}
	
	public String toJson(TlsTelegram tele) {
		return gson.toJson(tele);
	}
	
	public TlsTelegram toTelegram(String json) {
		return gson.fromJson(json, TlsTelegram.class);
	}
}
