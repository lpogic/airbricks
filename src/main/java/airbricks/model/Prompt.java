package airbricks.model;

import bricks.Color;
import bricks.graphic.ColorRectangle;
import bricks.trade.Host;
import suite.suite.$;
import suite.suite.Subject;

import java.util.ArrayList;
import java.util.List;

public class Prompt<O>/* extends Airbrick<Host>*/ {

    ColorRectangle bg;
    Subject options;
    int maxShownInputs;
    int firstOption;

    /*public Prompt(Host host) {
        super(host);

        maxShownInputs = 6;
        firstOption = 0;
        options = new $();

        bg = rect();
        bg.color().set(Color.hex("#19002e"));

        var leftRect = rect();
        leftRect.y().let(bg.y());
        leftRect.width().set(40);
        leftRect.left().let(bg.left());

        var rightRect = rect();
        rightRect.y().let(bg.y());
        rightRect.width().set(40);
        rightRect.right().let(bg.right());

        addBrick(bg, leftRect, rightRect);
    }*/

}
