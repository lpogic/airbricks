package airbricks.model;

import bricks.Color;
import bricks.Coordinated;
import bricks.font.BackedFont;
import bricks.font.FontManager;
import bricks.font.LoadedFont;
import bricks.graphic.ColorRectangle;
import bricks.graphic.ColorText;
import bricks.graphic.Rectangular;
import bricks.input.Mouse;
import bricks.input.Story;
import bricks.input.UserAction;
import bricks.trade.Host;
import bricks.var.Var;
import bricks.var.Vars;
import bricks.var.impulse.Impulse;
import bricks.var.impulse.State;
import bricks.var.special.Num;
import bricks.var.special.NumSource;
import suite.suite.util.Cascade;

public class Note extends Airbrick<Host> implements Rectangular {

    final ColorText text;
    final ColorRectangle cursor;
    final NoteCars cars;
    final Var<Integer> cursorPosition;
    final Var<Boolean> editable;

    final Impulse mousePress;

    public Note(Host host) {
        super(host);

        selected = new State<>(false);
        when(selected.signal(), () -> {
            if (selected.getInput()) select();
            else unselect();
        });
        shown = new State<>(false);
        when(shown.signal(), () -> {
            if(shown.getInput()) show();
            else hide();
        });

        mousePress = mouse().leftButton().willBe(Mouse.Button::pressed);

        text = text();
        text.height().set(20);
        text.color().set(Color.mix(1, 1, 1));

        cursorPosition = Vars.set(0);

        cursor = rect();
        cursor.width().set(1);
        cursor.height().let(text.height());
        cursor.color().let(text.color());
        cursor.x().let(() -> {
            int pos = cursorPosition.get();
            LoadedFont font = order(FontManager.class).getFont(text.font().get());
            float xOffset = font.getStringWidth(text.string().get().substring(0, pos), text.height().getFloat());
            float l = text.left().getFloat();
            return l + xOffset;
        });
        cursor.y().let(() -> {
            BackedFont font = order(FontManager.class).getFont(text.font().get(), text.height().getFloat());
            float y = text.y().getFloat();
            return y + font.getScaledDescent() / 2;
        });

        cars = new NoteCars(this);
        editable = Vars.set(true);
    }

    @Override
    public void move() {

    }

