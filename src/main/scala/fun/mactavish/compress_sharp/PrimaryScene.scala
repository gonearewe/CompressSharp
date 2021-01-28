package fun.mactavish.compress_sharp

import fun.mactavish.sevenz4s.{ExtractionEntry => Entry}
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.collections.ObservableBuffer
import scalafx.scene.Scene
import scalafx.scene.control.{Button, TableCell, TableColumn, TableView}
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

  root = new BorderPane {
    left = new Menu(stage, newEntries)

    center = new VBox {
      private val table: TableView[Item] = new TableView[Item](trie) {
        // enter double-clicked folder
        onMouseClicked = e => {
          if (e.getClickCount >= 2) {
            trie.forward(this.getSelectionModel.getSelectedItem)
          }
        }

        // first column of the table displays file icon
        private val iconColumn = new TableColumn[Item, FileType] {
          prefWidth = 50
          cellValueFactory =
            c => {
              if (c.value.isDir) ObjectProperty(DIRECTORY)
              else {
                val seq = c.value.getName.split("\\.")
                if (seq.isEmpty) ObjectProperty(UNKNOWN_FILE)
                else {
                  val extension = seq.last
                  ObjectProperty(FileType.of(extension))
                }
              }
            }
          cellFactory = (e: TableColumn[Item, FileType]) => {
            new TableCell[Item, FileType]() {
              item onChange {
                graphic = if (item.value != null) {
                  new Icon(item.value.icon)
                } else null
              }
            }
          }
        }

        columns ++= List(iconColumn,
          new TableColumn[Item, String] {
            text = "name"
            prefWidth = 250
            cellValueFactory = { c => StringProperty(c.value.getName) }
          },
          new TableColumn[Item, String] {
            text = "original size"
            prefWidth = 150
            cellValueFactory = { c => StringProperty(c.value.getOriginalSize) }
          },
          new TableColumn[Item, String] {
            text = "packed size"
            prefWidth = 150
            cellValueFactory = { c => StringProperty(c.value.getPackedSize) }
          },
          new TableColumn[Item, String] {
            text = "CRC"
            prefWidth = 150
            cellValueFactory = {
              c => StringProperty(c.value.getCRC)
            }
          }
        )
      }

      table.fixedCellSize = 35
      table.prefHeight <== stage.height // fit in with stage's size

      children = List(new HBox {
        children = new Button {
          graphic = new Icon(getClass.getResource("/back.png").getFile)
          onMouseClicked = _ => trie.back()
        }
      }, table)
    }
  }
}
