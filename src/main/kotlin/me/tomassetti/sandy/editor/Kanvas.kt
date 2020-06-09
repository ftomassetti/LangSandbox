package me.tomassetti.sandy.editor

import me.tomassetti.kanvas.Kanvas
import me.tomassetti.kanvas.languageSupportRegistry
import javax.swing.SwingUtilities

fun main(args: Array<String>) {
    languageSupportRegistry.register("sandy", SandyLanguageSupport)
    SwingUtilities.invokeLater { Kanvas().createAndShowKanvasGUI() }
}
