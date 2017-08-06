package com.cruz.sergio.myproteinpricechecker.helper;

/*****
 * Project MyProteinPriceChecker
 * Package com.cruz.sergio.myproteinpricechecker
 * Created by Sergio on 24/01/2017 14:42
 *
 *
 * .az/
 .bg/voucher-codes.list
 .ba/vaucer-kodovi.list
 .cz/voucher-codes.list
 .dk/voucher-codes.list
 .at/voucher-codes.list
 *
 *
 ******/

public class MyProteinDomain {
    public static final String getHref(String country) {

        String href;
        switch (country) {
            case "en-ae":href = "http://www.myprotein.ae/";break;       //voucher-codes.list
            case "az-az":href = "http://www.myprotein.az/";break;       //vaucer-kodlari.list <---
            case "bg-bg":href = "http://www.myprotein.bg/";break;       //voucher-codes.list
            case "bs-ba":href = "http://www.myprotein.ba/";break;       //vaucer-kodovi.list <---
            case "cs-cz":href = "http://www.myprotein.cz/";break;       //voucher-codes.list
            case "da-dk":href = "http://www.myprotein.dk/";break;       //voucher-codes.list
            case "de-at":href = "http://www.myprotein.at/";break;       //voucher-codes.list
            case "de-ch":href = "http://www.myprotein.ch/";break;       //voucher-codes.list
            case "de-de":href = "http://de.myprotein.com/";break;       //voucher-codes.list
            case "el-cy":href = "http://www.myprotein.com.cy/";break;   //voucher-codes.list
            case "el-gr":href = "http://www.myprotein.gr/";break;       //voucher-codes.list
            case "en-au":href = "http://au.myprotein.com/";break;       //voucher-codes.list
            case "en-ca":href = "http://ca.myprotein.com/";break;       //voucher-codes.list
            case "en-gb":href = "http://www.myprotein.com/";break;      //voucher-codes.list
            case "en-hk":href = "http://www.myprotein.com.hk/";break;   //voucher-codes.list (vazio...)
            case "en-ie":href = "http://www.myprotein.ie/";break;       //voucher-codes.list
            case "en-in":href = "http://www.myprotein.co.in/";break;    //voucher-codes.list (zero...)
            case "en-nz":href = "http://nz.myprotein.com/";break;       //voucher-codes.list (zero...)
            case "en-sg":href = "http://www.myprotein.com.sg/";break;   //voucher-codes.list
            case "en-us":href = "http://us.myprotein.com/";break;       //voucher-codes.list
            case "en-za":href = "http://www.myprotein.co.za/";break;    //voucher-codes.list (zero...)
            case "es-es":href = "http://www.myprotein.es/";break;       //voucher-codes.list
            case "et-ee":href = "http://www.myprotein.ee/";break;       //voucher-codes.list
            case "fi-fi":href = "http://www.myprotein.fi/";break;       //voucher-codes.list
            case "fr-ca":href = "http://fr-ca.myprotein.com/";break;    //codes-de-reduction.list <---
            case "fr-fr":href = "http://fr.myprotein.com/";break;       //voucher-codes.list
            case "hr-hr":href = "http://www.myprotein.hr/";break;       //voucher-codes.list
            case "it-it":href = "http://www.myprotein.it/";break;       //voucher-codes.list
            case "ja-jp":href = "http://www.myprotein.jp/";break;       //voucher-codes.list
            case "ko-kr":href = "http://www.myprotein.co.kr/";break;    //voucher-codes.list
            case "lt-lt":href = "http://www.myprotein.lt/";break;       //voucher-codes.list
            case "lv-lv":href = "http://www.myprotein.lv/";break;       //voucher-codes.list
            case "nb-no":href = "http://www.myprotein.no/";break;       //voucher-codes.list
            case "nl-be":href = "http://www.myprotein.be/";break;       //voucher-codes.list
            case "nl-nl":href = "http://nl.myprotein.com/";break;       //voucher-codes.list
            case "pl-pl":href = "http://www.myprotein.pl/";break;       //voucher-codes.list
            case "pt-pt":href = "http://pt.myprotein.com/";break;       //voucher-codes.list
            case "ro-ro":href = "http://www.myprotein.ro/";break;       //voucher-codes.list
            case "ru-ru":href = "http://www.myprotein.ru/";break;       //voucher-codes.list
            case "sk-sk":href = "http://www.myprotein.sk/";break;       //voucher-codes.list
            case "sl-si":href = "http://si.myprotein.com/";break;       //voucher-codes.list
            case "sr-rs":href = "http://www.myprotein.rs/";break;       //vaucer-kodovi.list <---
            case "sv-se":href = "http://www.myprotein.se/";break;       //voucher-codes.list
            case "uk-ua":href = "http://www.myprotein.com.ua/";break;   //voucher-codes.list
            case "zh-cn":href = "http://www.myprotein.cn/";break;       //voucher-codes.list
            case "zh-tw":href = "http://www.myprotein.tw/";break;       //voucher-codes.list
            case "hu-hu":href = "http://www.myprotein.hu/";break;       //voucher-codes.list
            default:href = "http://www.myprotein.com/";break;
        }
        return href;
    }

