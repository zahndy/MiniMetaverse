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
package libomv.capabilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;

import libomv.utils.Helpers;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;

import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.DefaultHttpAsyncClient;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.client.HttpAsyncRequestProducer;
import org.apache.http.nio.client.HttpAsyncResponseConsumer;
import org.apache.http.nio.concurrent.BasicFuture;
import org.apache.http.nio.concurrent.FutureCallback;
import org.apache.http.nio.conn.scheme.Scheme;
import org.apache.http.nio.conn.ssl.SSLLayeringStrategy;
import org.apache.http.nio.entity.NHttpEntityWrapper;
import org.apache.http.nio.entity.ProducingNHttpEntity;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.protocol.HTTP;

public abstract class AsyncHTTPClient<T>
{
	public interface ProgressCallback
	{
		public void progress(long bytesReceived, long totalBytes);
	}
	
	private final HttpAsyncClient client;
	private X509Certificate certificate;
	private Timer timeout;
	private FutureCallback<T> callback;
	private ProgressCallback progress;

	public void setResultCallback(FutureCallback<T> callback)
	{
		this.callback = callback;
	}
	
	public void setProgressCallback(ProgressCallback callback)
	{
		this.progress = callback;
	}
	
	public void setCertificate(X509Certificate cert)
	{
		this.certificate = cert;
	}
	
	private void cancel()
	{
		if (timeout != null)
		{
			synchronized (timeout)
			{
				timeout.cancel();
				timeout = null;
			}
		}
	}
	
	public void shutdown() throws InterruptedException
	{
		cancel();
		client.shutdown();
	}

	public AsyncHTTPClient() throws IOReactorException
	{
		client = new DefaultHttpAsyncClient();
		client.start();
	}

	/**
	 * Do a HTTP Get Request from the server
	 * 
	 * @param acceptHeader The content type to add as Accept: header or null
	 * @param millisecondTimeout The timeout to wait for a response or -1 if no timeout should be used
	 *                The request can still be aborted through the returned future.
	 * @return A Future that can be used to retrieve the data
	 */
	public Future<T> executeHttpGet(URI address, String acceptHeader, long millisecondTimeout)
	{
		HttpRequest request = new HttpGet();
		if (acceptHeader != null && !acceptHeader.isEmpty())
		{
			request.addHeader("Accept", acceptHeader);
		}
		return executeHttp(address, request, millisecondTimeout);
	}

	/**
	 * Do a HTTP Post Request from the server for string data
	 * 
	 * @param data The string data to add as entity content
	 * @param contentType The content type to add as ContentType: header or null
	 * @param millisecondTimeout The timeout to wait for a response or -1 if no timeout should be used
	 *                The request can still be aborted through the returned future.
	 * @return A Future that can be used to retrieve the data
	 */
	public Future<T> executeHttpPost(URI address, String data, String contentType, long millisecondTimeout)
			throws UnsupportedEncodingException
	{
		// Create the request
		HttpPost request = new HttpPost();
		if (contentType != null && !contentType.isEmpty())
		{
			request.addHeader("Content-Type", contentType);
		}

		// set POST body
		request.setEntity(new StringEntity(data));
		return executeHttp(address, request, millisecondTimeout);
	}

	/**
	 * Do a HTTP Post Request from the server for binary data
	 * 
	 * @param data The binary data to add as entity content
	 * @param contentType The content type to add as ContentType: header or null
	 * @param millisecondTimeout The timeout to wait for a response or -1 if no timeout should be used
	 *                The request can still be aborted through the returned future.
	 * @return A Future that can be used to retrieve the data
	 */
	public Future<T> executeHttpPost(URI address, byte[] data, String contentType, long millisecondTimeout)
	{
		// Create the request
		HttpPost request = new HttpPost();
		if (contentType != null && !contentType.isEmpty())
		{
			request.addHeader("Content-Type", contentType);
		}

		// set POST body
		request.setEntity(new ByteArrayEntity(data));
		return executeHttp(address, request, millisecondTimeout);
	}

