package org.railways.assistant.activity;

import org.railways.api.ticket.ICTCodeClient;
import org.railways.assistant.R;
import org.railways.assistant.TicketApplication;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TabHost;

public class AssistantActivity extends TabActivity {

	TicketApplication app;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		app = (TicketApplication) getApplication();
		// tab
		TabHost tabHost = getTabHost();
		Resources res = getResources();
		TabHost.TabSpec tabSpec = tabHost.newTabSpec("tab_ticket").setIndicator(res.getString(R.string.tab_ticket))
				.setContent(new Intent().setClass(this, TicketActivity.class));
		tabHost.addTab(tabSpec);
		tabSpec = tabHost.newTabSpec("tab_train").setIndicator(res.getString(R.string.tab_train))
				.setContent(new Intent().setClass(this, TicketActivity.class));
		tabHost.addTab(tabSpec);
		tabSpec = tabHost.newTabSpec("tab_timetable").setIndicator(res.getString(R.string.tab_timetable))
				.setContent(new Intent().setClass(this, TicketActivity.class));
		tabHost.addTab(tabSpec);
		tabHost.setCurrentTab(0);
		// request
		new AppInitTask().execute();
	}

	class AppInitTask extends AsyncTask<String, Integer, String> {
		@Override
		protected String doInBackground(String... arg0) {
			ICTCodeClient ictClient = new ICTCodeClient(5000, 10000);
			for (int i = 0; i < 3; i++) {
				try {
					app.setIctCode(ictClient.request());
					break;
				} catch (Exception e) {
					Log.e(AssistantActivity.class.getSimpleName(), i + ": request IctCode failed");
				}
			}
			return null;
		}
	}

}