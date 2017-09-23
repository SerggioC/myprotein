package com.cruz.sergio.myproteinpricechecker;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.annotation.LayoutRes;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import static android.content.res.Configuration.SCREENLAYOUT_SIZE_XLARGE;
import static com.cruz.sergio.myproteinpricechecker.MainActivity.mNavigationView;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {
    static String deviceLanguage;
    static String deviceCountry;
    static String deviceCurrency;
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);

                if (preference.getKey().equals("prz_website_location")) {
                    ListPreference langpreference = (ListPreference) preference.getPreferenceManager().findPreference("prz_language");
                    String[] lang_entries = new String[]{"English", "Français", "Español", "Português", "Italiano"};
                    String[] lang_entryValues = new String[]{"en", "fr", "es", "pt", "it"};

                    List<String> ww_arr = Arrays.asList("AL", "DZ", "AR", "AM", "AU", "BA", "BO", "BW", "CV", "CM", "CA", "ECT",
                            "CL", "CO", "CU", "DO", "EC", "EG", "MK", "FO", "GP", "GT", "GG", "GF", "HT", "HK", "IN", "ID",
                            "IM", "IL", "JM", "JP", "JE", "LB", "LI", "MO", "MY", "MQ", "UM", "MC", "ME", "MA", "MZ", "MX",
                            "NZ", "NI", "NG", "NO", "PA", "PY", "PE", "PH", "PR", "RE", "BL", "SA", "RS", "SG", "SO", "ZA",
                            "LK", "ST", "SN", "TW", "TH", "TL", "TN", "TR", "AE", "US", "UY", "VE");
                    if (ww_arr.contains(stringValue)) {
                        lang_entries = new String[]{"English", "Français", "Español", "Português", "Italiano"};
                        lang_entryValues = new String[]{"en", "fr", "es", "pt", "it"};
                    }
                    List<String> ao_arr = Arrays.asList("AO", "PT");
                    if (ao_arr.contains(stringValue)) {
                        lang_entries = new String[]{"Português", "English"};
                        lang_entryValues = new String[]{"pt", "en"};
                    }
                    List<String> ru_arr = Arrays.asList("AZ", "BY", "GE", "KZ", "MD", "RU", "UA", "UZ");
                    if (ru_arr.contains(stringValue)) {
                        lang_entries = new String[]{"Русский", "English"};
                        lang_entryValues = new String[]{"ru", "en"};
                    }
                    if (stringValue.contains("BE")) {
                        lang_entries = new String[]{"Français", "Nederlands", "English"};
                        lang_entryValues = new String[]{"fr", "nl", "en"};
                    }
                    if (stringValue.contains("BR")) {
                        lang_entries = new String[]{"Português"};
                        lang_entryValues = new String[]{"pt"};
                    }
                    List<String> eu_arr = Arrays.asList("BG", "CY", "CZ", "EE", "HR", "IS", "LV", "LT", "HU", "MT", "RO", "SI");
                    if (eu_arr.contains(stringValue)) {
                        lang_entries = new String[]{"English", "Français", "Español", "Português", "Italiano", "Ελληνικά"};
                        lang_entryValues = new String[]{"en", "fr", "es", "pt", "it", "el"};
                    }
                    if (stringValue.contains("CN")) {
                        lang_entries = new String[]{"Mandarin", "English"};
                        lang_entryValues = new String[]{"zh", "en"};
                    }
                    if (stringValue.contains("DK")) {
                        lang_entries = new String[]{"Dansk", "English"};
                        lang_entryValues = new String[]{"da", "en"};
                    }
                    List<String> de_arr = Arrays.asList("DE", "AT");
                    if (de_arr.contains(stringValue)) {
                        lang_entries = new String[]{"Deutsch", "English"};
                        lang_entryValues = new String[]{"de", "en"};
                    }
                    if (stringValue.contains("ES")) {
                        lang_entries = new String[]{"Español", "English"};
                        lang_entryValues = new String[]{"es", "en"};
                    }
                    if (stringValue.contains("FR")) {
                        lang_entries = new String[]{"Français", "English"};
                        lang_entryValues = new String[]{"fr", "en"};
                    }
                    if (stringValue.contains("GR")) {
                        lang_entries = new String[]{"Ελληνικά", "English"};
                        lang_entryValues = new String[]{"el", "en"};
                    }
                    List<String> ie_arr = Arrays.asList("IE", "GB");
                    if (ie_arr.contains(stringValue)) {
                        lang_entries = new String[]{"English"};
                        lang_entryValues = new String[]{"en"};
                    }
                    if (stringValue.contains("IT")) {
                        lang_entries = new String[]{"Italiano", "English"};
                        lang_entryValues = new String[]{"it", "en"};
                    }
                    if (stringValue.contains("LU")) {
                        lang_entries = new String[]{"Français", "Deutsch", "Português", "English"};
                        lang_entryValues = new String[]{"fr", "de", "pt", "en"};
                    }
                    if (stringValue.contains("NL")) {
                        lang_entries = new String[]{"Nederlands", "English"};
                        lang_entryValues = new String[]{"nl", "en"};
                    }
                    if (stringValue.contains("PL")) {
                        lang_entries = new String[]{"Polski", "English"};
                        lang_entryValues = new String[]{"pl", "en"};
                    }
                    if (stringValue.contains("SK")) {
                        lang_entries = new String[]{"Slovak", "English"};
                        lang_entryValues = new String[]{"sk", "en"};
                    }
                    List<String> fi_se_arr = Arrays.asList("FI", "SE");
                    if (fi_se_arr.contains(stringValue)) {
                        lang_entries = new String[]{"Svenska", "English"};
                        lang_entryValues = new String[]{"sv", "en"};
                    }
                    if (stringValue.contains("CN")) {
                        lang_entries = new String[]{"English", "Français", "Deutsch", "Italiano", "Português"};
                        lang_entryValues = new String[]{"en", "fr", "de", "it", "pt"};
                    }

                    langpreference.setEntries(lang_entries);
                    langpreference.setEntryValues(lang_entryValues);
                    langpreference.setDefaultValue(lang_entryValues[0]);
                    langpreference.setValueIndex(0);
                    langpreference.setSummary(lang_entries[0]);
                }


            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

