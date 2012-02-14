package com.zahndy.MiniMetaverse;

import com.zahndy.MiniMetaverse.R;

import android.app.Activity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TextView;

public class ChatActivity extends Activity {
	
	private String Saytext;
	private Button send;
	private EditText inputLine;
	private static TextView outputtxt; 
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout);
        
        inputLine = (EditText)findViewById(R.id.message);
        outputtxt = (TextView)findViewById(R.id.outputtext);
        
        send = (Button)findViewById(R.id.sendButton);
        final Button send = (Button)findViewById(R.id.sendButton);
        send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	say();
            }
        });
        print("system: you are offline.");
    }
    
    public void say()
    {
    	Saytext = inputLine.getText().toString();
    	print("me: "+Saytext);
    	inputLine.setText("");
    }
    
    public static void print(String text)
    {
    	outputtxt.setText(outputtxt.getText()+"\n"+String.valueOf(text));
    }

}