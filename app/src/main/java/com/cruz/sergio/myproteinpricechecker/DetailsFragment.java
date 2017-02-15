package com.cruz.sergio.myproteinpricechecker;

import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DetailsFragment extends Fragment {


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle extras = getArguments();
        if (extras != null) {
            String url = extras.getString("url");
            ((TextView) getActivity().findViewById(R.id.textViewDetails)).append("\n" + url);
            AsyncTask<String, Void, Document> getProductPage = new getProductPage();
            getProductPage.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, url);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        return inflater.inflate(R.layout.details_fragment_layout, null);

    }

    @Override
    public void onPause() {
        super.onPause();
        FragmentTransaction ft = MainActivity.mFragmentManager.beginTransaction();
        ft.hide(getParentFragment());
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        ft.show(SearchFragment.thisSearchFragment);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    private class getProductPage extends AsyncTask<String, Void, Document> {
        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected Document doInBackground(String... params) {
            Document resultDocument = null;
            try {
                resultDocument = Jsoup.connect(params[0])
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36")
                        .timeout(5000)
                        .maxBodySize(0) //sem limite de tamanho do doc recebido
                        .get();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return resultDocument;
        }

        @Override
        protected void onPostExecute(Document resultDocument) {
            super.onPostExecute(resultDocument);

            Elements productDetails = resultDocument.getElementsByClass("media"); // DIV com imagem e as opções do produto

            Elements titleElem = resultDocument.getElementsByClass("product-title");
            String title = titleElem.first().text();


            ArrayList<HashMap> arraylistHashMap = new ArrayList<>();

            Elements productVariations = resultDocument.getElementsByClass("productVariations__select");
            for (Element option : productVariations) {
                Log.i("Sergio>>>", "onPostExecute: " + option.attr("id"));
                Elements optionBoxes = option.getElementsByAttribute("value");
                HashMap<String, String> hmap = new HashMap<>();
                for (Element optionBox_i : optionBoxes) {
                    Log.i("Sergio>>>", "onPostExecute: " + optionBox_i.attr("value"));
                    Log.d("Sergio>>>", "onPostExecute: " + optionBox_i.text());
                    hmap.put(optionBox_i.attr("value"), optionBox_i.text());
                }
                arraylistHashMap.add(hmap);
            }

            Log.w("Sergio>>>", "onPostExecute: arraylistHashMap" + arraylistHashMap);



            for (HashMap map : arraylistHashMap) {

                ArrayList<String> spinnerArray = new ArrayList();

                Iterator it = map.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    spinnerArray.add(pair.getValue().toString());
                    it.remove(); // avoids a ConcurrentModificationException
                }

                Spinner spinner = new Spinner(getActivity());
                //Spinner spinner = (Spinner) getActivity().findViewById(spinner);
                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, spinnerArray);
                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(spinnerArrayAdapter);
                spinner.
                RelativeLayout relativeLayout = (RelativeLayout) getActivity().findViewById(R.id.relativeDetails);
                relativeLayout.addView(spinner);

            }
            ((TextView) getActivity().findViewById(R.id.textViewDetails)).append("\n" + title + "\n");
        }
    }

}









