package fr.dcproject.utils

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

fun DateTime.toIso() = ISODateTimeFormat.dateTime().print(this)