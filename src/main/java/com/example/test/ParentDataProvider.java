package com.example.test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.lang3.RandomStringUtils;

import com.vaadin.flow.component.crud.CrudFilter;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.SortDirection;

public class ParentDataProvider extends AbstractBackEndDataProvider<Parent, CrudFilter> {

    // A real app should hook up something like JPA
    final List<Parent> DATABASE = createRandomParents(1000);

    private Consumer<Long> sizeChangeListener;

    @Override
    protected Stream<Parent> fetchFromBackEnd(Query<Parent, CrudFilter> query) {
        int offset = query.getOffset();
        int limit = query.getLimit();

        Stream<Parent> stream = DATABASE.stream();

        if (query.getFilter().isPresent()) {
            stream = stream
                    .filter(predicate(query.getFilter().get()))
                    .sorted(comparator(query.getFilter().get()));
        }

        return stream.skip(offset).limit(limit);
    }

    @Override
    protected int sizeInBackEnd(Query<Parent, CrudFilter> query) {
        // For RDBMS just execute a SELECT COUNT(*) ... WHERE query
        long count = fetchFromBackEnd(query).count();

        if (sizeChangeListener != null) {
            sizeChangeListener.accept(count);
        }

        return (int) count;
    }

    void setSizeChangeListener(Consumer<Long> listener) {
        sizeChangeListener = listener;
    }

    private static Predicate<Parent> predicate(CrudFilter filter) {
        // For RDBMS just generate a WHERE clause
        return filter.getConstraints().entrySet().stream()
                .map(constraint -> (Predicate<Parent>) parent -> {
                    try {
                        Object value = valueOf(constraint.getKey(), parent);
                        return value != null && value.toString().toLowerCase()
                                .contains(constraint.getValue().toLowerCase());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                })
                .reduce(Predicate::and)
                .orElse(e -> true);
    }

    private static Comparator<Parent> comparator(CrudFilter filter) {
        // For RDBMS just generate an ORDER BY clause
        return filter.getSortOrders().entrySet().stream()
                .map(sortClause -> {
                    try {
                        Comparator<Parent> comparator
                                = Comparator.comparing(parent ->
                                (Comparable) valueOf(sortClause.getKey(), parent));

                        if (sortClause.getValue() == SortDirection.DESCENDING) {
                            comparator = comparator.reversed();
                        }

                        return comparator;
                    } catch (Exception ex) {
                        return (Comparator<Parent>) (o1, o2) -> 0;
                    }
                })
                .reduce(Comparator::thenComparing)
                .orElse((o1, o2) -> 0);
    }

    private static Object valueOf(String fieldName, Parent parent) {
        try {
            Field field = Parent.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(parent);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    void persist(Parent item) {
        if (item.getId() == null) {
            item.setId(DATABASE
                    .stream()
                    .map(Parent::getId)
                    .max(Comparator.naturalOrder())
                    .orElse(0) + 1);
        }

        final Optional<Parent> existingItem = find(item.getId());
        if (existingItem.isPresent()) {
            int position = DATABASE.indexOf(existingItem.get());
            DATABASE.remove(existingItem.get());
            DATABASE.add(position, item);
        } else {
            DATABASE.add(item);
        }
    }

    Optional<Parent> find(Integer id) {
        return DATABASE
                .stream()
                .filter(entity -> entity.getId().equals(id))
                .findFirst();
    }

    void delete(Parent item) {
        DATABASE.removeIf(entity -> entity.getId().equals(item.getId()));
    }
    
    
    private static List<Parent> createRandomParents(int len) {
		List<Parent> parentList = new ArrayList<>();
		for (int i = 0; i < len; i++) {
			Parent newParent = new Parent();
			int nameLen = ThreadLocalRandom.current().nextInt(3, 10 + 1);
			newParent.setName(RandomStringUtils.randomAlphabetic(nameLen));
			
			Set<Person> children = new HashSet<>();
			int numChildren = ThreadLocalRandom.current().nextInt(1, 4 + 1);
			for (int j = 0; j < numChildren; j++) {
				Person child = new Person();
				nameLen = ThreadLocalRandom.current().nextInt(3, 10 + 1);
				child.setName(RandomStringUtils.randomAlphabetic(nameLen));
				children.add(child);
			}
			newParent.setChildren(children);
			newParent.setId(i);
			parentList.add(newParent);
		}
		return parentList;
	}
}
