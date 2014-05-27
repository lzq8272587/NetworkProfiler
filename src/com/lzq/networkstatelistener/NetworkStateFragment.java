package com.lzq.networkstatelistener;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;





public class NetworkStateFragment extends Fragment implements OnClickListener{

	final String TAG="NetworkStateFragment";
	boolean running=false;
    public static TextView ThreegTextView;
    public static TextView NetworkInfoTextView;
    public static Handler UIHandler;
    Button startButton;
    Button stopButton;
    
    

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View v=inflater.inflate(R.layout.fragment_netstate, container,false);
        ThreegTextView=(TextView)v.findViewById(R.id.TextViewThreegInfo);
        NetworkInfoTextView=(TextView)v.findViewById(R.id.TextViewNetworkInfo);
        startButton=(Button)v.findViewById(R.id.StartButton);
        stopButton=(Button)v.findViewById(R.id.StopButton);
        startButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
        

		return v;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
        Toast.makeText(getActivity().getApplicationContext(), "NetworkStateListener",
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
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v.getId()==R.id.StartButton)
		{
			Log.i(TAG, "onClick: starting service"); 
			running=true;
			getActivity().startService(new Intent(getActivity(), NetworkInfoMonitorService.class));
			
		}
		else if(v.getId()==R.id.StopButton)
		{
			Log.i(TAG, "onClick: stopping service");  
			running=false;
			getActivity().stopService(new Intent(getActivity(), NetworkInfoMonitorService.class));
		}
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(running)
			getActivity().stopService(new Intent(getActivity(), NetworkInfoMonitorService.class));
	}
}
