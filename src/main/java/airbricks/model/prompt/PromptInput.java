package airbricks.model.prompt;

import airbricks.model.NoteInput;
import bricks.input.Keyboard;
import bricks.trade.Host;
import bricks.var.Var;


public class PromptInput extends NoteInput implements PromptClient {

    Prompt prompt;

    public PromptInput(Host host) {
        super(host);
    }

    @Override
    public void update() {

        if (selected().get()) {
            var keyboard = keyboard();
            var keyDownEvent = false;
            var kEvents = keyboard.getEvents();
            for(var e : kEvents.eachAs(Keyboard.KeyEvent.class)) {
                switch (e.key) {
                    case DOWN -> {
                        if(e.isHold()) {
                            kEvents.unset(e);
                            keyDownEvent = true;
                        }
                    }
                }
            }
            if(keyDownEvent) {
                prompt.select(0);
            }
        }

        super.update();
    }

    @Override
    public Var<Boolean> hasPrompt() {
        return null;
    }
}
