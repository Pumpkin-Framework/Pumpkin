package nl.jk5.pumpkin.server.scripting.architecture.jnlua;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.naef.jnlua.*;
import nl.jk5.pumpkin.server.Log;
import nl.jk5.pumpkin.server.Pumpkin;
import nl.jk5.pumpkin.server.scripting.*;
import nl.jk5.pumpkin.server.scripting.architecture.Architecture;
import nl.jk5.pumpkin.server.scripting.architecture.ExecutionResult;
import nl.jk5.pumpkin.server.scripting.architecture.jnlua.api.*;
import nl.jk5.pumpkin.server.scripting.component.impl.fs.FileSystem;
import nl.jk5.pumpkin.server.scripting.component.impl.fs.FileSystems;
import nl.jk5.pumpkin.server.scripting.component.impl.fs.Handle;
import nl.jk5.pumpkin.server.scripting.component.impl.fs.Mode;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.Callable;

@Architecture.Name("Lua 5.2")
public class JNLuaArchitecture implements Architecture {

    private final Machine machine;
    private final List<? extends NativeLuaApi> apis;

    private int kernelMemory = 0;

    LuaState lua;

    public JNLuaArchitecture(Machine machine){
        this.machine = machine;
        this.apis = ImmutableList.of(
                new ComponentApi(this),
                new ComputerApi(this),
                new OSApi(this),
                new SystemApi(this),
                new UnicodeApi(this),
                new UserdataApi(this),
                new BiosApi(this) // TODO: 1-3-16 Move the bios to a EEPROM component
        );
    }

    public int invoke(Callable<Object[]> method){
        try{
            Object[] args = method.call();
            if(args == null || args.length == 0){
                lua.pushBoolean(true);
                return 1;
            }else{
                lua.pushBoolean(true);
                for(Object arg : args){
                    LuaStateUtils.pushValue(lua, arg, this.machine);
                }
                return 1 + args.length;
            }
        }catch(Throwable e){
            if(Pumpkin.instance().getSettings().getLuaVmSettings().logLuaCallbackErrors() && !(e instanceof LimitReachedException)){
                Log.warn("Exception in Lua callback", e);
            }
            if(e instanceof LimitReachedException){
                return 0;
            }else if(e instanceof IllegalArgumentException && e.getMessage() != null){
                lua.pushBoolean(false);
                lua.pushString(e.getMessage());
                return 2;
            }else if(e.getMessage() != null){
                lua.pushBoolean(true);
                lua.pushNil();
                lua.pushString(e.getMessage());
                if(Pumpkin.instance().getSettings().getLuaVmSettings().logLuaCallbackErrors()){
                    lua.pushString(Joiner.on('\n').join(e.getStackTrace()));
                    return 4;
                }
                return 3;
            }else if(e instanceof IndexOutOfBoundsException){
                lua.pushBoolean(false);
                lua.pushString("index out of bounds");
                return 2;
            }else if(e instanceof IllegalArgumentException){
                lua.pushBoolean(false);
                lua.pushString("bad argument");
                return 2;
            }else if(e instanceof NoSuchMethodException){
                lua.pushBoolean(false);
                lua.pushString("no such method");
                return 2;
            }else if(e instanceof FileNotFoundException){
                lua.pushBoolean(true);
                lua.pushNil();
                lua.pushString("file not found");
                return 3;
            }else if(e instanceof SecurityException){
                lua.pushBoolean(true);
                lua.pushNil();
                lua.pushString("access denied");
                return 3;
            }else if(e instanceof IOException){
                lua.pushBoolean(true);
                lua.pushNil();
                lua.pushString("i/o error");
                return 3;
            }else if(e instanceof UnsupportedOperationException){
                lua.pushBoolean(false);
                lua.pushString("unsupported operation");
                return 2;
            }else{
                Log.warn("Unexpected error in Lua callback", e);
                lua.pushBoolean(true);
                lua.pushNil();
                lua.pushString("unknown error");
                return 3;
            }
        }
    }

