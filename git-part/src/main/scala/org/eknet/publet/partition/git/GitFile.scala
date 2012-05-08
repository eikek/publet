package org.eknet.publet.partition.git

import org.eknet.publet.vfs.fs.FileResource
import java.io.{OutputStream, InputStream, File}
import scala.Option
import org.eknet.publet.vfs.{Content, Path}

class GitFile(f: File,
              root: Path,
              gp: GitPartition) extends FileResource(f, root) {
  override def delete() {
    super.delete()
    gp.commitDelete(this)
  }

  override def writeFrom(in: InputStream, message: Option[String] = None) {
    Content.copy(in, new OutStream(super.outputStream, message), closeIn = false)
  }

  override def outputStream: OutputStream = {
    new OutStream(super.outputStream)
  }

  override protected def newDirectory(f: File, root: Path) = GitPartition.newDirectory(f, root, gp)

  override protected def newFile(f: File, root: Path) = GitPartition.newFile(f, root, gp)

  def lastAuthor = {
    val commit = gp.lastCommit(this)
    commit map (_.getAuthorIdent)
  }

  private class OutStream(out:OutputStream, message:Option[String] = None) extends OutputStream {

    def write(b: Int) {
      out.write(b)
    }

    override def write(b: Array[Byte]) {
      out.write(b)
    }

    override def write(b: Array[Byte], off: Int, len: Int) {
      out.write(b, off, len)
    }

    override def close() {
      out.close()
      gp.commitWrite(GitFile.this, message)
    }

    override def flush() {
      out.flush()
    }
  }
}
