import java.util.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.*;

/**
 * Class Mintris - this program creates a simpler version of the classic
 * Tetris game.  Mintris is turn-based and has only 2x2 pieces.
 * 
 * @author Andrew Nuxoll
 * @author Karen Ward  
 * @author Garrett Becker
 * 
 * @version 12 Feb 2014 :AMN: original
 * @version 18 Feb 2014 kw changed X/Y references to row/col convention; 
 *                         made randGen, blockColors private;
 *                         blockColors using defined constants;
 *                         corrected minor typos in comments, imports;
 *                         use array lengths in paint()
 * @version 20 Feb 2014 kw Corrected bug: testing EMPTY for invalid color
 * @version 27 Feb 2014 SM Assignment finished
 */
public class Mintris extends JPanel implements KeyListener
{

    /*======================================================================
     * Constants
     *----------------------------------------------------------------------
     */

    public static final int NUM_ROWS = 20;    //number of rows in the playing field
    public static final int NUM_COLS = 10;    //number of columns in the playing field
    public static final int BLOCK_SIZE = 20;  //a block is this many pixels on a side

    //These constants define the possible contents of each cell in the playing field
    public static final int NUM_COLORS    = 3;
    public static final int INVALID_COLOR = 0;
    public static final int RED_BLOCK     = 1;
    public static final int GREEN_BLOCK   = 2;
    public static final int BLUE_BLOCK    = 3;
    public static final int EMPTY         = NUM_COLORS + 1;

    //movement of the blocks on the playing field can be in one of these
    //directions
    public static final int LEFT         = -1;
    public static final int DOWN         =  0;
    public static final int RIGHT        =  1;

    /*======================================================================
     * Instance Variables
     *----------------------------------------------------------------------
     */
    // a 2D array to store the playing field
    private int[][] field = new int[NUM_ROWS][NUM_COLS];

    // current score
    private int score = 0;      

    /*======================================================================
     * Methods
     *----------------------------------------------------------------------
     */

    /**
     * clearField
     *
     * Creates a new playing field sets all cells in the field to EMPTY.
     *
     */
    public void clearField()
    {
        //for all [i][j] coordinates in the array, set value in those coordinates to EMPTY
        for (int i = 0; i < field.length; ++i)
        {
            for (int j = 0; j < field[i].length; ++j) 
            {
                field[i][j] = EMPTY;
            }
        }
    }//clearField

    /**
     * rotate
     *
     * This method rotates a 2x2 block by 90 degrees. 
     * It does not check that the rotation is valid.  
     *
     * @param row the row of the upper-left corner of the 2x2 block
     * @param col the column of the upper-left block
     *
     */
    public void rotate(int row, int col)
    {
        int temp = field[row][col]; //create a temp to house the replaced field[row][col]
        //rotate 90 degrees by moving blocks accordingly
        field[row][col] = field[row][col+1];
        field[row][col+1] = field[row+1][col+1];
        field[row+1][col+1] = field[row+1][col];
        field[row+1][col] = temp;

    }//rotate

    /**
     * moveFaceBlock
     *
     * Move face block (if filled) from one position to another depending on parameters set.
     * Don't move if empty.
     * 
     * @param rowFrom the row of the upper-left corner of the block before
     * @param colFrom the column of the upper-left corner of the block before
     * @param rowTo the row of the upper-left corner of the block after
     * @param colTo the column of the upper-left corner of the block after
     */
    public void moveFaceBlock(int rowFrom, int colFrom, int rowTo, int colTo)
    {
        if (field[rowFrom][colFrom] != EMPTY)
        {
            field[rowTo][colTo] = field[rowFrom][colFrom];
        }
        //if block is empty, don't move that block
    }

    /**
     * move
     *
     * This method moves a 2x2 piece by one space (left, right, or down)
     * This method does *not* check to make sure that the movement is valid; 
     * it is up to the caller to verify the move before calling move.
     *
     * The block is moved as would be expected for a Tetris game.
     * Specifically, if an empty space moves onto a block, then the block remains
     * and is not replaced by the empty space.
     *
     * @param row the row of the upper-left corner of the block
     * @param col the column of the upper-left corner of the block
     * @param direction the direction to move (LEFT, DOWN, or RIGHT)
     *        (see the constants defined at the top of this class)
     *
     */
    public void move(int row, int col, int direction)
    {
        if (direction == LEFT)
        {
            //set parameters so the front face moves left and then the back face
            moveFaceBlock(row,col,row,col-1);
            moveFaceBlock(row + 1,col,row + 1,col-1);
            field[row+1][col] = field[row+1][col+1];
            field[row][col] = field[row][col+1];
            //Empty blocks behind back plate
            field[row][col+1] = EMPTY;
            field[row+1][col+1] = EMPTY;
        }
        if (direction == DOWN)
        {
            //set parameters so the front face moves down and then the back face
            moveFaceBlock(row + 1,col,row + 2,col);
            moveFaceBlock(row + 1,col + 1,row + 2,col+1);
            field[row+1][col+1] = field[row][col+1];
            field[row+1][col] = field[row][col];
            //Empty blocks behind back plate
            field[row][col] = EMPTY;
            field[row][col+1] = EMPTY;
        }
        if (direction == RIGHT)
        {
            //set parameters so the front face moves right and then the back face
            moveFaceBlock(row,col+1,row,col+2);
            moveFaceBlock(row + 1,col+1,row + 1,col+2);
            field[row][col+1] = field[row][col];
            field[row+1][col+1] = field[row+1][col];
            //Empty blocks behind back plate
            field[row][col] = EMPTY;
            field[row+1][col] = EMPTY;
        }
        removeRows(); //checks if more than 1 row is needed for deletion

    }//move

