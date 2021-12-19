package airbricks.text;

import airbricks.Airbrick;
import airbricks.keyboard.KeyboardClient;
import airbricks.keyboard.KeyboardDealer;
import bricks.Color;
import bricks.Location;
import bricks.Located;
import bricks.font.BackedFont;
import bricks.font.FontManager;
import bricks.font.LoadedFont;
import bricks.input.mouse.MouseButton;
import bricks.slab.RectangleSlab;
import bricks.slab.Shape;
import bricks.input.keyboard.Keyboard;
import bricks.input.mouse.Mouse;
import bricks.input.Story;
import bricks.input.UserAction;
import bricks.trade.Host;
import bricks.var.OverriddenPush;
import bricks.var.Pull;
import bricks.var.Push;
import bricks.var.Var;
import bricks.var.num.NumPull;
import bricks.var.num.NumSource;
import suite.suite.util.Cascade;

public class TextBrick extends Airbrick<Host> implements KeyboardClient, Shape, Location {

    protected boolean editable;
    public HasKeyboard hasKeyboard;

    public final Push<String> text;
    public final SelectableTextBrick stb;
    public final RectangleSlab cursor;
    public final Pull<Integer> cursorPosition;

    public TextBrick(Host host) {
        super(host);
        editable = true;
        hasKeyboard = HasKeyboard.NO;

        text = Var.push("");
        stb = new SelectableTextBrick(this) {{
            height().set(20);
            color().set(Color.mix(1, 1, 1));
            text().let(text);
        }};

        cursorPosition = Var.pull(0);

        text.act(() -> {
            int len = text.get().length();
            if(len < cursorPosition.get()) {
                cursorPosition.set(len);
            }
        });

        cursor = new RectangleSlab(this) {{
            width().set(1);
            height().let(stb.height());
            color().let(stb.color());
            x().let(() -> {
                int pos = cursorPosition.get();
                LoadedFont font = order(FontManager.class).getFont(stb.textSlab.font().get());
                float xOffset = font.getStringWidth(text.get().substring(0, pos), stb.height().getFloat(), stb.textSlab.isHideEol());
                float l = stb.left().getFloat();
                return l + xOffset;
            }, cursorPosition, stb.textSlab.font(), text, stb.height(), stb.x());
            y().let(() -> {
                BackedFont font = order(FontManager.class).getFont(stb.textSlab.font().get(), stb.height().getFloat());
                float y = stb.y().getFloat();
                return y + font.getScaledDescent() / 2;
            });
        }};

        $bricks.set(stb);
    }

    public void updateCursorPosition(boolean resetCars) {
        float x = stb.left().getFloat();
        int newCursorPos = order(FontManager.class).getFont(stb.textSlab.font().get())
                .getCursorPosition(text.get(), stb.height().getFloat(),
                        x, (float) input().state.mouseCursorX(), stb.textSlab.isHideEol());
        cursorPosition.set(newCursorPos);
        stb.setSelectionHead(newCursorPos);
        if(resetCars) {
            stb.setSelectionTail(newCursorPos);
        }
    }