    public static final String[] MP_DESKTOP_SITES = new String[]{
            "www.myprotein.ae",
            "www.myprotein.at",
            "au.myprotein.com",
            "www.myprotein.az",
            "www.myprotein.ba",
            "www.myprotein.be",
            "www.myprotein.bg",
            "ca.myprotein.com",
            "www.myprotein.ch",
            "www.myprotein.cn",
            "www.myprotein.cz",
            "www.myprotein.com.cy",
            "www.myprotein.dk",
            "de.myprotein.com",
            "www.myprotein.ee",
            "www.myprotein.gr",
            "www.myprotein.com",
            "www.myprotein.es",
            "www.myprotein.fi",
            "fr.myprotein.com",
            "www.myprotein.com.hk",
            "www.myprotein.hr",
            "www.myprotein.hu",
            "www.myprotein.ie",
            "www.myprotein.co.in",
            "www.myprotein.it",
            "www.myprotein.jp",
            "www.myprotein.co.kr",
            "www.myprotein.lt",
            "www.myprotein.lv",
            "www.myprotein.no",
            "nl.myprotein.com",
            "nz.myprotein.com",
            "www.myprotein.pl",
            "pt.myprotein.com",
            "www.myprotein.ro",
            "www.myprotein.rs",
            "www.myprotein.ru",
            "www.myprotein.com.sg",
            "www.myprotein.sk",
            "si.myprotein.com",
            "www.myprotein.se",
            "www.myprotein.tw",
            "www.myprotein.com.ua",
            "us.myprotein.com",
            "fr-ca.myprotein.com",
            "www.myprotein.co.za"
    };
    public static final String[] MP_MOBILE_SITES = new String[]{
            "m.myprotein.ae",
            "m.myprotein.at",
            "m.au.myprotein.com",
            "m.myprotein.az",
            "m.myprotein.ba",
            "m.myprotein.be",
            "m.myprotein.bg",
            "m.ca.myprotein.com",
            "m.myprotein.ch",
            "m.myprotein.cn",
            "m.myprotein.cz",
            "m.myprotein.com.cy",
            "m.myprotein.dk",
            "m.de.myprotein.com",
            "m.myprotein.ee",
            "m.myprotein.gr",
            "m.myprotein.com",
            "m.myprotein.es",
            "m.myprotein.fi",
            "m.fr.myprotein.com",
            "m.myprotein.com.hk",
            "m.myprotein.hr",
            "m.myprotein.hu",
            "m.myprotein.ie",
            "m.myprotein.co.in",
            "m.myprotein.it",
            "m.myprotein.jp",
            "m.myprotein.co.kr",
            "m.myprotein.lt",
            "m.myprotein.lv",
            "m.myprotein.no",
            "m.nl.myprotein.com",
            "m.nz.myprotein.com",
            "m.myprotein.pl",
            "m.pt.myprotein.com",
            "m.myprotein.ro",
            "m.myprotein.rs",
            "m.myprotein.ru",
            "m.myprotein.com.sg",
            "m.myprotein.sk",
            "m.si.myprotein.com",
            "m.myprotein.se",
            "m.myprotein.tw",
            "m.myprotein.com.ua",
            "m.us.myprotein.com",
            "m.fr-ca.myprotein.com",
            "m.myprotein.co.za"
    };

