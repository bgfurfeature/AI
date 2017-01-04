package com.bgfurfeature.elasticsearch

/**
  * Created by C.J.YOU on 2017/1/4.
  * id:  类似行号
  * content: JSON 格式（_source下的字段格式）
  * {
        *"_index": "bank",
        *"_type": "account",
        *"_id": "0",
        *"_score": null,
        *"_source": {
          *"account_number": 0,
          *"balance": 16623,
          *"firstname": "Bradshaw",
          *"lastname": "Mckenzie",
          *"age": 29,
          *"gender": "F",
          *"address": "244 Columbus Place",
          *"employer": "Euron",
          *"email": "bradshawmckenzie@euron.com",
          *"city": "Hobucken",
          *"state": "CO"
        *}
  */

class Document(id: String, content: Array[String]) {

  def formatContent: String = {

    "\"AD\":\"%s\",\"stock\":\"%s\",\"ts\":\"%s\"，\"stype\":\"%s\"，\"level\":\"%s\"，\"count\":\"%s\"，\"ua\":\"%s\"，\"host\":\"%s\"，\"index\":\"%s\""
      .format(content(0),content(1),content(2),content(3),content(4),content(5),content(6),content(7),content(8))

  }


}
