package main;

import piece.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GamePanel extends JPanel implements Runnable {
    // width and height of the board
    public static final int WIDTH = 1100;
    public static final int HEIGHT = 800;
    final int FPS = 60;
    Thread gameThread;
    Board board = new Board();
    Mouse mouse = new Mouse();



    // PIECES
    public static ArrayList<Piece> pieces = new ArrayList<>();
    public static ArrayList<Piece> simPieces = new ArrayList<>();
    ArrayList<Piece> promoPieces = new ArrayList<>();
    Piece activeP,checkingP;
    public static Piece castlingP;
    // COLOR
    public static final int WHITE = 0;
    public static final int BLACK = 1;
    int currentColor = WHITE;

    // BOOLEANS
    boolean canMove;
    boolean validSquare;
    boolean promotion;
    boolean gameOver;
    boolean stalemate;

    // constructor
    public GamePanel(){
        setDoubleBuffered(true);
        setPreferredSize(new Dimension(WIDTH,HEIGHT));
        setBackground(Color.black);
        addMouseMotionListener(mouse);
        addMouseListener(mouse);

        setPieces();
        copyPieces(pieces,simPieces);
    }

    public void launchGame(){
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void setPieces(){
        // White
        pieces.add(new Pawn(WHITE,0,6));
        pieces.add(new Pawn(WHITE,1,6));
        pieces.add(new Pawn(WHITE,2,6));
        pieces.add(new Pawn(WHITE,3,6));
        pieces.add(new Pawn(WHITE,4,6));
        pieces.add(new Pawn(WHITE,5,6));
        pieces.add(new Pawn(WHITE,6,6));
        pieces.add(new Pawn(WHITE,7,6));
        pieces.add(new Rook(WHITE,0,7));
        pieces.add(new Rook(WHITE,7,7));
        pieces.add(new Knight(WHITE,1,7));
        pieces.add(new Knight(WHITE,6,7));
        pieces.add(new Bishop(WHITE,2,7));
        pieces.add(new Bishop(WHITE,5,7));
        pieces.add(new Queen(WHITE,3,7));
        pieces.add(new King(WHITE,4,7));
        // Black
        pieces.add(new Pawn(BLACK,0,1));
        pieces.add(new Pawn(BLACK,1,1));
        pieces.add(new Pawn(BLACK,2,1));
        pieces.add(new Pawn(BLACK,3,1));
        pieces.add(new Pawn(BLACK,4,1));
        pieces.add(new Pawn(BLACK,5,1));
        pieces.add(new Pawn(BLACK,6,1));
        pieces.add(new Pawn(BLACK,7,1));
        pieces.add(new Rook(BLACK,0,0));
        pieces.add(new Rook(BLACK,7,0));
        pieces.add(new Knight(BLACK,1,0));
        pieces.add(new Knight(BLACK,6,0));
        pieces.add(new Bishop(BLACK,2,0));
        pieces.add(new Bishop(BLACK,5,0));
        pieces.add(new Queen(BLACK,3,0));
        pieces.add(new King(BLACK,4,0));

    }

    private void copyPieces(ArrayList<Piece> source , ArrayList<Piece> target){
        target.clear();
        target.addAll(source);
    }

@Override
public void run() {
    double drawInterval = 1000000000 / FPS;
    long lastTime = System.nanoTime();
    long currentTime;
    while (gameThread != null) {
        currentTime = System.nanoTime();
        double delta = (currentTime - lastTime) / drawInterval;

        if (delta >= 1) {
            update();
            repaint();
            Toolkit.getDefaultToolkit().sync(); // Ensure frame synchronization
            lastTime = currentTime;
        }
    }
}

    // we call bottom two methods 60 times per second
    // used to handle update info ... pieces(x,y), no. of pieces left

    private void update(){
        if(promotion){
            promoting();
        }
        else if(!gameOver && !stalemate) {
            if(mouse.pressed){
                if(activeP == null){
                    for(Piece piece : simPieces){
                        if(piece.color == currentColor &&
                                piece.col == mouse.x/Board.SQUARE_SIZE &&
                                piece.row == mouse.y/Board.SQUARE_SIZE){
                            activeP = piece;
                        }
                    }
                }
                else{
                    simulate();
                }
            }

            // Mouse RELEASED
            if(!mouse.pressed){
                if(activeP != null){
                    if(validSquare){
                        // MOVE CONFIRMED
                        // Update the piece list in case a piece has been captured or removed during the simulation
                        copyPieces(simPieces,pieces);
                        activeP.updatePosition();
                        if(castlingP!=null){
                            castlingP.updatePosition();
                        }

                        if(isKingInCheck() && isCheckmate()){
                            gameOver = true;
                        }
                        else if(!isKingInCheck() && isStalemate()){
                            stalemate = true;
                        }
                        else{
                            if(canPromote()){
                                promotion = true;
                            }
                            else {
                                changePlayer();
                            }
                        }



                    }else {

                        // The move is not valid so reset everything, restore the original list
                        copyPieces(pieces,simPieces);
                        activeP.resetPosition();
                        activeP = null;
                    }
                }
            }
        }

    }

    private void simulate() {

        canMove = false;
        validSquare = false;

        // Reset the piece list in every loop
        // This is for restoring the removed piece during simulation
        copyPieces(pieces,simPieces);

        if(castlingP!=null){
            castlingP.col = castlingP.preCol;
            castlingP.x = castlingP.getX(castlingP.col);
            castlingP = null;
        }

        activeP.x = mouse.x - Board.HALF_SQUARE_SIZE;
        activeP.y = mouse.y - Board.HALF_SQUARE_SIZE;
        activeP.col = activeP.getCol(activeP.x);
        activeP.row = activeP.getRow(activeP.y);

        if(activeP.canMove(activeP.col,activeP.row)){
            canMove = true;

            // if hitting a Piece, remove it from the list
            if(activeP.hittingP != null){
                simPieces.remove(activeP.hittingP.getIndex());
            }
            checkCastling();

            if(!isIllegal(activeP) && !opponentCanCaptureKing()){
                validSquare = true;
            }

            validSquare = true;
        }
    }

    private boolean isIllegal(Piece king){
        if(king.type == Type.KING){
            for(Piece piece : simPieces){
                if(piece != king && piece.color != king.color && piece.canMove(king.col,king.row)){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean opponentCanCaptureKing(){
        Piece king = getKing(false);
        for(Piece piece : simPieces){
            if(piece.color != king.color && piece.canMove(king.col,king.row)){
                return true;
            }
        }
        return false;
    }

    private boolean isKingInCheck(){

        Piece king = getKing(true);
        if(activeP.canMove(king.col,king.row)){
            checkingP = activeP;
            return true;
        }
        else{
            checkingP = null;

        }

        return false;
    }

    private Piece getKing(boolean opponent){
        Piece king = null;
        for(Piece piece : simPieces){
            if(opponent){
                if(piece.type == Type.KING && piece.color != currentColor){
                    king = piece;
                }
            }
            else{
                if(piece.type == Type.KING && piece.color == currentColor){
                    king = piece;
                }
            }
        }
        return king;
    }

    private boolean isCheckmate(){
        Piece king = getKing(true);
        if(kingCanMove(king)){
            return false;
        }
        else{
            // Check if you can block the attack with the other piece
            int colDiff = Math.abs(checkingP.col - king.col);
            int rowDiff = Math.abs(checkingP.row - king.row);

            if(colDiff == 0){
                // vertically
                if(checkingP.row < king.row){
                    // The checking Piece is above the king
                    for(int row = checkingP.row ; row < king.row ; row++){
                        for(Piece piece : simPieces){
                            if(piece != king && piece.color != currentColor && piece.canMove(checkingP.col,row)){
                                return false;
                            }
                        }
                    }
                }
                if(checkingP.row > king.row){
                    // below the king
                    for(int row = checkingP.row ; row > king.row ; row--){
                        for(Piece piece : simPieces){
                            if(piece != king && piece.color != currentColor && piece.canMove(checkingP.col,row)){
                                return false;
                            }
                        }
                    }

                }
            }
            else if(rowDiff == 0){
                // horizontally
                if(checkingP.col < king.col){
                    // left
                    for(int col = checkingP.col ; col < king.col ; col++){
                        for(Piece piece : simPieces){
                            if(piece != king && piece.color != currentColor && piece.canMove(col,checkingP.row)){
                                return false;
                            }
                        }
                    }
                }
                if(checkingP.col > king.col){
                    //right
                    for(int col = checkingP.col ; col > king.col ; col--){
                        for(Piece piece : simPieces){
                            if(piece != king && piece.color != currentColor && piece.canMove(col,checkingP.row)){
                                return false;
                            }
                        }
                    }
                }
            }
            else if(colDiff == rowDiff){
                // diagonally
                if(checkingP.row < king.row){
                    if(checkingP.col < king.col){
                        for(int col = checkingP.col , row = checkingP.row ;col < king.col ; col++,row++){
                            for(Piece piece : simPieces){
                                if(piece != king && piece.color != currentColor && piece.canMove(col,row)){
                                    return false;
                                }
                            }
                        }
                    }
                    if(checkingP.col > king.col){
                        for(int col = checkingP.col , row = checkingP.row ;col > king.col ; col--,row++){
                            for(Piece piece : simPieces){
                                if(piece != king && piece.color != currentColor && piece.canMove(col,row)){
                                    return false;
                                }
                            }
                        }
                    }
                }
                if(checkingP.row > king.row){
                    if(checkingP.col < king.col){
                        for(int col = checkingP.col , row = checkingP.row ;col < king.col ; col++,row--){
                            for(Piece piece : simPieces){
                                if(piece != king && piece.color != currentColor && piece.canMove(col,row)){
                                    return false;
                                }
                            }
                        }
                    }
                    if(checkingP.col > king.col){
                        for(int col = checkingP.col , row = checkingP.row ;col > king.col ; col--,row--){
                            for(Piece piece : simPieces){
                                if(piece != king && piece.color != currentColor && piece.canMove(col,row)){
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }

        return true;
    }
    private boolean kingCanMove(Piece king){
        if(isValidMove(king,-1,-1)){return true;}
        if(isValidMove(king,0,-1)){return true;}
        if(isValidMove(king,1,-1)){return true;}
        if(isValidMove(king,-1,0)){return true;}
        if(isValidMove(king,1,0)){return true;}
        if(isValidMove(king,-1,1)){return true;}
        if(isValidMove(king,0,1)){return true;}
        if(isValidMove(king,1,1)){return true;}
        return false;
    }
    private boolean isValidMove(Piece king,int colPlus,int rowPlus){
        boolean isValidMove = false;
        king.col += colPlus;
        king.row += rowPlus;
        if(king.canMove(king.col,king.row)){
            if(king.hittingP != null){
                simPieces.remove(king.hittingP.getIndex());
            }
            if(!isIllegal(king)){
                isValidMove = true;
            }
        }
        king.resetPosition();
        copyPieces(pieces,simPieces);

        return isValidMove;
    }

    private boolean isStalemate(){
        int count = 0;
        for(Piece piece : simPieces){
            if(piece.color != currentColor){
                count++;
            }
        }

        if(count == 1){
            if(!kingCanMove(getKing(true))){
                return true;
            }
        }

        return false;
    }


    private void checkCastling(){
        if(castlingP != null){
            if(castlingP.col == 0){
                castlingP.col += 3;
            }
            else if(castlingP.col == 7){
                castlingP.col -= 2;
            }
            castlingP.x = castlingP.getX(castlingP.col);
        }
    }
    private void changePlayer(){
        if(currentColor == WHITE){
            currentColor = BLACK;
            for(Piece piece : pieces){
                if(piece.color == BLACK){
                    piece.twoStepped = false;
                }
            }
        }
        else{
            currentColor = WHITE;
            for(Piece piece : pieces){
                if(piece.color == WHITE){
                    piece.twoStepped = false;
                }
            }
        }
        activeP = null;
    }

    private boolean canPromote(){
        if(activeP.type == Type.PAWN){
            if((currentColor == WHITE && activeP.row == 0) || (currentColor == BLACK && activeP.row == 7)){
                promoPieces.clear();
                promoPieces.add(new Rook(currentColor,9,2));
                promoPieces.add(new Queen(currentColor,9,5));
                promoPieces.add(new Bishop(currentColor,9,4));
                promoPieces.add(new Knight(currentColor,9,3));
                return true;
            }
        }
        return false;
    }

    private void promoting(){
        if(mouse.pressed){
            for(Piece piece : promoPieces){
                if(piece.col == mouse.x/Board.SQUARE_SIZE && piece.row == mouse.y/Board.SQUARE_SIZE){
                    switch (piece.type){
                        case ROOK: simPieces.add(new Rook(currentColor,activeP.col,activeP.row));break;
                        case BISHOP: simPieces.add(new Bishop(currentColor,activeP.col,activeP.row));break;
                        case QUEEN: simPieces.add(new Queen(currentColor, activeP.col, activeP.row));break;
                        case KNIGHT: simPieces.add(new Knight(currentColor, activeP.col, activeP.row));break;
                        default: break;
                    }
                    simPieces.remove(activeP.getIndex());
                    copyPieces(simPieces,pieces);
                    activeP = null;
                    promotion = false;
                    changePlayer();
                }
            }
        }
    }


    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // BOARD draw
        board.draw(g2);

        // Safely draw pieces
        ArrayList<Piece> piecesCopy;
        synchronized (simPieces) {
            piecesCopy = new ArrayList<>(simPieces);
        }

        for (Piece p : piecesCopy) {
            if (p != null) {
                p.draw(g2);
            }
        }

        // Draw active piece and highlight if applicable
        if (activeP != null) {
            if (canMove) {
                if(isIllegal(activeP) || opponentCanCaptureKing()){
                    g2.setColor(Color.gray);
                }
                else{
                    g2.setColor(Color.white);
                }
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                g2.fillRect(activeP.col * Board.SQUARE_SIZE, activeP.row * Board.SQUARE_SIZE,
                        Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

            }

            activeP.draw(g2);
        }

        // STATUS MESSAGES
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(new Font("Book Antique",Font.PLAIN,40));
        g2.setColor(Color.WHITE);

        if(promotion){
            g2.drawString("Promote to:",840,150);
            for(Piece piece : promoPieces){
                g2.drawImage(piece.image,piece.getX(piece.col),piece.getY(piece.row),Board.SQUARE_SIZE,Board.SQUARE_SIZE, null);
            }
        }
        else{
            if(currentColor == WHITE){
                g2.drawString("White's Turn",840,550);
                if(checkingP != null && checkingP.color == BLACK){
                    g2.setColor(Color.RED);
                    g2.drawString("The King",840,650);
                    g2.drawString("Is in Check",840,700);
                }
            }
            else{
                g2.drawString("Black's Turn",840,250);
                if(checkingP != null && checkingP.color == WHITE){
                    g2.setColor(Color.RED);
                    g2.drawString("The King",840,100);
                    g2.drawString("Is in Check",840,150);
                }
            }
            if(gameOver){
                String s = "";
                if(currentColor == WHITE){
                    s = "White Wins!";
                }
                else{
                    s = "Black Wins!";
                }
                g2.setFont(new Font("Arial",Font.PLAIN,90));
                g2.setColor(Color.green);
                g2.drawString(s,200,420);
            }
        }
    }
}