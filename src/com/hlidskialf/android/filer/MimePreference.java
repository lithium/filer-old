package com.hlidskialf.android.filer;

import android.preference.DialogPreference;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import android.util.AttributeSet;
import android.app.AlertDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ListView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Button;

class MimePreference extends DialogPreference
{
    public static final String DEFAULT_MIME_DATA="text/html.html,text/html.htm,text/*.txt,image/*.png,image/*.gif,image/*.jpg,image/*.bmp,video/*.mpg,video/*.wmv,video/*.mov,video/*.avi,audio/*.wav,audio/*.mp3,audio/*.wma,audio/*.ogg";

    private SharedPreferences mPrefs;
    private Hashtable<String,String> mMap;
    private LayoutInflater mInflater;
    private Context mContext;

    protected class MimeRow extends Object { public String mime; public String ext; }
    protected class MimeAdapter extends BaseAdapter {
        private ArrayList<Object> keys;
        public MimeAdapter() {
          keys = new ArrayList(mMap.keySet());

          Collections.sort(keys, new Comparator() {
              public int compare(Object a, Object b) { return ((String)a).compareToIgnoreCase((String)b); }
              public boolean equals(Object o) { return false; }
          });
        }
        public View getView(int position, View convertView, ViewGroup parent) {
          View ret = mInflater.inflate(R.layout.mime_list_item, parent, false);
          TextView tv;
          MimeRow row = (MimeRow)getItem(position);
          tv = (TextView)ret.findViewById(R.id.mime_list_item_ext);
          tv.setText(row.ext);
          tv = (TextView)ret.findViewById(R.id.mime_list_item_mime);
          tv.setText(row.mime);
          return ret;
        }
        public long getItemId(int position) {
            return position;
        }
        public Object getItem(int position) {
            MimeRow row = new MimeRow();
            row.ext = (String)keys.get(position);
            row.mime = mMap.get(row.ext);
            return row;
        }
        public int getCount() {
          return keys.size();
        }
    }

    public MimePreference(Context context, AttributeSet attrs)
    {
        super(context,attrs);
        mContext = context;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        mMap = mimesFromString( mPrefs.getString("mimes", DEFAULT_MIME_DATA) );

    }

    @Override
    protected void onBindDialogView(View view) {
        final ListView listView = (ListView)view.findViewById(R.id.mime_list);

        Button mAddButton = (Button)view.findViewById(R.id.mime_button_new);
        mAddButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
              final View layout = mInflater.inflate(R.layout.new_mime_dialog, null, false);
              new AlertDialog.Builder(mContext)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface di, int which) {
                      TextView tv;
                      tv = (TextView)layout.findViewById(R.id.new_mime_extension);
                      if (tv == null) return;
                      String ext = tv.getText().toString();
                      tv = (TextView)layout.findViewById(R.id.new_mime_mimetype);
                      if (tv == null) return;
                      String mime = tv.getText().toString();

                      mime = mime.trim();
                      if (!mime.contains("/"))
                        mime = mime.concat("/*");
                      ext = ext.trim();
                      if (ext.startsWith(".")) 
                        ext = ext.substring(1);

                      if (ext.length() > 0 && mime.length() > 0) {
                        mMap.put(ext, mime);
                        listView.setAdapter( new MimeAdapter() );
                      }
                    }
                })
                .setTitle(R.string.new_mime)
                .setView(layout)
                .show();
            }
        });

        listView.setAdapter( new MimeAdapter() );
    } 

    @Override
    protected void onDialogClosed(boolean positiveResult) {
      if (positiveResult) {
        mPrefs.edit().putString("mimes", stringFromMimes(mMap)).commit();
      }
    }
    


    public static String stringFromMimes(Hashtable<String,String> map)
    {
      Iterator it = map.keySet().iterator();
      StringBuilder ret = new StringBuilder();
      boolean first = true;
      while (it.hasNext()) {
        if (!first) ret.append(",");
        else first = false;
        String ext = (String)it.next();
        String mime = map.get(ext);
        ret.append(mime).append(".").append(ext);
      }
      return ret.toString();
    }
    public static Hashtable<String,String> mimesFromString(String data)
    {
        Hashtable<String,String> map = new Hashtable();
        if (data == null) return map;

        String[] rows = data.split(",");
        int i;
        for (i=0; i < rows.length; i++) {
            int idx = rows[i].lastIndexOf('.');
            if (idx == -1) continue;
            String mime = rows[i].substring(0,idx);
            String ext = rows[i].substring(idx+1);
            map.put(ext, mime);
        }
        return map;
    }
}
