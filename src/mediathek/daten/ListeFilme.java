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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import mediathek.controller.filmeLaden.FilmeLaden;
import mediathek.controller.filmeLaden.suchen.sender.Mediathek3Sat;
import mediathek.controller.filmeLaden.suchen.sender.MediathekArd;
import mediathek.controller.filmeLaden.suchen.sender.MediathekMdr;
import mediathek.controller.filmeLaden.suchen.sender.MediathekNdr;
import mediathek.controller.filmeLaden.suchen.sender.MediathekWdr;
import mediathek.controller.filmeLaden.suchen.sender.MediathekZdf;
import mediathek.tool.DatumZeit;
import mediathek.tool.GuiFunktionen;
import mediathek.tool.Log;
import mediathek.tool.TModelFilm;
import org.apache.commons.lang3.StringEscapeUtils;

public class ListeFilme extends LinkedList<DatenFilm> {

    public static final String THEMA_LIVE = "Livestream";
    //Tags Infos Filmliste, erste Zeile der .filme-Datei
    public static final String FILMLISTE = "Filmliste";
    public static final int FILMLISTE_MAX_ELEM = 6;
    public static final String FILMLISTE_DATUM = "Filmliste-Datum";
    public static final int FILMLISTE_DATUM_NR = 0;
    public static final String FILMLISTE_ZEIT = "Filmliste-Nur-Zeit";
    public static final int FILMLISTE_NUR_ZEIT_NR = 1;
    public static final String FILMLISTE_DATUM_ZEIT = "Filmliste-Nur-Datum";
    public static final int FILMLISTE_NUR_DATUM_NR = 2;
    public static final String FILMLISTE_ANZAHL = "Filmliste-Anzahl";
    public static final int FILMLISTE_ANZAHL_NR = 3;
    public static final String FILMLISTE_VERSION = "Filmliste-Version";
    public static final int FILMLISTE_VERSION_NR = 4;
    public static final String FILMLISTE_PROGRAMM = "Filmliste-Programm";
    public static final int FILMLISTE_PRGRAMM_NR = 5;
    public static final String[] FILMLISTE_COLUMN_NAMES = {FILMLISTE_DATUM, FILMLISTE_ZEIT, FILMLISTE_DATUM_ZEIT,
        FILMLISTE_ANZAHL, FILMLISTE_VERSION, FILMLISTE_PROGRAMM};
    // Infos
//    public static final String FILMLISTE_INFOS = "Filmliste-Infos";
//    public static final int FILMLISTE_INFOS_MAX_ELEM = 2;
//    public static final String FILMLISTE_INFOS_SWR_NR_THEMA = "Filmliste-Infos-Swr-NrThema";
//    public static final int FILMLISTE_INFOS_SWR_NR_THEMA_NR = 0;
//    public static final String FILMLISTE_INFOS_frei = "Filmliste-Infos-frei";
//    public static final int FILMLISTE_INFOS_frei_NR = 1;
//    public static final String[] FILMLISTE_INFOS_COLUMN_NAMES = {FILMLISTE_INFOS_SWR_NR_THEMA, FILMLISTE_INFOS_frei};
    private int nr = 0;
//    public String[] infos;
    public String[] metaDaten;
    private HashSet<String> treeGetModelOfField = new HashSet<String>();
    private LinkedList<String> listGetModelOfField = new LinkedList<String>();

    /**
     *
     * @param ddaten
     */
    public ListeFilme() {
//        infos = newInfo();
        metaDaten = newMetaDaten();
    }
    //===================================
    // public
    //===================================

    public static String[] newMetaDaten() {
        String[] ret = new String[FILMLISTE_MAX_ELEM];
        for (int i = 0; i < ret.length; ++i) {
            ret[i] = "";
        }
        return ret;
    }

//    public static String[] newInfo() {
//        String[] ret = new String[FILMLISTE_INFOS_MAX_ELEM];
//        for (int i = 0; i < ret.length; ++i) {
//            ret[i] = "";
//        }
//        return ret;
//    }
    @Override
    public synchronized void clear() {
        nr = 0;
        super.clear();
    }

    public void sort() {
        Collections.<DatenFilm>sort(this);
        // und jetzt noch die Nummerierung in Ordnung brinegen
        Iterator<DatenFilm> it = this.iterator();
        int i = 0;
        while (it.hasNext()) {
            it.next().arr[DatenFilm.FILM_NR_NR] = getNr(i++);
        }
    }

//    public synchronized void setInfo(int feld, String wert) {
//        infos[feld] = wert;
//    }
//
//    public synchronized void setInfo(String[] iinfo) {
//        for (int i = 0; i < FILMLISTE_INFOS_MAX_ELEM; ++i) {
//            infos[i] = iinfo[i].toString();
//        }
//    }
    public synchronized void setMeta(String[] mmeta) {
        for (int i = 0; i < FILMLISTE_MAX_ELEM; ++i) {
            metaDaten[i] = mmeta[i].toString();
        }
    }

