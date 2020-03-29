package com.theevilroot.logically.common.elements;

import com.theevilroot.logically.common.gui.IView;
import com.theevilroot.logically.common.gui.Resources;
import com.theevilroot.logically.common.math.Vector;
import com.theevilroot.logically.common.observe.Observer;
import com.theevilroot.logically.common.ports.LogicOutputPort;
import com.theevilroot.logically.common.ports.LogicPort;
import java.util.ArrayList;
import java.util.List;

public abstract class LogicElement implements Observer<Boolean>, IView {

    private Vector position = new Vector(30f, 30f);
    private Vector size = new Vector(Vector.UNIT);

    private final int inputCount;
    private final int outputCount;

    protected ArrayList<LogicPort> inputPorts;
    protected ArrayList<LogicOutputPort> outputPorts;

    public LogicElement(int inputCount, int outputCount) {
        this.inputCount = inputCount;
        this.outputCount = outputCount;

        recalculateSize();

        this.inputPorts = new ArrayList<>();
        this.outputPorts = new ArrayList<>();

        double inDelta = getSize().getY() / (inputCount + 1);
        double outDelta = getSize().getY() / (outputCount + 1);

        System.out.println(getSize());
        System.out.println(outDelta);

        for (int i = 0; i < inputCount; i++) {
            LogicPort port = new LogicPort(0, (i + 1) * inDelta);
            inputPorts.add(port);
        }
        for(int i = 0; i < outputCount; i++) {
            outputPorts.add(new LogicOutputPort(0, outDelta * (i + 1)));
        }

        for (LogicPort p : inputPorts) {
            p.getObservableValue().subscribe(this);
        }

    }

    public int getOutputCount() {
        return outputCount;
    }

    public int getInputCount() {
        return inputCount;
    }

    public List<LogicPort> getInputPorts() {
        return inputPorts;
    }

    public List<LogicOutputPort> getOutputPorts() {
        return outputPorts;
    }

    public LogicElement connectPort(int outputIndex, LogicElement element, int inputIndex) {
        this.outputPorts.get(outputIndex).connect(element.inputPorts.get(inputIndex));
        update();
        return this;
    }

    public void update() {
        for (int i = 0; i < getOutputCount(); i++) {
            outputPorts.get(i).setValue(f(i, inputPorts.stream().map(LogicPort::getValue).toArray(Boolean[]::new)));
        }
    }

    @Override
    public void valueUpdated(Boolean newValue) {
        update();
    }

    @Override
    public String toString() {
        return "LogicElement{" +
                "inputCount=" + inputCount +
                ", outputCount=" + outputCount +
                ", inputPorts=" + inputPorts +
                ", outputPorts=" + outputPorts +
                '}';
    }

    @Override
    public Vector getPosition() {
        return position;
    }

    @Override
    public Vector getSize() {
        return size;
    }

    protected void recalculateSize() {
        size.set(Resources.ELEMENT_WIDTH_UNIT, getInputCount() * (Resources.ELEMENT_HEIGHT_PER_WIRE + 1));
    }

    protected abstract boolean f(int outputIndex, Boolean[] inputValues);

}
