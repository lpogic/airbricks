package airbricks.model;

public class Selector {

    Selectable currentSelected;

    public void select(Selectable selectable) {
        if(currentSelected != null) currentSelected.unselect();
        System.out.println(selectable);
        selectable.select();
        currentSelected = selectable;
    }
}
