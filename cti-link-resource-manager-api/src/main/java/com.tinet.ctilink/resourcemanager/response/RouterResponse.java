package com.tinet.ctilink.resourcemanager.response;

import com.tinet.ctilink.resourcemanager.model.Gateway;
import com.tinet.ctilink.resourcemanager.model.Router;
import com.tinet.ctilink.resourcemanager.model.Routerset;

/**
 * Created by nope-J on 2016/6/7.
 */
public class RouterResponse extends Router {
    private Routerset routerset;
    private Gateway gateway;

    public void setRouterset(Routerset routerset){
        this.routerset = routerset;
    }

    public Routerset getRouterset(){
        return routerset;
    }

    public void setGateway(Gateway gateway){
        this.gateway = gateway;
    }

    public Gateway getGateway(){
        return gateway;
    }
}
