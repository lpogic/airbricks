package airbricks.model;

import bricks.Color;
import bricks.Point;
import bricks.XOrigin;
import bricks.YOrigin;
import bricks.font.BackedFont;
import bricks.font.FontManager;
import bricks.graphic.ColorRectangle;
import bricks.graphic.ColorText;
import bricks.input.Mouse;
import bricks.input.Story;
import bricks.input.UserAction;
import bricks.trade.Host;
import bricks.var.Source;
import bricks.var.Var;
import bricks.var.Vars;
import bricks.var.impulse.Impulse;
import bricks.wall.Brick;
import suite.suite.util.Cascade;

public class Note extends Brick<Host> {

    ColorText text;
    ColorRectangle cursor;
    Var<Integer> cursorPosition;
    NoteCars cars;

    Impulse mousePress;

    public Note(Host host) {
        super(host);

        selected = Vars.set(false);
        when(selected, this::_select, this::_unselect);
        shown = Vars.set(false);
        when(shown, this::_show, this::_hide);

        mousePress = mouse().leftButton().willBe(Mouse.Button::pressed);

        text = text().setString("Fill me!").setHeight(20).setColor(Color.mix(1,1,1))
                .setOrigin(XOrigin.CENTER, YOrigin.CENTER).setPosition(400, 300);

        cursorPosition = Vars.set(0);

        cursor = rect().setWidth(1);
        cursor.height().let(text.height());
        cursor.color().let(text.color());
        cursor.yOrigin().let(text.yOrigin());
        cursor.position().let(() -> {
           int pos = cursorPosition.get();
           BackedFont font = order(FontManager.class).getFont(text.getFont(), text.getHeight());
           float xOffset = font.getLoadedFont().getStringWidth(text.getString().substring(0, pos), text.getHeight());
           Point textPosition = text.getPosition();
           XOrigin xOrigin = text.getXOrigin();
           return switch (xOrigin) {
               case LEFT -> new Point(textPosition.getX() + xOffset,
                       textPosition.getY() + font.getScaledDescent() / 2);
               case CENTER -> new Point(textPosition.getX() - text.getWidth() / 2 + xOffset,
                       textPosition.getY() + font.getScaledDescent() / 2);
               case RIGHT -> new Point(textPosition.getX() - text.getWidth() + xOffset,
                       textPosition.getY() + font.getScaledDescent() / 2);
           };
        }, text.font(), text.width(), text.string(), text.height(), text.position(), text.xOrigin(), cursorPosition);

        cars = new NoteCars(this);
    }

