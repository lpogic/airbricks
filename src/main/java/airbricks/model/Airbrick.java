package airbricks.model;

import airbricks.model.button.TextPowerButton;
import airbricks.model.assistance.AssistanceDealer;
import airbricks.model.selection.SelectionDealer;
import bricks.trade.Host;
import bricks.wall.Brick;

public abstract class Airbrick<H extends Host> extends Brick<H> {

    public Airbrick(H host) {
        super(host);
    }

    protected SelectionDealer selectionDealer() {
        return order(SelectionDealer.class);
    }
    protected AssistanceDealer assistanceDealer() {
        return order(AssistanceDealer.class);
    }

    protected TextPowerButton button() {
        return new TextPowerButton(this);
    }

    protected TextPowerButton button(String text) {
        var button = button();
        button.string().set(text);
        return button;
    }

    protected Note note() {
        return new Note(this);
    }

    protected Note note(String text) {
        var note = note();
        note.string().set(text);
        return note;
    }

    protected Intercom intercom() {
        return new Intercom(this);
    }
}
