package airbricks.note;

import airbricks.Airbrick;
import airbricks.selection.KeyboardClient;
import airbricks.selection.KeyboardDealer;
import bricks.Color;
import bricks.Location;
import bricks.Located;
import bricks.Sized;
import bricks.font.BackedFont;
import bricks.font.FontManager;
import bricks.font.LoadedFont;
import bricks.slab.RectangleSlab;
import bricks.slab.TextSlab;
import bricks.slab.Shape;
import bricks.input.Keyboard;
import bricks.input.Mouse;
import bricks.input.Story;
import bricks.input.UserAction;
import bricks.trade.Host;
import bricks.var.Pull;
import bricks.var.Var;
import bricks.var.special.NumPull;
import bricks.var.special.NumSource;
import suite.suite.util.Cascade;

public class NoteBrick extends Airbrick<Host> implements KeyboardClient, Shape, Location {

    protected boolean editable;
    public HasKeyboard hasKeyboard;

    public final RectangleSlab background;
    public final Pull<Color> backgroundColorDefault;
    public final Pull<Color> backgroundColorSeeCursor;
    public final Pull<Color> backgroundColorPressed;

    public final RectangleSlab outline;
    public final Pull<Color> outlineColorDefault;
    public final Pull<Color> outlineColorSeeKeyboard;

    public final NumPull outlineThick;

    public final TextSlab text;
    public final RectangleSlab cursor;
    public final NoteCars cars;
    public final Pull<Integer> cursorPosition;

    public NoteBrick(Host host) {
        super(host);
        editable = true;
        hasKeyboard = HasKeyboard.NO;

        backgroundColorDefault = Var.pull(Color.hex("#292B2B"));
        backgroundColorSeeCursor = Var.pull(Color.hex("#212323"));
        backgroundColorPressed = Var.pull(Color.hex("#191B1B"));

        outlineColorDefault = Var.pull(Color.hex("#1e1a2c"));
        outlineColorSeeKeyboard = Var.pull(Color.mix(1, .8, .6));

        outlineThick = Var.num(4);

        outline = new RectangleSlab(this) {{
            color().let(() -> seeKeyboard() ?
                    outlineColorSeeKeyboard.get() :
                    outlineColorDefault.get());
        }};

        background = new RectangleSlab(this) {{
            color().let(() -> seeCursor() ?
                    backgroundColorSeeCursor.get() :
                    backgroundColorDefault.get());
            aim(outline);
            adjust(Sized.relative(outline, outlineThick.perFloat(t -> -t)));
        }};

        text = new TextSlab(this) {{
            height().set(20);
            color().set(Color.mix(1, 1, 1));
        }};

        cursorPosition = Var.pull(0);
        cars = new NoteCars(this);

        when(text()).then(() -> {
            int len = text().get().length();
            if(len < cursorPosition.get()) {
                cursorPosition.set(len);
            }
            NoteCars.MinMax mm = cars.getMinMax();
            if(len > mm.min()) {
                cars.headIndex.set(len);
                cars.tailIndex.set(len);
            } else if(len > mm.max()) {
                cars.tailIndex.set(mm.min());
                cars.headIndex.set(len);
            }
        });

        cursor = new RectangleSlab(this) {{
            width().set(1);
            height().let(text.height());
            color().let(text.color());
            x().let(() -> {
                int pos = cursorPosition.get();
                LoadedFont font = order(FontManager.class).getFont(text.font().get());
                float xOffset = font.getStringWidth(text.text().get().substring(0, pos), text.height().getFloat());
                float l = text.left().getFloat();
                return l + xOffset;
            }, cursorPosition, text.font(), text.text(), text.height(), text.x());
            y().let(() -> {
                BackedFont font = order(FontManager.class).getFont(text.font().get(), text.height().getFloat());
                float y = text.y().getFloat();
                return y + font.getScaledDescent() / 2;
            });
        }};

        $bricks.set(text);
    }

    public void updateCursorPosition(boolean resetCars) {
        float x = text.left().getFloat();
        int newCursorPos = order(FontManager.class).getFont(text.font().get())
                .getCursorPosition(text.text().get(), text.height().getFloat(),
                        x, (float) input().state.mouseCursorX());
        cursorPosition.set(newCursorPos);
        cars.headIndex.set(newCursorPos);
        if(resetCars) {
            cars.tailIndex.set(newCursorPos);
        }
    }

