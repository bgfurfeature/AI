package com.bgfurfeature.hive

import org.apache.hadoop.hive.ql.exec.UDF
import org.apache.hadoop.io.Text

/**
  * Created by C.J.YOU on 2017/2/16.
  *
  */
class UrlFilterUDF  extends  UDF {

  def evaluate(text: Text) : Text = {

    new Text(text.toString.toUpperCase)

  }


}
