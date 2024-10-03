package com.github.kreativshikkk.taskmanagerplugin.graphicObjects

import javax.swing.text.AttributeSet
import javax.swing.text.PlainDocument

class NumberOnlyDocument : PlainDocument() {
    override fun insertString(offs: Int, str: String?, a: AttributeSet?) {
        if (str == null) return
        if (str.all { it.isDigit() } || str == "Set priority...") {
            super.insertString(offs, str, a)
        }
    }
}