    @Override
    public void update() {
        super.update();

        if(isSelected()) {
            boolean pressOccur = mousePress.occur();
            var mouse = mouse();
            if(mouse.leftButton().isPressed()) {
                float x = switch (text.getXOrigin()) {
                    case CENTER -> text.getPosition().getX() - text.getWidth() / 2;
                    case LEFT -> text.getPosition().getX();
                    case RIGHT -> text.getPosition().getX() - text.getWidth();
                };
                int newCursorPos = order(FontManager.class).getFont(text.getFont())
                        .getCursorPosition(text.getString(), text.getHeight(), x, mouse().position().get().getX());
                cursorPosition.set(newCursorPos);
                cars.headIndex.set(newCursorPos);
                if(pressOccur) {
                    cars.tailIndex.set(newCursorPos);
                }
            }

            var keyboard = keyboard();
            var charEvents = keyboard.getCharEvents();
            if(charEvents.size() > 0) {
                int cursorPos = cursorPosition.get();
                StringBuilder stringBuilder = new StringBuilder();
                for (var che : keyboard.getCharEvents()) {
                    stringBuilder.appendCodePoint(che.getCodepoint());
                }
                String inset = stringBuilder.toString();
                String str = text.getString();
                if(cars.isAny()) {
                    int[] minMax = cars.getMinMax();
                    text.setString(str.substring(0, minMax[0]) + inset + str.substring(minMax[1]));
                    cursorPosition.set(minMax[0] + inset.length());
                    cars.reset();
                } else {
                    text.setString(str.substring(0, cursorPos) + inset + str.substring(cursorPos));
                    cursorPosition.set(cursorPos + inset.length());
                }
            }

            var keyEvents = keyboard.getEvents();
            if(keyEvents.size() > 0) {
                int cursorPos = cursorPosition.get();
                for (var e : keyboard.getEvents()) {
                    if (e.isHold()) {
                        if (e.key.isNumPad() && !e.isNumLocked()) {
                            switch (e.key) {
                                case NUM_7_HOME -> {
                                    if(cars.isAny()) {
                                        if(e.isShifted()) {
                                            cars.headIndex.set(0);
                                        } else {
                                            cars.reset();
                                        }
                                    } else {
                                        if(e.isShifted()) {
                                            cars.tailIndex.set(cursorPos);
                                            cars.headIndex.set(0);
                                        }
                                    }
                                    cursorPosition.set(0);
                                }
                                case NUM_1_END -> {
                                    if(cars.isAny()) {
                                        if(e.isShifted()) {
                                            cars.headIndex.set(text.getString().length());
                                        } else {
                                            cars.reset();
                                        }
                                    } else {
                                        if(e.isShifted()) {
                                            cars.tailIndex.set(cursorPos);
                                            cars.headIndex.set(text.getString().length());
                                        }
                                    }
                                    cursorPosition.set(text.getString().length());
                                }
                                case NUM_0_INSERT -> {
                                    String clip = clipboard().get();
                                    int[] minMax;
                                    if(cars.isAny()) {
                                        minMax = cars.getMinMax();
                                    } else {
                                        minMax = new int[]{cursorPos, cursorPos};
                                    }
                                    String str = text.getString();
                                    text.setString(str.substring(0, minMax[0]) +
                                            clip +
                                            str.substring(minMax[1]));
                                    cursorPosition.set(cursorPos + clip.length());
                                }
                                case NUM_DECIMAL_DELETE -> {
                                    String str = text.getString();
                                    if(cars.isAny()) {
                                        int[] minMax = cars.getMinMax();
                                        text.setString(str.substring(0, minMax[0]) + str.substring(minMax[1]));
                                        cursorPosition.set(minMax[0]);
                                        cars.reset();
                                    } else {
                                        if (cursorPos < str.length()) {
                                            String txt = text.getString();
                                            text.setString(txt.substring(0, cursorPos) + txt.substring(cursorPos + 1));
                                        }
                                    }
                                }
                            }
                        }
                        switch (e.key) {
                            case BACKSPACE -> {
                                if(cars.isAny()) {
                                    int[] minMax = cars.getMinMax();
                                    String str = text.getString();
                                    text.setString(str.substring(0, minMax[0]) + str.substring(minMax[1]));
                                    cursorPosition.set(minMax[0]);
                                    cars.reset();
                                } else {
                                    if (cursorPos > 0) {
                                        cursorPosition.set(cursorPos - 1);
                                        String txt = text.getString();
                                        text.setString(txt.substring(0, cursorPos - 1) + txt.substring(cursorPos));
                                    }
                                }
                            }
                            case DELETE -> {
                                String str = text.getString();
                                if(cars.isAny()) {
                                    int[] minMax = cars.getMinMax();
                                    text.setString(str.substring(0, minMax[0]) + str.substring(minMax[1]));
                                    cursorPosition.set(minMax[0]);
                                    cars.reset();
                                } else {
                                    if (cursorPos < str.length()) {
                                        String txt = text.getString();
                                        text.setString(txt.substring(0, cursorPos) + txt.substring(cursorPos + 1));
                                    }
                                }
                            }
                            case LEFT -> {
                                int head = cars.getHead();
                                int tail = cars.getTail();
                                if(e.isShifted()) {
                                    if(e.isControlled()) {
                                        if(e.isAltered()) {
                                            if(tail < head) {
                                                cars.tailIndex.set(head);
                                                cars.headIndex.set(tail);
                                                cursorPosition.set(tail);
                                            }
                                        } else {
                                            if (cursorPos > 0) {
                                                int jump = ctrlJump(cursorPos, true);
                                                if(!cars.isAny()) {
                                                    cars.tailIndex.set(cursorPos);
                                                }
                                                cars.headIndex.set(cursorPos - jump);
                                                cursorPosition.set(cursorPos - jump);
                                            }
                                        }
                                    } else {
                                        if (!cars.isAny()) {
                                            cars.tailIndex.set(cursorPos);
                                        }
                                        if (cursorPos > 0) {
                                            cars.headIndex.set(cursorPos - 1);
                                            cursorPosition.set(cursorPos - 1);
                                        }
                                    }
                                } else {
                                    if(e.isControlled()) {
                                        if (cursorPos > 0) {
                                            if (cars.isAny()) {
                                                cars.reset();
                                            }
                                            int jump = ctrlJump(cursorPos, true);
                                            cursorPosition.set(cursorPos - jump);
                                        }
                                    } else {
                                        if (cars.isAny()) {
                                            cursorPosition.set(cars.getMinIndex());
                                            cars.reset();
                                        } else {
                                            if (cursorPos > 0) {
                                                cursorPosition.set(cursorPos - 1);
                                            }
                                        }
                                    }
                                }
                            }
                            case RIGHT -> {
                                int head = cars.getHead();
                                int tail = cars.getTail();
                                if(e.isShifted()) {
                                    if(e.isControlled()) {
                                        if(e.isAltered()) {
                                            if(tail > head) {
                                                cars.tailIndex.set(head);
                                                cursorPosition.set(tail);
                                                cars.headIndex.set(tail);
                                            }
                                        } else {
                                            if (cursorPos < text.getString().length()) {
                                                int jump = ctrlJump(cursorPos, false);
                                                if(!cars.isAny()) {
                                                    cars.tailIndex.set(cursorPos);
                                                }
                                                cars.headIndex.set(cursorPos + jump);
                                                cursorPosition.set(cursorPos + jump);
                                            }
                                        }
                                    } else {
                                        if (!cars.isAny()) {
                                            cars.tailIndex.set(cursorPos);
                                        }
                                        if (cursorPos < text.getString().length()) {
                                            cursorPosition.set(cursorPos + 1);
                                            cars.headIndex.set(cursorPos + 1);
                                        }
                                    }
                                } else {
                                    if(e.isControlled()) {
                                        if (cursorPos < text.getString().length()) {
                                            if (cars.isAny()) {
                                                cars.reset();
                                            }
                                            int jump = ctrlJump(cursorPos, false);
                                            cursorPosition.set(cursorPos + jump);
                                        }
                                    } else {
                                        if (cars.isAny()) {
                                            cursorPosition.set(cars.getMaxIndex());
                                            cars.reset();
                                        } else {
                                            if (cursorPos < text.getString().length()) {
                                                cursorPosition.set(cursorPos + 1);
                                            }
                                        }
                                    }
                                }
                            }
                            case HOME -> {
                                if(cars.isAny()) {
                                    if(e.isShifted()) {
                                        cars.headIndex.set(0);
                                    } else {
                                        cars.reset();
                                    }
                                } else {
                                    if(e.isShifted()) {
                                        cars.tailIndex.set(cursorPos);
                                        cars.headIndex.set(0);
                                    }
                                }
                                cursorPosition.set(0);
                            }
                            case END -> {
                                if(cars.isAny()) {
                                    if(e.isShifted()) {
                                        cars.headIndex.set(text.getString().length());
                                    } else {
                                        cars.reset();
                                    }
                                } else {
                                    if(e.isShifted()) {
                                        cars.tailIndex.set(cursorPos);
                                        cars.headIndex.set(text.getString().length());
                                    }
                                }
                                cursorPosition.set(text.getString().length());
                            }
                            case CAPS_LOCK -> {
                                if(cars.isAny()) {
                                    int[] minMax = cars.getMinMax();
                                    String str = text.getString();
                                    if(e.isCapsLocked()) {
                                        text.setString(str.substring(0, minMax[0]) +
                                                str.substring(minMax[0], minMax[1]).toUpperCase() +
                                                str.substring(minMax[1]));
                                    } else {
                                        text.setString(str.substring(0, minMax[0]) +
                                                str.substring(minMax[0], minMax[1]).toLowerCase() +
                                                str.substring(minMax[1]));
                                    }
                                }
                            }
                            case INSERT -> {
                                String clip = clipboard().get();
                                int[] minMax;
                                if(cars.isAny()) {
                                    minMax = cars.getMinMax();
                                } else {
                                    minMax = new int[]{cursorPos, cursorPos};
                                }
                                String str = text.getString();
                                text.setString(str.substring(0, minMax[0]) +
                                        clip +
                                        str.substring(minMax[1]));
                                cursorPosition.set(cursorPos + clip.length());
                            }
                            case X -> {
                                if(e.isControlled()) {
                                    String cut = cut();
                                    if(!cut.isEmpty()) clipboard().set(cut);
                                }
                            }
                            case C -> {
                                if(e.isControlled()) {
                                    String copy = copy();
                                    if(!copy.isEmpty()) {
                                        clipboard().set(copy);
                                    }
                                }
                            }
                            case V -> {
                                if(e.isControlled()) {
                                    String clip = clipboard().get();
                                    if(!clip.isEmpty()) {
                                        paste(clip);
                                        cars.reset();
                                    }
                                }
                            }
                            case Z -> {
                                if(e.isControlled()) {
                                    Story story = story();
                                    if(e.isShifted()) {
                                        story.front();
                                    } else {
                                        story.back();
                                    }
                                }
                            }
                            case A -> {
                                if(e.isControlled()) {
                                    cursorPosition.set(text.getString().length());
                                    cars.headIndex.set(text.getString().length());
                                    cars.tailIndex.set(0);
                                }
                            }
                            case ESCAPE -> {
                                if(cars.isAny()) {
                                    cars.reset();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void select(int begin, int length) {
        if(begin >= 0 && length >= 0 && begin + length < text.getString().length()) {
            cars.tailIndex.set(begin);
            cars.headIndex.set(begin + length);
        }
    }

    public String copy() {
        if (cars.isAny()) {
            int[] minMax = cars.getMinMax();
            String str = text.getString();
            return str.substring(minMax[0], minMax[1]);
        }
        return "";
    }

    public String cut() {
        if(cars.isAny()) {
            int[] minMax = cars.getMinMax();
            String str = text.getString();
            String cut = str.substring(minMax[0], minMax[1]);

            UserAction ua = new UserAction() {

                @Override
                public void front() {
                    String str = text.getString();
                    text.setString(str.substring(0, minMax[0]) +
                            str.substring(minMax[1]));
                    cursorPosition.set(minMax[0]);
                    cars.reset();
                }

                @Override
                public void back() {
                    String str = text.getString();
                    text.setString(str.substring(0, minMax[0]) + cut +
                            str.substring(minMax[0]));
                    cursorPosition.set(minMax[0]);
                    cars.headIndex.set(minMax[0]);
                    cars.tailIndex.set(minMax[0] + cut.length());
                }
            };
            ua.front();
            story().push(ua);
            return cut;
        }
        return "";
    }

    public void paste(String pasted) {
        int[] minMax;
        if(cars.isAny()) {
            minMax = cars.getMinMax();
        } else {
            int cursorPos = cursorPosition.get();
            minMax = new int[]{cursorPos, cursorPos};
        }
        String str = text.getString();
        String replaced = str.substring(minMax[0], minMax[1]);

        UserAction ua = new UserAction() {

            @Override
            public void front() {
                String str = text.getString();
                text.setString(str.substring(0, minMax[0]) + pasted +
                        str.substring(minMax[1]));
                cursorPosition.set(minMax[0] + pasted.length());
                cars.headIndex.set(minMax[0] + pasted.length());
                cars.tailIndex.set(minMax[0]);
            }

            @Override
            public void back() {
                String str = text.getString();
                text.setString(str.substring(0, minMax[0]) + replaced +
                        str.substring(minMax[0] + pasted.length()));
                cursorPosition.set(minMax[0] + replaced.length());
                cars.reset();
            }
        };
        ua.front();
        story().push(ua);
    }


    private int ctrlJump(int cursorPos, boolean reverse) {

        Cascade<Integer> cps;
        if(reverse) {
            StringBuilder str = new StringBuilder(text.getString().substring(0, cursorPos));
            cps = new Cascade<>(str.reverse().codePoints().iterator());
        } else {
            cps = new Cascade<>(text.getString().substring(cursorPos)
                    .codePoints().iterator());
        }
        int jump = 1;
        boolean nonWhitespaceFound = false;
        for (var cp : cps) {
            if(Character.isWhitespace(cp)) {
                if(nonWhitespaceFound) break;
                else {
                    if(cps.hasNext()) {
                        ++jump;
                    }
                }
            } else {
                nonWhitespaceFound = true;
                if(cps.hasNext()) {
                    ++jump;
                }
            }
        }
        return jump;
    }

    void cursorLeft() {

    }

    void cursorRight() {

    }


    Var<Boolean> shown;
    protected void _show() {
        show(text);
    }

    protected void _hide() {
        hide(text);
    }

    @Override
    public void show() {
        shown.set(true);
    }

    @Override
    public void hide() {
        shown.set(false);
        selected.set(false);
    }

    public boolean isShown() {
        return shown.get();
    }

    public Var<Boolean> shown() {
        return shown;
    }


    Var<Boolean> selected;

    protected void _select() {
        use(cursor);
        use(cars, text);
    }

    protected void _unselect() {
        cancel(cursor);
        cancel(cars);
    }

    public void select() {
        selected.set(true);
    }

    public void unselect() {
        selected.set(false);
    }

    public Var<Boolean> selected() {
        return selected;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public Var<String> string() {
        return text.string();
    }

    public float getWidth() {
        return text.getWidth();
    }

    public Source<Number> width() {
        return text.width();
    }
}
