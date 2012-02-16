package com.zahndy.MiniMetaverse;

import libomv.AgentManager.ChatType;

import com.zahndy.MiniMetaverse.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ChatActivity extends Activity {
	
	private String Saytext;
	@SuppressWarnings("unused")
	private Button send;
	private EditText inputLine;
	private static TextView outputtxt; 
	
    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout);
        
        inputLine = (EditText)findViewById(R.id.message);
        outputtxt = (TextView)findViewById(R.id.outputtext);
        
        send = (Button)findViewById(R.id.sendButton);
        final Button send = (Button)findViewById(R.id.sendButton);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
			public void onClick(View v) {
            	say();
            }
        });
        print("system: you are offline.");
    }
    
    public void say()
    {
    	Saytext = inputLine.getText().toString();
    	if(ZahndyBot.LoggedIn == true)
    	{
    		ZahndyBot.TypeChat(Saytext, 0 , ChatType.Normal);
    	}
    	
    	print("me: "+Saytext);
    	inputLine.setText("");
    }
    
    public static void print(String text)
    {
    	outputtxt.setText(outputtxt.getText()+"\n"+String.valueOf(text));
    }

}