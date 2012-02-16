package com.zahndy.MiniMetaverse;

import libomv.AgentManager.ChatAudibleLevel;
import libomv.AgentManager.ChatCallbackArgs;
import libomv.AgentManager.ChatType;
import libomv.LoginManager.LoginParams;
import libomv.LoginManager.LoginProgressCallbackArgs;
import libomv.LoginManager.LoginStatus;
import libomv.GridClient;
import libomv.Settings;
import libomv.capabilities.CapsMessage.UploadObjectAssetMessage.Object;
import libomv.types.UUID;
import libomv.utils.Callback;
import android.os.Bundle;

public class ZahndyBot {

	private static final String VERSION = "0.4";
	public static GridClient client;
	public static String fullname;
	public static boolean LoggedIn = false;
	
	public static ZahndyBot globalInstance;
	public static ZahndyBot GlobalInstance;
	public void onCreate(Bundle savedInstanceState) {
		
		    
	}


	
    
	public void Log_In(final String firstname, final String lastname, String password)
	{
		fullname = firstname + " " + lastname;
		LoginParams loginParams = client.Login.DefaultLoginParams(firstname, lastname,
				password, "MiniMetaverse", VERSION);
		loginParams.Start = "last";
		loginParams.URI = Settings.AGNI_LOGIN_SERVER;
		 
		try {
			ZahndyBot globalInstance = new ZahndyBot(new GridClient());
			@SuppressWarnings("unused")
			ZahndyBot GlobalIstance = globalInstance;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			 ChatActivity.print("Error: failed to create globalinstance.");
		}    
	    
		
		//client.Self.ChatFromSimulator += new EventHandler<ChatEventArgs>(Self_ChatFromSimulator); //Handles Chat from sim
		
		Callback<LoginProgressCallbackArgs> loginCallback = new Callback<LoginProgressCallbackArgs>()
				{
			@Override
			public boolean callback(LoginProgressCallbackArgs e)
			{
				ChatActivity.print(String.format("Login %s: %s", e.getStatus(), e.getMessage()));

				if (e.getStatus() == LoginStatus.Success)
				{

					ChatActivity.print("Logged in " + client.toString());
					SetClientTag();
					return true;
				}
				else if (e.getStatus() == LoginStatus.Failed)
				{
					ChatActivity.print("Failed to login " + firstname + " " + lastname + ": " + e.getMessage());
					return true;
				}
				return false;
			}
		};
		
		
		
		try {
			client.Login.RequestLogin(loginParams, loginCallback);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			ChatActivity.print("Failed to login " + firstname + " " + lastname + ": " + e1.getMessage());
		}
     
		
	}
	

    public static void SetClientTag()
    {
        client.Settings.CLIENT_IDENTIFICATION_TAG = new UUID("3da8a69a-58ca-023f-2161-57f2ab3b5702");
    }

    public ZahndyBot(GridClient client0)
    {
        GlobalInstance = this;

        client = client0;
    }
    
    public static void Self_ChatFromSimulator(Object sender, ChatCallbackArgs e)
    {
        //From audible chat only, and don't reply to starttyping message or stoptyping message
        if (e.getAudible() == ChatAudibleLevel.Fully && e.getType() != ChatType.StartTyping && e.getType() != ChatType.StopTyping)
        {
            //Don't Listen to yourself
            if (e.getFromName() != fullname)
            {
                //Display Every Chat Message on console window
                ChatActivity.print("[Local] " + e.getFromName() + ": " + e.getMessage());
            } 
        } 
    } 
    
    static void TypeChat(String say, int channel, ChatType type)
    {
    	
    	try {
			client.Self.Chat(say, channel, type);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ChatActivity.print("Error: failed to send text.");
		}
    }
    
    public static void Logout(ZahndyBot client0) throws Exception
	{
		client.Network.Logout();
		ChatActivity.print("System: Logged out");
	}
}
