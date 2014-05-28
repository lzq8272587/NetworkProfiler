package com.lzq.networkstatelistener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class TraceExchangeFragment extends Fragment {

	
	Button UploadTraceButton=null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View v=inflater.inflate(R.layout.fragment_traceexchange, container,false);
		UploadTraceButton=(Button)v.findViewById(R.id.UploadTraceButton);
		UploadTraceButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				File LogFolder=new File("/sdcard/network_log");
				if(!LogFolder.exists())
				{
					//not exist
				}
				else
				{
					File[] LogList=LogFolder.listFiles();
					File tempFolder=new File("/sdcard/.network_log_temp");
					if(tempFolder.exists())
						tempFolder.delete();
					tempFolder.mkdir();
					File tempZip=new File("/sdcard/.network_log_temp/temp.zip");
					try {
						tempZip.createNewFile();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String[] srcFilePath=new String[LogList.length];
					for(int i=0;i<LogList.length;i++)
					{
						srcFilePath[i]=LogList[i].getAbsolutePath();
					}
					String desFilePath=tempZip.getAbsolutePath();
					
					ZipCompressor zc=new ZipCompressor();
					zc.zipCompress(srcFilePath, desFilePath);
					
					Uploader ul=new Uploader(tempZip);
					ul.start();
					
				}
			}
		});
		return v;
	}

	
	class Uploader extends Thread
	{
		File ZipFile=null;
		public void run()
		{
			try {
				Socket socket=new Socket(Utils.SERVER_IP,Utils.SERVER_PORT);
				
				int count=0;
				byte[] buff=new byte[1024];
				
				FileInputStream is=new FileInputStream(ZipFile);
				while((count=is.read(buff))!=0)
				{
					socket.getOutputStream().write(buff,0,count);
					socket.getOutputStream().flush();
				}
				
				socket.close();
				ZipFile.deleteOnExit();
				
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		public Uploader(File f)
		{
			ZipFile=f;
		}
	}
}



