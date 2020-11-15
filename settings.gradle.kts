//pluginManagement {
//  repositories {
//    gradlePluginPortal()
//    maven {
//      val nexusRepo: String by settings
//      url = uri(nexusRepo)
//    }
//  }
//  resolutionStrategy {
//    eachPlugin {
//      if (requested.id.id == "crpt.release" || requested.id.id == "crpt.bootstrap" ) {
//        useModule("ru.crpt.tools:gradle-plugins:${requested.version}")
//      }
//    }
//  }
//}
