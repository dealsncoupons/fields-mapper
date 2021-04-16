package works.hop.fields.mapper;

import org.junit.jupiter.api.Test;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class FieldMapperTest {

    @Test
    void testTopLevelValues() {
        FieldMapper itemMapper = new FieldMapper();
        itemMapper.map("name", "task");
        itemMapper.map("completed", "done");

        Item item = new Item("read book", false, "one,two,three", emptyList());
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

        Item item = new Item("read book", false, "one,two,three", emptyList());
        assertThat(item.getName()).isEqualTo("read book");
        assertThat(item.getCompleted()).isEqualTo(false);
        
        ItemTO2 itemTO = itemMapper.map(item, ItemTO2.class);
        assertThat(itemTO.getItem().getName()).isEqualTo(item.getName());
        assertThat(itemTO.getItem().getCompleted()).isEqualTo(item.getCompleted());
    }
}