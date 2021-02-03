package fun.mactavish.compress_sharp

import java.io.File

import fun.mactavish.compress_sharp.CtrlMenu.{COMPRESS_FILE, OPEN, Item => MItem}
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.{ListCell, ListView}
import scalafx.stage.{FileChooser, Stage}

class CtrlMenu(stage: Stage, file: ObjectProperty[File])
  extends ListView[MItem](ObservableBuffer[MItem](OPEN, COMPRESS_FILE)) {

  cellFactory = _ => new ListCell[MItem] {
    item onChange {
      text = item.value.text
      graphic = new Icon(item.value.icon)
    }
  }

  onMouseClicked = _ => {
    this.getSelectionModel.getSelectedItem match {
      case CtrlMenu.OPEN =>
        file.value = new FileChooser() {
          //selectedExtensionFilter =
          //  new ExtensionFilter("archive", Seq("*.7z", "*.zip", "*.rar"))
        }.showOpenDialog(stage)

      case CtrlMenu.COMPRESS_FILE =>
        CompressionStage.show(file)

    }
  }
}

object CtrlMenu {

  sealed abstract class Item(val text: String, val icon: File)

  private final case object OPEN extends
    Item("Open", Resource.get("open"))

  private final case object COMPRESS_FILE extends
    Item("Compress File", Resource.get("compress"))

  private final case object COMPRESS_DIR extends
    Item("Compress Directory", Resource.get("compress"))

}