    @Override
    public void update() {

        var in = input();
        boolean mouseLeftButtonPress = false;
        for(var e : in.getEvents()) {
            if(e instanceof Mouse.ButtonEvent be) {
                if(be.button == MouseButton.Code.LEFT) {
                    if(be.isPress()) {
                        if(seeCursor()) {
                            mouseLeftButtonPress = true;
                        }
                    }
                }
            }
        }
        if(seeKeyboard()) {
            if(in.state.isPressed(MouseButton.Code.LEFT)) {
                if(seeCursor()) {
                    updateCursorPosition(mouseLeftButtonPress);
                }
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
                        String str = text.get();
                        int cursorPos = cursorPosition.get();
                        text.set(str.substring(0, cursorPos) + inset + str.substring(cursorPos));
                        cursorPosition.set(cursorPos + inset.length());
                    }
            }

            int cursorPos = cursorPosition.get();
            for (var e : in.getEvents().select(Keyboard.KeyEvent.class)) {
                if (e.isHold()) {
                    if (e.key.isNumPad() && !e.isNumLocked()) {
                        switch (e.key) {
                            case NUM_7_HOME -> {
                                if(stb.anySelected()) {
                                    if(e.isShifted()) {
                                        stb.setSelectionHead(0);
                                    } else {
                                        stb.resetSelection();
                                    }
                                } else {
                                    if(e.isShifted()) {
                                        stb.setSelection(0, cursorPos);
                                    }
                                }
                                cursorPosition.set(0);
                            }
                            case NUM_1_END -> {
                                if(stb.anySelected()) {
                                    if(e.isShifted()) {
                                        stb.setSelectionHead(text.get().length());
                                    } else {
                                        stb.resetSelection();
                                    }
                                } else {
                                    if(e.isShifted()) {
                                        stb.setSelectionTail(cursorPos);
                                        stb.setSelectionHead(text.get().length());
                                    }
                                }
                                cursorPosition.set(text.get().length());
                            }
                            case NUM_0_INSERT -> {
                                if(!editable) break;
                                String clip = clipboard().get();
                                int min, max;
                                if(stb.anySelected()) {
                                    min = stb.getSelectionMin();
                                    max = stb.getSelectionMax();
                                } else {
                                    min = max = cursorPos;
                                }
                                String str = text.get();
                                text.set(str.substring(0, min) +
                                        clip +
                                        str.substring(max));
                                cursorPosition.set(cursorPos + clip.length());
                            }
                            case NUM_DECIMAL_DELETE -> {
                                if(!editable) break;
                                String cut = cutSelected();
                                if(cut.isEmpty()) {
                                    String str = text.get();
                                    if (cursorPos < str.length()) {
                                        charErase(false);
                                        text.set(str.substring(0, cursorPos) + str.substring(cursorPos + 1));
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
                                    String str = text.get();
                                    text.set(str.substring(0, cursorPos - 1) + str.substring(cursorPos));
                                    cursorPosition.set(cursorPos - 1);
                                }
                            }
                            stb.resetSelection();
                        }
                        case DELETE -> {
                            if(!editable) break;
                            String cut = cutSelected();
                            if(cut.isEmpty()) {
                                String str = text.get();
                                if (cursorPos < str.length()) {
                                    charErase(false);
                                    text.set(str.substring(0, cursorPos) + str.substring(cursorPos + 1));
                                }
                            }
                        }
                        case LEFT -> {
                            int head = stb.selectionHead.get();
                            int tail = stb.selectionTail.get();
                            if(e.isShifted()) {
                                if(e.isControlled()) {
                                    if(e.isAltered()) {
                                        if(tail < head) {
                                            stb.setSelectionTail(head);
                                            stb.setSelectionHead(tail);
                                            cursorPosition.set(tail);
                                        }
                                    } else {
                                        if (cursorPos > 0) {
                                            int jump = ctrlJump(cursorPos, true);
                                            if(!stb.anySelected()) {
                                                stb.setSelectionTail(cursorPos);
                                            }
                                            stb.setSelectionHead(cursorPos - jump);
                                            cursorPosition.set(cursorPos - jump);
                                        }
                                    }
                                } else {
                                    if (!stb.anySelected()) {
                                        stb.setSelectionTail(cursorPos);
                                    }
                                    if (cursorPos > 0) {
                                        stb.setSelectionHead(cursorPos - 1);
                                        cursorPosition.set(cursorPos - 1);
                                    }
                                }
                            } else {
                                if(e.isControlled()) {
                                    if (cursorPos > 0) {
                                        if (stb.anySelected()) {
                                            stb.resetSelection();
                                        }
                                        int jump = ctrlJump(cursorPos, true);
                                        cursorPosition.set(cursorPos - jump);
                                    }
                                } else {
                                    if (stb.anySelected()) {
                                        cursorPosition.set(stb.getSelectionMin());
                                        stb.resetSelection();
                                    } else {
                                        if (cursorPos > 0) {
                                            cursorPosition.set(cursorPos - 1);
                                        }
                                    }
                                }
                            }
                        }
                        case RIGHT -> {
                            int head = stb.selectionHead.get();
                            int tail = stb.selectionTail.get();
                            if(e.isShifted()) {
                                if(e.isControlled()) {
                                    if(e.isAltered()) {
                                        if(tail > head) {
                                            stb.setSelectionTail(head);
                                            cursorPosition.set(tail);
                                            stb.setSelectionHead(tail);
                                        }
                                    } else {
                                        if (cursorPos < text.get().length()) {
                                            int jump = ctrlJump(cursorPos, false);
                                            if(!stb.anySelected()) {
                                                stb.setSelectionTail(cursorPos);
                                            }
                                            stb.setSelectionHead(cursorPos + jump);
                                            cursorPosition.set(cursorPos + jump);
                                        }
                                    }
                                } else {
                                    if (!stb.anySelected()) {
                                        stb.setSelectionTail(cursorPos);
                                    }
                                    if (cursorPos < text.get().length()) {
                                        cursorPosition.set(cursorPos + 1);
                                        stb.setSelectionHead(cursorPos + 1);
                                    }
                                }
                            } else {
                                if(e.isControlled()) {
                                    if (cursorPos < text.get().length()) {
                                        if (stb.anySelected()) {
                                            stb.resetSelection();
                                        }
                                        int jump = ctrlJump(cursorPos, false);
                                        cursorPosition.set(cursorPos + jump);
                                    }
                                } else {
                                    if (stb.anySelected()) {
                                        cursorPosition.set(stb.getSelectionMax());
                                        stb.resetSelection();
                                    } else {
                                        if (cursorPos < text.get().length()) {
                                            cursorPosition.set(cursorPos + 1);
                                        }
                                    }
                                }
                            }
                        }
                        case HOME -> {
                            if(stb.anySelected()) {
                                if(e.isShifted()) {
                                    stb.setSelectionHead(0);
                                } else {
                                    stb.resetSelection();
                                }
                            } else {
                                if(e.isShifted()) {
                                    stb.setSelectionTail(cursorPos);
                                    stb.setSelectionHead(0);
                                }
                            }
                            cursorPosition.set(0);
                        }
                        case END -> {
                            if(stb.anySelected()) {
                                if(e.isShifted()) {
                                    stb.setSelectionHead(text.get().length());
                                } else {
                                    stb.resetSelection();
                                }
                            } else {
                                if(e.isShifted()) {
                                    stb.setSelectionTail(cursorPos);
                                    stb.setSelectionHead(text.get().length());
                                }
                            }
                            cursorPosition.set(text.get().length());
                        }
                        case INSERT -> {
                            if(!editable) break;
                            String clip = clipboard().get();
                            if(!clip.isEmpty()) {
                                paste(clip);
                                stb.resetSelection();
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
                                    stb.resetSelection();
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
                                    stb.resetSelection();
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
                                cursorPosition.set(text.get().length());
                                stb.setSelectionHead(text.get().length());
                                stb.setSelectionTail(0);
                            }
                        }
                        case ESCAPE -> {
                            if(stb.anySelected()) {
                                stb.resetSelection();
                            }
                        }
                    }
                }
            }
        }

        super.update();
    }

