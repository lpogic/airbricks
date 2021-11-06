package airbricks.intercom;

import airbricks.assistance.AssistanceBrick;
import airbricks.assistance.AssistanceClient;
import airbricks.assistance.AssistanceDealer;
import bricks.Color;
import bricks.input.Key;
import bricks.slab.TextSlab;
import bricks.input.Keyboard;
import bricks.monitor.Monitor;
import bricks.trade.Host;
import bricks.var.Push;
import bricks.var.Var;
import bricks.var.impulse.DiversityImpulse;
import bricks.var.impulse.Impulse;
import suite.suite.Subject;

import java.util.ArrayList;
import java.util.List;


public class AssistedIntercomBrick extends IntercomBrick implements AssistanceClient {

    public Push<Long> doubleClicks;
    Impulse onDoubleClick;
    boolean assisted;
    Monitor pickListener;
    Monitor unselectListener;
    Impulse stringChange;

    AssistanceBrick assistance;
    TextSlab supplement;
    List<String> advices;

    public AssistedIntercomBrick(Host host) {
        super(host);
        doubleClicks = Var.push(0L);
        clicks.act((p, n) -> {
            if(n - p < 500) doubleClicks.set(n);
        });
        onDoubleClick = new DiversityImpulse<>(doubleClicks, 0L);

        advices = new ArrayList<>();
        assisted = false;

        supplement = new TextSlab(this);
        supplement.text().let(() -> {
            var str = text().get();
            for(var advice : advices) {
                if(advice.startsWith(str)) {
                    return advice.substring(str.length());
                }
            }
            return "";
        }, text());
        supplement.color().set(Color.hex("#585855"));
        supplement.left().let(note.right());
        supplement.bottom().let(note.bottom());

        stringChange = text().willChange();

        $bricks.set(supplement);
    }

    @Override
    public void update() {

        if (seeKeyboard()) {
            var input = input();
            for(var e : input.getEvents().select(Keyboard.KeyEvent.class)) {
                switch (e.key) {
                    case DOWN, UP -> {
                        if(e.isHold()) {
                            if(!assisted) {
                                e.suppress();
                                requestAssistance(e.key == Key.Code.UP);
                            }
                        }
                    }
                    case ESCAPE -> {
                        if(e.isRelease()) {
                            if (assisted) {
                                e.suppress();
                                depriveAssistance();
                            }
                        }
                    }
                    case RIGHT -> {
                        if(e.isPress()) {
                            if(note.cursorPosition().get() == note.text().get().length()) {
                                note.paste(supplement.text().get());
                                if(assisted) {
                                    depriveAssistance();
                                }
                            }
                        }
                    }
                }
            }
        }

        if(stringChange.occur()) {
            if(assisted) {
                var str = text().get();
                assistance.setOptions(advices.stream().filter(s -> s.contains(str)).toList(), str);
            }
        }

        if(onDoubleClick.occur()) {
            if(!assisted) {
                requestAssistance(false);
            }
        }

        super.update();
    }

    @Override
    public boolean isAssisted() {
        return assisted;
    }

    @Override
    public void depriveAssistance() {
        if(assisted) {
            assisted = false;
            drop(pickListener, unselectListener);
            wall().drop(assistance);
            assistance = null;
        }
    }

    public void requestAssistance(boolean preferTop) {
        var dealer = order(AssistanceDealer.class);
        var assistance = dealer.request(this);
        if(assistance != null) {
            assistance.attach(this, advices, preferTop);
            pickListener = when(assistance.picked()).then(() -> {
                getNote().select(0, text().get().length());
                getNote().paste(advices.get(assistance.picked().get().value));
                depriveAssistance();
            });
            unselectListener = when(this::seeKeyboard, (a, b) -> !b, this::depriveAssistance);
            assisted = true;
            this.assistance = assistance;
            assistance.indicateFirst();
            wall().lay(assistance);
        }
    }

    public List<String> getAdvices() {
        return advices;
    }

    public void advices(Subject adv) {
        advices.clear();
        advices.addAll(adv.list().each().convert(Object::toString).toList());
    }
}
