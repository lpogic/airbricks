package airbricks.text;

import airbricks.Airbrick;
import airbricks.FantomBrick;
import airbricks.button.SliderButtonBrick;
import airbricks.keyboard.KeyboardClient;
import airbricks.keyboard.KeyboardDealer;
import bricks.Color;
import bricks.font.BackedFont;
import bricks.font.FontManager;
import bricks.font.LoadedFont;
import bricks.input.Story;
import bricks.input.UserAction;
import bricks.input.keyboard.Keyboard;
import bricks.input.mouse.Mouse;
import bricks.input.mouse.MouseButton;
import bricks.slab.RectangleSlab;
import bricks.slab.Slab;
import bricks.trade.Host;
import bricks.var.*;
import bricks.var.impulse.Impulse;
import bricks.var.num.NumPull;
import suite.suite.Subject;
import suite.suite.util.Cascade;
import suite.suite.util.Sequence;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ArticleBrick extends Airbrick<Host> implements KeyboardClient, Slab {

    class Lines extends FantomBrick<Host> {

        List<String> lines;
        private int textLength;
        int lineOffset;
        Push<Integer> selectionHead;
        Push<Integer> selectionTail;
        private final Pull<Integer> bricksCount;



        public Lines(Host host) {
            super(host);
            lineOffset = 0;
            lines = new ArrayList<>();
            selectionHead = Var.push(0);
            selectionTail = Var.push(0);
            bricksCount = Var.pull(5);
        }

        public int getBricksCount() {
            return Math.max(1, bricksCount.get());
        }

        public Sequence<SelectableTextBrick> textBricks() {
            return bricks().each(SelectableTextBrick.class);
        }

        @Override
        public Subject bricks() {
            var bc = getBricksCount();
            var ls = super.bricks().size();
            if(ls < bc) {
                var pl = super.bricks().last().as(SelectableTextBrick.class, null);
                for (int i = ls; i < bc; ++i) {
                    var line = new SelectableTextBrick(this);
                    line.height().let(fontHeight);
                    line.left().let(left);
                    int finalI = i;
                    if (pl != null) {
                        line.top().let(pl.bottom());
                    }
                    line.selectionHead().let(() -> {
                        var global = getGlobal(finalI + lineOffset);
                        var bounds = getSelectionBounds();
                        if (SelectableTextBrick.Bounds.disjoint(global, bounds) || !global.contains(bounds.begin()))
                            return 0;
                        return bounds.begin() - global.begin();
                    });
                    line.selectionTail().let(() -> {
                        var global = getGlobal(finalI + lineOffset);
                        var bounds = getSelectionBounds();
                        if (bounds.end() < global.begin()) return 0;
                        if (bounds.end() <= global.end()) return bounds.end() - global.begin();
                        if (bounds.begin() > global.end()) return 0;
                        return global.end() - global.begin();
                    });
                    line.text().let(() -> {
                        var ind = finalI + lineOffset;
                        if (ind < lines.size()) return lines.get(ind);
                        else return "";
                    });
                    line.color().set(Color.mix(1, 1, 1));
                    super.bricks().set(line);
                    pl = line;
                }
                updateSlider();
            } else if(ls > bc) {
                super.bricks().unsetEntire(super.bricks().reverse().first(ls - bc).each());
                updateSlider();
            }
            return super.bricks();
        }

        public void setLineOffset(int offset) {
            lineOffset = offset;
            updateCursor(cursorPosition, false);
            updateSlider();
        }

        public int getMaxOffset() {
            return Math.max(lines.size() - getBricksCount(), 0);
        }

        public String getText() {
            var sb = new StringBuilder();
            for (var s : lines) {
                sb.append(s);
            }
            return sb.toString();
        }

        public void setText(String text) {
            var c = new Cascade<>(text.lines().iterator());
            lines = new ArrayList<>();
            var length = 0;
            for (var l : c) {
                if (c.opening()) {
                    lines.add(l);
                    length += l.length();
                }
                else {
                    lines.add("\n" + l);
                    length += l.length() + 1;
                }
            }
            if (text.endsWith("\n")) {
                lines.add("\n");
                ++length;
            }
            textLength = length;
            updateSlider();
        }

        public int getTextLength() {
            return textLength;
        }

        public LocalPosition getLocal(int global) {
            if (global == 0 && lines.size() == 0) return new LocalPosition(bricks().asExpected(), 0, global);
            for (int i = 0; i < lines.size(); ++i) {
                var length = lines.get(i).length();
                if (length >= global) {
                    if (i < lineOffset || i >= lineOffset + bricks().size()) {
                        return new LocalPosition(null, i, global);
                    } else {
                        return new LocalPosition(bricks().select(i - lineOffset).asExpected(), i, global);
                    }
                }
                global -= length;
            }

            throw new RuntimeException("Over " + global);
        }

        public LocalPosition local(int global, boolean scrollToView) {
            if (global == 0 && lines.size() == 0) return new LocalPosition(bricks().asExpected(), 0, global);
            if (!scrollToView) return getLocal(global);
            for (int i = 0; i < lines.size(); ++i) {
                var length = lines.get(i).length();
                if (length >= global) {
                    if (i <= lineOffset) {
                        if (i < 1) {
                            setLineOffset(0);
                            return new LocalPosition(bricks().asExpected(), i, global);
                        } else {
                            setLineOffset(i - 1);
                            return new LocalPosition(bricks().select(1).asExpected(), i, global);
                        }
                    } else if (i >= lineOffset + bricks().size() - 1) {
                        if (i == lines.size() - 1) {
                            setLineOffset(lines.size() - bricks().size());
                            return new LocalPosition(bricks().last().asExpected(), i, global);
                        } else {
                            setLineOffset(i - bricks().size() + 2);
                            return new LocalPosition(bricks().reverse().select(1).asExpected(), i, global);
                        }
                    } else {
                        return new LocalPosition(bricks().select(i - lineOffset).asExpected(), i, global);
                    }
                }
                global -= length;
            }

            throw new RuntimeException("Over " + global);
        }

        record LocalPosition(SelectableTextBrick brick, int lineIndex, int position) {
        }

        public SelectableTextBrick.Bounds getGlobal(int index) {
            if(index >= lines.size()) return new SelectableTextBrick.Bounds(0, 0);
            int total = 0;
            for(int i = 0;i < lines.size();++i) {
                if(i == index) return new SelectableTextBrick.Bounds(total, total + lines.get(i).length());
                total += lines.get(i).length();
            }
            throw new RuntimeException(index + " over " + lines.size());
        }

        public SelectableTextBrick.Bounds getSelectionBounds() {
            var head = selectionHead.get();
            var tail = selectionTail.get();
            return head < tail ? new SelectableTextBrick.Bounds(head, tail) : new SelectableTextBrick.Bounds(tail, head);
        }

        public void resetSelection() {
            selectionHead.set(0);
            selectionTail.set(0);
        }

        public void setSelection(int tail, int head) {
            this.selectionTail.set(tail);
            this.selectionHead.set(head);
        }
        
        public boolean anySelected() {
            int head = selectionHead.get();
            int tail = selectionTail.get();
            return head != tail;
        }
    }

    protected boolean editable;
    public HasKeyboard hasKeyboard;

    final Lines lines;
    public final RectangleSlab cursor;
    private int cursorPosition;

    SliderButtonBrick slider;

    NumPull fontHeight;
    NumPull width;
    NumPull left;

    Impulse sliderYChange;

    public ArticleBrick(Host host) {
        super(host);
        editable = true;
        hasKeyboard = HasKeyboard.NO;

        lines = new Lines(this);

        fontHeight = Var.num(20);
        width = Var.num(200);
        left = Var.num(400);

        cursorPosition = 0;

        cursor = new RectangleSlab(this) {{
            width().set(1);
        }};

        slider = new SliderButtonBrick(this);
        slider.width().set(15);
        slider.height().set(40);
        slider.right().let(right());
        slider.y().set(top().getFloat() + 20);

        sliderYChange = slider.y().willChange();

        $bricks.set(lines, slider);
    }

    public void updateCursor(int position, boolean updateScroll) {
        cursorPosition = position;
        var local = lines.local(position, updateScroll);
        var stb = local.brick();
        if (stb != null) {
            var x = local.position();
            cursor.height().let(stb.height());
            cursor.color().let(stb.color());
            cursor.x().let(() -> {
                LoadedFont font = order(FontManager.class).getFont(stb.textSlab.font().get());
                float xOffset = font.getStringWidth(stb.text().get().substring(0, x), stb.height().getFloat(), stb.textSlab.isHideEol());
                float l = stb.left().getFloat();
                return l + xOffset;
            }, stb.textSlab.font(), stb.text(), stb.height(), stb.x());
            cursor.y().let(() -> {
                BackedFont font = order(FontManager.class).getFont(stb.textSlab.font().get(), stb.height().getFloat());
                float y = stb.y().getFloat();
                return y + font.getScaledDescent() / 2;
            });
            bricks().set(cursor);
        } else {
            bricks().unset(cursor);
        }
    }

    public void updateCursorPosition(double mx, double my, boolean resetCars) {
        int cp = 0;
        var i = 0;
        for (;i < lines.lineOffset; ++i) {
            cp += lines.lines.get(i).length();
        }
        var c = lines.textBricks().cascade();
        for (var b : c) {
            if (b.text().get().isEmpty() && !c.opening()) {
                --cp;
                break;
            }
            if (b.bottom().getDouble() > my) {
                float x = b.left().getFloat();
                var xp = order(FontManager.class).getFont(b.textSlab.font().get())
                        .getCursorPosition(b.text().get(), b.height().getFloat(), x, (float) mx, b.textSlab.isHideEol());
                cp += xp;
                break;
            } else {
                cp += b.text().get().length();
            }
            if (c.opening()) ++cp;
        }
        var textLength = lines.getTextLength();
        if(cp > textLength) cp = textLength;
        if(cp < 0) cp = 0;
        updateCursor(cp, false);
        if (resetCars) {
            lines.setSelection(cp, cp);
        } else {
            lines.selectionHead.set(cp);
        }
    }

    @Override
    public void update() {

        if(sliderYChange.occur()) {
            var part = (slider.top().getFloat() - top().getFloat()) / (height().getFloat() - slider.height().getFloat());
            var maxOffset = lines.getMaxOffset();
            lines.lineOffset = NumPull.trim(Math.round((maxOffset) * part), 0, maxOffset);
            updateCursor(cursorPosition, false);
        }

        var in = input();
        boolean mouseLeftButtonPress = false;
        for (var e : in.getEvents()) {
            if (e instanceof Mouse.ButtonEvent be) {
                if (be.button == MouseButton.Code.LEFT) {
                    if (be.isPress()) {
                        if (seeCursor(true)) {
                            mouseLeftButtonPress = true;
                        }
                    }
                }
            }
        }
        if (seeKeyboard()) {
            if (in.state.isPressed(MouseButton.Code.LEFT) && seeCursor(true)) {
                updateCursorPosition(in.state.mouseCursorX(), in.state.mouseCursorY(), mouseLeftButtonPress);
            }

            if (editable) {
                StringBuilder stringBuilder = new StringBuilder();
                for (var che : in.getEvents().select(Keyboard.CharEvent.class)) {
                    stringBuilder.appendCodePoint(che.getCodepoint());
                }
                if (!stringBuilder.isEmpty()) {
                    String inset = stringBuilder.toString();
                    cutSelected();
                    charInset(inset);
                    String str = lines.getText();
                    lines.setText(str.substring(0, cursorPosition) + inset + str.substring(cursorPosition));
                    updateCursor(cursorPosition + inset.length(), true);
                }
            }

            for (var e : in.getEvents().select(Keyboard.KeyEvent.class)) {
                if (e.isHold()) {
                    if (e.key.isNumPad() && !e.isNumLocked()) {
                        switch (e.key) {
                            case NUM_7_HOME -> home(cursorPosition, e.isControlled(), e.isShifted());
                            case NUM_1_END -> end(cursorPosition, e.isControlled(), e.isShifted());
                            case NUM_9_PAGE_UP -> pageUp(cursorPosition, e.isShifted());
                            case NUM_3_PAGE_DOWN -> pageDown(cursorPosition, e.isShifted());
                            case NUM_0_INSERT -> {
                                if (!editable) break;
                                String clip = clipboard().get();
                                if (!clip.isEmpty()) {
                                    paste(clip);
                                    lines.resetSelection();
                                }
                            }
                            case NUM_DECIMAL_DELETE -> {
                                if (!editable) break;
                                String cut = cutSelected();
                                if (cut.isEmpty()) {
                                    String str = lines.getText();
                                    if (cursorPosition < str.length()) {
                                        charErase(false);
                                        lines.setText(str.substring(0, cursorPosition) + str.substring(cursorPosition + 1));
                                    }
                                }
                            }
                        }
                    }
                    switch (e.key) {
                        case BACKSPACE -> {
                            if (!editable) break;
                            String cut = cutSelected();
                            if (cut.isEmpty()) {
                                if (cursorPosition > 0) {
                                    charErase(true);
                                    String str = lines.getText();
                                    lines.setText(str.substring(0, cursorPosition - 1) + str.substring(cursorPosition));
                                    updateCursor(cursorPosition - 1, true);
                                }
                            }
                            lines.resetSelection();
                        }
                        case DELETE -> {
                            if (!editable) break;
                            String cut = cutSelected();
                            if (cut.isEmpty()) {
                                String str = lines.getText();
                                if (cursorPosition < str.length()) {
                                    charErase(false);
                                    lines.setText(str.substring(0, cursorPosition) + str.substring(cursorPosition + 1));
                                }
                            }
                        }
                        case LEFT -> {
                            int head = lines.selectionHead.get();
                            int tail = lines.selectionTail.get();
                            if (e.isShifted()) {
                                if (e.isControlled()) {
                                    if (e.isAltered()) {
                                        if (tail < head) {
                                            lines.setSelection(head, tail);
                                            updateCursor(tail, true);
                                        }
                                    } else {
                                        if (cursorPosition > 0) {
                                            int jump = ctrlJump(cursorPosition, true);
                                            updateCursor(cursorPosition - jump, true);
                                            if(lines.anySelected()) {
                                                lines.selectionHead.set(cursorPosition);
                                            } else {
                                                lines.setSelection(cursorPosition + jump, cursorPosition);
                                            }
                                        }
                                    }
                                } else {
                                    if (cursorPosition > 0) {
                                        updateCursor(cursorPosition - 1, true);
                                        if(lines.anySelected()) {
                                            lines.selectionHead.set(cursorPosition);
                                        } else {
                                            lines.setSelection(cursorPosition + 1, cursorPosition);
                                        }
                                    } else {
                                        if (!lines.anySelected()) {
                                            lines.selectionTail.set(cursorPosition);
                                        }
                                    }
                                }
                            } else {
                                if (e.isControlled()) {
                                    if (cursorPosition > 0) {
                                        if (lines.anySelected()) {
                                            lines.resetSelection();
                                        }
                                        int jump = ctrlJump(cursorPosition, true);
                                        updateCursor(cursorPosition - jump, true);
                                    }
                                } else {
                                    if (lines.anySelected()) {
                                        updateCursor(lines.getSelectionBounds().begin(), true);
                                        lines.resetSelection();
                                    } else {
                                        if (cursorPosition > 0) {
                                            updateCursor(cursorPosition - 1, true);
                                        }
                                    }
                                }
                            }
                        }
                        case RIGHT -> {
                            int head = lines.selectionHead.get();
                            int tail = lines.selectionTail.get();
                            if (e.isShifted()) {
                                if (e.isControlled()) {
                                    if (e.isAltered()) {
                                        if (tail > head) {
                                            lines.setSelection(head, tail);
                                            updateCursor(tail, true);
                                        }
                                    } else {
                                        if (cursorPosition < lines.getTextLength()) {
                                            int jump = ctrlJump(cursorPosition, false);
                                            updateCursor(cursorPosition + jump, true);
                                            if(lines.anySelected()) {
                                                lines.selectionHead.set(cursorPosition);
                                            } else {
                                                lines.setSelection(cursorPosition - jump, cursorPosition);
                                            }
                                        }
                                    }
                                } else {
                                    if (cursorPosition < lines.getTextLength()) {
                                        updateCursor(cursorPosition + 1, true);
                                        if(lines.anySelected()) {
                                            lines.selectionHead.set(cursorPosition);
                                        } else {
                                            lines.setSelection(cursorPosition - 1, cursorPosition);
                                        }
                                    } else {
                                        if (!lines.anySelected()) {
                                            lines.selectionTail.set(cursorPosition);
                                        }
                                    }
                                }
                            } else {
                                if (e.isControlled()) {
                                    if (cursorPosition < lines.getTextLength()) {
                                        if (lines.anySelected()) {
                                            lines.resetSelection();
                                        }
                                        int jump = ctrlJump(cursorPosition, false);
                                        updateCursor(cursorPosition + jump, true);
                                    }
                                } else {
                                    if (lines.anySelected()) {
                                        updateCursor(lines.getSelectionBounds().end(), true);
                                        lines.resetSelection();
                                    } else {
                                        if (cursorPosition < lines.getTextLength()) {
                                            updateCursor(cursorPosition + 1, true);
                                        }
                                    }
                                }
                            }
                        }
                        case UP -> {
                            var cursorPos = cursorPosition;
                            var local = lines.getLocal(cursorPosition);
                            if(local.lineIndex > 0) {
                                var l = lines.lines.get(local.lineIndex - 1).length();
                                if(local.lineIndex == 1) ++l;
                                if (l >= local.position) {
                                    updateCursor(cursorPosition - l, true);
                                } else {
                                    updateCursor(cursorPosition - local.position, true);
                                }
                            } else {
                                updateCursor(0, true);
                            }
                            if (e.isShifted()) {
                                if (lines.anySelected()) {
                                    lines.selectionHead.set(cursorPosition);
                                } else {
                                    lines.setSelection(cursorPos, cursorPosition);
                                }
                            } else {
                                lines.resetSelection();
                            }
                        }
                        case DOWN -> {
                            var cursorPos = cursorPosition;
                            var local = lines.getLocal(cursorPosition);
                            var ll = local.brick.text().get().length();
                            if (local.lineIndex < lines.lines.size() - 1) {
                                var l = lines.lines.get(local.lineIndex + 1).length();
                                if (l > local.position) {
                                    if (lines.bricks().raw() == local.brick) ++ll;
                                    updateCursor(cursorPosition + ll, true);
                                } else {
                                    updateCursor(cursorPosition - local.position + ll + l, true);
                                }
                            } else {
                                updateCursor(cursorPosition - local.position + ll, true);
                            }
                            if (e.isShifted()) {
                                if (lines.anySelected()) {
                                    lines.selectionHead.set(cursorPosition);
                                } else {
                                    lines.setSelection(cursorPos, cursorPosition);
                                }
                            } else {
                                lines.resetSelection();
                            }
                        }
                        case ENTER -> {
                            cutSelected();
                            charInset("\n");
                            String str = lines.getText();
                            lines.setText(str.substring(0, cursorPosition) + "\n" + str.substring(cursorPosition));
                            updateCursor(cursorPosition + 1, true);
                        }
                        case HOME -> home(cursorPosition, e.isControlled(), e.isShifted());
                        case END -> end(cursorPosition, e.isControlled(), e.isShifted());
                        case PAGE_UP -> pageUp(cursorPosition, e.isShifted());
                        case PAGE_DOWN -> pageDown(cursorPosition, e.isShifted());
                        case INSERT -> {
                            if (!editable) break;
                            String clip = clipboard().get();
                            if (!clip.isEmpty()) {
                                paste(clip);
                                lines.resetSelection();
                            }
                        }
                        case X -> {
                            if (!editable) break;
                            if (e.isControlled() && !e.isAltered()) {
                                String cut = cutSelected();
                                if (!cut.isEmpty()) clipboard().set(cut);
                            }
                        }
                        case C -> {
                            if (e.isControlled() && !e.isAltered()) {
                                if (e.isShifted()) {
                                    if (!editable) break;
                                    String cut = cutSelected();
                                    String clip = clipboard().get();
                                    if (!clip.isEmpty()) {
                                        paste(clip);
                                        lines.resetSelection();
                                    }
                                    if (!cut.isEmpty()) clipboard().set(cut);
                                } else {
                                    String copy = getSelected();
                                    if (!copy.isEmpty()) {
                                        clipboard().set(copy);
                                    }
                                }
                            }
                        }
                        case V -> {
                            if (!editable) break;
                            if (e.isControlled() && !e.isAltered()) {
                                String clip = clipboard().get();
                                if (!clip.isEmpty()) {
                                    paste(clip);
                                    lines.resetSelection();
                                }
                            }
                        }
                        case Z -> {
                            if (!editable) break;
                            if (e.isControlled() && !e.isAltered()) {
                                Story story = story();
                                pushOpenUserAction();
                                if (e.isShifted()) {
                                    story.front();
                                } else {
                                    story.back();
                                }
                            }
                        }
                        case A -> {
                            if (e.isControlled() && !e.isAltered()) {
                                var l = lines.getTextLength();
                                updateCursor(l, true);
                                lines.setSelection(0, l);
                            }
                        }
                        case ESCAPE -> {
                            if (lines.anySelected()) {
                                lines.resetSelection();
                                e.suppress();
                            }
                        }
                    }
                }
            }
        }

        for (var se : in.getEvents().select(Mouse.ScrollEvent.class)) {
            if(se.y > 0) {
                var offset = lines.lineOffset;
                if (offset > 0) {
                    lines.setLineOffset(offset - 1);
                    se.suppress();
                }
            } else if(se.y < 0) {
                var offset = lines.lineOffset;
                var maxOffset = lines.getMaxOffset();
                if (offset < maxOffset) {
                    lines.setLineOffset(offset + 1);
                    se.suppress();
                }
            }
        }


        super.update();
    }

    public void selectAll() {
        lines.setSelection(0, lines.getTextLength());
    }


    public void select(int begin, int length) {
        if (begin >= 0 && length >= 0 && begin + length <= lines.getTextLength()) {
            lines.setSelection(begin, begin + length);
        }
    }

    public String getSelected() {
        if (lines.anySelected()) {
            var bounds = lines.getSelectionBounds();
            String str = lines.getText();
            return str.substring(bounds.begin(), bounds.end());
        }
        return "";
    }

    public String cutSelected() {
        if (lines.anySelected()) {
            var bounds = lines.getSelectionBounds();
            String str = lines.getText();
            String cut = str.substring(bounds.begin(), bounds.end());
            int cursorBegin = bounds.begin();

            UserAction ua = new UserAction() {

                @Override
                public void front() {
                    String str = lines.getText();
                    lines.setText(str.substring(0, cursorBegin) +
                            str.substring(cursorBegin + cut.length()));
                    updateCursor(cursorBegin, true);
                    lines.resetSelection();
                }

                @Override
                public void back() {
                    String str = lines.getText();
                    lines.setText(str.substring(0, cursorBegin) + cut +
                            str.substring(cursorBegin));
                    updateCursor(cursorBegin, true);
                    lines.setSelection(cursorBegin + cut.length(), cursorBegin);
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
        var normPasted = pasted.lines().collect(Collectors.joining("\n"));
        SelectableTextBrick.Bounds bounds;
        if (lines.anySelected()) {
            bounds = lines.getSelectionBounds();
        } else {
            bounds = new SelectableTextBrick.Bounds(cursorPosition, cursorPosition);
        }
        String str = lines.getText();
        String replaced = str.substring(bounds.begin(), bounds.end());

        UserAction ua = new UserAction() {

            @Override
            public void front() {
                String str = lines.getText();
                lines.setText(str.substring(0, bounds.begin()) + normPasted +
                        str.substring(bounds.end()));
                updateCursor(bounds.begin() + normPasted.length(), true);
                lines.setSelection(bounds.begin(), bounds.begin() + normPasted.length());
            }

            @Override
            public void back() {
                String str = lines.getText();
                lines.setText(str.substring(0, bounds.begin()) + replaced +
                        str.substring(bounds.begin() + normPasted.length()));
                updateCursor(bounds.begin() + replaced.length(), true);
                lines.resetSelection();
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
            String str = lines.getText();
            lines.setText(str.substring(0, cursorBegin) + inset + str.substring(cursorBegin));
            updateCursor(cursorBegin + inset.length(), true);
            lines.resetSelection();
        }

        @Override
        public void back() {
            String str = lines.getText();
            lines.setText(str.substring(0, cursorBegin) + str.substring(cursorBegin + inset.length()));
            updateCursor(cursorBegin, true);
            lines.resetSelection();
        }
    }

    private CharInsetUserAction charInset = new CharInsetUserAction();

    private void charInset(String inset) {
        if (charInset.inset.isEmpty()) {
            pushOpenUserAction();
            charInset.cursorBegin = cursorPosition;
            charInset.inset = inset;
        } else {
            if (charInset.cursorBegin + charInset.inset.length() != cursorPosition) {
                story().push(charInset);
                charInset = new CharInsetUserAction(cursorPosition, inset);
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
            String str = lines.getText();
            lines.setText(str.substring(0, cursorBegin) + str.substring(cursorBegin + outset.length()));
            updateCursor(cursorBegin, true);
            lines.resetSelection();
        }

        @Override
        public void back() {
            String str = lines.getText();
            lines.setText(str.substring(0, cursorBegin) + outset + str.substring(cursorBegin));
            updateCursor(cursorBegin + outset.length(), true);
            lines.resetSelection();

        }
    }

    private CharEraseUserAction charErase = new CharEraseUserAction();

    private void charErase(boolean backspace) {
        if (charErase.outset.isEmpty()) {
            pushOpenUserAction();
            if (backspace) {
                charErase.cursorBegin = cursorPosition - 1;
                charErase.outset = lines.getText().substring(cursorPosition - 1, cursorPosition);
            } else {
                charErase.cursorBegin = cursorPosition;
                charErase.outset = lines.getText().substring(cursorPosition, cursorPosition + 1);
            }
        } else {
            if (charErase.cursorBegin != cursorPosition) {
                story().push(charErase);
                if (backspace) {
                    charErase = new CharEraseUserAction(cursorPosition - 1, lines.getText().substring(cursorPosition - 1, cursorPosition));
                } else {
                    charErase = new CharEraseUserAction(cursorPosition, lines.getText().substring(cursorPosition, cursorPosition + 1));
                }
            } else {
                if (backspace) {
                    charErase.outset = lines.getText().charAt(cursorPosition - 1) + charErase.outset;
                    --charErase.cursorBegin;
                } else {
                    charErase.outset += lines.getText().charAt(cursorPosition);
                }
            }
        }
    }

    private int ctrlJump(int cursorPos, boolean reverse) {

        Cascade<Integer> cps;
        if (reverse) {
            StringBuilder str = new StringBuilder(lines.getText().substring(0, cursorPos));
            cps = new Cascade<>(str.reverse().codePoints().iterator());
        } else {
            cps = new Cascade<>(lines.getText().substring(cursorPos)
                    .codePoints().iterator());
        }
        int jump = 1;
        boolean acceptWhitespaces = Character.isWhitespace(cps.next());
        for (var cp : cps) {
            if (Character.isWhitespace(cp)) {
                if (acceptWhitespaces) {
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
        if (!charInset.inset.isEmpty()) {
            story().push(charInset);
            charInset = new CharInsetUserAction();
        }
        if (!charErase.outset.isEmpty()) {
            story().push(charErase);
            charErase = new CharEraseUserAction();
        }
    }

    public void home(int cursorPos, boolean ctrl, boolean shift) {
        if (ctrl) {
            updateCursor(0, true);
        } else {
            var local = lines.getLocal(cursorPos);
            if (lines.bricks().raw() == local.brick) updateCursor(cursorPos - local.position, true);
            else updateCursor(cursorPos - local.position + 1, true);
        }
        if (lines.anySelected()) {
            if (shift) {
                lines.selectionHead.set(cursorPosition);
            } else {
                lines.resetSelection();
            }
        } else {
            if (shift) {
                lines.setSelection(cursorPos, cursorPosition);
            }
        }
    }

    public void end(int cursorPos, boolean ctrl, boolean shift) {
        if (ctrl) {
            updateCursor(lines.getTextLength(), true);
        } else {
            var local = lines.getLocal(cursorPos);
            updateCursor(cursorPos + local.brick.text().get().length() - local.position, true);
        }
        if (lines.anySelected()) {
            if (shift) {
                lines.selectionHead.set(cursorPosition);
            } else {
                lines.resetSelection();
            }
        } else {
            if (shift) {
                lines.setSelection(cursorPos, cursorPosition);
            }
        }
    }

    public void pageUp(int cursorPos, boolean shift) {
        var local = lines.local(cursorPos, true);
        var jump = lines.getBricksCount();
        if(jump < local.lineIndex) {
            var bounds = lines.getGlobal(local.lineIndex - jump);
            lines.setLineOffset(Math.max(lines.lineOffset - jump, 0));
            updateCursor(local.position > bounds.end() - bounds.begin() ?
                    bounds.end() : bounds.begin() + local.position, false);
        } else {
            var bounds = lines.getGlobal(0);
            lines.setLineOffset(0);
            var lp = local.position;
            if(lp > 0) --lp;
            updateCursor(local.position > bounds.end() - bounds.begin() ?
                    bounds.end() : bounds.begin() + lp, false);
        }

        if (lines.anySelected()) {
            if (shift) {
                lines.selectionHead.set(cursorPosition);
            } else {
                lines.resetSelection();
            }
        } else {
            if (shift) {
                lines.setSelection(cursorPos, cursorPosition);
            }
        }
    }

    public void pageDown(int cursorPos, boolean shift) {
        var local = lines.local(cursorPos, true);
        var jump = lines.getBricksCount();
        SelectableTextBrick.Bounds bounds;
        if(jump < lines.lines.size() - local.lineIndex) {
            bounds = lines.getGlobal(local.lineIndex + jump);
        } else {
            bounds = lines.getGlobal(lines.lines.size() - 1);
        }
        if(lines.lineOffset + jump <= lines.lines.size() - jump) {
            lines.setLineOffset(lines.lineOffset + jump);
        } else {
            lines.setLineOffset(lines.getMaxOffset());
        }
        var lp = local.position;
        if(local.lineIndex == 0) ++lp;
        updateCursor(local.position > bounds.end() - bounds.begin() ?
                bounds.end() : bounds.begin() + lp, false);

        if (lines.anySelected()) {
            if (shift) {
                lines.selectionHead.set(cursorPosition);
            } else {
                lines.resetSelection();
            }
        } else {
            if (shift) {
                lines.setSelection(cursorPos, cursorPosition);
            }
        }
    }


    public Push<String> text() {
        return new SubjectBasedPush<>("") {

            @Override
            public String get() {
                return lines.getText();
            }

            @Override
            public void set(String s) {
                selectAll();
                paste(s);
                updateCursor(0, true);
                lines.resetSelection();
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

    public NumPull textHeight() {
        return fontHeight;
    }

    public NumPull height() {
        return new NumPull() {
            @Override
            public void let(Supplier<Number> s) {
                lines.bricksCount.let(() -> (int)(s.get().floatValue() / fontHeight.getFloat()));
            }

            @Override
            public Number get() {
                return fontHeight.getFloat() * lines.getBricksCount();
            }
        };
    }

    public NumPull width() {
        return width;
    }

    public NumPull left() {
        return left;
    }

    public NumPull right() {
        return new NumPull() {
            @Override
            public void let(Supplier<Number> s) {
                left.let(() -> s.get().floatValue() - width.getFloat());
            }

            @Override
            public Number get() {
                return left.getFloat() + width.getFloat();
            }
        };
    }

    public NumPull top() {
        return lines.bricks().as(SelectableTextBrick.class).top();
    }

    public NumPull bottom() {
        return new NumPull() {
            @Override
            public void let(Supplier<Number> s) {
                top().let(() -> s.get().floatValue() - height().getFloat());
            }

            @Override
            public Number get() {
                return top().getFloat() + height().getFloat();
            }
        };
    }

    public NumPull x() {
        return new NumPull() {
            @Override
            public void let(Supplier<Number> s) {
                left.let(() -> s.get().floatValue() - width.getFloat() / 2);
            }

            @Override
            public Number get() {
                return left.getFloat() + width.getFloat() / 2;
            }
        };
    }

    public NumPull y() {
        return new NumPull() {
            @Override
            public void let(Supplier<Number> s) {
                top().let(() -> s.get().floatValue() - height().getFloat() / 2);
            }

            @Override
            public Number get() {
                return top().getFloat() + height().getFloat() / 2;
            }
        };
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
        lay(cursor);
    }

    public void hideCursor() {
        drop(cursor);
    }

    public void updateSlider() {
        var maxOffset = lines.getMaxOffset();
        if(maxOffset > 0 && lines.lineOffset <= maxOffset) {
            lay(slider);
            var part = (bottom().getFloat() - top().getFloat() - slider.height().getFloat()) / maxOffset;
            slider.y().set(top().getFloat() + slider.height().getFloat() / 2 + part * lines.lineOffset);
            sliderYChange.occur();
        } else if(lines.lineOffset > 0) {
            lay(slider);
            slider.y().set(bottom().getFloat() - slider.height().getFloat() / 2);
            sliderYChange.occur();
        } else {
            drop(slider);
        }
    }
}