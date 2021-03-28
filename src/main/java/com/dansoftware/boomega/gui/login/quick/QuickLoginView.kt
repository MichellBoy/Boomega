package com.dansoftware.boomega.gui.login.quick

import com.dansoftware.boomega.db.DatabaseMeta
import com.dansoftware.boomega.gui.context.Context
import com.dansoftware.boomega.gui.login.DatabaseLoginListener
import com.dlsc.workbenchfx.SimpleHeaderView
import com.dansoftware.boomega.gui.login.quick.QuickLoginView
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon
import com.dansoftware.boomega.gui.context.ContextTransformable
import com.dansoftware.boomega.gui.login.quick.QuickForm
import com.dansoftware.boomega.i18n.I18N
import javafx.scene.layout.Region
import javafx.scene.layout.VBox

class QuickLoginView(databaseMeta: DatabaseMeta, loginListener: DatabaseLoginListener) : SimpleHeaderView<Region?>(
    "${I18N.getValue("login.quick.title")} - $databaseMeta", MaterialDesignIconView(MaterialDesignIcon.LOGIN)
), ContextTransformable {
    private val asContext: Context = Context.from(this)

    init {
        content = QuickForm(asContext, databaseMeta, loginListener)
    }

    override fun getContext(): Context {
        return asContext
    }
}