local map = require("map")
local message = require("message")
local event = require("event")

local messages = {}
messages["attackers-sub1"] = message.create(message.color.green, "Giving the defenders 1 minute to get geared up")
messages["defenders-sub1"] = message.create(message.color.green, "Invaders will be released within 1 minute")
messages["attackers-sub2"] = message.create(message.color.green, "Try to destroy the sponges in the castle")
messages["defenders-sub2"] = message.create(message.color.green, "The invaders have been released")

local attackers = map.getTeam("Attackers")
local defenders = map.getTeam("Defenders")

local world = map.getWorld("default")

attackers.setSubtitle(messages["attackers-sub1"])
defenders.setSubtitle(messages["defenders-sub1"])

world.setTime(12500)
world.setDifficulty("easy")

local sponges = {}
sponges["a"] = false
sponges["b"] = false

function checkSponges()
    if sponges["a"] and sponges["b"] then
        map.setWinner(attackers)
    end
end

world.watchBlockDestroy(921, 96, 971)
world.watchBlockDestroy(922, 96, 971)
event.listen("block_break_default_921,96,971", function()
    sponges["a"] = true
    checkSponges()
end)
event.listen("block_break_default_922,96,971", function()
    sponges["b"] = true
    checkSponges()
end)

-- Start team blue

defenders.setSpawn(899, 91, 973, 0, 180)
defenders.setFastRespawnTimer(25)

for i, p in ipairs(defenders.getPlayers()) do
    p.setGamemode("survival")
    p.clearInventory()
end
map.getStat("startBlue").enable()

os.sleep(5)

for i, p in ipairs(defenders.getPlayers()) do
    p.heal()
end

os.sleep(15)

defenders.clearSubtitle()

os.sleep(30)

attackers.countdown(10)
event.pull("countdown_done_Attackers")

-- Start team red

attackers.setSubtitle(messages["attackers-sub2"])
defenders.setSubtitle(messages["defenders-sub2"])

attackers.setSpawn(922,84,787)
attackers.setFastRespawnTimer(5)

for i, p in ipairs(attackers.getPlayers()) do
    p.setGamemode("survival")
    p.clearInventory()
end
map.getStat("startRed").enable()

os.sleep(5)

for i, p in ipairs(attackers.getPlayers()) do
    p.heal()
end

os.sleep(5)

defenders.clearSubtitle()
attackers.clearSubtitle()

map.setWinTimeout((19 * 60) + 50, defenders)
event.pull("game_won")

for i, p in ipairs(attackers.getPlayers()) do
    p.setGamemode("adventure")
    p.clearInventory()
end
for i, p in ipairs(defenders.getPlayers()) do
    p.setGamemode("adventure")
    p.clearInventory()
end
world.setDifficulty("peaceful")
