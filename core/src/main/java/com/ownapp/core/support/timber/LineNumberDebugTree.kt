package com.ownapp.core.support.timber

import timber.log.Timber

/**
 * Updated by Robin on 2020/12/4
 */

class LineNumberDebugTree: Timber.DebugTree()
{
    companion object
    {
        private const val CALL_STACK_INDEX = 8
    }

    /**
     * Return the proper StackTraceElement that is responsible for
     * calling AndroidLogger.v, not Timber.v
     */
    private val stackTraces: StackTraceElement?
        get() = Throwable().stackTrace.let { if(CALL_STACK_INDEX < it.size) it[CALL_STACK_INDEX] else null }

    override fun createStackElementTag(element: StackTraceElement): String
    {
        return createTag(stackTraces ?: element)
    }

    private fun createTag(element: StackTraceElement): String
    {
        with(element) { return "Ownapp-($fileName:$lineNumber).$methodName()" }
    }
}