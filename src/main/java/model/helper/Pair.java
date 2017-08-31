package model.helper;

public class Pair<S, T> {

    private S key;
    private T value;

    public Pair(S key, T value) {
        this.key = key;
        this.value = value;
    }

    public S getKey() {
        return key;
    }

    public T getValue() {
        return value;
    }

    public Object getSameElement(Pair<S, T> other) {

        if (this.key != null) {
            if (this.key.equals(other.key)) return this.key;
            if (this.key.equals(other.value)) return this.key;
        }
        if (this.value != null) {
            if (this.value.equals(other.key)) return this.value;
            if (this.value.equals(other.value)) return this.value;
        }

        return null;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pair<?, ?> pair = (Pair<?, ?>) o;

        if (key != null ? !key.equals(pair.key) : pair.key != null) return false;
        return value != null ? value.equals(pair.value) : pair.value == null;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}