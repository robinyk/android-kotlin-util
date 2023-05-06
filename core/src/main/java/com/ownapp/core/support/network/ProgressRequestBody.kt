package com.ownapp.core.support.network

import com.ownapp.core.extensions.utility.logException
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.*
import java.io.IOException


class ProgressRequestBody(private val requestBody: RequestBody, private val listener: ProgressListener): RequestBody()
{
	internal class CountingSink(private val requestBody: ProgressRequestBody, request: Sink): ForwardingSink(request)
	{
		private var bytesWritten: Long = 0
		
		@Throws(IOException::class)
		override fun write(source: Buffer, byteCount: Long)
		{
			super.write(source, byteCount)
			bytesWritten += byteCount
			requestBody.listener.onRequestProgress((100f * bytesWritten) / requestBody.contentLength())
		}
	}
	
	interface ProgressListener
	{
		fun onRequestProgress(progress: Float)
	}
	
	override fun contentType(): MediaType? = requestBody.contentType()
	
	override fun contentLength(): Long
	{
		try
		{
			return requestBody.contentLength()
		}
		catch(e: IOException)
		{
			e.logException()
		}
		
		return -1
	}
	
	override fun writeTo(sink: BufferedSink)
	{
		CountingSink(this, sink).buffer().run {
			requestBody.writeTo(this)
			flush()
		}
	}
}