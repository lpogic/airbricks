package airbricks;

import airbricks.note.EssayBrick;
import airbricks.note.NoteBrick;

import static suite.suite.$uite.$;

public class Main extends Wall {

    @Override
    protected void setup() {
        var essay = new EssayBrick(this);
        essay.fill(this);
        essay.text().set("""
                List<String> lines;
                int lineOffset;

                public Lines(Host host) {
                    super(host);
                    lineOffset = 0;
                    lines = new ArrayList<>();
                }

                public Sequence<TextSlab> slabs() {
                    return bricks().each(TextSlab.class);
                }

                public String getText() {
                    var sb = new StringBuilder();
                    for (var s : lines) {
                        sb.append(s);
                    }
                    return sb.toString();
                }
                """);
        lay(essay);
//        bricks().set(new NoteBrick(this){{
//            text().set("OK");
//            width().set(200);
//            aim(Main.this);
//        }});
    }

    @Override
    public void update() {

        super.update();
    }

    public static void main(String[] args) {
        Wall.play($(Wall.class, $(new Main())));
    }
}
