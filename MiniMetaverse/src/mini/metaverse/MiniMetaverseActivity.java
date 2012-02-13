package mini.metaverse;

import android.app.Activity;
//import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
//import android.view.View;
import android.widget.Button;
//import android.widget.EditText;
//import android.widget.FrameLayout;
//import android.widget.TextView;
import android.widget.TextView;


public class MiniMetaverseActivity extends Activity {
	
	private TextView outputtxt;  
	/*private EditText firstn;
	private EditText lastn;
	private EditText passw;
	private String firstname;
	private String lastname;
	private String password;
	private FrameLayout inputcontainer;
	private FrameLayout LoginForm;
	//private FrameLayout menu;
	*/
	private String Saytext;
	private EditText inputfield;
	
	private Button chatTabBtn;
	private Button imsTabBtn;
	private Button contactsTabBtn;
	private Button systemTabBtn; 
	
	private LinearLayout LocalChat;
	private LinearLayout IMs;
	private LinearLayout Contacts;
	private LinearLayout System;
	
	private LinearLayout LoginScreen;
	private LinearLayout MainScreen;
	//public String client_mac= getMacAddress(); // Currently useless
	private Button LogInBtn;
	private Button btnLocalChat;
	private Button LogOutBtn;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        /*firstn = (EditText)findViewById(R.id.firstname);
        lastn = (EditText)findViewById(R.id.lastname);
        passw = (EditText)findViewById(R.id.password);
        
        
        
     
        
        outputtxt = (TextView)findViewById(R.id.outputtext);
        outputtxt.setText("welcome to Minimetaverse");
        inputfield = (EditText)findViewById(R.id.inputfield);
        inputcontainer = (FrameLayout)findViewById(R.id.frameLayout1);
        LoginForm = (FrameLayout)findViewById(R.id.loginform);
       */
        // android.os.Process.killProcess(android.os.Process.myPid());
        //log_in();
        inputfield = (EditText)findViewById(R.id.inputfield);
        outputtxt = (TextView)findViewById(R.id.outputtext);
        
        
        
        LocalChat = (LinearLayout)findViewById(R.id.localchat);
        IMs = (LinearLayout)findViewById(R.id.ims);
        Contacts = (LinearLayout)findViewById(R.id.contacts);
        System = (LinearLayout)findViewById(R.id.system);
        
        LoginScreen = (LinearLayout)findViewById(R.id.Login_Screen);
        MainScreen = (LinearLayout)findViewById(R.id.Main_Screen);
        
        LogInBtn = (Button) findViewById(R.id.loginbutton);
        final Button LogInBtn = (Button) findViewById(R.id.loginbutton);
        LogInBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	ShowLocalChat();
            	tab_chat();
            }
        });
        
        chatTabBtn = (Button)findViewById(R.id.chatmenubtn);
        final Button chatTabBtn = (Button)findViewById(R.id.chatmenubtn);
        chatTabBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	tab_chat();
            }
        });
        
        imsTabBtn = (Button)findViewById(R.id.IMmenubtn);
        final Button imsTabBtn = (Button)findViewById(R.id.IMmenubtn);
        imsTabBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	tab_ims();
            }
        });
        
        contactsTabBtn = (Button)findViewById(R.id.contactsmenubtn);
        final Button contactsTabBtn = (Button)findViewById(R.id.contactsmenubtn);
        contactsTabBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	tab_contacts();
            }
        });
        
        systemTabBtn = (Button)findViewById(R.id.systemmenubtn);
        final Button systemTabBtn = (Button)findViewById(R.id.systemmenubtn);
        systemTabBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	tab_system();
            }
        });
        
        btnLocalChat = (Button)findViewById(R.id.saybutton);
        final Button btnLocalChat = (Button)findViewById(R.id.saybutton);
        btnLocalChat.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Say_something();
            }
        });
        
        LogOutBtn = (Button)findViewById(R.id.logoff);
        final Button LogOutBtn = (Button)findViewById(R.id.logoff);
        btnLocalChat.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	LogOff();
            }
        });
    }
   /*
    public void log_in()
    {
    	//firstname = firstn.getText().toString();
    	//lastname = lastn.getText().toString();
    	//password = passw.getText().toString();
    	if(firstname.length()<=1 || lastname.length()<=1 || password.length()<=1)
    	{
    		print("please fill in all fields");
    	
    	}
    	else
    	{
    	
    		LoginForm.setVisibility(8);
    		//menu.setVisibility(0);
    		ShowLocalChat();
    		
    		print("Logging in...");
    		connect();
    		
    	}
    	
    }
    */
    public void Say_something()
    {
    	
    	Saytext = inputfield.getText().toString();
    	print(Saytext);
    	inputfield.setText("");
    	
    }
    
    public void print(String text)
    {
    	outputtxt.setText(outputtxt.getText()+"\n"+String.valueOf(text));
    }
    
    public void ShowLocalChat()
    {
    	LoginScreen.setVisibility(8);
    	MainScreen.setVisibility(0);
    }
    
    public void tab_chat()
    {
    	LocalChat.setVisibility(0);
    	chatTabBtn.setEnabled(false);
    	
    	IMs.setVisibility(8);
    	imsTabBtn.setEnabled(true);
    	
    	Contacts.setVisibility(8);
    	contactsTabBtn.setEnabled(true);
    	
    	System.setVisibility(8);
    	systemTabBtn.setEnabled(true);
    }
    
    public void tab_ims()
    {
    	LocalChat.setVisibility(8);
    	chatTabBtn.setEnabled(true);
    	
    	IMs.setVisibility(0);
    	imsTabBtn.setEnabled(false);
    	
    	Contacts.setVisibility(8);
    	contactsTabBtn.setEnabled(true);
    	
    	System.setVisibility(8);
    	systemTabBtn.setEnabled(true);
    }
    
    public void tab_contacts()
    {
    	LocalChat.setVisibility(8);
    	chatTabBtn.setEnabled(true);
    	
    	IMs.setVisibility(8);
    	imsTabBtn.setEnabled(true);
    	
    	Contacts.setVisibility(0);
    	contactsTabBtn.setEnabled(false);
    	
    	System.setVisibility(8);
    	systemTabBtn.setEnabled(true);
    }
    
    public void tab_system()
    {
    	LocalChat.setVisibility(8);
    	chatTabBtn.setEnabled(true);
    	
    	IMs.setVisibility(8);
    	imsTabBtn.setEnabled(true);
    	
    	Contacts.setVisibility(8);
    	contactsTabBtn.setEnabled(true);
    	
    	System.setVisibility(0);
    	systemTabBtn.setEnabled(false);
    }
    
    public void placeholder()
    {
    	print("this is a placeholder, it does not have a function yet");
    }
    
    public void murder(View view)
    {
    	android.os.Process.killProcess(android.os.Process.myPid());
    }
    
    /*
    //// get mac-address here
    
    public static String loadFileAsString(String filePath) throws java.io.IOException{
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }

    
    public String getMacAddress(){
        try {
            return loadFileAsString("/sys/class/net/eth0/address")
                .toUpperCase().substring(0, 17);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    */
    
    //// end of fetching mac adress
    
    // connect
    
    public void connect()
    {
    	placeholder();
    }
    
    public void LogOff()
    {
    	placeholder();
    }
    
}