    public synchronized boolean addFilmVomSender(DatenFilm film) {
        // Filme die beim Sender gesucht wurden (und nur die) hier eintragen
        // nur für die MediathekReader
        // ist eine URL,Sender,Thema,Titel schon vorhanden, wird sie verworfen, die aktuellste bleibt erhalten
        DatenFilm f;
        film.arr[DatenFilm.FILM_THEMA_NR] = StringEscapeUtils.unescapeHtml4(film.arr[DatenFilm.FILM_THEMA_NR].trim());
        film.arr[DatenFilm.FILM_TITEL_NR] = StringEscapeUtils.unescapeHtml4(film.arr[DatenFilm.FILM_TITEL_NR].trim());
        // erst mal schauen obs das schon gibt
        Iterator<DatenFilm> it = this.iterator();
        while (it.hasNext()) {
            f = it.next();
            if (f.getIndex().equals(film.getIndex())) {
                return false;
            }
        }
        return add(film);
    }

    public synchronized void updateListe(ListeFilme liste, boolean index /* Vergleich über Index, sonst nur URL */) {
        // in eine vorhandene Liste soll eine andere Filmliste einsortiert werden
        // es werden nur Filme die noch nicht vorhanden sind, einsortiert
        DatenFilm film;
        HashSet<String> hashSet = new HashSet<String>();
        Iterator<DatenFilm> it = this.iterator();
        while (it.hasNext()) {
            if (index) {
                hashSet.add(it.next().getIndex());
            } else {
                hashSet.add(it.next().arr[DatenFilm.FILM_URL_NR]);
            }
        }
        it = liste.iterator();
        while (it.hasNext()) {
            film = it.next();
            if (index) {
                if (!hashSet.contains(film.getIndex())) {
                    add(film);
                }
            } else {
                if (!hashSet.contains(film.arr[DatenFilm.FILM_URL_NR])) {
                    add(film);
                }
            }
        }
    }

    public synchronized boolean addWithNr(DatenFilm film) {
        film.arr[DatenFilm.FILM_THEMA_NR] = StringEscapeUtils.unescapeHtml4(film.arr[DatenFilm.FILM_THEMA_NR].trim());
        film.arr[DatenFilm.FILM_TITEL_NR] = StringEscapeUtils.unescapeHtml4(film.arr[DatenFilm.FILM_TITEL_NR].trim());
        film.arr[DatenFilm.FILM_NR_NR] = getNr(nr++);
        film.arr[DatenFilm.FILM_URL_NR] = film.getUrlOrg();
        return add(film);
    }

    private String getNr(int nr) {
        final int MAX_STELLEN = 5;
        final String FUELL_ZEICHEN = "0";
        String str = String.valueOf(nr++);
        while (str.length() < MAX_STELLEN) {
            str = FUELL_ZEICHEN + str;
        }
        return str;
    }

    public synchronized int countSender(String sender) {
        int ret = 0;
        ListIterator<DatenFilm> it = this.listIterator(0);
        while (it.hasNext()) {
            if (it.next().arr[DatenFilm.FILM_SENDER_NR].equalsIgnoreCase(sender)) {
                ++ret;
            }
        }
        return ret;
    }

    public synchronized void delSender(String sender) {
        // alle Filme VOM SENDER löschen
        DatenFilm film;
        ListIterator<DatenFilm> it = this.listIterator(0);
        while (it.hasNext()) {
            film = it.next();
            if (film.arr[DatenFilm.FILM_SENDER_NR].equalsIgnoreCase(sender)) {
                it.remove();
            }
        }
    }

