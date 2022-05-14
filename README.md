# TeenyWar

TeenyWar is my clone of the real-time strategy game [Nano War](https://benoitfreslon.com/en/my-games/nano-war/). I made it for the Android course at [BME](https://portal.vik.bme.hu/kepzes/targyak/VIEEAV01/) in the fall semester of 2018.  
You can play against an AI or other humans on premade levels.

You can pick from pre-made levels from a list. You can play against an other person on your local network with automatic discovery.  You can also play against the machine on 3 difficulty settings. The game has optional music. You can persistently save the color of your units, and the difficulty.

From the main menu you can go to settings, solo or multi player mode.

## Technical details

- I made a *MediaPlayer* wrapper to play the music. There are different pieces of music playing in menus, single player mode and multi player mode. The music can be muted. (I only uploaded a small segment of each piece.)
- I drew the levels using *SurfaceView*, through a central component for performance reasons. Detecting touches is handled through this too and the units corresponding touches are calculated indirectly.
- I used **SharedPreferences** to save settings.
- I used [**Gson**](https://github.com/google/gson) to serialize my model objects. This is convenient for both reading from / writing to files and network communication. It also made the levels easily editable by hand.
- **Network** communication is handled through [**KryoNet**](https://github.com/EsotericSoftware/kryonet). The symmetric, double client-server architecture I made was supposed to be more robust, and able to handle more simultaneous players in the future. The game can only be played on a local network and uses KryoNet's host discovery. If I started over now I would ditch both this architecture and KryoNet.
- I used [**EventBus**](https://github.com/greenrobot/EventBus) to handle in-game events, which made for a more elegant model. This way multiplayer and local events come from the same bus as far as the game logic is concerned.
- The levels and the player's score are written into **files** persistently in JSON.
- The **3 enemy difficulties** use three simple distinct strategies.
