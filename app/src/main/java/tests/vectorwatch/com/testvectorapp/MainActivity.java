package tests.vectorwatch.com.testvectorapp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.JsonWriter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import android.content.res.Resources;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;


public class MainActivity extends ActionBarActivity {

	private TextView dataText;

	private TextView buttons;

	private Button testSendButton;

	private Button toggleLogging;

	private boolean isLogging = false;

	private int counter = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Resources res = getResources();
		InputStream in_s = res.openRawResource(R.raw.handshake_vector);
		BufferedReader br = new BufferedReader(new InputStreamReader(in_s));
		String readLine = null;
		try {
			// While the BufferedReader readLine is not null
			while ((readLine = br.readLine()) != null) {
				Log.d("TEXT", readLine);
			}

// Close the InputStream and BufferedReader
			in_s.close();
			br.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		setContentView(R.layout.activity_main);
		dataText = (TextView) findViewById(R.id.data);
		testSendButton = (Button) findViewById(R.id.sendTestData);
		toggleLogging = (Button) findViewById(R.id.toggleLogging);
		buttons = (TextView) findViewById(R.id.buttonsPress);

		toggleLogging.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isLogging) {
					Log.d("Receiver", "-------------Stopped logging");
					toggleLogging.setText("Start Logging");
					isLogging = false;
				}
				else {
					Log.d("Receiver", "-------------Started logging");
					toggleLogging.setText("Stop logging");
					isLogging = true;
				}
			}
		});

		testSendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				Intent intent = new Intent();


				ComponentName cn = new ComponentName("com.vectorwatch.android", "com.vectorwatch.android.service.ble" +
						".BleService");
				intent.setComponent(cn);


				intent.putExtra("service_send", "RAW_PUSH_NOTIFICATION");

				VectorPushData data = new VectorPushData();
				data.setV(0);

				List<WriteToField> fieldList = new ArrayList<WriteToField>();

				WriteToField field = new WriteToField();

				field.setAppID(81);
				field.setD("test " + counter);
				field.setfID(1);
				field.setwID(1);

				fieldList.add(field);
				data.setP(fieldList);

				Gson gson = new Gson();


				counter ++;

				String serializedPayload = gson.toJson(data);
				intent.putExtra("RAW_PUSH_NOTIFICATION_PAYLOAD", serializedPayload);
				startService(intent);

				Log.d("sent notifications", "Data");
			}
		});

	}


	private class VectorPushData {

		private int v;

		private List<WriteToField> p;

		public int getV() {
			return v;
		}

		public void setV(int v) {
			this.v = v;
		}

		public List<WriteToField> getP() {
			return p;
		}

		public void setP(List<WriteToField> p) {
			this.p = p;
		}

	}

	private class WriteToField {

		private int appID;

		private int wID;

		private int fID;


		private int type = 3;

		//max 60 chars
		private String d;

		public int getAppID() {
			return appID;
		}

		public void setAppID(int appID) {
			this.appID = appID;
		}

		public int getwID() {
			return wID;
		}

		public void setwID(int wID) {
			this.wID = wID;
		}

		public int getfID() {
			return fID;
		}

		public void setfID(int fID) {
			this.fID = fID;
		}

		public String getD() {
			return d;
		}

		public void setD(String d) {
			this.d = d;
		}

		public int getType() {
			return type;
		}

	}

	class Acc
	{
		public int x,y,z;

		public Acc(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}

	//helper functions
	boolean isSmall(int val)
	{
		if(val> -75 && val < 75)
			return true;
		else
			return false;
	}

	boolean isLarge(int val)
	{
		if(val> 150 || val < -150)
			return true;
		else
			return false;
	}

	boolean isShake(int val)
	{
		if(val> 300 || val < -300)
			return true;
		else
			return false;
	}

	List<Acc> val=new ArrayList<Acc>();
	// handler for received Intents for the "my-event" event
	private BroadcastReceiver mVectorEventReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Extract data included in the Intent

			String event = intent.getStringExtra("EVENT_TYPE");

			boolean isHandShake=false;
			if(event.equals("ACCELEROMETER_DATA")) {
				int x = intent.getIntExtra("X", -10000);
				int y = intent.getIntExtra("Y", -10000);
				int z = intent.getIntExtra("Z", -10000);
				int kk = intent.describeContents();
				SimpleDateFormat s = new SimpleDateFormat("hh mm ss SSS");
				String format = s.format(new Date());
				String dataToSend = format +  "   X:" + x + " " + "Y:" + y + " " + "Z:" + z;
				dataText.setText(dataToSend);

				if(val.size()==10)
					val.remove(0);

				val.add(new Acc(x,y,z));

				Log.d("watch", "List size:"  + val.size());


				if(val.size()>=3)
				{
					int valSize=val.size();
					for(int i=0;i<valSize;++i)
					{
						if (isShake(val.get(i).x)) {
							isHandShake = true;
							break;
						}

					}
				}

//				if(val.size()>=3 && isLarge(val.get(0).y) && isSmall(val.get(1).y))
//				{
//					int valSize=val.size();
//					for(int i=0;i<valSize;++i)
//					{
//						if(isSmall(val.get(i).y))
//						{
//							for(int j=i+1;j<valSize;++j)
//							{
//								if (isShake(val.get(i).x)) {
//									isHandShake = true;
//									break;
//								}
//							}
//							if(isHandShake)
//								break;
//						}
//					}
//				}

				if(isHandShake)
				{
					//TODO
					val.clear();
					Log.d("watch", "this was a handshake *****************************");
				}


				if (isLogging) {
					Log.d("receiver", dataToSend);
				}
			} else if (event.equals("BTN_PRESS")) {


				int wID = intent.getIntExtra("WATCHFACE_INDEX", -1);
				String btn = intent.getStringExtra("BTN");

				Log.d("receiver", "Button press on face " + wID + "button : " + btn);

				buttons.setText("Pressed button " + btn + " on watchface ID:" + wID);

			}
			// Log.d("receiver", "Got event: " + event);
		}
	};

	@Override
	protected void onResume() {
		super.onResume();

		// Register mVectorEventReceiver to receive events.
		registerReceiver(mVectorEventReceiver,
				new IntentFilter("com.vectorwatch.android.event.BROADCAST"));


		Log.d("Receiver", "registered for events");
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mVectorEventReceiver);

		Log.d("Receiver", "unregistered receiver");

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
