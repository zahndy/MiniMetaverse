/**
 * Copyright (c) 2007-2008, openmetaverse.org
 * Copyright (c) 2009-2011, Frederick Martian
 * All rights reserved.
 *
 * - Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are met:
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Neither the name of the openmetaverse.org nor the names
 *   of its contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package libomv.utils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.nio.reactor.IOReactorException;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDFormat;
import libomv.StructuredData.OSD.OSDType;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.capabilities.CapsClient;
import libomv.types.UUID;
import libomv.types.Vector3;

public class RegistrationApi
{
    final int REQUEST_TIMEOUT = 1000 * 100;

    private class UserInfo
    {
        public String FirstName;
        public String LastName;
        public String Password;
    }

    private class RegistrationCaps
    {
        public URI CreateUser;
        public URI CheckName;
        public URI GetLastNames;
        public URI GetErrorCodes;
    }

    public class ErrorCode
    {
    	public int Code;
    	public String Name;
    	public String Description;
    	
    	public ErrorCode(int code, String name, String description)
    	{
    		Code = code;
    		Name = name;
    		Description = description;
    	}
    	
    	@Override
		public String toString()
    	{
    		return String.format("Code: %d, Name: %s, Description: %s", Code, Name, Description);
    	}
    }

    // See https://secure-web6.secondlife.com/developers/third_party_reg/#service_create_user or
    // https://wiki.secondlife.com/wiki/RegAPIDoc for description
    public class CreateUserParam
    {
        public String FirstName;
        public int LastNameID;
        public String Email;
        public String Password;
        public Date Birthdate;

        // optional:
        public Integer LimitedToEstate;
        public String StartRegionName;
        public Vector3 StartLocation;
        public Vector3 StartLookAt;
    }

    private UserInfo _userInfo;
    private RegistrationCaps _caps;
    private int _initializing;
    private Map<Integer, ErrorCode> _errors;
    private Map<String, Integer> _lastNames;

    public boolean getInitializing()
    {
        return (_initializing < 0);
    }

    public RegistrationApi(String firstName, String lastName, String password) throws IOReactorException, UnsupportedEncodingException, URISyntaxException, InterruptedException, ExecutionException, TimeoutException
    {
        _initializing = -2;

        _userInfo = new UserInfo();

        _userInfo.FirstName = firstName;
        _userInfo.LastName = lastName;
        _userInfo.Password = password;

        getCapabilities();
    }

    public void waitForInitialization() throws InterruptedException
    {
        while (getInitializing())
            Thread.sleep(10);
    }

    private URI getRegistrationApiCaps() throws URISyntaxException
    {
        return new URI("https://cap.secondlife.com/get_reg_capabilities");
    }

    private void getCapabilities() throws URISyntaxException, IOReactorException, UnsupportedEncodingException, InterruptedException, ExecutionException, TimeoutException
    {
        // build post data
        byte[] postData = String.format("first_name=%s&last_name=%s&password=%s", _userInfo.FirstName, _userInfo.LastName, 
                                        _userInfo.Password).getBytes(Helpers.ASCII_ENCODING);

        Future<OSD> future = new CapsClient().executeHttpPost(getRegistrationApiCaps(), postData, "application/x-www-form-urlencoded", REQUEST_TIMEOUT);
        OSD response = future.get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        if (response instanceof OSDMap)
        {
            OSDMap respTable = (OSDMap)response;        	
            // parse
            _caps = new RegistrationCaps();

            _caps.CreateUser = respTable.get("create_user").AsUri();
            _caps.CheckName = respTable.get("check_name").AsUri();
            _caps.GetLastNames = respTable.get("get_last_names").AsUri();
            _caps.GetErrorCodes = respTable.get("get_error_codes").AsUri();

            // finalize
            _initializing++;

			_errors = getErrorCodes(_caps.GetErrorCodes);
        }
    }

    /**
     * Retrieves a list of error codes, and their meaning, that the RegAPI can return.
     *
     * @param capability the capability URL for the "get_error_codes" RegAPI function.
     * @return a mapping from error codes (as a number) to an ErrorCode object
     * which contains more detail on that error code.
     */
    private Map<Integer, ErrorCode> getErrorCodes(URI capability) throws IOReactorException, InterruptedException, ExecutionException, TimeoutException
    {
        final Map<Integer, ErrorCode> errorCodes = new HashMap<Integer, ErrorCode>();

        Future<OSD> future = new CapsClient().executeHttpGet(capability, null, REQUEST_TIMEOUT);
        OSD response = future.get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        if (response instanceof OSDArray)
        {
            OSDArray respTable = (OSDArray)response;        	

            for (Iterator<OSD> iter = respTable.iterator(); iter.hasNext();)
            {
            	OSDArray errors = (OSDArray)iter.next();

            	errorCodes.put(errors.get(0).AsInteger(), new ErrorCode(errors.get(0).AsInteger(), errors.get(1).AsString(), errors.get(2).AsString()));
            }

            // finalize
            _initializing++;
        }
        return errorCodes;
    }

    /**
     * Retrieves a list of valid last names for newly created accounts.
     *
     * @param capability the capability URL for the "get_last_names" RegAPI function.
     * @return a mapping from last names, to their ID (needed for createUser()).
     */
    private Map<String, Integer> getLastNames(URI capability) throws IOReactorException, InterruptedException, ExecutionException, TimeoutException
    {
        final SortedMap<String, Integer> lastNames = new TreeMap<String, Integer>();
        
        Future<OSD> future = new CapsClient().executeHttpGet(capability, null, REQUEST_TIMEOUT);
        OSD response = future.get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        if (response instanceof OSDMap)
        {
            OSDMap respTable = (OSDMap)response;

            for (Entry<String, OSD> entry : respTable.entrySet())
            {
            	lastNames.put(entry.getValue().AsString(), Integer.valueOf(entry.getKey()));
            }
        }
        return lastNames;
    }

    /**
     * Retrieves a list of valid last names for newly created accounts.
     *
     * @return a mapping from last names, to their ID (needed for createUser()).
     */
    public synchronized Map<String, Integer> getLastNames() throws IOReactorException, InterruptedException, ExecutionException, TimeoutException
    {
        if (_lastNames.size() <= 0)
        {
            if (getInitializing())
                throw new IllegalStateException("still initializing");

            if (_caps.GetLastNames == null)
                throw new UnsupportedOperationException("access denied: only approved developers have access to the registration api");

            _lastNames = getLastNames(_caps.GetLastNames);
        }
        return _lastNames;
    }

    /**
     * Checks whether a name is already used in Second Life.
     *
     * @param firstName of the name to check.
     * @param lastNameID the ID (see getLastNames() for the list of valid last name IDs) to check.
     * @return true if they already exist, false if the name is available.
     * @throws Exception 
     */
    public boolean checkName(String firstName, int lastNameID) throws Exception
    {
        if (getInitializing())
            throw new IllegalStateException("still initializing");

        if (_caps.CheckName == null)
            throw new UnsupportedOperationException("access denied; only approved developers have access to the registration api");

        // Create the POST data
        OSDMap query = new OSDMap();
        query.put("username", OSD.FromString(firstName));
        query.put("last_name_id", OSD.FromInteger(lastNameID));

        Future<OSD> future = new CapsClient().executeHttpPost(_caps.GetLastNames, query, OSDFormat.Xml, -1);
        OSD response = future.get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        if (response.getType() != OSDType.Boolean)
        	throw new Exception("check_name did not return a boolean as the only element inside the <llsd> tag.");
       	return response.AsBoolean();
    }
			
	/**
     * Returns the new user ID or throws an exception containing the error code
     * The error codes can be found here: https://wiki.secondlife.com/wiki/RegAPIError
     *
     * @param user New user account to create
     * @returns The UUID of the new user account
	 * @throws Exception 
     */
    public UUID createUser(CreateUserParam user) throws Exception
    {
        if (getInitializing())
            throw new IllegalStateException("still initializing");

        if (_caps.CreateUser == null)
            throw new UnsupportedOperationException("access denied; only approved developers have access to the registration api");

        // Create the POST data
        OSDMap query = new OSDMap();
        query.put("username", OSD.FromString(user.FirstName));
        query.put("last_name_id", OSD.FromInteger(user.LastNameID));
        query.put("email", OSD.FromString(user.Email));
        query.put("password", OSD.FromString(user.Password));
        query.put("dob", OSD.FromString(new SimpleDateFormat("yyyy-MM-dd").format(user.Birthdate)));

        if (user.LimitedToEstate != null)
            query.put("limited_to_estate", OSD.FromInteger(user.LimitedToEstate));

        if (user.StartRegionName != null && !user.StartRegionName.isEmpty())
            query.put("start_region_name", OSD.FromString(user.StartRegionName));

        if (user.StartLocation != null)
        {
            query.put("start_local_x", OSD.FromReal(user.StartLocation.X));
            query.put("start_local_y", OSD.FromReal(user.StartLocation.Y));
            query.put("start_local_z", OSD.FromReal(user.StartLocation.Z));
        }

        if (user.StartLookAt != null)
        {
            query.put("start_look_at_x", OSD.FromReal(user.StartLookAt.X));
            query.put("start_look_at_y", OSD.FromReal(user.StartLookAt.Y));
            query.put("start_look_at_z", OSD.FromReal(user.StartLookAt.Z));
        }

        // Make the request
        Future<OSD> future = new CapsClient().executeHttpPost(_caps.CreateUser, query, OSDFormat.Xml, -1);
        OSD response = future.get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        if (response instanceof OSDMap)
        {
        	OSDMap map = (OSDMap)response;
        	return map.get("agent_id").AsUUID();
        }
        
		// an error happened
		OSDArray al = (OSDArray)response;

		StringBuilder sb = new StringBuilder();

		for (OSD ec : al)
		{
		    if (sb.length() > 0)
		        sb.append("; ");

		    sb.append(_errors.get(ec.AsInteger()));
		}
		throw new Exception("failed to create user: " + sb.toString());
    }
}