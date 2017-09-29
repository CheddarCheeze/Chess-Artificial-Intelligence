import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

/// Represents the state of a chess game
class ChessState {
    public static final int MAX_PIECE_MOVES = 27;
    public static final int None = 0;
    public static final int Pawn = 1;
    public static final int Rook = 2;
    public static final int Knight = 3;
    public static final int Bishop = 4;
    public static final int Queen = 5;
    public static final int King = 6;
    public static final int PieceMask = 7;
    public static final int WhiteMask = 8;
    public static final int AllMask = 15;

    int[] m_rows;
    static ChessState.ChessMove aimove = new ChessState.ChessMove();
    static boolean playerTurn;

    ChessState() {
        m_rows = new int[8];
        resetBoard();
    }

    ChessState(ChessState that) {
        m_rows = new int[8];
        for(int i = 0; i < 8; i++)
            this.m_rows[i] = that.m_rows[i];
    }

    int getPiece(int col, int row) {
        return (m_rows[row] >> (4 * col)) & PieceMask;
    }

    boolean isWhite(int col, int row) {
        return (((m_rows[row] >> (4 * col)) & WhiteMask) > 0 ? true : false);
    }

    /// Sets the piece at location (col, row). If piece is None, then it doesn't
    /// matter what the value of white is.
    void setPiece(int col, int row, int piece, boolean white) {
        m_rows[row] &= (~(AllMask << (4 * col)));
        m_rows[row] |= ((piece | (white ? WhiteMask : 0)) << (4 * col));
    }

    /// Sets up the board for a new game
    void resetBoard() {
        setPiece(0, 0, Rook, true);
        setPiece(1, 0, Knight, true);
        setPiece(2, 0, Bishop, true);
        setPiece(3, 0, Queen, true);
        setPiece(4, 0, King, true);
        setPiece(5, 0, Bishop, true);
        setPiece(6, 0, Knight, true);
        setPiece(7, 0, Rook, true);
        for(int i = 0; i < 8; i++)
            setPiece(i, 1, Pawn, true);
        for(int j = 2; j < 6; j++) {
            for(int i = 0; i < 8; i++)
                setPiece(i, j, None, false);
        }
        for(int i = 0; i < 8; i++)
            setPiece(i, 6, Pawn, false);
        setPiece(0, 7, Rook, false);
        setPiece(1, 7, Knight, false);
        setPiece(2, 7, Bishop, false);
        setPiece(3, 7, Queen, false);
        setPiece(4, 7, King, false);
        setPiece(5, 7, Bishop, false);
        setPiece(6, 7, Knight, false);
        setPiece(7, 7, Rook, false);
    }

    /// Positive means white is favored. Negative means black is favored.
    int heuristic(Random rand)
    {
        int score = 0;
        for(int y = 0; y < 8; y++)
        {
            for(int x = 0; x < 8; x++)
            {
                int p = getPiece(x, y);
                int value;
                switch(p)
                {
                    case None: value = 0; break;
                    case Pawn: value = 10; break;
                    case Rook: value = 63; break;
                    case Knight: value = 31; break;
                    case Bishop: value = 36; break;
                    case Queen: value = 88; break;
                    case King: value = 500; break;
                    default: throw new RuntimeException("what?");
                }
                if(isWhite(x, y))
                    score += value;
                else
                    score -= value;
            }
        }
        return score + rand.nextInt(3) - 1;
    }

    /// Returns an iterator that iterates over all possible moves for the specified color
    ChessMoveIterator iterator(boolean white) {
        return new ChessMoveIterator(this, white);
    }

    /// Returns true iff the parameters represent a valid move
    boolean isValidMove(int xSrc, int ySrc, int xDest, int yDest) {
        ArrayList<Integer> possible_moves = moves(xSrc, ySrc);
        for(int i = 0; i < possible_moves.size(); i += 2) {
            if(possible_moves.get(i).intValue() == xDest && possible_moves.get(i + 1).intValue() == yDest)
                return true;
        }
        return false;
    }

