package com.lzq.networkstatelistener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class NetworkStateFragment extends Fragment implements OnClickListener {

	final String TAG = "NetworkStateFragment";
	boolean running = false;
	public static TextView ThreegTextView;
	public static TextView NetworkInfoTextView;
	public static Handler UIHandler;
	Button startButton;
	Button stopButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View v = inflater.inflate(R.layout.fragment_netstate, container, false);
		ThreegTextView = (TextView) v.findViewById(R.id.TextViewThreegInfo);
		NetworkInfoTextView = (TextView) v
				.findViewById(R.id.TextViewNetworkInfo);
		startButton = (Button) v.findViewById(R.id.StartButton);
		stopButton = (Button) v.findViewById(R.id.StopButton);
		startButton.setOnClickListener(this);
		stopButton.setOnClickListener(this);

		return v;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Toast.makeText(getActivity().getApplicationContext(),
				"NetworkStateListener", Toast.LENGTH_SHORT).show();
		UIHandler = new Handler();

		System.out.println(new Date().toString().replace(" ", "_")
				.replace("+", "_").replace(":", "_"));
		File rootFolder = new File("/sdcard/network_log");
		if ((!rootFolder.exists()) || (!rootFolder.isDirectory())) {
			rootFolder.mkdir();
		}

		
		try {
			Runtime.getRuntime().exec("su");
			Process ps=Runtime.getRuntime().exec("tcpdump -p -vv -s 0 -w "+getActivity().getCacheDir()+"/capture.pcap");
			//Log.i("tcpdump","tcpdump running? output to "+"tcpdump -p -vv -s 0 -w "+getActivity().getCacheDir()+"/capture.pcap");

		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		new Thread() {
			public void run() {
				File tcpdump_sh = new File(getActivity().getCacheDir()
						+ "/run_tcpdump.sh");
				tcpdump_sh.deleteOnExit();
				try {
					Runtime.getRuntime().exec("su");
					tcpdump_sh.createNewFile();
					BufferedWriter bw = new BufferedWriter(new FileWriter(
							tcpdump_sh));
					bw.append("#!/system/bin/sh\n");
					bw.append("su\n");
					bw.append("rm /sdcard/network_log/trace.pcap\n");
					bw.append("/system/xbin/tcpdump -i any -p -s 0 -w /sdcard/network_log/trace.pcap");
					bw.flush();
					bw.close();
					Runtime.getRuntime().exec(
							"chmod 777 " + tcpdump_sh.getAbsolutePath());
					//System.out.println(tcpdump_sh.getAbsolutePath());
					System.out.println("exec shell script.");
					int process_tcpdump = Runtime
							.getRuntime()
							.exec(tcpdump_sh.getAbsolutePath())
							.waitFor();
					System.out.println("tcpdump running?");
					// int process_tcpdump =
					// Runtime.getRuntime().exec(tcpdump_sh.getAbsolutePath()).waitFor();
					// System.out.println("run by sh: "+process_tcpdump.getInputStream().read());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};//}.start();

		// // 获取Root权限
		// try {
		// // Runtime rt=Runtime.getRuntime();
		// // rt.exec("su");
		// //
		// rt.exec("tcpdump -i any -p -s 0 -w /sdcard/network_log/trace.pcap");
		// new Thread()
		// {
		// public void run()
		// {
		// try {
		// Runtime.getRuntime().exec("su");
		// Runtime.getRuntime().exec("chmod 777 /data/local/tcpdump");
		// Process process_tcpdump =
		// Runtime.getRuntime().exec("/data/local/tcpdump");
		// System.out.println(process_tcpdump.getInputStream().read());
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		// }
		// }.start();
		// Runtime.getRuntime().exec("su");
		// Process process_tcpdump =
		// Runtime.getRuntime().exec("/data/local/tcpdump ");
		// //"/data/local/tcpdump -i any -p -s 0 -w "+Environment.getExternalStorageDirectory()+"/network_log/trace.pcap");
		// System.out.println("tcpdump -i any -p -s 0 -w "+Environment.getExternalStorageDirectory()+"/network_log/trace.pcap");
		// System.out.println("tcpdump -i any -p -s 0 -w "+Environment.getExternalStorageDirectory()+"/network_log/trace.pcap");
		// if (process_tcpdump != null) {
		// System.err.println(process_tcpdump.getInputStream());
		// if(process_tcpdump.getInputStream()!=null)
		// {
		// System.out.println(process_tcpdump.getInputStream().read());
		// // BufferedReader br = new BufferedReader(new InputStreamReader(
		// // process_tcpdump.getInputStream()));
		// // System.err.println(br.readLine());
		// }
		// else
		// {
		// System.out.println("process_tcpdump.getInputStream()==null");
		// }
		//
		// } else {
		// System.err.println("process_tcpdump==null");
		// }
		// Process process = Runtime.getRuntime().exec("su");

		// Process process_tcpdump =
		// Runtime.getRuntime().exec("tcpdump -i any -p -s 0 -w /sdcard/network_log/trace.pcap");

		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.StartButton) {
			Log.i(TAG, "onClick: starting service");
			running = true;
			getActivity().startService(
					new Intent(getActivity(), NetworkInfoMonitorService.class));

		} else if (v.getId() == R.id.StopButton) {
			Log.i(TAG, "onClick: stopping service");
			running = false;
			getActivity().stopService(
					new Intent(getActivity(), NetworkInfoMonitorService.class));
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (running)
			getActivity().stopService(
					new Intent(getActivity(), NetworkInfoMonitorService.class));
	}
}
