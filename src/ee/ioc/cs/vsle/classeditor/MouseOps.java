package ee.ioc.cs.vsle.classeditor;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JColorChooser;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import ee.ioc.cs.vsle.editor.RuntimeProperties;
import ee.ioc.cs.vsle.editor.State;
import ee.ioc.cs.vsle.graphics.Arc;
import ee.ioc.cs.vsle.graphics.BoundingBox;
import ee.ioc.cs.vsle.graphics.Line;
import ee.ioc.cs.vsle.graphics.Oval;
import ee.ioc.cs.vsle.graphics.Rect;
import ee.ioc.cs.vsle.graphics.Shape;
import ee.ioc.cs.vsle.graphics.Text;
import ee.ioc.cs.vsle.util.VMath;
import ee.ioc.cs.vsle.vclass.Connection;
import ee.ioc.cs.vsle.vclass.GObj;
import ee.ioc.cs.vsle.vclass.ObjectList;
import ee.ioc.cs.vsle.vclass.PackageClass;
import ee.ioc.cs.vsle.vclass.Point;
import ee.ioc.cs.vsle.vclass.Port;
import ee.ioc.cs.vsle.vclass.RelObj;

/**
 * Mouse operations on Canvas.
 */
public class MouseOps extends MouseInputAdapter {

	
    // Remove a dragged breakpoint on mouse button release when it is closer
    // to the line segment between neighbouring points than this threshold.
    private static double BP_REMOVE_THRESHOLD = 200d;

    String state = State.selection;
    int startX, startY;
    boolean mouseOver;

    public int arcWidth, arcHeight;
    public boolean fill = false;
    public float strokeWidth = 1.0f;
    public int transparency = 255;
    public int lineType = 0;
    
    public Color color = Color.black;
    boolean dragged = false;
    public int arcStartAngle;
    public int arcAngle;
    
    private Canvas canvas;
    private Point draggedBreakPoint;
    private Connection draggedBreakPointConn;
    private GObj draggedObject;
    private int cornerClicked;
    private Port currentPort;

    public MouseOps( Canvas e ) {
        this.canvas = e;
    }
    
    public int getTransparency() {
        return this.transparency;
    }

    public int getLineType() {
        return this.lineType;
    }    

    public void changeObjectColors( Color col ) {
    	ArrayList<GObj> selectedObjs = canvas.getScheme().getObjectList().getSelected();
    	for (GObj gObj : selectedObjs) {
    		for (Shape s : gObj.getShapes()) {
    			s.setColor( col );
    			canvas.drawingArea.repaint();
			}
		}
    } // change object colors
    
