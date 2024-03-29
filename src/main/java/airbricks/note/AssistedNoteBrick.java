package airbricks.note;

import airbricks.assistance.AssistanceBrick;
import airbricks.assistance.AssistanceClient;
import airbricks.assistance.AssistanceDealer;
import bricks.Color;
import bricks.input.keyboard.Key;
import bricks.slab.TextSlab;
import bricks.input.keyboard.Keyboard;
import bricks.monitor.Monitor;
import bricks.trade.Host;
import bricks.trait.Trait;
import bricks.trait.Traits;
import bricks.trait.sensor.Sensor;
import bricks.trait.subject.SubjectPush;
import suite.suite.Subject;
import suite.suite.action.Statement;


public class AssistedNoteBrick extends NoteBrick implements AssistanceClient {

    public Trait<Long> doubleClicks;
    boolean assisted;
    Monitor unselectListener;

    AssistanceBrick assistance;
    TextSlab supplement;
    final SubjectPush advices;

    public AssistedNoteBrick(Host host) {
        super(host);
        doubleClicks = Traits.set(0L);
        clicks.act(doubleClicks.get(), (p, n) -> {
            if(n - p < 300) doubleClicks.set(n);
        });

        advices = new SubjectPush();
        assisted = false;

        supplement = new TextSlab(this);
        supplement.text().let(() -> {
            var str = text().get();
            for(var advice : advices.autoIn().each().convert(Object::toString)) {
                if(advice.startsWith(str)) {
                    return advice.substring(str.length());
                }
            }
            return "";
        }, text(), advices.changes());
        supplement.color().set(Color.hex("#585855"));
        supplement.left().let(note.right());
        supplement.bottom().let(note.bottom());

        whenTextChange = text().willChange();
        whenAdvicesChange = advices.changes().willChange();
        whenDoubleClick = doubleClicks.willChange();

        $bricks.set(supplement);
    }

    Sensor whenTextChange;
    Sensor whenAdvicesChange;
    Sensor whenDoubleClick;

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
                                if(!note.getSelected().isEmpty()) {
                                    note.paste(supplement.text().get());
                                    if (assisted) {
                                        depriveAssistance();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if(whenTextChange.check() | whenAdvicesChange.check()) {
            if(assisted) {
                var str = text().get();
                assistance.setOptions(advices.select(s -> s.raw().toString().contains(str)));
            }
        }

        if(whenDoubleClick.check()) {
            if(!assisted) {
                requestAssistance(false);
            } else {
                depriveAssistance();
            }
        }

        super.update();
    }

    @Override
    public boolean isAssisted() {
        return assisted;
    }

    Statement onAssistancePick = () -> {
        getNote().select(0, text().get().length());
        getNote().paste(assistance.picks().get().toString());
        depriveAssistance();
    };

    @Override
    public void depriveAssistance() {
        if(assisted) {
            assisted = false;
            assistance.picks().quit(onAssistancePick);
            drop(unselectListener);
            wall().drop(assistance);
            assistance = null;
        }
    }

    public void requestAssistance(boolean preferTop) {
        var dealer = order(AssistanceDealer.class);
        var assistance = dealer.request(this);
        if(assistance != null) {
            assistance.attach(this, advices, preferTop);
            assistance.picks().act(onAssistancePick);
            unselectListener = when(this::seeKeyboard, (a, b) -> !b, this::depriveAssistance);
            assisted = true;
            this.assistance = assistance;
            wall().lay(assistance);
        }
    }

    public Subject advices() {
        return advices;
    }
}
