package airbricks.model.selection;

public class ExclusiveSelectionDealer implements SelectionDealer {

    SelectionClient owner;

    @Override
    public boolean requestSelection(SelectionClient selectionClient) {
        if(owner != null) owner.depriveSelection();
        owner = selectionClient;
        return true;
    }
}
