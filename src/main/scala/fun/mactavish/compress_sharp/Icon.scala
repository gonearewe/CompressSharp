package fun.mactavish.compress_sharp

import java.io.File

import scalafx.scene.image.{Image, ImageView}

// add "file:" to indicate that img comes from local file system
class Icon(f: File) extends ImageView(new Image("file:" + f.getAbsolutePath)) {
  def this(f: String) {
    this(new File(f))
  }
}
