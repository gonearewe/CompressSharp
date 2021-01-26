package fun.mactavish.compress_sharp

import java.io.File
import java.nio.file.Paths

import fun.mactavish.compress_sharp.controller.ArchiveController
import fun.mactavish.sevenz4s.{ExtractionEntry => Entry}
import javafx.event.EventHandler
import javafx.scene.input.MouseEvent
import jfxtras.styles.jmetro.{JMetro, Style}
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.beans.property.StringProperty
import scalafx.collections.ObservableBuffer
import scalafx.scene.Scene
import scalafx.scene.control.{Button, ListView, TableColumn, TableView}
import scalafx.scene.layout.{BorderPane, HBox, VBox}

object Application extends Thread with JFXApp {
  stage = new PrimaryStage {
    scene = new Scene {
      private val currentEntries = ObservableBuffer[Entry]()
      private val menu = ObservableBuffer("Open", "Extract", "Settings")
      private var trie: TrieTreeViewer = _

      private class Item(private val entry: Entry) {
        private var filename: String = _

        def this(name: String) {
          this(null)
          this.filename = name
        }

        def name: String =
          if (entry != null) entry.path.split(File.separator).last
          else this.filename

        def isDir: Boolean =
          if (entry != null) entry.isDir
          else true

        def getOriginalSize: String =
          if (entry != null) entry.originalSize.toString
          else ""

        def getPackedSize: String = {
          if (entry != null) entry.packedSize.toString
          else ""
        }

        def getCRC: String =
          if (entry != null) entry.CRC.toString
          else ""
      }

      private class TrieTreeViewer(entries: Seq[Entry]) {
        private val root = buildTrie(entries)
        private var cur: TrieTree[String, Entry] = _

        def childrenOf(path: List[String]): Option[Set[TrieTree[String, Entry]]] = {
          root.search(path) match {
            case Some(e) => Some(e.children)
            case None => None
          }
        }

        private def buildTrie(entries: Seq[Entry]): TrieTree[String, Item] = {
          val trie = TrieTree.empty[String, Item]()
          entries foreach {
            entry =>
              val path = entry.path.split(File.separator)
              val values = Vector.tabulate(path.size - 1)(i => new Item(path(i))) :+ new Item(entry)
              trie.insert(path.zip(values).toList)
          }
          trie
        }
      }

      private val backwardHandler: EventHandler[_ >: MouseEvent] =
        _ => {

        }


      root = new BorderPane {
        left = new ListView(menu) {
          onMouseClicked = _ => {
            this.getSelectionModel.getSelectedItem match {
              case "Extract" =>
                val filepath = Paths.get("E:\\MyProjects\\CompressSharp\\compressed_generic.zip")
                val entries = ArchiveController.listEntries(filepath)
                trie = new TrieTreeViewer(entries)
                currentEntries.clear()
                currentEntries.addAll(trie.childrenOf(Nil).get.map(_.key))
            }
          }
        }
        center = new VBox {
          children = new HBox {
            children = new Button {
              text = "<"
              onMouseClicked = backwardHandler
            }
          } :: new TableView[Entry](currentEntries) {


            columns ++= List(
              new TableColumn[Entry, String] {
                text = "name"
                cellValueFactory = { c => StringProperty(c.value.path) }
              },
              new TableColumn[Entry, String] {
                text = "original size"
                cellValueFactory = { c => StringProperty(c.value.originalSize.toString) }
              },
              new TableColumn[Entry, String] {
                text = "packed size"
                cellValueFactory = { c => StringProperty(c.value.packedSize.toString) }
              },
              new TableColumn[Entry, String] {
                text = "CRC"
                cellValueFactory = {
                  c => StringProperty(c.value.CRC.toString)
                }
              }
            )
          } :: Nil
        }
      }

      new JMetro(Style.LIGHT).setScene(scene.value)
    }
  }

  override def start(): Unit = {
    main(Array())
  }
}
