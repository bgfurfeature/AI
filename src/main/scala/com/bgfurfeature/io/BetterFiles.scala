package com.bgfurfeature.io

/**
  * Created by C.J.YOU on 2017/2/16.
  */
import java.io.{File => JFile}

import better.files._

/**
  * 对Java NIO库进行了包装，不依赖其它的第三方库
  */
class BetterFiles {

  // 实例化
  def newInstances() = {

    val file = File("/User/johndoe/Documents") // using constructor
    val f1: File = file"/User/johndoe/Documents" // using string interpolator
    val f2: File = "/User/johndoe/Documents".toFile // convert a string path to a file
    val f3: File = new JFile("/User/johndoe/Documents").toScala // convert a Java file to Scala
    /* val f4: File = root / "User"/"johndoe"/"Documents" // using root helper to start from root
    val f5: File = `~`/ "Documents" // also equivalent to `home / "Documents"`
    val f7: File = home/"Documents"/"presentations"/`..` // Use `..` to navigate up to parent */
    val f6: File = "/User"/"johndoe"/"Documents" // using file separator DSL


    // 读写等操作
    file.overwrite("heoool")
    file.appendLine().append("world")

    file < "hello" // same as file.overwrite("hello")
    file << "world" // same as file.appendLines("world")
    assert(file == "hello\nworld")

    // "hello" `>:` file
    "world" >>: file
    val bytes: Array[Byte] = file.loadBytes

    // 流式风格
    ("root" / "tmp" / "diary.txt")
      .createIfNotExists()
      .appendLine()
      .appendLines("My name is", "Inigo Montoya")
      .moveTo("home"/"Documents")
      .renameTo("princess_diary.txt")
      .changeExtensionTo(".md")
      .lines

    // 编码
    val content: String = file.contentAsString // default codec
    // custom codec:
    import scala.io.Codec
    file.contentAsString(Codec.ISO8859)
    //or
    import scala.io.Codec.string2codec
    file.write("hello world")(codec = "US-ASCII")


  }

  def inteActive() = {
    // 与java 交互
    val file: File = "tmp" / "hello.txt"
    val javaFile : java.io.File = file.toJava
    val uri : java.net.URI = file.uri
    val reader : java.io.BufferedReader = file.newBufferedReader
    val outputstream : java.io.OutputStream = file.newOutputStream
    val writer : java.io.BufferedWriter = file.newBufferedWriter
    val inputstream : java.io.InputStream = file.newInputStream
    val path : java.nio.file.Path = file.path
    val fs : java.nio.file.FileSystem = file.fileSystem
    val channel : java.nio.channels.FileChannel = file.newFileChannel
    //  val ram : java.io.RandomAccessFile = file.newRandomAccess(File.RandomAccessMode)
    val fr : java.io.FileReader = file.newFileReader
    val fw : java.io.FileWriter = file.newFileWriter(append = true)
    val printer : java.io.PrintWriter = file.newPrintWriter(true)

    // pattern matcher（摸式匹配）
    /**
      * @return true if file is a directory with no children or a file with no contents
      */
    def isEmpty(file: File): Boolean = file match {
      // this must be first case statement if you want to handle symlinks specially; else will follow link
      case File.Type.SymbolicLink(to) => isEmpty(to)
      case File.Type.Directory(files) => files.isEmpty
      case File.Type.RegularFile(content) => content.isEmpty
      case _ => file.notExists // a file may not be one of the above e.g. UNIX pipes, sockets, devices etc
    }
    // or as extractors on LHS:
    val File.Type.Directory(researchDocs) = "home" / "Downloads" / "research"

    // 通配符
    val dir = "src" / "test"
    val matches: Iterator[File] = dir.glob("**/*.{java,scala}")
    // above code is equivalent to:
    dir.listRecursively.filter(f => f.extension.contains(".java") || f.extension.contains(".scala"))

    val matcherRegex = dir.glob("^\\w*$")(syntax = File.PathMatcherSyntax.regex)


  }

  // 文件系统操作
  def fileSystemOperation() = {

    val file: File = "tmp" / "hello.txt"
    file.touch()
    file.delete() // unlike the Java API, also works on directories as expected (deletes children recursively)
    file.clear() // If directory, deletes all children; if file clears contents
    /*
    file.renameTo(newName: String)
    file.moveTo(destination)
    file.copyTo(destination) // unlike the default API, also works on directories (copies recursively)
    file.linkTo(destination) // ln file destination
    file.symbolicLinkTo(destination) // ln -s file destination
    file.{checksum, md5, sha1, sha256, sha512, digest} // also works for directories
    file.setOwner(user: String) // chown user file
    file.setGroup(group: String) // chgrp group file
    Seq(file1, file2) >: file3 // same as cat file1 file2 > file3
    Seq(file1, file2) >>: file3 // same as cat file1 file2 >> file3
    file.isReadLocked / file.isWriteLocked / file.isLocked
    File.newTemporaryDirectory() / File.newTemporaryFile() // create temp dir/file  */


    // UNIX DSL // must import Cmds._ to bring in these utils
    /*pwd / cwd // current dir
    cp(file1, file2)
    mv(file1, file2)
    rm(file) /*or*/ del(file)
    ls(file) /*or*/ dir(file)
    ln(file1, file2) // hard link
    ln_s(file1, file2) // soft link
    cat(file1)
    cat(file1) >>: file
    touch(file)
    mkdir(file)
    mkdirs(file) // mkdir -p
    chown(owner, file)
    chgrp(owner, file)
    chmod_+(permission, files) // add permission
    chmod_-(permission, files) // remove permission
    md5(file) / sha1(file) / sha256(file) / sha512(file)
    unzip(zipFile)(targetDir)
    zip(file*)(zipFile) */


    // 排序
    val files = File("tmp/MyDir").list.toSeq
    files.sorted(File.Order.byName)
    files.max(File.Order.bySize)
    files.min(File.Order.byDepth)
    files.max(File.Order.byModificationTime)
    files.sorted(File.Order.byDirectoriesFirst)
  }

  def fileMonitor = {

    val watcher = new ThreadBackedFileMonitor("root" / "myDir", recursive = true) {
      override def onCreate(file: File) = println(s"$file got created")
      override def onModify(file: File) = println(s"$file got modified")
      override def onDelete(file: File) = println(s"$file got deleted")
    }
    watcher.start()

  }

}
