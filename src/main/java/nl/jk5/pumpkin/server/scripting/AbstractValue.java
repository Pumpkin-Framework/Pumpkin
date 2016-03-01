package nl.jk5.pumpkin.server.scripting;

public interface AbstractValue extends Value {

    default Object apply(Context context, Arguments arguments) {
        return null;
    }

    default void unapply(Context context, Arguments arguments) {

    }

    default Object[] call(Context context, Arguments arguments) {
        throw new RuntimeException("trying to call a non-callable value");
    }

    default void dispose(Context context) {

    }
}
