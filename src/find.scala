import _root_.java.io._

object RecursiveJavaP {
  
  implicit def fun2filter(filter: File => Boolean) = new FileFilter {
    def accept(f: File): Boolean = filter(f)
  }

  trait PipeMachine {
    def !>(fileName: String): Unit
  }
  implicit def str2Pipe(cmd: String) = new PipeMachine {
    def !>(fileName: String): Unit = {
      val proc = Runtime.getRuntime.exec(cmd)

      val os = new FileOutputStream(fileName)
      val is = proc.getInputStream

      val buffer = new Array[Byte](100)
      var read: Int = 0
      do {
			  read = is.read(buffer)
			  if (read >= 0)
  				os.write(buffer,0,read)
  		} while (read >= 0)

      os.close
    }
  }

  def findAllClasses(base: File)(dir: File): Map[String, File] = {
    def stripOffParent(f: File) = f.getCanonicalPath.substring(base.getCanonicalPath.length + 1)
    def toClassName(path: String) = path.replaceAll("/", ".").substring(0, path.length - 6)

    val classesInThisDir =
      dir.listFiles((f:File) => f.getPath endsWith ".class")
         .map(f => (toClassName(stripOffParent(f)), f))
         .toMap

    dir.listFiles((f: File) => f.isDirectory)
       .map(findAllClasses(base))
       .foldLeft(classesInThisDir)(_ ++ _)
  }

  val perform = classOf[sun.tools.javap.Main].getDeclaredMethod("perform", classOf[Array[String]])
  perform.setAccessible(true)

  def main(args: Array[String]) {
    val classDir = new File("class")
    val cmdBase: List[String] = List("-v", "-classpath", classDir.getCanonicalPath)

    def javap(className: String, outName: String) {
      import _root_.sun.tools.javap.{Main => JavaP}

      val dir = new File(outName).getParentFile
      if (!dir.exists)
        dir.mkdirs

      val writer = new PrintWriter(new FileWriter(outName))

      val jp = new JavaP(writer)
      val args: Array[String] = (cmdBase :+ className).toArray[String]
      perform.invoke(jp, args)

      writer.close
    }

    for ((className, classFile) <- findAllClasses(classDir)(classDir)) {
      val outName = classFile.getCanonicalPath.replaceAll("/class/","/bcode/").replaceAll("\\.class", ".bcode")
      println("Analyzing "+className)
      javap(className, outName)
    }
  }
}
