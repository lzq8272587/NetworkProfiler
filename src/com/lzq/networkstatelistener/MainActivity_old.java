package com.lzq.networkstatelistener;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity_old extends Activity implements OnClickListener{

	 
	final String TAG="MainActivity";
	NetworkInfo info;
	ConnectivityManager connectMgr;
	TelephonyManager tel;
    public static TextView ThreegTextView;
    public static TextView NetworkInfoTextView;
    public static Handler UIHandler;
    Button startButton;
    Button stopButton;
    boolean running=false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        ThreegTextView=(TextView)findViewById(R.id.TextViewThreegInfo);
        NetworkInfoTextView=(TextView)findViewById(R.id.TextViewNetworkInfo);
        startButton=(Button)findViewById(R.id.StartButton);
        stopButton=(Button)findViewById(R.id.StopButton);
        startButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
        
        Toast.makeText(getApplicationContext(), "NetworkStateListener",
        	     Toast.LENGTH_SHORT).show();
        
		UIHandler=new Handler();

  
		System.out.println(new Date().toString().replace(" ", "_").replace("+", "_").replace(":", "_"));
		File rootFolder=new File("/sdcard/network_log");
		if((!rootFolder.exists())||(!rootFolder.isDirectory()))
		{
			rootFolder.mkdir();
		}
		
		//获取Root权限
		try {
			Process process = Runtime.getRuntime().exec("su");

			Process process_tcpdump = Runtime.getRuntime().exec("tcpdump -i any -p -s 0 -w /sdcard/capture"+System.currentTimeMillis()+".pcap");
			 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(running)
			stopService(new Intent(this, NetworkInfoMonitorService.class));
	}


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v.getId()==R.id.StartButton)
		{
			Log.i(TAG, "onClick: starting service"); 
			running=true;
			startService(new Intent(this, NetworkInfoMonitorService.class));
			
		}
		else if(v.getId()==R.id.StopButton)
		{
			Log.i(TAG, "onClick: stopping service");  
			running=false;
			stopService(new Intent(this, NetworkInfoMonitorService.class));
		}
	}




	
}
