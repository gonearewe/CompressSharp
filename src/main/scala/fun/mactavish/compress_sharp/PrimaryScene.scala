package fun.mactavish.compress_sharp

import java.io.File

import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout.{BorderPane, HBox, VBox}
import scalafx.stage.{DirectoryChooser, Stage}

import scala.collection.mutable

class PrimaryScene(stage: Stage) extends Scene {
  private val trie = new TrieTreeViewer
  private val archiveFile = new ObjectProperty[File]() {
    onChange {
      (_, _, f) => {
        val entries = ArchiveController.listEntries(f.toPath)
        trie.reset(entries)
      }
    }
  }

  private def reloadTrie(): Unit = {
    val f = archiveFile.value
    val entries = ArchiveController.listEntries(f.toPath)
    trie.reset(entries)
  }

  root = new BorderPane {
    left = new Menu(stage, archiveFile)

    center = new VBox {
      private val table: TableView[Item] = new TableView[Item](trie) {
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

      table.getSelectionModel.setSelectionMode(SelectionMode.Multiple)
      // enter double-clicked folder
      table.onMouseClicked = e => {
        if (e.getClickCount >= 2) {
          trie.forward(table.getSelectionModel.getSelectedItem)
        }
      }
      table.fixedCellSize = 35
      table.prefHeight <== stage.height // fit in with stage's size
      table.setPlaceholder(new Label("Compress Sharp"))

      private val opMenu = List("/back.png", "/add.png", "/delete.png", "/extract.png")
        .map(i => getClass.getResource(i).getFile)

      children = List(new HBox {
        children = List(
          new Button {
            text = "Back"
            graphic = new Icon(opMenu.head)
            onMouseClicked = _ => trie.back()
          },
          new Button {
            text = "Add"
            graphic = new Icon(opMenu(1))
            onMouseClicked = _ => ???
          },
          new Button {
            text = "Delete"
            graphic = new Icon(opMenu(2))
            onMouseClicked = _ => {
              val items = mutable.ArrayBuffer[Item]()
              table.getSelectionModel.getSelectedItems.forEach(i => items.append(i))
              val paths = items.map(i => trie.pathOf(i)).toSet
              ArchiveController.deleteEntries(archiveFile.value.toPath, paths)
              reloadTrie()
            }
          },
          new Button {
            text = "Extract"
            graphic = new Icon(opMenu(3))
            onMouseClicked = _ => {
              val to = new DirectoryChooser().showDialog(stage)
              if (to != null) {
                val items = mutable.ArrayBuffer[Item]()
                table.getSelectionModel.getSelectedItems.forEach(i => items.append(i))
                val paths = items.map(i => trie.pathOf(i)).toSet
                ArchiveController.extractEntries(archiveFile.value.toPath, paths, to.toPath)
              }
            }
          },
          new Button {
            text = "Extract All"
            graphic = new Icon(opMenu(3))
            onMouseClicked = _ => {
              val to = new DirectoryChooser().showDialog(stage)
              if (to != null)
                ArchiveController.extractAll(archiveFile.value.toPath, to.toPath)
            }
          }
        )
      }, table)
    }
  }
}
