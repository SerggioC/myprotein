package com.cruz.sergio.myproteinpricechecker.helper;

/*****
 * Project MyProteinPriceChecker
 * Package com.cruz.sergio.myproteinpricechecker
 * Created by Sergio on 24/01/2017 14:42
 ******/

public class MyProteinDomain {
    public static String getHref(String country) {

        String href;
        switch (country) {
            case "az-az":href = "http://www.myprotein.az/";break;
            case "bg-bg":href = "http://www.myprotein.bg/";break;
            case "bs-ba":href = "http://www.myprotein.ba/";break;
            case "cs-cz":href = "http://www.myprotein.cz/";break;
            case "da-dk":href = "http://www.myprotein.dk/";break;
            case "de-at":href = "http://www.myprotein.at/";break;
            case "de-ch":href = "http://www.myprotein.ch/";break;
            case "de-de":href = "http://de.myprotein.com/";break;
            case "el-cy":href = "http://www.myprotein.com.cy/";break;
            case "el-gr":href = "http://www.myprotein.gr/";break;
            case "en-au":href = "http://au.myprotein.com/";break;
            case "en-ca":href = "http://ca.myprotein.com/";break;
            case "en-gb":href = "http://www.myprotein.com/";break;
            case "en-hk":href = "http://www.myprotein.com.hk/";break;
            case "en-ie":href = "http://www.myprotein.ie/";break;
            case "en-in":href = "http://www.myprotein.co.in/";break;
            case "en-nz":href = "http://nz.myprotein.com/";break;
            case "en-sg":href = "http://www.myprotein.com.sg/";break;
            case "en-us":href = "http://us.myprotein.com/";break;
            case "en-za":href = "http://www.myprotein.co.za/";break;
            case "es-es":href = "http://www.myprotein.es/";break;
            case "et-ee":href = "http://www.myprotein.ee/";break;
            case "fi-fi":href = "http://www.myprotein.fi/";break;
            case "fr-ca":href = "http://fr-ca.myprotein.com/";break;
            case "fr-fr":href = "http://fr.myprotein.com/";break;
            case "hr-hr":href = "http://www.myprotein.hr/";break;
            case "it-it":href = "http://www.myprotein.it/";break;
            case "ja-jp":href = "http://www.myprotein.jp/";break;
            case "ko-kr":href = "http://www.myprotein.co.kr/";break;
            case "lt-lt":href = "http://www.myprotein.lt/";break;
            case "lv-lv":href = "http://www.myprotein.lv/";break;
            case "nb-no":href = "http://www.myprotein.no/";break;
            case "nl-be":href = "http://www.myprotein.be/";break;
            case "nl-nl":href = "http://nl.myprotein.com/";break;
            case "pl-pl":href = "http://www.myprotein.pl/";break;
            case "pt-pt":href = "http://pt.myprotein.com/";break;
            case "ro-ro":href = "http://www.myprotein.ro/";break;
            case "ru-ru":href = "http://www.myprotein.ru/";break;
            case "sk-sk":href = "http://www.myprotein.sk/";break;
            case "sl-si":href = "http://si.myprotein.com/";break;
            case "sr-rs":href = "http://www.myprotein.rs/";break;
            case "sv-se":href = "http://www.myprotein.se/";break;
            case "uk-ua":href = "http://www.myprotein.com.ua/";break;
            case "zh-cn":href = "http://www.myprotein.cn/";break;
            default:href = "http://www.myprotein.com/";break;
        }
        return href;
    }
}
/*
*
	Dominios usados pela Myprotein

		<link rel="alternate" hreflang="de-at" href="http://www.myprotein.at/elysium.search">
 	    <link rel="alternate" hreflang="en-au" href="http://au.myprotein.com/elysium.search">
		<link rel="alternate" hreflang="az-az" href="http://www.myprotein.az/elysium.search">
		<link rel="alternate" hreflang="bs-ba" href="http://www.myprotein.ba/elysium.search">
		<link rel="alternate" hreflang="nl-be" href="http://www.myprotein.be/elysium.search">
		<link rel="alternate" hreflang="bg-bg" href="http://www.myprotein.bg/elysium.search">
		<link rel="alternate" hreflang="en-ca" href="http://ca.myprotein.com/elysium.search">
		<link rel="alternate" hreflang="de-ch" href="http://www.myprotein.ch/elysium.search">
		<link rel="alternate" hreflang="zh-cn" href="http://www.myprotein.cn/elysium.search">
		<link rel="alternate" hreflang="cs-cz" href="http://www.myprotein.cz/elysium.search">
		<link rel="alternate" hreflang="el-cy" href="http://www.myprotein.com.cy/elysium.search">
		<link rel="alternate" hreflang="da-dk" href="http://www.myprotein.dk/elysium.search">
		<link rel="alternate" hreflang="de-de" href="http://de.myprotein.com/elysium.search">
		<link rel="alternate" hreflang="et-ee" href="http://www.myprotein.ee/elysium.search">
		<link rel="alternate" hreflang="el-gr" href="http://www.myprotein.gr/elysium.search">
		<link rel="alternate" hreflang="en-gb" href="http://www.myprotein.com/elysium.search">
		<link rel="alternate" hreflang="es-es" href="http://www.myprotein.es/elysium.search">
		<link rel="alternate" hreflang="fi-fi" href="http://www.myprotein.fi/elysium.search">
		<link rel="alternate" hreflang="fr-fr" href="http://fr.myprotein.com/elysium.search">
		<link rel="alternate" hreflang="en-hk" href="http://www.myprotein.com.hk/elysium.search">
		<link rel="alternate" hreflang="hr-hr" href="http://www.myprotein.hr/elysium.search">
		<link rel="alternate" hreflang="en-ie" href="http://www.myprotein.ie/elysium.search">
		<link rel="alternate" hreflang="en-in" href="http://www.myprotein.co.in/elysium.search">
		<link rel="alternate" hreflang="it-it" href="http://www.myprotein.it/elysium.search">
		<link rel="alternate" hreflang="ja-jp" href="http://www.myprotein.jp/elysium.search">
		<link rel="alternate" hreflang="ko-kr" href="http://www.myprotein.co.kr/elysium.search">
		<link rel="alternate" hreflang="lt-lt" href="http://www.myprotein.lt/elysium.search">
		<link rel="alternate" hreflang="lv-lv" href="http://www.myprotein.lv/elysium.search">
		<link rel="alternate" hreflang="nb-no" href="http://www.myprotein.no/elysium.search">
		<link rel="alternate" hreflang="nl-nl" href="http://nl.myprotein.com/elysium.search">
		<link rel="alternate" hreflang="en-nz" href="http://nz.myprotein.com/elysium.search">
		<link rel="alternate" hreflang="pl-pl" href="http://www.myprotein.pl/elysium.search">
		<link rel="alternate" hreflang="pt-pt" href="http://pt.myprotein.com/elysium.search">
		<link rel="alternate" hreflang="ro-ro" href="http://www.myprotein.ro/elysium.search">
		<link rel="alternate" hreflang="sr-rs" href="http://www.myprotein.rs/elysium.search">
		<link rel="alternate" hreflang="ru-ru" href="http://www.myprotein.ru/elysium.search">
		<link rel="alternate" hreflang="en-sg" href="http://www.myprotein.com.sg/elysium.search">
		<link rel="alternate" hreflang="sk-sk" href="http://www.myprotein.sk/elysium.search">
		<link rel="alternate" hreflang="sl-si" href="http://si.myprotein.com/elysium.search">
		<link rel="alternate" hreflang="sv-se" href="http://www.myprotein.se/elysium.search">
		<link rel="alternate" hreflang="uk-ua" href="http://www.myprotein.com.ua/elysium.search">
		<link rel="alternate" hreflang="en-us" href="http://us.myprotein.com/elysium.search">
		<link rel="alternate" hreflang="fr-ca" href="http://fr-ca.myprotein.com/elysium.search">
		<link rel="alternate" hreflang="en-za" href="http://www.myprotein.co.za/elysium.search">

*
*
* */