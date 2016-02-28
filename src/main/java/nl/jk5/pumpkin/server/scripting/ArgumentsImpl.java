package nl.jk5.pumpkin.server.scripting;

import com.google.common.base.Charsets;
import com.google.common.collect.Iterators;
import com.naef.jnlua.LuaState;

import java.util.Iterator;
import java.util.Map;

public class ArgumentsImpl implements Arguments {

    private final Object[] args;

    public ArgumentsImpl(Object[] args) {
        this.args = args;
    }

    public static ArgumentsImpl of(LuaState lua) {
        return new ArgumentsImpl(LuaStateUtils.toSimpleJavaObjects(lua, 1));
    }

    @Override
    public int count() {
        return args.length;
    }

    @Override
    public Object checkAny(int index) {
        checkIndex(index, "value");
        return args[index];
    }

    @Override
    public boolean checkBoolean(int index) {
        checkIndex(index, "boolean");
        if(args[index] instanceof Boolean){
            return ((Boolean) args[index]);
        }else{
            throw typeError(index, args[index], "boolean");
        }
    }

    @Override
    public int checkInteger(int index) {
        checkIndex(index, "number");
        if(args[index] instanceof Number){
            return ((Number) args[index]).intValue();
        }else{
            throw typeError(index, args[index], "number");
        }
    }

    @Override
    public double checkDouble(int index) {
        checkIndex(index, "number");
        if(args[index] instanceof Number){
            return ((Number) args[index]).doubleValue();
        }else{
            throw typeError(index, args[index], "number");
        }
    }

    @Override
    public String checkString(int index) {
        checkIndex(index, "string");
        if(args[index] instanceof String){
            return ((String) args[index]);
        }else if(args[index] instanceof byte[]){
            return new String((byte[]) args[index], Charsets.UTF_8);
        }else{
            throw typeError(index, args[index], "string");
        }
    }

    @Override
    public byte[] checkByteArray(int index) {
        checkIndex(index, "string");
        if(args[index] instanceof String){
            return ((String) args[index]).getBytes(Charsets.UTF_8);
        }else if(args[index] instanceof byte[]){
            return ((byte[]) args[index]);
        }else{
            throw typeError(index, args[index], "string");
        }
    }

    @Override
    public Map checkTable(int index) {
        checkIndex(index, "table");
        if(args[index] instanceof Map<?, ?>){
            return ((Map<?, ?>) args[index]);
        }else{
            throw typeError(index, args[index], "table");
        }
    }

    @Override
    public Object optAny(int index, Object def) {
        if(!isDefined(index)){
            return def;
        }else{
            return checkAny(index);
        }
    }

    @Override
    public boolean optBoolean(int index, boolean def) {
        if(!isDefined(index)){
            return def;
        }else{
            return checkBoolean(index);
        }
    }

    @Override
    public int optInteger(int index, int def) {
        if(!isDefined(index)){
            return def;
        }else{
            return checkInteger(index);
        }
    }

    @Override
    public double optDouble(int index, double def) {
        if(!isDefined(index)){
            return def;
        }else{
            return checkDouble(index);
        }
    }

    @Override
    public String optString(int index, String def) {
        if(!isDefined(index)){
            return def;
        }else{
            return checkString(index);
        }
    }

    @Override
    public byte[] optByteArray(int index, byte[] def) {
        if(!isDefined(index)){
            return def;
        }else{
            return checkByteArray(index);
        }
    }

    @Override
    public Map optTable(int index, Map def) {
        if(!isDefined(index)){
            return def;
        }else{
            return checkTable(index);
        }
    }

    @Override
    public boolean isBoolean(int index) {
        if(!isDefined(index)) return false;
        return args[index] instanceof Boolean;
    }

    @Override
    public boolean isInteger(int index) {
        if(!isDefined(index)) return false;
        Object a = args[index];
        return a instanceof Byte || a instanceof Short || a instanceof Integer || a instanceof Long || a instanceof Double;
    }

    @Override
    public boolean isDouble(int index) {
        if(!isDefined(index)) return false;
        Object a = args[index];
        return a instanceof Double || a instanceof Float;
    }

    @Override
    public boolean isString(int index) {
        if(!isDefined(index)) return false;
        Object a = args[index];
        return a instanceof String || a instanceof byte[];
    }

    @Override
    public boolean isByteArray(int index) {
        if(!isDefined(index)) return false;
        Object a = args[index];
        return a instanceof String || a instanceof byte[];
    }

    @Override
    public boolean isTable(int index) {
        if(!isDefined(index)) return false;
        return args[index] instanceof Map<?, ?>;
    }

    @Override
    public Object[] toArray() {
        return this.args;
    }

    @Override
    public Iterator<Object> iterator() {
        return Iterators.forArray(args);
    }

    private boolean isDefined(int index){
        return index >= 0 && index < args.length;
    }

    private void checkIndex(int index, String name){
        if(index < 0){
            throw new IndexOutOfBoundsException();
        }else if(args.length <= index){
            throw new IllegalArgumentException("bad arguments #" + (index + 1) + " (" + name + " expected, got no value)");
        }
    }

    private IllegalArgumentException typeError(int index, Object have, String want){
        throw new IllegalArgumentException("bad argument #" + (index + 1) + " (" + want + " expected, got " + typeName(have) + ")");
    }

    private String typeName(Object value){
        if(value == null){
            return "nil";
        }else if(value instanceof Boolean){
            return "boolean";
        }else if(value instanceof Number){
            return "double";
        }else if(value instanceof byte[]){
            return "string";
        }else if(value instanceof Map<?, ?>){
            return "table";
        }else{
            return value.getClass().getSimpleName();
        }
    }
}
