package com.dansoftware.libraryapp.gui.entry.login.dbmanager;

import com.dansoftware.libraryapp.db.DBMeta;
import com.dansoftware.libraryapp.gui.workbench.SimpleHeaderView;
import com.dlsc.workbenchfx.view.controls.ToolbarItem;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;

import java.util.List;

import static com.dansoftware.libraryapp.locale.I18N.getGeneralWord;

public class DBManagerView extends SimpleHeaderView<DBManagerTable> {
    public DBManagerView(List<DBMeta> items) {
        super(getGeneralWord("database.manager.title"), new MaterialDesignIconView(MaterialDesignIcon.DATABASE));
        super.setContent(new DBManagerTable(this, items));
        this.getToolbarControlsRight().add(new ToolbarItem(new MaterialDesignIconView(MaterialDesignIcon.RELOAD), event -> {
            this.getContent().refresh();
        }));
    }
}
