package org.railways.assistant.activity;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.railways.api.IdentifyingClient;
import org.railways.api.Response;
import org.railways.api.StationType;
import org.railways.api.TrainType;
import org.railways.api.ticket.Ticket;
import org.railways.api.ticket.TicketClient;
import org.railways.api.ticket.TicketForm;
import org.railways.assistant.R;
import org.railways.assistant.TicketApplication;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 余票
 * 
 * @author gmz
 * @time 2012-6-10
 */
public class TicketActivity extends Activity {

	static final DateFormat format = new SimpleDateFormat("yyyyMMdd");
	TicketApplication app;
	ImageView image;
	TableLayout tickeyLayout;
	IdentifyingClient identifyingClient;
	TicketClient ticketClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ticket);
		app = (TicketApplication) getApplication();
		// search
		Button search = (Button) findViewById(R.id.search);
		search.setOnClickListener(new SearchListener());
		// image
		image = (ImageView) findViewById(R.id.codeImage);
		image.setOnClickListener(new CodeImageListener());
		// table
		tickeyLayout = (TableLayout) findViewById(R.id.tickeyLayout);
		// client
		identifyingClient = new IdentifyingClient(5000, 10000);
		ticketClient = new TicketClient(5000, 10000);
	}

	@Override
	protected void onResume() {
		super.onResume();
		new ImageTask().execute();
	}

	class SearchListener implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			TextView depart = (TextView) findViewById(R.id.depart);
			TextView dest = (TextView) findViewById(R.id.dest);
			TextView date = (TextView) findViewById(R.id.date);
			TextView code = (TextView) findViewById(R.id.code);
			new SearchTask().execute(toString(depart.getText()), toString(dest.getText()), toString(date.getText()),
					toString(code.getText()));
		}

		String toString(CharSequence seq) {
			return seq != null ? seq.toString() : null;
		}
	}

	class SearchTask extends AsyncTask<String, Integer, List<Ticket>> {
		@Override
		protected List<Ticket> doInBackground(String... args) {
			if (args == null || args.length < 4) {
				return Collections.emptyList();
			}
			String depart = args[0], dest = args[1], code = args[3];
			if (depart == null || depart.length() == 0 || dest == null || dest.length() == 0 || args[2] == null
					|| args[2].length() == 0) {
				return Collections.emptyList();
			}
			Date date;
			try {
				date = format.parse(args[2]);
			} catch (ParseException e) {
				Log.e(TicketActivity.class.getSimpleName(), "parse date " + args[2] + " failed");
				return Collections.emptyList();
			}
			TicketForm form = new TicketForm().setSearchType(TicketForm.SearchType.DEFAULT)
					.setIctCode(app.getIctCode()).setStationType(StationType.ALL).setTrainType(TrainType.ALL);
			form.setDeparture(depart).setDestination(dest).setDate(date).setRandomCode(code);
			Log.i(TicketActivity.class.getSimpleName(), form.toParameters());
			Log.i(TicketActivity.class.getSimpleName(), app.getCookie().toString());
			try {
				return ticketClient.request(form, app.getCookie());
			} catch (Exception e) {
				Log.e(TicketActivity.class.getSimpleName(), "request ticket failed");
				return Collections.emptyList();
			}
		}

		@Override
		protected void onPostExecute(List<Ticket> result) {
			super.onPostExecute(result);
			if (tickeyLayout.getChildCount() > 0) {
				tickeyLayout.removeViews(0, tickeyLayout.getChildCount());
			}
			for (Ticket t : result) {
				TableRow row = new TableRow(getApplicationContext());
				TextView view = new TextView(getApplicationContext());
				view.setText(t.toString());
				row.addView(view);
				tickeyLayout.addView(row);
			}
		}
	}

	class CodeImageListener implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			new ImageTask().execute();
		}
	}

	class ImageTask extends AsyncTask<String, Integer, Response> {
		@Override
		protected Response doInBackground(String... arg0) {
			try {
				return identifyingClient.request();
			} catch (Exception e) {
				Log.e(ImageTask.class.getSimpleName(), "request random code failed");
				Toast.makeText(getApplicationContext(), "获取验证码失败", Toast.LENGTH_SHORT).show();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Response result) {
			super.onPostExecute(result);
			if (result != null) {
				String cookie = result.getCookie();
				Log.i(ImageTask.class.getSimpleName(), cookie != null && cookie.length() > 0 ? cookie : "no cookie");
				app.setCookies(cookie);
				image.setImageBitmap(BitmapFactory.decodeStream(result.getContent()));
				try {
					result.close();
				} catch (IOException e) {
				}
			}
		}
	}

}
