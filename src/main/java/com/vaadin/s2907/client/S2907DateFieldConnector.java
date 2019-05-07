package com.vaadin.s2907.client;

import com.vaadin.client.ui.datefield.DateFieldConnector;
import com.vaadin.s2907.S2907DateField;
import com.vaadin.shared.ui.Connect;
import com.vaadin.shared.ui.datefield.AbstractDateFieldServerRpc;

@Connect(S2907DateField.class)
public class S2907DateFieldConnector extends DateFieldConnector {

    @Override
    protected void init() {
        super.init();
        getWidget().rpc = getRpcProxy(S2907DateFieldServerRpc.class);
    }

    public interface S2907DateFieldServerRpc
            extends AbstractDateFieldServerRpc {
    }
}
