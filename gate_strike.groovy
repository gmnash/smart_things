metadata {
	definition (name: "Gate Strike", namespace: "gmnash", author: "Graham Nash") {
    	capability "Momentary"
	    capability "Configuration"
		capability "Actuator"
        capability "Switch"
        fingerprint mfr: "0084", prod: "0453", model: "0111"
	}

    tiles(scale: 2) {
        // standard tile with actions named
        standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
            state "off", label: '${currentValue}', action: "switch.on",
                  icon: "st.switches.switch.off", backgroundColor: "#ffffff"
            state "on", label: '${currentValue}', action: "switch.off",
                  icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
        }

        // the "switch" tile will appear in the Things view
        main("switch")

        // the "switch" and "power" tiles will appear in the Device Details
        // view (order is left-to-right, top-to-bottom)
        details(["switch"])
    }
}

def parse(String description) {
    def result = null
    def cmd = zwave.parse(description, [0x60: 3])
    if (cmd) {
        log.info "Parsed ${cmd}"
        zwaveEvent(cmd)
    } else {
        log.info "Non-parsed event: ${description}"
    }
    result
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	if (cmd.value == 0) {
    	off()
    }
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	[:]
}

def on(){
	log.info "ON"
	delayBetween([
		zwave.basicV1.basicSet(value: 0xFF).format(),
		zwave.switchBinaryV1.switchBinaryGet().format()
	], 100)
}

def off() {
	log.info "OFF"
    sendEvent (name: "switch", value: "off", isStateChange: true)
    //delayBetween([
	//	zwave.basicV1.basicSet(value: 0x00).format(),
	//	zwave.switchBinaryV1.switchBinaryGet().format()
	//],100)
}

// If you add the Configuration capability to your device type, this
// command will be called right after the device joins to set
// device-specific configuration commands.

def configure() {
	log.info "Configuring...." //setting up to monitor power alarm and actuator duration
	delayBetween([
		zwave.associationV1.associationSet(groupingIdentifier:3, nodeId:[zwaveHubNodeId]).format(),
        zwave.configurationV1.configurationSet(parameterNumber: 11, configurationValue: [1000], size: 1).format(),
        zwave.configurationV1.configurationGet(parameterNumber: 11).format()
	],100)
}
