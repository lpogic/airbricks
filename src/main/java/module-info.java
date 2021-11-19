open module airbricks {
    requires org.joml;
    requires org.lwjgl.glfw;
    requires org.lwjgl.opengl;
    requires org.lwjgl.stb;
    requires brackettree;
    requires suite;
    requires bricks;

    exports airbricks.button;
    exports airbricks.assistance;
    exports airbricks.selection;
    exports airbricks.switches;
    exports airbricks.tool;
    exports airbricks.text;
    exports airbricks.note;
    exports airbricks.form;
    exports airbricks.table;
    exports airbricks;
}