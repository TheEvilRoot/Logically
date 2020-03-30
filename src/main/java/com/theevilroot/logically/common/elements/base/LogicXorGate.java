package com.theevilroot.logically.common.elements.base;

import com.theevilroot.logically.common.elements.LogicElement;

public class LogicXorGate extends LogicElement {

    public LogicXorGate(double x, double y) {
        super(x, y, 2, 1);
    }

    public LogicXorGate() {
        super(2, 1);
    }

    @Override
    protected boolean f(int outputIndex, Boolean[] inputValues) {
        return inputValues[0] != inputValues[1];
    }
}
