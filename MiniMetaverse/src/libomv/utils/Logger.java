/**
 * Copyright (c) 2007-2008, openmetaverse.org
 * Portions Copyright (c) 2009-2011, Frederick Martian
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

import libomv.GridClient;
import libomv.Settings;

/* Singleton logging class for the entire library */
public final class Logger
{
	// Passed to Logger.Log() to identify the severity of a log entry
	//
	// Non-noisy useful information, may be helpful in debugging a problem
	public interface LogLevel
	{
		public final static int None = 0;

		public final static int Info = 1;

		// A non-critical error occurred. A warning will not prevent the rest of
		// libomv from
		// operating as usual, although it may be indicative of an underlying
		// issue
		public final static int Warning = 2;

		// A critical error has occurred. Generally this will be followed by the
		// network layer
		// shutting down, although the stability of libomv after an error is
		// uncertain
		public final static int Error = 3;

		public final static int Debug = 4;
	}

	private static class Log
	{
		private void output(String level, Object message, Throwable ex)
		{
			System.out.println(level + ": " + message + (ex != null ? " Exception: " + ex.toString() : ""));
		}

		public void info(Object message, Throwable ex)
		{
			output("Info ", message, ex);
		}

		public void warn(Object message, Throwable ex)
		{
			output("Warn ", message, ex);
		}

		public void debug(Object message, Throwable ex)
		{
			output("Debug", message, ex);
		}

		public void error(Object message, Throwable ex)
		{
			output("Error", message, ex);
		}
	}

	public interface LogCallback
	{
		public void callback(Object message, int level);
	}

	/**
	 * Callback used for client apps to receive log messages from the library.
	 * Tyiggered whenever a message is logged. If this is left null, log
	 * messages will go to the console.
	 * 
	 * @param message
	 *            Data being logged
	 * @param level
	 *            The severity of the log entry from {@link Helpers.LogLevel}
	 */
	public static LogCallback OnLogMessage;

	public static Log LogInstance;

	/* Default constructor */
	static
	{
		LogInstance = new Log();
	}

	/**
	 * Send a log message to the logging engine
	 * 
	 * @param message
	 *            The log message
	 * @param level
	 *            The severity of the log entry
	 */
	public static void Log(Object message, int level)
	{
		Log(message, level, null, null);
	}

	/**
	 * Send a log message to the logging engine
	 * 
	 * @param message
	 *            The log message
	 * @param level
	 *            The severity of the log entry
	 * @param client
	 *            Instance of the client
	 */
	public static void Log(Object message, int level, GridClient client)
	{
		Log(message, level, client, null);
	}

	/**
	 * Send a log message to the logging engine
	 * 
	 * @param message
	 *            The log message
	 * @param level
	 *            The severity of the log entry
	 * @param exception
	 *            Exception that was raised
	 */
	public static void Log(Object message, int level, Throwable exception)
	{
		Log(message, level, null, exception);
	}

	/**
	 * Send a log message to the logging engine
	 * 
	 * @param message
	 *            The log message
	 * @param level
	 *            The severity of the log entry
	 * @param client
	 *            Instance of the client
	 * @param exception
	 *            Exception that was raised
	 */
	public static void Log(Object message, int level, GridClient client, Throwable exception)
	{
		if (client != null && client.Settings.LOG_NAMES)
		{
			message = String.format("<%s>: {%s}", client.Self.getName(), message);
		}

		if (OnLogMessage != null)
		{
			OnLogMessage.callback(message, level);
		}

		switch (level)
		{
			case LogLevel.Debug:
				if (Settings.LOG_LEVEL == LogLevel.Debug)
				{
					LogInstance.debug(message, exception);
				}
				break;
			case LogLevel.Info:
				if (Settings.LOG_LEVEL == LogLevel.Debug || Settings.LOG_LEVEL == LogLevel.Info)
				{
					LogInstance.info(message, exception);
				}
				break;
			case LogLevel.Warning:
				if (Settings.LOG_LEVEL == LogLevel.Debug || Settings.LOG_LEVEL == LogLevel.Info
						|| Settings.LOG_LEVEL == LogLevel.Warning)
				{
					LogInstance.warn(message, exception);
				}
				break;
			case LogLevel.Error:
				if (Settings.LOG_LEVEL == LogLevel.Debug || Settings.LOG_LEVEL == LogLevel.Info
						|| Settings.LOG_LEVEL == LogLevel.Warning || Settings.LOG_LEVEL == LogLevel.Error)
				{
					LogInstance.error(message, exception);
				}
				break;
			default:
				break;
		}
	}

	/**
	 * If the library is compiled with DEBUG defined, an event will be fired if
	 * an <code>OnLogMessage</code> handler is registered and the @param message
	 * message will be sent to the logging engine
	 * 
	 * @param message
	 *            The message to log at the DEBUG level to the current logging
	 *            engine
	 */
	public static void DebugLog(Object message)
	{
		DebugLog(message, null);
	}

	/**
	 * If <code>GridClient.Settings.DEBUG</code> is true, an event will be fired
	 * if an <code>OnLogMessage</code> handler is registered and the message
	 * will be sent to the logging engine
	 * 
	 * @param message
	 *            The message to log at the DEBUG level to the current logging
	 *            engine
	 * @param client
	 *            Instance of the client
	 */
	public static void DebugLog(Object message, GridClient client)
	{
		if (Settings.LOG_LEVEL == LogLevel.Debug)
		{
			if (client != null && client.Settings.LOG_NAMES)
			{
				message = String.format("<%s>: {%s}", client.Self.getName(), message);
			}

			if (OnLogMessage != null)
			{
				OnLogMessage.callback(message, LogLevel.Debug);
			}

			LogInstance.debug(message, null);
		}
	}
}
