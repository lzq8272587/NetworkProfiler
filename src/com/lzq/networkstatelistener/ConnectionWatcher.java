package com.lzq.networkstatelistener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;

import android.app.Fragment;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class ConnectionWatcher extends Fragment {

	String TAG = "ConnectionWatcher";
	TextView ConnInfoView = null;

	boolean running = true;
	ConnRecorder cr = null;

	File tcp = new File("/proc/net/tcp6");
	BufferedReader tcp_br = null;

	File ConnLog = null;
	BufferedWriter ConnLog_bw = null;

	String DropboxUID = null;

	HashSet<Connection> PriorCntSet = new HashSet<Connection>();
	HashSet<Connection> CurCntSet = new HashSet<Connection>();

	HashMap<Integer, Connection> ConnectionInfo = new HashMap<Integer, Connection>();
	HashMap<String, Connection> ConnectionInfoBuffer = new HashMap<String, Connection>();
	int index = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		try {
			Runtime.getRuntime().exec("su");
//			Process netstat = Runtime.getRuntime().exec("netstat -a");
//
//			String line = null;
//			BufferedReader br = new BufferedReader(new InputStreamReader(
//					netstat.getInputStream()));
//			while ((line = br.readLine()) != null) {
//				System.out.println(line);
//			}

			// get uid of dropbox
			try {
				PackageManager pm = getActivity().getPackageManager();
				ApplicationInfo ai = pm.getApplicationInfo(
						"com.dropbox.android", PackageManager.GET_ACTIVITIES);
				Log.v("UID", "Dropbox:" + ai.uid);
				DropboxUID = "" + ai.uid;
				Toast.makeText(getActivity(), Integer.toString(ai.uid, 10),
						Toast.LENGTH_SHORT).show();
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}

			startRecording();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View v = inflater.inflate(R.layout.fragment_connection, container,
				false);
		ConnInfoView = (TextView) v.findViewById(R.id.ConnectionInfoTextView);
		return v;
	}

	class ConnRecorder extends Thread {
		String line = null;

		public void run() {
			try {

				Log.e(TAG, "Read network state from netstat.");
				while (running) {
					tcp_br = new BufferedReader(new FileReader(tcp));
					tcp_br.readLine();
					// System.out.println(tcp_br.readLine());

					PriorCntSet = (HashSet<Connection>) CurCntSet.clone();
					CurCntSet = new HashSet<Connection>();

					while ((line = tcp_br.readLine()) != null) {

						String[] temp = line.split(" +");

						// System.out.println(line);
						String local_addr = temp[2];
						String rm_addr = temp[3];
						String uid = temp[8];

						// filter for Dropbox connection info
						// System.out.println("UID="+uid+"   DropboxUID="+DropboxUID);
						if (uid.equals(DropboxUID)) {
							// connection wrapper
							Connection cnt = new Connection("TCP",
									parseAddr(local_addr)[0],
									parseAddr(local_addr)[1],
									parseAddr(rm_addr)[0],
									parseAddr(rm_addr)[1]);
							// System.out.println("PriorCntSet= " + 
							// PriorCntSet);
							// System.out.println("cnt= " + cnt);
							// System.out.println(PriorCntSet.contains(cnt));
							CurCntSet.add(cnt);
							Log.e(TAG,line);
							// System.out.println("    " + line);
						}

					}

					//System.out.println("PriorCntSet: " + PriorCntSet);
					//System.out.println("CurCntSet: " + CurCntSet);

					if (CurCntSet.equals(PriorCntSet))
						continue;

					// new connection
					HashSet<Connection> startingCntSet = differencing(
							CurCntSet, PriorCntSet);
					// ended connection
					HashSet<Connection> endedCntSet = differencing(PriorCntSet,
							CurCntSet);

					// add start time for each new connection
					for (Connection c : startingCntSet) {
						Connection c_clone = c.clone();
						c_clone.setStartTime(System.currentTimeMillis());
						ConnectionInfoBuffer.put(
								c_clone.getSrcIP() + c_clone.getSrcPort()
										+ c_clone.getDesIP()
										+ c_clone.getDesPort(), c_clone);
						// System.err.println("##### in start set: "+c.toString());
					}

					// add end time for each ended connection
					for (Connection c : endedCntSet) {
						Connection cc = ConnectionInfoBuffer.get(c.getSrcIP()
								+ c.getSrcPort() + c.getDesIP()
								+ c.getDesPort());
						cc.setEndTime(System.currentTimeMillis());
						ConnectionInfoBuffer.remove(c.getSrcIP()
								+ c.getSrcPort() + c.getDesIP()
								+ c.getDesPort());
						ConnectionInfo.put(index++, cc);
						// System.err.println("##### in start set: "+cc.toString());
						recordConnection(ConnLog_bw, cc);
					}

					// ConnLog_bw.append(local_addr+","+rm_addr+","+uid+"\n");
					// ConnLog_bw.flush();
					// for(int i=0;i<temp.length;i++)
					// {
					// System.out.println(""+i+"-"+temp[i]);
					// }
					// System.out.print("Dropbox connection: "+parseAddr(local_addr)[0]+":"+parseAddr(local_addr)[1]+"     ");
					// System.out.println(parseAddr(rm_addr)[0]+":"+parseAddr(rm_addr)[1]);

					Thread.sleep(1000);
					tcp_br.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void startRecording() {
		if (cr == null) {
			running = true;
			ConnLog = new File("/sdcard/network_log/conn_log.csv");
			if (ConnLog.exists()) {
				ConnLog.delete();
			}
			try {
				ConnLog.createNewFile();
				ConnLog_bw = new BufferedWriter(new FileWriter(ConnLog));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			cr = new ConnRecorder();
			cr.start();
		}
	}

	public void stopRecording() {
		if (cr != null) {
			running = false;
			try {
				ConnLog_bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private String[] parseAddr(String s) {
		// System.out.println(s);
		String[] result = new String[2];
		String[] IP_Port = s.split(":");
		String IP = IP_Port[0].substring(IP_Port[0].length() - 8);
		String IP_1 = Integer.valueOf(IP.substring(0, 2), 16).toString();
		String IP_2 = Integer.valueOf(IP.substring(2, 4), 16).toString();
		String IP_3 = Integer.valueOf(IP.substring(4, 6), 16).toString();
		String IP_4 = Integer.valueOf(IP.substring(6, 8), 16).toString();

		result[0] = IP_4 + "." + IP_3 + "." + IP_2 + "." + IP_1;

		result[1] = Integer.valueOf(IP_Port[1], 16).toString();

		return result;
	}

	private HashSet<Connection> differencing(HashSet<Connection> A,
			HashSet<Connection> B) {
		HashSet<Connection> C = new HashSet<Connection>();
		C.clear();
		C.addAll(A);
		C.removeAll(B);
		return C;
	}

	private void recordConnection(BufferedWriter bw, Connection c) {
		try {
			bw.append(c.getSrcIP() + ",");
			bw.append(c.getSrcPort() + ",");
			bw.append(c.getDesIP() + ",");
			bw.append(c.getDesPort() + ",");
			bw.append(c.getStartTime() + ",");
			bw.append(c.getEndTime() + "");
			bw.append("\n");
			bw.flush();
			System.out.println("****append a new connection!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
