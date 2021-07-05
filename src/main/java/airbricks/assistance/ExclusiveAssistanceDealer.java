package airbricks.assistance;

import bricks.trade.Host;

public class ExclusiveAssistanceDealer implements AssistanceDealer {

    AssistanceBrick assistance;
    AssistanceClient owner;

    public ExclusiveAssistanceDealer(Host host) {
        assistance = new AssistanceBrick(host);
    }

    @Override
    public AssistanceBrick request(AssistanceClient client) {
        if(owner != null) owner.depriveAssistance();
        owner = client;
        return assistance;
    }
}
