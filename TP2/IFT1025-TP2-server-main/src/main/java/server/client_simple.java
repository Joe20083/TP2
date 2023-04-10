package server;

import server.models.Course;

import java.io.*;
import java.net.*;
import java.util.*;
public class client_simple {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 1337);
        PrintWriter inscription = new PrintWriter(socket.getOutputStream());
        inscription.println("Bienvenue au portail d'inscription de cours de l'UDEM");
        inscription.flush();
        InputStreamReader read = new InputStreamReader(socket.getInputStream());
        BufferedReader data = new BufferedReader(read);
        
        String dataEntered = data.readline();
    }
   
    }

