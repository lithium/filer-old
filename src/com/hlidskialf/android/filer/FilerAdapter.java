package com.hlidskialf.android.filer;

import android.widget.ArrayAdapter;
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import java.util.ArrayList;
import android.content.Context;

public class FilerAdapter extends ArrayAdapter<String> {
  private static final DecimalFormat decimal_format = new DecimalFormat("0.#");
  private static final SimpleDateFormat date_format_time = new SimpleDateFormat("MMM dd HH:mm");
  private static final SimpleDateFormat date_format_year  = new SimpleDateFormat("MMM dd yyyy");

  private ArrayList<String> mList = new ArrayList<String>();
  private FilerSystem mSystem;
  private FilerSystem.YankListener mYankListener;
  private FilerSystem.MimeListener mMimeListener;
  private LayoutInflater mInflater;
  private String mCurPath = null;

  public FilerAdapter(Context context, FilerSystem sys) {
    super(context, R.layout.file_list_item);
    mSystem = sys;

    mList = new ArrayList<String>(java.util.Arrays.asList( mSystem.list(null) ));
    java.util.Iterator<String> it = mList.iterator();
    while(it.hasNext()) {
      add(it.next());
    }

    mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  public void setYankListener(FilerSystem.YankListener yank_lstnr) { mYankListener = yank_lstnr; }
  public void setMimeListener(FilerSystem.MimeListener mime_lstnr) { mMimeListener = mime_lstnr; }

  @Override
  public View getView(int position, View v, ViewGroup parent) {
    String path = (String)mList.get(position);
    String name = mSystem.getName(path);

    android.util.Log.v("FilerAdapter/getView", path);

    if (v == null) {
      v = mInflater.inflate(R.layout.file_list_item, parent, false);
    }
    TextView txt = (TextView)v.findViewById(R.id.row_text);
    if (txt != null) txt.setText( name );
      
    if ( mYankListener != null && mYankListener.isYanked( path ) ) {
      v.setBackgroundResource(R.drawable.file_list_item_yanked);
    }
    else {
      v.setBackgroundResource(R.drawable.file_list_item_normal);
    }

    ImageView img = (ImageView)v.findViewById(R.id.row_icon);
    if (name.equals("..")) {
      img.setImageResource(android.R.drawable.ic_menu_revert);
    }
    else if (mSystem.isDirectory(path)) {
      img.setImageResource(android.R.drawable.ic_menu_more);
    }
    else {
      //TODO: set icon based on mimetype img.setImageResource();
      
      String mime = null;
      if (mMimeListener != null)
        mime = mMimeListener.onLookupMime(name);

      txt = (TextView)v.findViewById(R.id.row_type);
      txt.setText(mime != null ? mime : "text/*");

      txt = (TextView)v.findViewById(R.id.row_size);
      txt.setText( format_size(mSystem.getLength(path)) );

      txt = (TextView)v.findViewById(R.id.row_mtime);
      txt.setText( format_date(mSystem.getLastModified(path)) );
    }
    return v;
  }

  private static String format_size(long size)
  {
    String ret;
    if (size > 1024*1024*1024) ret = decimal_format.format((double)size / (double)(1024*1024*1024)) + "G";
    else if (size > 1024*1024) ret = decimal_format.format((double)size / (double)(1024*1024)) + "M";
    else if (size > 1024) ret = decimal_format.format((double)size / (double)1024) + "k";
    else ret = decimal_format.format(size) + "b";
    return ret;
  }

  private static String format_date(long when)
  {
    Date last = new Date(when);
    GregorianCalendar now = new GregorianCalendar();
    GregorianCalendar then = new GregorianCalendar();
    then.setTime(last);

    if (now.get(Calendar.YEAR) == then.get(Calendar.YEAR))
      return date_format_time.format(last);
    else
      return date_format_year.format(last);
  }
}
