package nl.jk5.pumpkin.server.scripting;

import nl.jk5.pumpkin.server.Log;

import java.util.*;

public final class Registry {

    private Registry() {
    }

    public static Object[] convert(Context ctx, Object[] args){
        if(args != null){
            List<Object> ret = new ArrayList<Object>();
            for(Object arg : args){
                ret.add(convertRecursively(ctx, arg, new IdentityHashMap<Object, Object>()));
            }
            return ret.toArray(new Object[ret.size()]);
        }else{
            return null;
        }
    }

    public static Object convertRecursively(Context ctx, Object value, IdentityHashMap<Object, Object> memo){
        return convertRecursively(ctx, value, memo, false);
    }

    public static Object convertRecursively(final Context ctx, final Object value, IdentityHashMap<Object, Object> memo, boolean force){
        if(!force && memo.containsKey(value)){
            return memo.get(value);
        }
        if(value == null){
            return null;
        }else if(value instanceof Boolean || value instanceof Byte || value instanceof Character || value instanceof Short || value instanceof Integer || value instanceof Long || value instanceof Float || value instanceof Double || value instanceof String){
            return value;
        }else if(value instanceof boolean[] || value instanceof byte[] || value instanceof char[] || value instanceof short[] || value instanceof int[] || value instanceof long[] || value instanceof float[] || value instanceof double[] || value instanceof String[]){
            return value;
        }else if(value instanceof Value){
            return value;
        }else if(value instanceof Object[]){
            return convertList(ctx, Arrays.asList(((Object[]) value)), memo);
        }else if(value instanceof List<?>){
            return convertList(ctx, ((List) value), memo);
        }else if(value instanceof Map<?, ?>){
            return convertMap(ctx, ((Map) value), memo);
        }else if(value instanceof Iterable<?>){
            return convertList(ctx, ((Iterable) value), memo);
        }else{
            Log.warn("No converter found for type: " + value.getClass().getName());
        }
        return null;
    }

    public static Object[] convertList(Context ctx, Iterable<?> list, IdentityHashMap<Object, Object> memo){
        List<Object> converted = new ArrayList<Object>();
        memo.put(list, converted);
        for(Object o : list){
            converted.add(convertRecursively(ctx, o, memo));
        }
        return converted.toArray(new Object[converted.size()]);
    }

    public static Map<Object, Object> convertMap(Context ctx, Map<?, ?> in, IdentityHashMap<Object, Object> memo){
        Map<Object, Object> converted;
        if(memo.containsKey(in)){
            converted = ((Map<Object, Object>) memo.get(in));
        }else{
            converted = new HashMap<Object, Object>();
            memo.put(in, converted);
        }
        for(Map.Entry<?, ?> entry : in.entrySet()){
            converted.put(convertRecursively(ctx, entry.getKey(), memo), convertRecursively(ctx, entry.getValue(), memo));
        }
        return converted;
    }
}
