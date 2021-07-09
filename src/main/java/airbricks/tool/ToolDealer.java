package airbricks.tool;

public interface ToolDealer {
    ToolBrick request(ToolClient client);
    void deprive(ToolBrick toolBrick);

}
