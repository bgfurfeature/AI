// work sheet for scala

val v = 1f

import java.security.MessageDigest

import com.sangupta.murmur.Murmur2
import org.apache.commons.codec.digest.DigestUtils
import org.apache.hadoop.hbase.util.MurmurHash

// MD5 encode
val digest = MessageDigest.getInstance("MD5")
val text = "MD5 this text! asflanfna,fnksdbfskdna,sfnksdhbksdandasfdsaf"
val data = digest.digest(text.getBytes) // 128位定长字符串（MD5 16)
val format = data.map(x => "%02x".format(x))
val md5hash1 = data.map("%02x".format(_)).mkString
// md5 encode
val md5Hex = DigestUtils.md5Hex("i am the content".getBytes)

// murmur hash32
MurmurHash.getInstance().hash("iammurmurhash".getBytes)

// murmur hash64
val bytes = "iammurmurhash".getBytes
val MURMUR_SEED = 0x7f3a21eaL
Murmur2.hash64(bytes, bytes.length, MURMUR_SEED)

// 去除首尾空格
var flag = "".trim
// 去除所有空格
"hell o".replaceAll("\\s*", "")


