//FULL CODEEEE :D
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;

public class SigmaBeats {
    public static Scanner input = new Scanner(System.in);
    public static ArrayList<User> userList = new ArrayList<>();
    public static boolean isPaused = false, isRunning = true;

    public static void main(String[] args) {

        try (BufferedReader br = new BufferedReader(new FileReader("users.csv"))) {
            boolean first = true;
            String line;
            while((line = br.readLine()) != null){
                if (first) {
                    first = false;
                    continue;
                }
                String[] userInfo = line.split(",");
                userList.add(new User(userInfo[0], userInfo[1]));
            }
        }catch(IOException e){
            System.out.println("failed to load saved user list");
        }
        for(int x = 0; x<userList.size(); x++){
            File folder = new File("data/" + userList.get(x).username);
            File[] files = folder.listFiles();
            if (files != null) {
                for(int y = 0; y<files.length; y++){
                    userList.get(x).playlists.add(new Playlist(files[y].getName()));

                    try (BufferedReader br = new BufferedReader(new FileReader("data/" + userList.get(x).username + "/" + userList.get(x).playlists.get(y).title))) {
                        boolean first = true;
                        String line;
                        while((line = br.readLine()) != null){
                            if (first) {
                                first = false;
                                continue;
                            }
                            String[] songInfo = line.split(",");
                            userList.get(x).playlists.get(y).songList.add(new Song(songInfo[0], songInfo[1], songInfo[2], songInfo[3], Integer.parseInt(songInfo[4]), new ArrayList<>(Arrays.asList(songInfo[5].split("/")))));
                        }
                    }catch(IOException e){
                        System.out.println("failed to load saved user list");
                    }
                }
            }
        }


        while (true) {
            if (!welcome()) {
                System.out.println("Have a sigma day!");
                return;
            }

            int userReference = login();
            if (userReference == 0) continue;

            while (true) {
                String playlistAction = masterPortfolio(userReference);
                if (playlistAction.equals("L")) break;

                int playlistIndex = handlePlaylistAction(userReference, playlistAction);
                if (playlistIndex == -1) continue;

                while (true) {
                    String songAction = songsPortfolio(userReference, playlistIndex);
                    if (songAction.equals("L")) break;

                    int songIndex = handleSongAction(userReference, playlistIndex, songAction);
                    if (songIndex == -1) continue;

                    handleSongPlayback(userReference, playlistIndex, songIndex);
                }
            }
        }
    }

    public static boolean welcome() {
        return mcq("Welcome to SigmaBeats! Login (Y/N)?", "Y", "N").equals("Y");
    }

    public static void saveUser(User user) {
        try (FileWriter writer = new FileWriter("users.csv", true)) {
            writer.write(user.username + "," + user.password + "\n");
        }catch (IOException e) {
            System.out.println("Error saving user");
        }
        File userFolder = new File("data/" + user.username);
        boolean isCreated = userFolder.mkdir();
    }

    public static int login() {
        while (true) {
            String action = mcq("Sign-In or Login (S/L)?", "S", "L");

            if (action.equals("S")) {
                String username = noEmptyString("Type in Username:");
                String password = noEmptyString("Create a Password:");
                userList.add(new User(username, password));
                saveUser(new User(username, password));
                return userList.size() - 1;
            } else {
                while (true) {
                    if(userList.isEmpty()){
                        System.out.println("login failed");
                        return -1;
                    }
                    String username = noEmptyString("Type in Username:");
                    String password = noEmptyString("Type in Password:");
                    for (int i = 0; i < userList.size(); i++) {
                        if (userList.get(i).username.equals(username) && userList.get(i).password.equals(password)) {
                            return i;
                        }
                    }
                    if (mcq("Login failed. Try again (T) or Escape (E)?", "T", "E").equals("E")) {
                        return -1;
                    }
                }
            }
        }
    }

    public static String masterPortfolio(int userReference) {
        System.out.println("Playlists:");
        ArrayList<Playlist> playlists = userList.get(userReference).playlists;
        if (playlists.isEmpty()) {
            System.out.println("No Playlists Created!");
        } else {
            for (int i = 0; i < playlists.size(); i++) {
                System.out.println((i + 1) + ". " + playlists.get(i).title);
            }
        }
        return mcq("Create Playlist, Play Playlist, or Logout (C/P/L)?", "C", "P", "L");
    }

    public static int handlePlaylistAction(int userReference, String action) {
        ArrayList<Playlist> playlists = userList.get(userReference).playlists;
        if (action.equals("C")) {
            createPlaylist(userReference);
            return playlists.size() - 1;
        } else if (action.equals("P")) {
            if (playlists.isEmpty()) {
                System.out.println("No playlists to play! Create a playlist first.");
                return -1;
            }
            return noEmptyInteger("Select a playlist by number:") - 1;
        }
        return -1;
    }

