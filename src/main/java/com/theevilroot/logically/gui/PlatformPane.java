package com.theevilroot.logically.gui;


import com.theevilroot.logically.common.elements.LogicCircuit;
import com.theevilroot.logically.common.elements.LogicElement;
import com.theevilroot.logically.common.elements.LogicInputPanel;
import com.theevilroot.logically.common.elements.LogicOutputPanel;
import com.theevilroot.logically.common.elements.base.LogicAndGate;
import com.theevilroot.logically.common.elements.base.LogicMultiplexer;
import com.theevilroot.logically.common.elements.base.LogicNotGate;
import com.theevilroot.logically.common.elements.base.LogicOrGate;
import com.theevilroot.logically.common.math.Vector;
import com.theevilroot.logically.common.mouse.MouseHandler;
import com.theevilroot.logically.common.mouse.MouseTrace;
import com.theevilroot.logically.common.mouse.states.IStateful;
import com.theevilroot.logically.common.ports.LogicPort;
import com.theevilroot.logically.common.view.IView;
import com.theevilroot.logically.common.view.drawers.IDrawer;
import com.theevilroot.logically.common.view.drawers.factory.IDrawerFactory;
import com.theevilroot.logically.common.view.impl.BaseView;
import com.theevilroot.logically.common.view.ui.UIButton;
import com.theevilroot.logically.common.view.ui.UITextView;
import com.theevilroot.logically.common.view.ui.UIView;
import com.theevilroot.logically.gui.drawable.impl.SimpleDrawablePane;
import javafx.beans.InvalidationListener;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;

public class PlatformPane extends SimpleDrawablePane implements EventHandler<MouseEvent> {

    private final Canvas canvas;

    private LogicCircuit circuit;
    private IDrawerFactory drawerFactory;

    private ArrayList<UIView> uiViews = new ArrayList<>();

    private Vector mouse = new Vector(Vector.UNIT);

    public PlatformPane(IDrawerFactory drawerFactory) {
        this.circuit = new LogicCircuit(0, 0, 600, 900);
        this.drawerFactory = drawerFactory;

        this.canvas = new Canvas();
        canvas.widthProperty().bind(this.widthProperty());
        canvas.heightProperty().bind(this.heightProperty());

        InvalidationListener canvasResizeListener = (e) -> {
            circuit.setSize(canvas.getWidth(), canvas.getHeight());
            setDirty();
        };
        canvas.widthProperty().addListener(canvasResizeListener);
        canvas.heightProperty().addListener(canvasResizeListener);

        canvas.setOnMouseMoved(e -> mouse.set(e.getX(), e.getY()));

        canvas.setOnMouseClicked(this);

        getChildren().add(canvas);

        LogicInputPanel input = new LogicInputPanel(-50f, 0, 10);
        LogicOutputPanel output = new LogicOutputPanel(500f, 0f, 2);

        LogicMultiplexer mux = new LogicMultiplexer(4);

        input.connectPort(0, mux, 0);
        input.connectPort(1, mux, 1);
        input.connectPort(2, mux, 2);
        input.connectPort(3, mux, 3);
        input.connectPort(4, mux, 4);
        input.connectPort(5, mux, 5);

        mux.connectPort(0, output, 0);

        LogicAndGate and = new LogicAndGate(100f, 100f, 2);
        LogicOrGate or = new LogicOrGate(200f, 200f, 2);
        LogicNotGate not = new LogicNotGate(100f, 100f);

        circuit.addElement(mux);
        circuit.addElement(input);
        circuit.addElement(output);
        circuit.addElement(and);
        circuit.addElement(or);
        circuit.addElement(not);

        UITextView textView = new UITextView(100f, 100f, "Hello World", 24f);
        UIButton button = new UIButton(150f, 150f, "Click me");
        button.setClickListener((event, sender) -> {
            System.out.println("CLiiick");
        });

        uiViews.add(textView);
        uiViews.add(button);

        drawingTimer.start();
    }

    private void drawCircuit() {
        circuit.handleHover(mouse);
        IDrawer<? super LogicCircuit> drawer = drawerFactory.getDrawerFor(circuit.getClass());
        if (drawer != null)
            drawer.drawElement(drawerFactory, canvas.getGraphicsContext2D(), canvas, circuit);
        else throw new RuntimeException("circuit");
    }

    private void drawUi() {
        uiViews.forEach(ui -> {
            IDrawer<? super IView> uiDrawer = drawerFactory.getDrawerFor(ui.getClass());
            if (uiDrawer != null) {
                uiDrawer.drawElement(drawerFactory, canvas.getGraphicsContext2D(), canvas, ui);
            } else throw new RuntimeException(ui.getClass().getSimpleName());
        });
    }

    public void draw() {
        drawCircuit();
        drawUi();
        if (circuit.is(IStateful.DIRTY) || uiViews.stream().anyMatch(e -> e.is(IStateful.DIRTY)))
            setDirty();
    }

    @Override
    public void resetDirty() {
        super.resetDirty();
        circuit.unset(IStateful.DIRTY);
        uiViews.forEach(e -> e.unset(IStateful.DIRTY));
    }

    @Override
    public void handle(MouseEvent mouseEvent) {
        Vector absMouse = new Vector(mouseEvent.getX(), mouseEvent.getY());
        MouseTrace trace = new MouseTrace();

        for (UIView uiView : uiViews) {
            Vector relMouse = Vector.minus(absMouse, uiView.getPosition());
            if (uiView.handle(mouseEvent, relMouse, trace)) {
                setDirty();
                return;
            }
            trace.reset();
        }

        Vector relMouse = Vector.minus(absMouse, circuit.getPosition());
        circuit.handle(mouseEvent, relMouse, trace);
        if (trace.getAcceptor() != null) {
            MouseHandler acceptor = trace.getAcceptor();
            if (acceptor instanceof LogicCircuit) {
                circuit.onCircuitClick(mouseEvent, relMouse);
            } else if (acceptor instanceof LogicElement) {
                circuit.onElementClick(mouseEvent, relMouse, (LogicElement) acceptor);
            } else if (acceptor instanceof LogicPort) {
                MouseHandler lastTrace = trace.getTrace().get(trace.getTrace().size() - 1);
                if (lastTrace instanceof LogicElement)
                    circuit.onElementPortClick(mouseEvent, relMouse, (LogicElement) lastTrace, (LogicPort) acceptor);
            }
        }
        setDirty();
    }
}
