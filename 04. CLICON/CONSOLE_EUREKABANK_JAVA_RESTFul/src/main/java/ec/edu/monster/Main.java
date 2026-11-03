package ec.edu.monster;

import ec.edu.monster.modelos.http.ConfigHTTP;
import ec.edu.monster.vista.LoginView;
import ec.edu.monster.vista.MenuView;
import ec.edu.monster.vista.utils.MessagesUtils;

/**
 * Cliente de consola de EurekaBank. Uso: java -jar &lt;jar&gt; [url del servidor].
 */
//revisado y aprovado por Marcos Escobar
public class Main {
  public static void main(String[] args) {
    MessagesUtils.prepararConsola();

    if (args.length > 0)
      ConfigHTTP.setBaseUrl(args[0]);

    // Splash Art
    MessagesUtils.printMonster();
    MessagesUtils.esperar(3);

    // Login y menú del perfil; al cerrar sesión (o si expira) se vuelve al inicio de sesión.
    while (LoginView.userHasLogin()) {
      MenuView.show();
    }

    MessagesUtils.printMessage("Hasta Luego!");
    MessagesUtils.esperar(3);
  }
}
