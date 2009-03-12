package com.hlidskialf.android.filer;

import android.widget.ArrayAdapter;
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
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
  private FilerSystem.FilerFilter mFilter;
  private FilerSystem.FilerComparator mComparator;
  private LayoutInflater mInflater;
  private String mCurPath = null;

  public FilerAdapter(Context context, FilerSystem sys) {
    super(context, R.layout.file_list_item);
    init(context,sys,null);
  }
  public FilerAdapter(Context context, FilerSystem sys, FilerSystem.FilerFilter filt, FilerSystem.FilerComparator comp) {
    super(context, R.layout.file_list_item);
    setComparator(comp);
    setFilter(filt);
    init(context,sys,null);
  }
  private void init(Context context, FilerSystem sys, String path)
  {
    if (context == null) throw new java.lang.IllegalArgumentException("Must have valid context");
    if (sys == null) throw new java.lang.IllegalArgumentException("Must have valid FilerSystem");
    mSystem = sys;
    mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    chdir(path);
  }

  public String pwd() { return mCurPath; }
  public boolean cdup() { 
    if (mCurPath == null) return false;
    int idx = mCurPath.lastIndexOf(mSystem.separator);
    return chdir(idx < 1 ? null : mCurPath.substring(0,idx), mFilter, mComparator);
  }
  public boolean cdto(String path) { return cdto(path,mFilter,mComparator); }
  public boolean cdto(String path, FilerSystem.FilerFilter filt, FilerSystem.FilerComparator comp) {
    if (mCurPath != null)
      path = mCurPath + mSystem.separator + path;
    if (!mSystem.isDirectory(path)) return false;

    return chdir(path, filt, comp);
  }
  public boolean chdir(String abs_path) { return chdir(abs_path,mFilter,mComparator); }
  public boolean chdir(String abs_path,FilerSystem.FilerFilter filt, FilerSystem.FilerComparator comp) 
  {
    mCurPath = abs_path;
    mList = mSystem.list(abs_path,filt,comp);

    mList.add(0,"..");

    clear();
    Iterator it = mList.iterator();
    while (it.hasNext()) {
      add((String)it.next());
    }
    return true;
  }

  public void setComparator(FilerSystem.FilerComparator comp) { mComparator = comp; } 
  public void setFilter(FilerSystem.FilerFilter filt) { mFilter = filt; }
  public void setYankListener(FilerSystem.YankListener yank_lstnr) { mYankListener = yank_lstnr; }
  public void setMimeListener(FilerSystem.MimeListener mime_lstnr) { mMimeListener = mime_lstnr; }

  @Override
  public View getView(int position, View v, ViewGroup parent) {
    String path = (String)mList.get(position);
    String name = mSystem.getName(path);

    if (v == null) {
      v = mInflater.inflate(R.layout.file_list_item, parent, false);
    }
    TextView txt = (TextView)v.findViewById(R.id.row_text);
    txt.setText( txt == null ? "" : name );
      
    if ( mYankListener != null && mYankListener.isYanked( path ) ) {
      v.setBackgroundResource(R.drawable.file_list_item_yanked);
    }
    else {
      v.setBackgroundResource(R.drawable.file_list_item_normal);
    }

    View detail = v.findViewById(R.id.row_detail);
    ImageView img = (ImageView)v.findViewById(R.id.row_icon);
    if (name.equals("..")) {
      img.setImageResource(android.R.drawable.ic_menu_revert);
      img.setVisibility(View.VISIBLE);
      if (detail != null) detail.setVisibility(View.INVISIBLE);
    }
    else if (mSystem.isDirectory(path)) {
      img.setImageResource(android.R.drawable.ic_menu_more);
      img.setVisibility(View.VISIBLE);
      if (detail != null) detail.setVisibility(View.INVISIBLE);
    }
    else {
      img.setVisibility(View.INVISIBLE);
      if (detail != null) detail.setVisibility(View.VISIBLE);
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
