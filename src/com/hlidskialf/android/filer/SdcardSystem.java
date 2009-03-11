package com.hlidskialf.android.filer;

import java.io.File;

class SdcardSystem extends FilerSystem {
  private File mFile;
  private String mRootPath;

  private File make_file(String path) {
    return new File(path == null ? mRootPath : mRootPath + File.pathSeparator + path);
  }

  public SdcardSystem(String root_path) {
    mRootPath = root_path;
  }

  public String[] list(String path) {
    File f = make_file(path);
    return f == null ? null : f.list();
  }
  public boolean delete(String path) {
    File f = make_file(path);
    return f == null ? null : f.delete();
  }
  public boolean exists(String path) {
    File f = make_file(path);
    return f == null ? null : f.exists();
  }
  public boolean mkdir(String path) {
    File f = make_file(path);
    return f == null ? null : f.mkdirs();
  }
  public boolean renameTo(String path, String dest) {
    File fsrc = make_file(path);
    File fdest = make_file(dest);
    return (fsrc == null || fdest == null) ? null : fsrc.renameTo(fdest);
  }
  public long getLength(String path) {
    File f = make_file(path);
    return f == null ? null : f.length();
  }
  public long getLastModified(String path) {
    File f = make_file(path);
    return f == null ? null : f.lastModified();
  }
  public boolean isDirectory(String path) {
    File f = make_file(path);
    return f == null ? null : f.isDirectory();
  }
  public String getName(String path) {
    int idx = path.lastIndexOf(java.io.File.pathSeparator);
    return path.substring(idx+1);
  }
  public String getParent(String path) {
    File f = make_file(path);
    return f == null ? null : f.getParent();
  }

}

