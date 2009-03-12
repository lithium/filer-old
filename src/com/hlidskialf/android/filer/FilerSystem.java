package com.hlidskialf.android.filer;

import android.net.Uri;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

  
public abstract class FilerSystem {
  public interface YankListener {
    public boolean onYank(String path);
    public boolean onUnyank(String path);
    public boolean isYanked(String path);
  }
  public interface MimeListener {
    public String onLookupMime(String path);
  }
  public interface FilerFilter {
    public boolean accept(FilerSystem sys, String path);
  }
  public interface FilerComparator {
    public int compare(FilerSystem sys, String path_a, String path_b);
  }

  public static String separator = java.io.File.separator;

  public FilerSystem() {}
  public FilerSystem(String root_path) {}

  abstract String[] list(String path);
  public ArrayList<String> list(String path, FilerFilter filt, FilerComparator comp) {
    String[] ls = list(path);
    ArrayList<String> ret = new ArrayList<String>();
    int i;
    for (i=0; i < ls.length; i++) {
      if ((filt == null) || filt.accept(this, ls[i])) {
        ret.add(path == null ? ls[i] : path + FilerSystem.separator + ls[i]);
      }
    }
    if (comp != null) {
      final FilerComparator comparer = comp;
      Collections.sort(ret, new Comparator() {
        public int compare(Object a, Object b) {
          return comparer.compare(FilerSystem.this, (String)a, (String)b);
        }
      });
    }
    return ret;
  }

  abstract public boolean delete(String path);
  abstract public boolean exists(String path);
  abstract public boolean mkdir(String path);
  abstract public boolean renameTo(String path, String dest);

  abstract public String getPath(String path);
  abstract public String getName(String path);
  abstract public String getParent(String path);
  abstract public long getLength(String path);
  abstract public long getLastModified(String path);
  abstract public boolean isDirectory(String path);
  abstract public Uri getUri(String path);
}