	private HttpHost determineTarget(URI address) throws ClientProtocolException
	{
		// A null target may be acceptable if there is a default target.
		// Otherwise, the null target is detected in the director.
		HttpHost target = null;

		if (address.getScheme().equals("https"))
		{
			try
			{
				String name = address.getHost();
				if (certificate == null)
				{
					certificate = Helpers.GetCertificate(address.getHost());
				}

				if (certificate != null)
				{
					KeyStore store = Helpers.GetExtendedKeyStore();
					store.setCertificateEntry(name, certificate);
					client.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, new SSLLayeringStrategy(store)));
				}
			}
			catch (Exception ex)
			{
			}
		}

		if (address.isAbsolute())
		{
			if (address.getHost() == null)
			{
				throw new ClientProtocolException("URI does not specify a valid host name: " + address);
			}
			target = new HttpHost(address.getHost(), address.getPort(), address.getScheme());
			// TODO use URIUtils#extractTarget once it becomes available
		}
		return target;
	}

	private Future<T> executeHttp(URI address, HttpRequest request, long millisecondTimeout)
	{
		synchronized (timeout)
		{
			if (timeout != null)
			{
				Logger.Log("This Capability Client is already waiting for a response", Logger.LogLevel.Error);
				return null;
			}
		}

		try
		{
			final Future<T> result = client.execute(new CapsHttpAsyncRequestProducer(determineTarget(address), request),
					                                new CapsHttpAsyncResponseConsumer(), new AsyncHttpResultCallback());

			if (millisecondTimeout >= 0)
			{
				synchronized (timeout)
				{
					timeout = new Timer();
					timeout.schedule(new TimerTask()
					{
						@Override
						public void run()
						{
							result.cancel(false);
						}
					}, millisecondTimeout);
				}
			}
			return result;
		}
		catch (Exception ex)
		{
			BasicFuture<T> failed = new BasicFuture<T>(callback);
			failed.failed(ex);
			return failed;
		}
	}

	private class AsyncHttpResultCallback implements FutureCallback<T>
	{
		@Override
		public void completed(T result)
		{
			if (callback != null)
				callback.completed(result);
			cancel();
		}

		@Override
		public void failed(Exception ex)
		{
			if (callback != null)
				callback.failed(ex);
			cancel();
		}

		@Override
		public void cancelled()
		{
			if (callback != null)
				callback.cancelled();
			cancel();
		}
	}
	private class CapsHttpAsyncRequestProducer implements HttpAsyncRequestProducer
	{
		private final HttpHost target;
		private final HttpRequest request;
		private final ProducingNHttpEntity producer;

		public CapsHttpAsyncRequestProducer(final HttpHost target, final HttpRequest request) throws IOException
		{
			super();
			if (target == null)
			{
				throw new IllegalArgumentException("Target host may not be null");
			}
			if (request == null)
			{
				throw new IllegalArgumentException("HTTP request may not be null");
			}
			this.target = target;
			this.request = request;
			HttpEntity entity = null;
			if (request instanceof HttpEntityEnclosingRequest)
			{
				entity = ((HttpEntityEnclosingRequest) request).getEntity();
			}
			if (entity != null)
			{
				if (entity instanceof ProducingNHttpEntity)
				{
					this.producer = (ProducingNHttpEntity) entity;
				}
				else
				{
					this.producer = new NHttpEntityWrapper(entity);
				}
			}
			else
			{
				this.producer = null;
			}
		}

		@Override
		public HttpRequest generateRequest()
		{
			return this.request;
		}

		@Override
		public HttpHost getTarget()
		{
			return this.target;
		}

		@Override
		public synchronized void produceContent(final ContentEncoder encoder, final IOControl ioctrl)
				throws IOException
		{
			if (this.producer == null)
			{
				throw new IllegalStateException("Content producer is null");
			}
			this.producer.produceContent(encoder, ioctrl);
			if (encoder.isCompleted())
			{
				this.producer.finish();
			}
		}

		@Override
		public synchronized boolean isRepeatable()
		{
			if (this.producer != null)
			{
				return this.producer.isRepeatable();
			}
			return true;
		}

		@Override
		public synchronized void resetRequest()
		{
			try
			{
				if (this.producer != null)
				{
					this.producer.finish();
				}
			}
			catch (IOException ignore)
			{
			}
		}
	}

	protected abstract T convertContent(InputStreamReader in) throws IOException;
	
	private class CapsHttpAsyncResponseConsumer implements HttpAsyncResponseConsumer<T>
	{
		private volatile Charset charset;
		private volatile long length;
		private volatile T result;
		private volatile Exception ex;
		private volatile boolean completed;

		@Override
		public synchronized void responseReceived(final HttpResponse response) throws IOException
		{
			Logger.Log("HTTP response: " + response.getStatusLine(), LogLevel.Info);

			StatusLine status = response.getStatusLine();
			if (status.getStatusCode() != HttpStatus.SC_OK)
			{
				throw new HttpResponseException(status.getStatusCode(), status.getReasonPhrase());
			}

			HttpEntity entity = response.getEntity();
			if (entity != null)
			{
				String charset = null;
				if (entity.getContentType() != null)
				{
					HeaderElement values[] = entity.getContentType().getElements();
					if (values.length > 0)
					{
						NameValuePair param = values[0].getParameterByName("charset");
						if (param != null)
						{
							charset = param.getValue();
						}
					}
				}
				if (charset == null)
				{
					charset = HTTP.DEFAULT_CONTENT_CHARSET;
				}
				this.length = entity.getContentLength();
				this.charset = Charset.forName(charset);
			}
		}

		/**
		 * Notification that content is available to be read from the decoder.
		 * {@link IOControl} instance passed as a parameter to the method can be
		 * used to suspend input events if the entity is temporarily unable to
		 * allocate more storage to accommodate all incoming content.
		 * 
		 * @param decoder
		 *            content decoder.
		 * @param ioctrl
		 *            I/O control of the underlying connection.
		 */
		@Override
		public synchronized void consumeContent(final ContentDecoder decoder, final IOControl ioctrl)
				throws IOException
		{
			InputStreamReader in = new InputStreamReader(new ContentDecoderStream(decoder), this.charset);
			try
			{
				this.result = convertContent(in);
			}
			finally
			{
				in.close();
			}
		}

		/**
		 * Notification that any resources allocated for reading can be released.
		 */
		@Override
		public synchronized void responseCompleted()
		{
			if (this.completed)
			{
				return;
			}
			this.completed = true;
		}

		@Override
		public synchronized void cancel()
		{
			if (this.completed)
			{
				return;
			}
			this.completed = true;
			this.result = null;
		}

		@Override
		public synchronized void failed(final Exception ex)
		{
			if (this.completed)
			{
				return;
			}
			this.ex = ex;
			this.completed = true;
		}

		@Override
		public T getResult()
		{
			return this.result;
		}

		@Override
		public Exception getException()
		{
			return ex;
		}

		private class ContentDecoderStream extends InputStream
		{
			private final ContentDecoder decoder;
			private final ByteBuffer buffer;
			private volatile long pos;

			public ContentDecoderStream(ContentDecoder decoder)
			{
				this.decoder = decoder;
				this.buffer = ByteBuffer.allocate(1024);
				this.pos = 0;
			}

			@Override
			public int read() throws IOException
			{
				// Find out how many bytes we can read from the intermediate
				// buffer
				int toread = this.buffer.remaining();
				if (toread == 0)
				{
					// is decoder stream complete?
					if (this.decoder.isCompleted())
						return -1;

					// Fill in the intermediate buffer again from the stream
					this.buffer.clear();
					this.decoder.read(this.buffer);
					this.buffer.flip();
				}
				progress(1);
				return this.buffer.get();
			}

			@Override
			public int read(byte[] b) throws IOException
			{
				int bytes = read(b, 0, b.length);
				if (bytes > 0)
					progress(bytes);
				return bytes;
			}

			/**
			 * Reads up to len bytes of data from the input stream into an array
			 * of bytes. An attempt is made to read as many as len bytes, but a
			 * smaller number may be read, possibly zero. The number of bytes
			 * actually read is returned as an integer. This method blocks until
			 * input data is available, end of file is detected, or an exception
			 * is thrown.
			 * 
			 * If b is null, a NullPointerException is thrown.
			 * 
			 * If off is negative, or len is negative, or off+len is greater
			 * than the length of the array b, then an IndexOutOfBoundsException
			 * is thrown.
			 * 
			 * If len is zero, then no bytes are read and 0 is returned;
			 * otherwise, there is an attempt to read at least one byte. If no
			 * byte is available because the stream is at end of file, the value
			 * -1 is returned; otherwise, at least one byte is read and stored
			 * into b.
			 * 
			 * The first byte read is stored into element b[off], the next one
			 * into b[off+1], and so on. The number of bytes read is, at most,
			 * equal to len. Let k be the number of bytes actually read; these
			 * bytes will be stored in elements b[off] through b[off+k-1],
			 * leaving elements b[off+k] through b[off+len-1] unaffected.
			 * 
			 * In every case, elements b[0] through b[off] and elements
			 * b[off+len] through b[b.length-1] are unaffected.
			 * 
			 * If the first byte cannot be read for any reason other than end of
			 * file, then an IOException is thrown. In particular, an
			 * IOException is thrown if the input stream has been closed.
			 * 
			 * @param b
			 *            - the buffer into which the data is read.
			 * @param off
			 *            - the start offset in array b at which the data is
			 *            written.
			 * @param len
			 *            - the maximum number of bytes to read.
			 * @return the total number of bytes read into the buffer, or -1 if
			 *         there is no more data because the end of the stream has
			 *         been reached.
			 * @throws IOException
			 */
			@Override
			public int read(byte[] b, int offset, int length) throws IOException
			{
				int toread, bytes = 0;

				if (b == null)
					throw new NullPointerException();

				if (offset < 0 || length < 0 || offset + length > b.length)
					throw new IndexOutOfBoundsException();

				while (length > 0)
				{
					// Find out how many bytes we can read from the intermediate buffer
					toread = this.buffer.remaining();
					if (length < toread)
						toread = length;

					if (toread > 0)
					{
						// get first bytes from the buffer if any
						this.buffer.get(b, offset, toread);
						length -= toread;
						offset += toread;
						bytes += toread;
					}

					// is decoder stream complete or have we copied all
					// requested bytes
					if (this.decoder.isCompleted() || length == 0)
					{
						progress(bytes);
						return bytes == 0 ? -1 : bytes;
					}

					// Fill in the intermediate buffer again from the stream
					this.buffer.clear();
					this.decoder.read(this.buffer);
					this.buffer.flip();
				}
				progress(bytes);
				return bytes;
			}
			
			private void progress(int bytes)
			{
				pos += bytes;
				if (progress != null)
					progress.progress(pos, length);
			}
		}
	}
}
