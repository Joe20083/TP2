package server;

import javafx.util.Pair;
import server.models.Course;
import server.models.RegistrationForm;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;


public class Server {

    public final static String REGISTER_COMMAND = "INSCRIRE";
    public final static String LOAD_COMMAND = "CHARGER";
    private final ServerSocket server;
    private Socket client;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private final ArrayList<EventHandler> handlers;

    public Server(int port) throws IOException {
        this.server = new ServerSocket(port, 1);
        this.handlers = new ArrayList<EventHandler>();
        this.addEventHandler(this::handleEvents);
    }

    public void addEventHandler(EventHandler h) {
        this.handlers.add(h);
    }

    private void alertHandlers(String cmd, String arg) {
        for (EventHandler h : this.handlers) {
            h.handle(cmd, arg);
        }
    }

    public void run() {
        while (true) {
            try {
                client = server.accept();
                System.out.println("Connecté au client: " + client);
                objectInputStream = new ObjectInputStream(client.getInputStream());
                objectOutputStream = new ObjectOutputStream(client.getOutputStream());
                listen();
                disconnect();
                System.out.println("Client déconnecté!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void listen() throws IOException, ClassNotFoundException {
        String line;
        if ((line = this.objectInputStream.readObject().toString()) != null) {
            Pair<String, String> parts = processCommandLine(line);
            String cmd = parts.getKey();
            String arg = parts.getValue();
            this.alertHandlers(cmd, arg);
        }
    }

    public Pair<String, String> processCommandLine(String line) {
        String[] parts = line.split(" ");
        String cmd = parts[0];
        String args = String.join(" ", Arrays.asList(parts).subList(1, parts.length));
        return new Pair<>(cmd, args);
    }

    public void disconnect() throws IOException {
        objectOutputStream.close();
        objectInputStream.close();
        client.close();
    }

    public void handleEvents(String cmd, String arg) {
        if (cmd.equals(REGISTER_COMMAND)) {
            handleRegistration();
        } else if (cmd.equals(LOAD_COMMAND)) {
            handleLoadCourses(arg);
        }
    }

    /**
     Lire un fichier texte contenant des informations sur les cours et les transofmer en liste d'objets 'Course'.
     La méthode filtre les cours par la session spécifiée en argument.
     Ensuite, elle renvoie la liste des cours pour une session au client en utilisant l'objet 'objectOutputStream'.
     @param arg la session pour laquelle on veut récupérer la liste des cours
     @throws Exception si une erreur se produit lors de la lecture du fichier ou de l'écriture de l'objet dans le flux
     */
    public void handleLoadCourses(String arg) {
        try {
            List<Course> courses = new ArrayList<>();
            File cours = new File("cours.txt");
            Scanner scanner = new Scanner(cours);
            //System.out.println("test");
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] sections = line.split(",");
                Course detailedCourses = new Course(sections[1], sections[0], sections[2]);
                courses.add(detailedCourses);
            }
            scanner.close();

            List<Course> filteredCours = courses.stream()
                    .filter(course -> course.getSession().equals(arg))
                    .collect(Collectors.toList());
            objectOutputStream.writeObject(filteredCours);
        } catch (IOException error) {
            System.err.println("Erreur de lecture ou écriture du fichier en question");
        }
    }


    /**
     Récupérer l'objet 'RegistrationForm' envoyé par le client en utilisant 'objectInputStream', l'enregistrer dans un fichier texte
     et renvoyer un message de confirmation au client.
     @throws Exception si une erreur se produit lors de la lecture de l'objet, l'écriture dans un fichier ou dans le flux de sortie.
     */
    public void handleRegistration() {
        try {
            Course coursesInfo = (Course) objectInputStream.readObject();
            RegistrationForm coursesRegistered = (RegistrationForm) objectInputStream.readObject();
            String inscription = coursesInfo.getSession() + " " +
                    coursesInfo.getCode() + " " +
                    coursesRegistered.getMatricule() + " " +
                    coursesRegistered.getNom() + " " +
                    coursesRegistered.getEmail();

            BufferedWriter writer = new BufferedWriter(new FileWriter("inscription.txt"));
            writer.write(inscription);
            writer.newLine();
            writer.close();

            objectOutputStream.writeObject("Enregistrement confirmé");
            objectOutputStream.flush();
        } catch (IOException error) {
            System.err.println("Erreur de lecture ou écriture du fichier en question");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}




