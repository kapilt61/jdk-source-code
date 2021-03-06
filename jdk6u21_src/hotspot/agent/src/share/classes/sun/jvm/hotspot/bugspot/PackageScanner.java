/*
 * Copyright (c) 2002, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package sun.jvm.hotspot.bugspot;

import java.io.*;

/** Scans a .java file for the package that it is in. */

public class PackageScanner {

  public PackageScanner() {
  }

  public String scan(String filename) {
    return scan(new File(filename));
  }

  /** Returns the String comprising the package name of the classes in
      this .java file. Returns the (non-null) empty string if any
      error occurs or if the classes are in the unnamed package. */
  public String scan(File file) {
    BufferedReader buf = null;
    String res = "";
    try {
      buf = new BufferedReader(new FileReader(file));
      StreamTokenizer tok = new StreamTokenizer(buf);
      tok.slashStarComments(true);
      tok.slashSlashComments(true);
      if (tok.nextToken() != StreamTokenizer.TT_WORD) {
        return res;
      }
      if (!tok.sval.equals("package")) {
        return res;
      }
      if (tok.nextToken() != StreamTokenizer.TT_WORD) {
        return res;
      }
      res = tok.sval;
      return res;
    } catch (FileNotFoundException e) {
      return res;
    } catch (IOException e) {
      return res;
    } finally {
      try {
        if (buf != null) {
          buf.close();
        }
      } catch (IOException e) {
      }
    }
  }

  public static void main(String[] args) {
    if (args.length != 1) {
      usage();
    }

    System.out.println(new PackageScanner().scan(args[0]));
  }

  private static void usage() {
    System.err.println("Usage: java PackageScanner <.java file name>");
    System.err.println("Prints package the .java file is in to stdout.");
    System.exit(1);
  }
}
