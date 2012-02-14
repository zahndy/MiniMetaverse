package com.zahndy.MiniMetaverse;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

import com.zahndy.MiniMetaverse.*;
import com.zahndy.MiniMetaverse.R;

public class MiniMetaverse extends TabActivity {
	
	TabHost tabHost;
	
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);
	    tabHost = getTabHost();
	    Resources res = getResources(); // Resource object to get Drawables
	      // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab
	 
	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this,LoginActivity.class);

	    // Initialize a TabSpec for each tab and add it to the TabHost
	    spec = tabHost.newTabSpec("login").setIndicator("Login",
	                      res.getDrawable(R.drawable.ic_tab_login))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    // Do the same for the other tabs
	    intent = new Intent().setClass(this, ChatActivity.class);
	    spec = tabHost.newTabSpec("chat").setIndicator("Chat",
	                      res.getDrawable(R.drawable.ic_tab_chat))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    intent = new Intent().setClass(this, SystemActivity.class);
	    spec = tabHost.newTabSpec("system").setIndicator("System",
	                      res.getDrawable(R.drawable.ic_tab_system))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    tabHost.setCurrentTab(0);
	    
	    
	}
	
	public void switchTab(int tab){
        tabHost.setCurrentTab(tab);
	}
	
	public TabHost getMyTabHost() { return tabHost; }

}
