package airbricks.intercom;

import airbricks.assistance.AssistanceBrick;
import airbricks.assistance.AssistanceClient;
import airbricks.assistance.AssistanceDealer;
import bricks.Color;
import bricks.graphic.TextBrick;
import bricks.input.Keyboard;
import bricks.monitor.Monitor;
import bricks.trade.Host;
import bricks.var.Source;
import bricks.var.Var;
import bricks.var.Vars;
import bricks.var.impulse.Impulse;

import java.util.ArrayList;
import java.util.List;


public class AssistedIntercomBrick extends IntercomBrick implements AssistanceClient {

    AssistanceBrick assistance;
    TextBrick supplement;
    List<String> advices;

    public AssistedIntercomBrick(Host host) {
        super(host);
        advices = new ArrayList<>();
        assisted = Vars.set(false);
        assistanceShown = Vars.set(false);

        supplement = new TextBrick(this);
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

        stringChange = string().willChange();

        $bricks.set(supplement);
    }

    Impulse stringChange;

    @Override
    public void frontUpdate() {

        if (selected().get()) {
            var input = input();
            for(var e : input.getEvents().filter(Keyboard.KeyEvent.class)) {
                switch (e.key) {
                    case DOWN, UP -> {
                        if(e.isHold()) {
                            if(!assisted.get()) {
                                e.suppress();
                                requestAssistance();
                            }
                            if(!assistanceShown.get()) {
                                e.suppress();
                                assistance.lightFirst();
                                showAssistance();
                            }
                        }
                    }
                    case ESCAPE -> {
                        if(e.isPress()) {
                            if (assistanceShown.get()) {
                                e.suppress();
                                hideAssistance();
                            }
                        }
                    }
                    case RIGHT -> {
                        if(e.isPress()) {
                            if(note.cursorPosition().get() == note.string().get().length()) {
                                note.paste(supplement.string().get());
                                if(assistanceShown.get()) {
                                    hideAssistance();
                                }
                            }
                        }
                    }
                }
            }
        }

        if(stringChange.occur()) {
            if(assisted.get()) {
                var str = string().get();
                assistance.setOptions(advices.stream().filter(s -> s.contains(str)).toList(), str);
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
                if(!assisted.get()) {
                    requestAssistance();
                }
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
