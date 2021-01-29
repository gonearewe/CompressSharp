package fun.mactavish.compress_sharp

import java.io.File

import fun.mactavish.compress_sharp.Menu.{COMPRESS_FILE, OPEN, Item => MItem}
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.ListView
import scalafx.stage.{FileChooser, Stage}

class Menu(stage: Stage, file: ObjectProperty[File])
  extends ListView[MItem](ObservableBuffer[MItem](OPEN, COMPRESS_FILE)) {

  onMouseClicked = _ => {
    this.getSelectionModel.getSelectedItem match {
      case Menu.OPEN =>
        file.value = new FileChooser() {
          //selectedExtensionFilter =
          //  new ExtensionFilter("archive", Seq("*.7z", "*.zip", "*.rar"))
        }.showOpenDialog(stage)

      case Menu.COMPRESS_FILE =>
    }
  }
}

object Menu {

  sealed abstract class Item(val text: String)

  private case object OPEN extends Item("Open")

  private case object COMPRESS_FILE extends Item("Compress File")

  private case object COMPRESS_DIR extends Item("Compress Directory")

}
