package com.zahndy.MiniMetaverse;

import com.zahndy.MiniMetaverse.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TabHost;

public class SystemActivity extends Activity {
	@SuppressWarnings("unused")
	private Button kill;
	@SuppressWarnings("unused")
	private Button logout;
	
    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        
        setContentView(R.layout.system_layout);
        
        
        kill = (Button)findViewById(R.id.shutdown);
        final Button kill = (Button)findViewById(R.id.shutdown);
        kill.setOnClickListener(new View.OnClickListener() {
            @Override
			public void onClick(View v) {
            	murder();
            }
        });
        
        logout = (Button)findViewById(R.id.logoff);
        final Button logout = (Button)findViewById(R.id.logoff);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
			public void onClick(View v) {
            	if(ZahndyBot.LoggedIn == true)
            	{
            		try {
						ZahndyBot.Logout(null);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            	}
            	switchTabInActivity(1);
            }
        });
        
    }
    
    public void murder()
    {
    	android.os.Process.killProcess(android.os.Process.myPid());
    }
    
    public void switchTabInActivity(int indexTabToSwitchTo){
    	MiniMetaverse ta = (MiniMetaverse) this.getParent();
    	TabHost th = ta.getMyTabHost();
    	th.setCurrentTab(indexTabToSwitchTo);
    }
}
