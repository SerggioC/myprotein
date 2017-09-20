package com.cruz.sergio.myproteinpricechecker;

import android.os.AsyncTask;

import org.jsoup.nodes.Document;

/**
 * Created by Sergio on 15/09/2017.
 */

class MyvitaminsSearch extends AsyncTask<String, Void, Document> {
    int thisWebstoreIndex;

    public MyvitaminsSearch(int thisWebstoreIndex) {
        this.thisWebstoreIndex = thisWebstoreIndex;
    }

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
        return null;
    }
}
