package airbricks.model;

import bricks.Color;
import bricks.Point;
import bricks.XOrigin;
import bricks.YOrigin;
import bricks.font.BackedFont;
import bricks.font.FontManager;
import bricks.graphic.ColorRectangle;
import bricks.graphic.ColorText;
import bricks.graphic.Rectangular;
import bricks.input.Mouse;
import bricks.input.Story;
import bricks.input.UserAction;
import bricks.trade.Host;
import bricks.var.Source;
import bricks.var.Var;
import bricks.var.Vars;
import bricks.var.impulse.Impulse;
import suite.suite.util.Cascade;

public class Note extends Airbrick<Host> implements Rectangular {

    final ColorText text;
    final ColorRectangle cursor;
    final NoteCars cars;
    Var<Integer> cursorPosition;
    Var<Boolean> editable;

    Impulse mousePress;

    public Note(Host host) {
        super(host);

        selected = new AutoState<>(Vars.set(false), Vars.set(false));
        when(selected.formal, this::select, this::unselect);
        shown = new AutoState<>(Vars.set(false), Vars.set(false));
        when(shown.formal, this::show, this::hide);

        mousePress = mouse().leftButton().willBe(Mouse.Button::pressed);

        text = text().setHeight(20).setColor(Color.mix(1,1,1))
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
               case LEFT -> new Point(textPosition.x() + xOffset,
                       textPosition.y() + font.getScaledDescent() / 2);
               case CENTER -> new Point(textPosition.x() - text.getWidth() / 2 + xOffset,
                       textPosition.y() + font.getScaledDescent() / 2);
               case RIGHT -> new Point(textPosition.x() - text.getWidth() + xOffset,
                       textPosition.y() + font.getScaledDescent() / 2);
           };
        }, text.font(), text.width(), text.string(), text.height(), text.position(), text.xOrigin(), cursorPosition);

        cars = new NoteCars(this);
        editable = Vars.set(true);
    }

    @Override
    public void move() {

    }

    @Override
    public void update() {
        super.update();

        if(isSelected()) {
            boolean pressOccur = mousePress.occur();
            var mouse = mouse();
            if(mouse.leftButton().isPressed()) {
                float x = switch (text.getXOrigin()) {
                    case CENTER -> text.getPosition().x() - text.getWidth() / 2;
                    case LEFT -> text.getPosition().x();
                    case RIGHT -> text.getPosition().x() - text.getWidth();
                };
                int newCursorPos = order(FontManager.class).getFont(text.getFont())
                        .getCursorPosition(text.getString(), text.getHeight(), x, mouse().position().get().x());
                cursorPosition.set(newCursorPos);
                cars.headIndex.set(newCursorPos);
                if(pressOccur) {
                    cars.tailIndex.set(newCursorPos);
                }
            }

            var keyboard = keyboard();
            if(editable.get()) {
                var charEvents = keyboard.getCharEvents();
                if (charEvents.size() > 0) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (var che : keyboard.getCharEvents()) {
                        stringBuilder.appendCodePoint(che.getCodepoint());
                    }
                    String inset = stringBuilder.toString();
                    cut();
                    charInset(inset);
                    String str = text.getString();
                    int cursorPos = cursorPosition.get();
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
                                    if(!editable.get()) break;
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
                                    if(!editable.get()) break;
                                    String cut = cut();
                                    if(cut.isEmpty()) {
                                        String str = text.getString();
                                        if (cursorPos < str.length()) {
                                            charErase(false);
                                            text.setString(str.substring(0, cursorPos) + str.substring(cursorPos + 1));
                                        }
                                    }
                                }
                            }
                        }
                        switch (e.key) {
                            case BACKSPACE -> {
                                if(!editable.get()) break;
                                String cut = cut();
                                if(cut.isEmpty()) {
                                    if (cursorPos > 0) {
                                        charErase(true);
                                        String str = text.getString();
                                        text.setString(str.substring(0, cursorPos - 1) + str.substring(cursorPos));
                                        cursorPosition.set(cursorPos - 1);
                                    }
                                }
                                cars.reset();
                            }
                            case DELETE -> {
                                if(!editable.get()) break;
                                String cut = cut();
                                if(cut.isEmpty()) {
                                    String str = text.getString();
                                    if (cursorPos < str.length()) {
                                        charErase(false);
                                        text.setString(str.substring(0, cursorPos) + str.substring(cursorPos + 1));
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
                                if(!editable.get()) break;
                                if(cars.isAny()) {
                                    caps(e.isCapsLocked());
                                }
                            }
                            case INSERT -> {
                                if(!editable.get()) break;
                                String clip = clipboard().get();
                                if(!clip.isEmpty()) {
                                    paste(clip);
                                    cars.reset();
                                }
                            }
                            case X -> {
                                if(!editable.get()) break;
                                if(e.isControlled() && !e.isAltered()) {
                                    String cut = cut();
                                    if(!cut.isEmpty()) clipboard().set(cut);
                                }
                            }
                            case C -> {
                                if(e.isControlled() && !e.isAltered()) {
                                    String copy = copy();
                                    if(!copy.isEmpty()) {
                                        clipboard().set(copy);
                                    }
                                }
                            }
                            case V -> {
                                if(!editable.get()) break;
                                if(e.isControlled() && !e.isAltered()) {
                                    String clip = clipboard().get();
                                    if(!clip.isEmpty()) {
                                        paste(clip);
                                        cars.reset();
                                    }
                                }
                            }
                            case Z -> {
                                if(!editable.get()) break;
                                if(e.isControlled() && !e.isAltered()) {
                                    Story story = story();
                                    pushOpenUserAction();
                                    if(e.isShifted()) {
                                        story.front();
                                    } else {
                                        story.back();
                                    }
                                }
                            }
                            case A -> {
                                if(e.isControlled() && !e.isAltered()) {
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

    @Override
    public void stop() {

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
            int cursorBegin = minMax[0];

            UserAction ua = new UserAction() {

                @Override
                public void front() {
                    String str = text.getString();
                    text.setString(str.substring(0, cursorBegin) +
                            str.substring(cursorBegin + cut.length()));
                    cursorPosition.set(cursorBegin);
                    cars.reset();
                }

                @Override
                public void back() {
                    String str = text.getString();
                    text.setString(str.substring(0, cursorBegin) + cut +
                            str.substring(cursorBegin));
                    cursorPosition.set(cursorBegin);
                    cars.headIndex.set(cursorBegin);
                    cars.tailIndex.set(cursorBegin + cut.length());
                }
            };
            pushOpenUserAction();
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
        pushOpenUserAction();
        ua.front();
        story().push(ua);
    }

    public void caps(boolean upper) {
        int[] minMax = cars.getMinMax();
        String str = text.getString();
        String capsed = str.substring(minMax[0], minMax[1]);
        int cursorBegin = minMax[0];

        UserAction ua = new UserAction() {

            @Override
            public void front() {
                String str = text.getString();
                int cursorEnd = cursorBegin + capsed.length();
                text.setString(str.substring(0, cursorBegin) +
                        (upper ? capsed.toUpperCase() : capsed.toLowerCase()) +
                        str.substring(cursorEnd));
                cursorPosition.set(cursorEnd);
                cars.headIndex.set(cursorEnd);
                cars.tailIndex.set(cursorBegin);
            }

            @Override
            public void back() {
                String str = text.getString();
                int cursorEnd = cursorBegin + capsed.length();
                text.setString(str.substring(0, cursorBegin) +
                        capsed +
                        str.substring(cursorEnd));
                cursorPosition.set(cursorEnd);
                cars.headIndex.set(cursorEnd);
                cars.tailIndex.set(cursorBegin);
            }
        };
        pushOpenUserAction();
        ua.front();
        story().push(ua);
    }

    class CharInsetUserAction implements UserAction {
        int cursorBegin;
        String inset;

        public CharInsetUserAction() {
            this(0, "");
        }

        public CharInsetUserAction(int cursorBegin, String inset) {
            this.cursorBegin = cursorBegin;
            this.inset = inset;
        }

        @Override
        public void front() {
            String str = text.getString();
            text.setString(str.substring(0, cursorBegin) + inset + str.substring(cursorBegin));
            cursorPosition.set(cursorBegin + inset.length());
            cars.reset();
        }

        @Override
        public void back() {
            String str = text.getString();
            text.setString(str.substring(0, cursorBegin) + str.substring(cursorBegin + inset.length()));
            cursorPosition.set(cursorBegin);
            cars.reset();
        }
    }
    private CharInsetUserAction charInset = new CharInsetUserAction();

    private void charInset(String inset) {
        int cursorPos = cursorPosition.get();
        if(charInset.inset.isEmpty()) {
            pushOpenUserAction();
            charInset.cursorBegin = cursorPos;
            charInset.inset = inset;
        } else {
            if(charInset.cursorBegin + charInset.inset.length() != cursorPos) {
                story().push(charInset);
                charInset = new CharInsetUserAction(cursorPos, inset);
            } else {
                charInset.inset += inset;
            }
        }
    }

    class CharEraseUserAction implements UserAction {
        int cursorBegin;
        String outset;

        public CharEraseUserAction() {
            this(0, "");
        }

        public CharEraseUserAction(int cursorBegin, String outset) {
            this.cursorBegin = cursorBegin;
            this.outset = outset;
        }

        @Override
        public void front() {
            String str = text.getString();
            text.setString(str.substring(0, cursorBegin) + str.substring(cursorBegin + outset.length()));
            cursorPosition.set(cursorBegin);
            cars.reset();
        }

        @Override
        public void back() {
            String str = text.getString();
            text.setString(str.substring(0, cursorBegin) + outset + str.substring(cursorBegin));
            cursorPosition.set(cursorBegin + outset.length());
            cars.reset();

        }
    }
    private CharEraseUserAction charErase = new CharEraseUserAction();

    private void charErase(boolean backspace) {
        int cursorPos = cursorPosition.get();
        if(charErase.outset.isEmpty()) {
            pushOpenUserAction();
            if(backspace) {
                charErase.cursorBegin = cursorPos - 1;
                charErase.outset = text.getString().substring(cursorPos - 1, cursorPos);
            } else {
                charErase.cursorBegin = cursorPos;
                charErase.outset = text.getString().substring(cursorPos, cursorPos + 1);
            }
        } else {
            if(charErase.cursorBegin != cursorPos) {
                story().push(charErase);
                if(backspace) {
                    charErase = new CharEraseUserAction(cursorPos - 1, text.getString().substring(cursorPos - 1, cursorPos));
                } else {
                    charErase = new CharEraseUserAction(cursorPos, text.getString().substring(cursorPos, cursorPos + 1));
                }
            } else {
                if(backspace) {
                    charErase.outset = text.getString().charAt(cursorPos - 1) + charErase.outset;
                    --charErase.cursorBegin;
                } else {
                    charErase.outset += text.getString().charAt(cursorPos);
                }
            }
        }
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
        boolean acceptWhitespaces = Character.isWhitespace(cps.next());
        for (var cp : cps) {
            if(Character.isWhitespace(cp)) {
                if(acceptWhitespaces) {
                    ++jump;
                } else break;
            } else {
                ++jump;
                acceptWhitespaces = false;
            }
        }
        return jump;
    }

    void pushOpenUserAction() {
        if(!charInset.inset.isEmpty()) {
            story().push(charInset);
            charInset = new CharInsetUserAction();
        }
        if(!charErase.outset.isEmpty()) {
            story().push(charErase);
            charErase = new CharEraseUserAction();
        }
    }

    AutoState<Boolean> shown;

    @Override
    public void show() {
        if(!shown.inner.get()) {
            show(text);
            shown.inner.set(true);
        }
    }

    @Override
    public void hide() {
        if (shown.inner.get()) {
            hide(text);
            selected.set(false);
            shown.inner.set(false);
        }
    }

    public boolean isShown() {
        return shown.get();
    }

    public Var<Boolean> shown() {
        return shown;
    }


    AutoState<Boolean> selected;

    public void select() {
        if(!selected.inner.get()) {
            use(cursor);
            use(cars, text);
            selected.inner.set(true);
        }
    }

    public void unselect() {
        if(selected.inner.get()) {
            cancel(cursor);
            cancel(cars);
            selected.inner.set(false);
        }
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

    @Override
    public Var<Point> position() {
        return text.position();
    }

    @Override
    public Var<XOrigin> xOrigin() {
        return text.xOrigin();
    }

    @Override
    public Var<YOrigin> yOrigin() {
        return text.yOrigin();
    }

    @Override
    public Var<Number> height() {
        return text.height();
    }

    public Var<Boolean> editable() {
        return editable;
    }

    public void fill(Rectangular rect) {
        xOrigin().let(rect.xOrigin());
        yOrigin().let(rect.yOrigin());
        position().let(rect.position());
    }
}
