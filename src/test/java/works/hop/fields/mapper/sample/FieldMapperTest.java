package works.hop.fields.mapper.sample;

import org.junit.jupiter.api.Test;
import works.hop.fields.mapper.sample.*;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class FieldMapperTest {

    @Test
    void testTopLevelValues() {
        FieldMapper itemMapper = new FieldMapper();
        itemMapper.map("name", "task");
        itemMapper.map("completed", "done");

        Item item = new Item("read book", false, "one,two,three", emptyList(), emptyList());
        assertThat(item.getName()).isEqualTo("read book");
        assertThat(item.getCompleted()).isEqualTo(false);

        ItemTO itemTO = itemMapper.map(item, ItemTO.class);
        assertThat(itemTO.getTask()).isEqualTo(item.getName());
        assertThat(itemTO.getDone()).isEqualTo(item.getCompleted());
    }

    @Test
    void testWithNestedValues() {
        FieldMapper itemMapper = new FieldMapper();
        itemMapper.map("name", "item.name");
        itemMapper.map("completed", "item.completed");

        Item item = new Item("read book", false, "one,two,three", emptyList(), emptyList());
        assertThat(item.getName()).isEqualTo("read book");
        assertThat(item.getCompleted()).isEqualTo(false);

        ItemTO2 itemTO = itemMapper.map(item, ItemTO2.class);
        assertThat(itemTO.getItem().getName()).isEqualTo(item.getName());
        assertThat(itemTO.getItem().getCompleted()).isEqualTo(item.getCompleted());
    }

    @Test
    void testWithMapperFunctionValues() {
        FieldMapper itemMapper = new FieldMapper();
        itemMapper.map("name", "task");
        itemMapper.map("completed", "done");
        itemMapper.map("notes", "notes", (Function<String, List<String>>) s -> Arrays.asList(s.split(",")));

        Item item = new Item("read book", false, "one,two,three", emptyList(), emptyList());
        assertThat(item.getName()).isEqualTo("read book");
        assertThat(item.getCompleted()).isEqualTo(false);
        assertThat(item.getNotes()).contains("one", "two", "three");

        ItemTO3 itemTO = itemMapper.map(item, ItemTO3.class);
        assertThat(itemTO.getTask()).isEqualTo(item.getName());
        assertThat(itemTO.getDone()).isEqualTo(item.getCompleted());
        assertThat(itemTO.getNotes().get(0)).isEqualTo("one");
        assertThat(itemTO.getNotes().get(1)).isEqualTo("two");
        assertThat(itemTO.getNotes().get(2)).isEqualTo("three");
    }

    @Test
    void testWithListOfPrimitives() {
        FieldMapper itemMapper = new FieldMapper();
        itemMapper.map("name", "task");
        itemMapper.map("completed", "done");
        itemMapper.map("items", "notes");

        Item item = new Item("read book", false, "one,two,three", Arrays.asList("four", "five", "six"), emptyList());
        assertThat(item.getName()).isEqualTo("read book");
        assertThat(item.getCompleted()).isEqualTo(false);
        assertThat(item.getNotes()).contains("one", "two", "three");
        assertThat(String.join(",", item.getItems())).contains("four", "five", "six");

        ItemTO3 itemTO = itemMapper.map(item, ItemTO3.class);
        assertThat(itemTO.getTask()).isEqualTo("read book");
        assertThat(itemTO.getDone()).isEqualTo(false);
        assertThat(String.join(",", itemTO.getNotes())).contains("four", "five", "six");
    }

    @Test
    void testWithNestedMapperFunctionValues() {
        FieldMapper itemMapper = new FieldMapper();
        itemMapper.map("name", "task");
        itemMapper.map("completed", "done");
        itemMapper.map("notes", "notes", (Function<String, List<String>>) s -> Arrays.asList(s.split(",")));
        itemMapper.map("nested", "children");

        Item child1 = new Item("child 1", true, "", emptyList(), emptyList());
        Item child2 = new Item("child 2", false, "", emptyList(), emptyList());
        Item child3 = new Item("child 3", true, "", emptyList(), emptyList());

        Item item = new Item("do sit-ups", false, "one,two,three", emptyList(), Arrays.asList(child1, child2, child3));
        assertThat(item.getName()).isEqualTo("do sit-ups");
        assertThat(item.getCompleted()).isEqualTo(false);
        assertThat(item.getNotes()).contains("one", "two", "three");
        assertThat(item.getNested().size()).isEqualTo(3);

        ItemTO4 itemTO = itemMapper.map(item, ItemTO4.class);
        assertThat(itemTO.getTask()).isEqualTo(item.getName());
        assertThat(itemTO.getDone()).isEqualTo(item.getCompleted());
        assertThat(itemTO.getNotes().get(0)).isEqualTo("one");
        assertThat(itemTO.getNotes().get(1)).isEqualTo("two");
        assertThat(itemTO.getNotes().get(2)).isEqualTo("three");
        assertThat(itemTO.getChildren().size()).isEqualTo(3);
        assertThat(itemTO.getChildren().get(0).getTask()).isEqualTo("do sit-ups");
    }
}