    /**
     * validMove
     *
     * This method calculates whether a block may be moved one space in a particular
     * direction: left, right or down.  A move is invalid if there is another block in
     * the target location, or if the movement would take the block off of the
     * playing field
     *
     * @param row the row of the upper-left corner of the block
     * @param col the column of the upper-left corner of the block 
     * @param direction direction to move (LEFT, RIGHT, or DOWN)
     *              (see the constants defined at the top of this class)
     *
     * @return    true if the movement is legal and false otherwise
     * 
     * known weakness: assumes that given initial position is valid
     */
    private boolean validMove(int row, int col, int direction)
    {
        //keeps blocks within boundarys
        if ((col <= 0) && ((direction == LEFT)))
        {
            return false; 
        }
        else if ((col > field[0].length - 3) && (direction == RIGHT))
        {
            return false; 
        }
        else if ((row > field.length - 3) && (direction == DOWN))
        {
            return false; 
        }

        //returns false if block goes down and hits other blocks
        else if ((field[row + 1][col] != EMPTY) && (field[row][col] != EMPTY)
        && (field[row + 1][col+1] != EMPTY) && (field[row][col+1] != EMPTY))
        {
            return false;
        }

        else if ((field[row + 2][col] != EMPTY) && (field[row + 1][col] != EMPTY))
        {
            return false;
        }
        else if ((field[row + 1][col + 1] != EMPTY) && (field[row + 2][col+1] != EMPTY))
        {
            return false;
        }

        //returns false if block goes to the right and hits blocks
        else if (direction == RIGHT)
        {
            if (((field[row][col+2] != EMPTY) && (field[row][col+1] != EMPTY)))
            {
                return false;
            }   
            else if (((field[row + 1][col+2] != EMPTY) && (field[row+1][col+1] != EMPTY)))
            {
                return false;
            }
            else
            {
                return true;
            }
        }

        //returns false if block goes to the left and hits blocks
        else if (direction == LEFT)
        {
            if (((field[row][col] != EMPTY) && (field[row][col-1] != EMPTY)))
            {
                return false;
            }   
            else if (((field[row + 1][col] != EMPTY) && (field[row+1][col-1] != EMPTY)))
            {
                return false;
            }
            else
            {
                return true;
            }
        }

        //if all if statements are false, return true
        else
        {
            return true; 
        }
    }//validMove

