package airbricks.model.assistance;

import airbricks.model.Intercom;
import bricks.Color;
import bricks.Debug;
import bricks.graphic.ColorText;
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
    ColorText supplement;
    List<String> advices;

    public AssistedIntercom(Host host) {
        super(host);
        advices = new ArrayList<>();
        assisted = Vars.set(false);
        assistanceShown = Vars.set(false);

        supplement = new ColorText(this);
        supplement.string().let(() -> {
            var str = string().get();
            for(var advice : advices) {
                if(advice.startsWith(str)) {
                    return advice.substring(str.length());
                }
            }
            return "";
        }, string());
        supplement.color().set(Color.hex("#585855"));
        supplement.left().let(note.right());
        supplement.bottom().let(note.bottom());
        $bricks.set(supplement);
    }


    @Override
    public void frontUpdate() {

        if (selected().get()) {
            var input = input();
            for(var e : input.getEvents().filter(Keyboard.KeyEvent.class)) {
                switch (e.key) {
                    case DOWN -> {
                        if(e.isHold()) {
                            if(!assisted.get()) {
                                suppressEvent(e);
                                requestAssistance();
                            }
                            if(!assistanceShown.get()) {
                                suppressEvent(e);
                                assistance.lightFirst();
                                showAssistance();
                            }
                        }
                    }
                    case BACKSPACE -> {
                        if(e.isPress()) {
                            if (assistanceShown.get()) {
                                suppressEvent(e);
                                hideAssistance();
                            }
                        }
                    }
                }
            }
        }

        super.frontUpdate();
    }

    Var<Boolean> assisted;
    Var<Boolean> assistanceShown;
    Monitor pickListener;
    Monitor unselectListener;

    @Override
    public Source<Boolean> assisted() {
        return assisted;
    }

    @Override
    public void depriveAssistance() {
        assisted.set(false);
        showAssistance(false);
        assistance = null;
        $bricks.unset(pickListener, unselectListener);

    }

    public void requestAssistance() {
        var dealer = order(AssistanceDealer.class);
        var assistance = dealer.request(this);
        if(assistance != null) {
            assistance.attach(this, advices);
            pickListener = when(assistance.picked()).then(() -> {
                getNote().select(0, string().get().length());
                getNote().paste(advices.get(assistance.picked().get().value));
                hideAssistance();
            });
            unselectListener = when(selected().willGive(false), this::hideAssistance);
            assisted.set(true);
            this.assistance = assistance;
        }
    }

    protected void showAssistance(boolean show) {
        if(assistanceShown.get() != show) {
            if(show) {
                wall().push(assistance);
            } else {
                wall().pop(assistance);
            }
            assistanceShown.set(show);
        }
    }

    protected void showAssistance() {
        showAssistance(true);
    }

    protected void hideAssistance() {
        showAssistance(false);
    }

    public List<String> getAdvices() {
        return advices;
    }
}
