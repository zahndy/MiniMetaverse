/**
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import libomv.utils.Callback;

public class CallbackHandler<T>
{
	private HashMap<Callback<T>, Boolean> callbackHandlers = null;

	public int count()
	{
		if (callbackHandlers == null)
			return 0;

		return callbackHandlers.size();
	}

	public boolean add(Callback<T> handler)
	{
		return add(handler, false);
	}

	/**
	 * Add a callback handler to the list of handlers
	 * 
	 * @param handler
	 *            The callback handler to add to the list
	 * @param autoremove
	 *            When true the callback handler is automatically removed when
	 *            invoked
	 * @return True when the callback handler replaced an earlier instance of
	 *         itself, false otherwise
	 */
	public boolean add(Callback<T> handler, boolean autoremove)
	{
		if (callbackHandlers == null)
			callbackHandlers = new HashMap<Callback<T>, Boolean>();

		synchronized (callbackHandlers)
		{
			return (callbackHandlers.put(handler, autoremove) != null);
		}
	}

	/**
	 * Remove a callback handler from the list of handlers
	 * 
	 * @param handler
	 *            The callback handler to add to the list
	 * @param autoremove
	 *            When true the callback handler is automatically removed when
	 *            invoked
	 * @return True when the callback handler was removed, false when it didn't
	 *         exist
	 */
	public boolean remove(Callback<T> handler)
	{
		if (callbackHandlers == null)
			return false;

		synchronized (callbackHandlers)
		{
			return (callbackHandlers.remove(handler) != null);
		}
	}

	/**
	 * Dispatches a callback to all registered handlers
	 * 
	 * @param args
	 *            The argument class to pass to the callback handlers
	 * @return The number of callback handlers that got invoked
	 */
	public int dispatch(T args)
	{
		int count = 0;

		if (callbackHandlers != null)
		{
			synchronized (callbackHandlers)
			{
				Iterator<Entry<Callback<T>, Boolean>> iter = callbackHandlers.entrySet().iterator();
				while (iter.hasNext())
				{
					Entry<Callback<T>, Boolean> entry = iter.next();
					Callback<T> handler = entry.getKey();
					boolean remove = false;
					
					synchronized (handler)
					{
						remove = handler.callback(args);
						handler.notifyAll();
					}
					if (remove || entry.getValue())
						iter.remove();
					count++;
				}
			}
		}
		return count;
	}
}
