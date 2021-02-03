package fun.mactavish.compress_sharp

import java.io.File

object Resource {
  def get(name: String) =
    new File(getClass.getResource(s"/$name.png").getFile)
}
