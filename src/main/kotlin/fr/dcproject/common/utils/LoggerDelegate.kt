package fr.dcproject.common.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

internal class LoggerDelegate<in R : Any> : ReadOnlyProperty<R, Logger> {
    override fun getValue(thisRef: R, property: KProperty<*>): Logger = LoggerFactory.getLogger(thisRef.javaClass.packageName)
}
