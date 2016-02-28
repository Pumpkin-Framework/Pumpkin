package nl.jk5.pumpkin.server.scripting;

import nl.jk5.pumpkin.server.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public final class Callbacks {

    private static final Map<Class<?>, Map<String, Callback>> cache = new HashMap<Class<?>, Map<String, Callback>>();

    private Callbacks() {
    }

    public static Map<String, Callback> search(Object host){
        if(cache.containsKey(host.getClass())){
            return cache.get(host.getClass());
        }
        Map<String, Callback> map = dynamicAnalyze(host);
        cache.put(host.getClass(), map);
        return map;
    }

    public static void clear(){
        cache.clear();
    }

    public static Map<String, Callback> fromClass(Class<?> cl){
        return staticAnalize(cl);
    }

    private static Map<String, Callback> dynamicAnalyze(Object host) {
        return staticAnalize(host.getClass());
    }

    private static Map<String, Callback> staticAnalize(Class<?> seed) {
        Map<String, Callback> callbacks = new HashMap<String, Callback>();
        Class<?> c = seed;
        while(c != null && c != Object.class){
            Method[] ms = c.getDeclaredMethods();

            for(Method m : ms){
                if(m.isAnnotationPresent(nl.jk5.pumpkin.server.scripting.Callback.class)){
                    if(m.getParameterTypes().length != 2 || m.getParameterTypes()[0] != Context.class || m.getParameterTypes()[1] != Arguments.class){
                        Log.error("Invalid use of Callback annotation on " + m.getDeclaringClass().getName() + "." + m.getName() + ": invalid argument types or count.");
                    }else if(m.getReturnType() != Object[].class){
                        Log.error("Invalid use of Callback annotation on " + m.getDeclaringClass().getName() + "." + m.getName() + ": invalid return type.");
                    }else if(!Modifier.isPublic(m.getModifiers())){
                        Log.error("Invalid use of Callback annotation on " + m.getDeclaringClass().getName() + "." + m.getName() + ": method must be public.");
                    }else{
                        nl.jk5.pumpkin.server.scripting.Callback a = m.getAnnotation(nl.jk5.pumpkin.server.scripting.Callback.class);
                        String name = a.value() != null && !a.value().trim().isEmpty() ? a.value() : m.getName();
                        callbacks.put(name, new ComponentCallback(a, m));
                    }
                }
            }

            c = c.getSuperclass();
        }
        return callbacks;
    }

    public static abstract class Callback {
        private final nl.jk5.pumpkin.server.scripting.Callback annotation;

        public Callback(nl.jk5.pumpkin.server.scripting.Callback annotation) {
            this.annotation = annotation;
        }

        public nl.jk5.pumpkin.server.scripting.Callback getAnnotation() {
            return annotation;
        }

        public abstract Object[] apply(Object instance, Context context, Arguments arguments) throws Exception;
    }

    public static class ComponentCallback extends Callback {
        private final Method method;

        public ComponentCallback(nl.jk5.pumpkin.server.scripting.Callback annotation, Method method) {
            super(annotation);
            this.method = method;
        }

        @Override
        public Object[] apply(Object instance, Context context, Arguments args) throws Exception {
            try{
                //TODO: ASM bridge method
                return (Object[]) method.invoke(instance, context, args);
            }catch(InvocationTargetException e){
                Throwable t = e.getCause();
                if(t instanceof Exception){
                    throw (Exception) t;
                }else{
                    throw e;
                }
            }
        }
    }
}
