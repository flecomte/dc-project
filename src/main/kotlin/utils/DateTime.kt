package fr.dcproject.utils

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

fun DateTime.toIso(): String = ISODateTimeFormat.dateTime().print(this)