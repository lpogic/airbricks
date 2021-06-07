open module airbricks {
    requires org.joml;
    requires org.lwjgl.glfw;
    requires org.lwjgl.opengl;
    requires org.lwjgl.stb;
    requires brackettree;
    requires suite.main;
    requires bricks;

    exports airbricks.model;
    exports airbricks.model.button;
    exports airbricks.model.assistance;
    exports airbricks.model.selection;
}