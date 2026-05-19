### Yappr Minecraft plugin
Minecraft plugin for use with https://github.com/honza03210/Yappr
Works by hosting a Websocket server. When the player types the voice chat command (/pvc by default) a link gets generated allowing the user to connect to the voice chat in the browser.
The player positions are then fed from the Minecraft Websocket server to the browser client and then exchanged with other peers in the call -> spatial audio is reconstructed upon receiving the peers positions and voice data.


Compiling the same as any spigot minecraft plugin, can be done using ```mvn clean package```.
Put the compiled .jar into the MINECRAFT_SERVER/plugins/ directory, it will then load with all the other plugins

#### File structure
There are only 2 files: ```MCWebSocketServer.java```, which takes care of the WebServer and the communication with in-browser clients and ```ProximityVoiceChat.java```, which is the code linking the WebSocket server to the Spigot API and giving the positions to the WebSocket server.