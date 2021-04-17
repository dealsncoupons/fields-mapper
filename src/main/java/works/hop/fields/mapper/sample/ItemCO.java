package works.hop.fields.mapper.sample;

import java.util.List;

public class ItemCO extends Item {

    final Mapper mapper;

    public ItemCO(Mapper mapper) {
        this.mapper = mapper;
    }

    public ItemCO(String name, Boolean completed, String notes, List<String> items, List<Item> objects, Mapper mapper) {
        super(name, completed, notes, items, objects);
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
    public List<Item> getNested() {
        this.mapper.copy("subList", super.getNested());
        return super.getNested();
    }
}
