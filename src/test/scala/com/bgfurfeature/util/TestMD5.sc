// work sheet for scala

val v = 1f

import java.security.MessageDigest

val digest = MessageDigest.getInstance("MD5")

val text = "MD5 this text! asflanfna,fnksdbfskdna,sfnksdhbksdandasfdsaf"

val data = digest.digest(text.getBytes) // 128位定长字符串（MD5 16)

val format = data.map(x => "%02x".format(x))

val md5hash1 = data.map("%02x".format(_)).mkString
