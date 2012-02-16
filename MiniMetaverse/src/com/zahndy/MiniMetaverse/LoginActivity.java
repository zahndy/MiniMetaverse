package com.zahndy.MiniMetaverse;

import libomv.GridClient;
import com.zahndy.MiniMetaverse.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;

public class LoginActivity extends Activity {

	@SuppressWarnings("unused")
	private Button login;

	public GridClient Client;

	public String FirstName;

	public String LastName;

	public String Password;

	protected boolean loggedIn = false;

	public boolean getLoggedIn() {
		return loggedIn;
	}
	
    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login_layout);

        //.print("system: you are offline.");
        
        login = (Button)findViewById(R.id.loginbutton);
        final Button login = (Button)findViewById(R.id.loginbutton);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
			public void onClick(View v) {
            	log_in();
            }
        });
        
    }
    
    EditText Fnametxt = (EditText)findViewById(R.id.firstname);
    EditText Lnametxt = (EditText)findViewById(R.id.lastname);
    EditText Passtxt = (EditText)findViewById(R.id.password);
    
    void log_in()
    {
    	FirstName = Fnametxt.getText().toString();
		LastName = Lnametxt.getText().toString();
		Password = Passtxt.getText().toString();

		switchTabInActivity(1);  
		try {
			//Client = new GridClient();
			ChatActivity.print("System: loggin in...");
			//Spawn();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ChatActivity.print("Error: failed to log in");
		}
    	
    	
    	
    	
    }
    
    public void switchTabInActivity(int indexTabToSwitchTo){
    	MiniMetaverse ta = (MiniMetaverse) this.getParent();
    	TabHost th = ta.getMyTabHost();
    	th.setCurrentTab(indexTabToSwitchTo);
    }
    
    /*public boolean Spawn() throws Exception
	{
		if (!loggedIn)
		{
			Kill();
		}

		LoginParams loginParams = Client.Login.DefaultLoginParams(FirstName, LastName, Password, "last");
		loginParams.URI = LOGIN_SERVER;
		loggedIn = Client.Login.Login(loginParams);
		return loggedIn;
	}
     */
	public void Kill() throws Exception {
		if (loggedIn) {
			Client.Network.Logout();
		}
	}
    
}