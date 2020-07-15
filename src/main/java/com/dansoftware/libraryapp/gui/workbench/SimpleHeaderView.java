package com.dansoftware.libraryapp.gui.workbench;

import com.dlsc.workbenchfx.model.WorkbenchModule;
import com.dlsc.workbenchfx.view.controls.ToolbarItem;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.scene.Node;
import javafx.scene.layout.Region;

public class SimpleHeaderView<T extends Region> extends BaseWorkbench {

    private class ModuleImpl extends WorkbenchModule {

        protected ModuleImpl() {
            super("-", (FontAwesomeIcon) null);
        }

        @Override
        public Node activate() {
            return SimpleHeaderView.this.content;
        }
    }

    private T content;

    public SimpleHeaderView(String title, Node graphic) {
        this(title, graphic, null);
    }

    public SimpleHeaderView(String title, Node graphic, Region content) {
        this.getToolbarControlsLeft().add(new ToolbarItem(title, graphic));
        this.getModules().add(new ModuleImpl());
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }
}