    public void selectAll() {
        stb.setSelectionTail(0);
        stb.setSelectionHead(text.get().length());
    }


    public void select(int begin, int length) {
        if(begin >= 0 && length >= 0 && begin + length <= text.get().length()) {
            stb.setSelectionTail(begin);
            stb.setSelectionHead(begin + length);
        }
    }

    public String getSelected() {
        return stb.selectedText().get();
    }

    public String cutSelected() {
        if(stb.anySelected()) {
            String cut = stb.selectedText().get();
            int cursorBegin = stb.getSelectionMin();

            UserAction ua = new UserAction() {

                @Override
                public void front() {
                    String str = text.get();
                    text.set(str.substring(0, cursorBegin) +
                            str.substring(cursorBegin + cut.length()));
                    cursorPosition.set(cursorBegin);
                    stb.resetSelection();
                }

                @Override
                public void back() {
                    String str = text.get();
                    text.set(str.substring(0, cursorBegin) + cut +
                            str.substring(cursorBegin));
                    cursorPosition.set(cursorBegin);
                    stb.setSelectionHead(cursorBegin);
                    stb.setSelectionTail(cursorBegin + cut.length());
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
        int min, max;
        if(!stb.anySelected()) {
            min = stb.getSelectionMin();
            max = stb.getSelectionMax();
        } else {
            int cursorPos = cursorPosition.get();
            min = max = cursorPos;
        }
        String str = text.get();
        String replaced = str.substring(min, max);

        UserAction ua = new UserAction() {

            @Override
            public void front() {
                String str = text.get();
                text.set(str.substring(0, min) + pasted +
                        str.substring(max));
                cursorPosition.set(min + pasted.length());
                stb.setSelectionHead(min + pasted.length());
                stb.setSelectionTail(min);
            }

            @Override
            public void back() {
                String str = text.get();
                text.set(str.substring(0, min) + replaced +
                        str.substring(min + pasted.length()));
                cursorPosition.set(min + replaced.length());
                stb.resetSelection();
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
            String str = text.get();
            text.set(str.substring(0, cursorBegin) + inset + str.substring(cursorBegin));
            cursorPosition.set(cursorBegin + inset.length());
            stb.resetSelection();
        }

        @Override
        public void back() {
            String str = text.get();
            text.set(str.substring(0, cursorBegin) + str.substring(cursorBegin + inset.length()));
            cursorPosition.set(cursorBegin);
            stb.resetSelection();
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
            String str = text.get();
            text.set(str.substring(0, cursorBegin) + str.substring(cursorBegin + outset.length()));
            cursorPosition.set(cursorBegin);
            stb.resetSelection();
        }

        @Override
        public void back() {
            String str = text.get();
            text.set(str.substring(0, cursorBegin) + outset + str.substring(cursorBegin));
            cursorPosition.set(cursorBegin + outset.length());
            stb.resetSelection();

        }
    }
    private CharEraseUserAction charErase = new CharEraseUserAction();

    private void charErase(boolean backspace) {
        int cursorPos = cursorPosition.get();
        if(charErase.outset.isEmpty()) {
            pushOpenUserAction();
            if(backspace) {
                charErase.cursorBegin = cursorPos - 1;
                charErase.outset = text.get().substring(cursorPos - 1, cursorPos);
            } else {
                charErase.cursorBegin = cursorPos;
                charErase.outset = text.get().substring(cursorPos, cursorPos + 1);
            }
        } else {
            if(charErase.cursorBegin != cursorPos) {
                story().push(charErase);
                if(backspace) {
                    charErase = new CharEraseUserAction(cursorPos - 1, text.get().substring(cursorPos - 1, cursorPos));
                } else {
                    charErase = new CharEraseUserAction(cursorPos, text.get().substring(cursorPos, cursorPos + 1));
                }
            } else {
                if(backspace) {
                    charErase.outset = text.get().charAt(cursorPos - 1) + charErase.outset;
                    --charErase.cursorBegin;
                } else {
                    charErase.outset += text.get().charAt(cursorPos);
                }
            }
        }
    }

    private int ctrlJump(int cursorPos, boolean reverse) {

        Cascade<Integer> cps;
        if(reverse) {
            StringBuilder str = new StringBuilder(text.get().substring(0, cursorPos));
            cps = new Cascade<>(str.reverse().codePoints().iterator());
        } else {
            cps = new Cascade<>(text.get().substring(cursorPos)
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

    public Push<String> text() {
        return new OverriddenPush<>(text) {

            @Override
            public void set(String s) {
                selectAll();
                paste(s);
                stb.resetSelection();
                super.set(s);
            }
        };
    }

    public void editable(boolean should) {
        editable = should;
    }

    public boolean isEditable() {
        return editable;
    }

    public NumPull height() {
        return stb.height();
    }
    public NumSource width() {
        return stb.width();
    }

    public NumPull left() {
        return stb.left();
    }

    public NumPull right() {
        return stb.right();
    }

    public NumPull top() {
        return stb.top();
    }

    public NumPull bottom() {
        return stb.bottom();
    }

    public NumPull x() {
        return stb.x();
    }

    public NumPull y() {
        return stb.y();
    }

    public void aim(Located located) {
        stb.aim(located);
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
        $bricks.set(cursor);
    }

    public void hideCursor() {
        $bricks.unset(cursor);
    }
}
