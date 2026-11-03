package ec.edu.monster.vista.utils;

import java.io.BufferedReader;
import java.io.Console;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

//revisado y aprovado por Marcos Escobar
public final class MessagesUtils {
  private static final int SEPARATOR_LENGTH = 15;
  private static final BufferedReader ENTRADA =
    new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

  private MessagesUtils() {
  }

  /** Acentos de los mensajes del servidor y el arte de inicio: la consola de Windows pasa a UTF-8. */
  public static void prepararConsola() {
    boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");

    if (windows && System.console() != null) {
      try {
        new ProcessBuilder("cmd", "/c", "chcp", "65001")
          .redirectOutput(ProcessBuilder.Redirect.DISCARD)
          .redirectError(ProcessBuilder.Redirect.DISCARD)
          .start()
          .waitFor();
      } catch (IOException e) {
        // Si no se puede cambiar la página de códigos, la consola solo mostrará mal algunos caracteres.
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

    System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
  }

  public static void printMonster() {
    System.out.println("""

        
  ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣠⡾⡛⠷⣂⣄⠀⢀⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣾⠟⢀⣼⣿⡿⠿⢷⡿⠓⠿⣖⣤⣤⣤⢄⣀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢻⣦⡸⣤⣀⣀⣀⡾⢥⡀⠀⠙⡿⣽⢿⣌⠉⢻⣦⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣠⣼⠟⢋⣭⡟⣏⠷⠖⣽⣦⢀⡇⡬⢿⠿⡈⢹⡟⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⣽⣶⣶⠾⠟⠚⣽⠒⠉⢦⠽⠟⠸⡐⣿⡨⠈⠉⠀⡇⠀⢣⠈⣺⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡿⣿⠁⠠⡀⠀⠀⠙⠧⣤⣬⣆⠀⠀⠀⠀⠀⠀⠀⠀⠈⠓⣾⡶⠗⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢰⢿⣏⠀⠀⠈⠲⣄⠀⠀⠀⠀⠀⢀⡀⠀⢀⠽⠒⠀⠀⠀⠀⢹⣣⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⢹⣯⠀⠀⠀⠀⠀⠙⠲⢄⡴⣢⣆⠼⠚⠁⠀⠀⠀⡄⠀⠀⠀⠻⣧⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢠⣤⣴⣶⣄⣤⣤⣤⣼⣿⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⡇⠀⠀⠀⠀⠙⢾⣆⢄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢈⣿⠶⠀⠉⠀⠀⠀⠀⠘⣦⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣼⠀⠀⠀⢰⠀⠀⠀⠈⠻⢮⣖⠄⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⣵⠖⠀⠀⠀⠀⠀⠀⠀⠈⠙⢦⣄⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣰⠇⠀⠀⠀⠈⠀⠀⠀⠀⠀⠀⠈⠛⠾⣔⡄⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⢀⣤⣴⡷⠧⠤⠤⣤⣄⡀⡀⠀⠀⠀⠀⠀⢳⠈⠉⠀⠀⠀⠀⠠⣤⡤⠴⡺⠋⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⠤⠀⠈⠙⠾⣕⣄⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⢀⢰⣿⡵⠛⠲⠦⠤⣄⠀⠀⠙⣧⠀⠀⠀⠀⠀⠀⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠂⠀⠀⠀⠀⠀⠀⠈⠻⢯⡦⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⢠⣾⣯⠼⠤⠤⣄⠀⠀⠈⣷⣀⣀⣼⣧⡤⠴⠶⠶⠶⠶⠶⠒⡒⠒⠒⠒⠲⢤⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠻⣯⣤⡀⠀⠀⠀⠀
⠀⠀⠀⣰⡟⣹⡇⠀⠀⣀⣬⠷⠚⠛⠉⠉⠁⢰⠀⠀⠀⠀⠀⠀⠀⢀⠔⠁⠀⠀⠀⠀⠀⠙⢦⡀⠀⠀⠀⠀⠀⡰⠀⠀⠀⠀⠀⠀⠀⠀⠐⠒⠤⣀⠀⠀⢰⣾⣶⠃⠀⠀⠀⠀
⠀⠀⢀⣧⠟⠁⢙⠷⠋⠁⠀⠀⠀⠀⠀⠀⠀⡞⠀⠀⠀⠀⠀⠀⠀⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠹⣄⠀⠀⢠⡞⠁⠀⠀⡀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠃⣼⣼⣿⠉⠀⠀⠀⠀⠀
⠀⣰⡿⠁⠀⠀⡔⠀⠀⠀⠀⠀⠀⠀⠀⠀⡜⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⡠⠀⠀⠀⠈⢣⡴⠋⠀⠀⠀⠐⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣰⣻⠿⠃⠀⠀⠀⠀⠀⠀
⣼⠞⠀⠀⠀⠀⠁⠀⠀⠀⠀⠀⠀⠀⠠⠊⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣠⠖⠛⠛⠛⠒⠲⠶⢯⣤⣀⣀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣰⣻⠁⠀⠀⠀⠀⠀⠀⠀⠀
⣼⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣠⡴⠛⠁⠀⠀⠀⠀⠀⠀⠀⠀⠰⡄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣰⡻⠃⠀⠀⠀⠀⠀⠀⠀⠀⠀
⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⡴⠞⠉⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢠⠃⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣼⡛⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠘⣿⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⣴⠞⠋⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠐⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣾⠝⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠻⣷⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⣤⡴⠟⠻⣄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣠⣟⠋⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠹⡿⣄⠀⢀⣀⣀⣀⣀⣠⣤⣴⡶⠛⠉⠁⠀⠀⠀⠈⠳⣄⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣼⡯⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠈⠫⢷⣾⣿⢻⠿⠛⠉⠉⣿⠇⠀⠀⠠⠀⠂⠈⠀⢀⠈⠻⣦⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣰⠿⣿⡁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠈⠉⠁⠀⠀⠀⢠⣿⠀⠀⠐⠀⠀⠄⠐⠈⠀⠀⠀⠀⠙⢷⣤⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣠⡾⠋⠀⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢨⣿⠀⠀⠄⠂⠀⠠⠀⢀⠐⠈⠀⠐⠀⠀⠈⠛⠷⣤⣀⠀⠀⠀⠀⠀⠀⠀⠀⢀⡴⠏⠀⠀⠀⢿⣧⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢨⣟⠀⠀⢀⠠⠐⠀⢀⠀⠀⠠⠐⠀⠀⠁⡀⠀⠀⠀⢉⣻⡷⢆⣀⣀⠀⣠⡶⠋⠀⢀⠀⠀⠀⢸⡾⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢨⣟⠀⠀⠀⠀⡀⠠⠀⠀⠈⠀⢀⠠⠈⠀⠀⡀⠁⠠⠀⠈⠉⠉⣽⠷⠚⠉⠀⠀⠀⠈⠒⠐⠊⠀⣷⡆⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⣿⣿⠀⠈⠀⠀⢀⠀⠈⠀⠐⠀⠀⢀⠀⠁⠀⠀⠄⠀⠄⠀⠄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⢳⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠿⣿⣷⡀⠀⠁⠀⢀⠀⠁⠀⠄⠈⠀⠀⠠⠈⠀⠀⠄⠀⠠⠀⠐⠀⠀⠀⠀⠀⢀⣀⠀⠀⠀⠀⢹⡏⢫⡆⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢼⡏⠙⢦⣄⠂⠀⠀⠐⠀⡀⠄⠂⠁⠀⡀⠐⠀⠠⠐⠀⠀⠄⠀⠀⠀⠀⠀⠁⠀⠉⠲⢄⠀⠈⣷⢾⣿⣀⣀⣀⣀⠀⠀⠀⢀⡀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣼⡇⡄⠀⠈⠓⠦⠨⣀⠀⠀⠀⡀⠄⠀⢀⠠⠐⠀⠀⡀⠂⡠⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠙⡀⢿⡄⠙⠿⣍⡉⠹⡵⡀⣴⠟⠙⣧⠶⣶
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⢿⠁⠘⡄⠀⠀⠀⠀⠀⠀⠈⠦⣀⡀⠠⠀⠀⢀⠠⠀⠀⠊⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢠⠃⢸⣧⠀⠀⠈⠙⠓⠳⠛⠛⠉⠁⠀⡴⠏
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣼⣿⢀⠀⡇⠀⠀⠀⠀⠀⠀⠀⠀⠈⠉⠛⣷⣶⠶⢶⣶⢶⡋⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠐⠁⠀⠀⣿⠆⠀⡠⠒⠰⠄⠀⠀⠀⢠⢾⠋⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠑⣽⣿⡀⠀⠀⠀⠀⠀⣀⠀⠀⠀⠀⠀⠀⡟⡇⠀⠀⠀⢿⣇⠀⠀⠀⠀⠀⠀⣀⣤⡀⠀⠀⠀⣠⢷⡏⠀⠀⠀⡠⠊⠀⠀⢀⣴⡟⠁⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣀⠈⢽⣷⡄⠀⠀⠀⠏⠀⠉⠑⢦⠀⠀⠀⢿⣧⠀⠀⠀⢿⠇⠀⠀⠀⠀⠀⢀⠀⠀⠈⡇⠀⣴⣏⣀⠀⠀⣀⣀⣤⡤⣶⠿⠛⠁⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣾⡟⠋⣉⠵⠂⢀⣀⠄⠀⠀⠀⢀⠰⠁⠀⠀⠘⣿⡄⠀⣼⡟⠀⠀⠀⠀⠀⠀⠀⠁⢢⣈⠀⠐⠤⣬⠉⣻⣿⡟⠛⠉⠁⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢰⡿⠉⢺⡗⠒⣄⣏⣤⡀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣿⡁⠀⢿⣅⣀⠀⠀⠀⠀⠀⠀⠀⠀⡠⢽⣦⠚⠚⢯⠀⠱⣵⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⣀⣤⡎⠀⢀⡞⠁⠀⢱⠀⠀⠀⢀⣀⣤⢶⡾⠛⠋⠁⠀⠀⠉⠋⠿⣶⣤⣄⡀⠀⠀⢰⠃⠀⠙⣆⣀⠈⣷⠶⠿⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠙⠉⠹⠾⣽⣿⡤⣴⢶⣿⣴⣶⡟⡯⠛⠉⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠙⠹⢿⣶⣶⣿⠶⣦⣿⡉⠛⢿⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
""");
  }

  public static void esperar(int segundos) {
    try {
      Thread.sleep(segundos * 1000L);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  public static String getSeparator() {
    return "=".repeat(SEPARATOR_LENGTH);
  }

  public static void printTitle(String title) {
    // Head
    System.out.println(getSeparator());

    System.out.println(title);

    // Bottom
    System.out.println(getSeparator());
  }

  public static void printMessage(String message) {
    System.out.println(message);
  }

  /** Errores destacados (8.1.1 del ERS). */
  public static void printError(String message) {
    System.out.println("[!] " + message);
  }

  /** Lee una línea; si la entrada se termina, el programa se despide y sale. */
  public static String readText(String label) {
    System.out.print(label + ": ");
    System.out.flush();

    try {
      String linea = ENTRADA.readLine();
      if (linea == null) {
        System.out.println();
        printMessage("Hasta Luego!");
        System.exit(0);
      }
      return linea.trim();
    } catch (IOException e) {
      return "";
    }
  }

  /** ECUD1 paso 2: la clave se muestra oculta cuando hay una terminal. */
  public static String readPassword(String label) {
    Console consola = System.console();

    if (consola == null || !esTerminal(consola))
      return readText(label);

    char[] clave = consola.readPassword(label + ": ");
    return clave == null ? "" : new String(clave).trim();
  }

  /** Desde Java 22, System.console() existe aunque la entrada venga de un archivo; isTerminal() lo distingue. */
  private static boolean esTerminal(Console consola) {
    try {
      return (Boolean) Console.class.getMethod("isTerminal").invoke(consola);
    } catch (ReflectiveOperationException e) {
      return true;
    }
  }

  /** RNF-010: resumen de la operación antes de registrarla. */
  public static boolean confirm(String question) {
    return "S".equalsIgnoreCase(readText(question + " (S/N)"));
  }

  /** RNF-011: los movimientos se muestran como una tabla de texto ordenada. */
  public static void printTable(String[] headers, List<String[]> rows, boolean[] alignRight) {
    int[] widths = new int[headers.length];

    for (int i = 0; i < headers.length; i++) {
      widths[i] = headers[i].length();
      for (String[] row : rows)
        widths[i] = Math.max(widths[i], row[i].length());
    }

    System.out.println(linea(headers, widths, alignRight));

    StringBuilder separador = new StringBuilder();
    for (int i = 0; i < widths.length; i++)
      separador.append(i == 0 ? "" : "-+-").append("-".repeat(widths[i]));
    System.out.println(separador);

    for (String[] row : rows)
      System.out.println(linea(row, widths, alignRight));
  }

  private static String linea(String[] celdas, int[] widths, boolean[] alignRight) {
    StringBuilder texto = new StringBuilder();
    for (int i = 0; i < celdas.length; i++) {
      String formato = "%" + (alignRight[i] ? "" : "-") + widths[i] + "s";
      texto.append(i == 0 ? "" : " | ").append(String.format(formato, celdas[i]));
    }
    return texto.toString().stripTrailing();
  }
}
