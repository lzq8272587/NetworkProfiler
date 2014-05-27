package com.lzq.networkstatelistener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Random;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.TextView;

public class NetworkInfoMonitorService extends Service {
	final String TAG = "NetworkService";
	final String LogPath = "/sdcard/network_log/"; 
	final String NetworkInfoBase = "NetworkInfo at "
			+ System.currentTimeMillis()
			+ "\n"
			+ "Time,getExtraInfo(),getReason(),getSubtype(),getSubtypeName(),getType(),getTypeName(),getDetailedState(),getState(),getAvailMemory(),getCurCpuFreq(),CpuFreq_sum,cellid\n";
	final String PhoneStateBase = "PhoneState at "
			+ System.currentTimeMillis()
			+ "\n"
			+ "Time,isGsm(),getCdmaDbm(),getCdmaEcio(),getGsmSignalStrength(),getGsmBitErrorRate()"
			+ "\n";

	TelephonyManager tel;
	ConnectivityManager connectMgr;
	NetworkInfo info;
	NetworkInfoRecorder nir;

	/**
	 * 记录下运行过程中终端的网络信息，CPU,RAM
	 */
	File NetworkInfoLog = null;
	File PhoneStateLog = null;

	BufferedWriter NetworkInfoLog_bw = null;
	BufferedWriter PhoneStateLog_bw = null;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		connectMgr = (ConnectivityManager) this
				.getSystemService(Context.CONNECTIVITY_SERVICE);

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (nir != null)
			nir.stopRecording();
		try {
			if (NetworkInfoLog_bw != null) {

				NetworkInfoLog_bw.flush();

				NetworkInfoLog_bw.close();
			}
			if (PhoneStateLog_bw != null) {
				PhoneStateLog_bw.flush();
				PhoneStateLog_bw.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		/**
		 * 初始化记录信息要用到的相关IO类
		 */
		try {
			NetworkInfoLog = new File(LogPath + "NetworkInfoLog_"
					+ new Date().toString().replace(" ", "_").replace("+", "_").replace(":", "_") + ".csv");
			System.out.println(NetworkInfoLog.getAbsolutePath());
			NetworkInfoLog.createNewFile();
			PhoneStateLog = new File(LogPath + "PhoneStateLog_"
					+ new Date().toString().replace(" ", "_").replace("+", "_").replace(":", "_") + ".csv");
			PhoneStateLog.createNewFile();
			
			
			NetworkInfoLog_bw = new BufferedWriter(new FileWriter(
					NetworkInfoLog));
			PhoneStateLog_bw = new BufferedWriter(new FileWriter(PhoneStateLog));
			NetworkInfoLog_bw.append(NetworkInfoBase);
			NetworkInfoLog_bw.flush();
			PhoneStateLog_bw.append(PhoneStateBase);
			PhoneStateLog_bw.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/**
		 * 启动一个线程不断循环，读取当前网络状态
		 */
		nir = new NetworkInfoRecorder();
		nir.start();

		
		/**
		 * 启动一个上行速率测试线程，不断向目标服务器发送随机字节，以统计上行发送速率变化情况 
		 */
		UploadSpeedTestThread ustt=new UploadSpeedTestThread();
		ustt.start();
		
		/**
		 * 开始监听信号强度
		 */
		tel.listen(new PhoneStateMonitor(),
				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
						| PhoneStateListener.LISTEN_SERVICE_STATE);

		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * 记录网络状态的进程 在此进程中，每秒钟刷新当前设备的网络状态，例如网络制式，是否在服务区内等信息
	 * 
	 * @author LZQ
	 */
	private class NetworkInfoRecorder extends Thread {
		boolean running = true;

		public void run() {
			Log.e(TAG, "NetworkInfoRecorder is starting for recording.");
			while (running) {

				info = connectMgr.getActiveNetworkInfo();
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				GsmCellLocation location = (GsmCellLocation) tel.getCellLocation();
				int cellid = location .getCid();
				if (info == null) {
					Log.v("NETSTAT", "no network detected:");
				} else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
					Log.v("NETSTAT", "wifi network detected:" + info.toString());
				} else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
					MainActivity_old.UIHandler.post(new UIUpdater(
							MainActivity_old.NetworkInfoTextView, info.toString()));
					try {
						NetworkInfoLog_bw.append(""+System.currentTimeMillis()+","+info.getExtraInfo() + ","
								+ info.getReason() + "," + info.getSubtype()
								+ "," + info.getSubtypeName() + ","
								+ info.getType() + "," + info.getTypeName()
								+ "," + info.getDetailedState() + ","
								+ info.getState() + ","+getAvailMemory()+","+getCurCpuFreq()+","+cellid+"\n");
						NetworkInfoLog_bw.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					Log.v("NETSTAT",
							"cellular network detected:" + info.toString()+","+cellid);

				}
			}
			Log.e(TAG, "NetworkInfoRecorder is stop.");
		}

		
		public long getAvailMemory() {
			ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
			ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
			am.getMemoryInfo(mi);
			return mi.availMem;
		}

        // 实时获取CPU当前频率（单位KHZ）
       public String getCurCpuFreq() {
               StringBuffer result = new StringBuffer();
               int cpuIndex=0;
               FileReader fr = null;
               BufferedReader br = null;
               double sum=0;
               try {
            	   while(true)
            	   {
            		   File cpuFile=new File("/sys/devices/system/cpu/cpu"+cpuIndex);
            		   if(!cpuFile.exists())
            			   break;
                       fr = new FileReader(
                                       "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq");
                       br = new BufferedReader(fr);
                       String text = br.readLine();
                       result.append("core"+cpuIndex+text.trim()+" ");
                       cpuIndex++;
                       sum+=Double.parseDouble(text.trim());
            	   }
            	   fr.close();
            	   br.close();
            	   
               } catch (FileNotFoundException e) {
                       e.printStackTrace();
               } catch (IOException e) {
                       e.printStackTrace();
               }
               return result.toString()+","+sum;
       }

       
		public void stopRecording() {
			running = false;
		}
	}

	/**
	 * 内部类，用于监听设备的信号强度
	 * 
	 * @author LZQ
	 * 
	 */
	class PhoneStateMonitor extends PhoneStateListener {
		
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			super.onSignalStrengthsChanged(signalStrength);
			/*
			 * signalStrength.isGsm() 是否GSM信号 2G or 3G
			 * signalStrength.getCdmaDbm(); 联通3G 信号强度
			 * signalStrength.getCdmaEcio(); 联通3G 载干比
			 * signalStrength.getEvdoDbm(); 电信3G 信号强度
			 * signalStrength.getEvdoEcio(); 电信3G 载干比
			 * signalStrength.getEvdoSnr(); 电信3G 信噪比
			 * signalStrength.getGsmSignalStrength(); 2G 信号强度
			 * signalStrength.getGsmBitErrorRate(); 2G 误码率
			 * 
			 * 载干比 ，它是指空中模拟电波中的信号与噪声的比值
			 */
			NetworkStateFragment.UIHandler.post(new UIUpdater(
					NetworkStateFragment.ThreegTextView, "IsGsm : "
							+ signalStrength.isGsm() + "\nCDMA Dbm : "
							+ signalStrength.getCdmaDbm() + "Dbm"
							+ "\nCDMA Ecio : " + signalStrength.getCdmaEcio()
							+ "dB*10" + "\nEvdo Dbm : "
							+ signalStrength.getEvdoDbm() + "Dbm"
							+ "\nEvdo Ecio : " + signalStrength.getEvdoEcio()
							+ "dB*10" + "\nGsm SignalStrength : "
							+ signalStrength.getGsmSignalStrength()
							+ "\nGsm BitErrorRate : "
							+ signalStrength.getGsmBitErrorRate()));

			Log.e("NETSTAT",
					"IsGsm : " + signalStrength.isGsm() + "\nCDMA Dbm : "
							+ signalStrength.getCdmaDbm() + "Dbm"
							+ "\nCDMA Ecio : " + signalStrength.getCdmaEcio()
							+ "dB*10" + "\nEvdo Dbm : "
							+ signalStrength.getEvdoDbm() + "Dbm"
							+ "\nEvdo Ecio : " + signalStrength.getEvdoEcio()
							+ "dB*10" + "\nGsm SignalStrength : "
							+ signalStrength.getGsmSignalStrength()
							+ "\nGsm BitErrorRate : "
							+ signalStrength.getGsmBitErrorRate());

			try {
				PhoneStateLog_bw.append(""+System.currentTimeMillis()+","+signalStrength.isGsm() + ","
						+ signalStrength.getCdmaDbm() + ","
						+ signalStrength.getCdmaEcio() + ","
						+ signalStrength.getGsmSignalStrength() + ","
						+ signalStrength.getGsmBitErrorRate() + "\n");
				PhoneStateLog_bw.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// mIcon3G.setImageLevel(Math.abs(signalStrength.getGsmSignalStrength()));
		}

		public void onServiceStateChanged(ServiceState serviceState) {
			super.onServiceStateChanged(serviceState);

			/*
			 * ServiceState.STATE_EMERGENCY_ONLY 仅限紧急呼叫
			 * ServiceState.STATE_IN_SERVICE 信号正常
			 * ServiceState.STATE_OUT_OF_SERVICE 不在服务区
			 * ServiceState.STATE_POWER_OFF 断电
			 */
			switch (serviceState.getState()) {
			case ServiceState.STATE_EMERGENCY_ONLY:
				Log.d(TAG, "3G STATUS : STATE_EMERGENCY_ONLY");
				break;
			case ServiceState.STATE_IN_SERVICE:
				Log.d(TAG, "3G STATUS : STATE_IN_SERVICE");
				break;
			case ServiceState.STATE_OUT_OF_SERVICE:
				Log.d(TAG, "3G STATUS : STATE_OUT_OF_SERVICE");
				break;
			case ServiceState.STATE_POWER_OFF:
				Log.d(TAG, "3G STATUS : STATE_POWER_OFF");
				break;
			default:
				break;
			}
		}

		@Override
		public void onCellLocationChanged(CellLocation location) {
			// TODO Auto-generated method stub
			super.onCellLocationChanged(location);
			
		}
		
		
	}

	
	private class UploadSpeedTestThread extends Thread
	{
		
		
		
		public void run()
		{
			try {
				Socket socket=new Socket(Utils.SERVER_IP,Utils.SERVER_PORT);
				byte[] buff=new byte[1024];
				int account=0;
				Random random=new Random();
				
				while(true)
				{
					Thread.sleep(1000);
					random.nextBytes(buff);
					socket.getOutputStream().write(buff);
				}
				
				
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 用于更新UI操作的Runnable对象
	 * 
	 * @author LZQ
	 * 
	 */
	private class UIUpdater implements Runnable {

		String UpdatedInfo = null;
		TextView tv = null;

		UIUpdater(TextView t, String s) {
			UpdatedInfo = s;
			tv = t;

		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			tv.setText(UpdatedInfo);
		}
	}
}
