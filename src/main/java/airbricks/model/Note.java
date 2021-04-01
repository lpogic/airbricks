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
import bricks.trade.Host;
import bricks.var.Source;
import bricks.var.Var;
import bricks.var.Vars;
import bricks.wall.Brick;
import suite.suite.util.Cascade;

public class Note extends Brick<Host> {

    ColorText text;
    ColorRectangle cursor;
    Var<Integer> cursorPosition;
    NoteCars cars;

    public Note(Host host) {
        super(host);

        selected = Vars.set(false);
        when(selected, this::_select, this::_unselect);
        shown = Vars.set(false);
        when(shown, this::_show, this::_hide);
        clicked = Vars.get();

        when(mouse().leftButton().willBe(Mouse.Button::pressed)).then(() -> {
            float x = switch (text.getXOrigin()) {
                case CENTER -> text.getPosition().getX() - text.getWidth() / 2;
                case LEFT -> text.getPosition().getX();
                case RIGHT -> text.getPosition().getX() - text.getWidth();
            };
            cursorPosition.set(order(FontManager.class).getFont(text.getFont())
                    .getCarriagePosition(text.getString(), text.getSize(), x, mouse().position().get().getX()));
        });

        text = text().setString("Fill me!").setSize(20).setColor(Color.mix(1,1,1))
                .setOrigin(XOrigin.CENTER, YOrigin.CENTER).setPosition(400, 300);

        cursorPosition = Vars.set(0);

        cursor = rect().setWidth(1);
        cursor.height().let(text.size());
        cursor.color().let(text.color());
        cursor.yOrigin().let(text.yOrigin());
        cursor.position().let(() -> {
           int pos = cursorPosition.get();
           BackedFont font = order(FontManager.class).getFont(text.getFont(), text.getSize());
           float xOffset = font.getLoadedFont().getStringWidth(text.getString().substring(0, pos), text.getSize());
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
        }, text.font(), text.width(), text.text(), text.size(), text.position(), text.xOrigin(), cursorPosition);

        cars = new NoteCars(this);
        cars.head().let(cursorPosition);
        cars.tail().let(cursorPosition);
    }

    @Override
    public void update() {
        super.update();

        if(isSelected()) {
            var mouse = mouse();
            if(mouse.leftButton().isPressed()) {
                float x = switch (text.getXOrigin()) {
                    case CENTER -> text.getPosition().getX() - text.getWidth() / 2;
                    case LEFT -> text.getPosition().getX();
                    case RIGHT -> text.getPosition().getX() - text.getWidth();
                };
                cursorPosition.set(order(FontManager.class).getFont(text.getFont())
                        .getCarriagePosition(text.getString(), text.getSize(), x, mouse().position().get().getX()));
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
                                        if(cars.isAny()) {
                                            if (cursorPos > 0) {
                                                cars.headIndex.set(cursorPos - 1);
                                                cursorPosition.set(cursorPos - 1);
                                            }
                                        } else {
                                            cars.tailIndex.set(cursorPos);
                                            if (cursorPos > 0) {
                                                cars.headIndex.set(cursorPos - 1);
                                                cursorPosition.set(cursorPos - 1);
                                            }
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
                                        if(cars.isAny()) {
                                            if (cursorPos < text.getString().length()) {
                                                cursorPosition.set(cursorPos + 1);
                                                cars.headIndex.set(cursorPos + 1);
                                            }
                                        } else {
                                            cars.tailIndex.set(cursorPos);
                                            if (cursorPos < text.getString().length()) {
                                                cursorPosition.set(cursorPos + 1);
                                                cars.headIndex.set(cursorPos + 1);
                                            }
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
                        }
                    }
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
            if(acceptWhitespaces == Character.isWhitespace(cp)) {
                ++jump;
            } else break;
        }
        return jump;
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

    Var<Number> clicked;

    public void click() {
        clicked.set(System.currentTimeMillis());
        selected.set(true);
    }

    public Var<Number> clicked() {
        return clicked;
    }

    public Var<String> string() {
        return text.text();
    }

    public float getWidth() {
        return text.getWidth();
    }

    public Source<Number> width() {
        return text.width();
    }
}
