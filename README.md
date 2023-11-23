# Ship-Editor
Visualizer and editor of object data in JSON and CSV format. Developed as utility tool for the purposes of working with data files of a game Starsector.

Forum thread: https://fractalsoftworks.com/forum/index.php?topic=28239.0

![CodeFactor Grade](https://img.shields.io/codefactor/grade/github/ontheheavens/ship-editor?style=flat-square&label=Code%20Quality) ![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/ontheheavens/ship-editor?style=flat-square&label=Code%20Size)



## Stack:

 - Java 19
 - Swing
 - Maven
 - Jackson
 - Lombok
 - Log4j2

## Used libraries:

 - JavaGL: https://github.com/javagl/Viewer
 - Ikonli: https://github.com/kordamp/ikonli
 - Flatlaf: https://github.com/JFormDesigner/FlatLaf

## Development Note 17.10.23:

![](showcase/history/Sample%207.png)

### Implemented:

- Built-in weapons logical split: normal and decorative.
- CellRenderer stamp panels code with custom TreeUI fixes.
- Variant weapons.
- Variant modules.
- Java 21 update.
- Display of logging activity in GUI with Log4J2 config.
- Hullmods and wings object hierarchy refactor.
- Variant hullmods.
- Variant wings (unfinished).
- Numerous fixes, refactors and small functionality additions across the board.

## Development Note 17.09.23:

![](showcase/history/Sample%205.png)

### Implemented:

- Engine slots.
- Built-ins components: hullmods, wings.
- Ship/Wing sprite previews.
- Variants code groundwork, weapon layer refactors.
- Layerless entity painters functionality (requirement for fitted weapons and modules).
- Projectile specs deserialization, missile rendering, weapon images.
- Performance optimizations, repaint timers.
- Hull and weapon search refactor and filtering.
- Module anchors functionality.
- Weapon render order, installable features painter framework.

## Development Note 17.08.23:

![](showcase/history/Sample%204.png)

### Implemented:

- Major refactors all around: point painting, layer hierarchy.
- Streamlined string painting with TextPainter: supports outlines and arbitrary shape interactions via GlyphVector.
- Robust layer ordering through sortable tabbed pane and PaintOrderController.
- Functional layer rotation with AffineTransform preserving all interactions.
- Finished deserialization of all important data types and their runtime wrappers.
- Skin and variantFile groundwork.
- Weapon slots functionality done, with reusable components also fit for skin slot overrides.
- Launch bays functionality done.

### Issues:

Due to diligent refactoring and much thought dedicated to app hierarchy, almost all important issues seem to be either solved or not a roadblock anymore:

 - EventBus listener pile-up is resolved through cleanup methods on layer removal, and did not prove a significant performance issue after tests and profiling.
 - Performance issues with point drawing after start-up are seemingly fixed with a timed repaint technique.
 - However, the issue with blank tooltips (most likely coming from FlatLaf interactions) still persists.

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
- Centers panel, includes collision and shield parts. Deserialization of hull styles and their use for displayed shield colors.

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

## First draft:

![](showcase/history/Sample%200.jpg)

Used bespoke zoom/translate system, which would have taken quite a bit more time to implement right. It was decided to use JavaGL lib instead for out-of-the-box AffineTransform functionality.
