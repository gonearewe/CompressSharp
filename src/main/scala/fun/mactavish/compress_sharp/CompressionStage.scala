package fun.mactavish.compress_sharp

import java.io.File
import java.nio.file.Path

import fun.mactavish.sevenz4s.{ArchiveFormat, CreatableArchiveFormat, SevenZ4S}
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer
import scalafx.scene.Scene
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, Button, ComboBox, TextField}
import scalafx.scene.layout.{HBox, VBox}
import scalafx.stage.{DirectoryChooser, Stage}

object CompressionStage {

  def show(file: ObjectProperty[File]): Unit = {
    val stage = new CompressionStage(file)
    stage.scene = new Scene {
      new HBox {
        private var from: Path = _

        private val fileChooser = new VBox {
          private val text = new TextField()
          children = List(
            text,
            new Button {
              graphic = new Icon(Resource.get("open"))
              onMouseClicked = _ => {
                val f = new DirectoryChooser().showDialog(stage)
                if (f != null) {
                  text.value = f.getAbsolutePath
                  from = f.toPath
                }
              }
            }
          )
        }

        private val formatChooser = new ComboBox[CreatableArchiveFormat] {
          items = ObservableBuffer[CreatableArchiveFormat](
            ArchiveFormat.SEVEN_Z,
            ArchiveFormat.TAR,
            ArchiveFormat.ZIP
          )
        }

        children = List(
          fileChooser,
          formatChooser,
          new Button {
            text = "Start"
            onMouseClicked = _ => {
              if (from == null || formatChooser.getValue == null) {
                new Alert(AlertType.Error) {
                  text = "Choose a directory and a format !"
                }
              } else {
                val outputPath = from.getParent
                SevenZ4S.compress(formatChooser.getValue, from, outputPath)
                file.value = outputPath.toFile
              }
            }
          }

        )
      }
    }
  }

  private class CompressionStage(file: ObjectProperty[File]) extends Stage

}
