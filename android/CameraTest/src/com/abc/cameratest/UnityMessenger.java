package com.abc.cameratest;
import com.unity3d.player.*;
import java.util.ArrayList;
import android.util.Log;

public class UnityMessenger
{
	//消息定义
	//平台相关; 1 - 100;
	public static int MSG_PLATFORM_LOGIN = 1;
	public static int MSG_PLATFORM_LOGOUT = 2;
	public static int MSG_PLATFORM_BACKTOGAME = 11;
	public static int MSG_PLATFORM_EXIT = 12;
	public static int MSG_PLATFORM_PAYORDER = 21;
	public static int MSG_PLATFORM_RECEIPT = 22;
	
    //101-200
    public static int MSG_TAKEPHOTO = 101;
    public static int MSG_BACK_PRESSED = 102;
	public static int MSG_UNZIP_PROGRESS = 103;
	
	//常数定义
	public static String RET_TRUE = "ture";
	public static String RET_FALSE = "false";
	
	
	static ArrayList<String> Listeners = new ArrayList<String>();
	static char Delemeter = (char)1;
	public static void AddMessageCenter(String path)
	{
		Listeners.add(path);
	}
	
	public static void SendMessage(String msg)
	{
		Log.d("Unity", "SendMessage: " + msg + " listener " + Listeners.get(0));
		UnityPlayer.UnitySendMessage(Listeners.get(0), "OnMessage", msg);
		Log.d("Unity", "SendMessage over");
	}
	
	public static void SendMessage(int msg, String...text)
	{
		int length = text.length;
		String param = "";
		for(int i = 0; i < length; i++)
		{
			if(i == 0)
				param = text[i];
			else
				param = param + Delemeter + text[i];
		} 
		for(int i = 0; i < Listeners.size(); i++)
		{
			UnityPlayer.UnitySendMessage(Listeners.get(i), "OnMessage", "" + msg + ":" + param);
		}
	}
}