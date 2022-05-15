# TeenyWar

![logo](https://user-images.githubusercontent.com/5298006/168467716-89299a16-7102-4d0c-9c4c-716a5fa044d1.png)

TeenyWar is my clone of the real-time strategy game [Nano War](https://benoitfreslon.com/en/my-games/nano-war/). I made it for the Android course at [BME](https://portal.vik.bme.hu/kepzes/targyak/VIEEAV01/) in the fall semester of 2018.  
You can play against an AI or other humans on premade levels.

| main menu | levels | settings |
| --------- | ------ | -------- |
| ![main-menu](https://user-images.githubusercontent.com/5298006/168468095-b9bd6574-db92-40af-80e7-0ab3a5a1cb9b.png) | ![levels](https://user-images.githubusercontent.com/5298006/168468094-6ed971aa-0687-4a15-9120-08bade075502.png) | ![settings](https://user-images.githubusercontent.com/5298006/168468093-e44a215e-852d-4e5d-aec0-7e55982bb37f.png) |

From the main menu you can go to settings, solo or multi player mode.

| single player level | multiplayer loading | multiplayer in progress |
| --------- | ------ | -------- |
| ![single](https://user-images.githubusercontent.com/5298006/168468299-1a381dd9-c008-4e7b-b114-4e3fbf4d3f24.png) | ![multi-loading](https://user-images.githubusercontent.com/5298006/168468298-f8f00ca2-b4be-4cdf-b1cf-f8f9c480dead.png) | ![multi-in-progress](https://user-images.githubusercontent.com/5298006/168468297-8f801de9-3703-432e-a2af-02be74b63c9d.png) |

You can pick from pre-made levels from a list. You can play against an other person on your local network with automatic discovery.  You can also play against the machine on 3 difficulty settings. The game has optional music. You can persistently save the color of your units, and the difficulty.

## Technical details

- I made a *MediaPlayer* wrapper to play the music. There are different pieces of music playing in menus, single player mode and multi player mode. The music can be muted. (I only uploaded a small segment of each piece.)
- I drew the levels using *SurfaceView*, through a central component for performance reasons. Detecting touches is handled through this too and the units corresponding touches are calculated indirectly.
- I used **SharedPreferences** to save settings.
- I used [**Gson**](https://github.com/google/gson) to serialize my model objects. This is convenient for both reading from / writing to files and network communication. It also made the levels easily editable by hand.
- **Network** communication is handled through [**KryoNet**](https://github.com/EsotericSoftware/kryonet). The symmetric, double client-server architecture I made was supposed to be more robust, and able to handle more simultaneous players in the future. The game can only be played on a local network and uses KryoNet's host discovery. If I started over now I would ditch both this architecture and KryoNet.
- I used [**EventBus**](https://github.com/greenrobot/EventBus) to handle in-game events, which made for a more elegant model. This way multiplayer and local events come from the same bus as far as the game logic is concerned.
- The levels and the player's score are written into **files** persistently in JSON.
- The **3 enemy difficulties** use three simple distinct strategies.
