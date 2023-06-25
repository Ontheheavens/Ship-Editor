# Ship-Editor
Visualizer and editor of object data in JSON and CSV format using GUI written with Swing.

Uses JavaGL library for low-level AffineTransform functionality, Jackson for JSON handling and Lombok for boilerplate code.

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
