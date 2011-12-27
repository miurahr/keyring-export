/*
 * Astro Info - a an astronomical calculator/almanac for PalmOS devices.
 * 
 * $Id: CreateOverlay.java 613 2006-08-21 18:24:39Z hoenicke $
 * Copyright (C) 2002, Astro Info SourceForge Project
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package net.sf.astroinfo.pdb;
import java.io.*;
import java.util.*;


public class CreateOverlay {
    public final static int INIT_BUF_SIZE = 1024;
    public final static int OM_OVERLAY_VERSION = 4;

    /*	'ovly' (in base) describes base itself  */
    public final static int OM_SPEC_ATTR_FOR_BASE = 1;
    public final static int OM_SPEC_ATTR_STRIPPED = 2;
    public final static int OM_SPEC_ATTR_SORTED   = 4;

    public final static int OM_OVERLAY_KIND_HIDE    = 0;
    public final static int OM_OVERLAY_KIND_ADD     = 1;
    public final static int OM_OVERLAY_KIND_REPLACE = 2;
    public final static int OM_OVERLAY_KIND_BASE    = 3;

    public final static int[] excludedRes = {
	Database.StringToID("CODE"),
	Database.StringToID("DATA"),
	Database.StringToID("code"),
	Database.StringToID("data"),
	Database.StringToID("boot"),
	Database.StringToID("extn"),
	Database.StringToID("pref")
    };

    static class Overlay implements Cloneable {
	int kind;
	int type;
	int id;
	int length;
	int checksum;
	boolean needed;

	public Object clone() {
	    try {
		return super.clone();
	    } catch (CloneNotSupportedException ex) {
		throw new InternalError();
	    }
	}
    }

    public static class RawPRCRecord extends PRCRecord implements Cloneable {
	byte[] buffer;
	public void read(PalmDataInputStream in) throws IOException {
	    int offset = 0;
	    byte[] buf = new byte[INIT_BUF_SIZE];
	    int len;
	    while ((len = in.read(buf, offset, buf.length - offset)) > 0) {
		offset += len;
		if (offset == buf.length) {
		    byte[] newbuf = new byte[buf.length*2];
		    System.arraycopy(buf, 0, newbuf, 0, offset);
		    buf = newbuf;
		}
	    }
	    buffer = new byte[offset];
	    System.arraycopy(buf, 0, buffer, 0, offset);
	}

	public void write(PalmDataOutputStream out) throws IOException {
	    out.write(buffer);
	}

	public boolean equals(Object o) {
	    if (o instanceof RawPRCRecord) {
		RawPRCRecord r = (RawPRCRecord) o;
		if (r.getType() != getType() || r.getID() != getID())
		    return false;
		if (r.buffer.length != buffer.length)
		    return false;
		for (int i = 0; i < buffer.length; i++)
		    if (buffer[i] != r.buffer[i])
			return false;
		return true;
	    } else
		return false;
	}

	public Object clone() {
	    try {
		return super.clone();
	    } catch (CloneNotSupportedException ex) {
		throw new InternalError();
	    }
	}
    }


    String base;
    String baselang;
    Database baseprc;
    int basecrc;
    HashMap baseEntries = new HashMap();

    HashMap overlayEntries = new HashMap();

    public CreateOverlay(String base, String baselang, Database baseprc) {
	this.base = base;
	this.baselang = baselang;
	this.baseprc = baseprc;
	this.basecrc = 0;
	Record[] entries = baseprc.getEntries();
	for (int i = 0; i < entries.length; i++) {
	    String key = "" + ((PRCRecord) entries[i]).getType()
		+ "!"+ ((PRCRecord) entries[i]).getID();
	    baseEntries.put(key, entries[i]);
	    basecrc = calcCrc16(((RawPRCRecord) entries[i]).buffer, basecrc);
	}
    }

    public static int[] crctt_16 =
    {
	0x0000, 0x1021, 0x2042, 0x3063, 0x4084, 0x50A5, 0x60C6, 0x70E7,
	0x8108, 0x9129, 0xA14A, 0xB16B, 0xC18C, 0xD1AD, 0xE1CE, 0xF1EF,
	0x1231, 0x0210, 0x3273, 0x2252, 0x52B5, 0x4294, 0x72F7, 0x62D6,
	0x9339, 0x8318, 0xB37B, 0xA35A, 0xD3BD, 0xC39C, 0xF3FF, 0xE3DE,
	0x2462, 0x3443, 0x0420, 0x1401, 0x64E6, 0x74C7, 0x44A4, 0x5485,
	0xA56A, 0xB54B, 0x8528, 0x9509, 0xE5EE, 0xF5CF, 0xC5AC, 0xD58D,
	0x3653, 0x2672, 0x1611, 0x0630, 0x76D7, 0x66F6, 0x5695, 0x46B4,
	0xB75B, 0xA77A, 0x9719, 0x8738, 0xF7DF, 0xE7FE, 0xD79D, 0xC7BC,
	0x48C4, 0x58E5, 0x6886, 0x78A7, 0x0840, 0x1861, 0x2802, 0x3823,
	0xC9CC, 0xD9ED, 0xE98E, 0xF9AF, 0x8948, 0x9969, 0xA90A, 0xB92B,
	0x5AF5, 0x4AD4, 0x7AB7, 0x6A96, 0x1A71, 0x0A50, 0x3A33, 0x2A12,
	0xDBFD, 0xCBDC, 0xFBBF, 0xEB9E, 0x9B79, 0x8B58, 0xBB3B, 0xAB1A,
	0x6CA6, 0x7C87, 0x4CE4, 0x5CC5, 0x2C22, 0x3C03, 0x0C60, 0x1C41,
	0xEDAE, 0xFD8F, 0xCDEC, 0xDDCD, 0xAD2A, 0xBD0B, 0x8D68, 0x9D49,
	0x7E97, 0x6EB6, 0x5ED5, 0x4EF4, 0x3E13, 0x2E32, 0x1E51, 0x0E70,
	0xFF9F, 0xEFBE, 0xDFDD, 0xCFFC, 0xBF1B, 0xAF3A, 0x9F59, 0x8F78,
	0x9188, 0x81A9, 0xB1CA, 0xA1EB, 0xD10C, 0xC12D, 0xF14E, 0xE16F,
	0x1080, 0x00A1, 0x30C2, 0x20E3, 0x5004, 0x4025, 0x7046, 0x6067,
	0x83B9, 0x9398, 0xA3FB, 0xB3DA, 0xC33D, 0xD31C, 0xE37F, 0xF35E,
	0x02B1, 0x1290, 0x22F3, 0x32D2, 0x4235, 0x5214, 0x6277, 0x7256,
	0xB5EA, 0xA5CB, 0x95A8, 0x8589, 0xF56E, 0xE54F, 0xD52C, 0xC50D,
	0x34E2, 0x24C3, 0x14A0, 0x0481, 0x7466, 0x6447, 0x5424, 0x4405,
	0xA7DB, 0xB7FA, 0x8799, 0x97B8, 0xE75F, 0xF77E, 0xC71D, 0xD73C,
	0x26D3, 0x36F2, 0x0691, 0x16B0, 0x6657, 0x7676, 0x4615, 0x5634,
	0xD94C, 0xC96D, 0xF90E, 0xE92F, 0x99C8, 0x89E9, 0xB98A, 0xA9AB,
	0x5844, 0x4865, 0x7806, 0x6827, 0x18C0, 0x08E1, 0x3882, 0x28A3,
	0xCB7D, 0xDB5C, 0xEB3F, 0xFB1E, 0x8BF9, 0x9BD8, 0xABBB, 0xBB9A,
	0x4A75, 0x5A54, 0x6A37, 0x7A16, 0x0AF1, 0x1AD0, 0x2AB3, 0x3A92,
	0xFD2E, 0xED0F, 0xDD6C, 0xCD4D, 0xBDAA, 0xAD8B, 0x9DE8, 0x8DC9,
	0x7C26, 0x6C07, 0x5C64, 0x4C45, 0x3CA2, 0x2C83, 0x1CE0, 0x0CC1,
	0xEF1F, 0xFF3E, 0xCF5D, 0xDF7C, 0xAF9B, 0xBFBA, 0x8FD9, 0x9FF8,
	0x6E17, 0x7E36, 0x4E55, 0x5E74, 0x2E93, 0x3EB2, 0x0ED1, 0x1EF0
    };  


    private int calcCrc16(byte[] buffer, int crc) {

	for (int i = 0; i< buffer.length; i++)
	    crc = (crc << 8) ^ crctt_16[ ((crc >> 8) ^ buffer[i]) & 0xff];
	return( crc & 0xffff );

    }

    private static HashMap langId = new HashMap();
    private static void langId_add(String id, int code) {
	langId.put(id, new Integer(code));
    }
    private static HashMap countryId = new HashMap();
    private static void countryId_add(String id, int code) {
	countryId.put(id, new Integer(code));
    }
    static {
	langId_add("en",   0);	// lEnglish
	langId_add("fr",   1);	// lFrench
	langId_add("de",   2);	// lGerman
	langId_add("it",   3);	// lItalian
	langId_add("es",   4);	// lSpanish
	//ngId_add("en",   5);	// lUnused LANGUAGE_WORKPAD (IBM WorkPad - English)

	langId_add("jp",   6);	// lJapanese (According to ISO 639, this should be JA)
	langId_add("nl",   7);	// lDutch
	langId_add("aa",   8);	// lAfar
	langId_add("ab",   9);	// lAbkhazian
	langId_add("af",  10);	// lAfrikaans
	langId_add("am",  11);	// lAmharic
	langId_add("ar",  12);	// lArabic
	langId_add("as",  13);	// lAssamese
	langId_add("ay",  14);	// lAymara
	langId_add("az",  15);	// lAzerbaijani
	langId_add("ba",  16);	// lBashkir
	langId_add("be",  17);	// lByelorussian
	langId_add("bg",  18);	// lBulgarian
	langId_add("bh",  19);	// lBihari
	langId_add("bi",  20);	// lBislama
	langId_add("bn",  21);	// lBengali
	langId_add("bo",  22);	// lTibetan
	langId_add("br",  23);	// lBreton
	langId_add("ca",  24);	// lCatalan
	langId_add("co",  25);	// lCorsican
	langId_add("cs",  26);	// lCzech
	langId_add("cy",  27);	// lWelsh
	langId_add("da",  28);	// lDanish
	langId_add("dz",  29);	// lBhutani
	langId_add("el",  30);	// lGreek
	langId_add("eo",  31);	// lEsperanto
	langId_add("et",  32);	// lEstonian
	langId_add("eu",  33);	// lBasque
	langId_add("fa",  34);	// lFarsi
	langId_add("fi",  35);	// lFinnish
	langId_add("fj",  36);	// lFiji
	langId_add("fo",  37);	// lFaroese
	langId_add("fy",  38);	// lFrisian
	langId_add("ga",  39);	// lIrish
	langId_add("gd",  40);	// lScotsGaelic
	langId_add("gl",  41);	// lGalician
	langId_add("gn",  42);	// lGuarani
	langId_add("gu",  43);	// lGujarati
	langId_add("ha",  44);	// lHausa
	langId_add("hi",  45);	// lHindi
	langId_add("hr",  46);	// lCroatian
	langId_add("hu",  47);	// lHungarian
	langId_add("hy",  48);	// lArmenian
	langId_add("ia",  49);	// lInterlingua
	langId_add("ie",  50);	// lInterlingue
	langId_add("ik",  51);	// lInupiak
	langId_add("in",  52);	// lIndonesian
	langId_add("is",  53);	// lIcelandic
	langId_add("iw",  54);	// lHebrew
	langId_add("ji",  55);	// lYiddish
	langId_add("jw",  56);	// lJavanese
	langId_add("ka",  57);	// lGeorgian
	langId_add("kk",  58);	// lKazakh
	langId_add("kl",  59);	// lGreenlandic
	langId_add("km",  60);	// lCambodian
	langId_add("kn",  61);	// lKannada
	langId_add("ko",  62);	// lKorean
	langId_add("ks",  63);	// lKashmiri
	langId_add("ku",  64);	// lKurdish
	langId_add("ky",  65);	// lKirghiz
	langId_add("la",  66);	// lLatin
	langId_add("ln",  67);	// lLingala
	langId_add("lo",  68);	// lLaothian
	langId_add("lt",  69);	// lLithuanian
	langId_add("lv",  70);	// lLatvian
	langId_add("mg",  71);	// lMalagasy
	langId_add("mi",  72);	// lMaori
	langId_add("mk",  73);	// lMacedonian
	langId_add("ml",  74);	// lMalayalam
	langId_add("mn",  75);	// lMongolian
	langId_add("mo",  76);	// lMoldavian
	langId_add("mr",  77);	// lMarathi
	langId_add("ms",  78);	// lMalay
	langId_add("mt",  79);	// lMaltese
	langId_add("my",  80);	// lBurmese
	langId_add("na",  81);	// lNauru
	langId_add("ne",  82);	// lNepali
	langId_add("no",  83);	// lNorwegian
	langId_add("oc",  84);	// lOccitan
	langId_add("om",  85);	// lAfan
	langId_add("or",  86);	// lOriya
	langId_add("pa",  87);	// lPunjabi
	langId_add("pl",  88);	// lPolish
	langId_add("ps",  89);	// lPashto
	langId_add("pt",  90);	// lPortuguese
	langId_add("qu",  91);	// lQuechua
	langId_add("rm",  92);	// lRhaetoRomance
	langId_add("rn",  93);	// lKirundi
	langId_add("ro",  94);	// lRomanian
	langId_add("ru",  95);	// lRussian
	langId_add("rw",  96);	// lKinyarwanda
	langId_add("sa",  97);	// lSanskrit
	langId_add("sd",  98);	// lSindhi
	langId_add("sg",  99);	// lSangro
	langId_add("sh", 100);	// lSerboCroatian
	langId_add("si", 101);	// lSinghalese
	langId_add("sk", 102);	// lSlovak
	langId_add("sl", 103);	// lSlovenian
	langId_add("sm", 104);	// lSamoan
	langId_add("sn", 105);	// lShona
	langId_add("so", 106);	// lSomali
	langId_add("sq", 107);	// lAlbanian
	langId_add("sr", 108);	// lSerbian
	langId_add("ss", 109);	// lSiswati
	langId_add("st", 110);	// lSesotho
	langId_add("su", 111);	// lSudanese
	langId_add("sv", 112);	// lSwedish
	langId_add("sw", 113);	// lSwahili
	langId_add("ta", 114);	// lTamil
	langId_add("te", 115);	// lTegulu
	langId_add("tg", 116);	// lTajik
	langId_add("th", 117);	// lThai
	langId_add("ti", 118);	// lTigrinya
	langId_add("tk", 119);	// lTurkmen
	langId_add("tl", 120);	// lTagalog
	langId_add("tn", 121);	// lSetswana
	langId_add("to", 122);	// lTonga
	langId_add("tr", 123);	// lTurkish
	langId_add("ts", 124);	// lTsonga
	langId_add("tt", 125);	// lTatar
	langId_add("tw", 126);	// lTwi
	langId_add("uk", 127);	// lUkrainian
	langId_add("ur", 128);	// lUrdu
	langId_add("uz", 129);	// lUzbek
	langId_add("vi", 130);	// lVietnamese
	langId_add("vo", 131);	// lVolapuk
	langId_add("wo", 132);	// lWolof
	langId_add("xh", 133);	// lXhosa
	langId_add("yo", 134);	// lYoruba
	langId_add("zh", 135);	// lChinese
	langId_add("zu", 136);	// lZulu

	countryId_add("AT",   1); // cAustria
	countryId_add("BE",   2); // cBelgium
	countryId_add("BR",   3); // cBrazil
	countryId_add("CA",   4); // cCanada
	countryId_add("DK",   5); // cDenmark
	countryId_add("FI",   6); // cFinland
	countryId_add("FR",   7); // cFrance
	countryId_add("DE",   8); // cGermany
	countryId_add("HK",   9); // cHongKong
	countryId_add("IS",  10); // cIceland
	countryId_add("IE",  11); // cIreland
	countryId_add("IT",  12); // cItaly
	countryId_add("JP",  13); // cJapan
	countryId_add("LU",  14); // cLuxembourg
	countryId_add("MX",  15); // cMexico
	countryId_add("NL",  16); // cNetherlands
	countryId_add("NZ",  17); // cNewZealand
	countryId_add("NO",  18); // cNorway
	countryId_add("ES",  19); // cSpain
	countryId_add("SE",  20); // cSweden
	countryId_add("CH",  21); // cSwitzerland
	countryId_add("GB",  22); // cUnitedKingdom (UK)
	countryId_add("US",  23); // cUnitedStates
	countryId_add("IN",  24); // cIndia
	countryId_add("ID",  25); // cIndonesia
	countryId_add("KR",  26); // cRepublicOfKorea
	countryId_add("MY",  27); // cMalaysia
	countryId_add("CN",  28); // cChina
	countryId_add("PH",  29); // cPhilippines
	countryId_add("SG",  30); // cSingapore
	countryId_add("TH",  31); // cThailand
	countryId_add("TW",  32); // cTaiwan
	countryId_add("AD",  33); // cAndorra
	countryId_add("AE",  34); // cUnitedArabEmirates
	countryId_add("AF",  35); // cAfghanistan
	countryId_add("AG",  36); // cAntiguaAndBarbuda
	countryId_add("AI",  37); // cAnguilla
	countryId_add("AL",  38); // cAlbania
	countryId_add("AM",  39); // cArmenia
	countryId_add("AN",  40); // cNetherlandsAntilles
	countryId_add("AO",  41); // cAngola
	countryId_add("AQ",  42); // cAntarctica
	countryId_add("AR",  43); // cArgentina
	countryId_add("AS",  44); // cAmericanSamoa
	countryId_add("AW",  45); // cAruba
	countryId_add("AZ",  46); // cAzerbaijan
	countryId_add("BA",  47); // cBosniaAndHerzegovina
	countryId_add("BB",  48); // cBarbados
	countryId_add("BD",  49); // cBangladesh
	countryId_add("BF",  50); // cBurkinaFaso
	countryId_add("BG",  51); // cBulgaria
	countryId_add("BH",  52); // cBahrain
	countryId_add("BI",  53); // cBurundi
	countryId_add("BJ",  54); // cBenin
	countryId_add("BM",  55); // cBermuda
	countryId_add("BN",  56); // cBruneiDarussalam
	countryId_add("BO",  57); // cBolivia
	countryId_add("BS",  58); // cBahamas
	countryId_add("BT",  59); // cBhutan
	countryId_add("BV",  60); // cBouvetIsland
	countryId_add("BW",  61); // cBotswana
	countryId_add("BY",  62); // cBelarus
	countryId_add("BZ",  63); // cBelize
	countryId_add("CC",  64); // cCocosIslands
	countryId_add("CD",  65); // cDemocraticRepublicOfTheCongo
	countryId_add("CF",  66); // cCentralAfricanRepublic
	countryId_add("CG",  67); // cCongo
	countryId_add("CI",  68); // cIvoryCoast
	countryId_add("CK",  69); // cCookIslands
	countryId_add("CL",  70); // cChile
	countryId_add("CM",  71); // cCameroon
	countryId_add("CO",  72); // cColumbia
	countryId_add("CR",  73); // cCostaRica
	countryId_add("CU",  74); // cCuba
	countryId_add("CV",  75); // cCapeVerde
	countryId_add("CX",  76); // cChristmasIsland
	countryId_add("CY",  77); // cCyprus
	countryId_add("CZ",  78); // cCzechRepublic
	countryId_add("DJ",  79); // cDjibouti
	countryId_add("DM",  80); // cDominica
	countryId_add("DO",  81); // cDominicanRepublic
	countryId_add("DZ",  82); // cAlgeria
	countryId_add("EC",  83); // cEcuador
	countryId_add("EE",  84); // cEstonia
	countryId_add("EG",  85); // cEgypt
	countryId_add("EH",  86); // cWesternSahara
	countryId_add("ER",  87); // cEritrea
	countryId_add("ET",  88); // cEthiopia
	countryId_add("FJ",  89); // cFiji
	countryId_add("FK",  90); // cFalklandIslands
	countryId_add("FM",  91); // cMicronesia
	countryId_add("FO",  92); // cFaeroeIslands
	countryId_add("FX",  93); // cMetropolitanFrance
	countryId_add("GA",  94); // cGabon
	countryId_add("GD",  95); // cGrenada
	countryId_add("GE",  96); // cGeorgia
	countryId_add("GF",  97); // cFrenchGuiana
	countryId_add("GH",  98); // cGhana
	countryId_add("GI",  99); // cGibraltar
	countryId_add("GL", 100); // cGreenland
	countryId_add("GM", 101); // cGambia
	countryId_add("GN", 102); // cGuinea
	countryId_add("GP", 103); // cGuadeloupe
	countryId_add("GQ", 104); // cEquatorialGuinea
	countryId_add("GR", 105); // cGreece
	countryId_add("GS", 106); // cSouthGeorgiaAndTheSouthSandwichIslands
	countryId_add("GT", 107); // cGuatemala
	countryId_add("GU", 108); // cGuam
	countryId_add("GW", 109); // cGuineaBisseu
	countryId_add("GY", 110); // cGuyana
	countryId_add("HM", 111); // cHeardAndMcDonaldIslands
	countryId_add("HN", 112); // cHonduras
	countryId_add("HR", 113); // cCroatia
	countryId_add("HT", 114); // cHaiti
	countryId_add("HU", 115); // cHungary
	countryId_add("IL", 116); // cIsrael
	countryId_add("IO", 117); // cBritishIndianOceanTerritory
	countryId_add("IQ", 118); // cIraq
	countryId_add("IR", 119); // cIran
	countryId_add("JM", 120); // cJamaica
	countryId_add("JO", 121); // cJordan
	countryId_add("KE", 122); // cKenya
	countryId_add("KG", 123); // cKyrgyzstan (Kirgistan)
	countryId_add("KH", 124); // cCambodia
	countryId_add("KI", 125); // cKiribati
	countryId_add("KM", 126); // cComoros
	countryId_add("KN", 127); // cStKittsAndNevis
	countryId_add("KP", 128); // cDemocraticPeoplesRepublicOfKorea
	countryId_add("KW", 129); // cKuwait
	countryId_add("KY", 130); // cCaymanIslands
	countryId_add("KK", 131); // cKazakhstan
	countryId_add("LA", 132); // cLaos
	countryId_add("LB", 133); // cLebanon
	countryId_add("LC", 134); // cStLucia
	countryId_add("LI", 135); // cLiechtenstein
	countryId_add("LK", 136); // cSriLanka
	countryId_add("LR", 137); // cLiberia
	countryId_add("LS", 138); // cLesotho
	countryId_add("LT", 139); // cLithuania
	countryId_add("LV", 140); // cLatvia
	countryId_add("LY", 141); // cLibya
	countryId_add("MA", 142); // cMorrocco
	countryId_add("MC", 143); // cMonaco
	countryId_add("MD", 144); // cMoldova
	countryId_add("MG", 145); // cMadagascar
	countryId_add("MH", 146); // cMarshallIslands
	countryId_add("MK", 147); // cMacedonia
	countryId_add("ML", 148); // cMali
	countryId_add("MM", 149); // cMyanmar
	countryId_add("MN", 150); // cMongolia
	countryId_add("MO", 151); // cMacau
	countryId_add("MP", 152); // cNorthernMarianaIslands
	countryId_add("MQ", 153); // cMartinique
	countryId_add("MR", 154); // cMauritania
	countryId_add("MS", 155); // cMontserrat
	countryId_add("MT", 156); // cMalta
	countryId_add("MU", 157); // cMauritius
	countryId_add("MV", 158); // cMaldives
	countryId_add("MW", 159); // cMalawi
	countryId_add("MZ", 160); // cMozambique
	countryId_add("NA", 161); // cNamibia
	countryId_add("NC", 162); // cNewCalidonia
	countryId_add("NE", 163); // cNiger
	countryId_add("NF", 164); // cNorfolkIsland
	countryId_add("NG", 165); // cNigeria
	countryId_add("NI", 166); // cNicaragua
	countryId_add("NP", 167); // cNepal
	countryId_add("NR", 168); // cNauru
	countryId_add("NU", 169); // cNiue
	countryId_add("OM", 170); // cOman
	countryId_add("PA", 171); // cPanama
	countryId_add("PE", 172); // cPeru
	countryId_add("PF", 173); // cFrenchPolynesia
	countryId_add("PG", 174); // cPapuaNewGuinea
	countryId_add("PK", 175); // cPakistan
	countryId_add("PL", 176); // cPoland
	countryId_add("PM", 177); // cStPierreAndMiquelon
	countryId_add("PN", 178); // cPitcairn
	countryId_add("PR", 179); // cPuertoRico
	countryId_add("PT", 180); // cPortugal
	countryId_add("PW", 181); // cPalau
	countryId_add("PY", 182); // cParaguay
	countryId_add("QA", 183); // cQatar
	countryId_add("RE", 184); // cReunion
	countryId_add("RO", 185); // cRomania
	countryId_add("RU", 186); // cRussianFederation
	countryId_add("RW", 187); // cRwanda
	countryId_add("SA", 188); // cSaudiArabia
	countryId_add("SB", 189); // cSolomonIslands
	countryId_add("SC", 190); // cSeychelles
	countryId_add("SD", 191); // cSudan
	countryId_add("SH", 192); // cStHelena
	countryId_add("SI", 193); // cSlovenia
	countryId_add("SJ", 194); // cSvalbardAndJanMayenIslands
	countryId_add("SK", 195); // cSlovakia
	countryId_add("SL", 196); // cSierraLeone
	countryId_add("SM", 197); // cSanMarino
	countryId_add("SN", 198); // cSenegal
	countryId_add("SO", 199); // cSomalia
	countryId_add("SR", 200); // cSuriname
	countryId_add("ST", 201); // cSaoTomeAndPrincipe
	countryId_add("SV", 202); // cElSalvador
	countryId_add("SY", 203); // cSyranArabRepublic
	countryId_add("SZ", 204); // cSwaziland
	countryId_add("TC", 205); // cTurksAndCaicosIslands
	countryId_add("TD", 206); // cChad
	countryId_add("TF", 207); // cFrenchSouthernTerritories
	countryId_add("TG", 208); // cTogo
	countryId_add("TJ", 209); // cTajikistan
	countryId_add("TK", 210); // cTokelau
	countryId_add("TM", 211); // cTurkmenistan
	countryId_add("TN", 212); // cTunisia
	countryId_add("TO", 213); // cTonga
	countryId_add("TP", 214); // cEastTimor
	countryId_add("TR", 215); // cTurkey
	countryId_add("TT", 216); // cTrinidadAndTobago
	countryId_add("TV", 217); // cTuvalu
	countryId_add("TZ", 218); // cTanzania
	countryId_add("UA", 219); // cUkraine
	countryId_add("UG", 220); // cUganda
	countryId_add("UM", 221); // cUnitedStatesMinorOutlyingIslands
	countryId_add("UY", 222); // cUruguay
	countryId_add("UZ", 223); // cUzbekistan
	countryId_add("VA", 224); // cHolySee
	countryId_add("VC", 225); // cStVincentAndTheGrenadines
	countryId_add("VE", 226); // cVenezuela
	countryId_add("VG", 227); // cBritishVirginIslands
	countryId_add("VI", 228); // cUSVirginIslands
	countryId_add("VN", 229); // cVietNam
	countryId_add("VU", 230); // cVanuatu
	countryId_add("WF", 231); // cWallisAndFutunaIslands
	countryId_add("WS", 232); // cSamoa
	countryId_add("YE", 233); // cYemen
	countryId_add("YT", 234); // cMayotte
	countryId_add("YU", 235); // cYugoslavia
	countryId_add("ZA", 236); // cSouthAfrica
	countryId_add("ZM", 237); // cZambia
	countryId_add("ZW", 238); // cZimbabwe
    }

    private int strToLocale(String locale) {
	try {
	    int lang = ((Integer) langId.get(locale.substring(0,2)))
		.intValue();
	    int country = ((Integer) countryId.get(locale.substring(2)))
		.intValue();
	    return (lang << 16) | (country);
	} catch (NullPointerException ex) {
	    throw new IllegalArgumentException("Wrong locale");
	}
    }

    public Overlay createOverlayRscType(RawPRCRecord base, int kind) {
	Overlay ovl = new Overlay();
	ovl.kind = kind;
	ovl.type = base.getType();
	ovl.id = base.getID();
	ovl.length = base.buffer.length;
	ovl.checksum = calcCrc16(base.buffer, 0);
	return ovl;
    }

    public PRCRecord createOvlyRecord(final Map ovlEntries, 
				      final int flags, 
				      final int targetLocale) {
	PRCRecord ovlrec = new PRCRecord() {
		public void read(PalmDataInputStream in) throws IOException {
		    throw new IOException("Can't read overlay record");
		}
		
		public void write(PalmDataOutputStream out) 
		    throws IOException 
		{
		    out.writeShort(OM_OVERLAY_VERSION);
		    out.writeInt(flags);
		    out.writeInt(-basecrc);
		    out.writeInt(targetLocale);
		    out.writeInt(baseprc.getType());
		    out.writeInt(baseprc.getCreator());
		    out.writeDate(baseprc.getCreationDate());
		    out.writeDate(baseprc.getModificationDate());
		    out.writeShort(ovlEntries.size());
		    Iterator i = ovlEntries.values().iterator();
		    while (i.hasNext()) {
			Overlay ovl = (Overlay) i.next();
			out.writeShort(ovl.kind);
			out.writeInt(ovl.type);
			out.writeShort(ovl.id);
			out.writeInt(ovl.length);
			out.writeInt(ovl.checksum);
		    }
		}
	    };
	ovlrec.setType(Database.StringToID("ovly"));
	ovlrec.setID(1000);
	return ovlrec;
    }

    public void overlay(String lang, Database langprc) {
	Record[] entries = langprc.getEntries();
	HashMap myOvlEntries = new HashMap();
	ArrayList newEntries = new ArrayList();
	for (int i = 0; i < entries.length; i++) {
	    String key = "" + ((PRCRecord) entries[i]).getType()
		+ "!"+ ((PRCRecord) entries[i]).getID();

	    if (!overlayEntries.containsKey(key)
		&& baseEntries.containsKey(key)) {
		RawPRCRecord base = (RawPRCRecord) baseEntries.get(key);
		Overlay baseOvl = createOverlayRscType
		    (base, OM_OVERLAY_KIND_BASE);
		overlayEntries.put(key, baseOvl); 
	    }

	    if (entries[i].equals(baseEntries.get(key))) {
		Overlay baseOvl = (Overlay)overlayEntries.get(key);
		baseOvl.needed = true;
		continue;
	    }

	    /* This entry must be overlayed */

	    Overlay ovl;
	    if (overlayEntries.containsKey(key)) {
		ovl = (Overlay) ((Overlay)overlayEntries.get(key)).clone();
		ovl.kind = OM_OVERLAY_KIND_REPLACE;
	    } else {
		ovl = createOverlayRscType((RawPRCRecord) entries[i], 
					   OM_OVERLAY_KIND_ADD);
	    }
		
	    myOvlEntries.put(key, ovl);
	    newEntries.add(((RawPRCRecord)entries[i]).clone());
	}
	System.err.println("Creating overlay for "+lang+"...");
	newEntries.add(0, createOvlyRecord(myOvlEntries, 0, 
					   strToLocale(lang)));
	langprc.setName(langprc.getName()+"_"+lang);
	langprc.setType(Database.StringToID("ovly"));
	langprc.setEntries((Record[]) newEntries.toArray(new Record[0]));
	try {
	    langprc.write(new FileOutputStream(base+"_"+lang+".prc"));
	} catch (IOException ex) {
	    System.err.println("Error writing "+base+"_"+lang+".prc");
	    ex.printStackTrace();
	}
    }

    public void writeBase() {
	ArrayList strippedEntries = new ArrayList();
	ArrayList otherEntries = new ArrayList();
	Record[] entries = baseprc.getEntries();
	strippedEntries.add(null);
	otherEntries.add(null);
	HashMap strippedOvlEntries = new HashMap();

	for (int i = 0; i < entries.length; i++) {
	    String key = "" + ((PRCRecord) entries[i]).getType()
		+ "!"+ ((PRCRecord) entries[i]).getID();
	    
	    Overlay baseOvl = (Overlay)overlayEntries.get(key);
	    if (baseOvl != null && !baseOvl.needed) {

		strippedEntries.add(entries[i]);

		Overlay ovl = (Overlay) baseOvl.clone();
		ovl.kind = OM_OVERLAY_KIND_REPLACE;
		strippedOvlEntries.put(key, ovl);

	    } else
		otherEntries.add(entries[i]);
	}


	Record[] newEntries = (Record[]) otherEntries.toArray(new Record[0]);
	newEntries[0] = createOvlyRecord 
	    (overlayEntries,
	     OM_SPEC_ATTR_FOR_BASE | OM_SPEC_ATTR_STRIPPED,
	     strToLocale(baselang));
	baseprc.setEntries(newEntries);
	try {
	    baseprc.write(new FileOutputStream(base+".prc"));
	} catch (IOException ex) {
	    System.err.println("Error writing "+base+".prc");
	    ex.printStackTrace();
	}

	newEntries = (Record[]) strippedEntries.toArray(new Record[0]);
	newEntries[0] = createOvlyRecord(strippedOvlEntries, 0,
					 strToLocale(baselang));
	baseprc.setName(baseprc.getName()+"_"+baselang);
	baseprc.setType(Database.StringToID("ovly"));
	baseprc.setEntries(newEntries);
	try {
	    baseprc.write(new FileOutputStream(base+"_"+baselang+".prc"));
	} catch (IOException ex) {
	    System.err.println("Error writing "+base+"_"+baselang+".prc");
	    ex.printStackTrace();
	}
    }

    public static void main(String[] params) {
	if (params.length < 3 || (params.length & 1) != 1 ) {
	    System.err.println("Usage: CreateOverlay <base> <def-lang> <def-prc-file> [ <lang> <lang-prcfile> ... ]");
	    return;
	}

	String base = params[0];
	Database defprc = new Database();
	try {
	    defprc.read(new FileInputStream(params[2]),
			null, null, RawPRCRecord.class);
	} catch (Exception ex) {
	    System.err.println("Error reading "+params[2]);
	    ex.printStackTrace();
	    return;
	}
	CreateOverlay ovl = new CreateOverlay(base, params[1], defprc);

	for (int i = 3; i < params.length; i+= 2) {
	    Database langprc = new Database();
	    try {
		langprc.read(new FileInputStream(params[i+1]),
			     null, null, RawPRCRecord.class);
	    } catch (IllegalAccessException ex) {
	    } catch (InstantiationException ex) {
	    } catch (IOException ex) {
		System.err.println("Error reading "+params[1]);
		ex.printStackTrace();
		continue;
	    }
	    ovl.overlay(params[i], langprc);
	}
	ovl.writeBase();
    }
}
