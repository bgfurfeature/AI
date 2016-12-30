package com.bgfurfeature.util

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

/**
  * Created by Administrator on 2016/1/8.
  */
object TimeUtil {

  def getDay: String = {
    val sdf: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd")
    val date: String = sdf.format(new Date)
    date
  }

  def getCurrentHour: Int = {
    val calendar = Calendar.getInstance
    calendar.setTime(new Date)
    calendar.get(Calendar.HOUR_OF_DAY)
  }

  def getTimeStamp:Long = {
    System.currentTimeMillis()
  }

  def getMinute(ts:String): String = {
    val sdf: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH_mm")
    val date: String = sdf.format(new Date(ts.toLong))
    date
  }
}
