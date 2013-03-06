/*
 * MediathekView
 * Copyright (C) 2008 W. Xaver
 * W.Xaver[at]googlemail.com
 * http://zdfmediathk.sourceforge.net/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package mediathek.daten;

import java.io.File;
import mediathek.controller.filmeLaden.FilmeLaden;
import mediathek.controller.io.IoXmlFilmlisteLesen;
import mediathek.controller.io.IoXmlFilmlisteSchreiben;
import mediathek.tool.GuiKonstanten;
import mediathek.tool.Konstanten;
import mediathek.tool.Log;

public class Daten {
    // Konstanten, Systemeinstellungen und alles was wichtig ist für
    // alle Versionen: MediathekGui, MediathekAuto, MediathekNoGui

    //alle Programmeinstellungen
    public static String[] system = new String[Konstanten.SYSTEM_MAX_ELEM];
    // flags
    public static boolean debug = false; // Debugmodus
    public static boolean nogui = false; // Version ohne Gui
    public static boolean auto = false; // Version: MediathekAuto
    // Verzeichnis zum Speichern der Programmeinstellungen
    private static String basisverzeichnis = "";
    // zentrale Klassen
    public static FilmeLaden filmeLaden;
    public static IoXmlFilmlisteLesen ioXmlFilmlisteLesen = null;
    public static ListeFilme listeFilme = null;
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    public Daten(String pfad) {
        basisverzeichnis = pfad;
        init();
    }

    private void init() {
        for (int i = 0; i < system.length; ++i) {
            system[i] = "";
        }
        //initialisieren
        system[Konstanten.SYSTEM_MAX_DOWNLOAD_NR] = "1";
        system[Konstanten.SYSTEM_WARTEN_NR] = "1";
        system[Konstanten.SYSTEM_USER_AGENT_NR] = Konstanten.USER_AGENT_DEFAULT;
        system[Konstanten.SYSTEM_WARTEN_NR] = "1";
        system[Konstanten.SYSTEM_LOOK_NR] = "0";
        system[Konstanten.SYSTEM_VERSION_NR] = Konstanten.VERSION;
        system[Konstanten.SYSTEM_UPDATE_SUCHEN_NR] = Boolean.TRUE.toString();
        system[Konstanten.SYSTEM_ABOS_SOFORT_SUCHEN_NR] = Boolean.TRUE.toString();
        system[Konstanten.SYSTEM_UNICODE_AENDERN_NR] = Boolean.TRUE.toString();
        if (Daten.debug) {
            Daten.system[Konstanten.SYSTEM_IMPORT_ART_FILME_NR] = String.valueOf(GuiKonstanten.UPDATE_FILME_AUS);
        }
        listeFilme = new ListeFilme();
        ioXmlFilmlisteLesen = new IoXmlFilmlisteLesen();
        filmeLaden = new FilmeLaden();
    }

    public static void setUserAgentAuto() {
        // Useragent wird vom Programm verwaltet
        system[Konstanten.SYSTEM_USER_AGENT_AUTO_NR] = Boolean.TRUE.toString();
    }

    public static void setUserAgentManuel(String ua) {
        // Useragent den der Benutzer vorgegeben hat
        system[Konstanten.SYSTEM_USER_AGENT_AUTO_NR] = Boolean.FALSE.toString();
        system[Konstanten.SYSTEM_USER_AGENT_NR] = ua;
    }

    public static boolean isUserAgentAuto() {
        if (system[Konstanten.SYSTEM_USER_AGENT_AUTO_NR].equals("")) {
            system[Konstanten.SYSTEM_USER_AGENT_AUTO_NR] = Boolean.TRUE.toString();
            return true;
        } else {
            return Boolean.parseBoolean(system[Konstanten.SYSTEM_USER_AGENT_AUTO_NR]);
        }
    }

    public static String getUserAgent() {
        if (isUserAgentAuto()) {
            return Konstanten.USER_AGENT_DEFAULT;
        } else {
            return system[Konstanten.SYSTEM_USER_AGENT_NR];
        }
    }

    /**
     * Liefert das Verzeichnis der Programmeinstellungen
     *
     * @return Den Verzeichnispfad als String.
     */
    public static String getBasisVerzeichnis() {
        // liefert das Verzeichnis der Programmeinstellungen
        return getBasisVerzeichnis(false);
    }

    /**
     * Liefert das Verzeichnis der Programmeinstellungen
     *
     * @param anlegen Anlegen, oder nicht.
     * @return Den Verzeichnispfad als String.
     */
    public static String getBasisVerzeichnis(boolean anlegen) {
        return getBasisVerzeichnis(basisverzeichnis, anlegen);
    }

    /**
     * Liefert das Verzeichnis der Programme
     *
     * @param basis Der Ordner, indem das Ve
     * @param anlegen Anlegen, oder nicht.
     * @return Den Verzeichnispfad als Strin
     */
    private static String getBasisVerzeichnis(String basis, boolean anlegen) {
        String ret;
        if (basis.equals("")) {
            ret = System.getProperty("user.home") + File.separator + Konstanten.VERZEICHNISS_EINSTELLUNGEN + File.separator;
        } else {
            ret = basis;
        }
        if (anlegen) {
            File basisF = new File(ret);
            if (!basisF.exists()) {
                if (!basisF.mkdir()) {
                    Log.fehlerMeldung(898736548, Log.FEHLER_ART_PROG, "Daten.getBasisVerzeichnis", new String[]{"Kann den Ordner zum Speichern der Daten nicht anlegen!",
                        "Daten.getBasisVerzeichnis"});
                }

            }
        }
        return ret;
    }

    public void allesLaden() {
        ioXmlFilmlisteLesen.filmlisteLesen(getBasisVerzeichnis() + Konstanten.XML_DATEI_FILME, false /* istUrl */, listeFilme);
    }

    public void allesSpeichern() {
        new IoXmlFilmlisteSchreiben().filmeSchreiben(getBasisVerzeichnis(true) + Konstanten.XML_DATEI_FILME, listeFilme);
    }
}
