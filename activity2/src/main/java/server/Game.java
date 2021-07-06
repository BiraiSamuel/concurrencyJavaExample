package server;
import java.util.Scanner;
import java.util.*; 
import java.io.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.Executors;

/**
 * Class: Game 
 * Description: Game class that can load an ascii image
 * Class can be used to hold the persistent state for a game for different threads
 * synchronization is not taken care of .
 * You can change this Class in any way you like or decide to not use it at all
 * I used this class in my SockBaseServer to create a new game and keep track of the current image evenon differnt threads. 
 * My threads each get a reference to this Game
 */

public class Game {
    private int idx = 0; // current index where x could be replaced with original
    private int idxMax; // max index of image
    private char[][] original; // the original image
    private char[][] hidden; // the hidden image
    private int col; // columns in original, approx
    private int row; // rows in original and hidden
    private boolean won; // if the game is won or not
    private List<String> files = new ArrayList<String>(); // list of files, each file has one image


    public Game(){
        // you can of course add more or change this setup completely. You are totally free to also use just Strings in your Server class instead of this class
        won = true; // setting it to true, since then in newGame() a new image will be created
        files.add("pig.txt");
        files.add("snail.txt");
        files.add("duck.txt");
        files.add("crab.txt");
        files.add("cat.txt");
        files.add("joke1.txt");
        files.add("joke2.txt");
        files.add("joke3.txt");
    }

    /**
     * Sets the won flag to true
     * @param args Unused.
     * @return Nothing.
     */
    public void setWon(){
        won = true;
    }

    /**
     * Method loads in a new image from the specified files and creates the hidden image for it. 
     * @return Nothing.
     */
    public void newGame(){
        if (won) {
            idx = 0;
            won = false; 
            List<String> rows = new ArrayList<String>();

            try{
                // loads one random image from list
                Random rand = new Random(); 
                col = 0;
                int randInt = rand.nextInt(files.size());
                File file = new File(
                        Game.class.getResource("/"+files.get(randInt)).getFile()
                        );
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null) {
                    if (col < line.length()) {
                        col = line.length();
                    }
                    rows.add(line);
                }
            }
            catch (Exception e){
                System.out.println("File load error"); // extremely simple error handling, you can do better if you like. 
            }

            // this handles creating the orinal array and the hidden array in the correct size
            String[] rowsASCII = rows.toArray(new String[0]);

            row = rowsASCII.length;

            // Generate original array by splitting each row in the original array.
            original = new char[row][col];
            for(int i = 0; i < row; i++) {
                char[] splitRow = rowsASCII[i].toCharArray();
                for (int j = 0; j < splitRow.length; j++) {
                    original[i][j] = splitRow[j];
                }
            }

            // Generate Hidden array with X's (this is the minimal size for columns)
            hidden = new char[row][col];
            for(int i = 0; i < row; i++){
                for(int j = 0; j < col; j++){
                    hidden[i][j] = 'X';
                }
            }
            setIdxMax(col * row);
        }
        else {
        }
    }

    /**
     * Method returns the String of the current hidden image
     * @return String of the current hidden image
     */
    public String getImage(){
        StringBuilder sb = new StringBuilder();
        for (char[] subArray : hidden) {
            sb.append(subArray);
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Method changes the next idx of the hidden image to the character in the original image
     * You can change this method if you want to turn more than one x to the original
     * @return String of the current hidden image
     */
    public String replaceOneCharacter() {
        int colNumber = idx%col;
        int rowNumber = idx/col;
        hidden[rowNumber][colNumber] = original[rowNumber][colNumber];
        idx++;
        return(getImage());
    }

    public int getIdxMax() {
        return idxMax;
    }

    public void setIdxMax(int idxMax) {
        this.idxMax = idxMax;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }
	
	// Board cells numbered 0-8, top to bottom, left to right; null if empty
    private Player[] board = new Player[9];

    Player currentPlayer;

    public boolean hasWinner() {
        return (board[0] != null && board[0] == board[1] && board[0] == board[2])
                || (board[3] != null && board[3] == board[4] && board[3] == board[5])
                || (board[6] != null && board[6] == board[7] && board[6] == board[8])
                || (board[0] != null && board[0] == board[3] && board[0] == board[6])
                || (board[1] != null && board[1] == board[4] && board[1] == board[7])
                || (board[2] != null && board[2] == board[5] && board[2] == board[8])
                || (board[0] != null && board[0] == board[4] && board[0] == board[8])
                || (board[2] != null && board[2] == board[4] && board[2] == board[6]);
    }

    public boolean boardFilledUp() {
        return Arrays.stream(board).allMatch(p -> p != null);
    }

    public synchronized void move(int location, Player player) {
        if (player != currentPlayer) {
            throw new IllegalStateException("Not your turn");
        } else if (player.opponent == null) {
            throw new IllegalStateException("You don't have an opponent yet");
        } else if (board[location] != null) {
            throw new IllegalStateException("Cell already occupied");
        }
        board[location] = currentPlayer;
        currentPlayer = currentPlayer.opponent;
    }
	
	/**
     * A Player is identified by a character mark which is either 'X' or 'O'. For
     * communication with the client the player has a socket and associated Scanner
     * and PrintWriter.
     */
    class Player implements Runnable {
        char mark;
        Player opponent;
        Socket socket;
        Scanner input;
        PrintWriter output;

        public Player(Socket socket, char mark) {
            this.socket = socket;
            this.mark = mark;
        }

        @Override
        public void run() {
            try {
                setup();
                processCommands();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (opponent != null && opponent.output != null) {
                    opponent.output.println("OTHER_PLAYER_LEFT");
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }

        private void setup() throws IOException {
            input = new Scanner(socket.getInputStream());
            output = new PrintWriter(socket.getOutputStream(), true);
            output.println("WELCOME " + mark);
            if (mark == 'X') {
                currentPlayer = this;
                output.println("MESSAGE Waiting for opponent to connect");
            } else {
                opponent = currentPlayer;
                opponent.opponent = this;
                opponent.output.println("MESSAGE Your move");
            }
        }

        private void processCommands() {
            while (input.hasNextLine()) {
                String command = input.nextLine();
                if (command.startsWith("QUIT")) {
                    return;
                } else if (command.startsWith("MOVE")) {
                    processMoveCommand(Integer.parseInt(command.substring(5)));
                }
            }
        }

        private void processMoveCommand(int location) {
            try {
                move(location, this);
                output.println("VALID_MOVE");
                opponent.output.println("OPPONENT_MOVED " + location);
                if (hasWinner()) {
                    output.println("VICTORY");
                    opponent.output.println("DEFEAT");
                } else if (boardFilledUp()) {
                    output.println("TIE");
                    opponent.output.println("TIE");
                }
            } catch (IllegalStateException e) {
                output.println("MESSAGE " + e.getMessage());
            }
        }
    }
}
