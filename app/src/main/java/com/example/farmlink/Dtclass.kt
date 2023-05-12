package com.example.farmlink

import java.text.SimpleDateFormat

// sometimes want added date but removed that app irrelavant
open class Dtclass {
    val dateFragment=SimpleDateFormat("dd-MMM-yyyy")
      fun millisecondsToDate(milliseconds:Long,dateFormat: SimpleDateFormat):String{
          return dateFormat.format(milliseconds)
      }
    fun dateToMilliseconds(date:String,dateFormat: SimpleDateFormat):Long{
        val mDate=dateFormat.parse(date)
        return mDate.time
    }
}