package works.hop.fields.mapper.sample;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class MapperTest {

    @Test
    void testTopLevelValues() {
        Mapper itemTOMapper = new Mapper();
        itemTOMapper.field("name", "task");
        itemTOMapper.field("completed", "done");

        Item item = new ItemCO("read book", false, "one,two,three", emptyList(), emptyList(), itemTOMapper);
        assertThat(item.getName()).isEqualTo("read book");
        assertThat(item.getCompleted()).isEqualTo(false);

        //scenario 1
        ItemTO itemTO = new ItemTO();
        itemTOMapper.map("name", itemTO);
        itemTOMapper.map("completed", itemTO);
        assertThat(itemTO.getTask()).isEqualTo(item.getName());
        assertThat(itemTO.getDone()).isEqualTo(item.getCompleted());
    }

    @Test
    void testWithNestedValues() {
        Mapper itemTOMapper = new Mapper();
        itemTOMapper.field("name", "item.name");
        itemTOMapper.field("completed", "item.completed");

        Item item = new ItemCO("drink water", true, "one,two,three", emptyList(), emptyList(), itemTOMapper);
        assertThat(item.getName()).isEqualTo("drink water");
        assertThat(item.getCompleted()).isEqualTo(true);

        ItemTO2 itemTO = new ItemTO2(item);
        itemTOMapper.map("name", itemTO);
        itemTOMapper.map("completed", itemTO);
        assertThat(itemTO.getItem().getName()).isEqualTo(item.getName());
        assertThat(itemTO.getItem().getCompleted()).isEqualTo(item.getCompleted());
    }

    @Test
    void testWithMapperFunctionValues() {
        Mapper itemTOMapper = new Mapper();
        itemTOMapper.field("name", "task");
        itemTOMapper.field("completed", "done");
        itemTOMapper.field("notes", "notes", (Function<String, List<String>>) s -> Arrays.asList(s.split(",")));

        Item item = new ItemCO("eat salad", true, "one,two,three", emptyList(), emptyList(), itemTOMapper);
        assertThat(item.getName()).isEqualTo("eat salad");
        assertThat(item.getCompleted()).isEqualTo(true);
        assertThat(item.getNotes()).contains("one", "two", "three");

        ItemTO3 itemTO = new ItemTO3();
        itemTOMapper.map("name", itemTO);
        itemTOMapper.map("completed", itemTO);
        itemTOMapper.map("notes", itemTO);
        assertThat(itemTO.getTask()).isEqualTo(item.getName());
        assertThat(itemTO.getDone()).isEqualTo(item.getCompleted());
        assertThat(itemTO.getNotes().get(0)).isEqualTo("one");
        assertThat(itemTO.getNotes().get(1)).isEqualTo("two");
        assertThat(itemTO.getNotes().get(2)).isEqualTo("three");
    }

    @Test
    void testWithNestedMapperFunctionValues() {
        Mapper itemTOMapper = new Mapper();
        itemTOMapper.field("name", "task");
        itemTOMapper.field("completed", "done");
        itemTOMapper.field("notes", "notes", (Function<String, List<String>>) s -> Arrays.asList(s.split(",")));
        itemTOMapper.field("nested", "children");

        Item child1 = new ItemCO("child 1", true, "", emptyList(), emptyList(), itemTOMapper);
        Item child2 = new ItemCO("child 2", false, "", emptyList(), emptyList(), itemTOMapper);
        Item child3 = new ItemCO("child 3", true, "", emptyList(), emptyList(), itemTOMapper);

        Item item = new ItemCO("do sit-ups", false, "one,two,three", emptyList(), Arrays.asList(child1, child2, child3), itemTOMapper);
        assertThat(item.getName()).isEqualTo("do sit-ups");
        assertThat(item.getCompleted()).isEqualTo(false);
        assertThat(item.getNotes()).contains("one", "two", "three");
        assertThat(item.getNested().size()).isEqualTo(3);

        ItemTO4 itemTO = new ItemTO4();
        itemTOMapper.map("name", itemTO);
        itemTOMapper.map("completed", itemTO);
        itemTOMapper.map("notes", itemTO);
        itemTOMapper.map("subList", itemTO);
        assertThat(itemTO.getTask()).isEqualTo(item.getName());
        assertThat(itemTO.getDone()).isEqualTo(item.getCompleted());
        assertThat(itemTO.getNotes().get(0)).isEqualTo("one");
        assertThat(itemTO.getNotes().get(1)).isEqualTo("two");
        assertThat(itemTO.getNotes().get(2)).isEqualTo("three");
        assertThat(itemTO.getChildren().size()).isEqualTo(3);
        assertThat(itemTO.getChildren().get(0).getTask()).isEqualTo("do sit-ups");
    }
}