    @Override
    public void update() {
        super.update();

        if(selected.get()) {
            boolean pressOccur = mousePress.occur();
            var mouse = mouse();
            if(mouse.leftButton().isPressed()) {
                float x = text.left().getFloat();
                int newCursorPos = order(FontManager.class).getFont(text.font().get())
                        .getCursorPosition(text.string().get(), text.height().getFloat(),
                                x, mouse.position().x().getFloat());
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
                    String str = text.string().get();
                    int cursorPos = cursorPosition.get();
                    text.string().set(str.substring(0, cursorPos) + inset + str.substring(cursorPos));
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
                                            cars.headIndex.set(text.string().get().length());
                                        } else {
                                            cars.reset();
                                        }
                                    } else {
                                        if(e.isShifted()) {
                                            cars.tailIndex.set(cursorPos);
                                            cars.headIndex.set(text.string().get().length());
                                        }
                                    }
                                    cursorPosition.set(text.string().get().length());
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
                                    String str = text.string().get();
                                    text.string().set(str.substring(0, minMax[0]) +
                                            clip +
                                            str.substring(minMax[1]));
                                    cursorPosition.set(cursorPos + clip.length());
                                }
                                case NUM_DECIMAL_DELETE -> {
                                    if(!editable.get()) break;
                                    String cut = cut();
                                    if(cut.isEmpty()) {
                                        String str = text.string().get();
                                        if (cursorPos < str.length()) {
                                            charErase(false);
                                            text.string().set(str.substring(0, cursorPos) + str.substring(cursorPos + 1));
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
                                        String str = text.string().get();
                                        text.string().set(str.substring(0, cursorPos - 1) + str.substring(cursorPos));
                                        cursorPosition.set(cursorPos - 1);
                                    }
                                }
                                cars.reset();
                            }
                            case DELETE -> {
                                if(!editable.get()) break;
                                String cut = cut();
                                if(cut.isEmpty()) {
                                    String str = text.string().get();
                                    if (cursorPos < str.length()) {
                                        charErase(false);
                                        text.string().set(str.substring(0, cursorPos) + str.substring(cursorPos + 1));
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
                                            if (cursorPos < text.string().get().length()) {
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
                                        if (cursorPos < text.string().get().length()) {
                                            cursorPosition.set(cursorPos + 1);
                                            cars.headIndex.set(cursorPos + 1);
                                        }
                                    }
                                } else {
                                    if(e.isControlled()) {
                                        if (cursorPos < text.string().get().length()) {
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
                                            if (cursorPos < text.string().get().length()) {
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
                                        cars.headIndex.set(text.string().get().length());
                                    } else {
                                        cars.reset();
                                    }
                                } else {
                                    if(e.isShifted()) {
                                        cars.tailIndex.set(cursorPos);
                                        cars.headIndex.set(text.string().get().length());
                                    }
                                }
                                cursorPosition.set(text.string().get().length());
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
                                    cursorPosition.set(text.string().get().length());
                                    cars.headIndex.set(text.string().get().length());
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
        if(begin >= 0 && length >= 0 && begin + length < text.string().get().length()) {
            cars.tailIndex.set(begin);
            cars.headIndex.set(begin + length);
        }
    }

    public String copy() {
        if (cars.isAny()) {
            int[] minMax = cars.getMinMax();
            String str = text.string().get();
            return str.substring(minMax[0], minMax[1]);
        }
        return "";
    }

    public String cut() {
        if(cars.isAny()) {
            int[] minMax = cars.getMinMax();
            String str = text.string().get();
            String cut = str.substring(minMax[0], minMax[1]);
            int cursorBegin = minMax[0];

            UserAction ua = new UserAction() {

                @Override
                public void front() {
                    String str = text.string().get();
                    text.string().set(str.substring(0, cursorBegin) +
                            str.substring(cursorBegin + cut.length()));
                    cursorPosition.set(cursorBegin);
                    cars.reset();
                }

                @Override
                public void back() {
                    String str = text.string().get();
                    text.string().set(str.substring(0, cursorBegin) + cut +
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
        String str = text.string().get();
        String replaced = str.substring(minMax[0], minMax[1]);

        UserAction ua = new UserAction() {

            @Override
            public void front() {
                String str = text.string().get();
                text.string().set(str.substring(0, minMax[0]) + pasted +
                        str.substring(minMax[1]));
                cursorPosition.set(minMax[0] + pasted.length());
                cars.headIndex.set(minMax[0] + pasted.length());
                cars.tailIndex.set(minMax[0]);
            }

            @Override
            public void back() {
                String str = text.string().get();
                text.string().set(str.substring(0, minMax[0]) + replaced +
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
        String str = text.string().get();
        String capsed = str.substring(minMax[0], minMax[1]);
        int cursorBegin = minMax[0];

        UserAction ua = new UserAction() {

            @Override
            public void front() {
                String str = text.string().get();
                int cursorEnd = cursorBegin + capsed.length();
                text.string().set(str.substring(0, cursorBegin) +
                        (upper ? capsed.toUpperCase() : capsed.toLowerCase()) +
                        str.substring(cursorEnd));
                cursorPosition.set(cursorEnd);
                cars.headIndex.set(cursorEnd);
                cars.tailIndex.set(cursorBegin);
            }

            @Override
            public void back() {
                String str = text.string().get();
                int cursorEnd = cursorBegin + capsed.length();
                text.string().set(str.substring(0, cursorBegin) +
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
            String str = text.string().get();
            text.string().set(str.substring(0, cursorBegin) + inset + str.substring(cursorBegin));
            cursorPosition.set(cursorBegin + inset.length());
            cars.reset();
        }

        @Override
        public void back() {
            String str = text.string().get();
            text.string().set(str.substring(0, cursorBegin) + str.substring(cursorBegin + inset.length()));
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
            String str = text.string().get();
            text.string().set(str.substring(0, cursorBegin) + str.substring(cursorBegin + outset.length()));
            cursorPosition.set(cursorBegin);
            cars.reset();
        }

        @Override
        public void back() {
            String str = text.string().get();
            text.string().set(str.substring(0, cursorBegin) + outset + str.substring(cursorBegin));
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
                charErase.outset = text.string().get().substring(cursorPos - 1, cursorPos);
            } else {
                charErase.cursorBegin = cursorPos;
                charErase.outset = text.string().get().substring(cursorPos, cursorPos + 1);
            }
        } else {
            if(charErase.cursorBegin != cursorPos) {
                story().push(charErase);
                if(backspace) {
                    charErase = new CharEraseUserAction(cursorPos - 1, text.string().get().substring(cursorPos - 1, cursorPos));
                } else {
                    charErase = new CharEraseUserAction(cursorPos, text.string().get().substring(cursorPos, cursorPos + 1));
                }
            } else {
                if(backspace) {
                    charErase.outset = text.string().get().charAt(cursorPos - 1) + charErase.outset;
                    --charErase.cursorBegin;
                } else {
                    charErase.outset += text.string().get().charAt(cursorPos);
                }
            }
        }
    }

    private int ctrlJump(int cursorPos, boolean reverse) {

        Cascade<Integer> cps;
        if(reverse) {
            StringBuilder str = new StringBuilder(text.string().get().substring(0, cursorPos));
            cps = new Cascade<>(str.reverse().codePoints().iterator());
        } else {
            cps = new Cascade<>(text.string().get().substring(cursorPos)
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

    State<Boolean> shown;

    @Override
    public void show() {
        if(!shown.get()) {
            show(text);
            shown.setState(true);
        }
    }

    @Override
    public void hide() {
        if (shown.get()) {
            hide(text);
            selected.set(false);
            shown.setState(false);
        }
    }

    public Var<Boolean> shown() {
        return shown;
    }

    State<Boolean> selected;

    public void select() {
        if(!selected.get()) {
            use(cursor);
            use(cars, text);
            selected.setState(true);
        }
    }

    public void unselect() {
        if(selected.getState()) {
            cancel(cursor);
            cancel(cars);
            selected.setState(false);
        }
    }

    public Var<Boolean> selected() {
        return selected;
    }

    public Var<String> string() {
        return text.string();
    }

    public Var<Boolean> editable() {
        return editable;
    }

    public Num height() {
        return text.height();
    }
    public NumSource width() {
        return text.width();
    }

    public Num left() {
        return text.left();
    }

    public Num right() {
        return text.right();
    }

    public Num top() {
        return text.top();
    }

    public Num bottom() {
        return text.bottom();
    }

    public Num x() {
        return text.x();
    }

    public Num y() {
        return text.y();
    }

    public void aim(Coordinated coordinated) {
        text.aim(coordinated);
    }
}
