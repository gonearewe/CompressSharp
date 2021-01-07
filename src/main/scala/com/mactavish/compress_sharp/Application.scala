package com.mactavish.compress_sharp

import jfxtras.styles.jmetro.{JMetro, Style}
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.beans.property.{BooleanProperty, StringProperty}
import scalafx.beans.value.ObservableValue
import scalafx.collections.ObservableBuffer
import scalafx.scene.Scene
import scalafx.scene.control.cell.CheckBoxTableCell
import scalafx.scene.control.{TableColumn, TableView}
import scalafx.scene.layout.{BorderPane, VBox}

object Application extends Thread with JFXApp {
  class Item(selected_ : Boolean, name_ : String) {
    val selected = new BooleanProperty(this, "selected", selected_)
    val name     = new StringProperty(this, "name", name_)
  }

  val data = ObservableBuffer[Item](
    (1 to 10).map { i => new Item(i % 2 == 0, s"Item $i") }
  )

  stage = new PrimaryStage {
    scene = new Scene {
      title = "Compress Sharp"
      root = new TableView[Item](data) {
        columns ++= List(
          new TableColumn[Item, java.lang.Boolean] {
            text = "Selected"
            // We need to explicitly cast `_.value.selected` to modify boolean type parameters.
            // `scala.Boolean` type is different from `java.lang.Boolean`, but eventually represented the same way
            // by the compiler.
            cellValueFactory = _.value.selected.asInstanceOf[ObservableValue[java.lang.Boolean, java.lang.Boolean]]
            cellFactory = CheckBoxTableCell.forTableColumn(this)
            editable = true
            prefWidth = 180
          },
          new TableColumn[Item, String] {
            text = "Name"
            cellValueFactory = {_.value.name}
            prefWidth = 180
          }
        )
        editable = true
      }
    }

    new JMetro(Style.LIGHT).setScene(scene.value)
  }

  override def start(): Unit = {
    main(Array())
  }
}
