package analysis;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class ObservableStack<T> extends Stack<T> {
    List<StackChangeListener<T>> changeListeners;

    public ObservableStack() {
        changeListeners = new LinkedList<>();
    }

    private void propagateChange(StackChangeListener.Change change) {
        for(StackChangeListener listener : changeListeners) {
            listener.onChanged(change);
        }
    }

    @Override
    public T push(T item) {
        T result = super.push(item);
        StackChangeListener.Change change = new StackChangeListener.Change();
        change.setStack(this);
        change.setValue(item);
        change.setPushed(true);
        assert(change.wasPushed());
        propagateChange(change);
        return result;
    }

    @Override
    public T pop() {
        T result = super.pop();
        StackChangeListener.Change change = new StackChangeListener.Change();
        change.setStack(this);
        change.setValue(result);
        change.setPopped(true);
        assert(change.wasPopped());
        propagateChange(change);
        return result;
    }

    @Override
    public void clear() {
        super.clear();
        StackChangeListener.Change change = new StackChangeListener.Change();
        change.setStack(this);
        change.setCleared(true);
        assert(change.wasCleared());
        propagateChange(change);
    }

    public void addListener(StackChangeListener listener) {
        changeListeners.add(listener);
    }

    public void removeListener(StackChangeListener listener) {
        changeListeners.remove(listener);
    }
}
