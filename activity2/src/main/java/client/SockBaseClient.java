package client;

import java.net.*;
import java.io.*;

import org.json.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Base64;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.net.ServerSocket;

import java.awt.Font;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import buffers.RequestProtos.Request;
import buffers.ResponseProtos.Response;
import buffers.ResponseProtos.Entry;

import java.util.*;
import java.util.stream.Collectors;
import org.json.JSONObject;

class SockBaseClient {
	private JFrame frame = new JFrame("Extra Game For Points");
    private JLabel messageLabel = new JLabel("End of Render...");

    private Square[] board = new Square[9];
    private Square currentSquare;

    private Socket socket;
    private Scanner in;
    private PrintWriter out;
	
	public SockBaseClient(String serverAddress) throws Exception {

        messageLabel.setBackground(Color.lightGray);
        frame.getContentPane().add(messageLabel, BorderLayout.SOUTH);

        JPanel boardPanel = new JPanel();
        boardPanel.setBackground(Color.black);
        boardPanel.setLayout(new GridLayout(3, 3, 2, 2));
        for (int i = 0; i < board.length; i++) {
            final int j = i;
            board[i] = new Square();
            board[i].addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    currentSquare = board[j];
                    out.println("MOVE " + j);
                }
            });
            boardPanel.add(board[i]);
        }
        frame.getContentPane().add(boardPanel, BorderLayout.CENTER);
    }
	/**
     * Function JSONObject add().
     */
    public static JSONObject leader() {
		JSONObject request = null;
        // Build the first request object just including the name
        Request op = Request.newBuilder()
                .setOperationType(Request.OperationType.LEADER)
				.setName("name").build();
        return request;
    }
	
	public static JSONObject newGame() {
		JSONObject request = null;

        // Build the first request object just including the name
        Request op = Request.newBuilder()
                .setOperationType(Request.OperationType.NEW)
				.setName("name").build();
        return request;
    }
	
	/**
     * The main thread of the client will listen for messages from the server. The
     * first message will be a "WELCOME" message in which we receive our mark. Then
     * we go into a loop listening for any of the other messages, and handling each
     * message appropriately. The "VICTORY", "DEFEAT", "TIE", and
     * "OTHER_PLAYER_LEFT" messages will ask the user whether or not to play another
     * game. If the answer is no, the loop is exited and the server is sent a "QUIT"
     * message.
     */
    public void play() throws Exception {
        try {
            String response = in.nextLine();
            char mark = response.charAt(8);
            char opponentMark = mark == 'X' ? 'O' : 'X';
            frame.setTitle("Tic Tac Toe: Player " + mark);
            while (in.hasNextLine()) {
                response = in.nextLine();
                if (response.startsWith("VALID_MOVE")) {
                    messageLabel.setText("Valid move, please wait");
                    currentSquare.setText(mark);
                    currentSquare.repaint();
                } else if (response.startsWith("OPPONENT_MOVED")) {
                    int loc = Integer.parseInt(response.substring(15));
                    board[loc].setText(opponentMark);
                    board[loc].repaint();
                    messageLabel.setText("Opponent moved, your turn");
                } else if (response.startsWith("MESSAGE")) {
                    messageLabel.setText(response.substring(8));
                } else if (response.startsWith("VICTORY")) {
                    JOptionPane.showMessageDialog(frame, "Winner Winner");
                    break;
                } else if (response.startsWith("DEFEAT")) {
                    JOptionPane.showMessageDialog(frame, "Sorry you lost");
                    break;
                } else if (response.startsWith("TIE")) {
                    JOptionPane.showMessageDialog(frame, "Tie");
                    break;
                } else if (response.startsWith("OTHER_PLAYER_LEFT")) {
                    JOptionPane.showMessageDialog(frame, "Other player left");
                    break;
                }
            }
            out.println("QUIT");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            socket.close();
            frame.dispose();
        }
    }
	
	static class Square extends JPanel {
        JLabel label = new JLabel();

        public Square() {
            setBackground(Color.white);
            setLayout(new GridBagLayout());
            label.setFont(new Font("Arial", Font.BOLD, 40));
            add(label);
        }

        public void setText(char text) {
            label.setForeground(text == 'X' ? Color.BLUE : Color.RED);
            label.setText(text + "");
        }
    }

    public static void main (String args[]) throws Exception {
        Socket serverSock = null;
        OutputStream out = null;
        InputStream in = null;
        int i1=0, i2=0;
        int port = 9099; // default port

        // Make sure two arguments are given
        if (args.length != 2) {
            System.out.println("Expected arguments: <host(String)> <port(int)>");
            System.exit(1);
        }
        String host = args[0];
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException nfe) {
            System.out.println("[Port] must be integer");
            System.exit(2);
        }

        // Ask user for username
        System.out.println("Please provide your name for the server ");
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        String strToSend = stdin.readLine();

        // Build the first request object just including the name
        Request op = Request.newBuilder()
                .setOperationType(Request.OperationType.NAME)
                .setName(strToSend).build();
        Response response;
        try {
            // connect to the server
            serverSock = new Socket(host, port);

            // write to the server
            out = serverSock.getOutputStream();
            in = serverSock.getInputStream();

            op.writeDelimitedTo(out);

            // read from the server
            response = Response.parseDelimitedFrom(in);

            // print the server response. 
            System.out.println(response.getGreeting());
			Scanner input = new Scanner(System.in);
			int choice;
			do {
                System.out.println();
                System.out.println("Client Menu");
                System.out.println("Please select a valid option (1-3). 3 to diconnect the client");
                System.out.println("1. leaderboard");
                System.out.println("2. play game");
                System.out.println("3. Quit - quit game");
                System.out.println();
                choice = input.nextInt(); // what if not int.. should error handle this
                JSONObject request = null;
                switch (choice) {
                    case (1):
                        request = leader();
                        break;
                    case (2):
                        request = newGame();
                        break;
                    case (3):
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Please select a valid option (1-3).");
                        break;
                }
                if (request != null) {
                    System.out.println(request);
                    NetworkUtils.send(out, JsonUtils.toByteArray(request));
                    byte[] responseBytes = NetworkUtils.receive(in);
                    JSONObject response1 = JsonUtils.fromByteArray(responseBytes);

                    if (response1.has("error")) {
                        System.out.println(response1.getString("error"));
                    } else {
                        System.out.println();
                        System.out.println("The response from the server: ");
                        System.out.println("datatype: " + response1.getString("type"));
                        System.out.println("data: " + response1.getString("data"));
                        System.out.println();
                        String typeStr = (String) response1.getString("type");
                        if (typeStr.equals("quit")) {
                            serverSock.close();
                            out.close();
                            in.close();
                            System.exit(0);
                        }
                    }
                }else{
					SockBaseClient client = new SockBaseClient(args[0]);
                    client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					client.frame.setSize(320, 320);
					client.frame.setVisible(true);
					client.frame.setResizable(false);
					client.play();
				}
            } while (true);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null)   in.close();
            if (out != null)  out.close();
            if (serverSock != null) serverSock.close();
        }
    }
}


