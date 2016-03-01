do
    _G._OSVERSION = "PumpkinOS 0.1"

    local component = component
    local computer = computer
    local unicode = unicode

    -- Runlevel information.
    local runlevel, shutdown = "S", computer.shutdown
    computer.runlevel = function() return runlevel end
    computer.shutdown = function(reboot)
        runlevel = reboot and 6 or 0
        if os.sleep then
            computer.pushSignal("shutdown")
            os.sleep(0.1) -- Allow shutdown processing.
        end
        shutdown(reboot)
    end

    -- Low level dofile implementation to read filesystem libraries.
    local rootfs = {}
    function rootfs.invoke(method, ...) return component.invoke(computer.getBootAddress(), method, ...) end
    function rootfs.open(file) return rootfs.invoke("open", file) end
    function rootfs.read(handle) return rootfs.invoke("read", handle, math.huge) end
    function rootfs.close(handle) return rootfs.invoke("close", handle) end
    function rootfs.inits() return ipairs(rootfs.invoke("list", "boot")) end
    function rootfs.isDirectory(path) return rootfs.invoke("isDirectory", path) end

    print("Booting " .. _OSVERSION .. "...")

    -- Custom low-level loadfile/dofile implementation reading from the Rootfs
    local function loadfile(file)
        print("> " .. file)
        local handle, reason = rootfs.open(file)
        if not handle then
            error(reason)
        end
        local buffer = ""
        repeat
            local data, reason = rootfs.read(handle)
            if not data and reason then
                error(reason)
            end
            buffer = buffer .. (data or "")
        until not data
        rootfs.close(handle)
        return load(buffer, "=" .. file)
    end

    local function dofile(file)
        local program, reason = loadfile(file)
        if program then
            local result = table.pack(pcall(program))
            if result[1] then
                return table.unpack(result, 2, result.n)
            else
                error(result[2])
            end
        else
            error(reason)
        end
    end

    print("Initializing package management...")

    -- Load file system related libraries we need to load other stuff moree
    -- comfortably. This is basically wrapper stuff for the file streams
    -- provided by the filesystem components.
    local package = dofile("/lib/package.lua")

    do
        -- Unclutter global namespace now that we have the package module.
        _G.component = nil
        _G.computer = nil
        _G.process = nil
        _G.unicode = nil

        -- Initialize the package module with some of our own APIs.
        package.loaded.component = component
        package.loaded.computer = computer
        package.loaded.unicode = unicode
        package.preload["buffer"] = loadfile("/lib/buffer.lua")
        package.preload["filesystem"] = loadfile("/lib/filesystem.lua")

        -- Inject the package and io modules into the global namespace, as in Lua.
        _G.package = package
        _G.io = loadfile("/lib/io.lua")()

        --mark modules for delay loaded api
        package.delayed["text"] = true
        package.delayed["sh"] = true
        package.delayed["transforms"] = true
    end

    print("Initializing file system...")

    -- Mount the Rootfs and temporary file systems to allow working on the file
    -- system module from this point on.
    require("filesystem").mount(computer.getBootAddress(), "/")
    package.preload={}

    print("Running boot scripts...")

    -- Run library startup scripts. These mostly initialize event handlers.
    local scripts = {}
    for _, file in rootfs.inits() do
        local path = "boot/" .. file
        if not rootfs.isDirectory(path) then
            table.insert(scripts, path)
        end
    end
    table.sort(scripts)
    for i = 1, #scripts do
        dofile(scripts[i])
    end

    print("Initializing components...")

    local primaries = {}
    for c, t in component.list() do
        if not primaries[t] then
            primaries[t] = {address=c}
        end
        computer.pushSignal("component_added", c, t)
    end
    for t, c in pairs(primaries) do
        component.setPrimary(t, c.address)
    end
    os.sleep(0.5) -- Allow signal processing by libraries.
    computer.pushSignal("init") -- so libs know components are initialized.

    print("Initializing system...")
    os.sleep(0.1) -- Allow init processing.
    runlevel = 1
end



local event = require "event"

local evt
pcall(function()
    repeat
        evt = table.pack(event.pull())
        print("[" .. os.date("%T") .. "] ")
        print(tostring(evt[1]) .. string.rep(" ", math.max(10 - #tostring(evt[1]), 0) + 1))
        print(tostring(evt[2]) .. string.rep(" ", 37 - #tostring(evt[2])))
        if evt.n > 2 then
            for i = 3, evt.n do
                print("  " .. tostring(evt[i]))
            end
        end

        print("\n")
    until evt[1] == "interrupted"
end)
