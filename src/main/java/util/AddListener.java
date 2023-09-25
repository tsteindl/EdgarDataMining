package util;

public interface AddListener<T> {
    void onAdd(T element, ObservableCopyOnWriteArrayList<T> list, int index);
}
