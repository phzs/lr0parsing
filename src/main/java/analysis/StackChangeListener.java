package analysis;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;

public interface StackChangeListener<T> {

    void onChanged(Change change);

    class Change<T> {
        private ObservableStack<T> stack;
        private T value;
        private boolean pushed, popped, cleared;

        public void setPushed(boolean pushed) {
            this.pushed = pushed;
        }

        public void setPopped(boolean popped) {
            this.popped = popped;
        }

        public void setCleared(boolean cleared) {
            this.cleared = cleared;
        }

        public ObservableStack<T> getStack() {
            return stack;
        }

        public void setStack(ObservableStack<T> stack) {
            this.stack = stack;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }

        public boolean wasPushed() {
            return pushed;
        }
        public boolean wasPopped() {
            return popped;
        }
        public boolean wasCleared() {
            return cleared;
        }
    }
}