    /// Print a representation of the board to the specified stream
    void printBoard(PrintStream stream)
    {
        stream.println("  A  B  C  D  E  F  G  H");
        stream.print(" +");
        for(int i = 0; i < 8; i++)
            stream.print("--+");
        stream.println();
        for(int j = 7; j >= 0; j--) {
            stream.print(Character.toString((char)(49 + j)));
            stream.print("|");
            for(int i = 0; i < 8; i++) {
                int p = getPiece(i, j);
                if(p != None) {
                    if(isWhite(i, j))
                        stream.print("w");
                    else
                        stream.print("b");
                }
                switch(p) {
                    case None: stream.print("  "); break;
                    case Pawn: stream.print("p"); break;
                    case Rook: stream.print("r"); break;
                    case Knight: stream.print("n"); break;
                    case Bishop: stream.print("b"); break;
                    case Queen: stream.print("q"); break;
                    case King: stream.print("K"); break;
                    default: stream.print("?"); break;
                }
                stream.print("|");
            }
            stream.print(Character.toString((char)(49 + j)));
            stream.print("\n +");
            for(int i = 0; i < 8; i++)
                stream.print("--+");
            stream.println();
        }
        stream.println("  A  B  C  D  E  F  G  H");
    }

    /// Pass in the coordinates of a square with a piece on it
    /// and it will return the places that piece can move to.
    ArrayList<Integer> moves(int col, int row) {
        ArrayList<Integer> pOutMoves = new ArrayList<Integer>();
        int p = getPiece(col, row);
        boolean bWhite = isWhite(col, row);
        int nMoves = 0;
        int i, j;
        switch(p) {
            case Pawn:
                if(bWhite) {
                    if(!checkPawnMove(pOutMoves, col, inc(row), false, bWhite) && row == 1)
                        checkPawnMove(pOutMoves, col, inc(inc(row)), false, bWhite);
                    checkPawnMove(pOutMoves, inc(col), inc(row), true, bWhite);
                    checkPawnMove(pOutMoves, dec(col), inc(row), true, bWhite);
                }
                else {
                    if(!checkPawnMove(pOutMoves, col, dec(row), false, bWhite) && row == 6)
                        checkPawnMove(pOutMoves, col, dec(dec(row)), false, bWhite);
                    checkPawnMove(pOutMoves, inc(col), dec(row), true, bWhite);
                    checkPawnMove(pOutMoves, dec(col), dec(row), true, bWhite);
                }
                break;
            case Bishop:
                for(i = inc(col), j=inc(row); true; i = inc(i), j = inc(j))
                    if(checkMove(pOutMoves, i, j, bWhite))
                        break;
                for(i = dec(col), j=inc(row); true; i = dec(i), j = inc(j))
                    if(checkMove(pOutMoves, i, j, bWhite))
                        break;
                for(i = inc(col), j=dec(row); true; i = inc(i), j = dec(j))
                    if(checkMove(pOutMoves, i, j, bWhite))
                        break;
                for(i = dec(col), j=dec(row); true; i = dec(i), j = dec(j))
                    if(checkMove(pOutMoves, i, j, bWhite))
                        break;
                break;
            case Knight:
                checkMove(pOutMoves, inc(inc(col)), inc(row), bWhite);
                checkMove(pOutMoves, inc(col), inc(inc(row)), bWhite);
                checkMove(pOutMoves, dec(col), inc(inc(row)), bWhite);
                checkMove(pOutMoves, dec(dec(col)), inc(row), bWhite);
                checkMove(pOutMoves, dec(dec(col)), dec(row), bWhite);
                checkMove(pOutMoves, dec(col), dec(dec(row)), bWhite);
                checkMove(pOutMoves, inc(col), dec(dec(row)), bWhite);
                checkMove(pOutMoves, inc(inc(col)), dec(row), bWhite);
                break;
            case Rook:
                for(i = inc(col); true; i = inc(i))
                    if(checkMove(pOutMoves, i, row, bWhite))
                        break;
                for(i = dec(col); true; i = dec(i))
                    if(checkMove(pOutMoves, i, row, bWhite))
                        break;
                for(j = inc(row); true; j = inc(j))
                    if(checkMove(pOutMoves, col, j, bWhite))
                        break;
                for(j = dec(row); true; j = dec(j))
                    if(checkMove(pOutMoves, col, j, bWhite))
                        break;
                break;
            case Queen:
                for(i = inc(col); true; i = inc(i))
                    if(checkMove(pOutMoves, i, row, bWhite))
                        break;
                for(i = dec(col); true; i = dec(i))
                    if(checkMove(pOutMoves, i, row, bWhite))
                        break;
                for(j = inc(row); true; j = inc(j))
                    if(checkMove(pOutMoves, col, j, bWhite))
                        break;
                for(j = dec(row); true; j = dec(j))
                    if(checkMove(pOutMoves, col, j, bWhite))
                        break;
                for(i = inc(col), j=inc(row); true; i = inc(i), j = inc(j))
                    if(checkMove(pOutMoves, i, j, bWhite))
                        break;
                for(i = dec(col), j=inc(row); true; i = dec(i), j = inc(j))
                    if(checkMove(pOutMoves, i, j, bWhite))
                        break;
                for(i = inc(col), j=dec(row); true; i = inc(i), j = dec(j))
                    if(checkMove(pOutMoves, i, j, bWhite))
                        break;
                for(i = dec(col), j=dec(row); true; i = dec(i), j = dec(j))
                    if(checkMove(pOutMoves, i, j, bWhite))
                        break;
                break;
            case King:
                checkMove(pOutMoves, inc(col), row, bWhite);
                checkMove(pOutMoves, inc(col), inc(row), bWhite);
                checkMove(pOutMoves, col, inc(row), bWhite);
                checkMove(pOutMoves, dec(col), inc(row), bWhite);
                checkMove(pOutMoves, dec(col), row, bWhite);
                checkMove(pOutMoves, dec(col), dec(row), bWhite);
                checkMove(pOutMoves, col, dec(row), bWhite);
                checkMove(pOutMoves, inc(col), dec(row), bWhite);
                break;
            default:
                break;
        }
        return pOutMoves;
    }

