package skadistats.clarity.model.state;

import skadistats.clarity.decoder.Util;

public class ArrayEntityState implements EntityState {

    private Object[] state;

    public ArrayEntityState(int length) {
        this.state = new Object[length];
    }

    @Override
    public int length() {
        return state.length;
    }

    @Override
    public boolean has(int idx) {
        return state[idx] != null;
    }

    @Override
    public Object get(int idx) {
        return state[idx];
    }

    @Override
    public void set(int idx, Object value) {
        state[idx] = value;
    }

    @Override
    public EntityState createSub(int idx, int length) {
        ArrayEntityState as = length != -1 ? new ArrayEntityState(length) : null;
        state[idx] = as;
        return as;
    }

    @Override
    public EntityState clone() {
        return Util.clone(this);
    }

}
