package piece;

import main.GamePanel;
import main.Type;

public class Rook extends Piece{
    public Rook(int color , int col , int row){
        super(color,col,row);
        type = Type.ROOK;
        if(color == GamePanel.WHITE){
            image = getImage("piece/wr.png");
        }
        else{
            image = getImage("piece/br.png");
        }
    }

    public boolean canMove(int targetCol , int targetRow){
        if(isWithinBoard(targetCol,targetRow) && !isSameSquare(targetCol, targetRow)){
            if(targetCol == preCol || targetRow == preRow){
                return isValidSquare(targetCol, targetRow) && !pieceIsOnStraightLine(targetCol, targetRow);
            }
        }
        return false;
    }
}
