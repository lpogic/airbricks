package airbricks.model;

import bricks.Color;
import bricks.graphic.ColorText;
import bricks.trade.Host;

public class NoteWithPlaceholder extends Note {

    public final ColorText placeholder;

    public NoteWithPlaceholder(Host host, String placeholderString) {
        super(host);

        placeholder = text().setString(placeholderString);
        placeholder.color().set(Color.mix(0.5, 0.5, 0.5));
        placeholder.position().let(position());
        placeholder.height().let(height());
        placeholder.xOrigin().let(xOrigin());
        placeholder.yOrigin().let(yOrigin());
        when(shown, () -> {
            if(text.getString().isEmpty()) show(placeholder);
        }, () -> hide(placeholder));
        when(selected, () -> hide(placeholder), () -> {
            if(text.getString().isEmpty()) show(placeholder);
        });
    }
}