    public static String songsPortfolio(int userReference, int playlistIndex) {
        Playlist playlist = userList.get(userReference).playlists.get(playlistIndex);
        System.out.println("Songs in Playlist:");
        if (playlist.songList.isEmpty()) {
            System.out.println("No Songs added!");
        } else {
            for (int i = 0; i < playlist.songList.size(); i++) {
                Song song = playlist.songList.get(i);
                System.out.println((i + 1) + ". " + song.title + " by " + song.artist);
            }
        }
        return mcq("Add Song, Play Song, sort songs, or Leave Song Portfolio (A/P/S/L)?", "A", "S", "P", "L");
    }

    public static int handleSongAction(int userReference, int playlistIndex, String action) {
        Playlist playlist = userList.get(userReference).playlists.get(playlistIndex);
        if (action.equals("A")) {
            createSong(userReference, playlistIndex);
            return playlist.songList.size() - 1;
        } else if (action.equals("P")) {
            if (playlist.songList.isEmpty()) {
                System.out.println("No songs to play! Add a song first.");
                return -1;
            }
            return noEmptyInteger("Select a song by number:") - 1;
        }else if(action.equals("S")){
            sortSongs(userReference, playlistIndex);
            return -1;
        }
        return -1;
    }

    public static void handleSongPlayback(int userReference, int playlistIndex, int songIndex) {
        isRunning = true;
        isPaused = false;
        Song song = userList.get(userReference).playlists.get(playlistIndex).songList.get(songIndex);

        Thread songThread = new Thread(() -> {
            int remaining = song.duration;
            while (remaining > 0 && isRunning) {
                if (!isPaused) {
                    System.out.println("Time left: " + remaining + " seconds");
                    song.ad();
                    remaining--;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {}
                }
            }
            if (remaining == 0) System.out.println("Song finished!");
        });

        songThread.start();

        while (isRunning) {
            char command = noEmptyString("Enter 'p' to pause, 'u' to unpause, or 'q' to quit:").toLowerCase().charAt(0);
            if (command == 'p') isPaused = true;
            else if (command == 'u') isPaused = false;
            else if (command == 'q') {
                isRunning = false;
                songThread.interrupt();
            }
        }
    }

    public static void createPlaylist(int userReference) {
        String title = noEmptyString("Type in a name for the new Playlist:");
        userList.get(userReference).playlists.add(new Playlist(title));
        File file = new File("data/"+ userList.get(userReference).username + "/" + title + ".csv");
        try (FileWriter writer = new FileWriter(file, true)) {
            writer.write("Title,Artist,Mood,Language,Duration,Date\n");
        }catch(IOException e) {
            System.out.println("Error saving playlist");
        }
    }

    public static void createSong(int userReference, int playlistIndex) {
        String title = noEmptyString("Type in the name of the Song:");
        String artist = noEmptyString("Type in the name of the Artist:");
        int duration = noEmptyInteger("Type in the duration of the Song (in seconds):");
        ArrayList<String> date = new ArrayList<>();
        date.add(noEmptyString("Month:"));
        date.add(noEmptyString("Day:"));
        date.add(noEmptyString("Year:"));
        String language = noEmptyString("Language:");
        String mood = noEmptyString("Mood:");
        Song song = new Song(title, artist, mood, language, duration, date);
        userList.get(userReference).playlists.get(playlistIndex).songList.add(song);
        try (FileWriter writer = new FileWriter("data/" + userList.get(userReference).username + "/" + userList.get(userReference).playlists.get(playlistIndex).title + ".csv", true)){
            writer.write(song.title + "," + song.artist + "," + song.mood + "," + song.language + "," + song.duration + "," + String.join("/", song.date) + "\n");
        }catch(IOException e){
            System.out.println("error saving song");
        }
    }

    public static String mcq(String question, String nOne, String nTwo) {
        while (true) {
            System.out.print(question + " ");
            String answer = input.nextLine().toUpperCase();
            if (answer.equals(nOne) || answer.equals(nTwo)){
                return answer;
            }
            System.out.println("Please choose either " + nOne + " or " + nTwo + ".");
        }
    }

    public static String mcq(String question, String nOne, String nTwo, String nThree) {
        while (true) {
            System.out.print(question + " ");
            String answer = input.nextLine().toUpperCase();
            if (answer.equals(nOne) || answer.equals(nTwo) || answer.equals(nThree)){
                return answer;
            }
            System.out.println("Please choose either " + nOne + ", " + nTwo + ", or " + nThree + ".");
        }
    }
    public static String mcq(String question, String nOne, String nTwo, String nThree, String nFour) {
        while (true) {
            System.out.print(question + " ");
            String answer = input.nextLine().toUpperCase();
            if (answer.equals(nOne) || answer.equals(nTwo) || answer.equals(nThree) || answer.equals(nFour)){
                return answer;
            }
            System.out.println("Please choose either " + nOne + ", " + nTwo + ", or " + nThree + ".");
        }
    }

