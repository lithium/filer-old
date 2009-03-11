package com.hlidskialf.android.filer;

import android.net.Uri;

  
public abstract class FilerSystem {
  public interface YankListener {
    public boolean onYank(String path);
    public boolean onUnyank(String path);
    public boolean isYanked(String path);
  }
  public interface MimeListener {
    public String onLookupMime(String path);
  }

  public FilerSystem() {}
  public FilerSystem(String root_path) {}

  abstract String[] list(String path);

  abstract public boolean delete(String path);
  abstract public boolean exists(String path);
  abstract public boolean mkdir(String path);
  abstract public boolean renameTo(String path, String dest);

  abstract public boolean isDirectory(String path);
  abstract public String getName(String path);
  abstract public String getParent(String path);
  abstract public long getLength(String path);
  abstract public long getLastModified(String path);
}

