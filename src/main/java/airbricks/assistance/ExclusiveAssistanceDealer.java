package airbricks.assistance;

import bricks.trade.Host;

public class ExclusiveAssistanceDealer implements AssistanceDealer {

    AssistanceBrick assistance;
    AssistanceClient owner;
    Host host;

    public ExclusiveAssistanceDealer(Host host) {
        this.host = host;
    }

    @Override
    public AssistanceBrick request(AssistanceClient client) {
        if(assistance == null) assistance = new AssistanceBrick(host);
        if(owner != null) owner.depriveAssistance();
        owner = client;
        return assistance;
    }
}