    /**
     * checkRow
     *
     * If the row (given by parameter row) has all columns filled then return true.
     * @param row the row to be checked
     */
    public boolean checkRow(int row)
    {
        int count = 0; //use count to determine if all columns are filled
        for (int j = 0; j < NUM_COLS; ++j)
        {
            if (field[row][j] != EMPTY)
            {
                ++count;
            }
        }
        if (count == NUM_COLS) //if whole row is filled, return true
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * removeRows
     *
     * This method searches the field for any complete rows of blocks and
     * removes them.  Rows above the removed show shift down one row.
     * The score is incremented for each complete row that is removed.
     */
    private void removeRows()
    {
        int i = 0;
        int j = 0;
        //if the checked row is filled
        for (i = NUM_ROWS - 1; i > 0; --i)
        {
            if (checkRow(i) == true)//if row is filled, empty that row and shift everything down
            {
                for (j = 0; j < NUM_COLS; ++j)
                {
                    field[i][j] = EMPTY;
                }
                for (int row = i - 1; row > 0; --row)
                {
                    for (int col = 0; col < NUM_COLS; ++col) 
                    {
                        field[row+1][col] = field[row][col];
                    }
                }
                score = score + 1; //increment score by 1 for each row deleted
            }
        }

    }//removeRows
    /*======================================================================
     *                    ATTENTION STUDENTS!
     *
     * The code below this point should not be edited.  However, you are
     * encouraged to examine the code to learn a little about how the rest
     * of the game was implemented.
     * ----------------------------------------------------------------------
     */
    /*======================================================================
     * More Instance Variables and Constants
     *
     * ==> You should not modify the values of these variables <==
     *----------------------------------------------------------------------
     */
    public static final int WINDOW_WIDTH = 230;
    public static final int WINDOW_HEIGHT = 500;
    public static final int WINDOW_MARGIN = 10;

    //The location of the current piece.
    private int currRow = 0;
    private int currCol = 0;

    //random number generator
    private Random randGen = new Random();

    // colors array for drawing the pieces
    // Constants for valid colors, INVALID_COLOR, and EMPTY are defined above, in the
    // area that students are expected to study
    // Additional block colors, if desired, should be inserted before EMPTY (the last 
    // entry below), and the defined constant NUM_COLORS (defined above) adjusted accordingly

    // possible block colors
    private Color[] blockColors = { 
            Color.MAGENTA,          // invalid (so must be cleared explicitly)
            Color.RED,              // red
            new Color(0, 110, 0),   // green
            new Color(0,0,170),     // blue
            Color.BLACK };          // EMPTY (should never be displayed)

    /**
     * createRandomPiece
     *
     * creates a new piece at the top of the Mintris board
     *
     */
    public void createRandomPiece()
    {
        //Select a random starting column and color
        int col = randGen.nextInt(NUM_COLS - 1);
        int type = randGen.nextInt(NUM_COLORS) + 1;

        //Fill the indicated 2x2 area
        for(int x = 0; x < 2; ++x) 
        {
            for(int y = 0; y < 2; ++y) 
            {
                this.field[x][y + col] = type;
            }
        }

        //randomly select which block in the 2x2 area of the piece will be empty
        int which = randGen.nextInt(4);
        int x = which / 2;
        int y = which % 2;
        field[x][y+col] = EMPTY;

        //record the location of this new piece
        this.currRow = 0;
        this.currCol = col;

    }//createRandomPiece

    /**
     * drawBlock
     *
     * a helper method for {@link paint}.  This method draws a Mintris block of a
     * given color at a given x,y coordinate.
     *
     * @param  g          the Graphics object for this application
     * @param  x, y       the coordinates of the block
     * @param  blockColor the main color of the block
     */
    public void drawBlock(Graphics g, int x, int y, Color blockColor)
    {
        //draw the main block
        g.setColor(blockColor);
        g.fillRect(x, y, BLOCK_SIZE, BLOCK_SIZE);

        //draw some shading on the edges for a 3D effect
        g.setColor(Color.white); //blockColor.brighter());
        g.drawLine(x, y+1, x + BLOCK_SIZE, y+1);
        g.drawLine(x + BLOCK_SIZE - 1, y, x + BLOCK_SIZE - 1, y + BLOCK_SIZE);
        g.setColor(blockColor.darker());
        g.drawLine(x+1, y, x+1, y + BLOCK_SIZE);
        g.drawLine(x+1, y + BLOCK_SIZE - 1, x + BLOCK_SIZE - 1, y + BLOCK_SIZE - 1);

        //draw a black border around it
        g.setColor(Color.BLACK);
        g.drawRect(x, y, BLOCK_SIZE, BLOCK_SIZE);

    }//drawBlock

    /**
     * paint
     *
     * This methods draws the current state of the game on a given canvas.  The
     * field occupies the bottom left corner.  A title is at the top and the
     * current score is shown at right.
     * 
     * @param  g   the Graphics object for this application
     */
    public void paint(Graphics g)
    {
        //start with the background color
        Color bgColor = new Color(0x330088);  //medium-dark purple
        g.setColor(bgColor);
        g.fillRect(0,0,WINDOW_WIDTH,WINDOW_HEIGHT);

        //Calculate the position of the playing field
        int margin = 5;
        int topSide = WINDOW_HEIGHT - ( NUM_ROWS * BLOCK_SIZE + margin + WINDOW_MARGIN);
        int bottomSide = topSide + NUM_ROWS * BLOCK_SIZE;
        int leftSide = WINDOW_MARGIN + margin;
        int rightSide = leftSide + NUM_COLS * BLOCK_SIZE;

        //Draw the playing field
        Color fieldColor = new Color(0x9966FF);  //lavender
        g.setColor(fieldColor);
        g.fillRect(leftSide, topSide, NUM_COLS * BLOCK_SIZE, NUM_ROWS * BLOCK_SIZE);

        //Draw a thick border around the playing field 
        g.setColor(Color.WHITE);
        for(int i = 1; i <= 5; ++i)
        {
            g.drawRect(leftSide - i, topSide - i,
                NUM_COLS * BLOCK_SIZE + margin , NUM_ROWS * BLOCK_SIZE + margin);
        }

        //Draw the blocks
        for(int row = 0; row < field.length; ++row)
        {
            for (int col = 0; col < field[row].length; ++col)
            {
                //calculate block position
                int xPos = leftSide + col * BLOCK_SIZE;
                int yPos = topSide + row * BLOCK_SIZE;

                //Verify the color index is valid
                // (NUM_COLORS + 1 is EMPTY)
                if ( (field[row][col] < 0) || (field[row][col] > EMPTY))
                {
                    field[row][col] = INVALID_COLOR;
                }

                //draw the block
                if (field[row][col] != EMPTY)
                {
                    drawBlock(g, xPos, yPos, blockColors[field[row][col]]);
                }
            }//for
        }//for

        //draw the title
        g.setColor(Color.WHITE);
        Font bigFont = new Font("SansSerif", Font.BOLD, 32);
        g.setFont(bigFont);
        g.drawString("Mintris",45,50);

        //draw the score
        g.setColor(Color.WHITE);
        Font medFont = new Font("SansSerif", Font.PLAIN, 18);
        g.setFont(medFont);
        int leftMargin = rightSide + 15;
        g.drawString("Score:" + this.score, 70, 75);

    }//paint

    /**
     * keyPressed
     *
     * when the user presses a key, this method examines it to see
     * if the key is one that the program responds to and then calls the
     * appropriate method.
     */
    public void keyPressed(KeyEvent e)
    {
        //Call the appropriate student method(s) based upon the key pressed
        int key = e.getKeyCode();
        switch(key)
        {
            //Move the piece left
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_KP_LEFT:
            case 'a':
            case 'A':
            if (validMove(currRow, currCol, LEFT))
            {
                move(currRow, currCol, LEFT);
                --currCol;
            }
            break;

            //Move the piece right
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_KP_RIGHT:
            case 'd':
            case 'D':
            if (validMove(currRow, currCol, RIGHT))
            {
                move(currRow, currCol, RIGHT);
                ++currCol;
            }
            break;

            //Drop the current piece down one row
            case KeyEvent.VK_DOWN:
            case 's':
            case 'S':
            if (validMove(currRow, currCol, DOWN))
            {
                move(currRow, currCol, DOWN);
                ++currRow;
            }
            break;

            //Drop the current piece all the way down
            case ' ':
            while (validMove(currRow, currCol, DOWN))
            {
                move(currRow, currCol, DOWN);
                ++currRow;
            }
            break;

            case KeyEvent.VK_UP:
            case 'w':
            case 'W':
            rotate(currRow, currCol);
            break;

            //Create a new game
            case 'n':
            case 'N':
            clearField();
            createRandomPiece();
            score = 0;
            break;

            //create a quick layout to aid in testing
            case 't':
            case 'T':
            clearField();
            for(int i = 3; i < field.length; ++i)
            {
                field[i][NUM_COLS/2] = BLUE_BLOCK;
            }
            for(int x = field.length - 2; x < field.length; ++x)
            {
                for (int y = 0; y < field[x].length; ++y)
                {
                    field[x][y] = RED_BLOCK;
                }
            }
            int lastRow = field.length - 1;
            field[lastRow][0]   = EMPTY;
            field[lastRow-1][1] = EMPTY;
            field[lastRow-1][0] = EMPTY;
            createRandomPiece();
            break;

            //Quit the game
            case 'q':
            case 'Q':
            System.exit(0);

        }//switch

        //Regardless of keypress check for a piece that has bottomed out
        if (! validMove(currRow, currCol, DOWN))
        {
            removeRows();
            createRandomPiece();
        }

        //redraw the screen so user can see changes
        repaint();
    }//keyPressed

    //These two method must be implemented but we don't care about these events.
    //We only care about key presses (see method above)
    public void keyReleased(KeyEvent e){}

    public void keyTyped(KeyEvent e){}

    /**
     * This method creates a window frame and displays the Mintris
     * game inside of it.  
     */
    public static void main(String[] args)
    {
        //Create a properly sized window for this program
        final JFrame myFrame = new JFrame();
        myFrame.setSize(WINDOW_WIDTH+10, WINDOW_HEIGHT+30);

        //Tell this window to close when someone presses the close button
        myFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {
                    System.exit(0);
                };
            });

        //Display a new Mintris object in the window
        Mintris mintrisGame = new Mintris();
        mintrisGame.clearField();
        mintrisGame.createRandomPiece();
        myFrame.addKeyListener(mintrisGame);
        myFrame.getContentPane().add(mintrisGame);

        //show the user
        myFrame.setVisible(true);

    }//main

}//class Mintris



