Android Tile-Based Game
=======================

Author: Dan Ruscoe (dan@ruscoe.org).

This is an example of a basic tile-based, 2D game using Android's Canvas. It can be used as a base for your own games.

This code provides a simple way to build maps based on your own tiles, player movement / tracking and detection of collisions with any number of different tile types.

This code is used in the Android game Radius:

http://ruscoe.org/android/radius/

Important Files
---------------

*src/org/ruscoe/example/tilegame/data/GameDAO.java*

This file populates the games database with tile and map data.

The file contains an example of custom tile definitions and how to use those tiles to build custom maps for your game's levels.

*src/org/ruscoe/example/tilegame/GameView.java*

This generates the game view and handles all game logic and user input.

The function *parseGameLevelData* shows how the tile and map data in the database is translated into a playable game level.

License
-------

Released under the The MIT License.

http://www.opensource.org/licenses/mit-license.php
