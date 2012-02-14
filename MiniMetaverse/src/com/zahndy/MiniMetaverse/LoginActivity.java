package com.zahndy.MiniMetaverse;

import com.zahndy.MiniMetaverse.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TextView;
import android.app.TabActivity;

public class LoginActivity extends Activity {

	private Button login;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login_layout);

        //.print("system: you are offline.");
        
        login = (Button)findViewById(R.id.loginbutton);
        final Button login = (Button)findViewById(R.id.loginbutton);
        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	log_in();
            }
        });
        
    }
    
    void log_in()
    {
    	
    	ChatActivity.print("loggin in...");
    	
    	switchTabInActivity(1);  
    }
    
    public void switchTabInActivity(int indexTabToSwitchTo){
    	MiniMetaverse ta = (MiniMetaverse) this.getParent();
    	TabHost th = ta.getMyTabHost();
    	th.setCurrentTab(indexTabToSwitchTo);
    }
    
}