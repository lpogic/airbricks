package airbricks.model;

import bricks.Color;
import bricks.graphic.ColorText;
import bricks.trade.Host;

public class NoteWithPlaceholder extends Note {

    public final ColorText placeholder;

    public NoteWithPlaceholder(Host host, String placeholderString) {
        super(host);

        placeholder = text();
        placeholder.string().set(placeholderString);
        placeholder.color().set(Color.mix(0.5, 0.5, 0.5));
        placeholder.aim(this);
        placeholder.height().let(height());
        when(shown(), () -> {
            if(text.string().get().isEmpty()) show(placeholder);
        }, () -> hide(placeholder));
        when(selected(), () -> hide(placeholder), () -> {
            if(text.string().get().isEmpty()) show(placeholder);
        });
    }
}