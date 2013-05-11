package org.eknet.publet.gitr

import org.eknet.publet.content.{Content, ModifyInfo, Path, FsPartition}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 09.05.13 02:47
 */
class GitPartition(val tandem: Tandem) extends FsPartition(tandem.workTree.getWorkTree.toPath) {
  override def createFolder(path: Path, info: ModifyInfo) = {
    val f = super.createFolder(path, info)
    f map { x =>
      tandem.workTree.git.add()
        .addFilepattern(path.toString)
        .setUpdate(false)
        .call()
      commit(info, path, "create")
      tandem.pushToBare()
    }
    f
  }

  override def createContent(path: Path, content: Content, info: ModifyInfo) = {
    val r = super.createContent(path, content, info)
    r map { x =>
      tandem.workTree.git.add()
        .addFilepattern(path.toString)
        .setUpdate(false)
        .call()
      commit(info, path, "create")
      tandem.pushToBare()
    }
    r
  }

  override def delete(path: Path, info: ModifyInfo) = {
    val result = super.delete(path, info)
    result filter(_ == true) map { x =>
      commit(info, path, "delete")
      tandem.pushToBare()
    }
    result
  }

  private def commit(info: ModifyInfo, path: Path, action: String) = {
    val msg = action +" on "+ path.absoluteString + (if (info.message.isEmpty) "" else ": "+ info.message)
    tandem.workTree.git.commit()
      .setMessage(msg)
      .setAuthor(info.user, "")
      .setAll(true)
      .call()
  }
}
