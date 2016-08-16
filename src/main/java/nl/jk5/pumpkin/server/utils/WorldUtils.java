package nl.jk5.pumpkin.server.utils;

import nl.jk5.pumpkin.server.Log;
import org.spongepowered.api.world.DimensionType;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.BitSet;

public final class WorldUtils {

    private static final Method unregisterDimensionMethod;
    private static final Method registerDimensionMethod;
    private static final BitSet dimensionIdList;

    static {
        try{
            Class<?> dimensionManager = Class.forName("net.minecraftforge.common.DimensionManager");
            unregisterDimensionMethod = dimensionManager.getDeclaredMethod("unregisterDimension", Integer.TYPE);
            Method m = null;
            for (Method method : dimensionManager.getDeclaredMethods()) {
                if(method.getName().equals("registerDimension")){
                    m = method;
                }
            }
            if(m == null){
                throw new NoSuchMethodException("net.minecraftforge.common.DimensionManager.registerDimension(int, DimensionType)");
            }
            registerDimensionMethod = m;

            Field dimMapField = dimensionManager.getDeclaredField("dimensionMap");
            dimMapField.setAccessible(true);
            dimensionIdList = (BitSet) dimMapField.get(null);
        }catch(Exception e){
            Log.error("Was not able to hook into DimensionManager");
            throw new RuntimeException(e);
        }
    }

    private WorldUtils() {
    }

    public static void unregisterDimension(int dimid) {
        try{
            unregisterDimensionMethod.invoke(null, dimid);
        }catch(IllegalAccessException | InvocationTargetException e){
            Log.warn("Was not able to unregister dimension " + dimid, e);
        }
    }

    public static void registerDimension(int dimid, DimensionType type) {
        try{
            registerDimensionMethod.invoke(null, dimid, type);
        }catch(IllegalAccessException | InvocationTargetException e){
            Log.warn("Was not able to register dimension " + dimid, e);
        }
    }

    public static void releaseDimensionId(int dimid) {
        if(dimid < 0) return;
        dimensionIdList.clear(dimid);
    }
}
