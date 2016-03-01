local component = require("component")
return {
    color = component.invoke("/dev/map", "getTextColors"),
    style = component.invoke("/dev/map", "getTextStyles"),
    create = component.invoke("/dev/map", "createText")
}
