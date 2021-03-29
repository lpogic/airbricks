package airbricks.model;

import bricks.Color;
import bricks.Point;
import bricks.XOrigin;
import bricks.YOrigin;
import bricks.font.BackedFont;
import bricks.font.FontManager;
import bricks.graphic.ColorRectangle;
import bricks.graphic.ColorText;
import bricks.input.Keyboard;
import bricks.input.Mouse;
import bricks.monitor.Monitor;
import bricks.trade.Host;
import bricks.var.Source;
import bricks.var.Var;
import bricks.var.Vars;
import bricks.wall.Brick;
import suite.suite.Subject;
import suite.suite.action.Statement;

public class Note extends Brick {

    ColorText text;
    ColorRectangle caret;
    Var<Integer> caretPosition;

    Var<Boolean> selected;

    Monitor selectMonitor;
    Monitor unselectMonitor;

//    Monitor keyLeftMonitor;
//    Monitor keyRightMonitor;
//    Monitor keyBackspaceMonitor;

    public Note(Host host) {
        super(host);

        selected = Vars.set(false);

        text = text().setText("Fill me!").setSize(20).setColor(Color.mix(1,1,1))
                .setOrigin(XOrigin.CENTER, YOrigin.CENTER).setPosition(400, 300);
        caretPosition = Vars.set(0);
        caret = rect().setWidth(1);
        caret.height().let(text.size());
        caret.color().let(text.color());
        caret.yOrigin().let(text.yOrigin());
        caret.position().preserve(() -> {
           int pos = caretPosition.get();
           BackedFont font = order(FontManager.class).getFont(text.getFont(), text.getSize());
           float xOffset = font.getLoadedFont().getStringWidth(text.getText().substring(0, pos), text.getSize());
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
        }, text.font(), text.width(), text.text(), text.size(), text.position(), text.xOrigin(), caretPosition);

        Subject $m = when(selected, () -> use(caret), () -> cancel(caret));
        selectMonitor = $m.in("rising").asExpected();
        selectMonitor.cancel();
        unselectMonitor = $m.in("falling").asExpected();
        unselectMonitor.cancel();
//        keyRightMonitor = when(order(Keyboard.class).key(262).willGive(Keyboard.Key::holding))
//                .then(() -> {
//                    int caretPos = caretPosition.get();
//                    if(caretPos < text.getText().length()) {
//                        caretPosition.set(caretPos + 1);
//                    }
//                }, false);
//        keyLeftMonitor = when(order(Keyboard.class).key(263).willGive(Keyboard.Key::holding))
//                .then(() -> {
//                    int caretPos = caretPosition.get();
//                    if(caretPos > 0) {
//                        caretPosition.set(caretPos - 1);
//                    }
//                }, false);
//        keyBackspaceMonitor = when(order(Keyboard.class).key(259).willGive(Keyboard.Key::holding))
//                .then(() -> {
//                    int caretPos = caretPosition.get();
//                    if(caretPos > 0) {
//                        caretPosition.set(caretPos - 1);
//                        String txt = text.getText();
//                        text.setText(txt.substring(0, caretPos - 1) + txt.substring(caretPos));
//                    }
//                }, false);
    }

    Var<Boolean> shown = Vars.set(false);
    @Override
    public void show() {
        shown.set(true);
        Monitor.useAll(selectMonitor, unselectMonitor);
        show(text);
    }

    @Override
    public void hide() {
        hide(text);
        Monitor.cancelAll(selectMonitor, unselectMonitor);
        shown.set(false);
    }

    @Override
    public void update() {
        super.update();

        if(selected.get()) {
            var mouse = mouse();
            if(mouse.leftButton().isPressed()) {
                float x = switch (text.getXOrigin()) {
                    case CENTER -> text.getPosition().getX() - text.getWidth() / 2;
                    case LEFT -> text.getPosition().getX();
                    case RIGHT -> text.getPosition().getX() - text.getWidth();
                };
                caretPosition.set(order(FontManager.class).getFont(text.getFont())
                        .getCarriagePosition(text.getText(), text.getSize(), x, mouse().position().get().getX()));
            }

            var keyboard = keyboard();
            int caretPos = caretPosition.get();
            StringBuilder stringBuilder = new StringBuilder();
            for(var che : keyboard.getCharEvents()) {
                stringBuilder.appendCodePoint(che.getCodepoint());
            }
            int sbLength = stringBuilder.length();
            if(sbLength > 0) {
                String inset = stringBuilder.toString();
                String txt = text.getText();
                text.setText(txt.substring(0, caretPos) + inset + txt.substring(caretPos));
                caretPos += sbLength;
                caretPosition.set(caretPos);
            }
            for(var e : keyboard.getEvents()) {
                if(e.isHold()) {
                    System.out.println(e.key);
                    if(e.key.isNumPad() && !e.isNumLocked()) {
                        switch (e.key) {
                            case NUM_7_HOME -> caretPosition.set(0);
                            case NUM_1_END -> caretPosition.set(text.getText().length());
                        }
                    }
                    switch (e.key) {
                        case BACKSPACE -> {
                            if (caretPos > 0) {
                                caretPosition.set(caretPos - 1);
                                String txt = text.getText();
                                text.setText(txt.substring(0, caretPos - 1) + txt.substring(caretPos));
                            }
                        }
                        case LEFT -> {
                            if(caretPos > 0) {
                                caretPosition.set(caretPos - 1);
                            }
                        }
                        case RIGHT -> {
                            if(caretPos < text.getText().length()) {
                                caretPosition.set(caretPos + 1);
                            }
                        }
                        case HOME -> caretPosition.set(0);
                        case END -> caretPosition.set(text.getText().length());
                    }
                }
            }
        }
    }

    Statement click = () -> {};
    public void click() {
        click.play();
        select();
    }

    public void click(Statement whenClick) {
        click = whenClick;
    }

    public void select() {
//        Monitor.useAll(keyLeftMonitor, keyRightMonitor, keyBackspaceMonitor);
        selected.set(true);
    }

    public void unselect() {
//        Monitor.cancelAll(keyLeftMonitor, keyRightMonitor, keyBackspaceMonitor);
        selected.set(false);
    }

    public Source<Boolean> selected() {
        return selected;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public boolean isShown() {
        return shown.get();
    }

    public Var<String> label() {
        return text.text();
    }

    public float getWidth() {
        return text.getWidth();
    }

    public Source<Number> width() {
        return text.width();
    }
}
