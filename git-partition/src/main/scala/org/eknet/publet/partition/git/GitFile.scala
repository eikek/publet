package org.eknet.publet.partition.git

import org.eknet.publet.vfs.fs.FileResource
import java.io.{OutputStream, InputStream, File}
import org.eknet.publet.vfs.Path

class GitFile(f: File,
              root: Path,
              gp: GitPartition) extends FileResource(f, root) {
  override def delete() {
    super.delete()
    gp.commitDelete(this)
  }

  override def writeFrom(in: InputStream) {
    super.writeFrom(in)
  }

  override def outputStream: Option[OutputStream] = {
    val out = super.outputStream.get
    Some(new OutputStream {

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
        gp.commitWrite(GitFile.this)
      }

      override def flush() {
        out.flush()
      }
    })
  }

  override protected def newDirectory(f: File, root: Path) = GitPartition.newDirectory(f, root, gp)

  override protected def newFile(f: File, root: Path) = GitPartition.newFile(f, root, gp)

  def lastAuthor = {
    val commit = gp.lastCommit(this)
    commit map (_.getAuthorIdent)
  }
}
