# Ship-Editor
Visualizer and editor of object data in JSON format using GUI written with Swing.

Uses JavaGL library for low-level AffineTransform functionality.

## Development Note 12.05.23:

![Preview](https://raw.githubusercontent.com/Ontheheavens/Ship-Editor/master/showcase/history/Sample%201.png)

 - Base viewer functionality like panning, zooming, rotating
 - Pixel snapped cursor, guides and snappable/rotatable overlays framework
 - Support for arbitrary number of coordinate system anchors, world points functionality
 - BoundPoints adding, inserting and removing
 - Representation of world points in side UI panel.

### Issues: 
 Poorly thought-out architecture leading to quick spaghetti code pile-up - needs rework with Event Bus system, which will provide robust loose coupling.
