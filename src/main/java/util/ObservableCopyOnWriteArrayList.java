package util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * CopyOnWriteArrayList extended so that you can listen on add
 */
public class ObservableCopyOnWriteArrayList<T> extends CopyOnWriteArrayList<T> {

    private List<AddListener<T>> listeners = new ArrayList<>();

    public void addListener(AddListener<T> listener) {
        listeners.add(listener);
    }

    @Override
    public boolean add(T element) {
        boolean added = super.add(element);
        if (added) {
            notifyListeners(element, size() - 1);
        }
        return added;
    }

    @Override
    public void add(int index, T element) {
        super.add(index, element);
        notifyListeners(element, index);
    }

    private void notifyListeners(T element, int index) {
        for (AddListener<T> listener : listeners) {
            listener.onAdd(element, this, index);
        }
    }

}
