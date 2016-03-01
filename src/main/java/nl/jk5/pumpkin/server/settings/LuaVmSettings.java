package nl.jk5.pumpkin.server.settings;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;

public final class LuaVmSettings {

    private final boolean logLuaCallbackErrors;
    private final boolean limitMemory;
    private final boolean disableLocaleChanging;
    private final int threads;
    private final int threadPriority;
    private final long tmpfsSize;
    private final boolean allowBytecode;
    private final double timeout;

    LuaVmSettings(CommentedConfigurationNode config){
        if(config.getNode("log-callback-errors").isVirtual()){
            config.getNode("log-callback-errors")
                    .setComment("This setting is meant for debugging errors that occur in Lua callbacks.\n" +
                            "Per default, if an error occurs and it has a message set, only the\n" +
                            "message is pushed back to lua, and that's it. If you encounter weird\n" +
                            "errors or are developing mappack scripts, you'll want the stacktrace for those\n" +
                            "errors. Enabling this setting will log them to the game log. This is\n" +
                            "disabled per default to avoid spamming the log with inconsequental\n" +
                            "exceptions suck IllegalArgumentExceptions and the like.")
                    .setValue(false);
        }
        this.logLuaCallbackErrors = config.getNode("log-callback-errors").getBoolean();

        if(config.getNode("disable-memory-limit").isVirtual()){
            config.getNode("disable-memory-limit")
                    .setComment("Disable memory limit enforcement. This means Lua states can\n" +
                            "theoretically use as much memory as they want. Only relevant when\n" +
                            "using the native lua implementation.")
                    .setValue(false);
        }
        this.limitMemory = config.getNode("disable-memory-limit").getBoolean();

        if(config.getNode("disable-locale-changing").isVirtual()){
            config.getNode("disable-locale-changing")
                    .setComment("Prevent Pumpkin calling Lua's os.setlocale method to ensure number\n" +
                            "formatting is the same on all systems it is run on. Use this if you\n" +
                            "suspect this might mess with some other part of Java (this affects\n" +
                            "the native C locale).")
                    .setValue(false);
        }
        this.disableLocaleChanging = config.getNode("disable-locale-changing").getBoolean();

        if(config.getNode("threads").isVirtual()){
            config.getNode("threads")
                    .setComment("The overall number of threads to use to drive lua VMs. Whenever a\n" +
                            "VM should run, for example because a signal should be processed or\n" +
                            "some sleep timer expired it is queued for execution by a worker thread.\n" +
                            "The higher the number of worker threads, the less likely it will be that\n" +
                            "VMs block each other from running, but the higher the host\n" +
                            "system's load may become.")
                    .setValue(4);
        }
        this.threads = config.getNode("threads").getInt();

        if(config.getNode("thread-priority").isVirtual()){
            config.getNode("thread-priority")
                    .setComment("Override for the worker threads' thread priority. If set to a value\n" +
                            "lower than 1 it will use the default value, which is half-way between\n" +
                            "the system minimum and normal priority. Valid values may differ between\n" +
                            "Java versions, but usually the minimum value (lowest priority) is 1,\n" +
                            "the normal value is 5 and the maximum value is 10. If a manual value is\n" +
                            "given it is automatically capped at the maximum.\n" +
                            "USE THIS WITH GREAT CARE. Using a high priority for worker threads may\n" +
                            "avoid issues with VMs timing out, but can also lead to higher\n" +
                            "server load. AGAIN, USE WITH CARE!")
                    .setValue(-1);
        }
        int customPriority = config.getNode("thread-priority").getInt();
        if(customPriority < 1){
            this.threadPriority = Thread.MIN_PRIORITY + (Thread.NORM_PRIORITY - Thread.MIN_PRIORITY) / 2;
        }else{
            this.threadPriority = Math.max(Math.min(customPriority, Thread.MAX_PRIORITY), Thread.MIN_PRIORITY);
        }

        if(config.getNode("tmpfs-size").isVirtual()){
            config.getNode("tmpfs-size")
                    .setComment("The size of the temporary filesystem each VM gets, in bytes")
                    .setValue(65536);
        }
        this.tmpfsSize = config.getNode("tmpfs-size").getLong();

        if(config.getNode("allow-bytecode").isVirtual()){
            config.getNode("allow-bytecode")
                    .setComment("Whether to allow loading precompiled bytecode via Lua's `load`\n" +
                            "function, or related functions (`loadfile`, `dofile`). Enable this\n" +
                            "only if you absolutely trust all users on your server and all Lua\n" +
                            "code you run. This can be a MASSIVE SECURITY RISK, since precompiled\n" +
                            "code can easily be used for exploits, running arbitrary code on the\n" +
                            "real server! I cannot stress this enough: only enable this if you\n" +
                            "know what you're doing.")
                    .setValue(false);
        }
        this.allowBytecode = config.getNode("allow-bytecode").getBoolean();

        if(config.getNode("timeout").isVirtual()){
            config.getNode("timeout")
                    .setComment("The time in seconds a program may run without yielding before it is\n" +
                            "forcibly aborted. This is used to avoid stupidly written or malicious\n" +
                            "programs blocking other VMs by locking down the executor threads.")
                    .setValue(5);
        }
        this.timeout = config.getNode("timeout").getDouble();
    }

    public boolean logLuaCallbackErrors() {
        return this.logLuaCallbackErrors;
    }

    public boolean limitMemory(){
        return this.limitMemory;
    }

    public boolean disableLocaleChanging(){
        return this.disableLocaleChanging;
    }

    public int threads() {
        return threads;
    }

    public int threadPriority() {
        return threadPriority;
    }

    public long tmpfsSize() {
        return this.tmpfsSize;
    }

    public boolean allowBytecode() {
        return this.allowBytecode;
    }

    public double timeout() {
        return this.timeout;
    }
}
