package com.dansoftware.libraryapp.gui.dock.docksystem;

import com.dansoftware.libraryapp.gui.dock.DockPosition;
import com.dansoftware.libraryapp.gui.dock.border.DockFrame;
import com.dansoftware.libraryapp.gui.dock.docknode.DockNode;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.util.Objects;

public class DockSystem<C extends Node> extends StackPane {

    private final ObservableList<DockNode> dockNodes = FXCollections.observableArrayList();
    private final ObjectProperty<C> center = new SimpleObjectProperty<>(this, "centerPane");
    private final DockFrame frame;
    private final SplitPaneSystem splitPaneSystem;


    public DockSystem() {
        this.splitPaneSystem = new SplitPaneSystem();
        this.frame = new DockFrame();
        this.frame.setCenter(splitPaneSystem);

        this.getChildren().add(this.frame);

        this.center.addListener((observable, oldCenter, newCenter) ->
                splitPaneSystem.setCenterNode(newCenter));
    }

    public void hide(DockPosition pos, DockNode dockNode) {
        Objects.requireNonNull(pos, "The pos mustn't be null");

        pos.getRemover().accept(this.splitPaneSystem, dockNode);
    }

    public void dock(DockPosition pos, DockNode dockNode) {
        if (pos == null) pos = DockPosition.TOP_LEFT;

        this.frame.allocate(pos, dockNode.getBorderButton());

        dockNode.setDockSystem(this);
        dockNode.setDockPosition(pos);

        pos.getAdder().accept(this.splitPaneSystem, dockNode);

        this.dockNodes.add(dockNode);
    }

    public ObservableList<DockNode> getDockNodes() {
        return dockNodes;
    }

    public Node getCenter() {
        return center.get();
    }

    public ObjectProperty<C> centerProperty() {
        return center;
    }

    public void setCenter(C center) {
        this.center.set(center);
    }
}
