package works.hop.fields.mapper;

import java.util.List;

public class ItemCO extends Item{

    final Mapper mapper;

    public ItemCO(Mapper mapper) {
        this.mapper = mapper;
    }

    public ItemCO(String name, Boolean completed, String notes, List<Item> subList, Mapper mapper) {
        super(name, completed, notes, subList);
        this.mapper = mapper;
    }

    @Override
    public String getName() {
        this.mapper.copy("name", super.getName());
        return super.getName();
    }

    @Override
    public Boolean getCompleted() {
        this.mapper.copy("completed", super.getCompleted());
        return super.getCompleted();
    }

    @Override
    public String getNotes() {
        this.mapper.copy("notes", super.getNotes());
        return super.getNotes();
    }

    @Override
    public List<Item> getSubList() {
        this.mapper.copy("subList", super.getSubList());
        return super.getSubList();
    }
}
