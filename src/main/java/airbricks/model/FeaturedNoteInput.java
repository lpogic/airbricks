package airbricks.model;

import bricks.Color;
import bricks.graphic.ColorText;
import bricks.trade.Host;

public class FeaturedNoteInput extends NoteInput {

    final ColorText feature;

    public FeaturedNoteInput(Host host) {
        super(host);

        feature = text();
        feature.color().set(Color.mix(0.5, 0.5, 0.5));
        feature.aim(this);
        feature.height().let(height());
        when(note.shown(), () -> {
            if(note.text.string().get().isEmpty()) $bricks.set(feature);
        }, () -> $bricks.unset(feature));
        when(selected(), () -> $bricks.unset(feature), () -> {
            if(note.text.string().get().isEmpty()) $bricks.set(feature);
        });
    }

    public ColorText getFeature() {
        return feature;
    }
}