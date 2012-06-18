package org.railways.assistant.activity;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.railways.api.StationType;
import org.railways.api.TrainType;
import org.railways.api.ticket.Ticket;
import org.railways.api.ticket.TicketClient;
import org.railways.api.ticket.TicketForm;
import org.railways.assistant.R;
import org.railways.assistant.TicketApplication;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * 余票
 * 
 * @author gmz
 * @time 2012-6-10
 */
public class TicketActivity extends Activity {

	public static final int REQUEST_SEARCH_FORM = 11;

	static final DateFormat format = new SimpleDateFormat("yyyyMMdd");
	TicketApplication app;
	TicketClient ticketClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ticket);
		app = (TicketApplication) getApplication();
		ticketClient = new TicketClient(5000, 10000);
		// search
		openSearchForm();
		View view = findViewById(R.id.openForm);
		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openSearchForm();
			}
		});
	}

	void openSearchForm() {
		Intent i = new Intent(this, TicketSearchFormActivity.class);
		startActivityForResult(i, REQUEST_SEARCH_FORM);
	}

	@Override
	protected void onResume() {
		super.onResume();
		getParent().setTitle(this.getTitle());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_SEARCH_FORM:
			if (resultCode == Activity.RESULT_OK) {
				String depart = data.getStringExtra(String.valueOf(R.id.depart));
				String dest = data.getStringExtra(String.valueOf(R.id.dest));
				String date = data.getStringExtra(String.valueOf(R.id.date));
				String code = data.getStringExtra(String.valueOf(R.id.code));
				new SearchTask().execute(depart, dest, date, code);
			}
			break;

		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
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

}