    @Override
    public void update() {

        var in = input();
        boolean mouseLeftButtonPress = false;
        for(var e : in.getEvents()) {
            if(e instanceof Mouse.ButtonEvent be) {
                if(be.button == Mouse.Button.Code.LEFT) {
                    if(be.isPress()) {
                        if(seeCursor()) {
                            mouseLeftButtonPress = true;
                        }
                    }
                }
            }
        }
        if(seeKeyboard()) {
            if(in.state.isPressed(Mouse.Button.Code.LEFT)) {
                updateCursorPosition(mouseLeftButtonPress);
            }

            if(editable) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (var che : in.getEvents().select(Keyboard.CharEvent.class)) {
                        stringBuilder.appendCodePoint(che.getCodepoint());
                    }
                    if(!stringBuilder.isEmpty()) {
                        String inset = stringBuilder.toString();
                        cutSelected();
                        charInset(inset);
                        String str = text.text().get();
                        int cursorPos = cursorPosition.get();
                        text.text().set(str.substring(0, cursorPos) + inset + str.substring(cursorPos));
                        cursorPosition.set(cursorPos + inset.length());
                    }
            }

            int cursorPos = cursorPosition.get();
            for (var e : in.getEvents().select(Keyboard.KeyEvent.class)) {
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
                                        cars.headIndex.set(text.text().get().length());
                                    } else {
                                        cars.reset();
                                    }
                                } else {
                                    if(e.isShifted()) {
                                        cars.tailIndex.set(cursorPos);
                                        cars.headIndex.set(text.text().get().length());
                                    }
                                }
                                cursorPosition.set(text.text().get().length());
                            }
                            case NUM_0_INSERT -> {
                                if(!editable) break;
                                String clip = clipboard().get();
                                NoteCars.MinMax minMax;
                                if(cars.isAny()) {
                                    minMax = cars.getMinMax();
                                } else {
                                    minMax = new NoteCars.MinMax(cursorPos, cursorPos);
                                }
                                String str = text.text().get();
                                text.text().set(str.substring(0, minMax.min()) +
                                        clip +
                                        str.substring(minMax.max()));
                                cursorPosition.set(cursorPos + clip.length());
                            }
                            case NUM_DECIMAL_DELETE -> {
                                if(!editable) break;
                                String cut = cutSelected();
                                if(cut.isEmpty()) {
                                    String str = text.text().get();
                                    if (cursorPos < str.length()) {
                                        charErase(false);
                                        text.text().set(str.substring(0, cursorPos) + str.substring(cursorPos + 1));
                                    }
                                }
                            }
                        }
                    }
                    switch (e.key) {
                        case BACKSPACE -> {
                            if(!editable) break;
                            String cut = cutSelected();
                            if(cut.isEmpty()) {
                                if (cursorPos > 0) {
                                    charErase(true);
                                    String str = text.text().get();
                                    text.text().set(str.substring(0, cursorPos - 1) + str.substring(cursorPos));
                                    cursorPosition.set(cursorPos - 1);
                                }
                            }
                            cars.reset();
                        }
                        case DELETE -> {
                            if(!editable) break;
                            String cut = cutSelected();
                            if(cut.isEmpty()) {
                                String str = text.text().get();
                                if (cursorPos < str.length()) {
                                    charErase(false);
                                    text.text().set(str.substring(0, cursorPos) + str.substring(cursorPos + 1));
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
                                        if (cursorPos < text.text().get().length()) {
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
                                    if (cursorPos < text.text().get().length()) {
                                        cursorPosition.set(cursorPos + 1);
                                        cars.headIndex.set(cursorPos + 1);
                                    }
                                }
                            } else {
                                if(e.isControlled()) {
                                    if (cursorPos < text.text().get().length()) {
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
                                        if (cursorPos < text.text().get().length()) {
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
                                    cars.headIndex.set(text.text().get().length());
                                } else {
                                    cars.reset();
                                }
                            } else {
                                if(e.isShifted()) {
                                    cars.tailIndex.set(cursorPos);
                                    cars.headIndex.set(text.text().get().length());
                                }
                            }
                            cursorPosition.set(text.text().get().length());
                        }
                        case INSERT -> {
                            if(!editable) break;
                            String clip = clipboard().get();
                            if(!clip.isEmpty()) {
                                paste(clip);
                                cars.reset();
                            }
                        }
                        case X -> {
                            if(!editable) break;
                            if(e.isControlled() && !e.isAltered()) {
                                String cut = cutSelected();
                                if(!cut.isEmpty()) clipboard().set(cut);
                            }
                        }
                        case C -> {
                            if(e.isControlled() && !e.isAltered()) {
                                String copy = getSelected();
                                if(!copy.isEmpty()) {
                                    clipboard().set(copy);
                                }
                            }
                        }
                        case V -> {
                            if(!editable) break;
                            if(e.isControlled() && !e.isAltered()) {
                                String clip = clipboard().get();
                                if(!clip.isEmpty()) {
                                    paste(clip);
                                    cars.reset();
                                }
                            }
                        }
                        case B -> {
                            if(!editable) break;
                            if(e.isControlled() && !e.isAltered()) {
                                String cut = cutSelected();
                                String clip = clipboard().get();
                                if(!clip.isEmpty()) {
                                    paste(clip);
                                    cars.reset();
                                }
                                if(!cut.isEmpty()) clipboard().set(cut);
                            }
                        }
                        case Z -> {
                            if(!editable) break;
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
                                cursorPosition.set(text.text().get().length());
                                cars.headIndex.set(text.text().get().length());
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

        super.update();
    }

    public void select(int begin, int length) {
        if(begin >= 0 && length >= 0 && begin + length <= text.text().get().length()) {
            cars.tailIndex.set(begin);
            cars.headIndex.set(begin + length);
        }
    }

    public String getSelected() {
        if (cars.isAny()) {
            var minMax = cars.getMinMax();
            String str = text.text().get();
            return str.substring(minMax.min(), minMax.max());
        }
        return "";
    }

    public String cutSelected() {
        if(cars.isAny()) {
            var minMax = cars.getMinMax();
            String str = text.text().get();
            String cut = str.substring(minMax.min(), minMax.max());
            int cursorBegin = minMax.min();

            UserAction ua = new UserAction() {

                @Override
                public void front() {
                    String str = text.text().get();
                    text.text().set(str.substring(0, cursorBegin) +
                            str.substring(cursorBegin + cut.length()));
                    cursorPosition.set(cursorBegin);
                    cars.reset();
                }

                @Override
                public void back() {
                    String str = text.text().get();
                    text.text().set(str.substring(0, cursorBegin) + cut +
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
        NoteCars.MinMax minMax;
        if(cars.isAny()) {
            minMax = cars.getMinMax();
        } else {
            int cursorPos = cursorPosition.get();
            minMax = new NoteCars.MinMax(cursorPos, cursorPos);
        }
        String str = text.text().get();
        String replaced = str.substring(minMax.min(), minMax.max());

        UserAction ua = new UserAction() {

            @Override
            public void front() {
                String str = text.text().get();
                text.text().set(str.substring(0, minMax.min()) + pasted +
                        str.substring(minMax.max()));
                cursorPosition.set(minMax.min() + pasted.length());
                cars.headIndex.set(minMax.min() + pasted.length());
                cars.tailIndex.set(minMax.min());
            }

            @Override
            public void back() {
                String str = text.text().get();
                text.text().set(str.substring(0, minMax.min()) + replaced +
                        str.substring(minMax.min() + pasted.length()));
                cursorPosition.set(minMax.min() + replaced.length());
                cars.reset();
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
            String str = text.text().get();
            text.text().set(str.substring(0, cursorBegin) + inset + str.substring(cursorBegin));
            cursorPosition.set(cursorBegin + inset.length());
            cars.reset();
        }

        @Override
        public void back() {
            String str = text.text().get();
            text.text().set(str.substring(0, cursorBegin) + str.substring(cursorBegin + inset.length()));
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
            String str = text.text().get();
            text.text().set(str.substring(0, cursorBegin) + str.substring(cursorBegin + outset.length()));
            cursorPosition.set(cursorBegin);
            cars.reset();
        }

        @Override
        public void back() {
            String str = text.text().get();
            text.text().set(str.substring(0, cursorBegin) + outset + str.substring(cursorBegin));
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
                charErase.outset = text.text().get().substring(cursorPos - 1, cursorPos);
            } else {
                charErase.cursorBegin = cursorPos;
                charErase.outset = text.text().get().substring(cursorPos, cursorPos + 1);
            }
        } else {
            if(charErase.cursorBegin != cursorPos) {
                story().push(charErase);
                if(backspace) {
                    charErase = new CharEraseUserAction(cursorPos - 1, text.text().get().substring(cursorPos - 1, cursorPos));
                } else {
                    charErase = new CharEraseUserAction(cursorPos, text.text().get().substring(cursorPos, cursorPos + 1));
                }
            } else {
                if(backspace) {
                    charErase.outset = text.text().get().charAt(cursorPos - 1) + charErase.outset;
                    --charErase.cursorBegin;
                } else {
                    charErase.outset += text.text().get().charAt(cursorPos);
                }
            }
        }
    }

    private int ctrlJump(int cursorPos, boolean reverse) {

        Cascade<Integer> cps;
        if(reverse) {
            StringBuilder str = new StringBuilder(text.text().get().substring(0, cursorPos));
            cps = new Cascade<>(str.reverse().codePoints().iterator());
        } else {
            cps = new Cascade<>(text.text().get().substring(cursorPos)
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

    public Pull<Integer> cursorPosition() {
        return cursorPosition;
    }

    public Pull<String> text() {
        return text.text();
    }

    public void editable(boolean should) {
        editable = should;
    }

    public boolean isEditable() {
        return editable;
    }

    public NumPull height() {
        return text.height();
    }
    public NumSource width() {
        return text.width();
    }

    public NumPull left() {
        return text.left();
    }

    public NumPull right() {
        return text.right();
    }

    public NumPull top() {
        return text.top();
    }

    public NumPull bottom() {
        return text.bottom();
    }

    public NumPull x() {
        return text.x();
    }

    public NumPull y() {
        return text.y();
    }

    public void aim(Located located) {
        text.aim(located);
    }

    @Override
    public HasKeyboard hasKeyboard() {
        return hasKeyboard;
    }

    @Override
    public void depriveKeyboard() {
        hasKeyboard = HasKeyboard.NO;
    }

    @Override
    public void requestKeyboard() {
        var keyboardDealer = order(KeyboardDealer.class);
        hasKeyboard = keyboardDealer.requestKeyboard(this);
    }

    public void showCursor() {
        $bricks.aimedSet(text, cars);
        $bricks.set(cursor);
    }

    public void hideCursor() {
        $bricks.unset(cars, cursor);
    }
}
