# Ship-Editor
Visualizer and editor of object data in JSON and CSV format using GUI written with Swing.

Uses JavaGL library for low-level AffineTransform functionality, Jackson for JSON handling and Lombok for boilerplate code.

## Development Note 17.07.23:

![](showcase/history/Sample%203.png)

### Implemented:

- Game data panel, which holds entries for ship and hullmod entries. Ship entries encompass CSV data, Hull and Skin files.
- UI components and all the deserialization support for game data panel. Handling of unconventional JSON and CSV features.
- Optimized loading and caching for on-the-fly display of hullmod icons.
- Extensive context menu options for data entries, support for fast layer loading via double-click and PNG drag-and-drop.
- Trial runs for fat JAR packing with Maven-Shade: works as prototype, will need some tweaks at release.
- Hotkey hints painting.
- Bound mirroring support.
- Centers panel, includes collision and shield parts. Deserialization of hullSpecFile styles and their use for displayed shield colors.

### Issues:

- EventBus listener storage unresolved, bits of duplicated code multiply. Complexity grows steadily, but the framework base is still robust enough. 

## Development Note 17.06.23:

![](showcase/history/Sample%202.png)

### Implemented:

 - Event Bus system, which provides loose coupling. Now any part of the system can easily send and receive targeted messages to any other part, the only required dependency being Event Bus class itself and the specific event record.
 - Layers system, which enables simultaneous view and editing of several ship entities at once, practically limited only by memory and performance of app. This provides a way to implement modular ship editing and makes it easy to compare different ships.
 - Groundwork for instrument panels and templates for layer and centers panel - this entailed modes refactor. For layer, opacity value is implemented, while centers panel has collision circles done, which will be extended to shield center and radius. 
 - Undo/Redo system basics, implemented as encapsulated edits.

### Issues:

 - Listener Storage in Event Bus does not have a robust clearing functionality; so far this is not a problem as bus size is not significant and ad-hoc clearing methods are implemented, but might need a considerable refactoring later.

## Development Note 12.05.23:

![](showcase/history/Sample%201.png)

### Implemented:

 - Base viewer functionality like panning, zooming, rotating
 - Pixel snapped cursor, guides and snappable/rotatable overlays framework
 - Support for arbitrary number of coordinate system anchors, world points functionality
 - BoundPoints adding, inserting and removing
 - Representation of world points in side UI panel.

### Issues: 

 - Poorly thought-out architecture leading to quick spaghetti code pile-up - needs rework with Event Bus system, which will provide robust loose coupling.