    public int documentation(Callable<String> method){
        try{
            String doc = method.call();
            if(doc == null || doc.isEmpty()){
                lua.pushNil();
            }else{
                lua.pushString(doc);
            }
            return 1;
        }catch(NoSuchElementException e){
            lua.pushNil();
            lua.pushString("no such method");
            return 2;
        }catch(Throwable t){
            lua.pushNil();
            if(t.getMessage() != null){
                lua.pushString(t.getMessage());
            }else{
                lua.pushString(t.toString());
            }
            return 2;
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean isInitialized() {
        return this.kernelMemory > 0;
    }

    @Override
    public void runSynchronized() {
        assert lua.getTop() == 2;
        assert lua.isThread(1);
        assert lua.isFunction(2);

        try{
            // Synchronized call protocol requires the called function to return
            // a table, which holds the results of the call, to be passed back
            // to the coroutine.yield() that triggered the call.
            lua.call(0, 1);
            lua.checkType(2, LuaType.TABLE);
        }catch(LuaMemoryAllocationException e){
            throw new java.lang.OutOfMemoryError("not enough memory");
        }
    }

    @Override
    public ExecutionResult runThreaded(boolean isSynchronizedReturn) {
        try{
            // The kernel thread will always be at stack index one.
            assert lua.isThread(1);

            // Resume the Lua state and remember the number of results we got
            int results;
            if(isSynchronizedReturn){
                assert lua.getTop() == 2;
                assert lua.isTable(2);
                results = lua.resume(1, 1);
            }else{
                if(kernelMemory == 0){
                    // We're doing the initialization run
                    if(lua.resume(1, 0) > 0){
                        // We expect to get nothing here. If we do get something, we had an error
                        results = 0;
                    }else{
                        // Run the garbage collector to get rid of stuff left behind after
                        // the initialization phase to get a good estimate of the base
                        // memory usage the kernel has (including libraries). We remember
                        // that size to grant user-space programs a fixed base amount of
                        // memory, regardless of the memory need of the underlying system
                        // (which may change across releases).
                        lua.gc(LuaState.GcAction.COLLECT, 0);
                        kernelMemory = Math.max(lua.getTotalMemory() - lua.getFreeMemory(), 1);

                        // Fake zero sleep to avoid stopping if there are no signals.
                        lua.pushInteger(0);
                        results = 1;
                    }
                }else{
                    Signal signal = machine.popSignal();
                    if(signal != null){
                        lua.pushString(signal.getName());
                        for(Object arg : signal.getArgs()){
                            LuaStateUtils.pushValue(lua, arg, getMachine());
                        }
                        results = lua.resume(1, 1 + signal.getArgs().length);
                    }else{
                        results = lua.resume(1, 0);
                    }
                }
            }

            // Check if the kernel is still alive.
            if(lua.status(1) == LuaState.YIELD){
                // If we get one function it must be a wrapper for a synchronized
                // call. The protocol is that a closure is pushed that is then called
                // from the main server thread, and returns a table, which is in turn
                // passed to the originating coroutine.yield().
                if(results == 1 && lua.isFunction(2)){
                    return new ExecutionResult.SynchronizedCall();
                }
                // Check if we are shutting down, and if so if we're rebooting. This
                // is signalled by boolean values, where `false` means shut down,
                // `true` means reboot (i.e shutdown then start again).
                else if(results == 1 && lua.isBoolean(2)){
                    return new ExecutionResult.Shutdown(lua.toBoolean(2));
                }else{
                    // If we have a single number, that's how long we may wait before
                    // resuming the state again. Note that the sleep may be interrupted
                    // early if a signal arrives in the meantime. If we have something
                    // else we just process the next signal or wait for one.
                    int ticks = (results == 1 && lua.isNumber(2)) ? (int) (lua.toNumber(2) * 20) : Integer.MAX_VALUE;
                    lua.pop(results);
                    return new ExecutionResult.Sleep(ticks);
                }
            }else{ // The kernel thread returned. If it threw we'd be in the catch below.
                assert lua.isThread(1);
                // We're expecting the result of a pcall, if anything, so boolean + (result | string).
                if(!lua.isBoolean(2) || !(lua.isString(3) || lua.isNoneOrNil(3))){
                    Log.warn("Kernel returned unexpected results.");
                }
                // The pcall *should* never return normally... but check for it nonetheless.
                if(lua.toBoolean(2)){
                    Log.warn("Kernel stopped unexpectedly.");
                    return new ExecutionResult.Shutdown(false);
                }else{
                    if(Pumpkin.instance().getSettings().getLuaVmSettings().limitMemory()){
                        lua.setTotalMemory(Integer.MAX_VALUE);
                    }
                    String error;
                    if(lua.isJavaObjectRaw(3)){
                        error = lua.toJavaObjectRaw(3).toString();
                    }else{
                        error = lua.toString(3);
                    }
                    if(error != null){
                        return new ExecutionResult.Error(error);
                    }else{
                        return new ExecutionResult.Error("unknown error");
                    }
                }
            }
        }catch(LuaRuntimeException e){
            Log.warn("Kernel crashed. This is a bug!\n" + e.toString() + "\n\tat" + Joiner.on("\n\tat").join(e.getLuaStackTrace()));
            return new ExecutionResult.Error("kernel panic: this is a bug, check your log file and report it");
        }catch(LuaGcMetamethodException e){
            if(e.getMessage() != null){
                return new ExecutionResult.Error("kernel panic:\n" + e.getMessage());
            }else{
                return new ExecutionResult.Error("kernel panic:\nerror in garbage collection metamethod");
            }
        }catch(LuaMemoryAllocationException e){
            return new ExecutionResult.Error("not enough memory");
        }catch(Error e){
            if(e.getMessage().equals("not enough memory")){
                return new ExecutionResult.Error("not enough memory");
            }else{
                throw e;
            }
        }
    }

    @Override
    public void onSignal(){

    }

    ////////////////////////////////////////////////////

    @Override
    public boolean initialize() throws IOException {
        // At this point the state is unsandboxed

        Optional<LuaState> state = LuaStateFactory.create();
        if(!state.isPresent()){
            lua = null;
            machine.crash("native libraries not available");
            return false;
        }
        this.lua = state.get();

        this.apis.forEach(NativeLuaApi::initialize);

        FileSystem fs = FileSystems.fromClass(Pumpkin.class, "pumpkin", "lua");
        int fd = fs.open("kernel/kernel.lua", Mode.Read);
        Handle handle = fs.getHandle(fd);
        byte[] buf = new byte[(int) handle.length()];
        handle.read(buf);

        lua.load(new ByteArrayInputStream(buf), "=kernel", "t");
        lua.newThread(); // Left as the first value on the stack.

        handle.close();
        fs.close();

        return true;
    }

    @Override
    public void onConnect() {

    }

    @Override
    public void close() {
        if(lua != null){
            if(Pumpkin.instance().getSettings().getLuaVmSettings().limitMemory()){
                lua.setTotalMemory(Integer.MAX_VALUE);
            }
            lua.close();
            lua = null;
        }
        kernelMemory = 0;
    }





    //Mismatched methods:

    public Machine getMachine() {
        return machine;
    }

    public int getKernelMemory() {
        return kernelMemory;
    }
}
