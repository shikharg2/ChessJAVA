package piece;

import main.Board;
import main.GamePanel;
import main.Type;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Piece {
    public Type type;
    public BufferedImage image;
    public int x , y;
    public int col, row, preCol, preRow;
    public int color;
    public Piece hittingP;
    boolean moved;
    public boolean twoStepped;

    public Piece(int color , int col, int row){
        this.color = color;
        this.col = col;
        this.row = row;
        x = getX(col);
        y = getY(row);
        preCol = col;
        preRow = row;
    }

    public BufferedImage getImage(String imagePath) {

        BufferedImage image = null;
        try{
            image = ImageIO.read(getClass().getClassLoader().getResourceAsStream(imagePath));
        } catch (IOException e){
            e.printStackTrace();
        }
        return image;
    }

    public int getX(int col){
        return col * Board.SQUARE_SIZE;
    }
    public int getY(int row){
        return row * Board.SQUARE_SIZE;
    }
    public int getCol(int x){
        return (x + Board.HALF_SQUARE_SIZE)/Board.SQUARE_SIZE;
    }
    public int getRow(int y){
        return (y + Board.HALF_SQUARE_SIZE)/Board.SQUARE_SIZE;
    }

    public int getIndex(){
        for(int i = 0 ; i < GamePanel.simPieces.size() ; i++){
            if(GamePanel.simPieces.get(i) == this){
                return i;
            }
        }
        return 0;
    }

    public void updatePosition(){
        // To check En-Passant
        if(type == Type.PAWN){
            if(Math.abs(row - preRow) == 2){
                twoStepped = true;
            }
        }

        moved = true;
        x = getX(col);
        y = getY(row);
        preCol = getCol(x);
        preRow = getRow(y);

    }

    public void resetPosition(){
        col = preCol;
        row = preRow;
        x = getX(col);
        y = getY(row);
    }

    public boolean canMove(int targetCol, int targetRow){
        // will be overridden in each piece class
        return false;
    }

    public boolean isWithinBoard(int targetCol , int targetRow){
        if(targetCol >= 0 && targetCol <= 7 && targetRow >= 0 && targetRow <= 7){
            return true;
        }
        return false;
    }

    public boolean isSameSquare(int targetCol , int targetRow){
        if(targetCol == preCol && targetRow == preRow){
            return true;
        }
        return false;
    }

    public Piece getHittingP(int targetCol , int targetRow){
        for(Piece piece : GamePanel.simPieces){
            if(piece.col == targetCol && piece.row == targetRow && piece != this){
                return piece;
            }
        }
        return null;
    }

    public boolean isValidSquare(int targetCol , int targetRow){
        hittingP = getHittingP(targetCol,targetRow);

        if(hittingP == null){   // This square is vacant
            return true;
        }
        else{
            // This square is occupied
            if(hittingP.color != this.color){ // If color is different , capturable piece
                return true;
            }
            else{
                hittingP = null;
            }
        }

        return false;
    }

    public boolean pieceIsOnStraightLine(int targetCol ,int targetRow){
        //  Left
        for(int c = preCol - 1 ; c > targetCol ; c--){
            for(Piece piece : GamePanel.simPieces){
                if(piece.col == c && piece.row == targetRow){
                    hittingP = piece;
                    return true;
                }
            }
        }
        //  Right
        for(int c = preCol + 1 ; c < targetCol ; c++){
            for(Piece piece : GamePanel.simPieces){
                if(piece.col == c && piece.row == targetRow){
                    hittingP = piece;
                    return true;
                }
            }
        }
        //  Up
        for(int c = preRow - 1 ; c > targetRow ; c--){
            for(Piece piece : GamePanel.simPieces){
                if(piece.row == c && piece.col == targetCol){
                    hittingP = piece;
                    return true;
                }
            }
        }
        //  Down
        for(int c = preRow + 1 ; c < targetRow ; c++){
            for(Piece piece : GamePanel.simPieces){
                if(piece.row == c && piece.col == targetCol){
                    hittingP = piece;
                    return true;
                }
            }
        }

        return false;
    }

    public boolean pieceIsOnDiagonal(int targetCol,int targetRow){
        if(targetRow < preRow){
            // Up left
            for(int c = preCol - 1 ; c > targetCol ; c--){
                int diff = Math.abs(c - preCol);
                for(Piece piece : GamePanel.simPieces){
                    if(piece.col == c && piece.row == preRow - diff){
                        return true;
                    }
                }
            }
            // Up right
            for(int c = preCol + 1 ; c < targetCol ; c++){
                int diff = Math.abs(c - preCol);
                for(Piece piece : GamePanel.simPieces){
                    if(piece.col == c && piece.row == preRow - diff){
                        return true;
                    }
                }
            }
        }
        if(targetRow > preRow){
            // Down left
            for(int c = preCol - 1 ; c > targetCol ; c--){
                int diff = Math.abs(c - preCol);
                for(Piece piece : GamePanel.simPieces){
                    if(piece.col == c && piece.row == preRow + diff){
                        return true;
                    }
                }
            }
            // Down right
            for(int c = preCol + 1 ; c < targetCol ; c++){
                int diff = Math.abs(c - preCol);
                for(Piece piece : GamePanel.simPieces){
                    if(piece.col == c && piece.row == preRow + diff){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void draw(Graphics2D g2){
        g2.drawImage(image,x,y,Board.SQUARE_SIZE,Board.SQUARE_SIZE,null);
    }
}
