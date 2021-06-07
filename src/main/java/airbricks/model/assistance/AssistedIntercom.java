package airbricks.model.assistance;

import airbricks.model.Intercom;
import bricks.input.Keyboard;
import bricks.monitor.Monitor;
import bricks.trade.Host;
import bricks.var.Source;
import bricks.var.Var;
import bricks.var.Vars;

import java.util.ArrayList;
import java.util.List;


public class AssistedIntercom extends Intercom implements AssistanceClient {

    Assistance assistance;
    List<String> advices;

    public AssistedIntercom(Host host) {
        super(host);
        advices = new ArrayList<>();
        assisted = Vars.set(false);
    }

    @Override
    public void update() {

        var keyDownEvent = false;
        if (selected().get()) {
            var input = input();
            for(var e : input.getEvents().selectAs(Keyboard.KeyEvent.class)) {
                switch (e.key) {
                    case DOWN -> {
                        if(e.isPress()) {
                            keyDownEvent = true;
//                            e.consume();
                        }
                    }
                }
            }
        }
        if(keyDownEvent) {
            if(!assisted().get()) requestAssistance();
            wall().push(assistance);
            assistance.select(0);
        }

        super.update();
    }

    Var<Boolean> assisted;
    Monitor pickListener;
    Monitor selectedListener;

    @Override
    public Source<Boolean> assisted() {
        return assisted;
    }

    @Override
    public void depriveAssistance() {
        assisted.set(false);
        assistance = null;
        $bricks.unset(pickListener, selectedListener);

    }

    public void requestAssistance() {
        var dealer = order(AssistanceDealer.class);
        var assistance = dealer.request(this);
        if(assistance != null) {
            assistance.attach(this, advices);
            pickListener = when(assistance.picked()).then(() -> {
                getNote().select(0, string().get().length());
                getNote().paste(advices.get(assistance.picked().get().value));
                wall().pop(assistance);
            });
            selectedListener = when(selected().willBe(b -> true), () -> {
                if(selected.get()) wall().push(assistance);
                else wall().pop(assistance);
            });
            assisted.set(true);
            this.assistance = assistance;
        }
    }

    public List<String> getAdvices() {
        return advices;
    }
}
