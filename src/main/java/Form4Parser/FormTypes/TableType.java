package Form4Parser.FormTypes;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class TableType /*implements Iterable<>*/ { //TODO: implement iterator
/*
    @Override
    public Iterator iterator() {
        Iterator<> it = new Iterator<>() {

            private int currentIndex = 0;

            @Override
            public boolean hasNext() {
                return currentIndex < currentSize && arrayList[currentIndex] != null;
            }

            @Override
            public Type next() {
                return arrayList[currentIndex++];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
        return it;
    }

 */
    public List<String> keys() {
        Class<? extends TableType> clazz = this.getClass();
        List<String> fieldNames = new ArrayList<>();
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields)
            if (!Modifier.isStatic(field.getModifiers()))
                fieldNames.add(field.getName());
        return fieldNames;
    }

    public List<Object> values() {
        Class<? extends TableType> clazz = this.getClass();
        List<Object> fieldValues = new ArrayList<>();
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (!Modifier.isStatic(field.getModifiers())) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(this);
                    fieldValues.add(value != null ? value.toString() : "");
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return fieldValues;
    }

    public String getId() {
        return this.getClass().getSimpleName().toLowerCase();
    }
}