    public void liveStreamEintragen() {
        // Live-Stream eintragen
        //DatenFilm(Daten ddaten, String ssender, String tthema, String urlThema, String ttitel, String uurl, String datum, String zeit) {
        addFilmVomSender(
                new DatenFilm(MediathekZdf.SENDER, THEMA_LIVE, ""/* urlThema */, MediathekZdf.SENDER + " " + THEMA_LIVE, "http://wstreaming.zdf.de/encoder/livestream2_vh.asx", ""/* datum */, ""/* zeit */));
        addFilmVomSender(
                new DatenFilm(Mediathek3Sat.SENDER, THEMA_LIVE, ""/* urlThema */, Mediathek3Sat.SENDER + " " + THEMA_LIVE, "http://wstreaming.zdf.de/encoder/3sat_vh.asx", ""/* datum */, ""/* zeit */));
        addFilmVomSender(
                new DatenFilm(MediathekArd.SENDER, THEMA_LIVE, ""/* urlThema */, MediathekArd.SENDER + " Tagesschau" + " " + THEMA_LIVE, "mmsh://tagesschau-live1-webm-wmv.wm.llnwd.net/tagesschau_live1_webm_wmv?MSWMExt=.asf", ""/* datum */, ""/* zeit */));
        addFilmVomSender(
                new DatenFilm(MediathekNdr.SENDER, THEMA_LIVE, ""/* urlThema */, MediathekNdr.SENDER + " " + THEMA_LIVE, "http://www.ndr.de/resources/metadaten/ndr_fs_nds_hi_wmv.asx", ""/* datum */, ""/* zeit */));
        addFilmVomSender(
                new DatenFilm(MediathekWdr.SENDER, THEMA_LIVE, ""/* urlThema */, MediathekWdr.SENDER + " " + THEMA_LIVE, "http://www.wdr.de/wdrlive/media/wdr-fernsehen_web-l.asx", ""/* datum */, ""/* zeit */));
        addFilmVomSender(
                new DatenFilm(MediathekMdr.SENDER, THEMA_LIVE, ""/* urlThema */, MediathekMdr.SENDER + " SACHSEN" + " " + THEMA_LIVE, "http://avw.mdr.de/livestreams/mdr_tv_sachsen.asx", ""/* datum */, ""/* zeit */));
        addFilmVomSender(
                new DatenFilm(MediathekMdr.SENDER, THEMA_LIVE, ""/* urlThema */, MediathekMdr.SENDER + " SACHSEN-ANHALT" + " " + THEMA_LIVE, "http://avw.mdr.de/livestreams/mdr_tv_sachsen-anhalt.asx", ""/* datum */, ""/* zeit */));
        addFilmVomSender(
                new DatenFilm(MediathekMdr.SENDER, THEMA_LIVE, ""/* urlThema */, MediathekMdr.SENDER + " THÜRINGEN" + " " + THEMA_LIVE, "http://avw.mdr.de/livestreams/mdr_tv_thueringen.asx", ""/* datum */, ""/* zeit */));
        addFilmVomSender(
                new DatenFilm(MediathekArd.SENDER, THEMA_LIVE, ""/* urlThema */, MediathekArd.SENDER + " Phoenix" + " " + THEMA_LIVE, "http://hstreaming.zdf.de/encoder/phoenix_vh.mov", ""/* datum */, ""/* zeit */));
    }

    public synchronized void getModelTabFilme(DDaten ddaten, TModelFilm modelFilm, String filterSender, String filterThema, String filterTitel, String filterThemaTitel) {
        modelFilm.setRowCount(0);
        if (this.size() != 0) {
            if (filterSender.equals("") && filterThema.equals("") && filterTitel.equals("") && filterThemaTitel.equals("")) {
                addObjectDataTabFilme(ddaten, modelFilm);
            } else {
                ListeFilme liste = new ListeFilme();
                DatenFilm film;
                Iterator<DatenFilm> it = this.iterator();
                while (it.hasNext()) {
                    film = it.next();
                    // aboPruefen(String senderSuchen, String themaSuchen, boolean themaExakt, String textSuchen,
                    //                     String imSender, String imThema, String imText) {
                    if (ListeAbo.filterAufAboPruefen(filterSender, filterThema, filterTitel, filterThemaTitel,
                            film.arr[DatenFilm.FILM_SENDER_NR], film.arr[DatenFilm.FILM_THEMA_NR], film.arr[DatenFilm.FILM_TITEL_NR])) {
                        liste.add(film);
                    }
                }
                liste.addObjectDataTabFilme(ddaten, modelFilm);
            }
        }
    }

    public synchronized String[] getModelOfField(int feld, String filterString, int filterFeld) {
        // erstellt ein StringArray mit den Daten des Feldes und filtert nach filterFeld
        // ist für die Filterfelder im GuiFilme
        // doppelte Einträge (bei der Groß- und Klienschribung) werden entfernt
        String str;
        String s;
        listGetModelOfField.add("");
        DatenFilm film;
        Iterator<DatenFilm> it = this.iterator();
        if (filterString.equals("")) {
            //alle Werte dieses Feldes
            while (it.hasNext()) {
                str = it.next().arr[feld];
                //hinzufügen
                s = str.toLowerCase();
                if (!treeGetModelOfField.contains(s)) {
                    treeGetModelOfField.add(s);
                    listGetModelOfField.add(str);
                }
            }
        } else {
            //Werte dieses Feldes, filtern nach filterString im FilterFeld
            while (it.hasNext()) {
                film = it.next();
                str = film.arr[feld];
                if (film.arr[filterFeld].equalsIgnoreCase(filterString)) {
                    //hinzufügen
                    s = str.toLowerCase();
                    if (!treeGetModelOfField.contains(s)) {
                        treeGetModelOfField.add(s);
                        listGetModelOfField.add(str);
                    }
                }
            }
        }
        treeGetModelOfField.clear();
        GuiFunktionen.listeSort(listGetModelOfField);
        String[] ret = new String[0];
        ret = listGetModelOfField.toArray(ret);
        listGetModelOfField.clear();
        return ret;
    }