    public void setState( String state ) {
    	System.out.println("MouseOps setState " + state);
    	
        if (State.chooseColor.equals(state)) {
            Color col = JColorChooser.showDialog(ClassEditor.getInstance(), "Choose Color",
                    Color.black);

            // col is null when the dialog was cancelled or closed
            if (col != null) {
                this.color = col;
                changeObjectColors(col);
            }

            canvas.iconPalette.resetButtons();
            this.state = State.selection;
        }
        else {
	        if ( canvas.currentCon != null || canvas.getCurrentObj() != null ) {
	            canvas.cancelAdding();
	            if ( currentPort != null ) {
	                currentPort.setSelected( false );
	                currentPort = null;
	            }
	        }
	
	        assert currentPort == null;
	        assert canvas.currentCon == null;
	        assert canvas.getCurrentObj() == null;
	        assert canvas.currentPainter == null;
	
	        this.state = state;
        }
//        if ( State.addRelation.equals( state ) ) {
//            canvas.setCursor( Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR ) );
//        } else if ( State.selection.equals( state ) ) {
//            canvas.setCursor( Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR ) );
//            canvas.palette.resetButtons();
//        } else if ( State.isAddRelClass( state ) ) {
//            canvas.setCursor( Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR ) );
//        } else if ( State.isAddObject( state ) ) {
//            canvas.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
//            canvas.startAddingObject();
//        }
        
        if ( State.addRelation.equals( state ) ) {
            canvas.setCursor( Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR ) );
        } else if ( State.selection.equals( state ) ) {
            canvas.setCursor( Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR ) );
            canvas.iconPalette.resetButtons();
        } else if ( State.isAddRelClass( state ) ) {
            canvas.setCursor( Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR ) );
        } else if ( State.isAddObject( state ) ) {
            canvas.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
            canvas.startAddingObject();
        }        
    }

    private void openTextEditor( int x, int y ) {
    	System.out.println("ClassEditor.getInstance() " + ClassEditor.getInstance());
        new TextDialog( ClassEditor.getInstance(), x, y ).setVisible( true );
    } // openTextEditor
    
    
    /**
     * Draws port on the drawing area of the ClassEditor.
     * @param portName - name of the port.
     * @param isAreaConn - port is area connectible or not.
     * @param isStrict - port is strict or not.
     * @param portType - type of port: Integer, String, Object
     */
    public void drawPort( String portName, boolean isAreaConn, boolean isStrict, String portType, boolean isMulti ) {
        ArrayList<Port> ports = new ArrayList<Port>();
        String portConnection = null;
        if (isAreaConn) portConnection = "area";
        Port p = new Port(portName, portType, 0, 0, portConnection, isStrict, isMulti);
        ports.add(p);
        
        GObj obj = new GObj();
        p.setObject(obj);
        obj.setX(canvas.mouseX);
        obj.setY(canvas.mouseY);
        
        obj.setHeight(p.getHeight());
        obj.setWidth(p.getWidth());     
        obj.setName("port");            
        obj.setPorts(ports);
        System.out.println("GObj " + obj.toString());
        ObjectList objectList = canvas.getScheme().getObjectList();
        objectList.add(obj);
        canvas.getScheme().setObjects(objectList);  
        canvas.repaint();
    } // drawPort
    
    public void drawPort( Port p ) {
        ArrayList<Port> ports = new ArrayList<Port>();
        ports.add(p);
        
        GObj obj = new GObj();
        p.setObject(obj);
        obj.setX(canvas.mouseX);
        obj.setY(canvas.mouseY);
        
        obj.setHeight(p.getHeight());
        obj.setWidth(p.getWidth());     
        obj.setName("port");            
        obj.setPorts(ports);
        System.out.println("GObj " + obj.toString());
        ObjectList objectList = canvas.getScheme().getObjectList();
        objectList.add(obj);
        canvas.getScheme().setObjects(objectList);  
        canvas.repaint();
    } // drawPort    
     
    
    /**
     * Open the dialog for specifying port properties. Returns port to the
     * location the dialog was opened from. Dialog is modal and aligned
     * to the center of the open application window.
     */
    // TODO
    private void openPortPropertiesDialog() {
    	System.out.println("openPortPropertiesDialog");
        new PortPropertiesDialog( ClassEditor.getInstance(), null ).setVisible( true );
    } // openPortPropertiesDialog
    
    /**
     * Draws text on the drawing area of the IconEditor.
     * @param font Font - font used for drawing the text.
     * @param color Color - font color.
     * @param text String - the actual string of text drawn.
     */
    // TODO
    public void drawText( Font font, Color color, String text, int x, int y ) {
//        Shape shape = editor.getSelectedShape();
//        if ( shape != null && shape instanceof Text ) {
//            shape.setColor( color );
//            ( (Text) shape ).setFont( font );
//            ( (Text) shape ).setText( text );
//        } else {
//            Text t = new Text( x, y, font, Shape.createColorWithAlpha( color, getTransparency() ), text );
//            editor.shapeList.add( t );
//        }
        
        Text t = new Text( x, y, font, Shape.createColorWithAlpha( color, getTransparency() ), text );
        addShape(t);
        
        canvas.drawingArea.repaint();
    } // drawText   
    
    public void changeTransparency( int transparencyPercentage ) {
    	System.out.println("changeTransparency " + transparencyPercentage);
    	this.transparency = transparencyPercentage;
    	ArrayList<GObj> selectedObjs = canvas.getScheme().getObjectList().getSelected();
    	for (GObj gObj : selectedObjs) {
    		for (Shape s : gObj.getShapes()) {
    			s.setColor( Shape.createColorWithAlpha( s.getColor(), transparency ) );
    			canvas.drawingArea.repaint();
			}
		}
    }

    /**
     * Change line type of the selected shape(s).
     * @param lineType - selected line type icon name.
     */
    public void changeLineType( int lineType ) {
    	System.out.println("changeLineType " + lineType);
    	this.lineType = lineType;
    	ArrayList<GObj> selectedObjs = canvas.getScheme().getObjectList().getSelected();
    	for (GObj gObj : selectedObjs) {
    		for (Shape s : gObj.getShapes()) {
    			s.setLineType( lineType );
    			canvas.drawingArea.repaint();
			}
		}
    } // changeLineType

    /**
    * Change the stroke with of the selected shape(s).
    * @param strokeW double - stroke width selected from the spinner.
    */
    public void changeStrokeWidth( float strokeW ) {
    	System.out.println("changeStrokeWidth " + strokeW);
    	this.strokeWidth = strokeW;
    	ArrayList<GObj> selectedObjs = canvas.getScheme().getObjectList().getSelected();
    	for (GObj gObj : selectedObjs) {
    		for (Shape s : gObj.getShapes()) {
    			s.setStrokeWidth( strokeWidth );
    			canvas.drawingArea.repaint();
			}
		}    	
    } // changeStrokeWidth    
    
    public void addShape(Shape s) {
        ArrayList<Shape> shapes = new ArrayList<Shape>();
        shapes.add(s);
        GObj obj = new GObj();
        System.out.println("shape " + s.toText());
        System.out.println("addShape x " + s.getX() + " y " + s.getY() + " h " + s.getHeight() + " w " + s.getWidth());
        obj.setX(s.getX());
        obj.setY(s.getY());
        // TODO this is a hack to get GObj and Shape to play nice with each other
        s.setX(0);
        s.setY(0);
        System.out.println("shape " + s.toText());
        obj.setHeight(s.getHeight());
        obj.setWidth(s.getWidth());     
        obj.setName(s.getClass().getName());
        obj.setShapes(shapes);
        System.out.println("GObj shape " + obj.toString());
        ObjectList objectList = canvas.getScheme().getObjectList();
        objectList.add(obj);
        canvas.getScheme().setObjects(objectList);    	
    }
    /**
     * Mouse entered event from the MouseMotionListener. Invoked when the mouse
     * enters a component.
     * 
     * @param e MouseEvent - Mouse event performed.
     */
    @Override
    public void mouseEntered( MouseEvent e ) {
        mouseOver = true;
    }

    /**
     * Mouse exited event from the MouseMotionListener. Invoked when the mouse
     * exits a component.
     * 
     * @param e MouseEvent - Mouse event performed.
     */
    @Override
    public void mouseExited( MouseEvent e ) {
        mouseOver = false;
        canvas.drawingArea.repaint();
    }

    private void openObjectPopupMenu( GObj obj, int x, int y ) {
        ObjectPopupMenu popupMenu = new ObjectPopupMenu( obj, canvas );
        popupMenu.show( canvas, x, y );
    }

    /**
     * Mouse clicked event from the MouseListener. Invoked when the mouse button
     * has been clicked (pressed and released) on a component.
     * @param e MouseEvent - Mouse event performed. In the method a distinction
     *                       is made between left and right mouse clicks.
     */
    @Override
    public void mouseClicked( MouseEvent e ) {
    	System.out.println("IconMouseOps mouseClicked: " + state );
        int x, y;
        x = e.getX();
        y = e.getY();

        if ( state.equals( State.drawArc1 ) ) {
            setState( State.drawArc2 );
            double legOpp = startY + arcHeight / 2 - y;
            double legNear = x - ( startX + arcWidth / 2 );
            arcStartAngle = (int) ( Math.atan( legOpp / legNear ) * 180 / Math.PI );
            if ( legNear < 0 )
                arcStartAngle = arcStartAngle + 180;
            if ( legNear > 0 )
                arcStartAngle = arcStartAngle + 360;
            if ( arcStartAngle > 360 )
                arcStartAngle = arcStartAngle - 360;
            return;
        }
        if ( state.equals( State.drawArc2 ) ) {
            Arc arc = new Arc( startX, startY, arcWidth, arcHeight, arcStartAngle, arcAngle, 
                    Shape.createColorWithAlpha( color, getTransparency() ), fill, strokeWidth, lineType );
            addShape( arc );
            setState( State.selection );
        }
        // LISTEN RIGHT MOUSE BUTTON
        if ( SwingUtilities.isRightMouseButton( e ) ) {
            // TODO
//        	popupMenuListener( x, y );
        	
            GObj obj = canvas.getObjectList().checkInside( x, y );
//            if ( obj != null || canvas.getObjectList().getSelectedCount() > 1  ) {
//            	if (obj.getPorts() != null) {
//            		openPortPopupMenu( p, x, y );
//            	} else {
//            		openObjectPopupMenu( obj, e.getX() + canvas.drawingArea.getX(), e.getY() + canvas.drawingArea.getY() );
//            	}
//            }
            if ( obj != null || canvas.getObjectList().getSelectedCount() > 1 ) {
                openObjectPopupMenu( obj, e.getX() + canvas.drawingArea.getX(), e.getY() + canvas.drawingArea.getY() );
            }        	
        } // END OF LISTENING RIGHT MOUSE BUTTON
        else {
            if ( state.equals( State.addRelation ) ) {
                // **********Relation adding code**************************
                Port port = canvas.getObjectList().getPort( x, y );
                if ( port != null ) {
                    if ( canvas.currentCon == null ) {
                        if ( port.canBeConnected() )
                            canvas.startAddingConnection( port );
                    } else {
                        Port firstPort = canvas.currentCon.getBeginPort();
                        if ( port.canBeConnectedTo( firstPort ) ) {
                            if ( port == firstPort ) {
                                // Connecting a port to itself does not make
                                // any sense, so do not allow it.
                                canvas.cancelAdding();
                            } else {
                                canvas.addCurrentConnection( port );
                            }
                        }
                    }
                } else {
                    // double click on the background cancels relation adding,
                    // one click on the background adds a new breakpoint.
                    if ( canvas.currentCon != null ) {
                        if ( e.getClickCount() == 2 )
                            canvas.cancelAdding();
                        else
                            canvas.currentCon.addBreakPoint( new Point( x, y ) );
                    }
                }

            } else if ( state.equals( State.selection ) ) {
                // **********Selecting objects code*********************
            	System.out.println("HERE Selecting objects code");
                if ( !e.isShiftDown() ) {
                    canvas.getObjectList().clearSelected();
                    canvas.getConnections().clearSelected();
                }

                Connection con = canvas.getConnectionNearPoint( x, y );

                if ( con != null ) {
                    con.setSelected( true );
                } else {
                	System.out.println("x, y " + x + ", "+y +"canvas.getObjectList() " + canvas.getObjectList());
                    GObj obj = canvas.getObjectList().checkInside(x, y);
                    System.out.println("HERE Selecting objects code GObj obj " + obj);
                    if ( obj != null ) {
                        obj.setSelected( true );
                        // FIXME
//                        if (SwingUtilities.isLeftMouseButton(e)
//                                && e.getClickCount() >= 2)
//                            canvas.openPropertiesDialog(obj);
//                        else 
                        if (SwingUtilities.isMiddleMouseButton(e))
                            canvas.openClassCodeViewer(obj.getClassName());
                        else
                            canvas.setCurrentObj( obj );
                    }
                }

            } else {
                if ( State.isAddRelClass( state ) ) {
                    Port port = canvas.getObjectList().getPort(x, y);

                    if ( port != null ) {
                        PackageClass obj = canvas.getPackage().getClass( State.getClassName( state ) );

                        // Relation classes must have exactly 2 ports. Although
                        // package parser should have already catched this an
                        // additional check does not hurt.
                        if ( obj.getPorts().size() != 2 )
                            return;

                        if ( canvas.getCurrentObj() == null ) {
                            if ( port.canBeConnectedTo( obj.getPorts().get( 0 ) ) )
                                canvas.startAddingRelObject( port );
                        } else {
                            if ( port.canBeConnectedTo( obj.getPorts().get( 1 ) ) )
                                canvas.addCurrentObject( port );
                        }
                    }
                } else if ( canvas.getCurrentObj() != null ) {
                    canvas.addCurrentObject();
                    setState( State.selection );
                }
            }
        } // END OF LISTENING LEFT MOUSE BUTTON

        canvas.drawingArea.repaint();
    }
    

    @Override
    public void mousePressed( MouseEvent e ) {
    	System.out.println("MouseOps mousePressed " + state);
    	
        if ( !( state.equals( State.drawArc1 ) || state.equals( State.drawArc2 ) ) ) {
            startX = e.getX();
            startY = e.getY();
        }
        
        if ( state.equals( State.selection ) ) {
            GObj obj = null;
            canvas.mouseX = Math.round( e.getX() / canvas.getScale() );
            canvas.mouseY = Math.round( e.getY() / canvas.getScale() );
            Connection con = canvas.getConnectionNearPoint( canvas.mouseX, canvas.mouseY );

            if ( con != null ) {
                Point bp = con.breakPointContains(canvas.mouseX, canvas.mouseY);
                if (bp != null) {
                    startBreakPointDrag(con, bp);
                } else {
                    obj = canvas.getObjectList().checkInside(canvas.mouseX, canvas.mouseY);
                }
            } else {
                obj = canvas.getObjectList().checkInside(canvas.mouseX, canvas.mouseY);
            }

            if ( obj != null ) {
                if ( e.isShiftDown() ) {
                    obj.setSelected( true );
                } else {
                    if ( !obj.isSelected() ) {
                        canvas.getObjectList().clearSelected();
                        obj.setSelected( true );
                    }
                }
                if ( SwingUtilities.isLeftMouseButton( e ) ) {
                    setState( State.drag );
                    draggedObject = obj;
                }
                canvas.drawingArea.repaint();
            } else if ( con == null ) {
                cornerClicked = canvas.getObjectList().controlRectContains(
                        canvas.mouseX, canvas.mouseY);
                if ( cornerClicked != 0 ) {
                    setState( State.resize );
                } else {
                    setState( State.dragBox );
                    startX = canvas.mouseX;
                    startY = canvas.mouseY;
                }
            }
        }
        System.out.println("mousePressed startX, startY : " + startX + ", " +startY);
        canvas.setActionInProgress( true );
    }

    @Override
    public void mouseDragged( MouseEvent e ) {
    	System.out.println("MouseOps mouseDragged " + state);
        if ( !SwingUtilities.isLeftMouseButton( e ) ) {
            return;
        }
        int x = Math.round( e.getX() / canvas.getScale() );
        int y = Math.round( e.getY() / canvas.getScale() );

        canvas.setPosInfo( x, y );

        if ( State.dragBreakPoint.equals( state ) ) {
            if ( RuntimeProperties.getSnapToGrid() ) {
                draggedBreakPoint.x = Math.round( (float) x / RuntimeProperties.getGridStep() ) * RuntimeProperties.getGridStep();
                draggedBreakPoint.y = Math.round( (float) y / RuntimeProperties.getGridStep() ) * RuntimeProperties.getGridStep();
            } else {
                draggedBreakPoint.x = x;
                draggedBreakPoint.y = y;
            }
        } else if ( State.drag.equals( state ) ) {
            int moveX, moveY;

            if ( RuntimeProperties.getSnapToGrid() ) {
                GObj obj = draggedObject;
                int step = RuntimeProperties.getGridStep();

                // When snap to grid is on mouse coordinates are calculated
                // as if the mouse jumped from one grid line to the next.
                // The dragged object's top left corner is always at a grid
                // line intersection point. The relative positions of the
                // dragged class and other selected objects should be constant.
                moveX = Math.round( (float) ( obj.getX() + Math.round( (float) x / step ) * step - canvas.mouseX ) / step )
                        * step - obj.getX();
                moveY = Math.round( (float) ( obj.getY() + Math.round( (float) y / step ) * step - canvas.mouseY ) / step )
                        * step - obj.getY();
            } else {
                moveX = x - canvas.mouseX;
                moveY = y - canvas.mouseY;
            }

            // If there are strict ports being moved find the first one that
            // would create a new connection and snap it to the other port.
            ArrayList<GObj> selected = canvas.getObjectList().getSelected();
            SNAP: for ( GObj obj : selected ) {
                if ( obj.isStrict() ) {
                    for ( Port port1 : obj.getPortList() ) {
                        if ( port1.isStrict() ) {
                            Point p1 = obj.toCanvasSpace( port1.getRealCenterX(), port1.getRealCenterY() );
                            Port port2 = canvas.getObjectList().getPort(
                                    p1.x + moveX, p1.y + moveY, obj);

                            if ( port2 != null && !selected.contains( port2.getObject() ) && port1.canBeConnectedTo( port2 )
                                    && ( !port1.isConnectedTo( port2 ) || port1.isStrictConnected() ) ) {

                                Point p2 = port2.getObject().toCanvasSpace( port2.getRealCenterX(), port2.getRealCenterY() );
                                
                                moveX = p2.x - p1.x;

                                moveY = p2.y - p1.y;

                                break SNAP;
                            }
                        }
                    }
                }
            }

            canvas.moveObjects( moveX, moveY );
            canvas.mouseX += moveX;
            canvas.mouseY += moveY;
        } else if ( State.resize.equals( state ) ) {
            // Do not allow resizing of strictly connected objects as it
            // seems to be not very useful and creates problems such as
            // the connected ports could get misplaced and should be
            // disconnected but maybe that is not what the user is expecting.
            // Until this operation is proven necessary and is clearly specified
            // it is better to deny it.
            if ( !draggedObject.isStrictConnected() ) {
                int moveX = x - canvas.mouseX;
                int moveY = y - canvas.mouseY;
                canvas.resizeObjects( moveX, moveY, cornerClicked );
                canvas.mouseX += moveX;
                canvas.mouseY += moveY;
            }
        } else if ( State.dragBox.equals( state ) ) {
            canvas.mouseX = x;
            canvas.mouseY = y;
        } else if ( state.equals( State.drawLine ) ) {
            canvas.mouseX = x;
            canvas.mouseY = y;
        } else if ( state.equals( State.drawArc ) || state.equals( State.drawFilledArc ) ) {
            fill = false;
            if ( state.equals( State.drawFilledArc ) ) {
                fill = true;
            }
            canvas.mouseX = x;
            canvas.mouseY = y;
        } else if ( state.equals( State.drawText ) ) {
            startX = x;
            startY = y;
        } else if ( state.equals( State.addPort ) ) {
            startX = x;
            startY = y;
        } else if ( state.equals( State.insertImage ) ) {
            startX = x;
            startY = y;
        } else if ( state.equals( State.drawRect ) || state.equals( State.drawFilledRect ) || state.equals( State.boundingbox ) ) {
            fill = false;
            if ( state.equals( State.drawFilledRect ) ) {
                fill = true;
            }
            canvas.mouseX = x;
            canvas.mouseY = y;
        } else if ( state.equals( State.drawOval ) || state.equals( State.drawFilledOval ) ) {
            fill = false;
            if ( state.equals( State.drawFilledOval ) ) {
                fill = true;
            }
            canvas.mouseX = x;
            canvas.mouseY = y;
        } else {
            Connection c = canvas.getConnectionNearPoint(x, y);
            if (c != null) {
                canvas.getConnections().clearSelected();
                c.setSelected(true);
                draggedBreakPoint = new Point(x, y);
                c.addBreakPoint(c.indexOf(x, y), draggedBreakPoint);
                setState(State.dragBreakPoint);
            }
        }
        
        //System.out.println("MouseOps mouseDragged canvas.mouseX, canvas.mouseY " + canvas.mouseX + ", " + canvas.mouseY);
        canvas.drawingArea.repaint();
    }

    @Override
    public void mouseMoved( MouseEvent e ) {
    	//System.out.println("MouseOps mouseMoved " + state);
        int x = Math.round( e.getX() / canvas.getScale() );
        int y = Math.round( e.getY() / canvas.getScale() );

        canvas.setPosInfo( x, y );
        
        if ( state.equals( State.drawArc2 ) ) {
            double legOpp = startY + arcHeight / 2 - y;
            double legNear = x - ( startX + arcWidth / 2 );
            arcAngle = (int) ( Math.atan( legOpp / legNear ) * 180 / Math.PI ) - arcStartAngle;
            if ( legNear < 0 )
                arcAngle = arcAngle + 180;
            if ( legNear > 0 )
                arcAngle = arcAngle + 360;
            if ( arcAngle > 360 )
                arcAngle = arcAngle - 360;
            if ( arcAngle < 0 ) {
                arcAngle += 360;

            }
        }        

        // Check if port needs to be hilighted because of mouseover.
        // A repaint() is always necessary when adding connections and the
        // mouse has moved because the disconnected end has to follow the mouse.
        if ( state.equals( State.addRelation ) ) {
            updateConnectionPortHilight( x, y );
            canvas.drawingArea.repaint();
        } else if ( State.isAddRelClass( state ) ) {
            updateRelClassPortHilight( x, y );
            canvas.drawingArea.repaint();
        } else if ( State.isAddObject( state ) && canvas.getCurrentObj() != null ) {
            // if we're adding a new object...

            if ( RuntimeProperties.getSnapToGrid() ) {
                canvas.getCurrentObj().setX( Math.round( x / RuntimeProperties.getGridStep() ) * RuntimeProperties.getGridStep() );
                canvas.getCurrentObj().setY( Math.round( y / RuntimeProperties.getGridStep() ) * RuntimeProperties.getGridStep() );
            } else {
                canvas.getCurrentObj().setY( y );
                canvas.getCurrentObj().setX( x );
            }

            if ( canvas.getCurrentObj().isStrict() )
                updateStrictPortHilight();

            Rectangle rect = new Rectangle( e.getX() - 10, e.getY() - 10, Math.round( canvas.getCurrentObj().getRealWidth()
                    * canvas.getScale() ) + 10, Math.round( ( canvas.getCurrentObj().getRealHeight() * canvas.getScale() ) + 10 ) );

            canvas.drawingArea.scrollRectToVisible( rect );

            if ( e.getX() + canvas.getCurrentObj().getRealWidth() > canvas.drawAreaSize.width ) {

                canvas.drawAreaSize.width = e.getX() + canvas.getCurrentObj().getRealWidth();

                canvas.drawingArea.setPreferredSize( canvas.drawAreaSize );
                canvas.drawingArea.revalidate();
            }

            if ( e.getY() + canvas.getCurrentObj().getRealHeight() > canvas.drawAreaSize.height ) {

                canvas.drawAreaSize.height = e.getY() + canvas.getCurrentObj().getRealHeight();

                canvas.drawingArea.setPreferredSize( canvas.drawAreaSize );
                canvas.drawingArea.revalidate();
            }

            canvas.drawingArea.repaint();
        }

        canvas.mouseX = x;
        canvas.mouseY = y;
    }

    /**
     * Hilights the port under the mouse cursor when adding a relation class and
     * the port of the relation class can be connected to the mouseover port.
     * 
     * @param x the X coordinate of the mouse cursor
     * @param y the Y coordinate of the mouse cursor
     */
    private void updateRelClassPortHilight( int x, int y ) {
        Port port = canvas.getObjectList().getPort(x, y);
        RelObj curObj = (RelObj) canvas.getCurrentObj();

        if ( currentPort != null && currentPort != port ) {
            if ( curObj == null || curObj.getStartPort() != currentPort )
                currentPort.setSelected( false );

            currentPort = null;
        }

        if ( port != null && currentPort == null ) {
            if ( curObj != null ) {
                // hilight the second port only if its type is compatible
                // with the first already connected port
                if ( port.canBeConnectedTo( curObj.getStartPort() ) ) {
                    port.setSelected( true );
                    currentPort = port;
                }
            } else {
                port.setSelected( true );
                currentPort = port;
            }
        }
    }

    /**
     * Hilights the port the connection could be attached to.
     * 
     * @see #updateRelClassPortHilight(int, int)
     * @param x the X coordinate of the mouse cursor
     * @param y the Y coordinate of the mouse cursor
     */
    /*
     * The logic for adding connections and relation classes is quite similar,
     * is is possible to generalise and merge these methods?
     */
    private void updateConnectionPortHilight( int x, int y ) {
        Port port = canvas.getObjectList().getPort(x, y);

        if ( currentPort != null && currentPort != port ) {
            if ( canvas.currentCon == null || canvas.currentCon.getBeginPort() != currentPort ) {
                currentPort.setSelected( false );
            }
            currentPort = null;
        }

        if ( port != null && currentPort == null ) {
            if ( canvas.currentCon != null ) {
                // hilight the second port only if its type is compatible
                // with the first already connected port
                Port firstPort = canvas.currentCon.getBeginPort();
                if ( port.canBeConnectedTo( firstPort ) ) {
                    port.setSelected( true );
                    currentPort = port;
                }
            } else if ( port.canBeConnected() ) {
                port.setSelected( true );
                currentPort = port;
            }
        }
    }

    /**
     * Hilights (selects) strict ports of the current object (the object being
     * added or moved) that are about to be strictly connected at the current
     * location.
     */
    private void updateStrictPortHilight() {
        for ( Port port : canvas.getCurrentObj().getPortList() ) {
            port.setSelected( false );

            Port port2 = canvas.getObjectList().getPort(
                    port.getRealCenterX(), port.getRealCenterY());

            if ( port2 != null && port2.isStrict() && !port2.isStrictConnected() && port.canBeConnectedTo( port2 ) ) {

                // Maybe there is a huge strict port which contains more than
                // one of currentObj's ports. In this case only the first
                // port will be connected an the others should not be
                // hilighted.
                boolean ignore = false;
                for ( Port p : canvas.getCurrentObj().getPortList() ) {
                    if (p.isSelected() && canvas.getObjectList().getPort(
                            p.getRealCenterX(), p.getRealCenterY()) == port2) {
                        ignore = true;
                        break;
                    }

                }

                if ( !ignore ) {
                    port.setSelected( true );
                    canvas.getCurrentObj().setX( canvas.getCurrentObj().getX()
                            + ( port2.getRealCenterX() - port.getRealCenterX() ) );
                    canvas.getCurrentObj().setY( canvas.getCurrentObj().getY()
                            + ( port2.getRealCenterY() - port.getRealCenterY() ) );
                }
            }
        }
    }

    @Override
    public void mouseReleased( MouseEvent e ) {
    	System.out.println("MouseOps mouseReleased: " + state);
        if ( state.equals( State.dragBreakPoint ) ) {
            endBreakPointDrag();
        } else if ( state.equals( State.drag ) ) {
            if ( !SwingUtilities.isLeftMouseButton( e ) )
                return;
            state = State.selection;
        } else if ( state.equals( State.resize ) ) {
            state = State.selection;
        } else if ( state.equals( State.dragBox ) ) {
            int x1 = Math.min( startX, canvas.mouseX );
            int x2 = Math.max( startX, canvas.mouseX );
            int y1 = Math.min( startY, canvas.mouseY );
            int y2 = Math.max( startY, canvas.mouseY );
            canvas.getObjectList().selectObjectsInsideBox(
                    x1, y1, x2, y2, e.isShiftDown());
            state = State.selection;
            canvas.drawingArea.repaint();
        }

        if ( state != null && state.equals( State.drawRect ) || state.equals( State.drawFilledRect )
                || state.equals( State.boundingbox ) ) {
            final int width = Math.abs( canvas.mouseX - startX );
            final int height = Math.abs( canvas.mouseY - startY );

            if ( state.equals( State.boundingbox ) ) {
                BoundingBox box = new BoundingBox( Math.min( startX, canvas.mouseX ), Math.min( startY, canvas.mouseY ), width, height );
                // TODO
//                editor.boundingbox = box;
                // ONLY ONE BOUNDING BOX IS ALLOWED.
                addShape(box);
                canvas.iconPalette.boundingbox.setEnabled( false );
                state = State.selection;
            } else {
	            System.out.println("startX, startY " + startX + ", " + this.startY);
	            System.out.println("canvas.mouseX, canvas.mouseY " + canvas.mouseX + ", " + canvas.mouseY);
	            System.out.println(" Math.min( startX, canvas.mouseX ) " +  Math.min( startX, canvas.mouseX ));
	            System.out.println(" Math.min( startY, canvas.mouseY ) " + Math.min( startY, canvas.mouseY ));
	            System.out.println("width, height " + width + "," + height);
	            System.out.println("fill, strokeWidth, lineType " + this.fill + ", " + this.strokeWidth + ", " + this.lineType);
	            Rect rect = new Rect( Math.min( startX, canvas.mouseX ), Math.min( startY, canvas.mouseY ), width, height, 
	                    Shape.createColorWithAlpha( color, getTransparency() ), fill, strokeWidth, lineType );
	            addShape(rect);
            }
            canvas.drawingArea.repaint();
        } else if ( state.equals( State.drawOval ) || state.equals( State.drawFilledOval ) ) {
            int width = Math.abs( canvas.mouseX - startX );
            int height = Math.abs( canvas.mouseY - startY );
            Oval oval = new Oval( Math.min( startX, canvas.mouseX ), Math.min( startY, canvas.mouseY ), width, height, 
                    Shape.createColorWithAlpha( color, getTransparency() ),
                    fill, strokeWidth, lineType );
            addShape(oval);
            canvas.drawingArea.repaint();
        } else if ( state.equals( State.drawArc ) || state.equals( State.drawFilledArc ) ) {
            arcWidth = Math.abs( canvas.mouseX - startX );
            arcHeight = Math.abs( canvas.mouseY - startY );
            setState( State.drawArc1 );
        } else if ( state.equals( State.drawLine ) ) {
            System.out.println("startX, startY " + startX + ", " + this.startY);
            System.out.println("canvas.mouseX, canvas.mouseY " + canvas.mouseX + ", " + canvas.mouseY);
//            System.out.println(" Math.min( startX, canvas.mouseX ) " +  Math.min( startX, canvas.mouseX ));
//            System.out.println(" Math.min( startY, canvas.mouseY ) " + Math.min( startY, canvas.mouseY ));
            System.out.println("fill, strokeWidth, lineType " + this.fill + ", " + this.strokeWidth + ", " + this.lineType);
            
            Line line = new Line( startX, startY, canvas.mouseX, canvas.mouseY, 
                    Shape.createColorWithAlpha( color, getTransparency() ), strokeWidth, lineType );
            addShape( line );
            canvas.drawingArea.repaint();
        } else if ( state.equals( State.resize ) ) {
            state = State.selection;
        } else if ( state.equals( State.freehand ) ) {
        	// TODO
//        	drawDotOnClick( color );
        } else if ( state.equals( State.eraser ) ) {
        	// select obj and delete
        	GObj obj = canvas.getObjectList().checkInside(canvas.mouseX, canvas.mouseY); 
        	if (obj != null) {
        		obj.setSelected(true);
        		// TODO check for bounding box
        		canvas.deleteSelectedObjects();
        	}
            canvas.drawingArea.repaint();
        } else if ( state.equals( State.drawText ) ) {
            openTextEditor( canvas.mouseX, canvas.mouseY );
        } else if ( state.equals( State.addPort ) ) {
        	// TODO
            openPortPropertiesDialog();
        } else if ( state.equals( State.insertImage ) ) {
            openImageDialog();
        }
        
        List<GObj> selected = canvas.getObjectList().getSelected();
        if ( selected != null && selected.size() > 0 )
            canvas.setStatusBarText( "Selection: " + selected.toString() );

        canvas.setActionInProgress( false );
    }
    
    /**
     * Open the image dialog. Returns
     * image to the location the dialog was opened from. Dialog
     * is modal and aligned to the center of the open application window.
     */
 // TODO
    private void openImageDialog() {
        if ( ClassEditor.getInstance().checkPackage() )
            new ImageDialog( ClassEditor.getInstance(), null ).setVisible( true );
    } // openTextEditor    

    private void startBreakPointDrag(Connection con, Point bp) {
        draggedBreakPoint = bp;
        draggedBreakPointConn = con;
        setState(State.dragBreakPoint);
        con.setSelected(true);
        canvas.drawingArea.repaint();
    }

    private void endBreakPointDrag() {
        setState(State.selection);

        // Remove the dragged breakpoint if it is on a straight line
        // or close to another breakpoint or endpoint.
        if (draggedBreakPoint != null && draggedBreakPointConn != null) {
            ArrayList<Point> ps = draggedBreakPointConn.getBreakPoints();
            assert ps != null;
            int n = ps.indexOf(draggedBreakPoint);
            assert n >= 0 && n < ps.size();

            // Find neighbour anchor points, could be breakpoints or ports
            Point p1, p2;
            if (n == 0) {
                p1 = draggedBreakPointConn.getBeginPort().getAbsoluteCenter();
            } else {
                p1 = ps.get(n - 1);
            }
            if (n == ps.size() - 1) {
                p2 = draggedBreakPointConn.getEndPort().getAbsoluteCenter();
            } else {
                p2 = ps.get(n + 1);
            }

            // Remove the dragged breakpoint if it lies on an almost
            // straight line.
            double d = (draggedBreakPoint.x - p1.x) * (p2.y - p1.y)
                     - (draggedBreakPoint.y - p1.y) * (p2.x - p1.x);

            if (Math.abs(d) < BP_REMOVE_THRESHOLD) {
                ps.remove(draggedBreakPoint);
                draggedBreakPoint = null;
            }

            // Remove the breakpoint if it is close to another breakpoint
            if (draggedBreakPoint != null) {
                d = VMath.distanceBetweenPoints(p2, draggedBreakPoint);
                if (d < Connection.NEAR_DISTANCE * 3) {
                    draggedBreakPointConn.removeBreakPoint(n);
                    draggedBreakPoint = null;
                }
            }
            if (draggedBreakPoint != null) {
                d = VMath.distanceBetweenPoints(p1, draggedBreakPoint);
                if (d < Connection.NEAR_DISTANCE * 3) {
                    draggedBreakPointConn.removeBreakPoint(n);
                    draggedBreakPoint = null;
                }
            }

            // a breakpoint was removed, redrawing is needed
            if (draggedBreakPoint == null) {
                canvas.drawingArea.repaint();
            }
        }
        draggedBreakPoint = null;
        draggedBreakPointConn = null;
    }
    
    void destroy() {
        
        canvas = null;
        draggedBreakPoint = null;
        draggedBreakPointConn = null;
        draggedObject = null;
        currentPort = null;
    }
}