    public static String noEmptyString(String question) {
        while (true) {
            System.out.print(question + " ");
            String answer = input.nextLine();
            if (!answer.isEmpty()) return answer;
            System.out.println("Input cannot be empty try again.");
        }
    }

    public static int noEmptyInteger(String question) {
        while (true) {
            try {
                System.out.print(question + " ");
                int answer = input.nextInt();
                input.nextLine();
                if (answer > 0) return answer;
                System.out.println("Input must be a positive integer try againnnnnn");
            } catch (Exception e) {
                System.out.println("Please enter a positive integer }:(");
                input.nextLine();
            }
        }
    }

    public static void sortSongs(int userReference, int playlistIndex) {
        Playlist playlist = userList.get(userReference).playlists.get(playlistIndex);

        if (playlist.songList.isEmpty()) {
            System.out.println("No songs to sort!");
            return;
        }

        String answer = mcq( "Sort songs by Title (T), Artist (A), Mood (M)", "T", "A", "M");

        switch (answer){
            case "T":
                Collections.sort(playlist.songList, new Comparator<Song>() {
                    @Override
                    public int compare(Song s1, Song s2) {
                        return s1.title.toLowerCase().compareTo(s2.title.toLowerCase());
                    }
                });
                System.out.println("sorted by title");
                break;
            case "A":
                Collections.sort(playlist.songList, new Comparator<Song>() {
                    @Override
                    public int compare(Song s1, Song s2) {
                        return s1.artist.toLowerCase().compareTo(s2.artist.toLowerCase());
                    }
                });
                System.out.println("sorted by artist");
                break;
            case "M":
                Collections.sort(playlist.songList, new Comparator<Song>() {
                    @Override
                    public int compare(Song s1, Song s2) {
                        return s1.mood.toLowerCase().compareTo(s2.mood.toLowerCase());
                    }
                });
                System.out.println("sorted by mood");
                break;
            default:
                System.out.println("not possible");

        }

        for (int i = 0; i < playlist.songList.size(); i++) {
            Song song = playlist.songList.get(i);
            System.out.println((i + 1) + " " + song.title + " by " + song.artist);
        }

    }
}

class User {
    public String username;
    public String password;
    public ArrayList<Playlist> playlists = new ArrayList<>();

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
    public int totalSongDuration() {
        int totalDuration = 0;
        if(playlists.size()>0) {
            for(int i = 0; i<playlists.size(); i++) {
                if(playlists.get(i).songList.size()>0) {
                    for(int k = 0; k<playlists.get(i).songList.size(); k++) {
                        totalDuration += playlists.get(i).songList.get(k).duration;
                    }
                }
            }
        }
        return totalDuration;
    }
}

class Playlist {
    public String title;
    public ArrayList<Song> songList = new ArrayList<>();

    public Playlist(String title) {
        this.title = title;
    }
    public int totalSongListDuration() {
        int totalDuration = 0;
        if(songList.size()>0) {
            for(int i = 0; i<songList.size(); i++) {
                totalDuration += songList.get(i).duration;
            }
        }
        return totalDuration;
    }
}

class Song {
    public String title, artist, mood, language;
    public int duration;
    public ArrayList<String> date;
    private String[] line;
    private String[] line2;
    private int linenum = 0;
    private int adnum = 0;



    public Song(String title, String artist, String mood, String language, int duration, ArrayList<String> date) {
        this.title = title;
        this.artist = artist;
        this.mood = mood;
        this.language = language;
        this.duration = duration;
        this.date = date;
    }
    public String durationConvert() {
        return duration/60 + ":" + duration%60;
    }
    public void ad(){
        try (BufferedReader br = new BufferedReader(new FileReader("ads.csv"))) {

            if(adnum == 0){
                line = br.readLine().split(",");
            }
            else if(adnum == 1){
                line = br.readLine().split(",");
                line = br.readLine().split(",");
            }
            else{
                line = br.readLine().split(",");
                line = br.readLine().split(",");
                line = br.readLine().split(",");
            }

            line2 = line[1].split("\\|");
            if(linenum == 0){
                System.out.println(line[0] + " by the company " + line[2]);
                linenum++;
            }
            if(linenum <= line2.length){
                System.out.println(line2[linenum-1]);
                if(linenum == line2.length){
                    linenum = 0;
                    if(adnum < 3){
                        adnum++;
                    }
                    else{
                        adnum = 0;
                    }
                }
                linenum++;
            }

        }catch(IOException e){
            System.out.println("error getting ad");
        }
    }
}