    /// Moves the piece from (xSrc, ySrc) to (xDest, yDest). If this move
    /// gets a pawn across the board, it becomes a queen. If this move
    /// takes a king, then it will remove all pieces of the same color as
    /// the king that was taken and return true to indicate that the move
    /// ended the game.
    boolean move(int xSrc, int ySrc, int xDest, int yDest) throws Exception {
        if(xSrc < 0 || xSrc >= 8 || ySrc < 0 || ySrc >= 8)
            throw new Exception("out of range");
        if(xDest < 0 || xDest >= 8 || yDest < 0 || yDest >= 8)
            throw new Exception("out of range");
        int target = getPiece(xDest, yDest);
        int p = getPiece(xSrc, ySrc);
        if(p == None)
            throw new Exception("There is no piece in the source location");
        if(target != None && isWhite(xSrc, ySrc) == isWhite(xDest, yDest))
            throw new Exception("It is illegal to take your own piece");
        if(p == Pawn && (yDest == 0 || yDest == 7))
            p = Queen; // a pawn that crosses the board becomes a queen
        boolean white = isWhite(xSrc, ySrc);
        setPiece(xDest, yDest, p, white);
        setPiece(xSrc, ySrc, None, true);
        if(target == King) {
            // If you take the opponent's king, remove all of the opponent's pieces. This
            // makes sure that look-ahead strategies don't try to look beyond the end of
            // the game (example: sacrifice a king for a king and some other piece.)
            int x, y;
            for(y = 0; y < 8; y++) {
                for(x = 0; x < 8; x++) {
                    if(getPiece(x, y) != None) {
                        if(isWhite(x, y) != white) {
                            setPiece(x, y, None, true);
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    static int inc(int pos) {
        if(pos < 0 || pos >= 7)
            return -1;
        return pos + 1;
    }

    static int dec(int pos) {
        if(pos < 1)
            return -1;
        return pos -1;
    }

    boolean checkMove(ArrayList<Integer> pOutMoves, int col, int row, boolean bWhite) {
        if(col < 0 || row < 0)
            return true;
        int p = getPiece(col, row);
        if(p > 0 && isWhite(col, row) == bWhite)
            return true;
        pOutMoves.add(col);
        pOutMoves.add(row);
        return (p > 0);
    }

    boolean checkPawnMove(ArrayList<Integer> pOutMoves, int col, int row, boolean bDiagonal, boolean bWhite) {
        if(col < 0 || row < 0)
            return true;
        int p = getPiece(col, row);
        if(bDiagonal) {
            if(p == None || isWhite(col, row) == bWhite)
                return true;
        }
        else {
            if(p > 0)
                return true;
        }
        pOutMoves.add(col);
        pOutMoves.add(row);
        return (p > 0);
    }

    /// Represents a possible  move
    static class ChessMove {
        int xSource;
        int ySource;
        int xDest;
        int yDest;
    }

    /// Iterates through all the possible moves for the specified color.
    static class ChessMoveIterator
    {
        int x, y;
        ArrayList<Integer> moves;
        ChessState state;
        boolean white;

        /// Constructs a move iterator
        ChessMoveIterator(ChessState curState, boolean whiteMoves) {
            x = -1;
            y = 0;
            moves = null;
            state = curState;
            white = whiteMoves;
            advance();
        }

        private void advance() {
            if(moves != null && moves.size() >= 2) {
                moves.remove(moves.size() - 1);
                moves.remove(moves.size() - 1);
            }
            while(y < 8 && (moves == null || moves.size() < 2)) {
                if(++x >= 8) {
                    x = 0;
                    y++;
                }
                if(y < 8) {
                    if(state.getPiece(x, y) != ChessState.None && state.isWhite(x, y) == white)
                        moves = state.moves(x, y);
                    else
                        moves = null;
                }
            }
        }

        /// Returns true iff there is another move to visit
        boolean hasNext() {
            return (moves != null && moves.size() >= 2);
        }

        /// Returns the next move
        ChessState.ChessMove next() {
            ChessState.ChessMove m = new ChessState.ChessMove();
            m.xSource = x;
            m.ySource = y;
            m.xDest = moves.get(moves.size() - 2);
            m.yDest = moves.get(moves.size() - 1);
            advance();
            return m;
        }
    }


    // Fix return score, and fix Try every possible move
    // Use a depth of 5, no more than 8
    // Alpha starts -infinity, Beta starts +infinity
    int minimax(ChessState inBoard, int depth, int alpha, int beta, boolean maxPlayer, boolean sourcePlayer) {

        ChessState node = new ChessState(inBoard);
        int score;
        ChessState.ChessMove bestMove = new ChessState.ChessMove();

        if(depth == 0 || heuristicWin(node)) {
            Random rand = new Random();
            score = heuristic(rand);
            return score;
        }

        if(maxPlayer) {
            score = -9999;
            int bestValue = -9999;
            ChessMoveIterator it = node.iterator(sourcePlayer);
            ChessState.ChessMove m = new ChessState.ChessMove();
            ChessState.ChessMove temp;
            while(it.hasNext()){
                
                temp = it.next();
                m.xDest = temp.xSource;
                m.xSource = temp.xDest;
                m.yDest = temp.ySource;
                m.ySource = temp.yDest;
                
                try{
                    node.move(temp.xSource,temp.ySource,temp.xDest, temp.yDest);
                    score = minimax(node,depth-1,alpha,beta,false,!sourcePlayer);
                    if(score > bestValue) {
                        bestValue = score;
                        bestMove = temp;
                    }
                    alpha = Math.max(bestValue, alpha);
                    node.move(m.xSource,m.ySource,m.xDest, m.yDest);
                }
                catch(Exception e) {
                    System.out.println("Minimax exception.");
                }
                
                if(beta <= alpha) {
                    break;
                }
            }
            aimove = bestMove;
            return bestValue;
        }
        else {
            score = 9999;
            int bestValue = 9999;
            ChessMoveIterator it = node.iterator(sourcePlayer);
            ChessState.ChessMove m = new ChessState.ChessMove();
            ChessState.ChessMove temp;
            while(it.hasNext()){
                
                temp = it.next();
                m.xDest = temp.xSource;
                m.xSource = temp.xDest;
                m.yDest = temp.ySource;
                m.ySource = temp.yDest;
                
                try{
                    node.move(temp.xSource,temp.ySource,temp.xDest, temp.yDest);
                    score = minimax(node,depth-1,alpha,beta,true,!sourcePlayer);
                    if(score < bestValue) {
                        bestValue = score;
                    }
                    beta = Math.min(bestValue, beta);
                    node.move(m.xSource,m.ySource,m.xDest, m.yDest);
                }
                catch(Exception e) {
                    System.out.println("Minimax exception.");
                }
                
                if(beta <= alpha) {
                    break;
                }
            }
            return bestValue;
        }
    }

    public static int[] inputToMove(String s) {
        int m[] = new int[4];

        m[0] = letterToIntChess(s.charAt(0));
        m[1] = Character.getNumericValue(s.charAt(1)) - 1;
        m[2] = letterToIntChess(s.charAt(2));
        m[3] = Character.getNumericValue(s.charAt(3)) - 1;

        return m;
    }

    public static void humanPrompt(boolean both, int player) {
        // Check to seee if two human players
        if(both) {
            System.out.println("Player " + player + " move?");
        }
        else {
            System.out.println("Your move?");
        }
    }

    public static void invalidPrompt() {
        System.out.println("Invalid move.");
    }
    
    public static char intToLetterChess(int a) {
        switch(a) {
            case 0:
                return 'A';
            case 1:
                return 'B';
            case 2:
                return 'C';
            case 3:
                return 'D';
            case 4:
                return 'E';
            case 5:
                return 'F';
            case 6:
                return 'G';
            case 7:
                return 'H';
            default: return 'z';
        }
    }

    public static int letterToIntChess(char a) {
        switch(a) {
            case 'a': 
            case 'A': 
                return 0;
            case 'b': 
            case 'B': 
                return 1;
            case 'c': 
            case 'C': 
                return 2;
            case 'd': 
            case 'D': 
                return 3;
            case 'e': 
            case 'E': 
                return 4;
            case 'f': 
            case 'F': 
                return 5;
            case 'g': 
            case 'G': 
                return 6;
            case 'h': 
            case 'H': 
                return 7;
            default: return 8;
        }
    }

    public static boolean checkInput(String input) {
        if(input.equals("q") || input.equals("Q")) {
            System.exit(0);
        }
        
        if(input.length() != 4) {
            return false;
        }

        char[] parse = new char[4];
        int[] move = new int[4];
        for(int i = 0; i < input.length(); i++) {
            parse[i] = input.charAt(i);
        }

        try {
            move[1] = Character.getNumericValue(parse[1]);
            move[3] = Character.getNumericValue(parse[3]);

            if(move[1] < 1 || move[1] > 8 || move[3] < 1 || move[3] > 8) {
                return false;
            }

            move[0] = letterToIntChess(parse[0]);
            move[2] = letterToIntChess(parse[2]);
            if(move[0] == 8 || move[2] == 8) {
                return false;
            }
        }
        catch(Exception e) {
            return false;
        }

        return true;
    }
    
    public static boolean heuristicWin(ChessState s) {
        // Same as checkWin but without print message
        ChessMoveIterator it = s.iterator(playerTurn);
        try {
            it.moves.size();
        }
        catch(Exception e) {
            return true;
        }
        return false;
    }

    public static boolean checkWin(ChessState s) {
        // Checks to see if either player has any moves left
        // If no moves left, player loses and game ends
        ChessMoveIterator it = s.iterator(true);
        try {
            it.moves.size();
        }
        catch(Exception e) {
            System.out.println("Dark wins!");
            return true;
        }
        it = s.iterator(false);
        try {
            it.moves.size();
        }
        catch(Exception e) {
            System.out.println("Light wins!");
            return true;
        }
        return false;
    }

    public static void main(String[] args) throws Exception {
        Scanner s = new Scanner(System.in);
        String move;
        boolean human1 = false, human2 = false;
        boolean human1help = false, human2help = false;
        
        args = new String[2];
        args[0] = "0";
        args[1] = "2";

        // args[0] - look ahead depth for light player Case 0 will be human.
        String arg1 = args[0];
        int play1 = Integer.valueOf(arg1);
        if(play1 == 0) {
            human1 = true;
            System.out.println("Does player1 want AI help? Y/N");
            move = s.nextLine();
            if(move.equals("Y")) {
                human1help = true;
            }
        }
        
            // Light always go first
        // args[1] - look ahead depth for dark player. Case 0 will be human.
        String arg2 = args[1];
        int play2 = Integer.valueOf(arg2);
        if(play2 == 0) {
            human2 = true;
            System.out.println("Does player2 want AI help? Y/N");
            move = s.nextLine();
            if(move.equals("Y")) {
                human2help = true;
            }
        }

        // Maybe limit depth to 8?
        ChessState cs = new ChessState();
        cs.resetBoard();
        int[] validMove = new int[4];

        while(!checkWin(cs)) {

            playerTurn = false;
            
            // If human player, else AI
            if(human1) {
                if(human1help) {
                    cs.minimax(cs, 8, -9999, 9999, true, true);
                    System.out.println("Suggested move: " + 
                            intToLetterChess(aimove.xSource) + (aimove.ySource +1) + 
                            intToLetterChess(aimove.xDest) + (aimove.yDest +1));
                }
                move = "";
                while(true) {
                    // Print board before each move
                    // If human player, prompt "Your move?"
                    cs.printBoard(System.out);
                    humanPrompt(human2, 1);
                    
                    // User can enter "q" at any time to quit game.
                    move = s.nextLine();
                    
                    // Require 4 character string. e.g. b1c3
                    // If invalid, prompt again
                    if(checkInput(move)) {
                        validMove = inputToMove(move);
                        
                        // Check if move is valid
                        if(cs.isWhite(validMove[0], validMove[1]) && 
                           cs.isValidMove(validMove[0], validMove[1], validMove[2], validMove[3])) {
                            break;
                        }
                    }
                    invalidPrompt();
                }
                
                // Perform move
                cs.move(validMove[0], validMove[1], validMove[2], validMove[3]);
            }
            else {
                cs.printBoard(System.out);
                
                cs.minimax(cs, play1, -9999, 9999, true, true);
                cs.move(aimove.xSource, aimove.ySource, aimove.xDest, aimove.yDest);
            }

            // Quit loop if game is over
            // When game ends, display "Light wins!" or "Dark wins!"
            if(checkWin(cs)) {
                break;
            }

            playerTurn = true;
            
            // If human player, else AI
            if(human2) {
                move = "";
                while(true) {
                    // If human player, prompt "Your move?"
                    cs.printBoard(System.out);
                    humanPrompt(human1, 2);
                    move = s.nextLine();
                    if(checkInput(move)) {
                        validMove = inputToMove(move);
                        if(!cs.isWhite(validMove[0], validMove[1]) && 
                           cs.isValidMove(validMove[0], validMove[1], validMove[2], validMove[3])) {
                            break;
                        }
                    }
                    invalidPrompt();
                }
                cs.move(validMove[0], validMove[1], validMove[2], validMove[3]);
            }
            else {
                cs.printBoard(System.out);
                
                cs.minimax(cs, play2, -9999, 9999, true, false);
                cs.move(aimove.xSource, aimove.ySource, aimove.xDest, aimove.yDest);
            }
        }

//            ChessState s = new ChessState();             // Make a new state
//            s.resetBoard();                              // Initialize to starting setup
//            ChessMoveIterator it = s.iterator(true);     // Iterate over all valid moves
//            
//            Random rand = new Random();
//            
//            ChessState.ChessMove m = null;               //  for the light player
//            while(it.hasNext()) {                        // Find the last valid move.
//                m = it.next();                           // (Obviously, this is not
//            }                                            //  a great strategy.)
//            s.move(m.xSource, m.ySource, m.xDest, m.yDest); // Move the piece
//            int h = s.heuristic(rand);                       // evaluate the state
//            s.printBoard(System.out);                    // print the board

//            s.printBoard(System.out);
//            System.out.println();
//            s.move(1/*B*/, 0/*1*/, 2/*C*/, 2/*3*/);
//            s.printBoard(System.out);
    }
}