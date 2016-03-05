/**
 *  MiLight / EasyBulb / LimitlessLED Light Controller
 *
 *  Copyright 2015 Jared Jensen / jared /at/ cloudsy /dot/ com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "MiLight Controller", namespace: "cloudsyjared", author: "Jared Jensen", singleInstance: false) {
		capability "Switch Level"
		capability "Actuator"
		capability "Switch"
        capability "Color Control"
        capability "Polling"
        capability "Sensor"
        capability "Refresh" 
        
        command "reload" 
        command "flash"
        command "onDaylight"
        command "unknown"
	}
    
    preferences {       
       input "mac", "string", title: "MAC Address",
       		  description: "The MAC address of this MiLight bridge", defaultValue: "The MAC address here",
              required: true, displayDuringSetup: false 
       
       input "group", "number", title: "Group Number",
       		  description: "The group you wish to control (0-4), 0 = all", defaultValue: "0",
              required: false, displayDuringSetup: false       
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.Lighting.light20", backgroundColor:"#79b821", nextState:"off"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.Lighting.light20", backgroundColor:"#ffffff", nextState:"on"
				attributeState "unknown", label:'unknwn', action:"switch.on", icon:"st.unknown.unknown.unknown", backgroundColor:"#d3d3d3", nextState:"on"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
            tileAttribute ("device.color", key: "COLOR_CONTROL") {
				attributeState "color", action:"setColor"
			}
		}
        
        standardTile("refresh", "device.testing", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label:"", action:"testing", icon:"st.secondary.refresh"
        }
       
		main(["switch"])
		details(["switch","levelSliderControl", "rgbSelector", "refresh"])
	} 
}

def poll() {
    return refresh()
}

def parse(String description) {
    //if(isDebug) { log.debug "MiLight device: ${mac}, parse description ${description}" }
    parseResponse(description)
}

private parseResponse(String resp) {
	
     log.debug "Received response: ${resp}"
}

private parseResponse(resp) {
	
    log.debug "Received response: ${resp.data}" 
}

def setLevel(percentage, boolean sendHttp = true) {
    
    if (percentage < 1 && percentage > 0) {
		percentage = 1
	}
        
    sendEvent(name: 'level', value: percentage, data: [sendReq: sendHttp])
    
    return sendEvent(name: 'switch', value: "on", data: [sendReq: sendHttp])
}

def setColor(value, boolean sendHttp = true) { 
  	if(value in String) {
        def j = value
        sendEvent(name: 'color', value: j, data: [sendReq: sendHttp])
    } else {
    	def h = value.hex
        sendEvent(name: 'color', value: h, data: [sendReq: sendHttp])
    }
    log.debug "milight color value: $value"
    
	return sendEvent(name: 'switch', value: "on", data: [sendReq: sendHttp])
}

def flash(boolean sendHttp = true){

//def red = [red:255, level:100, hex:"#FF0023", blue:35, saturation:99.6078431372549, hue:97.70341207349081, green:0, alpha:1]
//def white = [red:255, level:100, hex:"#FFFFFF", blue:255, saturation:0, hue:0, green:255, alpha:1]
def redHex = "#FF0023"
def whiteHex = "#FFFFFF"
sendEvent(name: 'switch', value: "on", data: [sendReq: sendHttp])
return delayBetween([
delayBetween([sendEvent(name: 'color', value: redHex, data: [sendReq: sendHttp]), sendEvent(name: 'color', value: whiteHex, data: [sendReq: sendHttp])], 1000),
//delayBetween([sendEvent(name: 'color', value: redHex, data: [sendReq: sendHttp]), sendEvent(name: 'color', value: whiteHex, data: [sendReq: sendHttp])], 1000),
delayBetween([sendEvent(name: 'color', value: redHex, data: [sendReq: sendHttp]), sendEvent(name: 'color', value: whiteHex, data: [sendReq: sendHttp])], 1000)],
2000)
}

def onDaylight(boolean sendHttp = true){
	def whiteHex = "#FFFFFF"
    sendEvent(name: 'color', value: whiteHex, data: [sendReq: sendHttp])
    return sendEvent(name: 'switch', value: "on", data: [sendReq: sendHttp])


}

def unknown() {
    sendEvent(name: "switch", value: "unknown")
}

def on(boolean sendHttp = true) {
        
    return sendEvent(name: "switch", value: "on", data: [sendReq: sendHttp])
}

def off(boolean sendHttp = true) {
    return sendEvent(name: "switch", value: "off", data: [sendReq: sendHttp]);
}