    public synchronized DatenFilm getFilmByUrl(String url) {
        DatenFilm ret = null;
        ListIterator<DatenFilm> it = this.listIterator(0);
        while (it.hasNext()) {
            DatenFilm f = it.next();
            if (f.arr[DatenFilm.FILM_URL_NR].equals(url)) {
                ret = f;
                break;
            }
        }
        return ret;
    }

//    public synchronized DatenFilm getFilmByTitel(String sender, String thema, String titel) {
//        ListIterator<DatenFilm> it = this.listIterator(0);
//        while (it.hasNext()) {
//            DatenFilm f = it.next();
//            if (f.arr[DatenFilm.FILM_TITEL_NR].equalsIgnoreCase(titel)) {
//                if (f.arr[DatenFilm.FILM_THEMA_NR].equalsIgnoreCase(thema)) {
//                    if (f.arr[DatenFilm.FILM_SENDER_NR].equals(sender)) {
//                        return f;
//                    }
//                }
//            }
//        }
//        return null;
//    }

    //===================================
    // private
    //===================================
    private void addObjectDataTabFilme(DDaten ddaten, TModelFilm model) {
        Object[] object;
        DatenFilm film;
        DatenAbo datenAbo;
        if (this.size() > 0) {
            ListIterator<DatenFilm> iterator = this.listIterator(0);
            while (iterator.hasNext()) {
                film = iterator.next();
                datenAbo = ddaten.listeAbo.getAbo(film.arr[DatenFilm.FILM_SENDER_NR],
                        film.arr[DatenFilm.FILM_THEMA_NR],
                        film.arr[DatenFilm.FILM_TITEL_NR]);
                if (datenAbo != null) {
                    film.arr[DatenFilm.FILM_ABO_NAME_NR] = datenAbo.arr[DatenAbo.ABO_NAME_NR];
                } else {
                    film.arr[DatenFilm.FILM_ABO_NAME_NR] = "";
                }
                object = new Object[DatenFilm.FILME_MAX_ELEM];
                for (int m = 0; m < DatenFilm.FILME_MAX_ELEM; ++m) {
                    if (m == DatenFilm.FILM_DATUM_NR) {
                        object[m] = DatumZeit.getDatumForObject(film);
                    } else {
                        object[m] = film.arr[m];
                    }
                }
                model.addRow(object);
            }
        }
    }

    public int alterFilmlisteSek() {
        // Alter der Filmliste in Sekunden
        int ret = 0;
        Date jetzt = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy, HH:mm");
        String date = metaDaten[ListeFilme.FILMLISTE_DATUM_NR];
        Date filmDate = null;
        try {
            filmDate = sdf.parse(date);
        } catch (ParseException ex) {
        }
        if (jetzt != null && filmDate != null) {
            ret = Math.round((jetzt.getTime() - filmDate.getTime()) / (1000));
            ret = Math.abs(ret);
        }
        return ret;
    }

    public boolean filmlisteIstAelter() {
        // Filmliste ist älter als: FilmeLaden.ALTER_FILMLISTE_SEKUNDEN_FUER_AUTOUPDATE
        int ret = -1;
        Date jetzt = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy, HH:mm");
        String date = metaDaten[ListeFilme.FILMLISTE_DATUM_NR];
        Date filmDate = null;
        try {
            filmDate = sdf.parse(date);
        } catch (ParseException ex) {
        }
        if (jetzt != null && filmDate != null) {
            ret = Math.round((jetzt.getTime() - filmDate.getTime()) / (1000));
            ret = Math.abs(ret);
        }
        if (ret != -1) {
            Log.systemMeldung("Die Filmliste ist " + ret / 60 + " Minuten alt");
        }
        if (ret > FilmeLaden.ALTER_FILMLISTE_SEKUNDEN_FUER_AUTOUPDATE) {
            return true;
        } else if (ret == -1) {
            return true;
        }
        return false;
    }
}
