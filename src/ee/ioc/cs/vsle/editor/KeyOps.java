package ee.ioc.cs.vsle.editor;

import java.awt.event.*;

public class KeyOps implements KeyListener {

	Canvas canvas;

	public KeyOps(Canvas canvas) {
		this.canvas = canvas;
	}

	public void keyPressed(KeyEvent e) {
	} // keyPressed

	public void keyTyped(KeyEvent e) {
	} // keyTyped

	public void keyReleased(KeyEvent e) {

		if (e.getKeyCode() == 127) {        // event: delete object(s), key: del
			canvas.deleteObjects();
		} else if (e.getKeyCode() == 68) {   // event: clone object(s), key: ctrl+d
			canvas.cloneObject();
		} else if (e.getKeyCode() == 71) { // event: group objects, key: ctrl+g
			canvas.groupObjects();
		} else if (e.getKeyCode() == 85) { // event: ungroup objects, key: ctrl+u
			canvas.ungroupObjects();
		} else if (e.getKeyCode() == 39) { // event: move shape to right, key: right arrow key
			canvas.moveObject(1, 0);
		} else if (e.getKeyCode() == 37) { // event: move shape to left, key: left arrow key
			canvas.moveObject(-1, 0);
		} else if (e.getKeyCode() == 38) { // event: move shape up, key: up arrow key
			canvas.moveObject(0, -1);
		} else if (e.getKeyCode() == 40) { // event: move shape down, key: down arrow key
			canvas.moveObject(0, 1);
		} else if (e.getKeyCode() == 72) {// event: hilight object ports, key: ctrl+h
			canvas.hilightPorts();
		} else if (e.getKeyCode() == 82) {	// event: open object properties, key: ctrl+r
			canvas.openPropertiesDialog();
		} else if (e.getKeyCode() == 27) {	// event: escape key, return to selection
			canvas.stopRelationAdding();
		}


	} // keyReleased

}
