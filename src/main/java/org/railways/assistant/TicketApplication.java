package org.railways.assistant;

import java.util.HashMap;
import java.util.Map;

import org.railways.api.ticket.IctCode;

import android.app.Application;

public class TicketApplication extends Application {

	private IctCode ictCode;
	private Map<String, String> cookie;
	private int retryTimes = 10;

	public void setIctCode(IctCode ictCode) {
		this.ictCode = ictCode;
	}

	public IctCode getIctCode() {
		return ictCode;
	}

	public Map<String, String> getCookie() {
		return cookie;
	}

	public void setCookies(String cookie) {
		if (this.cookie == null) {
			this.cookie = new HashMap<String, String>();
		}
		this.cookie.put("Cookie", cookie);
	}

	public int getRetryTimes() {
		return retryTimes;
	}

	public void setRetryTimes(int retryTimes) {
		this.retryTimes = retryTimes;
	}

}
