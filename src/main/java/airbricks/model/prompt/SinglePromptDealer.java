package airbricks.model.prompt;

public class SinglePromptDealer implements PromptDealer {

    Prompt prompt;

    public SinglePromptDealer() {
        prompt = new Prompt(null);
    }

    @Override
    public Prompt getPrompt() {
        return prompt;
    }
}
