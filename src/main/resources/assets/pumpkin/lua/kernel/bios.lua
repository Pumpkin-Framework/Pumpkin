local component_invoke = component.invoke
function boot_invoke(address, method, ...)
    local result = table.pack(pcall(component_invoke, address, method, ...))
    if not result[1] then
        return nil, result[2]
    else
        return table.unpack(result, 2, result.n)
    end
end

--local eeprom = component.list("eeprom")()
computer.getBootAddress = function()
    return "/dev/pknrootfs"
    --return boot_invoke(eeprom, "getData")
end
computer.setBootAddress = function(address)
    --return boot_invoke(eeprom, "setData", address)
end

local function tryLoadFrom(address)
    local handle, reason = boot_invoke(address, "open", "/init.lua")
    if not handle then
        return nil, reason
    end
    local buffer = ""
    repeat
        local data, reason = boot_invoke(address, "read", handle, math.huge)
        if not data and reason then
            return nil, reason
        end
        buffer = buffer .. (data or "")
    until not data
    boot_invoke(address, "close", handle)
    return load(buffer, "=init")
end
local init, reason
if computer.getBootAddress() then
    init, reason = tryLoadFrom(computer.getBootAddress())
end
if not init then
    computer.setBootAddress()
    for address in component.list("filesystem") do
        init, reason = tryLoadFrom(address)
        if init then
            computer.setBootAddress(address)
            break
        end
    end
end
if not init then
    error("no bootable medium found" .. (reason and (": " .. tostring(reason)) or ""), 0)
end
--computer.beep(1000, 0.2)
init()
