package nl.jk5.pumpkin.server.scripting;

public abstract class AbstractValue implements Value {

    @Override
    public Object apply(Context context, Arguments arguments) {
        return null;
    }

    @Override
    public void unapply(Context context, Arguments arguments) {

    }

    @Override
    public Object[] call(Context context, Arguments arguments) {
        throw new RuntimeException("trying to call a non-callable value");
    }

    @Override
    public void dispose(Context context) {

    }
}