    public static String getCountryFromUrl(String theSite) {
        String country;
        switch (theSite) {
            case "www.myprotein.ae": country = "United Arab Emirates"; break;
            case "m.myprotein.ae": country = "United Arab Emirates"; break;
            case "www.myprotein.at": country = "Austria"; break;
            case "m.myprotein.at": country = "Austria"; break;
            case "au.myprotein.com": country = "Australia"; break;
            case "m.au.myprotein.com": country = "Australia"; break;
            case "www.myprotein.az": country = "Azerbaijan"; break;
            case "m.myprotein.az": country = "Azerbaijan"; break;
            case "www.myprotein.ba": country = "Bosnia and Herzegovina"; break;
            case "m.myprotein.ba": country = "Bosnia and Herzegovina"; break;
            case "www.myprotein.be": country = "Belgium"; break;
            case "m.myprotein.be": country = "Belgium"; break;
            case "www.myprotein.bg": country = "Bulgaria"; break;
            case "m.myprotein.bg": country = "Bulgaria"; break;
            case "ca.myprotein.com": country = "Canada"; break;
            case "m.ca.myprotein.com": country = "Canada"; break;
            case "www.myprotein.ch": country = "Switzerland"; break;
            case "m.myprotein.ch": country = "Switzerland"; break;
            case "www.myprotein.cn": country = "China"; break;
            case "m.myprotein.cn": country = "China"; break;
            case "www.myprotein.cz": country = "Czech Republic"; break;
            case "m.myprotein.cz": country = "Czech Republic"; break;
            case "www.myprotein.com.cy": country = "Cyprus"; break;
            case "m.myprotein.com.cy": country = "Cyprus"; break;
            case "www.myprotein.dk": country = "Denmark"; break;
            case "m.myprotein.dk": country = "Denmark"; break;
            case "de.myprotein.com": country = "Germany"; break;
            case "m.de.myprotein.com": country = "Germany"; break;
            case "www.myprotein.ee": country = "Estonia"; break;
            case "m.myprotein.ee": country = "Estonia"; break;
            case "www.myprotein.gr": country = "Greece"; break;
            case "m.myprotein.gr": country = "Greece"; break;
            case "www.myprotein.com": country = "United Kingdom"; break;
            case "m.myprotein.com": country = "United Kingdom"; break;
            case "www.myprotein.es": country = "Spain"; break;
            case "m.myprotein.es": country = "Spain"; break;
            case "www.myprotein.fi": country = "Finland"; break;
            case "m.myprotein.fi": country = "Finland"; break;
            case "fr.myprotein.com": country = "France"; break;
            case "m.fr.myprotein.com": country = "France"; break;
            case "www.myprotein.com.hk": country = "Hong Kong"; break;
            case "m.myprotein.com.hk": country = "Hong Kong"; break;
            case "www.myprotein.hr": country = "Croatia"; break;
            case "m.myprotein.hr": country = "Croatia"; break;
            case "www.myprotein.hu": country = "Hungary"; break;
            case "m.myprotein.hu": country = "Hungary"; break;
            case "www.myprotein.ie": country = "Ireland"; break;
            case "m.myprotein.ie": country = "Ireland"; break;
            case "www.myprotein.co.in": country = "India"; break;
            case "m.myprotein.co.in": country = "India"; break;
            case "www.myprotein.it": country = "Italy"; break;
            case "m.myprotein.it": country = "Italy"; break;
            case "www.myprotein.jp": country = "Japan"; break;
            case "m.myprotein.jp": country = "Japan"; break;
            case "www.myprotein.co.kr": country = "Korea, Republic of"; break;
            case "m.myprotein.co.kr": country = "Korea, Republic of"; break;
            case "www.myprotein.lt": country = "Lithuania"; break;
            case "m.myprotein.lt": country = "Lithuania"; break;
            case "www.myprotein.lv": country = "Latvia"; break;
            case "m.myprotein.lv": country = "Latvia"; break;
            case "www.myprotein.no": country = "Norway"; break;
            case "m.myprotein.no": country = "Norway"; break;
            case "nl.myprotein.com": country = "Netherlands"; break;
            case "m.nl.myprotein.com": country = "Netherlands"; break;
            case "nz.myprotein.com": country = "New Zealand"; break;
            case "m.nz.myprotein.com": country = "New Zealand"; break;
            case "www.myprotein.pl": country = "Poland"; break;
            case "m.myprotein.pl": country = "Poland"; break;
            case "pt.myprotein.com": country = "Portugal"; break;
            case "m.pt.myprotein.com": country = "Portugal"; break;
            case "www.myprotein.ro": country = "Romania"; break;
            case "m.myprotein.ro": country = "Romania"; break;
            case "www.myprotein.rs": country = "Serbia"; break;
            case "m.myprotein.rs": country = "Serbia"; break;
            case "www.myprotein.ru": country = "Russian Federation"; break;
            case "m.myprotein.ru": country = "Russian Federation"; break;
            case "www.myprotein.com.sg": country = "Singapore"; break;
            case "m.myprotein.com.sg": country = "Singapore"; break;
            case "www.myprotein.sk": country = "Slovakia"; break;
            case "m.myprotein.sk": country = "Slovakia"; break;
            case "si.myprotein.com": country = "Slovenia"; break;
            case "m.si.myprotein.com": country = "Slovenia"; break;
            case "www.myprotein.se": country = "Sweden"; break;
            case "m.myprotein.se": country = "Sweden"; break;
            case "www.myprotein.tw": country = "Taiwan"; break;
            case "m.myprotein.tw": country = "Taiwan"; break;
            case "www.myprotein.com.ua": country = "Ukraine"; break;
            case "m.myprotein.com.ua": country = "Ukraine"; break;
            case "us.myprotein.com": country = "United States"; break;
            case "m.us.myprotein.com": country = "United States"; break;
            case "fr-ca.myprotein.com": country = "Canada"; break;
            case "m.fr-ca.myprotein.com": country = "Canada"; break;
            case "www.myprotein.co.za": country = "South Africa"; break;
            case "m.myprotein.co.za": country = "South Africa"; break;
            default: country = "United Kingdom"; break;
        }
            return country;
    }
}
/*
*
	Domínios usados pela Myprotein

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

        //Mobile sites

		<link rel="alternate" hreflang="en-ae" href="https://m.myprotein.ae/"/>
		<link rel="alternate" hreflang="de-at" href="https://m.myprotein.at/home.dept"/
		<link rel="alternate" hreflang="en-au" href="https://m.au.myprotein.com/home.dept"/>
		<link rel="alternate" hreflang="az-az" href="https://m.myprotein.az/home.dept"/>
		<link rel="alternate" hreflang="bs-ba" href="https://m.myprotein.ba/home.dept"/>
		<link rel="alternate" hreflang="nl-be" href="https://m.myprotein.be/home.dept"/>
		<link rel="alternate" hreflang="bg-bg" href="https://m.myprotein.bg/home.dept"/>
		<link rel="alternate" hreflang="en-ca" href="https://m.ca.myprotein.com/home.dept"/>
		<link rel="alternate" hreflang="de-ch" href="https://m.myprotein.ch/home.dept"/>
		<link rel="alternate" hreflang="zh-cn" href="https://m.myprotein.cn/home.dept"/>
		<link rel="alternate" hreflang="cs-cz" href="https://m.myprotein.cz/home.dept"/>
		<link rel="alternate" hreflang="el-cy" href="https://m.myprotein.com.cy/home.dept"/>
		<link rel="alternate" hreflang="da-dk" href="https://m.myprotein.dk/home.dept"/>
		<link rel="alternate" hreflang="de-de" href="https://m.de.myprotein.com/home.dept"/>
		<link rel="alternate" hreflang="et-ee" href="https://m.myprotein.ee/home.dept"/>
		<link rel="alternate" hreflang="el-gr" href="https://m.myprotein.gr/home.dept"/>
		<link rel="alternate" hreflang="en-gb" href="https://m.myprotein.com/home.dept"/>
		<link rel="alternate" hreflang="es-es" href="https://m.myprotein.es/home.dept"/>
		<link rel="alternate" hreflang="fi-fi" href="https://m.myprotein.fi/home.dept"/>
		<link rel="alternate" hreflang="fr-fr" href="https://m.fr.myprotein.com/home.dept"/>
		<link rel="alternate" hreflang="en-hk" href="https://m.myprotein.com.hk/home.dept"/>
		<link rel="alternate" hreflang="hr-hr" href="https://m.myprotein.hr/home.dept"/>
		<link rel="alternate" hreflang="hu-hu" href="https://m.myprotein.hu/home.dept"/>
		<link rel="alternate" hreflang="en-ie" href="https://m.myprotein.ie/home.dept"/>
		<link rel="alternate" hreflang="en-in" href="https://m.myprotein.co.in/home.dept"/>
		<link rel="alternate" hreflang="it-it" href="https://m.myprotein.it/home.dept"/>
		<link rel="alternate" hreflang="ja-jp" href="https://m.myprotein.jp/home.dept"/>
		<link rel="alternate" hreflang="ko-kr" href="https://m.myprotein.co.kr/home.dept"/>
		<link rel="alternate" hreflang="lt-lt" href="https://m.myprotein.lt/home.dept"/>
		<link rel="alternate" hreflang="lv-lv" href="https://m.myprotein.lv/home.dept"/>
		<link rel="alternate" hreflang="nb-no" href="https://m.myprotein.no/home.dept"/>
		<link rel="alternate" hreflang="nl-nl" href="https://m.nl.myprotein.com/home.dept"/>
		<link rel="alternate" hreflang="en-nz" href="https://m.nz.myprotein.com/home.dept"/>
		<link rel="alternate" hreflang="pl-pl" href="https://m.myprotein.pl/home.dept"/>
		<link rel="alternate" hreflang="pt-pt" href="https://m.pt.myprotein.com/home.dept"/>
		<link rel="alternate" hreflang="ro-ro" href="https://m.myprotein.ro/home.dept"/>
		<link rel="alternate" hreflang="sr-rs" href="https://m.myprotein.rs/home.dept"/>
		<link rel="alternate" hreflang="ru-ru" href="https://m.myprotein.ru/home.dept"/>
		<link rel="alternate" hreflang="en-sg" href="https://m.myprotein.com.sg/home.dept"/>
		<link rel="alternate" hreflang="sk-sk" href="https://m.myprotein.sk/home.dept"/>
		<link rel="alternate" hreflang="sl-si" href="https://m.si.myprotein.com/home.dept"/>
		<link rel="alternate" hreflang="sv-se" href="https://m.myprotein.se/home.dept"/>
		<link rel="alternate" hreflang="zh-tw" href="https://m.myprotein.tw/"/>
        <link rel="alternate" hreflang="uk-ua" href="https://m.myprotein.com.ua/home.dept"/>
		<link rel="alternate" hreflang="en-us" href="https://m.us.myprotein.com/home.dept"/>
		<link rel="alternate" hreflang="fr-ca" href="https://m.fr-ca.myprotein.com/home.dept"/>
		<link rel="alternate" hreflang="en-za" href="https://m.myprotein.co.za/home.dept"/>


*
*
* */