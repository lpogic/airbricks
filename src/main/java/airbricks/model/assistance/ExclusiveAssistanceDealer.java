package airbricks.model.assistance;

import bricks.trade.Host;

public class ExclusiveAssistanceDealer implements AssistanceDealer {

    Assistance assistance;
    AssistanceClient owner;

    public ExclusiveAssistanceDealer(Host host) {
        assistance = new Assistance(host);
    }

    @Override
    public Assistance request(AssistanceClient client) {
        if(owner != null) owner.depriveAssistance();
        owner = client;
        return assistance;
    }
}
