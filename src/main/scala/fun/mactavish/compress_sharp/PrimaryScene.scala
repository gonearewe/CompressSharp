package fun.mactavish.compress_sharp

import fun.mactavish.sevenz4s.{ExtractionEntry => Entry}
import javafx.event.EventHandler
import javafx.scene.input.MouseEvent
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.collections.ObservableBuffer
import scalafx.scene.Scene
import scalafx.scene.control.{Button, TableColumn, TableView}
import scalafx.scene.layout.{BorderPane, HBox, VBox}
import scalafx.stage.Stage

class PrimaryScene(stage: Stage) extends Scene {
  private val trie = new TrieTreeViewer
  private val menu = ObservableBuffer("Open", "Extract", "Settings")
  private val newEntries = new ObjectProperty[Seq[Entry]]() {
    onChange {
      (_, _, entries) => trie.reset(entries)
    }
  }

  private val backwardHandler: EventHandler[_ >: MouseEvent] =
    _ => trie.back()

  root = new BorderPane {
    left = new Menu(stage, newEntries)

    center = new VBox {
      children = new HBox {
        children = new Button {
          text = "<"
          onMouseClicked = backwardHandler
        }
      } :: new TableView[Item](trie) {
        onMouseClicked = e => {
          if (e.getClickCount >= 2) {
            trie.forward(this.getSelectionModel.getSelectedItem)
          }
        }

        columns ++= List(
          new TableColumn[Item, String] {
            text = "name"
            cellValueFactory = { c => StringProperty(c.value.getName) }
          },
          new TableColumn[Item, String] {
            text = "original size"
            cellValueFactory = { c => StringProperty(c.value.getOriginalSize) }
          },
          new TableColumn[Item, String] {
            text = "packed size"
            cellValueFactory = { c => StringProperty(c.value.getPackedSize) }
          },
          new TableColumn[Item, String] {
            text = "CRC"
            cellValueFactory = {
              c => StringProperty(c.value.getCRC)
            }
          }
        )
      } :: Nil
    }
  }
}
