package fun.mactavish.compress_sharp

import jfxtras.styles.jmetro.{JMetro, Style}
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage

object Application extends Thread with JFXApp {
  stage = new PrimaryStage
  private val scene = new PrimaryScene(stage)
  stage.setScene(scene)

  new JMetro(Style.LIGHT).setScene(scene)

  override def start(): Unit = {
    main(Array())
  }
}
