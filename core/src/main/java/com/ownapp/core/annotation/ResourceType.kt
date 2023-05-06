package com.ownapp.core.annotation

/**
 * Updated by Robin on 2020/12/4
 */
@Retention(AnnotationRetention.BINARY)
annotation class ResourceType
{
    companion object
    {
        const val STRING = "string"
        const val DRAWABLE = "drawable"
        const val COLOR = "color"
        const val ATTRIBUTE = "attr"
        const val DIMENSION = "dimen"
        const val LAYOUT = "layout"
        const val RAW = "raw"
    }
}
