package com.thaiopensource.relaxng.output.rng;

import java.io.Writer;
import java.io.IOException;
import java.util.Stack;

class XmlWriter {
  private String lineSep;
  private Writer w;
  private Stack tagStack = new Stack();
  private boolean inStartTag = false;
  private boolean inText = false;
  private int level = 0;
  private String[] topLevelAttributes;

  public XmlWriter(String lineSep, Writer w, String[] topLevelAttributes) {
    this.lineSep = lineSep;
    this.w = w;
    this.topLevelAttributes = topLevelAttributes;
  }

  void startElement(String name) {
    if (inStartTag) {
      maybeWriteTopLevelAttributes();
      inStartTag = false;
      write(">");
      newline();
    }
    if (inText)
      inText = false;
    else
      indent();
    write('<');
    write(name);
    tagStack.push(name);
    inStartTag = true;
    level++;
  }

  void endElement() {
    if (inStartTag) {
      maybeWriteTopLevelAttributes();
      level--;
      inStartTag = false;
      tagStack.pop();
      write("/>");
    }
    else {
      level--;
      if (inText)
        inText = false;
      else
        indent();
      write("</");
      write((String)tagStack.pop());
      write(">");
    }
    newline();
  }

  void attribute(String name, String value) {
    if (!inStartTag)
      throw new IllegalStateException("attribute outside of start-tag");
    write(' ');
    write(name);
    write('=');
    write('"');
    data(value);
    write('"');
  }

  void text(String s) {
    if (s.length() == 0)
      return;
    if (inStartTag) {
      maybeWriteTopLevelAttributes();
      inStartTag = false;
      write(">");
    }
    data(s);
    inText = true;
  }

  void data(String s) {
    int n = s.length();
    for (int i = 0; i < n; i++) {
      switch (s.charAt(i)) {
      case '<':
        write("&lt;");
        break;
      case '>':
        write("&gt;");
        break;
      case '&':
        write("&amp;");
        break;
      case '\r':
        write("&#xD;");
        break;
      case '\n':
        write(lineSep);
        break;
      default:
        write(s.charAt(i));
        break;
      }
    }
  }

  private void indent() {
    for (int i = 0; i < level; i++)
      write("  ");
  }

  private void newline() {
    write(lineSep);
  }

  private void maybeWriteTopLevelAttributes() {
    if (level != 1)
      return;
    for (int i = 0; i < topLevelAttributes.length; i += 2)
      attribute(topLevelAttributes[i], topLevelAttributes[i + 1]);
  }

  private void write(String s) {
    try {
      w.write(s);
    }
    catch (IOException e) {
      throw new WrappedException(e);
    }
  }

  private void write(char c) {
    try {
      w.write(c);
    }
    catch (IOException e) {
      throw new WrappedException(e);
    }
  }

  void close() {
    try {
      w.close();
    }
    catch (IOException e) {
      throw new WrappedException(e);
    }
  }

}