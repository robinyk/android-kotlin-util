package com.ownapp.core.util.helper

import android.os.Handler
import android.os.Looper
import com.ownapp.core.extensions.utility.logError

/**
 * Updated by Robin on 2020/12/4
 */

class LoopHelper(
	private val onSuccess: (LoopHelper) -> Unit = {}
	, private val onFailed: (LoopHelper) -> Unit = {}
	, private val isBreakCondition: () -> Boolean = { false }
	, private val loopInterval: Int = 1000
) {
	//**--------------------------------------------------------------------------------------------------
	//*      Variable
	//---------------------------------------------------------------------------------------------------*/
	// Class
	private var handler = Handler(Looper.getMainLooper())
	private var runnable: Runnable? = null

	// Value
	var maximumTryCount = 99

	private var elapsedCount = 0

	init
	{
		runnable = object: Runnable {
			override fun run()
			{
				if(maximumTryCount != 0 && elapsedCount >= maximumTryCount)
				{
					"Looper failed to complete task within time".logError()
					onFailed(this@LoopHelper)
					stop()
					return
				}
				else if(!isBreakCondition())
				{
					elapsedCount++
					handler.postDelayed(this, loopInterval.toLong())
					return
				}

				onSuccess(this@LoopHelper)
			}
		}
	}

	fun start() = runnable?.let { handler.post(it) }
	fun stop() = runnable?.let { handler.removeCallbacks(it) }
	fun restart()
	{
		stop()
		start()
	}
}