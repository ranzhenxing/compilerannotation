package com.tianque.libcompiler;

import javax.lang.model.element.VariableElement;

public class VariableInfo {
    int viewId;
    VariableElement variableElement;
    public VariableElement getVariableElement() {
        return variableElement;
    }

    public void setVariableElement(VariableElement variableElement) {
        this.variableElement = variableElement;
    }

    @Override
    public String toString() {
        return "VariableInfo{" +
                "viewId=" + viewId +
                ", variableElement=" + variableElement +
                '}';
    }

    public int getViewId() {
        return viewId;
    }

    public void setViewId(int viewId) {
        this.viewId = viewId;
    }
}
