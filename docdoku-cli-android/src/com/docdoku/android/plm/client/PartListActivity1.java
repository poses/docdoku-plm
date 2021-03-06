/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.android.plm.client;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.docdoku.android.plm.network.listeners.HttpGetListener;
import com.docdoku.android.plm.network.HttpGetTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 *
 * @author: Martin Devillers
 */
public class PartListActivity1 extends SearchActionBarActivity implements HttpGetListener {

    public static final String  LIST_MODE_EXTRA = "list mode";
    public static final int ALL_PARTS_LIST = 0;
    public static final int RECENTLY_VIEWED_PARTS_LIST = 1;
    public static final int PART_SEARCH = 2;
    public static final String SEARCH_QUERY_EXTRA = "search_light query";
    private static final String HISTORY_SIZE = "Part history_light size";
    private static final String PART_HISTORY_PREFERENCE = "part history_light";
    private static final int MAX_PARTS_IN_HISTORY = 15;

    private ListView partListView;
    private LinkedHashSet<String> partKeyHistory;
    private View loading;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_element_list);

        partListView = (ListView) findViewById(R.id.elementList);
        loading = findViewById(R.id.loading);

        getPartHistory();

        Intent intent = getIntent();
        int listCode = intent.getIntExtra(LIST_MODE_EXTRA, ALL_PARTS_LIST);
        switch (listCode){
            case ALL_PARTS_LIST:
                new HttpGetTask(this).execute("api/workspaces/" + getCurrentWorkspace() + "/parts/");
                break;
            case RECENTLY_VIEWED_PARTS_LIST:
                ((ViewGroup) loading.getParent()).removeView(loading);
                ArrayList<Part> partArray= new ArrayList<Part>();
                PartAdapter adapter = new PartAdapter(partArray);
                Iterator<String> iterator = partKeyHistory.iterator();
                while (iterator.hasNext()){
                    Part part = new Part(iterator.next());
                    partArray.add(part);
                    getPartInformation(part);
                }
                partListView.setAdapter(adapter);
                break;
            case PART_SEARCH:
                new HttpGetTask(this).execute("api/workspaces/" + getCurrentWorkspace() + "/parts/search_light/" + intent.getStringExtra(SEARCH_QUERY_EXTRA));
                break;
        }
    }

    @Override
    public void onHttpGetResult(String result) {
        if (loading !=null){
            ((ViewGroup) loading.getParent()).removeView(loading);
            loading = null;
        }
        ArrayList<Part> partsArray = new ArrayList<Part>();
        try {
            JSONArray partsJSON = new JSONArray(result);
            for (int i=0; i<partsJSON.length(); i++){
                JSONObject partJSON = partsJSON.getJSONObject(i);
                Part part = new Part(partJSON.getString("partKey"));
                partsArray.add(part.updateFromJSON(partJSON, getResources()));
            }
            partListView.setAdapter(new PartAdapter(partsArray));
        } catch (JSONException e) {
            Log.e("docdoku.DocDokuPLM", "Error handling json of workspace's parts");
            e.printStackTrace();
            Log.i("docdoku.DocDokuPLM", "Error message: " + e.getMessage());
        }
    }

    private void getPartHistory(){
        partKeyHistory = new LinkedHashSet<String>();
        SharedPreferences preferences = getSharedPreferences(getCurrentWorkspace() + PART_HISTORY_PREFERENCE, MODE_PRIVATE);
        int numPartsInHistory = preferences.getInt(HISTORY_SIZE, 0);
        for (int i = 0; i<numPartsInHistory; i++){
            Log.i("com.docdoku.android.plm.client","Retreiving key at position " + i + ": " + preferences.getString(Integer.toString(i),""));
            partKeyHistory.add(preferences.getString(Integer.toString(i), ""));
        }
    }

    private void updatePartHistory(String key){
        Log.i("com.docdoku.android.plm.client", "Adding part " + key +" to history_light");
        if (partKeyHistory.contains(key)) partKeyHistory.remove(key);
        partKeyHistory.add(key);
        SharedPreferences preferences = getSharedPreferences(getCurrentWorkspace() + PART_HISTORY_PREFERENCE, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Iterator<String> iterator = partKeyHistory.iterator();
        while (partKeyHistory.size() > MAX_PARTS_IN_HISTORY){
            iterator.next();
            iterator.remove();
        }
        editor.putInt(HISTORY_SIZE, partKeyHistory.size());
        int i = partKeyHistory.size()-1;
        while (iterator.hasNext()){
            String next = iterator.next();
            Log.i("com.docdoku.android.plm.client", "Storing key " + next + " in preferences at position " + i);
            editor.putString(Integer.toString(i), next);
            i--;
        }
        editor.commit();
    }

    private synchronized void getPartInformation(final Part part){
        new HttpGetTask(new HttpGetListener() {
            @Override
            public void onHttpGetResult(String result) {
                try {
                    JSONObject partJSON = new JSONObject(result);
                    part.updateFromJSON(partJSON, getResources());
                } catch (JSONException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }).execute("api/workspaces/" + getCurrentWorkspace() + "/parts/" + part.getKey());
    }

    @Override
    protected int getActivityButtonId() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private class PartAdapter extends BaseAdapter{

        private ArrayList<Part> parts;
        private final LayoutInflater inflater;

        public PartAdapter(ArrayList<Part> parts){
            this.parts = parts;
            inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return parts.size();
        }

        @Override
        public Object getItem(int i) {
            return parts.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View partRowView = inflater.inflate(R.layout.adapter_part, null);
            final Part part = parts.get(i);
            TextView reference = (TextView) partRowView.findViewById(R.id.number);
            reference.setText(part.getKey());
            TextView reservedBy = (TextView) partRowView.findViewById(R.id.checkOutUser);
            ImageView reservedPart = (ImageView) partRowView.findViewById(R.id.reservedPart);
            String reservedByName = part.getCheckOutUserName();
            if (reservedByName != null){
                String reservedByLogin = part.getCheckOutUserLogin();
                if (reservedByLogin.equals(getCurrentUserLogin())){
                    reservedPart.setImageResource(R.drawable.checked_out_current_user_light);
                }
                reservedBy.setText(reservedByName);
            }
            else{
                reservedBy.setText("");
                reservedPart.setImageResource(R.drawable.checked_in_light);
            }
            partRowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updatePartHistory(part.getKey());
                    Intent intent = new Intent(PartListActivity1.this, PartActivity.class);
                    intent.putExtra(PartActivity.PART_EXTRA,part);
                    startActivity(intent);
                }
            });
            return partRowView;
        }
    }

    /**
     * SearchActionBarActivity methods
     */
    @Override
    protected int getSearchQueryHintId() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void executeSearch(String query) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}