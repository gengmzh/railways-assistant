package org.railways.assistant.activity;

import java.io.IOException;

import org.railways.api.IdentifyingClient;
import org.railways.api.Response;
import org.railways.assistant.R;
import org.railways.assistant.TicketApplication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class TicketSearchFormActivity extends Activity {

	// static final DateFormat format = new SimpleDateFormat("yyyyMMdd");
	TicketApplication app;
	TextView departView;
	TextView destView;
	TextView dateView;
	TextView codeView;
	ImageView codeImage;
	IdentifyingClient identifyingClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ticket_search_form);
		app = (TicketApplication) getApplication();
		// form
		departView = (TextView) findViewById(R.id.depart);
		destView = (TextView) findViewById(R.id.dest);
		dateView = (TextView) findViewById(R.id.date);
		codeView = (TextView) findViewById(R.id.code);
		codeImage = (ImageView) findViewById(R.id.codeImage);
		codeImage.setOnClickListener(new CodeImageListener());
		Button search = (Button) findViewById(R.id.search);
		search.setOnClickListener(new SearchListener());
		// client
		identifyingClient = new IdentifyingClient(5000, 10000);
	}

	@Override
	protected void onResume() {
		super.onResume();
		new CodeImageTask().execute();
	}

	class CodeImageListener implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			new CodeImageTask().execute();
		}
	}

	class CodeImageTask extends AsyncTask<String, Integer, Response> {
		@Override
		protected Response doInBackground(String... arg0) {
			try {
				return identifyingClient.request();
			} catch (Exception e) {
				Log.e(CodeImageTask.class.getSimpleName(), "request random code failed");
			}
			return null;
		}

		@Override
		protected void onPostExecute(Response result) {
			super.onPostExecute(result);
			if (result != null) {
				String cookie = result.getCookie();
				Log.i(CodeImageTask.class.getSimpleName(), cookie != null && cookie.length() > 0 ? cookie : "no cookie");
				app.setCookies(cookie);
				codeImage.setImageBitmap(BitmapFactory.decodeStream(result.getContent()));
				try {
					result.close();
				} catch (IOException e) {
				}
			} else {
				Toast.makeText(getApplicationContext(), "获取验证码失败", Toast.LENGTH_SHORT).show();
			}
		}
	}

	class SearchListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			boolean illegal = false;
			CharSequence depart = departView.getText();
			if (depart == null || depart.length() == 0) {
				illegal = true;
				departView.setHintTextColor(android.R.color.background_light);
			}
			CharSequence dest = destView.getText();
			if (dest == null || dest.length() == 0) {
				illegal = true;
				destView.setHintTextColor(android.R.color.background_light);
			}
			// Date date = null;
			CharSequence date = dateView.getText();
			// if (dateStr != null && dateStr.length() > 0) {
			// try {
			// date = format.parse(dateStr.toString());
			// } catch (ParseException e) {
			// Log.e(TicketSearchFormActivity.class.getSimpleName(),
			// "parse date " + dateStr + " failed");
			// }
			// }
			if (date == null || date.length() == 0) {
				illegal = true;
				dateView.setHintTextColor(android.R.color.background_light);
			}
			CharSequence code = codeView.getText();
			if (code == null || code.length() == 0) {
				illegal = true;
				codeView.setHintTextColor(android.R.color.background_light);
			}
			if (illegal) {
				return;
			}
			Intent i = new Intent();
			i.putExtra(String.valueOf(R.id.depart), depart.toString());
			i.putExtra(String.valueOf(R.id.dest), dest.toString());
			i.putExtra(String.valueOf(R.id.date), date.toString());
			i.putExtra(String.valueOf(R.id.code), code.toString());
			TicketSearchFormActivity.this.setResult(Activity.RESULT_OK, i);
			TicketSearchFormActivity.this.finish();
		}
	}

}
