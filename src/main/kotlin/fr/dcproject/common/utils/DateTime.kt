package fr.dcproject.common.utils

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

fun DateTime.toIso(): String = ISODateTimeFormat.dateTime().print(this)
