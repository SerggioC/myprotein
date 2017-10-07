package com.cruz.sergio.myproteinpricechecker.helper;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Sergio on 17/09/2017.
 */

public class ProzisDomain {

    public static String getProzisWebLocation(String przCountry) {
        List<String> ww_arr = Arrays.asList("AL", "DZ", "AR", "AM", "AU", "BA", "BO", "BW", "CV", "CM", "CA", "ECT",
                "CL", "CO", "CU", "DO", "EC", "EG", "MK", "FO", "GP", "GT", "GG", "GF", "HT", "HK", "IN", "ID",
                "IM", "IL", "JM", "JP", "JE", "LB", "LI", "MO", "MY", "MQ", "UM", "MC", "ME", "MA", "MZ", "MX",
                "NZ", "NI", "NG", "NO", "PA", "PY", "PE", "PH", "PR", "RE", "BL", "SA", "RS", "SG", "SO", "ZA",
                "LK", "ST", "SN", "TW", "TH", "TL", "TN", "TR", "AE", "US", "UY", "VE");
        List<String> ru_arr = Arrays.asList("AZ", "BY", "GE", "KZ", "MD", "RU", "UA", "UZ");
        List<String> eu_arr = Arrays.asList("BG", "CY", "CZ", "EE", "HR", "IS", "LV", "LT", "HU", "MT", "RO", "SI");

        if (ww_arr.contains(przCountry)) {
            return "ww";
        } else if (ru_arr.contains(przCountry)) {
            return "ru";
        } else if (eu_arr.contains(przCountry)) {
            return "eu";
        } else {
            return przCountry.toLowerCase();
        }
    }

//
//    public static final String getHref(String country, String language) {
//        String country_href = "";
//        String language_href = "";
//
//
//        switch (country) {
//            case "it": country_href = "it"; break;
//            case "pt": country_href = "pt"; break;
//
//        }
//
//
//
//        JSONObject stores_languages = new JSONObject() {"it":
//
//            {
//                "it":{
//                "description":"Italiano", "url":"/it/it/"
//            },"en":{
//                "description":"English", "url":"/it/en/"
//            }
//            },"pt":
//
//            {
//                "pt":{
//                "description":"Português", "url":"/pt/pt/"
//            },"en":{
//                "description":"English", "url":"/pt/en/"
//            }
//            },"fr":
//
//            {
//                "fr":{
//                "description":"Français", "url":"/fr/fr/"
//            },"en":{
//                "description":"English", "url":"/fr/en/"
//            }
//            },"es":
//
//            {
//                "es":{
//                "description":"Español", "url":"/es/es/"
//            },"en":{
//                "description":"English", "url":"/es/en/"
//            }
//            },"be":
//
//            {
//                "fr":{
//                "description":"Français", "url":"/be/fr/"
//            },"nl":{
//                "description":"Nederlands", "url":"/be/nl/"
//            },"en":{
//                "description":"English", "url":"/be/en/"
//            }
//            },"de":
//
//            {
//                "de":{
//                "description":"Deutsch", "url":"/de/de/"
//            },"en":{
//                "description":"English", "url":"/de/en/"
//            }
//            },"gr":
//
//            {
//                "el":{
//                "description":"Ελληνικά", "url":"/gr/el/"
//            },"en":{
//                "description":"English", "url":"/gr/en/"
//            }
//            },"uk":
//
//            {
//                "en":{
//                "description":"English", "url":"/uk/en/"
//            }
//            },"ch":
//
//            {
//                "en":{
//                "description":"English", "url":"/ch/en/"
//            },"fr":{
//                "description":"Français", "url":"/ch/fr/"
//            },"de":{
//                "description":"Deutsch", "url":"/ch/de/"
//            },"it":{
//                "description":"Italiano", "url":"/ch/it/"
//            },"pt":{
//                "description":"Português", "url":"/ch/pt/"
//            }
//            },"se":
//
//            {
//                "sv":{
//                "description":"Svenska", "url":"/se/sv/"
//            },"en":{
//                "description":"English", "url":"/se/en/"
//            }
//            },"nl":
//
//            {
//                "nl":{
//                "description":"Nederlands", "url":"/nl/nl/"
//            },"en":{
//                "description":"English", "url":"/nl/en/"
//            }
//            },"at":
//
//            {
//                "de":{
//                "description":"Deutsch", "url":"/at/de/"
//            },"en":{
//                "description":"English", "url":"/at/en/"
//            }
//            },"br":
//
//            {
//                "pt":{
//                "description":"Português", "url":"/br/pt/"
//            }
//            },"dk":
//
//            {
//                "da":{
//                "description":"Dansk", "url":"/dk/da/"
//            },"en":{
//                "description":"English", "url":"/dk/en/"
//            }
//            },"ru":
//
//            {
//                "ru":{
//                "description":"Русский", "url":"/ru/ru/"
//            },"en":{
//                "description":"English", "url":"/ru/en/"
//            }
//            },"eu":
//
//            {
//                "en":{
//                "description":"English", "url":"/eu/en/"
//            },"fr":{
//                "description":"Français", "url":"/eu/fr/"
//            },"es":{
//                "description":"Español", "url":"/eu/es/"
//            },"pt":{
//                "description":"Português", "url":"/eu/pt/"
//            },"it":{
//                "description":"Italiano", "url":"/eu/it/"
//            },"el":{
//                "description":"Ελληνικά", "url":"/eu/el/"
//            }
//            },"ww":
//
//            {
//                "en":{
//                "description":"English", "url":"/ww/en/"
//            },"fr":{
//                "description":"Français", "url":"/ww/fr/"
//            },"es":{
//                "description":"Español", "url":"/ww/es/"
//            },"pt":{
//                "description":"Português", "url":"/ww/pt/"
//            },"it":{
//                "description":"Italiano", "url":"/ww/it/"
//            }
//            },"lu":
//
//            {
//                "fr":{
//                "description":"Français", "url":"/lu/fr/"
//            },"de":{
//                "description":"Deutsch", "url":"/lu/de/"
//            },"pt":{
//                "description":"Português", "url":"/lu/pt/"
//            },"en":{
//                "description":"English", "url":"/lu/en/"
//            }
//            },"ao":
//
//            {
//                "en":{
//                "description":"English", "url":"/ao/en/"
//            },"pt":{
//                "description":"Português", "url":"/ao/pt/"
//            }
//            },"fi":
//
//            {
//                "en":{
//                "description":"English", "url":"/fi/en/"
//            },"sv":{
//                "description":"Svenska", "url":"/fi/sv/"
//            }
//            },"ie":
//
//            {
//                "en":{
//                "description":"English", "url":"/ie/en/"
//            }
//            },"pl":
//
//            {
//                "pl":{
//                "description":"Polski", "url":"/pl/pl/"
//            },"en":{
//                "description":"English", "url":"/pl/en/"
//            }
//            },"sk":
//
//            {
//                "sk":{
//                "description":"Slovak", "url":"/sk/sk/"
//            },"en":{
//                "description":"English", "url":"/sk/en/"
//            }
//            },"cn":
//
//            {
//                "zh":{
//                "description":"Mandarin", "url":"/cn/zh/"
//            },"en":{
//                "description":"English", "url":"/cn/en/"
//            }
//            }
//        };
//
//    }



//        <option value="ww" data-country-id="AL">Albania (AL)</item>
//        <option value="ww" data-country-id="DZ">Algeria (DZ)</item>
//        <option value="ao" data-country-id="AO">Angola (AO)</item>
//        <option value="ww" data-country-id="AR">Argentina (AR)</item>
//        <option value="ww" data-country-id="AM">Armenia (AM)</item>
//        <option value="ww" data-country-id="AU">Australia (AU)</item>
//        <option value="ru" data-country-id="AZ">Az.rbaycan (AZ)</item>
//        <option value="ru" data-country-id="BY">Belarus (BY)</item>
//        <option value="be" data-country-id="BE">Belgique (BE)</item>
//        <option value="ww" data-country-id="BO">Bolivia (BO)</item>
//        <option value="ww" data-country-id="BA">Bosna i Hercegovina (BA)</item>
//        <option value="ww" data-country-id="BW">Botswana (BW)</item>
//        <option value="br" data-country-id="BR">Brasil (BR)</item>
//        <option value="eu" data-country-id="BG">Bâlgariya (BG)</item>
//        <option value="ww" data-country-id="CV">Cabo Verde (CV)</item>
//        <option value="ww" data-country-id="CM">Cameroun (CM)</item>
//        <option value="ww" data-country-id="CA">Canada (CA)</item>
//        <option value="ww" data-country-id="ECT">Ceuta (ECT)</item>
//        <option value="ww" data-country-id="CL">Chile (CL)</item>
//        <option value="cn" data-country-id="CN">China (CN)</item>
//        <option value="ww" data-country-id="CO">Colombia (CO)</item>
//        <option value="ww" data-country-id="CU">Cuba (CU)</item>
//        <option value="eu" data-country-id="CY">Cyprus (CY)</item>
//        <option value="eu" data-country-id="CZ">Czech Republic (CZ)</item>
//        <option value="dk" data-country-id="DK">Danmark (DK)</item>
//        <option value="de" data-country-id="DE">Deutschland (DE)</item>
//        <option value="ww" data-country-id="DO">Dominican Republic (DO)</item>
//        <option value="ww" data-country-id="EC">Ecuador (EC)</item>
//        <option value="eu" data-country-id="EE">Eesti (EE)</item>
//        <option value="ww" data-country-id="EG">Egypt (EG)</item>
//        <option value="es" data-country-id="ES">España (ES)</item>
//        <option value="ww" data-country-id="MK">FYROM (MK)</item>
//        <option value="ww" data-country-id="FO">Faroe Islands (FO)</item>
//        <option value="fr" data-country-id="FR">France (FR)</item>
//        <option value="ru" data-country-id="GE">Georgia (GE)</item>
//        <option value="gr" data-country-id="GR">Greece (GR)</item>
//        <option value="ww" data-country-id="GP">Guadeloupe (GP)</item>
//        <option value="ww" data-country-id="GT">Guatemala (GT)</item>
//        <option value="ww" data-country-id="GG">Guernsey (GG)</item>
//        <option value="ww" data-country-id="GF">Guyane Française (GF)</item>
//        <option value="ww" data-country-id="HT">Haiti (HT)</item>
//        <option value="ww" data-country-id="HK">Hong Kong (HK)</item>
//        <option value="eu" data-country-id="HR">Hrvatska (HR)</item>
//        <option value="eu" data-country-id="IS">Iceland (IS)</item>
//        <option value="ww" data-country-id="IN">India (IN)</item>
//        <option value="ww" data-country-id="ID">Indonesia (ID)</item>
//        <option value="ie" data-country-id="IE">Ireland (IE)</item>
//        <option value="ww" data-country-id="IM">Isle of Man (IM)</item>
//        <option value="ww" data-country-id="IL">Israel (IL)</item>
//        <option value="it" data-country-id="IT">Italia (IT)</item>
//        <option value="ww" data-country-id="JM">Jamaica (JM)</item>
//        <option value="ww" data-country-id="JP">Japan (JP)</item>
//        <option value="ww" data-country-id="JE">Jersey (JE)</item>
//        <option value="ru" data-country-id="KZ">Kazakhstan (KZ)</item>
//        <option value="eu" data-country-id="LV">Latvija (LV)</item>
//        <option value="ww" data-country-id="LB">Lebanon (LB)</item>
//        <option value="ww" data-country-id="LI">Liechtenstein (LI)</item>
//        <option value="eu" data-country-id="LT">Lietuva (LT)</item>
//        <option value="lu" data-country-id="LU">Lëtzebuerg (LU)</item>
//        <option value="ww" data-country-id="MO">Macao (MO)</item>
//        <option value="eu" data-country-id="HU">Magyarország (HU)</item>
//        <option value="ww" data-country-id="MY">Malaysia (MY)</item>
//        <option value="eu" data-country-id="MT">Malta (MT)</item>
//        <option value="ww" data-country-id="MQ">Martinique (MQ)</item>
//        <option value="ww" data-country-id="MU">Mauritius (MU)</item>
//        <option value="ww" data-country-id="MC">Monaco (MC)</item>
//        <option value="ww" data-country-id="ME">Montenegro (ME)</item>
//        <option value="ww" data-country-id="MA">Morocco (MA)</item>
//        <option value="ww" data-country-id="MZ">Moçambique (MZ)</item>
//        <option value="ww" data-country-id="MX">México (MX)</item>
//        <option value="nl" data-country-id="NL">Nederland (NL)</item>
//        <option value="ww" data-country-id="NZ">New Zealand (NZ)</item>
//        <option value="ww" data-country-id="NI">Nicarágua (NI)</item>
//        <option value="ww" data-country-id="NG">Nigeria (NG)</item>
//        <option value="ww" data-country-id="NO">Norge (NO)</item>
//        <option value="ww" data-country-id="PA">Panamá (PA)</item>
//        <option value="ww" data-country-id="PY">Paraguay (PY)</item>
//        <option value="ww" data-country-id="PE">Perú (PE)</item>
//        <option value="ww" data-country-id="PH">Philippines (PH)</item>
//        <option value="pl" data-country-id="PL">Polska (PL)</item>
//        <option value="pt" data-country-id="PT">Portugal (PT)</item>
//        <option value="ww" data-country-id="PR">Puerto Rico (PR)</item>
//        <option value="ru" data-country-id="MD">Republicii Moldova (MD)</item>
//        <option value="eu" data-country-id="RO">România (RO)</item>
//        <option value="ru" data-country-id="RU">Russia (RU)</item>
//        <option value="ww" data-country-id="RE">Réunion (RE)</item>
//        <option value="ww" data-country-id="BL">Saint Barthelemy (BL)</item>
//        <option value="ww" data-country-id="SA">Saudi Arabia (SA)</item>
//        <option value="ww" data-country-id="RS">Serbia (RS)</item>
//        <option value="ww" data-country-id="SG">Singapore (SG)</item>
//        <option value="eu" data-country-id="SI">Slovenija (SI)</item>
//        <option value="sk" data-country-id="SK">Slovensko (SK)</item>
//        <option value="ww" data-country-id="SO">Somalia (SO)</item>
//        <option value="ww" data-country-id="ZA">South Africa (ZA)</item>
//        <option value="ww" data-country-id="LK">Sri Lanka (LK)</item>
//        <option value="fi" data-country-id="FI">Suomi (FI)</item>
//        <option value="se" data-country-id="SE">Sverige (SE)</item>
//        <option value="ch" data-country-id="CH">Switzerland (CH)</item>
//        <option value="ww" data-country-id="ST">São Tomé e Príncipe (ST)</item>
//        <option value="ww" data-country-id="SN">Sénégal (SN)</item>
//        <option value="ww" data-country-id="TW">Taiwan (TW)</item>
//        <option value="ww" data-country-id="TH">Thailand (TH)</item>
//        <option value="ww" data-country-id="TL">Timor Leste (TL)</item>
//        <option value="ww" data-country-id="TN">Tunisia (TN)</item>
//        <option value="ww" data-country-id="TR">Türkiye (TR)</item>
//        <option value="ww" data-country-id="AE">UAE (AE)</item>
//        <option value="ww" data-country-id="US">USA (US)</item>
//        <option value="ru" data-country-id="UA">Ukraine (UA)</item>
//        <option value="uk" data-country-id="GB">United Kingdom (GB)</item>
//        <option value="ww" data-country-id="UY">Uruguay (UY)</item>
//        <option value="ru" data-country-id="UZ">Uzbekistan (UZ)</item>
//        <option value="ww" data-country-id="VE">Venezuela (VE)</item>
//        <option value="at" data-country-id="AT">Österreich (AT)</item>

}
