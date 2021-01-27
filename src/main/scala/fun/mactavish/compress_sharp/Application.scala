package fun.mactavish.compress_sharp

import jfxtras.styles.jmetro.{JMetro, Style}
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage

object Application extends Thread with JFXApp {
  stage = new PrimaryStage {
    scene = new PrimaryScene(stage)
    new JMetro(Style.LIGHT).setScene(scene.value)
  }

  override def start(): Unit = {
    main(Array())
  }
}
