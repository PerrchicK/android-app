package com.perrchick.someapplication.utilities;

import android.os.Bundle;

/**
 * Created by perrchick on 17/05/2017.
 */

public abstract class RunnableWithExtra implements Runnable {
    private Bundle extraData;

    protected Bundle getExtraData() {
        return extraData;
    }

    public void setExtraData(Bundle extraData) {
        this.extraData = extraData;
    }
}
