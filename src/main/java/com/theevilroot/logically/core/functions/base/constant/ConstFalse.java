package com.theevilroot.logically.core.functions.base.constant;

import com.theevilroot.logically.core.functions.BaseLogicFunction;

public class ConstFalse extends BaseLogicFunction {

    public ConstFalse(int inputCount, int outputCount) {
        super(inputCount, outputCount);
    }

    @Override
    public boolean f(int outputIndex, Boolean[] inputValues) {
        return false;
    }
}