//    /*** Set up the {@link android.app.ActionBar}, if the API is available.     */
//    private void setupActionBar() {
//        ActionBar actionBar = getSupportActionBar();
//        Log.i("Sergio>>>", "setupActionBar: actionbar= " + actionBar);
//        if (actionBar != null) {
//            // Show the Up button in the action bar.
//            actionBar.setDisplayHomeAsUpEnabled(true);
//        }
//    }

    /**
     * MyHelper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {

        switch (preference.getKey()) {
            case "mp_website_location": {
                preference.setDefaultValue(deviceLanguage);
                ((ListPreference) preference).setValue(deviceLanguage);
                break;
            }
            case "mp_shipping_location": {
                preference.setDefaultValue(deviceCountry);
                ((ListPreference) preference).setValue(deviceCountry);
                break;
            }
            case "mp_currencies": {
                preference.setDefaultValue(deviceCurrency);
                ((ListPreference) preference).setValue(deviceCurrency);
                break;
            }
            case "prz_website_location": {
                preference.setDefaultValue(deviceCountry);
                ((ListPreference) preference).setValue(deviceCountry);
                break;
            }
            case "prz_language": {
                preference.setDefaultValue(deviceCountry.toLowerCase());
                ((ListPreference) preference).setValue(deviceCountry.toLowerCase());
                break;
            }
        }

        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setupActionBar();
        Locale local = getCurrentLocale();
        Log.i("Sergio>", this + "onCreate: local= " + local + " country= " + local.getCountry());
        deviceCountry = local.getCountry(); // PT
        deviceLanguage = local.toString().toLowerCase().replace("_", "-"); // pt_PT -> pt-pt
        deviceCurrency = Currency.getInstance(local).toString();

        Log.i("Sergio>", this + " onCreate\ndeviceCountry= " + deviceCountry + " deviceLanguage= " + deviceLanguage + " deviceCurrency= " + deviceCurrency);
    }

    @TargetApi(Build.VERSION_CODES.N)
    public Locale getCurrentLocale() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return getResources().getConfiguration().getLocales().get(0);
        } else {
            //noinspection deprecation
            return getResources().getConfiguration().locale;

        }
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);

//        // To fit bellow the statusbar
//        View decorView = getWindow().getDecorView();
//        decorView.setFitsSystemWindows(false);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//            decorView.requestFitSystemWindows();
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNavigationView.getMenu().findItem(R.id.nav_item_settings).setChecked(false);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            int menuId = bundle.getInt("menuId");
            mNavigationView.setCheckedItem(menuId);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {

        int screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        if (screenSize >= SCREENLAYOUT_SIZE_XLARGE) {
            loadHeadersFromResource(R.xml.pref_headers, target);
        } else {
            AllSettingsPreferenceFragment fragment = new AllSettingsPreferenceFragment();
            getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
        }

    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || DataSyncPreferenceFragment.class.getName().equals(fragmentName)
                || NotificationPreferenceFragment.class.getName().equals(fragmentName)
                || AllSettingsPreferenceFragment.class.getName().equals(fragmentName);
    }

    public static class AllSettingsPreferenceFragment extends PreferenceFragment {

        Activity settingsActivity;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_all);
            settingsActivity = getActivity();

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design guidelines.
            bindPreferenceSummaryToValue(findPreference("mp_website_location"));
            bindPreferenceSummaryToValue(findPreference("mp_shipping_location"));
            bindPreferenceSummaryToValue(findPreference("mp_currencies"));
            bindPreferenceSummaryToValue(findPreference("prz_website_location"));
            bindPreferenceSummaryToValue(findPreference("prz_language"));
            bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
            bindPreferenceSummaryToValue(findPreference("sync_frequency"));

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.activity_settings, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            Toolbar toolbar = (Toolbar) settingsActivity.findViewById(R.id.toolbar);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    settingsActivity.finish();
                }
            });

            int dpvalue = 6;
            float pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpvalue, getResources().getDisplayMetrics());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                toolbar.setElevation(pixels);
            } else {
                ViewCompat.setElevation(toolbar, pixels);
            }
        }
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design guidelines.
            bindPreferenceSummaryToValue(findPreference("mp_website_location"));
            bindPreferenceSummaryToValue(findPreference("mp_shipping_location"));
            bindPreferenceSummaryToValue(findPreference("mp_currencies"));
            bindPreferenceSummaryToValue(findPreference("prz_website_location"));
            bindPreferenceSummaryToValue(findPreference("prz_language"));
            bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
            bindPreferenceSummaryToValue(findPreference("sync_frequency"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DataSyncPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_data_sync);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("sync_frequency"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}