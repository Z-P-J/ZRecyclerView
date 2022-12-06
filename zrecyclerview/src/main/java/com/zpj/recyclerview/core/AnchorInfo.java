package com.zpj.recyclerview.core;

import android.view.View;

public class AnchorInfo {

    public int x;
    public int y;
    public int position;
    public View anchorView;

    @Override
    public String toString() {
        return "AnchorInfo{" +
                "x=" + x +
                ", y=" + y +
                ", position=" + position +
                ", anchorView=" + anchorView +
                '}';
    }
}
